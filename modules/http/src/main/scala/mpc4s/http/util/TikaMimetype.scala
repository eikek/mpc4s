package mpc4s.http.util

import fs2.Stream
import cats.effect.Sync
import cats.implicits._
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.{HttpHeaders, Metadata, TikaMetadataKeys}
import org.apache.tika.mime.{MediaType => TikaMediaType}
import spinoco.protocol.mime._
import scodec.bits.BitVector

object TikaMimetype {
  private val tika = new TikaConfig().getDetector

  val unknown: ContentType = ContentType.BinaryContent(MediaType.`application/octet-stream`, None)

  case class MimetypeHint(filename: Option[String], advertised: Option[String])

  object MimetypeHint {
    val none = MimetypeHint(None, None)
  }

  def detect[F[_]: Sync](data: Stream[F, Byte], hint: MimetypeHint = MimetypeHint.none): F[ContentType] =
    data.take(256).
      compile.toVector.
      map(bytes => fromBytes(bytes.toArray, hint))


  def from(mt: String): ContentType =
    ContentType.codec.decodeValue(BitVector(mt.getBytes)).toOption.getOrElse(unknown)


  private def convert(mt: TikaMediaType): ContentType =
    Option(mt).map(_.toString).
      map(from).
      map(normalize).
      getOrElse(unknown)

  private def makeMetadata(hint: MimetypeHint): Metadata = {
    val md = new Metadata
    hint.filename.
      foreach(md.set(TikaMetadataKeys.RESOURCE_NAME_KEY, _))
    hint.advertised.
      foreach(md.set(HttpHeaders.CONTENT_TYPE, _))
    md
  }

  private def normalize(in: ContentType): ContentType = in match {
    case ContentType.TextContent(dm @ MediaType.DefaultMediaType(_, sub, _, _, _), cs) if sub contains "xhtml" =>
      ContentType.TextContent(dm.copy(sub = "html"), cs)
    case _ => in
  }

  private def fromBytes(bv: Array[Byte], hint: MimetypeHint): ContentType = {
    convert(tika.detect(new java.io.ByteArrayInputStream(bv), makeMetadata(hint)))
  }
}
