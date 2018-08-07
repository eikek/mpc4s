package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class StoredPlaylistAnswer(playlist: StoredPlaylist) extends Answer

object StoredPlaylistAnswer {

  implicit def codec(implicit pc: LineCodec[StoredPlaylist]): LineCodec[StoredPlaylistAnswer] =
    pc.xmap(StoredPlaylistAnswer.apply, _.playlist)
}
