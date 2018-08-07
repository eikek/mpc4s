package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

case class StatsAnswer(artists: Int
  , albums: Int
  , songs: Int
  , uptime: Seconds
  , `db_playtime`: Long
  , `db_update`: Long
  , playtime: Long) extends Answer {

  val dbPlaytime: Long = `db_playtime`
  val dbUpdate: Long = `db_update`
}

object StatsAnswer {

  implicit val codec: LineCodec[StatsAnswer] =
    LineCodec[StatsAnswer].keyValues
}
