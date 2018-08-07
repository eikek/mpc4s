package mpc4s.protocol.codec.implicits

import mpc4s.protocol.codec._
import java.time.Instant

trait linecodec {

  implicit def optionCodec[A](implicit c: LineCodec[A]): LineCodec[Option[A]] =
    codecs.option(c)

  implicit val decimalCodec: LineCodec[BigDecimal] =
    codecs.decimal

  implicit val doubleCodec: LineCodec[Double] =
    codecs.double

  implicit val bigintCodec: LineCodec[BigInt] =
    codecs.bigint

  implicit val intCodec: LineCodec[Int] =
    codecs.int

  implicit val longCodec: LineCodec[Long] =
    codecs.long

  implicit val booleanCodec: LineCodec[Boolean] =
    codecs.boolean

  implicit val instantCodec: LineCodec[Instant] =
    codecs.instant
}


object linecodec extends linecodec
