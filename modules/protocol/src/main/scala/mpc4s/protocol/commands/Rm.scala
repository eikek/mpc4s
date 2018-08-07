package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Rm(playlist: String) extends Command {
  val name = Rm.name
}

object Rm {
  val name = CommandName("rm")

  implicit val codec: LineCodec[Rm] =
    (cs.commandName(name, ()) :<>: cs.quotedString).dropUnits.as[Rm]

  implicit val selectAnswer = SelectAnswer[Rm, Answer.Empty.type]
}
