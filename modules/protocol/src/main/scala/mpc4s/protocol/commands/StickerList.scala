package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class StickerList(uri: Uri) extends Command {
  val name = StickerList.name
}

object StickerList {
  val name = CommandName("sticker", "list")

  implicit def codec: LineCodec[StickerList] =
    (cs.commandName(name, ()) :<>: cs.constant("song", ()) :<>: Uri.quotedStringCodec).dropUnits.as[StickerList]

  implicit val selectAnswer = SelectAnswer[StickerList, StickerListAnswer]
}
