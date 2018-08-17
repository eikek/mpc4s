package mpc4s.protocol

import mpc4s.protocol.codec._
import mpc4s.protocol.codec.implicits._

sealed trait CommandOrList

object CommandOrList {

  def apply(list: CommandList): CommandOrList =
    List(list)

  def apply(cmd: Command): CommandOrList =
    Cmd(cmd)

  case class Cmd(command: Command) extends CommandOrList
  object Cmd {
    implicit def codec(implicit cc: LineCodec[Command]): LineCodec[Cmd] =
      cc.xmap(Cmd.apply, _.command)
  }

  case class List(list: CommandList) extends CommandOrList
  object List {
    implicit def codec(implicit cc: LineCodec[Command]): LineCodec[List] =
      CommandList.codec(cc).xmap(List.apply, _.list)
  }


  implicit def codec(implicit cc: LineCodec[Command]): LineCodec[CommandOrList] = {
    LineCodec[CommandOrList].choice
  }
}
