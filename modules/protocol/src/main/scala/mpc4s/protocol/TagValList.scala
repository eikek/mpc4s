package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class TagValList(items: Vector[TagVal]) {
  def isEmpty: Boolean = items.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object TagValList {
  val Empty = TagValList(Vector.empty)

  def apply(tv: TagVal*): TagValList =
    TagValList(tv.toVector)

  def from(tvs: (Tag, String)*): TagValList =
    apply(tvs.map(t => TagVal(t._1, t._2)): _*)

  implicit def codec(implicit tc: LineCodec[TagVal]): LineCodec[TagValList] = {
    tc.repeat.xmap[TagValList](TagValList.apply, _.items).
      allowEmpty(Empty, _.isEmpty)
  }
}
