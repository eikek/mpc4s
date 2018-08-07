package mpc4s.protocol.commands

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

/** Moves the song with FROM (songid) to TO (playlist index) in the
  * playlist. If TO is negative, it is relative to the current song in
  * the playlist (if there is one).
  */
// note: this command supports positive relative move, but this is probably not supported by mpd…
case class MoveId(from: Id, relation: Relation, to: Int) extends Command {
  val name = MoveId.name
}

object MoveId {
  val name = CommandName("moveid")

  implicit def codec(implicit ic: LineCodec[Id], rc: LineCodec[Relation]): LineCodec[MoveId] =
    (cs.commandName(name, ()) :<>: ic :<>: rc :<>: cs.int).
      dropUnits.as[MoveId]

  implicit val selectAnswer = SelectAnswer[MoveId, Answer.Empty.type]

}
