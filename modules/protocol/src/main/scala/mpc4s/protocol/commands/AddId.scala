package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

/** Adds a song to the playlist (non-recursive) and returns the song id.
  */
case class AddId(uri: Uri, pos: Option[Int]) extends Command {
  val name = AddId.name
}


object AddId {

  val name = CommandName("addid")

  implicit def codec: LineCodec[AddId] = {
    (cs.commandName(name, ()) :<>: Uri.quotedStringCodec :: (cs.whitespace :: cs.int).dropUnits.head.option).dropUnits.as[AddId]
  }

  implicit val selectAnswer = SelectAnswer[AddId, AddIdAnswer]

}
