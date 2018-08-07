package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._
import mpc4s.protocol.internal.Case

private final class ConstantLineCodec[A](str: String, value: A, caseInsensitive: Boolean) extends LineCodec[A] {
  private val strLen = str.length

  def write(in: A): Result[String] =
    if (in == value) Result.successful(str)
    else Result.failure(ErrorMessage(s"Wrong value '$in' for encoding into '$str'"))

  def parse(in: String): Result[ParseResult[A]] = {
    val check =
      if (!caseInsensitive) in.startsWith(str)
      else Case.startsWithIgnoreCase(str, in) // in.toLowerCase.startsWith(strLower)

    if (check) Result.successful(ParseResult(value, in.substring(strLen)))
    else Result.failure(ErrorMessage(s"Expected next to be constant '$str' but got '$in'"))
  }

  // showed up in performance analysis…
//  override def toString() = s"ConstantLineCodec($str, $value)"
}
