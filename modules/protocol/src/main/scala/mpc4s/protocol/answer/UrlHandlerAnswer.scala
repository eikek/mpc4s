package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class UrlHandlerAnswer(handlers: Vector[String]) extends Answer {
  def isEmpty: Boolean = handlers.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object UrlHandlerAnswer {
  val Empty = UrlHandlerAnswer(Vector.empty)

  private val handlerCodec: LineCodec[String] =
    (cs.constant("handler:", ()) :<>: cs.until("\n") :: cs.constant("\n", ())).dropUnits.head

  implicit def codec: LineCodec[UrlHandlerAnswer] = {
    val existing: LineCodec[UrlHandlerAnswer] =
      handlerCodec.repeat.xmap(UrlHandlerAnswer.apply, _.handlers)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
