package mpc4s.http.util

import fs2._
import cats.effect.Sync
import cats.implicits._
import spinoco.protocol.http.{HttpStatusCode, HttpResponseHeader, Uri}
import spinoco.protocol.http.header._
import spinoco.protocol.http.header.value._
import spinoco.protocol.mime._
import spinoco.fs2.http._
import spinoco.fs2.http.body.{BodyEncoder, StreamBodyEncoder}
import org.log4s._
import java.net.URL
import java.time.{Instant, ZoneId}
import java.nio.file.Path

import mpc4s.http.util.Files._

trait Responses {
  private[this] val logger = getLogger

  def emptyResponse[F[_]](status: HttpStatusCode): HttpResponse[F] =
    HttpResponse(
      HttpResponseHeader(
        status = status,
        reason = status.label,
        headers = Nil),
      Stream.empty
    )

  val Ok = HttpStatusCode.Ok
  val PartialContent = HttpStatusCode.PartialContent
  val NotFound = HttpStatusCode.NotFound
  val Unauthorized = HttpStatusCode.Unauthorized
  val Forbidden = HttpStatusCode.Forbidden
  val BadRequest = HttpStatusCode.BadRequest
  val Created = HttpStatusCode.Created
  val NoContent = HttpStatusCode.NoContent
  val NotModified = HttpStatusCode.NotModified
  val InternalServerError = HttpStatusCode.InternalServerError
  val Conflict = HttpStatusCode.Conflict
  val PermRedirect = HttpStatusCode.PermanentRedirect

  implicit class ResponseStatusOps(status: HttpStatusCode) {

    def emptyBody[F[_]] = emptyResponse[F](status)

    def body[F[_],A](body: A)(implicit enc: BodyEncoder[A]): HttpResponse[F] =
      emptyResponse[F](status).withBody(body)

    def streamBody[F[_],A](body: Stream[F,A])(implicit enc: StreamBodyEncoder[F,A]): HttpResponse[F] =
      emptyBody[F].withStreamBody(body)(enc)

    def byteBody[F[_]](body: Stream[F, Byte]): HttpResponse[F] =
      emptyBody.copy(body = body)

  }

  implicit final class ReqRespOps[F[_]](r: HttpResponse[F]) {
    def withContentLength(len: Long): HttpResponse[F] =
      r.withHeader(`Content-Length`(len))

    def withContentLength(len: Option[Long]): HttpResponse[F] =
      len.map(withContentLength).getOrElse(r)

    def withETag(id: String) =
      r.withHeader(ETag(EntityTag(id, false)))

    def withETag(id: Option[String]): HttpResponse[F] =
      id.map(t => r.withHeader(ETag(EntityTag(t, false)))).getOrElse(r)

    def withLastModified(time: Instant) =
      r.withHeader(`Last-Modified`(time.atZone(ZoneId.of("UTC")).toLocalDateTime))

    def withHeader(name: String, value: String) =
      r.withHeader(GenericHeader(name, value))

    def withDisposition(value: String, filename: String) =
      r.withHeader(`Content-Disposition`(ContentDisposition(value, Map("filename" -> filename))))

    def detectContentType(contents: Stream[F, Byte], hint: TikaMimetype.MimetypeHint)(implicit F: Sync[F]): F[HttpResponse[F]] =
      TikaMimetype.detect(contents, hint).map(ct => r.withContentType(ct))

    def detectContentType(file: Path)(implicit F: Sync[F]): F[HttpResponse[F]] = {
      val hint = TikaMimetype.MimetypeHint(Some(file.name), Some(file.mimeType))
      detectContentType(file.contents, hint)
    }

    def detectContentType(url: URL, advertisedMime: Option[String] = None)(implicit F: Sync[F]): F[HttpResponse[F]] = {
      val hint = TikaMimetype.MimetypeHint(Some(url.name), advertisedMime)
      detectContentType(url.open(1024), hint)
    }

    def useOrDetect(url: URL, mime: Option[String])(implicit F: Sync[F]): F[HttpResponse[F]] =
      mime match {
        case Some(m) =>
          F.pure(r.withContentType(TikaMimetype.from(m)))
        case None =>
          detectContentType(url, None)
      }

    def withLocation(loc: Uri.Path): HttpResponse[F] =
      r.withHeader(Location(LocationDefinition.Relative(loc, Uri.Query.empty)))
  }

  def fromError[F[_]](th: Throwable): HttpResponse[F] = {
    implicit val be = BodyEncoder.utf8String

    BadRequest.body(Option(th).map(_.toString).getOrElse("no error message"))
  }

  def staticFile[F[_]: Sync](body: Path, root: Path, noneMatch: Option[String], baseName: Option[String]): Stream[F, HttpResponse[F]] =
    if (body.isSubpathOf(root)) {
      logger.trace(s"About to deliver file '$body' (exists: ${body.exists})")
      if (body.notExists) Stream.emit(NotFound.emptyBody[F])
      else if (noneMatch.exists(body.checkETag)) Stream(NotModified.emptyBody[F].withETag(body.etag))
      else Stream.eval(Ok.byteBody(body.contents).
        withContentLength(body.size).
        withETag(body.etag).
        withDisposition("inline", makeFilename(body, baseName)).
        detectContentType(body))
    } else {
      logger.warn(s"Cancelled attempt to deliver file above root '$root': '$body'")
      Stream.emit(NotFound.emptyBody[F])
    }

  def urlContents[F[_]: Sync](body: URL, length: Option[Long], tag: Option[String], mime: Option[String], noneMatch: Option[String]): Stream[F, HttpResponse[F]] =
    if (noneMatch.isDefined && noneMatch == tag) {
      Stream(NotModified.emptyBody[F].withETag(tag))
    } else {
      //try to get 1 byte to decide whether not-found or not
      body.open(2).take(1).attempt.last.
        flatMap {
          case Some(_) =>
            Stream.eval {
              Ok.byteBody(body.open(16 * 1024)).
                withContentLength(length).
                withETag(tag).
                useOrDetect(body, mime)
            }
          case None =>
            Stream(NotFound.emptyBody[F])
        }
    }

  def urlContents[F[_]: Sync](body: ResourceFile, noneMatch: Option[String]): Stream[F, HttpResponse[F]] =
    urlContents(body.url, Some(body.length), Some(body.checksum), Some(body.mime), noneMatch)

  private def makeFilename(file: Path, base: Option[String]): String =
    base match {
      case Some(name) =>
        file.extension.
          map(ext => s"${name}.${ext}").
          getOrElse(name)
      case _ =>
        file.getFileName.toString
    }
}

object Responses extends Responses
