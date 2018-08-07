package mpc4s.protocol

//import java.time.Instant
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

case class PlaylistSong(song: Song
  , pos: Int
  , id: Id)

object PlaylistSong {

  implicit val codec: LineCodec[PlaylistSong] =
    codecs.keyValue.exmap(fromMap, toMap)

  def fromMap(m: ListMap[ListMap.Key, String]): Result[PlaylistSong] = {
    import mpc4s.protocol.codec.implicits.keyvalues._

    implicit val mc: LineCodec[Song] =
      codecs.ignore.xmap(_ => Song(Uri(""), None, None, None, ListMap.empty), _ => ())

    for {
      sng <- Song.fromMap(m)
      ps  <- m.as[PlaylistSong]
    } yield ps.copy(song = sng)
  }

  def toMap(ps: PlaylistSong): Result[ListMap[ListMap.Key, String]] = {
    import mpc4s.protocol.codec.implicits.keyvalues._

    implicit val mc: LineCodec[Song] =
      codecs.ignore.xmap(_ => Song(Uri(""), None, None, None, ListMap.empty), _ => ())

    for {
      m1 <- ps.toStringMap
      m2 <- Song.toMap(ps.song)
    } yield m2 ++ m1
  }


}
