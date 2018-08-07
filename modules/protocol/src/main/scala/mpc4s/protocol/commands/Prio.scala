package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}


case class Prio(prio: Int, range: Vector[Range]) extends Command {
  val name = Prio.name
}

object Prio {
  val name = CommandName("prio")

  implicit def codec(implicit rc: LineCodec[Range]): LineCodec[Prio] =
    (cs.commandName(name, ()) :<>: cs.int :<>: rc.repsep(cs.whitespace)).
      dropUnits.as[Prio]

  implicit val selectAnswer = SelectAnswer[Prio, Answer.Empty.type]
}
