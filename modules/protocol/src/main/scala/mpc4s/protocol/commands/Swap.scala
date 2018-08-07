package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Swap(pos1: Int, pos2: Int) extends Command {
  val name = Swap.name
}

object Swap {
  val name = CommandName("swap")

  implicit val codec: LineCodec[Swap] =
    (cs.commandName(name, ()) :<>: cs.int :<>: cs.int).dropUnits.as[Swap]

  implicit val selectAnswer = SelectAnswer[Swap, Answer.Empty.type]

}
