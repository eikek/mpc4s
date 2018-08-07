package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class SongListAnswer(songs: SongList) extends Answer

object SongListAnswer {

  implicit def codec(implicit pc: LineCodec[SongList]): LineCodec[SongListAnswer] =
    pc.xmap(SongListAnswer.apply, _.songs)
}
