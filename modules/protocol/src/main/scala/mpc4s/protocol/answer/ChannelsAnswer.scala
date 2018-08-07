package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class ChannelsAnswer(channels: Vector[String]) extends Answer {
  def isEmpty: Boolean = channels.isEmpty

  def nonEmpty: Boolean = !isEmpty

}

object ChannelsAnswer {
  val Empty = ChannelsAnswer(Vector.empty)

  implicit def codec: LineCodec[ChannelsAnswer] = {
    val existing: LineCodec[ChannelsAnswer] =
      (cs.constant("channel:", ()) :<>: cs.withEOL).
        dropUnits.head.
        repeat.
        xmap(ChannelsAnswer.apply, _.channels)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
