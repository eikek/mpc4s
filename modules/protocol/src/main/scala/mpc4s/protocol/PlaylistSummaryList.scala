package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class PlaylistSummaryList(songs: Vector[PlaylistSummary]) {
  def isEmpty: Boolean = songs.isEmpty

  def nonEmpty: Boolean = songs.nonEmpty
}

object PlaylistSummaryList {
  val Empty = PlaylistSummaryList(Vector.empty)

  implicit def codec(implicit pc: LineCodec[PlaylistSummary]): LineCodec[PlaylistSummaryList] = {
    val existing: LineCodec[PlaylistSummaryList] =
      cs.map(cs.splitPlaylists, (pc :: cs.empty).dropUnits.head).
        xmap(PlaylistSummaryList.apply, _.songs)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
