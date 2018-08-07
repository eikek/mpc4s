package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class PlaylistDelete(playlist: String, pos: Int) extends Command {
  val name = PlaylistDelete.name
}

object PlaylistDelete {
  val name = CommandName("playlistclear")

  implicit val codec: LineCodec[PlaylistDelete] =
    (cs.commandName(name, ()) :<>: cs.quotedString :<>: cs.int).dropUnits.as[PlaylistDelete]

  implicit def selectAnswer = SelectAnswer[PlaylistDelete, Answer.Empty.type]
}
