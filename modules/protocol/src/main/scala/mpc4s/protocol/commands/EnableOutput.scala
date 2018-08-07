package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class EnableOutput(id: Id) extends Command {
  val name = EnableOutput.name
}

object EnableOutput {
  val name = CommandName("enableoutput")

  implicit def codec(implicit ic: LineCodec[Id]): LineCodec[EnableOutput] =
    (codecs.commandName(name, ()) :<>: ic).dropUnits.as[EnableOutput]

  implicit val selectAnswer = SelectAnswer[EnableOutput, Answer.Empty.type]

}
