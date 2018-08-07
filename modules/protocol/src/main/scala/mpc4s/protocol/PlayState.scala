package mpc4s.protocol

import mpc4s.protocol.codec._

sealed trait PlayState extends internal.Enum

object PlayState {

  case object Play extends PlayState

  case object Stop extends PlayState

  case object Pause extends PlayState

  val all = List(Play, Stop, Pause)

  implicit val playStateCodec: LineCodec[PlayState] =
    internal.Enum.codecFromAll(all)

  def parse(name: String): Option[PlayState] =
    all.find(_.name.toLowerCase == name.toLowerCase)
}
