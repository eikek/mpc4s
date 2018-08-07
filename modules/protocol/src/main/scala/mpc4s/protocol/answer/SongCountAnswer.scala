package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class SongCountAnswer(songCounts: SongCountList) extends Answer

object SongCountAnswer {

  implicit def codec(implicit sc: LineCodec[SongCountList]): LineCodec[SongCountAnswer] =
    sc.xmap(SongCountAnswer.apply, _.songCounts)
}
