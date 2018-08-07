package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

case class SongCount(songs: Int, playtime: Seconds, group: Option[TagVal])

object SongCount {

  def fromMap(m: ListMap[ListMap.Key, String])(implicit sc: LineCodec[Seconds]): Result[SongCount] = {
    import mpc4s.protocol.codec.implicits.keyvalues._

    implicit val mc: LineCodec[Option[TagVal]] =
      codecs.ignore.xmap(_ => None, _ => ())

    m.as[SongCount].map(_.copy(group = TagVal.findFirst(m)))
  }

  def toMap(sc: SongCount): Result[ListMap[ListMap.Key,String]] = {
    import mpc4s.protocol.codec.implicits.keyvalues._

    implicit val mc: LineCodec[Option[TagVal]] =
      codecs.ignore.xmap(_ => None, _ => ())

    val group = sc.group match {
      case Some(TagVal(t, v)) => ListMap(ListMap.key(t.name) -> v)
      case None => ListMap.empty[ListMap.Key, String]
    }

    sc.toStringMap.map(m => group ++ m)
  }

  implicit val codec: LineCodec[SongCount] =
    codecs.keyValue.exmap[SongCount](fromMap, toMap)

}
