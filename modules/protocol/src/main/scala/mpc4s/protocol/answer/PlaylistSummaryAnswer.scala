package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class PlaylistSummaryAnswer(playlist: PlaylistSummaryList) extends Answer

object PlaylistSummaryAnswer {

  implicit def codec(implicit pc: LineCodec[PlaylistSummaryList]): LineCodec[PlaylistSummaryAnswer] =
    pc.xmap(PlaylistSummaryAnswer.apply, _.playlist)
}
