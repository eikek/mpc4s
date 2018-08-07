package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class List(ltype: ListType
  , filter: Option[Filter] = None
  , group: Vector[Tag] = Vector.empty) extends Command {
  val name = List.name
}

object List {
  val name = CommandName("list")

  implicit def codec(implicit ltc: LineCodec[ListType], fc: LineCodec[Filter], tc: LineCodec[Tag]): LineCodec[List] = {
    val group = (cs.whitespace :: cs.constant("group", ()) :<>: tc.repsep(cs.whitespace)).
      dropUnits.head.option.
      xmap[Vector[Tag]](_.getOrElse(Vector.empty), v => Some(v).filter(_.nonEmpty))

    val filter = (cs.whitespace :: fc).dropUnits.head.option

    (cs.commandName(name, ()) :<>: ltc :: filter :: group).dropUnits.as[List]
  }

  implicit val selectAnswer = SelectAnswer[List, ListAnswer]
}
