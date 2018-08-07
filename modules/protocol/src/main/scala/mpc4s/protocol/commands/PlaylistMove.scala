package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class PlaylistMove(playlist: String, from: Int, to: Int) extends Command {
  val name = PlaylistMove.name
}

object PlaylistMove {
  val name = CommandName("playlistmove")

  implicit val codec: LineCodec[PlaylistMove] =
    (cs.commandName(name, ()) :<>: cs.quotedString :<>: cs.int :<>: cs.int).dropUnits.as[PlaylistMove]

  implicit def selectAnswer = SelectAnswer[PlaylistMove, Answer.Empty.type]
}
