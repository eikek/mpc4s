package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class PlaylistSearch(tag: Tag, needle: String) extends Command {
  val name = PlaylistSearch.name
}

object PlaylistSearch {
  val name = CommandName("playlistsearch")

  implicit def codec(implicit rc: LineCodec[Tag]): LineCodec[PlaylistSearch] =
    (cs.commandName(name, ()) :<>: rc :<>: cs.until("\n")).dropUnits.as[PlaylistSearch]

  implicit val selectAnswer = SelectAnswer[PlaylistSearch, PlaylistAnswer]
}
