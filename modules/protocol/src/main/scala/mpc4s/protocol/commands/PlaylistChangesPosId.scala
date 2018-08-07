package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class PlaylistChangesPosId(version: Int, range: Option[Range]) extends Command {
  val name = PlaylistChangesPosId.name
}

object PlaylistChangesPosId {
  val name = CommandName("plchangesposid")

  implicit def codec(implicit rc: LineCodec[Option[Range]]): LineCodec[PlaylistChangesPosId] =
    (cs.commandName(name, ()) :<>: cs.int :<>: rc).dropUnits.as[PlaylistChangesPosId]

  implicit val selectAnswer = SelectAnswer[PlaylistChangesPosId, PlaylistAnswer]
}
