package mpc4s.protocol

import mpc4s.protocol.codec._

trait Command {
  def name: CommandName
}

object Command {

  def defaultCodec: LineCodec[Command] =
    CommandCodec.defaultCodec

  def createCodec(custom: Map[CommandName, CommandName.Config]): LineCodec[Command] =
    CommandCodec.createCodec(CommandCodec.defaultConfig ++ custom)
}
