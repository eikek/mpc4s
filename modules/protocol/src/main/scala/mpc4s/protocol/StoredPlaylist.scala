package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.{codecs => cs}
import mpc4s.protocol.codec.syntax._

case class StoredPlaylist(items: Vector[Uri]) {
  def isEmpty: Boolean = items.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object StoredPlaylist {
  val Empty = StoredPlaylist(Vector.empty)

  implicit def codec: LineCodec[StoredPlaylist] = {
    val line = (cs.constant("file:", ()) :<>: cs.whitespace :<>: Uri.endOfLineCodec :: cs.constant("\n", ())).
      dropUnits.head
    line.repeat.xmap[StoredPlaylist](StoredPlaylist.apply, _.items).
      allowEmpty(Empty, _.isEmpty)
  }
}
