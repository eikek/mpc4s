package mpc4s.protocol

import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class AudioFormat(freq: Int, bits: Int, channels: Int) {

  def asString = s"${freq}:${bits}:${channels}"

}

object AudioFormat {

  implicit val codec: LineCodec[AudioFormat] =
    (cs.int ::
      cs.constant(":", ()) ::
      cs.int ::
      cs.constant(":", ()) ::
      cs.int).dropUnits.as[AudioFormat]
}
