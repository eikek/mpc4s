package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._

case class StickerAnswer(sticker: Sticker) extends Answer

object StickerAnswer {

  implicit def codec(implicit sc: LineCodec[Sticker]): LineCodec[StickerAnswer] =
    sc.xmap(StickerAnswer.apply, _.sticker)
}
