package mpc4s.player

import fs2.Stream
import cats.effect.Sync
import cats.implicits._
import shapeless.{HNil, ::}
import spinoco.fs2.http.routing._

import mpc4s.http.config.AppConfig
import mpc4s.http.util.all._
import mpc4s.player.webjar.Webjars

object Endpoint {

  def apply[F[_]: Sync](cfg: AppConfig): Route[F] = {
    val toc = Toc.readToc
    val mpdConns = cfg.mpd.map((id, c) => Index.MpdConnections(id, c.title))
    choice(assets(toc), index(toc, Index.Flags(cfg.baseurl, mpdConns)))
  }

  def assets[F[_]](toc: F[Webjars.Toc])(implicit F: Sync[F]): Route[F] = {
    Get >> "static" / as[String] :/: restPath :: ifNoneMatch map {
      case name :: rest :: noneMatch :: HNil =>
        Stream.eval(toc).
          flatMap(Assets.lookup(name, rest, _, noneMatch))
    }
  }

  def index[F[_]: Sync](toc: F[Webjars.Toc], flags: Index.Flags): Route[F] =
    Get >> choice(empty, "index.html") >> ifNoneMatch map { noneMatch =>
      Stream.eval(toc).
        flatMap(Index.lookup(flags, _, noneMatch))
    }
}
