package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class ListPlaylist(playlist: String) extends Command {
  val name = ListPlaylist.name
}


object ListPlaylist {

  val name = CommandName("listplaylist")

  implicit val codec: LineCodec[ListPlaylist] =
    (codecs.commandName(name, ()) :<>: cs.quotedString.nonEmpty).dropUnits.as[ListPlaylist]

  implicit val selectAnswer = SelectAnswer[ListPlaylist, StoredPlaylistAnswer]

}
