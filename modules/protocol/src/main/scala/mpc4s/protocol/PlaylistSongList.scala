package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class PlaylistSongList(songs: Vector[PlaylistSong]) {
  def isEmpty: Boolean = songs.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object PlaylistSongList {
  val Empty = PlaylistSongList(Vector.empty)

  implicit def codec(implicit pc: LineCodec[PlaylistSong]): LineCodec[PlaylistSongList] = {
    val existing: LineCodec[PlaylistSongList] =
      cs.map(cs.splitSongs, (pc :: cs.empty).dropUnits.head).
        xmap(PlaylistSongList.apply, _.songs)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
