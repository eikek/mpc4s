package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class ReplayGainStatusAnswer(replayGainMode: ReplayGainMode) extends Answer

object ReplayGainStatusAnswer {

  implicit def codec: LineCodec[ReplayGainStatusAnswer] =
    LineCodec[ReplayGainStatusAnswer].keyValues
}
