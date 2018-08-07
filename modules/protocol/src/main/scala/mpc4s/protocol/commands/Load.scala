package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Load(playlist: String, range: Option[Range]) extends Command {
  val name = Load.name
}

object Load {
  val name = CommandName("load")

  implicit def codec(implicit rc: LineCodec[Range]): LineCodec[Load] =
    (cs.commandName(name, ()) :<>: cs.quotedString :: (cs.whitespace :: rc).dropUnits.head.option).dropUnits.as[Load]

  implicit val selectAnswer = SelectAnswer[Load, Answer.Empty.type]
}
