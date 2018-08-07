package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Subscribe(channel: String) extends Command {
  val name = Subscribe.name
}

object Subscribe {
  val name = CommandName("subscribe")

  implicit val codec: LineCodec[Subscribe] =
    (cs.commandName(name, ()) :<>: cs.quotedString).dropUnits.as[Subscribe]

  implicit val selectAnswer = SelectAnswer[Subscribe, Answer.Empty.type]
}
