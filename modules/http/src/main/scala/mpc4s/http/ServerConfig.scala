package mpc4s.http

import cats.effect.Effect

import spinoco.fs2.http.routing._
import mpc4s.protocol.codec.ProtocolConfig
import mpc4s.http.config.AppConfig

case class ServerConfig[F[_]](app: AppConfig
  , protocolConfig: ProtocolConfig
  , playerRoute: Option[Route[F]])

object ServerConfig {

  def create[F[_]: Effect](cfg: AppConfig, protocolConfig: ProtocolConfig, playerRoute: Option[Route[F]]): F[ServerConfig[F]] = {
    Effect[F].pure(ServerConfig[F](cfg, protocolConfig, playerRoute))
  }

}
