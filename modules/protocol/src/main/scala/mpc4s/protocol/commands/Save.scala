package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class Save(playlist: String) extends Command {
  val name = Save.name
}


object Save {

  val name = CommandName("save")

  implicit val codec: LineCodec[Save] =
    (codecs.commandName(name, ()) :<>: cs.quotedString).dropUnits.as[Save]

  implicit val selectAnswer = SelectAnswer[Save, Answer.Empty.type]

}
