package mpc4s.http

import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.Effect
import fs2.Stream
import scodec.{Attempt, Codec}
import spinoco.fs2.http
import spinoco.fs2.http.HttpResponse
import spinoco.fs2.http.routing._
import spinoco.protocol.http.{HttpRequestHeader, HttpResponseHeader}
import spinoco.protocol.http.codec.{HttpHeaderCodec, HttpRequestHeaderCodec, HttpResponseHeaderCodec}
import spinoco.protocol.http.header.HttpHeader

import org.log4s.{Error => _, _}

import mpc4s.http.config.ServerBind
import mpc4s.http.util.Responses

final class Server[F[_]: Effect](val app: App[F], val bind: ServerBind) {
  private final val logger = getLogger

  def create(implicit ec: ExecutionContext, acg: AsynchronousChannelGroup) = {
    http.server[F](
      bindTo = new InetSocketAddress(bind.host, bind.port),
      requestCodec = requestHeaderCodec,
      responseCodec = responseHeaderCodec,
      requestHeaderReceiveTimeout = 10.seconds,
      sendFailure = handleSendFailure _, // (Option[HttpRequestHeader], HttpResponse[F], Throwable) => Stream[F, Nothing],
      requestFailure = logRequestErrors _)(route(app.endpoints))
  }

  private def logRequestErrors(error: Throwable): Stream[F, HttpResponse[F]] = Stream.suspend {
    logger.error(error)("Error processing request")

    Stream.emit(Responses.fromError[F](error))
  }

  private def handleSendFailure(header: Option[HttpRequestHeader], response: HttpResponse[F], err:Throwable): Stream[F, Nothing] = {
    Stream.suspend {
      err match {
        case _: java.io.IOException if err.getMessage == "Broken pipe" || err.getMessage == "Connection reset by peer" =>
          logger.warn(s"Error sending response: ${err.getMessage}! Request headers: ${header}")
        case _ =>
          logger.error(err)(s"Error sending response! Request headers: ${header}")
      }
      Stream.empty
    }
  }


  private val headerCodec: Codec[HttpHeader]=
    HttpHeaderCodec.codec(Int.MaxValue)

  private def requestHeaderCodec: Codec[HttpRequestHeader] = {
    val codec = HttpRequestHeaderCodec.codec(headerCodec)
    Codec (
      h => codec.encode(h),
      v => codec.decode(v) match {
        case a: Attempt.Successful[_] => a
        case f@ Attempt.Failure(cause) =>
          logger.error(s"Error parsing request ${v.decodeUtf8} \n$cause")
          f
      }
    )
  }

  private def responseHeaderCodec: Codec[HttpResponseHeader] =
    HttpResponseHeaderCodec.codec(headerCodec)

}
