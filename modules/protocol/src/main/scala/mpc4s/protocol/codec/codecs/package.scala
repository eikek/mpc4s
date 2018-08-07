package mpc4s.protocol.codec

import shapeless._
import shapeless.ops.coproduct._
import scala.util.matching.Regex
import mpc4s.protocol._
import mpc4s.protocol.internal._
import mpc4s.protocol.codec.syntax._
import java.time.Instant

package object codecs {
  /** Parsing: When `c` fails to parse, return None, otherwise the
    * result in a Some. When writing: Write an empty string for a None
    * or use `c` for a Some.
    */
  def option[A](implicit c: LineCodec[A]): LineCodec[Option[A]] =
    new OptionLineCodec(c)

  val decimal: LineCodec[BigDecimal] =
    RegexLineCodec.decimal

  val double: LineCodec[Double] =
    decimal.exmap(
      bd => if (bd.isDecimalDouble) Result.successful(bd.toDouble)
      else Result.failure(ErrorMessage(s"Not a valid double: $bd")),
      d  => Result.successful(BigDecimal(d)))

  val bigint: LineCodec[BigInt] =
    RegexLineCodec.integer

  val int: LineCodec[Int] =
    bigint.exmap(
      bi => if (bi.isValidInt) Result.successful(bi.toInt)
      else Result.failure(ErrorMessage(s"Not a valid int: $bi")),
      i  => Result.successful(BigInt(i)))

  val long: LineCodec[Long] =
    bigint.exmap(
      bi => if (bi.isValidLong) Result.successful(bi.toLong)
      else Result.failure(ErrorMessage(s"Not a valid long: $bi")),
      i  => Result.successful(BigInt(i)))

  def boolean[A](c: LineCodec[A], trueValue: A, falseValue: A): LineCodec[Boolean] =
    c.xmap(a => a == trueValue, b => if (b) trueValue else falseValue)

  /** Uses the convention of mpd: 1 is true and 0 is false
    */
  val boolean: LineCodec[Boolean] =
    boolean(int, 1, 0)


  val instant: LineCodec[Instant] =
    regex("""[\d]{4}\-[\d]{2}\-[\d]{2}T[\d]{2}:[\d]{2}:[\d]{2}Z""".r).
      exmap(str => Result.attempt(Instant.parse(str)), i => Result.successful(i.toString))

  /** Parses only the empty string.
    */
  val empty: LineCodec[Unit] =
    LineCodec(str =>
      if (str.isEmpty) Result.successful(ParseResult((), str))
      else Result.failure(ErrorMessage("not empty")), _ => Result.successful(""))

  /** Ignores all input. When parsing, returns the input string, and writes an empty string.
    */
  val ignore: LineCodec[Unit] =
    LineCodec(str => Result.successful(ParseResult((), str)), _ => Result.successful(""))

  /** Consumes one or more whitespace (except '\n') when parseing, but
    * writes only a single space on writing.
    */
  val whitespace: LineCodec[Unit] =
    WhitespaceLineCodec.whitespace

  /** Consumes none or more whitespace (except '\n') when parseing,
    * writes a single space on writing.
    */
  val whitespaceOptional: LineCodec[Unit] =
    WhitespaceLineCodec.optional

  /** Return the input string as is when parsing and write any given string when writing.
    */
  val rest: LineCodec[String] =
    LineCodec(str => Result.successful(ParseResult(str, "")), str => Result.successful(str))

  /** Return the input string until end of line ('\n'). The eol char is
    * consumed but not returned. */
  val withEOL: LineCodec[String] =
    (until("\n") :: constant("\n", ())).dropUnits.head

  /** Parses the given string as is but case-insensitive and emits the
    * given value.
    */
  def constant[A](str: String, value: A): LineCodec[A] =
    new ConstantLineCodec(str, value, true)

  /** Parses the given string exactly as is (case-sensitive) and emits
    * the given value.
    */
  def constantCase[A](str: String, value: A): LineCodec[A] =
    new ConstantLineCodec(str, value, false)

  /** Parses only the empty string to return an empty vector. When
    * writing only empty vectors are allowed.
    */
  def emptyVector[A]: LineCodec[Vector[A]] =
    empty.exmap(_ => Result.successful(Vector.empty[A]),
      v => {
        if (v.isEmpty) Result.successful(())
        else Result.failure(ErrorMessage("Non-empty vector not allowed"))
      })

  /** If [[existing]] fails, try the [[empty]] codec mapping it to the
    * given empty element.
    */
  def allowEmpty[A](existing: LineCodec[A])(emptyA: A, isEmpty: A => Boolean): LineCodec[A] = {
    val whenEmpty: LineCodec[A] =
      empty.exmap(_ => Result.successful(emptyA),
        v => {
          if (isEmpty(v)) Result.successful(())
          else Result.failure(ErrorMessage("Non-empty elements not allowed"))
        })

    fallback(whenEmpty, existing).xmap(_.fold(identity,identity), { v =>
      if (isEmpty(v)) Left(v)
      else Right(v)
    })
  }

  def repeat[A](c: LineCodec[A], min: Int = 0): LineCodec[Vector[A]] =
    new RepeatLineCodec(c, min, ignore)

  def repsep[A](c: LineCodec[A], sep: LineCodec[Unit], min: Int = 0): LineCodec[Vector[A]] =
    new RepeatLineCodec(c, min, sep)

  def choice[A](c: LineCodec[A], cs: LineCodec[A]*): LineCodec[A] =
    c match {
      case ac: ChoiceLineCodec[A] =>
        ac.append(c)
      case _ =>
        new ChoiceLineCodec(c, cs)
    }

  def until(stop: String): LineCodec[String] =
    new UntilLineCodec(stop)

  val quotedString: LineCodec[String] =
    new QuotedStringLineCodec()

  def fallback[A,B](left: LineCodec[A], right: LineCodec[B])
    (implicit cte: CoproductToEither.Aux[A :+: B :+: CNil, Either[A,B]], etc: EitherToCoproduct.Aux[A, B, A :+: B :+: CNil]): LineCodec[Either[A,B]] = {
    (left :+: right).xmap[Either[A,B]](cp => cte(cp), e => etc(e))
  }

  def filter(f: Char => Boolean): LineCodec[String] =
    new FilterLineCodec(f)

  def charsIn(chars: Seq[Char]): LineCodec[String] =
    new FilterLineCodec(chars.toSet.contains)

  def charsNotIn(chars: Seq[Char]): LineCodec[String] =
    new FilterLineCodec(chars.toSet.contains _ andThen (b => !b))

  def regex(r: Regex): LineCodec[String] =
    new RegexLineCodec(r, str => Result.successful(str), str => Result.successful(str))

  def require[A](c: LineCodec[A], p: A => Boolean, errMsg: String = ""): LineCodec[A] = {
    val f: A => Result[A] = a =>
      if (p(a)) Result.successful(a)
      else Result.failure(ErrorMessage(if (errMsg.nonEmpty) errMsg else s"Value '$a' did not match predicate"))

    c.exmap(f, f)
  }

  def nonEmpty(c: LineCodec[String]): LineCodec[String] =
    require(c, _.nonEmpty, "Expected non-empty string, but it was empty")

  def trim(c: LineCodec[String]): LineCodec[String] =
    c.xmap(_.trim, _.trim)

  def alphanumAnd(chars: Seq[Char]): LineCodec[String] =
    charsIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ chars)

  def alphanum: LineCodec[String] =
    alphanumAnd(Seq.empty)

  def stickerName: LineCodec[String] =
    charsNotIn(" \t\n=")

  def commandName[A](name: CommandName, a: A): LineCodec[A] = {
    name.path match {
      case List(n) => constant(n, a)
      case ns => constant(ns.mkString(" "), a)
    }
  }

  def split(starting: Regex): LineCodec[Vector[String]] =
    new SplitLineCodec(starting)

  def splitSongs: LineCodec[Vector[String]] =
    SplitLineCodec.splitSongs

  def splitPlaylists: LineCodec[Vector[String]] =
    SplitLineCodec.splitPlaylists

  def splitTag: LineCodec[Vector[String]] =
    SplitLineCodec.splitTag

  def splitFiles: LineCodec[Vector[String]] =
    SplitLineCodec.splitFiles

  def splitOutputs: LineCodec[Vector[String]] =
    SplitLineCodec.splitOutputs

  def splitDecoderPlugins: LineCodec[Vector[String]] =
    SplitLineCodec.splitDecoderPlugins

  def map[A](d: LineCodec[Vector[String]], c: LineCodec[A]): LineCodec[Vector[A]] =
    d.exmap(
      vs => Result.flatten(vs.map(c.parse).map(_.map(_.value))),
      va => Result.flatten(va.map(c.write)))

  def transform[A](c: LineCodec[String], f: LineCodec[A]): LineCodec[A] =
    c.exmap(
      s => f.parse(s).map(_.value),
      a => f.write(a))

  // def nonEmptyIter[A <: Iterable[_]](c: LineCodec[A]): LineCodec[A] =
  //   require(c, _.nonEmpty, "Expected non-empty iterable, but it was empty")

  def dropUnits[K <: HList, L <: HList](codec: LineCodec[K])(implicit du: DropUnits.Aux[K, L]) =
    codec.xmap[L](du.removeUnits, du.addUnits)

  /** Parses one line into a key-value pair around separator. Repeats
    * this for multiple lines. Whitespace after the separator is
    * ignored.
    */
  def keyValue(sep: String): LineCodec[ListMap[ListMap.Key, String]] = {
    val nl = constant("\n", ())
    val ows = option(whitespace).xmap[Unit](_ => (), _ => Some(()))
    val key = until(sep).xmap[ListMap.Key](n => ListMap.key(n), _.name)

    (key :: constant(sep, ()) :: ows :: until("\n") :: nl).
      dropUnits.
      as[(ListMap.Key, String)].
      repeat.
      xmap(v => ListMap.from(v), _.toVector)
  }

  /** Parse one line into a key-value pair using mpds response syntax:
    * key: value. Repeat this for multiple lines.
    */
  val keyValue: LineCodec[ListMap[ListMap.Key, String]] = keyValue(":")
}
