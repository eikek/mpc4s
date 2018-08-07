package mpc4s.http.routes

import cats.effect.Effect
import shapeless.{::, HNil}
import spinoco.fs2.http.routing._
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext

import mpc4s.protocol.codec.ProtocolConfig
import mpc4s.protocol.commands._
import mpc4s.http.internal.Mpd
import mpc4s.http.util.all._

object Special {

  def apply[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {
    choice(search(cfg, mpd)
      , find(cfg, mpd)
      , list(cfg, mpd)
      , count(cfg, mpd)
      , currentsong(cfg, mpd)
      , status(cfg, mpd)
      , stats(cfg, mpd)
      , playlistinfo(cfg, mpd)
      , listplaylists(cfg, mpd))
  }

  def search[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "search" / Get >> filter :: sort.? :: range.? map {
      case q :: sort :: range :: HNil =>
        mpd.request(cfg, Search(q, sort, range))
    }
  }

  def find[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "find" / Get >> filter :: sort.? :: range.? map {
      case q :: sort :: range :: HNil =>
        mpd.request(cfg, Find(q, sort, range))
    }
  }

  def list[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "list" / Get >> listType :: filter.? map {
      case tag :: q :: HNil =>
        mpd.request(cfg, List(tag, q))
    }
  }

  def count[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "count" / Get >> filter map { filter =>
      mpd.request(cfg, Count.FilterOnly(filter))
    }
  }

  def currentsong[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "currentsong" / Get map { _ =>
      mpd.request(cfg, CurrentSong)
    }
  }

  def status[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "status" / Get map { _ =>
      mpd.request(cfg, Status)
    }
  }

  def stats[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "stats" / Get map { _ =>
      mpd.request(cfg, Stats)
    }
  }

  def playlistinfo[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "playlistinfo" / Get >> param[Int]("pos").? :: range.? map {
      case pos :: window :: HNil =>
        val arg = pos.map(Right(_)).orElse(window.map(Left(_)))

        mpd.request(cfg, PlaylistInfo(arg))
    }
  }

  def listplaylists[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {

    "listplaylists" / Get map { _ =>
      mpd.request(cfg, ListPlaylists)
    }
  }

}
