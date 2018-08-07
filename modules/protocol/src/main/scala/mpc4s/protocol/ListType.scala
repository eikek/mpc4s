package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._
import mpc4s.protocol.codec.{codecs => cs}

sealed trait ListType

object ListType {

  case object File extends ListType {
    implicit val codec: LineCodec[File.type] =
      cs.constant("file", this)
  }

  case class TagListType(tag: Tag) extends ListType
  object TagListType {
    implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[TagListType] =
      tc.xmap(TagListType.apply, _.tag)
  }

  implicit val codec: LineCodec[ListType] =
    LineCodec[ListType].choice
}
