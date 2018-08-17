module Pages.NowPlaying.Update exposing (update
                                        , initCommands
                                        , initProgress
                                        )

import Time
import Ports
import Route exposing (Route(..))
import Requests
import Pages.NowPlaying.Data exposing (..)
import Data.Answer exposing (Answer(..))
import Data.MpdCommand exposing (MpdCommand(..))
import Data.Range exposing (Range)
import Data.Status
import Data.PlayState
import Data.SingleState
import Data.Tag exposing (Tag(..))
import Data.TagValue exposing (TagValue)
import Data.Settings exposing (Settings)

update: Settings -> Msg -> Model -> (Model, Cmd Msg, List MpdCommand, Cmd Msg)
update settings msg model =
    case msg of
        HandleAnswer (CurrentSongInfo s) ->
            ({model| current = Just s}, Cmd.none, [], Ports.initElements())

        HandleAnswer (Playlist pl) ->
            ({model|playlist = pl}, Cmd.none, [], Cmd.none)

        HandleAnswer (StatusInfo status) ->
            let
                time = case status.state of
                           Data.PlayState.Play -> model.currentTime
                           _ -> 0
            in
                ({model|status = status, currentTime = time}, Cmd.none, [], Ports.initElements())

        HandleAnswer _ ->
            (model, Cmd.none, [], Cmd.none)

        Tick time ->
            let
                cur = model.status
                diff = if model.currentTime <= 0 then 1
                       else let
                           now = Time.inSeconds time |> ceiling
                           pre = Time.inSeconds model.currentTime |> ceiling
                      in now - pre
                status_ = case cur.time of
                              Just range ->
                                  {cur|time = Just (Range (range.start + diff) range.end)}
                              Nothing ->
                                  cur
                progress = case status_.time of
                               Just range ->
                                   Ports.updateProgress ("play-progress", range)
                               Nothing ->
                                   Cmd.none
            in
                ({model|status = status_, currentTime = time}, Cmd.none, [], progress)

        Run cmd ->
            (model, Cmd.none, [cmd], Cmd.none)

        ToggleRandom ->
            (model, Cmd.none, [Random (not model.status.random)], Cmd.none)

        ToggleRepeat ->
            (model, Cmd.none, [Repeat (not model.status.repeat)], Cmd.none)

        ToggleConsume ->
            (model, Cmd.none, [Consume (not model.status.consume)], Cmd.none)

        ToggleSingle ->
            let
                next = case model.status.single of
                           Data.SingleState.On -> Data.SingleState.Off
                           Data.SingleState.Oneshot -> Data.SingleState.Off
                           Data.SingleState.Off -> Data.SingleState.On
            in
                (model, Cmd.none, [Single next], Cmd.none)

        Seek perc ->
            case model.status.time of
                Just range ->
                    let
                        secs = floor ((toFloat range.end) * perc)
                    in
                        (model, Cmd.none, [SeekCur secs], Cmd.none)

                Nothing ->
                    (model, Cmd.none, [], Cmd.none)

        GotoAlbum name ->
            let
                cmd = if name == "" then Cmd.none
                      else Route.setPage (LibraryPage (Just name) [])
            in
                (model, cmd, [], Cmd.none)

        GotoArtist name ->
            let
                cmd = if name == "" then Cmd.none
                      else Route.setPage (LibraryPage Nothing [TagValue Artist name])
            in
                (model, cmd, [], Cmd.none)

        GotoComposer name ->
            let
                cmd = if name == "" then Cmd.none
                      else Route.setPage (LibraryPage Nothing [TagValue Composer name])
            in
                (model, cmd, [], Cmd.none)

        ToggleAddUri ->
            ({model|addUriVisible = model.addUriVisible |> not}, Cmd.none, [], Cmd.none)

        AddUriChange uri ->
            ({model|uri = uri}, Cmd.none, [], Cmd.none)

        AddUri ->
            let
                cmd = if model.uri == "" then []
                      else [AddId model.uri Nothing]
                model_ = if cmd == [] then model
                         else {model|uri = "", addUriVisible = False}
            in
                (model_, Cmd.none, cmd, Cmd.none)

        ToggleSavePlaylist ->
            ({model|savePlaylistVisible = model.savePlaylistVisible |> not}, Cmd.none, [], Cmd.none)

        SavePlaylistChange str ->
            ({model|saveAs = str}, Cmd.none, [], Cmd.none)

        SavePlaylist ->
            let
                cmd = if model.saveAs == "" then []
                      else [SaveCurrentPlaylist model.saveAs]
                model_ = if cmd == [] then model
                         else {model|saveAs = "", savePlaylistVisible = False}
            in
                (model_, Cmd.none, cmd, Cmd.none)

        RequestMpdConns ->
            (model, Requests.info model.baseurl ReceiveMpdConns, [], Cmd.none)

        ReceiveMpdConns (Ok info) ->
            ({model|mpdConns = info.mpd}, Cmd.none, [], Cmd.none)

        ReceiveMpdConns (Err err) ->
            let
                x = Debug.log "Error receiving mpd connections:" err
            in
                (model, Cmd.none, [], Cmd.none)

        ReceiveSettings settings ->
            ({model|settings = settings}, Cmd.none, [], Cmd.none)

        PlayCurrentAt conn ->
            let
                adds = List.map .song model.playlist
                       |> List.map .file
                       |> List.map Add
                play = [Play Nothing]
                seek = model.status.time
                       |> Maybe.map .start
                       |> Maybe.map (\pos -> max (pos - settings.playElsewhereOffset) 0)
                       |> Maybe.map (SeekPos (model.status.song |> Maybe.withDefault 0))
                       |> Maybe.map List.singleton
                       |> Maybe.withDefault []
                mpd = List.concat [Clear :: adds, seek, play]
                cmd = Requests.sendList conn model.baseurl mpd
            in
                (model, cmd, [], Cmd.none)

initCommands: Model -> List MpdCommand
initCommands model =
    let
        pl = if model.playlist == [] then [PlaylistInfo] else []
        st = if Data.Status.isEmpty model.status then [Status] else []
    in
       [CurrentSong, PlaylistInfo] ++ st

initProgress: Model -> Cmd Msg
initProgress model =
    case model.status.time of
        Just range ->
            Ports.updateProgress ("play-progress", range)
        Nothing ->
            Cmd.none
