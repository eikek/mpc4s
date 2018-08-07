package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class SetVol(vol: Volume) extends Command {
  val name = SetVol.name
}

object SetVol {
  val name = CommandName("setvol")

  implicit def codec(implicit sc: LineCodec[Volume]): LineCodec[SetVol] =
    (cs.commandName(name, ()) :<>: sc).dropUnits.as[SetVol]

  implicit val selectAnswer: SelectAnswer[SetVol, Answer.Empty.type] =
    SelectAnswer[SetVol, Answer.Empty.type]
}
