package mpc4s.protocol.answer

import mpc4s.protocol._
import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._
import mpc4s.protocol.codec.implicits.keyvalues._

case class StatusAnswer(volume: Int
  , repeat: Boolean
  , random: Boolean
  , single: SingleState
  , consume: Boolean
  , playlist: Long
  , playlistlength: Int
  , state: PlayState
  , song: Option[Int]
  , songid: Option[Id]
  , nextsong: Option[Int]
  , nextsongid: Option[Id]
  , time: Option[Range]
  , elapsed: Option[Double]
  , duration: Option[Double]
  , bitrate: Option[Int]
  , xfade: Option[Int]
  , mixrampdb: Option[Double]
  , audio: Option[AudioFormat]
  , `updating_db`: Option[String]
  , error: Option[String]
) extends Answer {

  val updatingDb: Option[String] = `updating_db`

}

object StatusAnswer {

  implicit def codec: LineCodec[StatusAnswer] =
    LineCodec[StatusAnswer].keyValues
}
