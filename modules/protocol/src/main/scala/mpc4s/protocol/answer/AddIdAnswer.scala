package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class AddIdAnswer(id: Id) extends Answer

object AddIdAnswer {

  implicit val codec: LineCodec[AddIdAnswer] =
    LineCodec[AddIdAnswer].keyValues
}
