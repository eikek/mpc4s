package mpc4s.protocol

import mpc4s.protocol.answer._
import mpc4s.protocol.codec.{codecs => cs, _}

package commands {
  abstract class NamedCommand(val name: CommandName) extends Command


  case object ClearError extends NamedCommand(CommandName("clearerror")) {
    implicit val selectAnswer = SelectAnswer[ClearError.type, Answer.Empty.type]
    implicit val codec: LineCodec[ClearError.type] =
      cs.commandName(name, this)
  }

  case object CurrentSong extends NamedCommand(CommandName("currentsong")) {
    implicit val selectAnswer = SelectAnswer[CurrentSong.type, CurrentSongAnswer]
    implicit val codec: LineCodec[CurrentSong.type] =
      cs.commandName(name, this)
  }

  case object NoIdle extends NamedCommand(CommandName("noidle")) {
    implicit val selectAnswer = SelectAnswer[NoIdle.type, Answer.Empty.type]
    implicit val codec: LineCodec[NoIdle.type] =
      cs.commandName(name, this)
  }

  case object Status extends NamedCommand(CommandName("status")) {
    implicit val selectAnswer = SelectAnswer[Status.type, StatusAnswer]
    implicit val codec: LineCodec[Status.type] =
      cs.commandName(name, this)
  }

  case object Stats extends NamedCommand(CommandName("stats")) {
    implicit val selectAnswer = SelectAnswer[Stats.type, StatsAnswer]
    implicit val codec: LineCodec[Stats.type] =
      cs.commandName(name, this)
  }

  case object ReplayGainStatus extends NamedCommand(CommandName("replay_gain_status")) {
    implicit val selectAnswer = SelectAnswer[ReplayGainStatus.type, ReplayGainStatusAnswer]
    implicit val codec: LineCodec[ReplayGainStatus.type] =
      cs.commandName(name, this)
  }

  case object Next extends NamedCommand(CommandName("next")) {
    implicit val selectAnswer = SelectAnswer[Next.type, Answer.Empty.type]
    implicit val codec: LineCodec[Next.type] =
      cs.commandName(name, this)
  }

  case object Previous extends NamedCommand(CommandName("previous")) {
    implicit val selectAnswer = SelectAnswer[Previous.type, Answer.Empty.type]
    implicit val codec: LineCodec[Previous.type] =
      cs.commandName(name, this)
  }

  case object Stop extends NamedCommand(CommandName("stop")) {
    implicit val selectAnswer = SelectAnswer[Stop.type, Answer.Empty.type]
    implicit val codec: LineCodec[Stop.type] =
      cs.commandName(name, this)
  }

  case object Clear extends NamedCommand(CommandName("clear")) {
    implicit val selectAnswer = SelectAnswer[Clear.type, Answer.Empty.type]
    implicit val codec: LineCodec[Clear.type] =
      cs.commandName(name, this)
  }

  case object ListPlaylists extends NamedCommand(CommandName("listplaylists")) {
    implicit val selectAnswer = SelectAnswer[ListPlaylists.type, PlaylistSummaryAnswer]
    implicit val codec: LineCodec[ListPlaylists.type] =
      cs.commandName(name, this)
  }

  case object Update extends NamedCommand(CommandName("update")) {
    implicit val selectAnswer = SelectAnswer[Update.type, JobIdAnswer]
    implicit val codec: LineCodec[Update.type] =
      cs.commandName(name, this)
  }

  case object Rescan extends NamedCommand(CommandName("rescan")) {
    implicit val selectAnswer = SelectAnswer[Rescan.type, JobIdAnswer]
    implicit val codec: LineCodec[Rescan.type] =
      cs.commandName(name, this)
  }

  case object ListMounts extends NamedCommand(CommandName("listmounts")) {
    implicit val selectAnswer = SelectAnswer[ListMounts.type, GenericAnswer]
    implicit val codec: LineCodec[ListMounts.type] =
      cs.commandName(name, this)
  }

  case object ListNeighbors extends NamedCommand(CommandName("listneighbors")) {
    implicit val selectAnswer = SelectAnswer[ListNeighbors.type, GenericAnswer]
    implicit val codec: LineCodec[ListNeighbors.type] =
      cs.commandName(name, this)
  }

  case object Close extends NamedCommand(CommandName("close")) {
    implicit val selectAnswer = SelectAnswer[Close.type, Answer.Empty.type]
    implicit val codec: LineCodec[Close.type] =
      cs.commandName(name, this)
  }

  case object Kill extends NamedCommand(CommandName("kill")) {
    implicit val selectAnswer = SelectAnswer[Kill.type, Answer.Empty.type]
    implicit val codec: LineCodec[Kill.type] =
      cs.commandName(name, this)
  }

  case object Ping extends NamedCommand(CommandName("ping")) {
    implicit val selectAnswer = SelectAnswer[Ping.type, Answer.Empty.type]
    implicit val codec: LineCodec[Ping.type] =
      cs.commandName(name, this)
  }

  case object TagTypes extends NamedCommand(CommandName("tagtypes")) {
    implicit val selectAnswer = SelectAnswer[TagTypes.type, TagTypesAnswer]
    implicit val codec: LineCodec[TagTypes.type] =
      cs.commandName(name, this)
  }

  case object TagTypesClear extends NamedCommand(CommandName("tagtypes", "clear")) {
    implicit val selectAnswer = SelectAnswer[TagTypesClear.type, Answer.Empty.type]
    implicit val codec: LineCodec[TagTypesClear.type] =
      cs.commandName(name, this)
  }

  case object TagTypesAll extends NamedCommand(CommandName("tagtypes", "all")) {
    implicit val selectAnswer = SelectAnswer[TagTypesAll.type, Answer.Empty.type]
    implicit val codec: LineCodec[TagTypesAll.type] =
      cs.commandName(name, this)
  }

  case object Outputs extends NamedCommand(CommandName("outputs")) {
    implicit val selectAnswer = SelectAnswer[Outputs.type, OutputsAnswer]
    implicit val codec: LineCodec[Outputs.type] =
      cs.commandName(name, this)
  }

  case object Commands extends NamedCommand(CommandName("commands")) {
    implicit val selectAnswer = SelectAnswer[Commands.type, CommandsAnswer]
    implicit val codec: LineCodec[Commands.type] =
      cs.commandName(name, this)
  }

  case object NotCommands extends NamedCommand(CommandName("notcommands")) {
    implicit val selectAnswer = SelectAnswer[NotCommands.type, CommandsAnswer]
    implicit val codec: LineCodec[NotCommands.type] =
      cs.commandName(name, this)
  }

  case object UrlHandlers extends NamedCommand(CommandName("urlhandlers")) {
    implicit val selectAnswer = SelectAnswer[UrlHandlers.type, UrlHandlerAnswer]
    implicit val codec: LineCodec[UrlHandlers.type] =
      cs.commandName(name, this)
  }

  case object Decoders extends NamedCommand(CommandName("decoders")) {
    implicit val selectAnswer = SelectAnswer[Decoders.type, DecodersAnswer]
    implicit val codec: LineCodec[Decoders.type] =
      cs.commandName(name, this)
  }

  case object Channels extends NamedCommand(CommandName("channels")) {
    implicit val selectAnswer = SelectAnswer[Channels.type, ChannelsAnswer]
    implicit val codec: LineCodec[Channels.type] =
      cs.commandName(name, this)
  }

  case object ReadMessages extends NamedCommand(CommandName("readmessages")) {
    implicit val selectAnswer = SelectAnswer[ReadMessages.type, ReadMessagesAnswer]
    implicit val codec: LineCodec[ReadMessages.type] =
      cs.commandName(name, this)
  }
}
