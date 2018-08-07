package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class PlaylistChanges(version: Int, range: Option[Range]) extends Command {
  val name = PlaylistChanges.name
}

object PlaylistChanges {
  val name = CommandName("plchanges")

  implicit def codec(implicit rc: LineCodec[Option[Range]]): LineCodec[PlaylistChanges] =
    (cs.commandName(name, ()) :<>: cs.int :<>: rc).dropUnits.as[PlaylistChanges]

  implicit val selectAnswer = SelectAnswer[PlaylistChanges, PlaylistAnswer]
}
