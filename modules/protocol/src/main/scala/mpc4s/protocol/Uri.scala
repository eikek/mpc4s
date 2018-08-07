package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}

case class Uri(uri: String) {

  def nonEmpty: Boolean = uri.nonEmpty
}

object Uri {

  // codec for an uri is different for answers and commands

  val quotedStringCodec: LineCodec[Uri] =
    cs.quotedString.xmap(Uri.apply, _.uri)

  val endOfLineCodec: LineCodec[Uri] = {
    cs.choice(cs.until("\n"), cs.rest).xmap(Uri.apply, _.uri)
  }
}
