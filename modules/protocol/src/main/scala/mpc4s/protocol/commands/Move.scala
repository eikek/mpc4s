package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Move(at: Either[Range, Int], to: Int) extends Command {
  val name = Move.name
}

object Move {
  val name = CommandName("move")

  implicit def codec(implicit rc: LineCodec[Range]): LineCodec[Move] =
    (cs.commandName(name, ()) :<>: cs.fallback(rc, cs.int) :<>: cs.int).
      dropUnits.as[Move]

  implicit val selectAnswer = SelectAnswer[Move, Answer.Empty.type]

}
