package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class IdleAnswer(changes: Seq[ChangeEvent]) extends Answer {
  def isEmpty: Boolean = changes.isEmpty
  def nonEmpty: Boolean = !isEmpty
}

object IdleAnswer {
  val Empty = IdleAnswer(Seq.empty)

  implicit def codec(implicit cc: LineCodec[ChangeEvent]): LineCodec[IdleAnswer] = {
    val existing: LineCodec[IdleAnswer] =
      (cc :: cs.constant("\n", ())).dropUnits.head.repeat.
        xmap(v => IdleAnswer(v.toSeq), _.changes.toVector)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
