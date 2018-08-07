package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class RangeId(songId: Id, range: Range) extends Command {
  val name = RangeId.name
}

object RangeId {

  val name = CommandName("rangeid")

  implicit def codec(implicit ic: LineCodec[Id], rc: LineCodec[Range]): LineCodec[RangeId] =
    (cs.commandName(name, ()) :<>: ic :<>: rc).dropUnits.as[RangeId]

  implicit val selectAnswer = SelectAnswer[RangeId, Answer.Empty.type]
}
