package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Version(version: String) extends Answer

object Version {

  implicit val codec: LineCodec[Version] =
    (cs.constant("OK", ()) :<>: cs.constant("MPD", ()) :<>: cs.rest).
      dropUnits.as[Version]

}
