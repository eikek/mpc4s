package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Play(pos: Option[Int]) extends Command {
  val name = Play.name
}

object Play {
  val name = CommandName("play")

  implicit def codec(implicit sc: LineCodec[Int]): LineCodec[Play] =
    (cs.commandName(name, ()) :: (cs.whitespace :: sc).dropUnits.head.option).dropUnits.as[Play]

  implicit val selectAnswer: SelectAnswer[Play, Answer.Empty.type] =
    SelectAnswer[Play, Answer.Empty.type]
}
