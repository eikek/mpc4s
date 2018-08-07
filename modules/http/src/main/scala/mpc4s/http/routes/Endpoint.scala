package mpc4s.http.routes

import fs2.Scheduler
import cats.effect.Effect
import spinoco.fs2.http.routing._
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext

import mpc4s.protocol.codec.ProtocolConfig
import mpc4s.http._
import mpc4s.http.config.AppConfig
import mpc4s.http.internal._
import mpc4s.http.util.all._

object Endpoint {

  def apply[F[_]: Effect](cfg: ServerConfig[F], cache: PathCache[F], mpds: Mpds[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext, SCH: Scheduler): Route[F] = {

    val pcfg = cfg.protocolConfig
    val mcfg = cfg.app.mpd.default

    choice(
      "mpd"/createMpdRoutes(pcfg, mpds),
      "mpdspecial"/createSpecialRoutes(pcfg, mpds),
      "cover"/createCoverRoutes(pcfg, mpds, cfg.app, cache),
      "info"/cut(Version(mcfg))
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

  def createCoverRoutes[F[_]: Effect](pcfg: ProtocolConfig, mpds: Mpds[F], appCfg: AppConfig, cache: PathCache[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] =
    if (mpds.size <= 1) {
      makeDefaultRoute(Cover(pcfg, mpds.default, appCfg.musicDirectory, appCfg.cover, cache))
    } else {
      makeAllRoute(mpds, mpd => {
        val musicDirectory = mpd.cfg.musicDirectory.
          getOrElse(sys.error("Bug: Configuration problem: no music directory set in mpd config"))

        Cover(pcfg, mpd, musicDirectory, appCfg.cover, cache)
      })
    }

  private def makeDefaultRoute[F[_]](route: Route[F]): Route[F] =
    choice("default"/cut(route), cut(route))

  private def makeAllRoute[F[_]](mpds: Mpds[F], f: Mpd[F] => Route[F]): Route[F] = {
    val md = cut(f(mpds.default))
    val seq: Seq[Route[F]] = mpds.mapRoute((name, mpd) => name/cut(f(mpd)))

    choice(seq.head, (seq.tail :+ md): _*)
  }
}
