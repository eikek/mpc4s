package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.internal.Enum

sealed trait SingleState extends Enum

object SingleState {

  case object On extends SingleState {
    override val name = "1"
  }

  case object Off extends SingleState {
    override val name = "0"
  }

  case object Oneshot extends SingleState

  val all = List(On, Off, Oneshot)

  implicit val codec: LineCodec[SingleState] =
    Enum.codecFromAll(all)

  def parse(name: String): Option[SingleState] =
    all.find(_.name.toLowerCase == name.toLowerCase)
}
