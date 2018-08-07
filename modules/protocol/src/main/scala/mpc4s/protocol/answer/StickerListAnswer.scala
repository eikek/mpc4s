package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class StickerListAnswer(stickers: Vector[Sticker]) extends Answer {
  def isEmpty: Boolean = stickers.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object StickerListAnswer {
  val Empty = StickerListAnswer(Vector.empty)

  implicit def codec(implicit sc: LineCodec[Sticker]): LineCodec[StickerListAnswer] = {
    val existing: LineCodec[StickerListAnswer] =
      sc.repeat.xmap(StickerListAnswer.apply, _.stickers)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
