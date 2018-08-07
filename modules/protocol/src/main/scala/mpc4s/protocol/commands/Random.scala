package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Random(state: Boolean) extends Command {
  val name = Random.name
}

object Random {
  val name = CommandName("random")

  implicit val codec: LineCodec[Random] =
    (cs.commandName(name, ()) :<>: cs.boolean).dropUnits.as[Random]

  implicit val selectAnswer: SelectAnswer[Random, Answer.Empty.type] =
    SelectAnswer[Random, Answer.Empty.type]
}
