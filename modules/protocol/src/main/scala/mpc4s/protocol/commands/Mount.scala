package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Mount(path: String, uri: Uri) extends Command {
  val name = Mount.name
}

object Mount {
  val name = CommandName("mount")

  implicit def codec: LineCodec[Mount] =
    (cs.commandName(name, ()) :<>: cs.quotedString :<>: Uri.quotedStringCodec).dropUnits.as[Mount]

  implicit val selectAnswer = SelectAnswer[Mount, Answer.Empty.type]
}
