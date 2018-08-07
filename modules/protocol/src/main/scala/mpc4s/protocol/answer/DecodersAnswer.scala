package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class DecodersAnswer(plugins: Vector[DecoderPlugin]) extends Answer {
  def isEmpty: Boolean = plugins.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object DecodersAnswer {
  val Empty = DecodersAnswer(Vector.empty)

  implicit def codec(implicit dc: LineCodec[DecoderPlugin]): LineCodec[DecodersAnswer] = {
    val existing: LineCodec[DecodersAnswer] =
      dc.repeat.xmap(DecodersAnswer.apply, _.plugins)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
