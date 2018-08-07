package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.internal._

sealed trait Subsystem extends Enum

object Subsystem {

  case object Database extends Subsystem

  case object Update extends Subsystem

  case object StoredPlaylist extends Subsystem

  case object Playlist extends Subsystem

  case object Mixer extends Subsystem

  case object Output extends Subsystem

  case object Options extends Subsystem

  case object Partition extends Subsystem

  case object Sticker extends Subsystem

  case object Message extends Subsystem

  case object Player extends Subsystem

  val all: List[Subsystem] = List(Database
    , Update
    , StoredPlaylist
    , Playlist
    , Mixer
    , Output
    , Options
    , Partition
    , Sticker
    , Player
    , Message)

  implicit val codec: LineCodec[Subsystem] =
    Enum.codecFromAll(all)
}
