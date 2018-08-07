package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Consume(state: Boolean) extends Command {
  val name = Consume.name
}

object Consume {
  val name = CommandName("consume")

  implicit val codec: LineCodec[Consume] =
    (cs.commandName(name, ()) :<>: cs.boolean).dropUnits.as[Consume]

  implicit val selectAnswer: SelectAnswer[Consume, Answer.Empty.type] =
    SelectAnswer[Consume, Answer.Empty.type]
}
