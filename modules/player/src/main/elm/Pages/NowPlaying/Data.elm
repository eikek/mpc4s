module Pages.NowPlaying.Data exposing (..)

import Time exposing (Time)
import Util.Time
import Util.List
import Data.Answer exposing (Answer)
import Data.PlaylistSong exposing (PlaylistSong)
import Data.Status exposing (Status)
import Data.MpdCommand exposing (MpdCommand)
import Data.MpdConn exposing (MpdConn)
import Data.Settings exposing (Settings)
import Data.Info exposing (Info)

type alias Model =
    { playlist: List PlaylistSong
    , current: Maybe PlaylistSong
    , status: Status
    , currentTime: Time
    , addUriVisible: Bool
    , savePlaylistVisible: Bool
    , uri: String
    , saveAs: String
    , mpdConns: List MpdConn
    , settings: Settings
    , baseurl: String
    }

makeModel: String -> Model
makeModel baseurl =
    { playlist = []
    , current = Nothing
    , status = Data.Status.empty
    , currentTime = 0
    , addUriVisible = False
    , savePlaylistVisible = False
    , uri = ""
    , saveAs = ""
    , mpdConns = []
    , settings = Data.Settings.empty
    , baseurl = baseurl
    }

type Msg
    = HandleAnswer Answer
    | Tick Time
    | ToggleRandom
    | ToggleRepeat
    | ToggleSingle
    | ToggleConsume
    | Run MpdCommand
    | Seek Float
    | GotoAlbum String
    | GotoArtist String
    | GotoComposer String
    | ToggleAddUri
    | AddUriChange String
    | AddUri
    | ToggleSavePlaylist
    | SavePlaylistChange String
    | SavePlaylist
    | ReceiveInfo Info
    | ReceiveSettings Settings
    | PlayCurrentAt MpdConn

playlistLength: Model -> String
playlistLength model =
    playlistTime model.playlist

playlistPlayedSum: Model -> Int
playlistPlayedSum model =
    case model.current of
        Just ps ->
            let
                prevSum =
                    Util.List.takeWhile (\s -> s.pos < ps.pos) model.playlist
                        |> playlistTimeSum
                current = case model.status.time of
                              Just range -> range.start
                              Nothing -> 0
            in
                current + prevSum

        Nothing ->
            0

playlistPlayedTime: Model -> String
playlistPlayedTime model =
    playlistPlayedSum model
        |> Util.Time.formatSeconds


playlistRemainSum: Model -> Int
playlistRemainSum model =
    let
        total = playlistTimeSum model.playlist
        played = playlistPlayedSum model
    in
        total - played

playlistRemainTime: Model -> String
playlistRemainTime model =
    playlistRemainSum model
        |> Util.Time.formatSeconds

playlistTimeSum: List PlaylistSong -> Int
playlistTimeSum songs =
    List.map .song songs
        |> List.map .time
        |> List.sum

playlistTime: List PlaylistSong -> String
playlistTime songs =
    playlistTimeSum songs
        |> Util.Time.formatSeconds

playlistEndsAt: Model -> String
playlistEndsAt model =
    let
        remain = (playlistRemainSum model |> toFloat) * Time.second
    in
        model.currentTime + remain |> Util.Time.formatTime

otherMpdConnections: Model -> List MpdConn
otherMpdConnections model =
    let
        curr = model.settings.mpdConn.id
    in
        List.filter (\item -> item.id /= curr) model.mpdConns
