package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Find(filter: Filter
  , sort: Option[Sort]
  , window: Option[Range]) extends Command {
  val name = Find.name
}

object Find {
  val name = CommandName("find")

  implicit def codec(implicit fc: LineCodec[Filter], tc: LineCodec[Sort], rc: LineCodec[Range]): LineCodec[Find] =
    (cs.commandName(name, ()) :<>: fc ::
      (cs.whitespace :: cs.constant("sort", ()) :<>: tc).dropUnits.head.option ::
      (cs.whitespace :: cs.constant("window", ()) :<>: rc).dropUnits.head.option).dropUnits.as[Find]

  implicit val selectAnswer = SelectAnswer[Find, SongListAnswer]
}
