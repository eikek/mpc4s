package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class PlaylistAnswer(songs: PlaylistSongList) extends Answer

object PlaylistAnswer {

  implicit def codec(implicit pc: LineCodec[PlaylistSongList]): LineCodec[PlaylistAnswer] =
    pc.xmap(PlaylistAnswer.apply, _.songs)
}
