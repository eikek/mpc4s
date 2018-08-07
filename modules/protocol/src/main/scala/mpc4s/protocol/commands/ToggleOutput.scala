package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class ToggleOutput(id: Id) extends Command {
  val name = ToggleOutput.name
}

object ToggleOutput {
  val name = CommandName("toggleoutput")

  implicit def codec(implicit ic: LineCodec[Id]): LineCodec[ToggleOutput] =
    (codecs.commandName(name, ()) :<>: ic).dropUnits.as[ToggleOutput]

  implicit val selectAnswer = SelectAnswer[ToggleOutput, Answer.Empty.type]

}
