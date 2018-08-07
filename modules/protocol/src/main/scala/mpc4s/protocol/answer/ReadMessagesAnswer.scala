package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class ReadMessagesAnswer(messages: Vector[Message]) extends Answer {
  def isEmpty: Boolean = messages.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object ReadMessagesAnswer {
  val Empty = ReadMessagesAnswer(Vector.empty)

  implicit def codec(implicit mc: LineCodec[Message]): LineCodec[ReadMessagesAnswer] = {
    val existing: LineCodec[ReadMessagesAnswer] =
      mc.repeat.xmap(ReadMessagesAnswer.apply, _.messages)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
