package mpc4s.player

import fs2.{text, Stream}
import cats.effect.Sync
import cats.implicits._
import spinoco.fs2.http.HttpResponse
import org.log4s._
import io.circe._, io.circe.generic.semiauto._, io.circe.syntax._

import mpc4s.player.webjar.Webjars
import mpc4s.http.util.all._

object Index {

  private[this] val logger = getLogger

  def lookup[F[_]](flags: Flags
    , toc: Webjars.Toc
    , noneMatch: Option[String] = None)
    (implicit F: Sync[F]): Stream[F, HttpResponse[F]] = {

    val p = "index.html"
    val name = "mpc4s-player"

    Toc.find(name, Seq(p)) match {
      case Some((wj, _)) if Some(wj.hash + flags.hashCode) == noneMatch =>
        logger.trace(s"Found ETag for asset $name/$p")
        Stream(NotModified.emptyBody[F].withETag(wj.hash))

      case Some((wj, url)) =>
        logger.trace(s"Found $name/$p")
        val file = toc.get(wj.hash).flatMap(_.get(p))
        Stream.eval(indexContents(toc, flags)).
          evalMap {
            case Some(str) =>
              Ok.byteBody(Stream.emits(str.getBytes).covary[F]).
                withContentLength(str.getBytes.length.toLong).
                withETag(wj.hash + flags.hashCode).
                useOrDetect(url, file.map(_.contentType))

            case None =>
              F.pure(NotFound.emptyBody[F])
          }

      case None =>
        logger.trace(s"Asset $name/$p not found.")
        Stream(NotFound.emptyBody[F])
    }
  }

  private def indexContents[F[_]: Sync](toc: Webjars.Toc, flags: Flags): F[Option[String]] = Sync[F].delay({
    val p = "index.html"
    val name = "mpc4s-player"

    Toc.find(name, Seq(p)) match {
      case Some((wj, url)) =>
        logger.trace(s"Found $name/$p")
        url.open[F](16 * 1024).
          through(text.utf8Decode).
          fold1(_ + _).
          map(str => str.replaceAll("\\{\\{flags\\}\\}", flags.asJson.spaces2)).
          compile.last

      case None =>
        Sync[F].pure(None: Option[String])
    }
  }).flatMap(identity)


  case class Flags(baseUrl: String, mpdConns: List[MpdConnections])

  object Flags {
    implicit def encoder: Encoder[Flags] =
      deriveEncoder[Flags]
  }

  case class MpdConnections(id: String, title: String)
  object MpdConnections {
    implicit def encoder: Encoder[MpdConnections] =
      deriveEncoder[MpdConnections]
  }
}
