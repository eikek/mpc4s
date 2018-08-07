package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class TagVal(tag: Tag, value: String)

object TagVal {

  def findFirst(m: ListMap[ListMap.Key, String]): Option[TagVal] = {
    def loop(tags: List[Tag]): Option[TagVal] =
      tags match {
        case h :: t =>
          m.get(ListMap.key(h.name)) match {
            case Some(str) => Some(TagVal(h, str))
            case None => loop(t)
          }
        case Nil =>
          None
      }
    loop(Tag.all)
  }

  implicit def codec(implicit tc: LineCodec[Tag]): LineCodec[TagVal] =
    (tc :: cs.constant(":", ()) :: cs.whitespaceOptional :: cs.withEOL.trim).dropUnits.as[TagVal]

}
