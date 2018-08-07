package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class JobIdAnswer(jobId: Id) extends Answer

object JobIdAnswer {

  implicit def codec(implicit ic: LineCodec[Id]): LineCodec[JobIdAnswer] =
    (cs.constant("updating_db:", ()) :<>: ic :: cs.constant("\n", ())).
      dropUnits.as[JobIdAnswer]

}
