package mpc4s.player

import fs2.Stream
import cats.effect.Sync
import spinoco.fs2.http.HttpResponse
import org.log4s._

import mpc4s.player.webjar.Webjars
import mpc4s.http.util.Size.Implicits._
import mpc4s.http.util.all._

object Assets {
  private[this] val logger = getLogger

  def lookup[F[_]](name: String
    , path: Seq[String]
    , toc: Webjars.Toc
    , noneMatch: Option[String] = None)
    (implicit F: Sync[F]): Stream[F, HttpResponse[F]] = {

    val p = path.mkString("/")
    Toc.find(name, path) match {
      case Some((wj, _)) if Some(wj.hash) == noneMatch =>
        logger.trace(s"Found ETag for asset $name/$p")
        Stream(NotModified.emptyBody[F].withETag(wj.hash))

      case Some((wj, url)) =>
        logger.trace(s"Found asset $name/$p")
        val file = toc.get(wj.hash).flatMap(_.get(p))
        val len = file.map(_.length.bytes)
        urlContents(url, len, Some(wj.hash), file.map(_.contentType), noneMatch)

      case None =>
        logger.trace(s"Asset $name/$p not found.")
        Stream(NotFound.emptyBody[F])
    }
  }
}
