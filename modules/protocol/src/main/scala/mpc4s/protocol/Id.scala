package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class Id(id: String)

object Id {

  implicit val codec: LineCodec[Id] =
    codecs.alphanum.nonEmpty.xmap(Id.apply, _.id)
}
