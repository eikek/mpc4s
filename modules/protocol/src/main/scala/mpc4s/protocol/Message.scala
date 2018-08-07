package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Message(channel: String, message: String)

object Message {

  implicit val codec: LineCodec[Message] = {
    val channel = (cs.constant("channel:", ()) :<>: cs.withEOL).dropUnits.head
    val msg = (cs.constant("message:", ()) :<>: cs.withEOL).dropUnits.head

    (channel :: msg).as[Message]
  }
}
