package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

private final class RepeatLineCodec[A](c: LineCodec[A], min: Int, sep: LineCodec[Unit]) extends LineCodec[Vector[A]] {

  private val codec: LineCodec[A] = (sep :: c).dropUnits.head

  def write(in: Vector[A]): Result[String] =
    if (in.isEmpty) Result.successful("")
    else in.tail.foldLeft(c.write(in.head)) { (res, a) =>
      res.flatMap(s => codec.write(a).map(as => s + as))
    }

  def parse(in: String): Result[ParseResult[Vector[A]]] = {

    def loop(c: LineCodec[A], acc: ParseResult[Vector[A]]): Result[ParseResult[Vector[A]]] =
      c.parse(acc.rest) match {
        case Right(pr) =>
          loop(codec, pr.map(v => acc.value :+ v))
        case Left(err) =>
          if (acc.value.size >= min) Result.successful(acc)
          else Result.failure(ErrorMessage(s"Expected at least $min occurrences when decoding with $c, but got ${acc.value.size}"))
      }

    loop(c, ParseResult(Vector.empty, in))
  }

  override def toString() = s"RepeatLineCodec($c)"
}
