package mpc4s.protocol.codec

import mpc4s.protocol._
import mpc4s.protocol.commands._
import mpc4s.protocol.codec.implicits.linecodec._

import CommandName.Config

object CommandCodec {

  val defaultConfig: ProtocolConfig = Map(
    // status commands
    ClearError.name -> Config[ClearError.type].apply,
    CurrentSong.name -> Config[CurrentSong.type].apply,
    Idle.name -> Config[Idle].apply,
    NoIdle.name -> Config[NoIdle.type].apply,
    Status.name -> Config[Status.type].apply,
    Stats.name -> Config[Stats.type].apply,

    // playback options
    Consume.name -> Config[Consume].apply,
    Crossfade.name -> Config[Crossfade].apply,
    MixrampDb.name -> Config[MixrampDb].apply,
    MixrampDelay.name -> Config[MixrampDelay].apply,
    Random.name -> Config[Random].apply,
    Repeat.name -> Config[Repeat].apply,
    SetVol.name -> Config[SetVol].apply,
    Single.name -> Config[Single].apply,
    SetReplayGainMode.name -> Config[SetReplayGainMode].apply,
    ReplayGainStatus.name -> Config[ReplayGainStatus.type].apply,

    // controlling playback
    Next.name -> Config[Next.type].apply,
    Pause.name -> Config[Pause].apply,
    Play.name -> Config[Play].apply,
    PlayId.name -> Config[PlayId].apply,
    Previous.name -> Config[Previous.type].apply,
    Seek.name -> Config[Seek].apply,
    SeekId.name -> Config[SeekId].apply,
    SeekCur.name -> Config[SeekCur].apply,
    Stop.name -> Config[Stop.type].apply,

    // current playlist
    Add.name -> Config[Add].apply,
    AddId.name -> Config[AddId].apply,
    Clear.name -> Config[Clear.type].apply,
    Delete.name -> Config[Delete].apply,
    DeleteId.name -> Config[DeleteId].apply,
    Move.name -> Config[Move].apply,
    MoveId.name -> Config[MoveId].apply,
    PlaylistFind.name -> Config[PlaylistFind].apply,
    PlaylistId.name -> Config[PlaylistId].apply,
    PlaylistInfo.name -> Config[PlaylistInfo].apply,
    PlaylistSearch.name -> Config[PlaylistSearch].apply,
    PlaylistChanges.name -> Config[PlaylistChanges].apply,
    PlaylistChangesPosId.name -> Config[PlaylistChangesPosId].apply,
    Prio.name -> Config[Prio].apply,
    PrioId.name -> Config[PrioId].apply,
    RangeId.name -> Config[RangeId].apply,
    Shuffle.name -> Config[Shuffle].apply,
    Swap.name -> Config[Swap].apply,
    SwapId.name -> Config[SwapId].apply,
    AddTagId.name -> Config[AddTagId].apply,
    ClearTagId.name -> Config[ClearTagId].apply,

    // Stored playlists
    ListPlaylist.name -> Config[ListPlaylist].apply,
    ListPlaylistInfo.name -> Config[ListPlaylistInfo].apply,
    ListPlaylists.name -> Config[ListPlaylists.type].apply,
    Load.name -> Config[Load].apply,
    PlaylistAdd.name -> Config[PlaylistAdd].apply,
    PlaylistClear.name -> Config[PlaylistClear].apply,
    PlaylistMove.name -> Config[PlaylistMove].apply,
    Rename.name -> Config[Rename].apply,
    Rm.name -> Config[Rm].apply,
    Save.name -> Config[Save].apply,

    // music database commands
    Count.name -> Config[Count].apply,
    Find.name -> Config[Find].apply,
    FindAdd.name -> Config[FindAdd].apply,
    List.name -> Config[List].apply,
    ListFiles.name -> Config[ListFiles].apply,
    LsInfo.name -> Config[LsInfo].apply,
    ReadComments.name -> Config[ReadComments].apply,
    Search.name -> Config[Search].apply,
    SearchAdd.name -> Config[SearchAdd].apply,
    SearchAddPl.name -> Config[SearchAddPl].apply,
    Update.name -> Config[Update.type].apply,
    Rescan.name -> Config[Rescan.type].apply,

    // Mounts and neighbors
    Mount.name -> Config[Mount].apply,
    Unmount.name -> Config[Unmount].apply,
    ListMounts.name -> Config[ListMounts.type].apply,
    ListNeighbors.name -> Config[ListNeighbors.type].apply,

    // Stickers
    StickerGet.name -> Config[StickerGet].apply,
    StickerSet.name -> Config[StickerSet].apply,
    StickerDelete.name -> Config[StickerDelete].apply,
    StickerList.name -> Config[StickerList].apply,
    StickerFind.name -> Config[StickerFind].apply,

    // Connection Settings
    Close.name -> Config[Close.type].apply,
    Kill.name -> Config[Kill.type].apply,
    Password.name -> Config[Password].apply,
    Ping.name -> Config[Ping.type].apply,
    TagTypes.name -> Config[TagTypes.type].apply,
    TagTypesDisable.name -> Config[TagTypesDisable].apply,
    TagTypesEnable.name -> Config[TagTypesEnable].apply,
    TagTypesClear.name -> Config[TagTypesClear.type].apply,
    TagTypesAll.name -> Config[TagTypesAll.type].apply,

    // Partition commands
    // don't work here…

    // Audio output devices
    DisableOutput.name -> Config[DisableOutput].apply,
    EnableOutput.name -> Config[DisableOutput].apply,
    ToggleOutput.name -> Config[ToggleOutput].apply,
    Outputs.name -> Config[Outputs.type].apply,

    // Reflection
    Commands.name -> Config[Commands.type].apply,
    NotCommands.name -> Config[NotCommands.type].apply,
    UrlHandlers.name -> Config[UrlHandlers.type].apply,
    Decoders.name -> Config[Decoders.type].apply,

    // Client to client
    Subscribe.name -> Config[Subscribe].apply,
    Unsubscribe.name -> Config[Unsubscribe].apply,
    Channels.name -> Config[Channels.type].apply,
    ReadMessages.name -> Config[ReadMessages.type].apply,
    SendMessage.name -> Config[SendMessage].apply
  )

  val defaultCodec: LineCodec[Command] = createCodec(defaultConfig)

  def createCodec(config: ProtocolConfig): LineCodec[Command] =
    LineCodec(
      str => CommandName.find(config.keySet, str).flatMap(config.get) match {
        case Some(cfg) =>
          cfg.commandCodec.parseFull(str).asInstanceOf[Result[ParseResult[Command]]]
        case None =>
          Result.failure(ErrorMessage(s"No codec for command: $str"))
      },
      cmd => config.get(cmd.name) match {
        case Some(cfg) =>
          cfg.commandCodec.write(cmd.asInstanceOf[cfg.Cmd])
        case None =>
          Result.failure(ErrorMessage(s"No codec for command ${cmd.name.path}"))
      }
    )
}
