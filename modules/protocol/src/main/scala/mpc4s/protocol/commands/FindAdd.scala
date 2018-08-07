package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class FindAdd(filter: Filter) extends Command {
  val name = FindAdd.name
}

object FindAdd {
  val name = CommandName("findadd")

  implicit def codec(implicit fc: LineCodec[Filter]): LineCodec[FindAdd] =
    (cs.commandName(name, ()) :<>: fc).dropUnits.as[FindAdd]

  implicit val selectAnswer = SelectAnswer[FindAdd, Answer.Empty.type]
}
