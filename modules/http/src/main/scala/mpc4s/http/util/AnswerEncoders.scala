package mpc4s.http.util

import java.time.Instant
import io.circe._, io.circe.syntax._, io.circe.generic.semiauto._
import mpc4s.protocol._
import mpc4s.protocol.answer._


trait AnswerEncoders {

  implicit val instantEncoder: Encoder[Instant] =
    Encoder.encodeString.contramap[Instant](_.toString)

  implicit val uriEncoder: Encoder[Uri] =
    Encoder.encodeString.contramap[Uri](_.uri)

  implicit val tagEncoder: Encoder[Tag] =
    Encoder.instance(t => t.name.asJson)

  implicit val tagvalEncoder: Encoder[TagVal] =
    deriveEncoder[TagVal]

  implicit val tagvalListEncoder: Encoder[TagValList] =
    Encoder.instance(_.items.asJson)

  implicit val tagKeyEncoder: KeyEncoder[Tag] =
    new KeyEncoder[Tag] {
      def apply(t: Tag): String = t.name
    }

  implicit val secondsEncoder: Encoder[Seconds] =
    Encoder.encodeLong.contramap[Seconds](_.n)

  implicit val singleStateEncoder: Encoder[SingleState] =
    Encoder.encodeString.contramap[SingleState](_.name)

  implicit val playStateEncoder: Encoder[PlayState] =
    Encoder.encodeString.contramap[PlayState](_.name)

  implicit val idEncoder: Encoder[Id] =
    Encoder.encodeString.contramap[Id](_.id)

  implicit val rangeEncoder: Encoder[Range] =
    Encoder.instance(r => Json.obj(("start", r.start.asJson), ("end", r.end.asJson)))

  implicit val changeEventEncoder: Encoder[ChangeEvent] =
    Encoder.instance(ce => ce.system.name.asJson)

  implicit val songEncoder: Encoder[Song] =
    deriveEncoder[Song]

  implicit val songlistEncoder: Encoder[SongList] =
    Encoder.instance(_.songs.asJson)

  implicit val playlistSongEncoder: Encoder[PlaylistSong] =
    deriveEncoder[PlaylistSong]

  implicit def playlistSongListEncoder: Encoder[PlaylistSongList] =
    Encoder.instance(_.songs.asJson)

  implicit def songCountEncoder: Encoder[SongCount] =
    deriveEncoder[SongCount]

  implicit def songCountListEncoder: Encoder[SongCountList] =
    Encoder.instance(_.items.asJson)

  implicit val audioFormatEncoder: Encoder[AudioFormat] =
    Encoder.instance(af => Json.obj(
      ("asString", af.asString.asJson),
      ("freq", af.freq.asJson),
      ("bits", af.bits.asJson),
      ("channels", af.channels.asJson)
    ))


  implicit def listMapEncoder[A,B](implicit ae: KeyEncoder[A], be: Encoder[B]): Encoder[ListMap[A,B]] =
    Encoder.encodeMap[A,B].contramap(_.toMap)

  implicit def ackCodeEncoder: Encoder[Ack.Code] =
    Encoder.instance(c => Json.obj(
      ("value", Json.fromInt(c.value)),
      ("name", Json.fromString(c.name))
    ))

  implicit def ackEncoder: Encoder[Ack] =
    deriveEncoder[Ack]

  implicit def statsEncoder: Encoder[StatsAnswer] =
    deriveEncoder[StatsAnswer]

  implicit def statusEncoder: Encoder[StatusAnswer] =
    deriveEncoder[StatusAnswer]

  implicit val decoderPluginEncoder: Encoder[DecoderPlugin] =
    deriveEncoder[DecoderPlugin]

  implicit val playlistSummaryEncoder: Encoder[PlaylistSummary] =
    deriveEncoder[PlaylistSummary]

  implicit val playlistSummaryListEncoder: Encoder[PlaylistSummaryList] =
    Encoder.instance(_.songs.asJson)

  implicit val messageEncoder: Encoder[Message] =
    deriveEncoder[Message]

