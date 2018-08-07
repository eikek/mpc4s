package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class SearchAdd(filter: Filter) extends Command {
  val name = SearchAdd.name
}

object SearchAdd {
  val name = CommandName("searchadd")

  implicit def codec(implicit fc: LineCodec[Filter]): LineCodec[SearchAdd] =
    (cs.commandName(name, ()) :<>: fc).dropUnits.as[SearchAdd]

  implicit val selectAnswer = SelectAnswer[SearchAdd, Answer.Empty.type]
}
