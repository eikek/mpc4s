package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Pause(state: Boolean) extends Command {
  val name = Pause.name
}

object Pause {
  val name = CommandName("pause")

  implicit val codec: LineCodec[Pause] =
    (cs.commandName(name, ()) :<>: cs.boolean).dropUnits.as[Pause]

  implicit val selectAnswer: SelectAnswer[Pause, Answer.Empty.type] =
    SelectAnswer[Pause, Answer.Empty.type]
}
