package mpc4s.protocol.codec

case class ParseResult[A](value: A, rest: String) {

  def map[B](f: A => B): ParseResult[B] =
    ParseResult(f(value), rest)
}
