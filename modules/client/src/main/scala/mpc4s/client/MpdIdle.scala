package mpc4s.client

import fs2._
import cats.effect.{Effect, Sync}
import cats.implicits._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import org.log4s._

import mpc4s.protocol._
import mpc4s.protocol.codec.{LineCodec, ProtocolConfig}
import mpc4s.protocol.answer.{GenericAnswer, IdleAnswer}
import mpc4s.protocol.commands.{Idle, NoIdle}

/** Use one persistent connection to mpd to send commands and listen
  * for notifications.
  *
  * This will initiate the idle command to wait for notifications from
  * mpd. When another command is issued, the idling is cancelled, the
  * command is send and afterwards the connection goes back to idle.
  *
  * To explicitely close the connection, simply `write` a `Close`
  * or `NoIdle` command.
  */
trait MpdIdle[F[_]] {

  /** Continually reads responses from mpd. This may be an
    * `IdleAnswer` or any answer to a command issued using
    * `write`.
    *
    * The answer is decoded using a registered `LineCodec` for each
    * command. If for some reason there is none, a `GenericAnswer`
    * is used that contains the string as is.
    */
  def read: Stream[F, Response[Answer]]

  /** Sends this command using the current connection. First the noidle
    * command is issued, then the given command. At last the
    * connection goes back to idle.
    */
  def write(cmd: Command): F[Unit]

  /** Sends every command from input stream to MPD.
    */
  def writeSink: Sink[F, Command]
}

object MpdIdle {
  private[this] val logger = getLogger

  private def debug[F[_]: Sync](msg: => String): F[Unit] =
    Sync[F].delay(logger.debug(msg))

  private[client] def apply[F[_]: Effect](conn: MpdConnection[F], cfg: ProtocolConfig, idle: Idle)
    (implicit EC: ExecutionContext): F[MpdIdle[F]] = {
    val timeout = 5.seconds

    def findCodec(name: CommandName): LineCodec[Response[Answer]] =
      cfg.get(name).
        map(_.responseCodec).
        getOrElse(GenericAnswer.codec).
        asInstanceOf[LineCodec[Response[Answer]]]

    async.unboundedQueue[F, CommandName].flatMap { nameQ =>
    async.semaphore[F](1).flatMap { permit =>
    async.signalOf(false).flatMap { idleState =>

      // send command and memorize its name to be able to get the
      // correct codec for the answer
      def send(cmd: Command): F[Unit] = {
        conn.send(cmd, timeout) >> nameQ.enqueue1(cmd.name)
      }

      def setIdle: F[Unit] =
        idleState.get.
          flatMap {
            case true => Effect[F].pure(())
            case false =>
              send(idle) >> idleState.set(true)
          }

      // uses the response codec from previous idle command; so do not
      // enqueue name. This is ok, since its only an empty response.
      def breakIdle: F[Unit] =
        idleState.get.
          flatMap {
            case false => Effect[F].pure(())
            case true =>
              conn.send(NoIdle, timeout) >> idleState.set(false)
          }

      setIdle.map { _ =>
        new MpdIdle[F] {
          def read: Stream[F, Response[Answer]] =
            conn.reads(Duration.Inf).
              zip(nameQ.dequeue).
              map(t => findCodec(t._2).parseValue(t._1)).
              flatMap({
                case Right(res) => Stream(res.asInstanceOf[Response[Answer]])
                case Left(err) => Stream.raiseError(new Exception(err.message))
              }).
              flatMap({
                case r@ Response.MpdResult(IdleAnswer(events)) =>
                  val res: Response[Answer] = r
                  if (events.nonEmpty) Stream.bracket(permit.decrement)(
                    _ => Stream.eval(setIdle.map(_ => res)),
                    _ => permit.increment)
                  else Stream.empty
                case r => Stream.emit(r)
              })

          def write(cmd: Command): F[Unit] = {
            Stream.bracket(permit.decrement)(
              _ => Stream.eval(debug(s"Send command $cmd") >> breakIdle >> send(cmd) >> setIdle),
              _ => permit.increment).
              compile.drain
          }

          def writeSink: Sink[F, Command] =
            _.evalMap(write)
        }
      }
    }}}
  }
}
