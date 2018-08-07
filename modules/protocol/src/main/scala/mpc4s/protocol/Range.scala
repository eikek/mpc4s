package mpc4s.protocol

import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Range(start: Int, end: Int)

object Range {

  implicit val codec: LineCodec[Range] =
    (cs.int :: cs.constant(":", ()) :: cs.int).dropUnits.as[Range]
}
