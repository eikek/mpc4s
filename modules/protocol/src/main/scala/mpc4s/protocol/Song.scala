package mpc4s.protocol

import java.time.Instant
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

case class Song(file: Uri
  , `Last-Modified`: Option[Instant]
  , time: Option[Seconds]
  , duration: Option[Double]
  , tags: ListMap[Tag, String]) {

  val lastModified = `Last-Modified`
}

object Song {

  implicit val codec: LineCodec[Song] =
    codecs.keyValue.exmap[Song](fromMap, toMap)

  def fromMap(m: ListMap[ListMap.Key, String]): Result[Song] = {
    import mpc4s.protocol.codec.implicits.keyvalues._
    implicit val uriCodec = Uri.endOfLineCodec
    implicit val mc: LineCodec[ListMap[Tag, String]] =
      codecs.ignore.xmap(_ => ListMap.empty[Tag,String], _ => ())

    m.as[Song].map(_.copy(tags = Tag.from(m)))
  }

  def toMap(s: Song): Result[ListMap[ListMap.Key, String]] = {
    import mpc4s.protocol.codec.implicits.keyvalues._
    implicit val uriCodec = Uri.endOfLineCodec
    implicit val mc: LineCodec[ListMap[Tag, String]] =
      codecs.ignore.xmap(_ => ListMap.empty[Tag,String], _ => ())

    s.toStringMap.map(_ ++ s.tags.mapKeys(tag => ListMap.key(tag.name)))
  }
}
