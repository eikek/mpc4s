package mpc4s.protocol

import mpc4s.protocol.codec.{codecs => cs, _}

trait Answer

object Answer {

  case object Empty extends Answer {

    implicit val codec: LineCodec[Empty.type] =
      cs.empty.xmap(_ => Empty, _ => ())
  }
}
