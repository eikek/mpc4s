package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class OutputList(items: Vector[Output]) {
  def isEmpty: Boolean = items.isEmpty

  def nonEmpty: Boolean = !isEmpty

}

object OutputList {
  val Empty = OutputList(Vector.empty)

  implicit def codec(implicit pc: LineCodec[Output]): LineCodec[OutputList] = {
    val existing: LineCodec[OutputList] =
      cs.map(cs.splitOutputs, (pc :: cs.empty).dropUnits.head).
        xmap(OutputList.apply, _.items)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
