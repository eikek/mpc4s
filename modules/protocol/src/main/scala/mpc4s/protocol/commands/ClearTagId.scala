package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class ClearTagId(song: Id, tag: Tag) extends Command {
  val name = ClearTagId.name
}

object ClearTagId {
  val name = CommandName("addtagid")

  implicit def codec(implicit tc: LineCodec[Tag], ic: LineCodec[Id]): LineCodec[ClearTagId] =
    (cs.commandName(name, ()) :<>: ic :<>: tc).dropUnits.as[ClearTagId]

  implicit val selectAnswer = SelectAnswer[ClearTagId, Answer.Empty.type]

}
