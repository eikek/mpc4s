package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}


case class PrioId(prio: Int, ids: Vector[Id]) extends Command {
  val name = PrioId.name
}

object PrioId {
  val name = CommandName("prioid")

  implicit def codec(implicit ic: LineCodec[Id]): LineCodec[PrioId] =
    (cs.commandName(name, ()) :<>: cs.int :<>: ic.repsep(cs.whitespace)).
      dropUnits.as[PrioId]

  implicit val selectAnswer = SelectAnswer[PrioId, Answer.Empty.type]
}
