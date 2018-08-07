package mpc4s.protocol.codec.syntax

import shapeless._
import shapeless.ops.hlist._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.codecs._
import mpc4s.protocol.internal.DropUnits

trait linecodec {

  implicit final class LineCodecHListOps[L <: HList](c: LineCodec[L]) {

    def :: [B](b: LineCodec[B]): LineCodec[B :: L] = {
      HListLineCodec.hcons(b, c)
    }

    def :<>:[B](b: LineCodec[B]): LineCodec[B :: Unit :: L] =
      HListLineCodec.hcons(b, HListLineCodec.hcons(cs.whitespace, c))

    // Taken from scodec; see DropUnits.scala
    def dropUnits[K <: HList](implicit du: DropUnits.Aux[L, K]) =
      cs.dropUnits(c)

    def as[B](implicit gen: Generic.Aux[B, L]): LineCodec[B] =
      c.xmap(l => gen.from(l), b => gen.to(b))


    def head[A](implicit hh: IsHCons.Aux[L, A, HNil]): LineCodec[A] =
      c.xmap(hl => hl.head, a => hh.cons(a, HNil))
  }

  implicit final class LineCodecCoproductOps[C <: Coproduct](c: LineCodec[C]) {

    def :+:[B](d: LineCodec[B]): LineCodec[B :+: C] =
      CoproductLineCodec.coproduct(d, c)

    def as[A](implicit gen: LabelledGeneric.Aux[A,C]): LineCodec[A] =
      c.xmap(cp => gen.from(cp), a => gen.to(a))
  }

  implicit final class LineCodecOps[A](c: LineCodec[A]) {

    def ::[B](b: LineCodec[B]): LineCodec[B :: A :: HNil] = {
      HListLineCodec.hcons(b, HListLineCodec.hcons(c, HListLineCodec.hnil))
    }

    def :<>:[B](b: LineCodec[B]): LineCodec[B :: Unit :: A :: HNil] =
      b :: cs.whitespace :: c

    def :+:[B](b: LineCodec[B]): LineCodec[B :+: A :+: CNil] =
      CoproductLineCodec.coproduct(b,
        CoproductLineCodec.coproduct(c,
          CoproductLineCodec.cnil))

    def boolean(trueValue: A, falseValue: A): LineCodec[Boolean] =
      cs.boolean(c, trueValue, falseValue)

    def orElse(other: LineCodec[A]) = cs.choice(c, other)

    def repeat = cs.repeat(c)

    def repsep(sep: LineCodec[Unit]) = cs.repsep(c, sep)

    def option = cs.option(c)

    def require(f: A => Boolean, errMsg: String = ""): LineCodec[A] =
      cs.require(c, f, errMsg)

    def allowEmpty(emptyA: A, isEmpty: A => Boolean): LineCodec[A] =
      cs.allowEmpty(c)(emptyA, isEmpty)
  }

  implicit final class StringLineCodecOps(c: LineCodec[String]) {

    def nonEmpty: LineCodec[String] =
      cs.nonEmpty(c)

    def trim: LineCodec[String] =
      cs.trim(c)

    def transform[A](a: LineCodec[A]): LineCodec[A] =
      cs.transform(c, a)
  }
}

object linecodec extends linecodec
