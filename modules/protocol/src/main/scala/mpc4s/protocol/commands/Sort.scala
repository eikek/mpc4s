package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Sort(descending: Boolean, by: FilterType)

object Sort {

  def apply(tag: Tag): Sort =
    Sort(false, FilterType.TagFilter(tag))

  implicit def codec(implicit fc: LineCodec[FilterType]): LineCodec[Sort] = {
    val desc = cs.constant("-", ()).option.
      xmap[Boolean](_.isDefined, flag => if (flag) Some(()) else None)

    (desc :: fc).as[Sort]
  }
}
