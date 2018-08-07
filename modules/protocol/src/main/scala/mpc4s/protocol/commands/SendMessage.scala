package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class SendMessage(channel: String, text: String) extends Command {
  val name = SendMessage.name
}

object SendMessage {
  val name = CommandName("subscribe")

  implicit val codec: LineCodec[SendMessage] =
    (cs.commandName(name, ()) :<>: cs.quotedString :<>: cs.quotedString).dropUnits.as[SendMessage]

  implicit val selectAnswer = SelectAnswer[SendMessage, Answer.Empty.type]
}
