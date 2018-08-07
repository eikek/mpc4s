package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._

private final class UntilLineCodec(stop: String) extends LineCodec[String] {

  def write(in: String): Result[String] =
    if (in.contains(stop)) Result.failure(ErrorMessage(s"Input string '$in' contains stop word '$stop'"))
    else Result.successful(in)


  def parse(in: String): Result[ParseResult[String]] =
    in.indexOf(stop) match {
      case -1 => Result.failure(ErrorMessage(s"Stopword '$stop' not found in '$in'"))
      case n => Result.successful(ParseResult(in.substring(0, n), in.substring(n)))
    }

  override def toString() = s"UntilLineCodec($stop)"
}
