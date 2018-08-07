package mpc4s.protocol

import mpc4s.protocol.answer._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.implicits._

sealed trait Response[+A] {

  def toOption: Option[A]
}

object Response {

  case class MpdError(ack: Ack) extends Response[Nothing] {
    val toOption = None
  }
  object MpdError {
    implicit def mpdErrorCodec(implicit ac: LineCodec[Ack]): LineCodec[MpdError] =
      ac.xmap(MpdError.apply, _.ack)
  }

  case class MpdResult[+A](value: A) extends Response[A] {
    val toOption = Some(value)
  }
  object MpdResult {
    implicit def codec[A](implicit ac: LineCodec[A]): LineCodec[MpdResult[A]] =
      ac.xmap(MpdResult.apply, _.value)
  }

  implicit def responseCodec[A]
    (implicit ec: LineCodec[MpdError], ac: LineCodec[MpdResult[A]]): LineCodec[Response[A]] = {

    val result = (cs.until("OK\n").transform(ac) :: cs.constant("OK\n", ())).dropUnits.head

    cs.fallback(ec, result).xmap(_.fold(identity, identity), {
      case e: MpdError => Left(e)
      case r: MpdResult[_] => Right(r)
    })
  }

  def createCodec[A](ac: LineCodec[A]): LineCodec[Response[A]] = {
    implicit val lca: LineCodec[A] = ac
    responseCodec
  }

  val genericCodec: LineCodec[Response[GenericAnswer]] =
    createCodec(GenericAnswer.codec)
}
