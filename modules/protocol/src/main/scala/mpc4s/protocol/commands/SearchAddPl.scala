package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class SearchAddPl(playlist: String, filter: Filter) extends Command {
  val name = SearchAddPl.name
}

object SearchAddPl {
  val name = CommandName("searchaddpl")

  implicit def codec(implicit fc: LineCodec[Filter]): LineCodec[SearchAddPl] =
    (cs.commandName(name, ()) :<>: cs.quotedString :<>: fc).dropUnits.as[SearchAddPl]

  implicit val selectAnswer = SelectAnswer[SearchAddPl, Answer.Empty.type]
}
