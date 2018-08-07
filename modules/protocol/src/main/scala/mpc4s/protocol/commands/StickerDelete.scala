package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class StickerDelete(uri: Uri, key: String) extends Command {
  val name = StickerDelete.name
}

object StickerDelete {
  val name = CommandName("sticker", "delete")

  // sticker can only be applied to songs
  implicit def codec: LineCodec[StickerDelete] =
    (cs.commandName(name, ()) :<>: cs.constant("song", ()) :<>:
      Uri.quotedStringCodec :<>: cs.stickerName).dropUnits.as[StickerDelete]

  implicit val selectAnswer = SelectAnswer[StickerDelete, Answer.Empty.type]
}
