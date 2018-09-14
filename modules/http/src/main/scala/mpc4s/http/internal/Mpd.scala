package mpc4s.http.internal

import fs2.{async, Stream}
import fs2.async.mutable.Semaphore
import cats.effect.{Effect, Sync}
import cats.Traverse
import cats.implicits._
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext
import spinoco.fs2.http.HttpResponse
import spinoco.fs2.http.routing.Route
import org.log4s._

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.client._
import mpc4s.http.config._
import mpc4s.http.util.all._

final class Mpd[F[_]: Effect](val cfg: MpdConfig, maxConn: Semaphore[F]) {
  private[this] val logger = getLogger

  def request(pc: ProtocolConfig, cmd: Command)
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Stream[F, HttpResponse[F]] =
    connect(pc).
      flatMap(_.send1(cmd, cfg.timeout.asScala)).
      map({
        case e: Response.MpdError => BadRequest.body(e)
        case s => Ok.body(s)
      })

  def send[C <: Command, A <: Answer](pc: ProtocolConfig, cmd: C)
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext, sel: SelectAnswer[C,A]): Stream[F, Response[A]] =
    connect(pc).flatMap(_.send(cmd, cfg.timeout.asScala))


  def connect(pc: ProtocolConfig)(implicit ACG: AsynchronousChannelGroup, ec: ExecutionContext): Stream[F, MpdClient[F]] =
    Stream.bracket(maxConn.decrement)(
      _ => Stream.eval(Effect[F].delay(Connect(cfg.host, cfg.port).withPassword(cfg.password))).
        map(c => MpdClient[F](c, pc, cfg.timeout.asScala, 32 * 1024, clientLogger)),
      _ => maxConn.increment)

  private def clientLogger: mpc4s.client.Logger = new mpc4s.client.Logger {
    def trace[G[_]: Sync](msg: => String): G[Unit] =
      Sync[G].delay(logger.trace(msg))
    def debug[G[_]: Sync](msg: => String): G[Unit] =
      Sync[G].delay(logger.debug(msg))
  }
}

final class Mpds[F[_]](val toMap: Map[String, Mpd[F]]) {

  val default: Mpd[F] = toMap("default")

  val size = toMap.size

  def map[B](f: (String, Mpd[F]) => B): List[B] =
    toMap.map(f.tupled).toList

  def mapRoute(f: (String, Mpd[F]) => Route[F]): Seq[Route[F]] =
    map(f)
}

object Mpds {
  def apply[F[_]: Effect](mc: MpdConfigs)(implicit EC: ExecutionContext): F[Mpds[F]] = {
    val tuplesInF: List[F[(String, Mpd[F])]] =
      mc.configs.map({ case (key, cfg) =>
        val sem = async.semaphore[F](cfg.maxConnections.toLong)
        sem.map(s => (key -> new Mpd(cfg, s)))
      }).toList

    Traverse[List].sequence(tuplesInF).
      map(ts => new Mpds[F](ts.toMap))
  }
}
