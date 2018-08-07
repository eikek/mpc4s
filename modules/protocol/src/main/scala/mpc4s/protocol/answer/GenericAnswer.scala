package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}

case class GenericAnswer(content: String) extends Answer

object GenericAnswer {

  implicit val codec: LineCodec[GenericAnswer] =
    cs.rest.xmap[GenericAnswer](GenericAnswer.apply, _.content)

}
