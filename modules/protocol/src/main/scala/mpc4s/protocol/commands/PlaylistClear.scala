package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class PlaylistClear(playlist: String) extends Command {
  val name = PlaylistClear.name
}

object PlaylistClear {
  val name = CommandName("playlistclear")

  implicit val codec: LineCodec[PlaylistClear] =
    (cs.commandName(name, ()) :<>: cs.quotedString).dropUnits.as[PlaylistClear]

  implicit def selectAnswer = SelectAnswer[PlaylistClear, Answer.Empty.type]
}
