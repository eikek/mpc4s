package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class SeekId(songId: Id, sec: Seconds) extends Command {
  val name = SeekId.name
}

object SeekId {
  val name = CommandName("seekid")

  implicit def codec(implicit ic: LineCodec[Id], sc: LineCodec[Seconds]): LineCodec[SeekId] =
    (cs.commandName(name, ()) :<>: ic :<>: sc).dropUnits.as[SeekId]

  implicit val selectAnswer: SelectAnswer[SeekId, Answer.Empty.type] =
    SelectAnswer[SeekId, Answer.Empty.type]
}
