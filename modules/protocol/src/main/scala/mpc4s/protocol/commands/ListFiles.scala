package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class ListFiles(uri: Option[Uri]) extends Command {
  val name = ListFiles.name
}

object ListFiles {
  val name = CommandName("listfiles")

  implicit def codec: LineCodec[ListFiles] =
    (cs.commandName(name, ()) :: (cs.whitespace :: Uri.quotedStringCodec).
      dropUnits.head.option).dropUnits.as[ListFiles]

  implicit val selectAnswer = SelectAnswer[ListFiles, FileListAnswer]
}
