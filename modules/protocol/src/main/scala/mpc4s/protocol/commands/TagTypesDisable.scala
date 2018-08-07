package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class TagTypesDisable(tag: Tag, more: Vector[Tag]) extends Command {
  val name = TagTypesDisable.name
}

object TagTypesDisable {

  val name = CommandName("tagtypes", "disable")

  implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[TagTypesDisable] =
    (cs.commandName(name, ()) :<>: tc :<>: tc.repsep(cs.whitespace)).dropUnits.as[TagTypesDisable]

  implicit val selectAnswer = SelectAnswer[TagTypesDisable, Answer.Empty.type]
}
