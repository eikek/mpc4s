package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class StickerGet(uri: Uri, key: String) extends Command {
  val name = StickerGet.name
}

object StickerGet {
  val name = CommandName("sticker", "get")

  // sticker can only be applied to songs
  implicit def codec: LineCodec[StickerGet] =
    (cs.commandName(name, ()) :<>: cs.constant("song", ()) :<>:
      Uri.quotedStringCodec :<>: cs.stickerName).dropUnits.as[StickerGet]

  implicit val selectAnswer = SelectAnswer[StickerGet, StickerAnswer]
}
