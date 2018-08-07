package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class ReadComments(uri: Uri) extends Command {
  val name = ReadComments.name
}

object ReadComments {
  val name = CommandName("readcomments")

  implicit def codec: LineCodec[ReadComments] =
    (cs.commandName(name, ()) :<>: Uri.quotedStringCodec).dropUnits.as[ReadComments]

  implicit val selectAnswer = SelectAnswer[ReadComments, ReadCommentsAnswer]
}
