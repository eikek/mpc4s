package mpc4s.protocol.codec

import shapeless._
import mpc4s.protocol.ListMap

/** Convert from/to a string
  */
trait LineCodec[A] { self =>

  def write(in: A): Result[String]

  def parse(in: String): Result[ParseResult[A]]

  final def parseValue(in: String): Result[A] = {
//    val start = System.currentTimeMillis
    val r = parse(in).map(_.value)
//    val time = System.currentTimeMillis - start
//    println(s"Parsing took $time ms")
    r
  }

  final def parseFull(in: String): Result[ParseResult[A]] =
    parse(in) match {
      case Right(pr) =>
        if (pr.rest.isEmpty) Result.successful(pr)
        else Result.failure(ErrorMessage(s"Not exhaustively parsed. Input left: '${pr.rest}'"))
      case Left(err) => Result.failure(err)
    }

  final def xmap[B](p: A => B, w: B => A): LineCodec[B] =
    new LineCodec[B] {
      def write(in: B): Result[String] =
        self.write(w(in))
      def parse(in: String): Result[ParseResult[B]] =
        self.parse(in).map(_.map(p))
    }

  final def exmap[B](p: A => Result[B], w: B => Result[A]): LineCodec[B] =
    new LineCodec[B] {
      def write(in: B): Result[String] =
        w(in).flatMap(self.write)
      def parse(in: String): Result[ParseResult[B]] =
        self.parse(in).flatMap(ar => p(ar.value).map(b => ParseResult(b, ar.rest)))
    }
}


object LineCodec {

  final class Deriver[A] {
    import codecs.CaseClassCodec._

    def derive[L](implicit gen: Generic.Aux[A, L], c: LineCodec[L]): LineCodec[A] =
      c.xmap(l => gen.from(l), b => gen.to(b))

    def choice[C](implicit gen: Generic.Aux[A, C], c: LineCodec[C]): LineCodec[A] = derive

    def keyValues[L <: HList]
      (c: LineCodec[ListMap[ListMap.Key, String]])
      (implicit gen: LabelledGeneric.Aux[A, L], decR: Decode[L], encR: Encode[L]): LineCodec[A] =
      c.exmap[A](_.as[A], _.toStringMap)

    def keyValues[L <: HList]
      (implicit gen: LabelledGeneric.Aux[A, L], decR: Decode[L], encR: Encode[L]): LineCodec[A] =
      keyValues(codecs.keyValue)
  }

  def apply[A] = new Deriver[A]

  def apply[A](p: String => Result[ParseResult[A]], w: A => Result[String]): LineCodec[A] =
    new LineCodec[A] {
      def write(in: A): Result[String] = w(in)
      def parse(in: String): Result[ParseResult[A]] = p(in)
    }

}
