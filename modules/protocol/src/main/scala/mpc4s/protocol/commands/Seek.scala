package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Seek(pos: Int, sec: Seconds) extends Command {
  val name = Seek.name
}

object Seek {
  val name = CommandName("seek")

  implicit def codec(implicit sc: LineCodec[Seconds]): LineCodec[Seek] =
    (cs.commandName(name, ()) :<>: cs.int :<>: sc).dropUnits.as[Seek]

  implicit val selectAnswer: SelectAnswer[Seek, Answer.Empty.type] =
    SelectAnswer[Seek, Answer.Empty.type]
}
