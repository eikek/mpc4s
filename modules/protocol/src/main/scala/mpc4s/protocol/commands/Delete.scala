package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

/** Deletes a song from the playlist
  */
case class Delete(what: Either[Range, Int]) extends Command {
  val name = Delete.name
}

object Delete {
  val name = CommandName("delete")

  implicit def codec(implicit rc: LineCodec[Range]): LineCodec[Delete] =
    (cs.commandName(name, ()) :<>: cs.fallback(rc, cs.int)).
      dropUnits.as[Delete]

  implicit val selectAnswer = SelectAnswer[Delete, Answer.Empty.type]
}
