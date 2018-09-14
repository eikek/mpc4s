package mpc4s.http.routes

import java.nio.file.Path
import fs2.Stream
import cats.effect.Effect
import cats.implicits._
import shapeless.{::, HNil}
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext
import spinoco.fs2.http.HttpResponse
import spinoco.fs2.http.routing._
import spinoco.protocol.http.Uri
import org.log4s._
import java.net.URL
import io.circe._, io.circe.generic.semiauto._

import mpc4s.protocol._
import mpc4s.protocol.codec.ProtocolConfig
import mpc4s.protocol.commands._
import mpc4s.http.internal._
import mpc4s.http.config._
import mpc4s.http.util._
import mpc4s.http.util.all._

/** Routes for finding a single file per album.
  *
  * For example, it is used to find the cover art or booklet file for
  * a given song file or a given album name.
  */
final class AlbumFile[F[_]: Effect](mpd: Mpd[F], cache: PathCache[F], pcfg: ProtocolConfig) {
  private[this] val logger = getLogger

  private val musicDirectory = mpd.cfg.musicDirectory.
    getOrElse(sys.error("Bug: Configuration problem: no music directory set in mpd config"))

  def all(cfg: AlbumFile.Config[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] =
    choice(testFile(cfg), testAlbum(cfg), byFile(cfg), byAlbum(cfg), clearCache)

  def byAlbum(cfg: AlbumFile.Config[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] =
    "album" / Get >> param[String]("name") :: ifNoneMatch map {
      case albumName :: noneMatch :: HNil =>
        lookupFile(albumName, cfg).
          flatMap {
            case Some(file) if file.exists =>
              logger.trace(s"Find album-file $file")
              staticFile(file, musicDirectory, noneMatch, Some(s"Booklet - $albumName"))
            case _ =>
              logger.trace(s"Album-file not found (${cfg.cacheDim}) for album '${albumName}'")
              cfg.missingRoutes.byAlbum(albumName, noneMatch)
          }
    }

  def byFile(cfg: AlbumFile.Config[F]): Route[F] =
    "file" / Get >> asFile(musicDirectory) :: ifNoneMatch map {
      case file :: noneMatch :: HNil =>
        cfg.findFile(file) match {
          case Some(f) if f.exists =>
            logger.trace(s"Found album-file '$f' for file '$file'")
            staticFile(f, musicDirectory, noneMatch, None)
          case _ =>
            logger.trace(s"Album-file not found (${cfg.cacheDim}) for file '$file'")
            cfg.missingRoutes.byFile(file, noneMatch)
        }
    }

  def testFile(cfg: AlbumFile.Config[F]): Route[F] =
    "test"/"file"/Get >> asFile(musicDirectory) map { file =>
      val f = cfg.findFile(file)
      val p = f.map(_ => makeFileUrl(file, cfg.basePath, "file"))
      val info = AlbumFile.AlbumFileInfo(f.exists(_.exists), p)
      Stream.emit(Ok.body[F, AlbumFile.AlbumFileInfo](info))
    }

  def testAlbum(cfg: AlbumFile.Config[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] =
    "test"/"album"/Get >> param[String]("name") map { albumName =>
      lookupFile(albumName, cfg).
        map { ofile =>
          val p = makeAlbumUrl(albumName, cfg.basePath, "album")
          Ok.body(AlbumFile.AlbumFileInfo(ofile.exists(_.exists), ofile.map(_ => p)))
        }
    }

  def clearCache: Route[F] =
    "clearcache" / Post map { _ =>
      Stream.eval(cache.clear).
        map(_ => Ok.emptyBody[F])
    }

  /** Ask MPD for a song of the given album.
    */
  private def findSongFile(albumName: String, musicDirectory: Path)
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Stream[F, Option[Path]] =
    mpd.send(pcfg, Find(Filter(FilterType(Tag.Album) -> albumName), None, Some(Range(0,1)))).
      map(_.toOption.flatMap(_.songs.songs.headOption)).
      map(_.headOption.map(song => musicDirectory / song.file.uri))


  /** Lookup the file by first asking MPD for any file of the given
    * album and then using it to resolve the single file using the
    * given configuration.
    */
  private def lookupFile(albumName: String, cfg: AlbumFile.Config[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Stream[F, Option[Path]] = {

    val lookup: F[Option[Path]] =
      findSongFile(albumName, musicDirectory).
        map(_.flatMap(cfg.findFile)).
        compile.last.map(_.flatten)

    Stream.eval(cache.getOrCreate(cfg.cacheKey(musicDirectory, albumName), lookup))
  }

  /** Create the URL to the album file given a song file.
    */
  private def makeFileUrl(songFile: Path, basePath: Uri.Path, prefix: String): String = {
    import scala.collection.JavaConverters._

    val rel = musicDirectory.relativize(songFile)
    basePath.copy(segments = (basePath.segments :+ prefix) ++ rel.asScala.map(_.toString)).stringify
  }

  /** Create the URL to the album file given an album name.
    */
  private def makeAlbumUrl(albumName: String, basePath: Uri.Path, prefix: String): String = {
    import scodec.{Attempt, Err}

    val p = basePath/prefix
    val q = Uri.Query(Uri.QueryParameter.single("name", albumName) :: Nil)
    p.stringify + "?" + (Uri.Query.codec.encode(q).flatMap { bytes =>
      Attempt.fromEither(bytes.decodeUtf8.left.map(rsn => Err(s"Failed to decode UTF8: $rsn")))
    }).fold(err => throw new Exception(s"Unable to render query param: ${err.message}"), identity)
  }

}

object AlbumFile {

  case class Config[F[_]](dirCfg: DirectoryConfig
    , fileCfg: FilenameConfig
    , cacheDim: String
    , basePath: Uri.Path
    , missingRoutes: MissingRoutes[F]) {

    def findFile(f: Path): Option[Path] =
      FilenameConfig.findFile(f, dirCfg, fileCfg)

    def cacheKey(musicDirectory: Path, albumName: String) =
      s"${musicDirectory}::${albumName}::${cacheDim}"
  }

  case class AlbumFileInfo(exists: Boolean, fileUrl: Option[String])

  object AlbumFileInfo {
    implicit def jsonEncoder: Encoder[AlbumFileInfo] = deriveEncoder[AlbumFileInfo]
  }

  trait MissingRoutes[F[_]] {

    def byAlbum(album: String, noneMatch: Option[String]): Stream[F, HttpResponse[F]]

    def byFile(file: Path, noneMatch: Option[String]): Stream[F, HttpResponse[F]]
  }

  object MissingRoutes {

    def notFound[F[_]]: MissingRoutes[F] =
      new MissingRoutes[F] {
        def byAlbum(album: String, noneMatch: Option[String]): Stream[F, HttpResponse[F]] =
          Stream.emit(NotFound.emptyBody[F])

        def byFile(file: Path, noneMatch: Option[String]): Stream[F, HttpResponse[F]] =
          Stream.emit(NotFound.emptyBody[F])
      }

    def imagePlaceholder[F[_]: Effect](mpd: Mpd[F], pcfg: ProtocolConfig)
      (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): MissingRoutes[F] =
      new RoboHash[F](mpd, pcfg)
  }

  private final class RoboHash[F[_]: Effect](mpd: Mpd[F], pcfg: ProtocolConfig)
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext)
      extends MissingRoutes[F] {

    private val musicDirectory = mpd.cfg.musicDirectory.
      getOrElse(sys.error("Bug: Configuration problem: no music directory set in mpd config"))
    private val pictureUrl = ResourceFile.pictureUnsplash
    private[this] val logger = getLogger
    require(pictureUrl != null)

    def byAlbum(albumName: String, noneMatch: Option[String]): Stream[F, HttpResponse[F]] =
      urlContents(new URL(s"https://robohash.org/${albumName}.png"), None, None, Some("image/png"), Some(albumName))

    def byFile(file: Path, noneMatch: Option[String]): Stream[F, HttpResponse[F]] = {
      val song = musicDirectory.relativize(file.toAbsolutePath)
      findAlbumName(song).
        flatMap {
          case Some(name) => urlContents(new URL(s"https://robohash.org/${name}.png"), None, None, Some("image/png"), Some(name))
          case None => urlContents(pictureUrl, noneMatch)
        }
    }

    /** Ask MPD for metadata of the given song file to get the album name.
      */
    private def findAlbumName(file: Path): Stream[F, Option[String]] = {
      Stream.eval(Effect[F].delay(logger.debug(s"Find album for file '$file'"))).drain ++
      mpd.send(pcfg, Find(Filter(FilterType.File -> file.toString), None, Some(Range(0,1)))).
        map(_.toOption.flatMap(_.songs.songs.headOption)).
        map(_.flatMap(song => song.tags.get(Tag.Album)))
    }
  }

}
