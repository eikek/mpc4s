package mpc4s.http.routes

import fs2.Stream
import fs2.async.mutable.Semaphore
import cats.effect.{Effect, Sync}
import cats.implicits._
import spinoco.fs2.http.HttpResponse
import java.nio.file.Path
import scala.concurrent.ExecutionContext

import org.log4s._
import mpc4s.http.config._
import mpc4s.http.internal.ImageCrop
import mpc4s.http.util.all._

final class Thumbnail[F[_]: Sync](cfg: ThumbnailConfig, maxParallel: Semaphore[F]) {
  private[this] val logger = getLogger

  def serve(file: Path
    , albumName: Option[String]
    , noneMatch: Option[String]
    , size: Option[Int]
    , namePrefix: String): Stream[F, HttpResponse[F]] =
    size match {
      case Some(sz) if isEnabled(file, sz) =>
        val out = makeOut(file, sz).toAbsolutePath
        if (out.exists) fileContents(out, noneMatch, albumName.map(n => s"$namePrefix - $n"))
        else Stream.bracket(maxParallel.decrement)(
          _ =>  Stream.eval(resize(file, sz, out)).
            flatMap(f => fileContents(f, noneMatch, albumName.map(n => s"$namePrefix - $n"))),
          _ => maxParallel.increment
        )

      case _ =>
        logger.trace(s"Resizing file '$file' not enabled")
        fileContents(file, noneMatch, albumName.map(n => s"$namePrefix - $n"))
    }

  private def isEnabled(file: Path, size: Int): Boolean =
    cfg.enable &&
      (size > 0) &&
      (file.size > cfg.minFileSize) &&
      (file.size < cfg.maxFileSize)

  private def makeFilename(in: Path, size: Int): String =
    s"${in.etag}-${size}.jpg"

  private def makeOut(in: Path, size: Int): Path =
    cfg.directory.resolve(makeFilename(in, size))

  private def resize(in: Path, size: Int, out: Path): F[Path] =
    Sync[F].delay(logger.trace(s"Resize image '$in' to size $size in '$out'")) >>
    TempFile.create("tmp", ".jpg", out.getParent.toAbsolutePath).
      flatMap(tmp => ImageCrop.resizeFile(in, tmp, size).map(_ => tmp)).
      flatMap(tmp => tmp.moveTo(out)).
      handleError { ex =>
        logger.warn(ex)(s"Error resizing file '$in'")
        in
      }
}

object Thumbnail {

  def apply[F[_]: Effect](cfg: ThumbnailConfig)(implicit ec: ExecutionContext): F[Thumbnail[F]] =
    fs2.async.semaphore(cfg.maxParallel.toLong).
      map(sem => new Thumbnail(cfg, sem))

}
