package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class ListPlaylistInfo(playlist: String) extends Command {
  val name = ListPlaylistInfo.name
}

object ListPlaylistInfo {
  val name = CommandName("listplaylistinfo")

  implicit val codec: LineCodec[ListPlaylistInfo] =
    (codecs.commandName(name, ()) :<>: cs.quotedString).dropUnits.as[ListPlaylistInfo]

  implicit val selectAnswer = SelectAnswer[ListPlaylistInfo, SongListAnswer]

}
