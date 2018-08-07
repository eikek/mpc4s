package mpc4s.protocol.codec.codecs

import shapeless._
import mpc4s.protocol.codec._

trait CoproductLineCodec {

  implicit val cnil: LineCodec[CNil] =
    LineCodec(_ => Result.failure(ErrorMessage(s"No choice applies")), _.impossible)

  implicit def coproduct[H, T <: Coproduct](implicit
    hlc: LineCodec[H],
    tc: LineCodec[T]): LineCodec[H :+: T] = new LineCodec[H :+: T] {

    def write(in: H :+: T): Result[String] =
      in.eliminate(
        h => hlc.write(h),
        t => tc.write(t))

    def parse(in: String): Result[ParseResult[H :+: T]] = {
      hlc.parse(in) match {
        case Right(pr) =>
          val h: H = pr.value
          Result.successful(ParseResult(Inl(h), pr.rest))

        case Left(err) =>
          tc.parse(in) match {
            case Right(pr) =>
              Result.successful(ParseResult(Inr(pr.value), pr.rest))

            case Left(_) =>
              Result.failure(ErrorMessage(s"No choice applies for input '$in'"))
         }
      }
    }
  }
}

object CoproductLineCodec extends CoproductLineCodec
