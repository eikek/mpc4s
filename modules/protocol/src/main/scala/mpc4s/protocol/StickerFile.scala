package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class StickerFile(file: String, sticker: Sticker)

object StickerFile {

  implicit def codec(implicit sc: LineCodec[Sticker]): LineCodec[StickerFile] = {

    val fileCodec = (cs.constant("file:", ()) ::
      cs.whitespaceOptional ::
      cs.until("\n") ::
      cs.constant("\n", ())).dropUnits.head

    (fileCodec :: sc).as[StickerFile]
  }
}
