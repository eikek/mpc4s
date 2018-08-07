package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._

private final class OptionLineCodec[A](c: LineCodec[A]) extends LineCodec[Option[A]] {

  def write(in: Option[A]): Result[String] =
    in.map(c.write).
      getOrElse(Result.successful(""))

  def parse(in: String): Result[ParseResult[Option[A]]] = {
    c.parse(in) match {
      case Right(pr) =>  Right(pr.map(Option(_)))
      case Left(_) => Right(ParseResult(None, in))
    }
  }

  override def toString() = s"OptionLineCodec($c)"
}
