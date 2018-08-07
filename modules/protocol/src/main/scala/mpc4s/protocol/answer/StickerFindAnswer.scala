package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class StickerFindAnswer(stickers: Vector[StickerFile]) extends Answer {
  def isEmpty: Boolean = stickers.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object StickerFindAnswer {
  val Empty = StickerFindAnswer(Vector.empty)

  implicit def codec(implicit sc: LineCodec[StickerFile]): LineCodec[StickerFindAnswer] = {
    val existing: LineCodec[StickerFindAnswer] =
      sc.repeat.xmap(StickerFindAnswer.apply, _.stickers)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
