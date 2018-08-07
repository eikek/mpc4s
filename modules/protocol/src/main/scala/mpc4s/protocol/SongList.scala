package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class SongList(songs: Vector[Song])

object SongList {
  implicit def codec(implicit pc: LineCodec[Song]): LineCodec[SongList] =
    cs.map(cs.splitSongs, (pc :: cs.empty).dropUnits.head).xmap(SongList.apply, _.songs)
}
