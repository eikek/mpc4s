package mpc4s.protocol

import mpc4s.protocol.codec._

import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class SongCountList(items: Vector[SongCount]) {
  def isEmpty: Boolean = items.isEmpty

  def nonEmpty: Boolean = items.nonEmpty
}

object SongCountList {

  val Empty = SongCountList()

  def apply(is: SongCount*): SongCountList =
    SongCountList(is.toVector)

  implicit def codec(implicit pc: LineCodec[SongCount]): LineCodec[SongCountList] = {
    val elements: LineCodec[SongCountList] =
      cs.fallback(
        cs.map(cs.splitTag.require(_.nonEmpty), pc).xmap[SongCountList](SongCountList.apply, _.items),
        pc).
        xmap({
          case Right(sc) => SongCountList(Vector(sc))
          case Left(sl) => sl
        },
          { case s@SongCountList(vec) =>
            if (vec.size == 1) Right(vec.head)
            else Left(s)
          })

    elements.allowEmpty(Empty, _.isEmpty)
  }

}
