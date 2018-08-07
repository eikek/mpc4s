package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.syntax._

case class PlayId(songId: Option[Id]) extends Command {
  val name = PlayId.name
}

object PlayId {
  val name = CommandName("playid")

  implicit def codec(implicit sc: LineCodec[Option[Id]]): LineCodec[PlayId] =
    (cs.commandName(name, ()) :<>: sc).dropUnits.as[PlayId]

  implicit val selectAnswer: SelectAnswer[PlayId, Answer.Empty.type] =
    SelectAnswer[PlayId, Answer.Empty.type]
}
