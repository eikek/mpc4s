package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Unsubscribe(channel: String) extends Command {
  val name = Unsubscribe.name
}

object Unsubscribe {
  val name = CommandName("unsubscribe")

  implicit val codec: LineCodec[Unsubscribe] =
    (cs.commandName(name, ()) :<>: cs.quotedString).dropUnits.as[Unsubscribe]

  implicit val selectAnswer = SelectAnswer[Unsubscribe, Answer.Empty.type]
}
