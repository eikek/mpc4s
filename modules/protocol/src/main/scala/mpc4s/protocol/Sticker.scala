package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Sticker(name: String, value: String)

object Sticker {

  implicit val codec: LineCodec[Sticker] =
    (cs.constant("sticker:", ()) :<>:
      cs.stickerName ::
      cs.constant("=", ()) ::
      cs.until("\n") ::
      cs.constant("\n", ())).dropUnits.as[Sticker]
}
