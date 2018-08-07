package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

/** Deletes a song from the playlist
  */
case class DeleteId(songId: Id) extends Command {
  val name = DeleteId.name
}

object DeleteId {
  val name = CommandName("deleteid")

  implicit def codec(implicit ic: LineCodec[Id]): LineCodec[DeleteId] =
    (cs.commandName(name, ()) :<>: ic).dropUnits.as[DeleteId]

  implicit val selectAnswer = SelectAnswer[DeleteId, Answer.Empty.type]
}
