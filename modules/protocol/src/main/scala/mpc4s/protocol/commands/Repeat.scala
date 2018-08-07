package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Repeat(state: Boolean) extends Command {
  val name = Repeat.name
}

object Repeat {
  val name = CommandName("repeat")

  implicit def codec(implicit sc: LineCodec[Boolean]): LineCodec[Repeat] =
    (cs.commandName(name, ()) :<>: sc).dropUnits.as[Repeat]

  implicit val selectAnswer: SelectAnswer[Repeat, Answer.Empty.type] =
    SelectAnswer[Repeat, Answer.Empty.type]
}
