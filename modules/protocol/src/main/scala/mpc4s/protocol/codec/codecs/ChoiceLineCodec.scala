package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._

private[codec] final class ChoiceLineCodec[A](first: LineCodec[A], rest: Seq[LineCodec[A]]) extends LineCodec[A] {

  val codecs: List[LineCodec[A]] = first :: rest.toList

  def write(in: A): Result[String] = {
    @annotation.tailrec
    def loop(cs: List[LineCodec[A]]): Result[String] =
      cs match {
        case c :: rest =>
          c.write(in) match {
            case r: Right[_,_] => r
            case l: Left[_,_] => loop(rest)
          }
        case Nil =>
          Result.failure(ErrorMessage(s"No choice from '$codecs' is applicable for input '$in'"))
      }

    loop(codecs)
  }

  def parse(in: String): Result[ParseResult[A]] = {
    @annotation.tailrec
    def loop(cs: List[LineCodec[A]]): Result[ParseResult[A]] =
      cs match {
        case c :: rest =>
          c.parse(in) match {
            case r: Right[_,_] => r
            case l: Left[_,_] => loop(rest)
          }
        case Nil =>
          Result.failure(ErrorMessage(s"No choice from '$codecs' is applicable for input '$in'"))
      }

    loop(codecs)
  }

  def append(c: LineCodec[A]): ChoiceLineCodec[A] =
    new ChoiceLineCodec(first, rest :+ c)

  override def toString() = s"ChoiceLineCodec($codecs)"
}
