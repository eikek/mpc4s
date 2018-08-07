package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class CurrentSongAnswer(song: Option[PlaylistSong]) extends Answer {
  def isEmpty: Boolean = song.isEmpty
  def isDefined: Boolean = song.isDefined
}


object CurrentSongAnswer {
  val Empty = CurrentSongAnswer(None)

  implicit def codec(implicit pc: LineCodec[PlaylistSong]): LineCodec[CurrentSongAnswer] = {
    val existing: LineCodec[CurrentSongAnswer] =
      pc.xmap(p => CurrentSongAnswer(Some(p)), _.song.get)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
