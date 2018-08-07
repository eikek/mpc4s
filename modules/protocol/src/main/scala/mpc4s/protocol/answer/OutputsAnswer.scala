package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class OutputsAnswer(outputs: OutputList) extends Answer

object OutputsAnswer {

  implicit def codec(implicit oc: LineCodec[OutputList]): LineCodec[OutputsAnswer] =
    oc.xmap(OutputsAnswer.apply, _.outputs)
}
