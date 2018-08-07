package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class SwapId(id1: Id, id2: Id) extends Command {
  val name = SwapId.name
}

object SwapId {
  val name = CommandName("swapid")

  implicit def codec(implicit ic: LineCodec[Id]): LineCodec[SwapId] =
    (cs.commandName(name, ()) :<>: ic :<>: ic).dropUnits.as[SwapId]

  implicit val selectAnswer = SelectAnswer[SwapId, Answer.Empty.type]

}
