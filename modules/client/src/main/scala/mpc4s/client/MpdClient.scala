package mpc4s.client

import fs2._
import cats.effect.Effect
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import java.nio.channels.AsynchronousChannelGroup
import mpc4s.protocol._
import mpc4s.protocol.commands.Idle
import mpc4s.protocol.codec._

trait MpdClient[F[_]] {

  /** Connect to MPD. The connection is closed once the returned
    * single-element stream terminates. That means it can only be used
    * “inside” that stream; using `flatMap` for example.
    *
    * Note that MPD has server side timeouts for idle connections. If
    * you want to (re)use a connection for a longer time, see
    * `idle`.
    */
  def connect: Stream[F, MpdConnection[F]]

  /** Using MPD idle concept to hold one connection open in order to
    * receive notifications and send commands.
    *
    * The given `Idle` command tells what events to listen to. The
    * returned `MpdIdle` interface allows to read responses and send
    * commands using the same single connection.
    */
  def idle(cmd: Idle): Stream[F, MpdIdle[F]]

  /** Same as `idle` and listens for all events.
    */
  def idle: Stream[F, MpdIdle[F]] = idle(Idle.All)

  /** Opens a new connection to MPD and sends the given command. The
    * response is then read and decoded using the given `LineCodec`.
    */
  def send[A <: Answer](req: Command, ac: LineCodec[Response[A]], timeout: Duration): Stream[F, Response[A]] =
    connect.evalMap(_.requestDecodeWith(req, timeout, ac))

  /** Opens a new connection to MPD and sends the given command. The
    * response is then read and decoded using the `LineCodec` looked
    * up from implicit scope.
    */
  def send[C <: Command, A <: Answer]
    (req: C, timeout: Duration)
    (implicit sel: SelectAnswer[C,A]): Stream[F, Response[A]] = {
    connect.evalMap(_.requestDecode(req, timeout))
  }

  /** Opens a new connection to MPD and sends the given command. The
    * response is then read and decoded using a `LineCodec` that was
    * registered when `MpdClient` was created.
    */
  def send1(cmd: Command, timeout: Duration): Stream[F, Response[Answer]] =
    connect.evalMap(_.requestDecode1(cmd, timeout))

  /** Opens a new connection to MPD and sends all commands sequentially
    * one after the other. Then the responses are read and decoded
    * using decoders registered at creation time of `MpdClient` for
    * each command.
    *
    * Since responses are read after all commands have been submitted,
    * they have to be buffered somewhere.
    */
  def sendN(cmds: Seq[Command], timeout: Duration): Stream[F, Response[Answer]]
}

object MpdClient {

  def apply[F[_]: Effect](connectInfo: Connect
    , config: ProtocolConfig = CommandCodec.defaultConfig
    , connectionTimeout: Duration = 5.seconds
    , chunkSize: Int = 16384
    , logger: Logger = Logger.none)
    (implicit ACG: AsynchronousChannelGroup, ec: ExecutionContext): MpdClient[F] =
    new MpdClient[F] {

      def connect: Stream[F, MpdConnection[F]] =
        MpdConnection[F](connectInfo, config, connectionTimeout, chunkSize, logger)

      def idle(cmd: Idle): Stream[F, MpdIdle[F]] =
        connect.evalMap(conn => MpdIdle(conn, config, cmd, logger))

      def sendN(cmds: Seq[Command], timeout: Duration): Stream[F, Response[Answer]] = {
        val decoders = cmds.map(_.name).flatMap(config.get).map(_.responseCodec).toList
        if (decoders.size != cmds.size) Stream.raiseError(new Exception(s"Cannot find decoders for all commands: $cmds"))
        else connect.flatMap { conn =>
          val send = Stream.emits(cmds).evalMap(c => conn.send(c, timeout))
          val recv = conn.reads(timeout).
            zip(Stream.emits(decoders)).
            take(cmds.size.toLong).
            map(t => t._2.parseValue(t._1)).
            flatMap {
              case Right(a) => Stream.emit(a)
              case Left(err) => Stream.raiseError(new Exception(err.message))
            }

          send.drain ++ recv
        }
      }
    }
}
