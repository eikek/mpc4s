package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class MixrampDb(sec: Decibel) extends Command {
  val name = MixrampDb.name
}

object MixrampDb {
  val name = CommandName("mixrampdb")

  implicit def codec(implicit sc: LineCodec[Decibel]): LineCodec[MixrampDb] =
    (cs.commandName(name, ()) :<>: sc).dropUnits.as[MixrampDb]

  implicit val selectAnswer: SelectAnswer[MixrampDb, Answer.Empty.type] =
    SelectAnswer[MixrampDb, Answer.Empty.type]
}
