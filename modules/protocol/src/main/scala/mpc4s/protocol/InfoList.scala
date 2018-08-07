package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class InfoList(items: Vector[Info]) {

  def size: Int = items.size

  def isEmpty: Boolean = items.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object InfoList {
  val Empty = InfoList(Vector.empty)

  implicit def codec(implicit ic: LineCodec[Info]): LineCodec[InfoList] = {
    val elements: LineCodec[InfoList] =
      cs.map(cs.splitFiles, (ic :: cs.empty).dropUnits.head).
        xmap(InfoList.apply, _.items)

    elements.allowEmpty(Empty, _.isEmpty)
  }

}
