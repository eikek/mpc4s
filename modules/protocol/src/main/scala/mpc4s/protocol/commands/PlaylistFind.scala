package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class PlaylistFind(tag: Tag, needle: String) extends Command {
  val name = PlaylistFind.name
}

object PlaylistFind {
  val name = CommandName("playlistfind")

  implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[PlaylistFind] =
    (cs.commandName(name, ()) :<>: tc :<>: cs.until("\n").nonEmpty).dropUnits.as[PlaylistFind]

  implicit val selectAnswer = SelectAnswer[PlaylistFind, PlaylistAnswer]
}
