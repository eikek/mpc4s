package mpc4s.client

import fs2._
import fs2.io.tcp.Socket
import cats.effect.Effect
import cats.implicits._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import java.nio.channels.AsynchronousChannelGroup
import org.log4s._
import mpc4s.protocol._
import mpc4s.protocol.codec._

trait MpdConnection[F[_]] {

  /** Return mpd protocol version line. */
  def protocolVersion: String

  /** Reads the next response from the socket and returns it as
    * string */
  def read(timeout: Duration): F[String]

  /** Reads the next response from the socket and decodes it using the
    * given decoder.
    */
  def readDecode[A <: Answer](timeout: Duration, codec: LineCodec[Response[A]]): F[Response[A]]

  /** Continually reads responses from the socket. */
  def reads(timeout: Duration): Stream[F, String]

  /** Send the command to mpd  */
  def send(req: Command, timeout: Duration): F[Unit]

  /** Send a list of commands to mpd using mpds `command_list` feature.
    */
  def sendList(req: CommandList, timeout: Duration): F[Unit]

  /** Sends the command to mpd and returns the response that is read
    * immediatly after the command has been submitted. */
  def request(req: Command, timeout: Duration): F[String]

  /** Sends the command to mpd and returns the response that is read and
    * decoded using the given decoder immediatly after the command has
    * been submitted.
    */
  def requestDecodeWith[A <: Answer](req: Command, timeout: Duration, codec: LineCodec[Response[A]]): F[Response[A]]

  /** Sends the command to mpd and returns the response that is read and
    * decoded immediatly after the command has been submitted.
    *
    * The decoder is looked up from implicit scope.
    */
  def requestDecode[C <: Command, A <: Answer]
    (req: C, timeout: Duration)
    (implicit sel: SelectAnswer[C,A]): F[Response[A]]

  /** Sends the command to mpd and returns the response that is read and
    * decoded immediatly after the command has been submitted.
    *
    * A default decoder is used that has been registered with the
    * given command.
    */
  def requestDecode1(cmd: Command, timeout: Duration): F[Response[Answer]]

}

object MpdConnection {
  private[this] val logger = getLogger

  def apply[F[_]](connect: Connect
    , config: ProtocolConfig = CommandCodec.defaultConfig
    , connectionTimeout: Duration = 5.seconds
    , chunkSize: Int = 32 * 1024)
    (implicit ACG: AsynchronousChannelGroup, ec: ExecutionContext, F: Effect[F]): Stream[F, MpdConnection[F]] =
    io.tcp.client(connect.address).
      evalMap({ socket =>
        socketRead(socket, chunkSize, connectionTimeout).
          through(text.utf8Decode).
          through(text.lines).
          take(1).
          compile.toVector.
          map(initialLine =>
            new MpdConnection[F] {
              logger.debug(s"$connect: ${initialLine.headOption.map(_.trim)}")

              val commandCodec = CommandCodec.createCodec(config)
              val commandListCodec = CommandList.codec(commandCodec)
              def protocolVersion = initialLine.headOption.map(_.trim).getOrElse("")

              def reads(timeout: Duration): Stream[F, String] =
                socketRead(socket, chunkSize, timeout).
                  through(ResponseSplit.responsesUtf8(chunkSize))

              def read(timeout: Duration): F[String] =
                reads(timeout).take(1).
                  compile.last.flatMap {
                    case Some(s) => F.pure(s)
                    case None => F.raiseError(new Exception("No response"))
                  }

              def readDecode[A <: Answer](timeout: Duration, codec: LineCodec[Response[A]]): F[Response[A]] =
                read(timeout).
                  map(codec.parseValue).
                  flatMap {
                    case Right(r) => F.pure(r)
                    case Left(err) => F.raiseError(new Exception(err.message))
                  }

              def send(cmd: Command, timeout: Duration): F[Unit] =
                requestStream[F](cmd, commandCodec).
                  to(socketWrite(socket, timeout)).
                  last.
                  evalMap(_ => F.delay(logger.trace(s"Sent command $cmd"))).
                  compile.drain

              def sendList(req: CommandList, timeout: Duration): F[Unit] =
                requestListStream[F](req, commandListCodec).
                  to(socketWrite(socket, timeout)).
                  last.
                  evalMap(_ => F.delay(logger.trace(s"Sent command list $req"))).
                  compile.drain

              def request(req: Command, timeout: Duration): F[String] =
                send(req, timeout).flatMap(_ => read(timeout))

              def requestDecodeWith[A <: Answer](req: Command, timeout: Duration, codec: LineCodec[Response[A]]): F[Response[A]] =
                send(req, timeout).flatMap(_ => readDecode(timeout, codec))

              def requestDecode[C <: Command, A <: Answer]
                (req: C, timeout: Duration)
                (implicit sel: SelectAnswer[C,A]): F[Response[A]] =
                send(req, timeout).flatMap(_ => readDecode(timeout, sel.codec))

              def requestDecode1(cmd: Command, timeout: Duration): F[Response[Answer]] =
                config.get(cmd.name).map(_.responseCodec) match {
                  case Some(codec) =>
                    request(cmd, timeout).flatMap { str =>
                      codec.parseValue(str) match {
                        case Right(res) => F.pure(res.asInstanceOf[Response[Answer]])
                        case Left(err) => F.raiseError(new Exception(err.message))
                      }
                    }
                  case None =>
                    F.raiseError(new Exception(s"No response codec for command: $cmd"))
                }
          })
      })

  private def socketWrite[F[_]](socket: Socket[F], timeout: Duration): Sink[F, Byte] =
    timeout match {
      case fin: FiniteDuration =>
        socket.writes(Some(fin))
      case _ =>
        socket.writes(None)
    }

  private def socketRead[F[_]](socket: Socket[F], chunkSize: Int, timeout: Duration): Stream[F, Byte] = {
    val bytes = timeout match {
      case fin: FiniteDuration =>
        socket.reads(chunkSize, Some(fin))
      case _ =>
        socket.reads(chunkSize, None)
    }
    // note: without `buffer(1)` or `map(i => i)` the last 2 or 3 responses are gone…?
    bytes
  }

  private def requestStream[F[_]](req: Command, codec: LineCodec[Command]): Stream[F, Byte] =
    Stream(codec.write(req)).
      map(_.left.map(err => new Exception(err.message))).
      rethrow.
      through(text.utf8Encode) ++ Stream.emit('\n'.toByte)

  private def requestListStream[F[_]](req: CommandList, codec: LineCodec[CommandList]): Stream[F, Byte] =
    Stream(codec.write(req)).
      map(_.left.map(err => new Exception(err.message))).
      rethrow.
      through(text.utf8Encode) ++ Stream.emit('\n'.toByte)

}
