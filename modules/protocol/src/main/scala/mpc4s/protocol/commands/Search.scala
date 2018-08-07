package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Search(filter: Filter
  , sort: Option[Sort]
  , window: Option[Range]) extends Command {
  val name = Search.name
}

object Search {
  val name = CommandName("search")

  implicit def codec(implicit fc: LineCodec[Filter], tc: LineCodec[Sort], rc: LineCodec[Range]): LineCodec[Search] =
    (cs.commandName(name, ()) :<>: fc ::
      (cs.whitespace :: cs.constant("sort", ()) :<>: tc).dropUnits.head.option ::
      (cs.whitespace :: cs.constant("window", ()) :<>: rc).dropUnits.head.option).dropUnits.as[Search]

  implicit val selectAnswer = SelectAnswer[Search, SongListAnswer]
}
