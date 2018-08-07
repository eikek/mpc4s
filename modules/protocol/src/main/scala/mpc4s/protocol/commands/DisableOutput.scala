package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class DisableOutput(id: Id) extends Command {
  val name = DisableOutput.name
}


object DisableOutput {

  val name = CommandName("disableoutput")

  implicit def codec(implicit ic: LineCodec[Id]): LineCodec[DisableOutput] =
    (codecs.commandName(name, ()) :<>: ic).dropUnits.as[DisableOutput]

  implicit val selectAnswer = SelectAnswer[DisableOutput, Answer.Empty.type]

}
