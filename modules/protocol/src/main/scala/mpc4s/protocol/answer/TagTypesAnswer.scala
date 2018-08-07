package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class TagTypesAnswer(tags: Vector[Tag]) extends Answer {
  def isEmpty: Boolean = tags.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object TagTypesAnswer {
  val Empty = TagTypesAnswer(Vector.empty)

  implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[TagTypesAnswer] = {
    val existing: LineCodec[TagTypesAnswer] =
      (cs.constant("tagtype:", ()) ::
        cs.whitespaceOptional ::
        tc ::
        cs.constant("\n", ())).
        dropUnits.head.repeat.
        xmap(TagTypesAnswer.apply, _.tags)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
