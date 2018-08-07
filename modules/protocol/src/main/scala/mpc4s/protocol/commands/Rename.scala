package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Rename(src: String, target: String) extends Command {
  val name = Rename.name
}

object Rename {
  val name = CommandName("rename")

  implicit val codec: LineCodec[Rename] =
    (cs.commandName(name, ()) :<>: cs.quotedString :<>: cs.quotedString).
      dropUnits.as[Rename]

  implicit val selectAnswer = SelectAnswer[Rename, Answer.Empty.type]
}
