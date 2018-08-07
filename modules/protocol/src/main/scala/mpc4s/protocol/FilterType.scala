package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._
import mpc4s.protocol.codec.{codecs => cs}

sealed trait FilterType

object FilterType {

  def apply(tag: Tag): FilterType =
    TagFilter(tag)

  case object Anywhere extends FilterType {
    implicit val codec: LineCodec[Anywhere.type] =
      cs.constant("any", this)
  }

  case object File extends FilterType {
    implicit val codec: LineCodec[File.type] =
      cs.constant("file", this)
  }

  case object Base extends FilterType {
    implicit val codec: LineCodec[Base.type] =
      cs.constant("base", this)
  }

  case object ModifiedSince extends FilterType {
    implicit val codec: LineCodec[ModifiedSince.type] =
      cs.constant("modified-since", this)
  }

  case class TagFilter(tag: Tag) extends FilterType
  object TagFilter {
    implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[TagFilter] =
      tc.xmap(TagFilter.apply, _.tag)
  }

  implicit val codec: LineCodec[FilterType] =
    LineCodec[FilterType].choice
}
