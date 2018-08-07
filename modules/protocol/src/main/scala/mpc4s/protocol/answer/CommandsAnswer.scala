package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class CommandsAnswer(commands: Vector[String]) extends Answer {
  def isEmpty: Boolean = commands.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object CommandsAnswer {
  val Empty = CommandsAnswer(Vector.empty)

  private val commandCodec: LineCodec[String] =
    (cs.constant("command:", ()) :<>: cs.until("\n") :: cs.constant("\n", ())).dropUnits.head

  implicit def codec: LineCodec[CommandsAnswer] = {
    val existing: LineCodec[CommandsAnswer] =
      commandCodec.repeat.xmap(CommandsAnswer.apply, _.commands)

    existing.allowEmpty(Empty, _.isEmpty)
  }
}
