package mpc4s.http.util

import fs2.{io, Stream}
import cats.effect.Sync
import java.net.URL
import java.nio.file.{Files => JF, Path, Paths}

trait Files {

  implicit final class UrlOps(url: URL) {

    def name: String =
      Option(url.getPath).
        map(p => Paths.get(p).name).
        getOrElse("")

    def open[F[_]](chunkSize: Int)(implicit F: Sync[F]): Stream[F, Byte] = {
      Stream.bracket(F.delay(url.openStream))(
        in => io.readInputStream(F.pure(in), chunkSize),
        in => F.delay(in.close))
    }

  }

  implicit final class PathOps(path: Path) {
    require(path != null)

    def /(child: String): Path = path.resolve(child).normalize

    def exists: Boolean =
      JF.exists(path)

    def notExists: Boolean =
      !exists

    def isDirectory: Boolean =
      exists && JF.isDirectory(path)

    def parent: Option[Path] =
      Option(path.getParent)

    def name: String =
      path.getFileName.toString

    def extension: Option[String] =
      name.lastIndexOf('.') match {
        case -1 => None
        case idx => Some(name.substring(idx + 1))
      }

    def basename: String =
      extension match {
        case Some(e) =>
          val n = name
          n.substring(0, n.length - e.length)
        case None =>
          name
      }

    def findAnyFile(files: Seq[String]): Option[Path] =
      files.toStream.map(path.resolve).find(_.exists)

    def findAnyFileInSubDirs(subs: Seq[String], files: Seq[String]): Option[Path] =
      subs.toStream.map(path.resolve).
        flatMap(sub => files.map(sub.resolve)).
        find(_.exists)

    def contents[F[_]: Sync]: Stream[F, Byte] =
      io.file.readAll(path, 32 * 1024)

    def size: Long = JF.size(path)

    def etag: String =
      size + name

    def checkETag(tag: String): Boolean =
      etag == tag

    def isSubpathOf(parent: Path): Boolean =
      path.startsWith(parent)

    // TODO make this more robust, but this is enough at first…
    def mimeType: String =
      extension.getOrElse("").toLowerCase match {
        case "jpg" => "image/jpeg"
        case "jpeg" => "image/jpeg"
        case "png" => "image/png"
        case "gif" => "image/gif"
        case _ => "application/octet-stream"
      }
  }
}

object Files extends Files
