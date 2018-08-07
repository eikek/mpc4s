package mpc4s.protocol.codec.codecs

import scala.util.matching.Regex
import mpc4s.protocol.codec._

private[codec] final class RegexLineCodec[A](
  regex: Regex
    , p: String => Result[A]
    , w: A => Result[String]) extends LineCodec[A] {

  def write(in: A): Result[String] =
    w(in) match {
      case Right(s) =>
        if (regex.pattern.matcher(s).matches) Result.successful(s)
        else Result.failure(ErrorMessage(s"The string '$s' doesn't match regex '$regex'"))
      case l: Left[_, _] =>
        l
    }

  def parse(in: String): Result[ParseResult[A]] = {
    regex.findFirstIn(in) match {
      case Some(str) =>
        p(str).map(a => ParseResult(a, in.substring(str.length)))
      case None =>
        Result.failure(ErrorMessage(s"Input '$in' not starting with regex '$regex'"))
    }
  }

  override def toString() = s"RegexLineCodec($regex)"
}

private[codec] object RegexLineCodec {

  def decimal: LineCodec[BigDecimal] =
    new RegexLineCodec(
      """[\+-]{0,1}[0-9]*(\.[0-9]+)?(e[\+-]{0,1}[0-9]+)?""".r
        , str => Result.attempt(BigDecimal(str), s"Cannot convert '$str' into a decimal")
        , bd => Result.successful(bd.toString)
    )

  def integer: LineCodec[BigInt] =
    new RegexLineCodec(
      """[\+-]{0,1}[0-9]+""".r
        , str => Result.attempt(BigInt(str), s"Cannot convert '$str' into a integer")
        , bi => Result.successful(bi.toString)
    )

}
