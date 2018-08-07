package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.internal.Enum

sealed trait ReplayGainMode extends Enum

object ReplayGainMode {

  case object Off extends ReplayGainMode

  case object Track extends ReplayGainMode

  case object Album extends ReplayGainMode

  case object Auto extends ReplayGainMode

  val all = List(Off, Track, Album, Auto)

  implicit val codec: LineCodec[ReplayGainMode] =
    Enum.codecFromAll(all)

}
