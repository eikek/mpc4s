package mpc4s.protocol.codec.codecs

import mpc4s.protocol.codec._

private final class QuotedStringLineCodec extends LineCodec[String] {

  private val needsQuote = """[\s"]+""".r

  def write(in: String): Result[String] = {
    if (in.contains("\n")) Result.failure(ErrorMessage(s"Input string '$in' contains a newline"))
    else if (needsQuote.findFirstIn(in).isDefined) Result.successful(quoteString(in))
    else Result.successful(in)
  }

  def parse(in: String): Result[ParseResult[String]] = {
    val (cur, rest) =
      if (!in.startsWith("\"")) untilWhitespace(in)
      else findNextQuote(in, 1) match {
        case -1 => untilWhitespace(in)
        case n => (in.substring(0, n + 1), in.substring(n + 1))
      }

    if (cur.startsWith("\"") && cur.endsWith("\"")) {
      Result.successful(ParseResult(unquoteString(cur), rest))
    } else {
      Result.successful(ParseResult(cur, rest))
    }
  }

  private def findNextQuote(in: String, start: Int): Int =
    in.indexOf("\"", start) match {
      case -1 => -1
      case n =>
        if (n == 0) n
        else in.charAt(n - 1) match {
          case '\\' => findNextQuote(in, n + 1)
          case _ => n
        }
    }

  private def untilWhitespace(in: String): (String, String) =
    in.indexWhere(_.isWhitespace) match {
      case -1 => (in, "")
      case n => (in.substring(0, n), in.substring(n))
    }

  private def quoteString(s: String): String =
    '"' + s.replace("\"", "\\\"") + '"'

  def unquoteString(s: String): String =
    s.drop(1).dropRight(1).replace("\\\"", "\"")

  override def toString() = s"QuotedStringLineCodec"
}