  implicit val fileBasicEncoder: Encoder[File.Basic] =
    deriveEncoder[File.Basic]

  implicit val fileDirectoryEncoder: Encoder[File.Directory] =
    deriveEncoder[File.Directory]

  implicit val fileEncoder: Encoder[File] =
    deriveEncoder[File]

  implicit val fileListEncoder: Encoder[FileList] =
    Encoder.instance(_.songs.asJson)

  implicit val infoDirEncoder: Encoder[Info.DirInfo] =
    deriveEncoder[Info.DirInfo]

  implicit val infoSongInfoEncode: Encoder[Info.SongInfo] =
    deriveEncoder[Info.SongInfo]

  implicit val infoPlaylistInfoEncoder: Encoder[Info.PlaylistInfo] =
    deriveEncoder[Info.PlaylistInfo]

  implicit val infoEncoder: Encoder[Info] =
    deriveEncoder[Info]

  implicit val infoListEncoder: Encoder[InfoList] =
    Encoder.instance(_.items.asJson)

  implicit val replayGainModeEncoder: Encoder[ReplayGainMode] =
    Encoder.instance(_.name.asJson)

  implicit val stickerEncoder: Encoder[Sticker] =
    deriveEncoder[Sticker]

  implicit val stickerFileEncoder: Encoder[StickerFile] =
    deriveEncoder[StickerFile]

  implicit val storedPlaylistEncoder: Encoder[StoredPlaylist] =
    deriveEncoder[StoredPlaylist]

  implicit def idleAnswerEncoder: Encoder[IdleAnswer] =
    deriveEncoder[IdleAnswer]

  implicit def currentSongAnswerEncoder: Encoder[CurrentSongAnswer] =
    deriveEncoder[CurrentSongAnswer]

  implicit def playlistAnswerEncoder: Encoder[PlaylistAnswer] =
    deriveEncoder[PlaylistAnswer]

  implicit def songlistAnswerEncoder: Encoder[SongListAnswer] =
    deriveEncoder[SongListAnswer]

  implicit def addIdAnswerEncoder: Encoder[AddIdAnswer] =
    deriveEncoder[AddIdAnswer]

  implicit def channelsAnwser: Encoder[ChannelsAnswer] =
    deriveEncoder[ChannelsAnswer]

  implicit def commandsAnswer: Encoder[CommandsAnswer] =
    deriveEncoder[CommandsAnswer]

  implicit def songCountAnswerEncoder: Encoder[SongCountAnswer] =
    deriveEncoder[SongCountAnswer]

  implicit def decodersAnswerEncoder: Encoder[DecodersAnswer] =
    deriveEncoder[DecodersAnswer]

  implicit def fileListAnswerEncoder: Encoder[FileListAnswer] =
    deriveEncoder[FileListAnswer]

  implicit def lsInfoAnswerEncoder: Encoder[LsInfoAnswer] =
    deriveEncoder[LsInfoAnswer]

  implicit def genericAnswerEncoder: Encoder[GenericAnswer] =
    deriveEncoder[GenericAnswer]

  implicit def jobIdAnswerEncoder: Encoder[JobIdAnswer] =
    deriveEncoder[JobIdAnswer]

  implicit def listAnswerEncoder: Encoder[ListAnswer] =
    deriveEncoder[ListAnswer]

  implicit val outputEncoder: Encoder[Output] =
    deriveEncoder[Output]

  implicit val outputListEncoder: Encoder[OutputList] =
    Encoder.instance(_.items.asJson)

  implicit def outputsAnswerEncoder: Encoder[OutputsAnswer] =
    deriveEncoder[OutputsAnswer]

  implicit def playlistSummaryAnswer: Encoder[PlaylistSummaryAnswer] =
    deriveEncoder[PlaylistSummaryAnswer]

  implicit def readCommentsAnswer: Encoder[ReadCommentsAnswer] =
    deriveEncoder[ReadCommentsAnswer]

  implicit def readMessagesAnswer: Encoder[ReadMessagesAnswer] =
    deriveEncoder[ReadMessagesAnswer]

