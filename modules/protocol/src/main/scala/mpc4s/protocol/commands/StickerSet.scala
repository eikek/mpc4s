package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class StickerSet(uri: Uri, key: String, value: String) extends Command {
  val name = StickerSet.name
}

object StickerSet {
  val name = CommandName("sticker", "set")

  // sticker can only be applied to songs
  implicit def codec: LineCodec[StickerSet] =
    (cs.commandName(name, ()) :<>: cs.constant("song", ()) :<>:
      Uri.quotedStringCodec :<>: cs.stickerName :<>: cs.quotedString).dropUnits.as[StickerSet]

  implicit val selectAnswer = SelectAnswer[StickerSet, Answer.Empty.type]
}
