package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._
import mpc4s.protocol.internal.Enum

sealed trait Tag extends Enum

object Tag {

  case object Disc extends Tag
  case object Date extends Tag
  case object Name extends Tag
  case object Genre extends Tag
  case object Track extends Tag
  case object Title extends Tag
  case object Album extends Tag
  case object Artist extends Tag
  case object Comment extends Tag
  case object Composer extends Tag
  case object Performer extends Tag
  case object Albumsort extends Tag
  case object Artistsort extends Tag
  case object Albumartist extends Tag
  case object Albumartistsort extends Tag
  case object MusicbrainzWorkid extends Tag
  case object MusicbrainzTrackid extends Tag
  case object MusicbrainzAlbumid extends Tag
  case object MusicbrainzArtistid extends Tag
  case object MusicbrainzAlbumartistid extends Tag
  case object MusicbrainzReleasetrackid extends Tag

  val all = List(MusicbrainzReleasetrackid
    , MusicbrainzAlbumartistid
    , MusicbrainzArtistid
    , MusicbrainzAlbumid
    , MusicbrainzTrackid
    , MusicbrainzWorkid
    , Albumartistsort
    , Albumartist
    , Artistsort
    , Albumsort
    , Performer
    , Composer
    , Comment
    , Artist
    , Album
    , Title
    , Track
    , Genre
    , Name
    , Date
    , Disc)

  implicit val codec: LineCodec[Tag] =
    Enum.codecFromAll(all)

  /** Finds tags in the given list of tuples. Ignores unknown keys.
    */
  def from(data: Iterable[(ListMap.Key, String)]): ListMap[Tag, String] = {
    val c = (codec :: codecs.empty).dropUnits.head
    data.foldLeft(ListMap.empty[Tag, String]) { (map, t) =>
      c.parse(t._1.name) match {
        case Right(pr) => map + (pr.value -> t._2)
        case Left(_) => map
      }
    }
  }

  def fromStrings(data: Iterable[(String,String)]): ListMap[Tag, String] =
    from(data.map(t => (ListMap.key(t._1), t._2)))
}
