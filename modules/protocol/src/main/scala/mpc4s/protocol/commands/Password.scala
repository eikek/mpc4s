package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

case class Password(pw: String) extends Command {
  val name = Password.name
}

object Password {
  val name = CommandName("password")

  implicit def codec: LineCodec[Password] =
    (cs.commandName(name, ()) :<>: cs.quotedString).
      dropUnits.head.
      xmap(Password.apply, _.pw)

  implicit val selectAnswer = SelectAnswer[Password, Answer.Empty.type]
}
