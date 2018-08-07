package mpc4s.http

import fs2.Scheduler
import cats.effect.Effect
import cats.implicits._
import spinoco.fs2.http.routing._
import spinoco.protocol.http.header.{HttpHeader, GenericHeader, Server => ServerHeader}
import spinoco.protocol.http.header.value.{ServerProduct, ProductDescription}
import java.nio.channels.AsynchronousChannelGroup
import java.nio.file.Path
import scala.concurrent.ExecutionContext

import mpc4s.protocol.codec.ProtocolConfig
import mpc4s.http.config._
import mpc4s.http.internal._
import mpc4s.http.routes.{CustomContent, Endpoint}
import mpc4s.http.util.all._

final class App[F[_]](coreConfig: ServerConfig[F], cache: PathCache[F], mpds: Mpds[F])
  (implicit F: Effect[F], ACG: AsynchronousChannelGroup, EC: ExecutionContext, SCH: Scheduler) {

  val config = coreConfig.app

  def endpoints: Route[F] =
    withDefaultHeaders(choice(
      "api"/"v1"/cut(Endpoint(coreConfig, cache, mpds)),
      "custom"/cut(CustomContent(config.customContent)),
      "player"/cut(coreConfig.playerRoute.getOrElse(Matcher.respond(NotFound.emptyBody)))
    ))

  private def withDefaultHeaders: Route[F] => Route[F] =
    r => r.map(_.map(resp => defaultHeaders.foldLeft(resp)(_ withHeader _)))

  private def defaultHeaders: List[HttpHeader] = List(
    GenericHeader("X-Content-Type-Options", "nosniff"),
    ServerHeader(ServerProduct(ProductDescription(Version.projectString, None) :: Nil))
  )

  def start: F[Unit] = F.pure(())

  def stop: F[Unit] = F.pure(())
}

object App {

  def apply[F[_]: Effect](cfg: ServerConfig[F], cache: Cache[F,String,Option[Path]], mpds: Mpds[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext, SCH: Scheduler) =
    new App(cfg, cache, mpds)

  def create[F[_]: Effect](cfg: AppConfig, protocolConfig: ProtocolConfig, playerRoute: Option[Route[F]])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext, SCH: Scheduler): F[App[F]] =
    for {
      cache <- Cache.empty[F,String,Option[Path]](cfg.cover.cacheSize)
      cfg   <- ServerConfig.create(cfg, protocolConfig, playerRoute)
      mpds  <- Mpds(cfg.app.mpd)
    } yield App(cfg, cache, mpds)

}
