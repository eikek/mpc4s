package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.syntax._
import mpc4s.protocol.codec.{codecs => cs}

sealed trait Relation

object Relation {

  case object Negative extends Relation

  case object Positive extends Relation

  case object Absolute extends Relation

  implicit val codec: LineCodec[Relation] =
    cs.regex("[+-]{1}".r).option.
      xmap({
        case Some("+") => Positive
        case Some("-") => Negative
        case _ => Absolute
      },
        {
          case Positive => Some("+")
          case Negative => Some("-")
          case Absolute => None
        })
}
