package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class TagTypesEnable(tag: Tag, more: Vector[Tag]) extends Command {
  val name = TagTypesEnable.name
}

object TagTypesEnable {

  val name = CommandName("tagtypes", "enable")

  implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[TagTypesEnable] =
    (cs.commandName(name, ()) :<>: tc :<>: tc.repsep(cs.whitespace)).dropUnits.as[TagTypesEnable]

  implicit val selectAnswer = SelectAnswer[TagTypesEnable, Answer.Empty.type]
}
