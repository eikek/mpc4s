package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class MixrampDelay(sec: Seconds) extends Command {
  val name = MixrampDelay.name
}

object MixrampDelay {
  val name = CommandName("mixrampdelay")

  implicit def codec(implicit sc: LineCodec[Seconds]): LineCodec[MixrampDelay] =
    (cs.commandName(name, ()) :<>: sc).dropUnits.as[MixrampDelay]

  implicit val selectAnswer: SelectAnswer[MixrampDelay, Answer.Empty.type] =
    SelectAnswer[MixrampDelay, Answer.Empty.type]
}
