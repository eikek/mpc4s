package mpc4s.http.routes

import java.nio.file.Path
import fs2.Stream
import cats.effect.Effect
import cats.implicits._
import shapeless.{::, HNil}
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext
import spinoco.fs2.http.routing._
import org.log4s._
import java.net.URL

import mpc4s.protocol._
import mpc4s.protocol.codec.ProtocolConfig
import mpc4s.protocol.commands._
import mpc4s.http.internal._
import mpc4s.http.config._
import mpc4s.http.util._
import mpc4s.http.util.all._

object Cover {
  private[this] val logger = getLogger

  private val pictureUrl = ResourceFile.pictureUnsplash

  require(pictureUrl != null)

  def apply[F[_]: Effect](cfg: ProtocolConfig
    , mpd: Mpd[F]
    , musicDirectory: Path
    , coverCfg: CoverConfig
    , cache: PathCache[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] =
    choice(byFile(musicDirectory, coverCfg)
      , byFileMissing(musicDirectory, cfg, mpd)
      , byAlbum(cfg, mpd, musicDirectory, coverCfg, cache)
      , clearCache(cache)
    )

  def byFile[F[_]: Effect](rootDir: Path, coverCfg: CoverConfig): Route[F] =
    "file" / Get >> coverFile(rootDir, coverCfg) :: ifNoneMatch map {
      case cover :: noneMatch :: HNil =>
        staticFile(cover, rootDir, noneMatch)
    }

  def byFileMissing[F[_]: Effect](rootDir: Path, cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {
    "file" / Get >> asFile(rootDir) :: ifNoneMatch map {
      case file :: noneMatch :: HNil =>
        val song = rootDir.relativize(file.toAbsolutePath)
        findAlbumName(song, cfg, mpd).
          flatMap {
            case Some(name) => urlContents(new URL(s"https://robohash.org/${name}.png"), None, None, Some("image/png"), Some(name))
            case None => urlContents(pictureUrl, noneMatch)
          }
    }
  }

  def byAlbum[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F], musicDirectory: Path, coverCfg: CoverConfig, cache: Cache[F,String,Option[Path]])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] =
    "album" / Get >> param[String]("name") :: ifNoneMatch map {
      case albumName :: noneMatch :: HNil =>
        logger.trace(s"Find cover art for album '$albumName'")
        val op: F[Option[Path]] =
          mpd.send(cfg, Find(Filter(FilterType(Tag.Album) -> albumName), None, Some(Range(0,1)))).
            map(_.toOption.flatMap(_.songs.songs.headOption)).
            map(_.headOption.map(song => musicDirectory / song.file.uri)).
            map(_.flatMap(findCoverFile(_, coverCfg))).
            compile.last.map(_.flatten)

        val key = musicDirectory.toString + "::" + albumName
        Stream.eval(cache.getOrCreate(key, op)).
          flatMap {
            case Some(file) if file.exists => staticFile(file, musicDirectory, noneMatch)
            case _ => urlContents(new URL(s"https://robohash.org/${albumName}.png"), None, None, Some("image/png"), Some(albumName))
          }
    }

  def clearCache[F[_]: Effect](cache: PathCache[F]): Route[F] =
    "clearcache" / Post map { _ =>
      Stream.eval(cache.clear).
        map(_ => Ok.emptyBody[F])
    }

  private def findAlbumName[F[_]: Effect](file: Path, cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Stream[F, Option[String]] = {
    logger.debug(s"Find album for file '$file'")
    mpd.send(cfg, Find(Filter(FilterType.File -> file.toString), None, Some(Range(0,1)))).
      map(_.toOption.flatMap(_.songs.songs.headOption)).
      map(_.flatMap(song => song.tags.get(Tag.Album)))
  }
}
