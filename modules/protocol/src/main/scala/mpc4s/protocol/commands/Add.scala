package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._

case class Add(uri: Uri) extends Command {
  val name = Add.name
}


object Add {

  val name = CommandName("add")

  implicit def codec: LineCodec[Add] =
    (codecs.commandName(name, ()) :<>: Uri.quotedStringCodec).dropUnits.as[Add]

  implicit val selectAnswer = SelectAnswer[Add, Answer.Empty.type]

}
