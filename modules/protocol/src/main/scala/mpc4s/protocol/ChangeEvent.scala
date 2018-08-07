package mpc4s.protocol

import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class ChangeEvent(system: Subsystem)

object ChangeEvent {

  implicit def codec(implicit sc: LineCodec[Subsystem]): LineCodec[ChangeEvent] =
    (cs.constant("changed:", ()) :<>: sc).dropUnits.as[ChangeEvent]

}
