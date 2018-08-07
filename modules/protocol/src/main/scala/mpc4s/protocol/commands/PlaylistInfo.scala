package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class PlaylistInfo(what: Option[Either[Range, Int]]) extends Command {
  val name = PlaylistInfo.name
}

object PlaylistInfo {
  val name = CommandName("playlistinfo")

  implicit def codec(implicit rc: LineCodec[Range]): LineCodec[PlaylistInfo] =
    (cs.commandName(name, ()) :: cs.option((cs.whitespace :: cs.fallback(rc, cs.int)).dropUnits.head)).dropUnits.as[PlaylistInfo]

  implicit val selectAnswer = SelectAnswer[PlaylistInfo, PlaylistAnswer]
}
