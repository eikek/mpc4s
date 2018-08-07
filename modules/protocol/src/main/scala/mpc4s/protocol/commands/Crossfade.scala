package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Crossfade(sec: Seconds) extends Command {
  val name = Crossfade.name
}

object Crossfade {
  val name = CommandName("crossfade")

  implicit def codec(implicit sc: LineCodec[Seconds]): LineCodec[Crossfade] =
    (cs.commandName(name, ()) :<>: sc).dropUnits.as[Crossfade]

  implicit val selectAnswer: SelectAnswer[Crossfade, Answer.Empty.type] =
    SelectAnswer[Crossfade, Answer.Empty.type]
}
