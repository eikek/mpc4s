package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._

private final class WhitespaceLineCodec(optional: Boolean) extends LineCodec[Unit] {

  def write(u: Unit): Result[String] =
    Result.successful(" ")

  def parse(in: String): Result[ParseResult[Unit]] = {
    val ws = in.takeWhile(c => c != '\n' && c.isWhitespace)
    if (ws.isEmpty && !optional) Result.failure(ErrorMessage(s"Expected some whitespace but got '$in'"))
    else Result.successful(ParseResult((), in.substring(ws.length)))
  }

  override def toString() = "WhitespaceLineCodec"
}

private[codecs] object WhitespaceLineCodec {

  val whitespace: LineCodec[Unit] =
    new WhitespaceLineCodec(false)

  val optional: LineCodec[Unit] =
    new WhitespaceLineCodec(true)
}
