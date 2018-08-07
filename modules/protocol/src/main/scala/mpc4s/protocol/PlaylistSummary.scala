package mpc4s.protocol

import java.time.Instant
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

case class PlaylistSummary(playlist: Uri, `Last-Modified`: Instant) {
  val lastModified = `Last-Modified`
}

object PlaylistSummary {

  implicit def codec: LineCodec[PlaylistSummary] =
    codecs.keyValue.exmap[PlaylistSummary](fromMap, toMap)

  def fromMap(m: ListMap[ListMap.Key, String]): Result[PlaylistSummary] = {
    import mpc4s.protocol.codec.implicits.keyvalues._
    implicit val uriCodec = Uri.endOfLineCodec

    m.as[PlaylistSummary]
  }

  def toMap(ps: PlaylistSummary): Result[ListMap[ListMap.Key, String]] = {
    import mpc4s.protocol.codec.implicits.keyvalues._
    implicit val uriCodec = Uri.endOfLineCodec

    ps.toStringMap
  }
}
