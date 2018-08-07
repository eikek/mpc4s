package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class AddTagId(song: Id, tag: Tag, value: String) extends Command {
  val name = AddTagId.name
}

object AddTagId {
  val name = CommandName("addtagid")

  implicit def codec(implicit tc: LineCodec[Tag], ic: LineCodec[Id]): LineCodec[AddTagId] =
    (cs.commandName(name, ()) :<>: ic :<>: tc :<>: cs.quotedString).dropUnits.as[AddTagId]

  implicit val selectAnswer = SelectAnswer[AddTagId, Answer.Empty.type]

}
