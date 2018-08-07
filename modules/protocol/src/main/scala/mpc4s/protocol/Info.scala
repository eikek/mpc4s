package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

sealed trait Info

object Info {

  case class DirInfo(dir: File.Directory) extends Info
  object DirInfo {
    implicit def codec(implicit dc: LineCodec[File.Directory]): LineCodec[DirInfo] =
      dc.xmap(DirInfo.apply, _.dir)
  }

  case class SongInfo(song: Song) extends Info
  object SongInfo {
    implicit def codec(implicit sc: LineCodec[Song]): LineCodec[SongInfo] =
      sc.require(_.file.nonEmpty).xmap(SongInfo.apply, _.song)
  }

  case class PlaylistInfo(playlist: PlaylistSummary) extends Info
  object PlaylistInfo {
    implicit def codec(implicit pc: LineCodec[PlaylistSummary]): LineCodec[PlaylistInfo] =
      pc.require(_.playlist.nonEmpty).xmap(PlaylistInfo.apply, _.playlist)
  }

  implicit val codec: LineCodec[Info] =
    LineCodec[Info].choice
}
