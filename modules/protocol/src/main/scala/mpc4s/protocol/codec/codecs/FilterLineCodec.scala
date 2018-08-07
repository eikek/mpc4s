package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._

private final class FilterLineCodec(filter: Char => Boolean) extends LineCodec[String] {

  def write(in: String): Result[String] = {
    if (in.forall(filter)) Result.successful(in)
    else Result.failure(ErrorMessage(s"Input '$in' contains invalid characters according to predicate"))
  }

  def parse(in: String): Result[ParseResult[String]] = {
    val s = in.takeWhile(filter)
    if (s.isEmpty) Result.successful(ParseResult("", in))
    else Result.successful(ParseResult(s, in.substring(s.length)))
  }

  override def toString() = s"FilterLineCodec($filter)"
}
