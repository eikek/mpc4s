package mpc4s.http.util

import spinoco.fs2.http.body.{BodyDecoder, BodyEncoder}
import spinoco.protocol.mime._
import spinoco.fs2.http.routing.{body => rbody, Matcher}
import scodec.{Attempt, Err}
import scodec.bits.ByteVector
import io.circe.{Json, Encoder, Decoder}, io.circe.parser._, io.circe.syntax._
import cats.effect.Effect

trait JsonBody {

  private def parseJson(b: ByteVector): Attempt[Json] =
    for {
      str <- b.decodeUtf8.attempt
      json <- parse(str).attempt
    } yield json

  private def decodeJson[A](b: ByteVector)(implicit dec: Decoder[A]): Attempt[A] =
    for {
      json <- parseJson(b)
      a <- dec.decodeJson(json).attempt
    } yield a


  implicit def jsonBodyDecoder[A](implicit jd: Decoder[A]): BodyDecoder[A] =
    BodyDecoder { (bs, ct) =>
      if (ct.mediaType == MediaType.`application/json`) decodeJson(bs)
      else Attempt.failure(Err(s"Unsupported content type: $ct"))
    }

  implicit def jsonBodyEncoder[A](implicit je: Encoder[A]): BodyEncoder[A] =
    BodyEncoder(ContentType.TextContent(MediaType.`application/json`, Some(MIMECharset.`UTF-8`))) { a =>
      ByteVector.encodeUtf8(a.asJson.noSpaces).attempt
    }

  // implicit def validatedErrorBodyEncoder[A](implicit encA: Encoder[A]): BodyEncoder[Validated[Error, A]] =
  //   BodyEncoder(ContentType.TextContent(MediaType.`application/json`, Some(MIMECharset.`UTF-8`))) { a =>
  //     a.fold(
  //       e => ByteVector.encodeUtf8(e.asJson.spaces2).attempt,
  //       a => ByteVector.encodeUtf8(a.asJson.spaces2).attempt
  //     )
  //   }

  def jsonBody[F[_], A](implicit d: BodyDecoder[A], F: Effect[F]): Matcher[F, A] = rbody[F].as[A]

  implicit final class EitherAttempt[A, B](e: Either[A,B]) {
    def attempt: Attempt[B] = Attempt.fromEither(e.left.map(a => Err(a.toString)))
  }

  implicit final class StringOps(s: String) {
    def asNonEmpty: Option[String] = Option(s).map(_.trim).filter(_.nonEmpty)
  }
}

object JsonBody extends JsonBody
