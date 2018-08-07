package mpc4s.protocol.codec.codecs

import shapeless._
import mpc4s.protocol.codec._

trait HListLineCodec {

  implicit val hnil: LineCodec[HNil] =
    codecs.ignore.xmap[HNil](_ => HNil, _ => ())

  implicit def hcons[A, L <: HList](implicit ac: LineCodec[A], lc: LineCodec[L]): LineCodec[A :: L] =
    LineCodec(
      { str =>
        for {
          ar <- ac.parse(str)
          lr <- lc.parse(ar.rest)
        } yield ParseResult(ar.value :: lr.value, lr.rest)
      },
      { case a :: l =>
        for {
          as <- ac.write(a)
          ls <- lc.write(l)
        } yield as + ls
      }
    )

}

object HListLineCodec extends HListLineCodec
