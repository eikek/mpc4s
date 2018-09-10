package mpc4s.http.routes

import fs2.Scheduler
import cats.effect.Effect
import spinoco.fs2.http.routing._
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext

import mpc4s.protocol.codec.ProtocolConfig
import mpc4s.http._
import mpc4s.http.internal._
import mpc4s.http.util.all._

object Endpoint {

  def apply[F[_]: Effect](cfg: ServerConfig[F], cache: PathCache[F], mpds: Mpds[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext, SCH: Scheduler): Route[F] = {

    val pcfg = cfg.protocolConfig

    choice(
      "mpd"/createMpdRoutes(pcfg, mpds),
      "mpdspecial"/createSpecialRoutes(pcfg, mpds),
      "cover"/createAlbumFileRoutes(mpds, AlbumFileRoute.cover(cache, cfg)),
      "booklet"/createAlbumFileRoutes(mpds, AlbumFileRoute.booklet(cache, cfg)),
      "info"/cut(Version(cfg.app.mpd))
    )
  }

  def createMpdRoutes[F[_]: Effect](pcfg: ProtocolConfig, mpds: Mpds[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext, SCH: Scheduler): Route[F] =
    if (mpds.size <= 1) makeDefaultRoute(MpdRequest(pcfg, mpds.default))
    else makeAllRoute(mpds, mpd => MpdRequest(pcfg, mpd))

  def createSpecialRoutes[F[_]: Effect](pcfg: ProtocolConfig, mpds: Mpds[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] =
    if (mpds.size <= 1) makeDefaultRoute(Special(pcfg, mpds.default))
    else makeAllRoute(mpds, mpd => Special(pcfg, mpd))

  def createAlbumFileRoutes[F[_]: Effect](mpds: Mpds[F], routes: AlbumFileRoute[F]): Route[F] =
    if (mpds.size <= 1) makeDefaultRoute(routes(mpds.default))
    else makeAllRoute(mpds, routes)

  private def makeDefaultRoute[F[_]](route: Route[F]): Route[F] =
    choice("default"/cut(route), cut(route))

  private def makeAllRoute[F[_]](mpds: Mpds[F], f: Mpd[F] => Route[F]): Route[F] = {
    val md = cut(f(mpds.default))
    val seq: Seq[Route[F]] = mpds.mapRoute((name, mpd) => name/cut(f(mpd)))

    choice(seq.head, (seq.tail :+ md): _*)
  }

  case class AlbumFileRoute[F[_]: Effect]
    (f: Mpd[F] => (AlbumFile[F], AlbumFile.Config[F]))
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext)
      extends (Mpd[F] => Route[F]) {

    def apply(mpd: Mpd[F]): Route[F] = {
      val (albumInfo, in) = f(mpd)
      albumInfo.all(in)
    }
  }

  object AlbumFileRoute {

    def cover[F[_]: Effect](cache: PathCache[F], serverCfg: ServerConfig[F])
      (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): AlbumFileRoute[F] =
      AlbumFileRoute(mpd =>
        ( new AlbumFile(mpd, cache, serverCfg.protocolConfig)
        , AlbumFile.Config(serverCfg.app.albumFile
          , serverCfg.app.cover
          , "cover"
          , AlbumFile.MissingRoutes.imagePlaceholder(mpd, serverCfg.protocolConfig)))
        )

    def booklet[F[_]: Effect](cache: PathCache[F], serverCfg: ServerConfig[F])
      (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): AlbumFileRoute[F] =
      AlbumFileRoute(mpd =>
        ( new AlbumFile(mpd, cache, serverCfg.protocolConfig)
        , AlbumFile.Config(serverCfg.app.albumFile
          , serverCfg.app.booklet
          , "booklet"
          , AlbumFile.MissingRoutes.notFound))
        )
  }
}
