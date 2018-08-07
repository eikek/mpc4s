package mpc4s.protocol

import mpc4s.protocol.codec._

trait SelectAnswer[C <: Command, A <: Answer] {

  def codec: LineCodec[Response[A]]

}


object SelectAnswer {

  def apply[C <: Command, A <: Answer](implicit mc: LineCodec[Response[A]]): SelectAnswer[C,A] =
    new SelectAnswer[C,A]{
      val codec = mc
    }
}
