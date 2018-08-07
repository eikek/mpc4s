package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Shuffle(range: Option[Range]) extends Command {
  val name = Shuffle.name
}

object Shuffle {
  val name = CommandName("shuffle")

  implicit def codec(implicit rc: LineCodec[Range]): LineCodec[Shuffle] =
    (cs.commandName(name, ()) :: (cs.whitespace :: rc).dropUnits.head.option).dropUnits.as[Shuffle]

  implicit val selectAnswer = SelectAnswer[Shuffle, Answer.Empty.type]

}
