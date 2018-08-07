package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class SetReplayGainMode(state: ReplayGainMode) extends Command {
  val name = SetReplayGainMode.name
}

object SetReplayGainMode {
  val name = CommandName("replay_gain_mode")

  implicit def codec(implicit sc: LineCodec[ReplayGainMode]): LineCodec[SetReplayGainMode] =
    (cs.commandName(name, ()) :<>: sc).dropUnits.as[SetReplayGainMode]

  implicit val selectAnswer: SelectAnswer[SetReplayGainMode, Answer.Empty.type] =
    SelectAnswer[SetReplayGainMode, Answer.Empty.type]
}
