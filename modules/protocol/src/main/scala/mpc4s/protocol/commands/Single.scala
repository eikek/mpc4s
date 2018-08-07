package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Single(state: Boolean) extends Command {
  val name = Single.name
}

object Single {
  val name = CommandName("single")

  implicit def codec: LineCodec[Single] =
    (cs.commandName(name, ()) :<>: cs.boolean).dropUnits.as[Single]

  implicit val selectAnswer: SelectAnswer[Single, Answer.Empty.type] =
    SelectAnswer[Single, Answer.Empty.type]
}
