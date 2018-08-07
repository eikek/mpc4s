package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Unmount(path: String) extends Command {
  val name = Unmount.name
}

object Unmount {
  val name = CommandName("unmount")

  implicit val codec: LineCodec[Unmount] =
    (cs.commandName(name, ()) :<>: cs.quotedString).dropUnits.as[Unmount]

  implicit val selectAnswer = SelectAnswer[Unmount, Answer.Empty.type]
}
