package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class StickerFind(uri: Uri, key: String, value: Option[String]) extends Command {
  val name = StickerFind.name
}

object StickerFind {
  val name = CommandName("sticker", "find")

  implicit def codec: LineCodec[StickerFind] = {
    val optionalValue = (cs.whitespaceOptional ::
      cs.constant("=", ()) ::
      cs.whitespaceOptional ::
      cs.until("\n")).
      dropUnits.head.option

    (cs.commandName(name, ()) :<>:
      cs.constant("song", ()) :<>:
      Uri.quotedStringCodec :<>:
      cs.stickerName ::
      optionalValue).dropUnits.as[StickerFind]
  }

  implicit val selectAnswer = SelectAnswer[StickerFind, StickerFindAnswer]
}
