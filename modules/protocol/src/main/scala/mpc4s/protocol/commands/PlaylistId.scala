package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class PlaylistId(songId: Option[Id]) extends Command {
  val name = PlaylistId.name
}

object PlaylistId {
  val name = CommandName("playlistid")

  implicit def codec(implicit ic: LineCodec[Option[Id]]): LineCodec[PlaylistId] =
    (cs.commandName(name, ()) :<>: ic).dropUnits.as[PlaylistId]

  implicit val selectAnswer = SelectAnswer[PlaylistId, PlaylistAnswer]
}