  implicit def replayGainStatusAnswerEncoder: Encoder[ReplayGainStatusAnswer] =
    deriveEncoder[ReplayGainStatusAnswer]

  implicit def stickerAnswerEncoder: Encoder[StickerAnswer] =
    deriveEncoder[StickerAnswer]

  implicit def stickerFindAnswerEncoder: Encoder[StickerFindAnswer] =
    deriveEncoder[StickerFindAnswer]

  implicit def stickerListAnswerEncoder: Encoder[StickerListAnswer] =
    deriveEncoder[StickerListAnswer]

  implicit def storedPlaylistAnswerEncoder: Encoder[StoredPlaylistAnswer] =
    deriveEncoder[StoredPlaylistAnswer]

  implicit def tagtypesAnswerEncoder: Encoder[TagTypesAnswer] =
    deriveEncoder[TagTypesAnswer]

  implicit def urlHandlerAnswer: Encoder[UrlHandlerAnswer] =
    deriveEncoder[UrlHandlerAnswer]

  implicit def versionEncoder: Encoder[Version] =
    deriveEncoder[Version]

  implicit def answerEncoder: Encoder[Answer] =
    new Encoder[Answer] {
      final def apply(a: Answer): Json = a match {
        case s: StatsAnswer => s.asJson
        case s: StatusAnswer => s.asJson
        case s: IdleAnswer => s.asJson
        case s: CurrentSongAnswer => s.asJson
        case s: PlaylistAnswer => s.asJson
        case s: SongListAnswer => s.asJson
        case s: AddIdAnswer => s.asJson
        case s: ChannelsAnswer => s.asJson
        case s: CommandsAnswer => s.asJson
        case s: SongCountAnswer => s.asJson
        case s: DecodersAnswer => s.asJson
        case s: FileListAnswer => s.asJson
        case s: LsInfoAnswer => s.asJson
        case s: GenericAnswer => s.asJson
        case s: JobIdAnswer => s.asJson
        case s: ListAnswer => s.asJson
        case s: OutputsAnswer => s.asJson
        case s: PlaylistSummaryAnswer => s.asJson
        case s: ReadCommentsAnswer => s.asJson
        case s: ReadMessagesAnswer => s.asJson
        case s: ReplayGainStatusAnswer => s.asJson
        case s: StickerAnswer => s.asJson
        case s: StickerFindAnswer => s.asJson
        case s: StickerListAnswer => s.asJson
        case s: StoredPlaylistAnswer => s.asJson
        case s: TagTypesAnswer => s.asJson
        case s: UrlHandlerAnswer => s.asJson
        case s: Version => s.asJson
        case Answer.Empty => Json.obj()

        case _ =>
          throw new RuntimeException(s"Cannot encode '$a' to JSON")
      }
    }


  implicit def mpdErrorEncoder: Encoder[Response.MpdError] =
    Encoder.instance({
      case Response.MpdError(ack) =>
        Json.obj(
          ("success", Json.fromBoolean(false)),
          ("type", Json.fromString("Ack")),
          ("ack", ack.asJson)
        )
    })

  implicit def mpdResultEncoder[A](implicit e: Encoder[A]): Encoder[Response.MpdResult[A]] =
    Encoder.instance({
      case Response.MpdResult(ans) =>
          Json.obj(
            ("success", Json.fromBoolean(true)),
            ("type", Json.fromString(nameFromClass(ans.getClass))),
            ("result", ans.asJson)
          )
    })

  implicit def responseEncoder: Encoder[Response[Answer]] =
    new Encoder[Response[Answer]] {
      final def apply(r: Response[Answer]): Json = r match {
        case e: Response.MpdError => e.asJson
        case r: Response.MpdResult[Answer] => r.asJson
      }
    }

  private def nameFromClass(c: Class[_]): String =
    c.getSimpleName match {
      case s if s.endsWith("$") => s.dropRight(1)
      case s => s
    }

}

object AnswerEncoders extends AnswerEncoders
