package mpc4s.protocol

import mpc4s.protocol.codec.{codecs => cs, _}
import mpc4s.protocol.codec.implicits._

case class Ack(code: Ack.Code, position: Int, command: String, message: String) 

object Ack {

  sealed trait Code extends internal.Enum {
    def value: Int
  }

  object Code {
    /** Invalid argument passed (ACK 2) */
    case object InvalidArgument extends Code {
      val value = 2
    }
    /** Invalid password supplied (ACK 3) */
    case object InvalidPassword extends Code {
      val value = 3
    }
    /** Authentication required (ACK 4) */
    case object Auth extends Code {
      val value = 4
    }
    /** Unknown command (ACK 5) */
    case object UnknownCommand extends Code {
      val value = 5
    }
    /** File or directory not found ACK 50) */
    case object FileNotFound extends Code {
      val value = 50
    }
    /** Playlist at maximum size (ACK 51) */
    case object PlaylistMax extends Code {
      val value = 51
    }
    /** A system error (ACK 52) */
    case object System extends Code {
      val value = 52
    }
    /** Playlist loading failed (ACK 53) */
    case object PlaylistLoad extends Code {
      val value = 53
    }
    /** Update already running (ACK 54) */
    case object Busy extends Code {
      val value = 54
    }
    /** An operation requiring playback got interrupted (ACK 55) */
    case object NotPlaying extends Code {
      val value = 55
    }
    /** File already exists (ACK 56) */
    case object FileExists extends Code {
      val value = 56
    }
    /** An unknown ACK (aka. bug) */
    case class UnknownACK(value: Int) extends Code

    def fromInt(value: Int): Code = value match {
      case 2 => InvalidArgument
      case 3 => InvalidPassword
      case 4 => Auth
      case 5 => UnknownCommand
      case 50 => FileNotFound
      case 51 => PlaylistMax
      case 52 => System
      case 53 => PlaylistLoad
      case 54 => Busy
      case 55 => NotPlaying
      case 56 => FileExists
      case _ => UnknownACK(value)
    }

  }

  implicit val codec: LineCodec[Ack] =
    (cs.constant("ACK", ()) ::
      cs.whitespace ::
      cs.constant("[", ()) ::
      cs.int.xmap[Code](Code.fromInt, _.value) ::
      cs.constant("@", ()) ::
      cs.int ::
      cs.constant("]", ()) ::
      cs.whitespace ::
      cs.constant("{", ()) ::
      cs.charsNotIn("}") ::
      cs.constant("}", ()) ::
      cs.whitespace ::
      cs.rest.trim).dropUnits.as[Ack]

}
