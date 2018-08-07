package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class ReadCommentsAnswer(values: ListMap[String, String]) extends Answer {
  def isEmpty: Boolean = values.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object ReadCommentsAnswer {
  val Empty = ReadCommentsAnswer(ListMap.empty)

  def from(data: ListMap[ListMap.Key, String]): ReadCommentsAnswer =
    ReadCommentsAnswer(data.mapKeys(_.name))

  implicit def codec: LineCodec[ReadCommentsAnswer] = {
    val existing: LineCodec[ReadCommentsAnswer] =
      cs.keyValue.xmap(ReadCommentsAnswer.from, _.values.mapKeys(n => ListMap.key(n)))

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
