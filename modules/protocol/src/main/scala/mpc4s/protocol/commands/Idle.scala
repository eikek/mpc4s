package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.answer._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class Idle(subsystems: Vector[Subsystem]) extends Command {
  val name = Idle.name
}

object Idle {
  val name = CommandName("idle")
  val All = Idle(Vector.empty)
  def of(systems: Subsystem*): Idle = Idle(systems.toVector)

  implicit def codec(implicit sc: LineCodec[Subsystem]): LineCodec[Idle] =
    (cs.commandName(name, ()) :: (cs.whitespace :: sc.repsep(cs.whitespace)).dropUnits.head.option.xmap[Vector[Subsystem]](_.getOrElse(Vector.empty), Some(_))).
      dropUnits.as[Idle]

  implicit val idleAnswer: SelectAnswer[Idle, IdleAnswer] =
    SelectAnswer[Idle, IdleAnswer]
}
