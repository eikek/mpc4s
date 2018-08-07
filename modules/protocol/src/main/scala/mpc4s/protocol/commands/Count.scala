package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._
import mpc4s.protocol.codec.{codecs => cs}

sealed trait Count extends Command {
  val name = Count.name
}


object Count {
  val name = CommandName("count")

  case class FilterOnly(filter: Filter) extends Count
  object FilterOnly{
    implicit def codec(implicit tc: LineCodec[Filter]): LineCodec[FilterOnly] =
      (cs.commandName(name, ()) :<>: tc).
        dropUnits.as[FilterOnly]
  }

  case class GroupOnly(group: Tag) extends Count
  object GroupOnly {
    implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[GroupOnly] =
      (cs.commandName(name, ()) :<>: cs.constant("group", ()) :<>: tc).
        dropUnits.as[GroupOnly]
  }

  case class FilterAndGroup(filter: Filter, group: Tag) extends Count
  object FilterAndGroup {
    implicit def codec(implicit fc: LineCodec[Filter], tc: LineCodec[Tag]): LineCodec[FilterAndGroup] =
      (cs.commandName(name, ()) :<>: fc :<>: cs.constant("group", ()) :<>: tc).
        dropUnits.as[FilterAndGroup]
  }


  implicit def codec: LineCodec[Count] =
    LineCodec[Count].choice

  implicit val selectAnswer = SelectAnswer[Count, SongCountAnswer]
}
