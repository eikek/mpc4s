package mpc4s.protocol

import shapeless.syntax.std.tuple._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Filter(filter: ListMap[FilterType, String]) {

  def isEmpty: Boolean = filter.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def headOption: Option[(FilterType, String)] =
    filter.headOption
}

object Filter {

  def apply(t: (FilterType, String)*): Filter =
    Filter(ListMap(t: _*))

  def tags(t: (Tag, String)*): Filter =
    apply(t.foldLeft(ListMap.empty[FilterType, String]) { (m, e) =>
      m + (FilterType(e._1) -> e._2)
    })

  implicit def codec(implicit fc: LineCodec[FilterType]): LineCodec[Filter] =
    (fc :<>: cs.quotedString).
      dropUnits.
      xmap[(FilterType, String)](_.tupled, _.productElements).
      repsep(cs.whitespace).
      xmap[ListMap[FilterType, String]](ListMap.from _, _.toVector).
      xmap[Filter](Filter.apply, _.filter).
      require(_.filter.nonEmpty)

  /** Finds filtertype -> value pairs in the given list. Ignores unknown keys.
    */
  def fromTuples(data: Iterable[(String, String)])(implicit ftc: LineCodec[FilterType]): Filter =
    Filter(data.collect(Function.unlift(t => ftc.parseValue(t._1).map(ft => (ft, t._2)).toOption)).toSeq: _*)
}
