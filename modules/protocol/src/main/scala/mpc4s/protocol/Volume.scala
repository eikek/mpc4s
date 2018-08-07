package mpc4s.protocol

import mpc4s.protocol.codec.{codecs => cs, _}

case class Volume(n: Int)

object Volume {
  implicit val codec: LineCodec[Volume] =
    cs.int.xmap(Volume.apply, _.n)
}
