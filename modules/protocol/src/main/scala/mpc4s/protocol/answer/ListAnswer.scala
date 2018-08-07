package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class ListAnswer(tags: TagValList) extends Answer

object ListAnswer {

  implicit def codec(implicit tc: LineCodec[TagValList]):  LineCodec[ListAnswer] =
    tc.xmap(ListAnswer.apply, _.tags)
}
