package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class PlaylistAdd(playlist: String, song: Uri) extends Command {
  val name = PlaylistAdd.name
}

object PlaylistAdd {
  val name = CommandName("playlistadd")

  implicit def codec: LineCodec[PlaylistAdd] =
    (cs.commandName(name, ()) :<>: cs.quotedString :<>: Uri.quotedStringCodec).dropUnits.as[PlaylistAdd]

  implicit val selectAnswer = SelectAnswer[PlaylistAdd, Answer.Empty.type]
}
