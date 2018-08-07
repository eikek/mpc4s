package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs, _}

case class Seconds(n: Long) {
  require(n >= 0, "Seconds must be positive or 0")
}

object Seconds {

  implicit val secondsCodec: LineCodec[Seconds] =
    cs.long.exmap(
      n => Result.attempt(Seconds(n)),
      sec => Result.successful(sec.n))
}
