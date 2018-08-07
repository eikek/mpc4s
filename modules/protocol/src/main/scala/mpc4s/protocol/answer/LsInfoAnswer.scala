package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class LsInfoAnswer(infos: InfoList) extends Answer

object LsInfoAnswer {

  implicit def codec(implicit ic: LineCodec[InfoList]): LineCodec[LsInfoAnswer] =
    ic.xmap(LsInfoAnswer.apply, _.infos)

}
