package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class SeekCur(rel: Relation, sec: Seconds) extends Command {
  val name = SeekCur.name

  def isAbsolute = rel == Relation.Absolute
  def isRelative = !isAbsolute

}

object SeekCur {
  val name = CommandName("seekcur")

  implicit def codec(implicit rel: LineCodec[Relation], sc: LineCodec[Seconds]): LineCodec[SeekCur] = {
    (cs.commandName(name, ()) :<>: rel :: sc).dropUnits.as[SeekCur]
  }

  implicit val selectAnswer: SelectAnswer[SeekCur, Answer.Empty.type] =
    SelectAnswer[SeekCur, Answer.Empty.type]
}
