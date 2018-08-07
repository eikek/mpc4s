package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class DecoderPlugin(plugin: String, suffixes: Vector[String], mimeTypes: Vector[String]) extends Answer

object DecoderPlugin {

  implicit def codec: LineCodec[DecoderPlugin] = {
    val plugin = (cs.constant("plugin:", ()) :<>: cs.withEOL).
      dropUnits.head

    val suffix = (cs.constant("suffix:", ()) :<>: cs.withEOL).
      dropUnits.head

    val mime = (cs.constant("mime_type:", ()) :<>: cs.withEOL).
      dropUnits.head

    (plugin :: suffix.repeat :: mime.repeat).as[DecoderPlugin]
  }
}
