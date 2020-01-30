package mpc4s.http.routes

import fs2.{Scheduler, Stream}
import cats.effect.Effect
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext
import spinoco.fs2.http.routing._
import spinoco.fs2.http.websocket.Frame
import scodec._
import scodec.bits.BitVector
import io.circe.{Decoder => JsonDecoder}
import io.circe.syntax._, io.circe.parser._

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.http._
import mpc4s.http.internal.Mpd
import mpc4s.http.util.all._

object MpdRequest {

  def apply[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext, SCH: Timer[F]): Route[F] = {

    cut(choice(command(cfg,  mpd), idle(cfg, mpd)))
  }

  def command[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, EC: ExecutionContext): Route[F] = {
    val codec = CommandCodec.createCodec(cfg)

    empty >> Post >> mpdCommand(codec) map { cmd =>
      if (cmd.name != commands.Idle.name && cmd.name != commands.NoIdle.name) mpd.request(cfg, cmd)
      else Stream(BadRequest.body[F, Error](Error("The idle/noidle commands cannot be used with this api")))
    }
  }

  def idle[F[_]: Effect](cfg: ProtocolConfig, mpd: Mpd[F])
    (implicit ACG: AsynchronousChannelGroup, S: Timer[F], EC: ExecutionContext): Route[F] = {

    val cc = CommandCodec.createCodec(cfg)
    val codec = CommandCodec.commandOrListCodec(cc)
    implicit val cmdDec: Decoder[JsonCommand] = commandFrameDecoder
    implicit val ansEnc: Encoder[Response[Answer]] = answerFrameEncoder

    empty >> Get >> websocket[F, JsonCommand, Response[Answer]]() map { pipef =>
      mpd.connect(cfg).
        flatMap(_.idle).
        flatMap(idle => pipef(in => {
          val write: Stream[F, Unit] = in.
            map(jc => codec.parseValue(jc.a.command).left.map(err => new Exception(s"command: '${jc.a.command}': ${err.message}"))).
            rethrow.
            to(idle.writeSink)

          val read: Stream[F, Frame[Response[Answer]]] = idle.read.map(Frame.Text.apply)

          write.drain.merge(read).onFinalize(idle.write(CommandOrList(commands.Close)))
        }))
    }
  }

  private def commandFrameDecoder(implicit dec: JsonDecoder[JsonCommand]): Decoder[JsonCommand] =
    Decoder({ bv =>
      val decoded: Either[Err, String] =
        bv.decodeUtf8.left.map(e => Err(e.getMessage))

      val parsed: Either[Err, DecodeResult[JsonCommand]] =
        decoded.
          flatMap(str => parse(str).left.map(e => Err(e.getMessage))).
          flatMap(_.as[JsonCommand].left.map(e => Err(e.getMessage))).
          map(cmd => DecodeResult(cmd, BitVector.empty))

      parsed.fold(Attempt.failure, Attempt.successful)
    })


  private def answerFrameEncoder: Encoder[Response[Answer]] =
    Encoder { resp => BitVector.encodeUtf8(resp.asJson.noSpaces).
      left.map(ex => Err(ex.getMessage)).
      fold(Attempt.failure, Attempt.successful)
    }
}
