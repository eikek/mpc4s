module App.Update exposing (update)

import Time
import Data.MpdResponse exposing (..)
import Navigation
import Ports
import Requests
import Route exposing (Route(..), findPage, setPage)
import Util.List
import Data.Answer exposing (Answer(..))
import Data.MpdCommand exposing (MpdCommand)
import Data.Subsystem
import App.Data exposing (..)
import App.Messages exposing (getTitle)
import Pages.Library.Update
import Pages.Library.Data
import Pages.NowPlaying.Update
import Pages.NowPlaying.Data
import Pages.Index.Update
import Pages.Index.Data
import Pages.Settings.Update
import Pages.Settings.Data
import Pages.Playlists.Update
import Pages.Playlists.Data

initPage: Route -> Model -> (Model, Cmd Msg)
initPage page model =
    let
        conn = model.settingsModel.settings.mpdConn
        globalInit = [Requests.send conn model.baseUrl Data.MpdCommand.Status]
        send = Requests.sendAll conn model.baseUrl
    in
        case (Debug.log "Page is " page) of
            LibraryPage ma filter ->
                case ma of
                    Just album ->
                        let
                            (m1, c1) = libraryPageMsg (Pages.Library.Data.FindAlbum album) model
                            libraryInit = Pages.Library.Update.initCommands m1.libraryModel
                        in
                            m1 ! ((libraryInit |> send) :: c1 :: globalInit)

                    Nothing ->
                        let
                            m_ = {model|libraryModel = Pages.Library.Data.setFilter filter model.libraryModel}
                            (m1, c1) = libraryPageMsg (Pages.Library.Data.SwitchMode Pages.Library.Data.AlbumList) m_
                            libraryInit = Pages.Library.Update.initCommands m1.libraryModel
                        in
                            m1 ! ((libraryInit |> send) :: c1 :: globalInit)

            NowPlayingPage ->
                let
                    nowPlayingInit = Pages.NowPlaying.Update.initCommands model.nowPlayingModel
                    progress = Cmd.map NowPlayingMsg
                                 <| (Pages.NowPlaying.Update.initProgress model.nowPlayingModel)
                in
                    {model|deferredCmds = progress :: model.deferredCmds} ! ((nowPlayingInit |> send) :: globalInit)

            SettingsPage ->
                let
                    (m1, c1) = settingsMsg Pages.Settings.Data.RequestMpdConnections model
                    settingsInit = Pages.Settings.Update.initCommands m1.settingsModel
                in
                   m1 ! ((settingsInit |> send) :: c1 :: globalInit)

            PlaylistsPage mn ->
                let
                    (m_, mpd) = Pages.Playlists.Update.initCommands mn model.playlistsModel
                in
                    {model|playlistsModel = m_} ! ((mpd |> send) :: globalInit)

            _ ->
                model ! globalInit

{-| Update the model with current uri information.

Additionally the page title is changed and a command is executed that
initialises page elments (like dropdowns) using semantic uis api. This
must be done after the view is rendered, so it is added to
`deferredCmds'.
-}
updatePage: Navigation.Location -> Model -> (Model, Cmd Msg)
updatePage loc model =
    let
        page = findPage loc
        lang = getLanguage model
        (model_, cmd_) = initPage page model
    in {model_
           | page = page
           , location = loc
           , deferredCmds = Ports.initElements() :: model.deferredCmds} ! [Ports.setTitle (getTitle lang page), cmd_]


update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        DeferredTick time ->
            {model|deferredCmds = []} ! model.deferredCmds

        SwitchPage p ->
            model ! [setPage p]

        UrlChange loc ->
            (updatePage loc model)

        RunCommand cmd ->
            sendMpd model [cmd]

        IndexMsg m ->
            let
                (lm, cm) = Pages.Index.Update.update m model.indexModel
            in
                {model|indexModel = lm} ! [Cmd.map IndexMsg cm]

        LibraryMsg m ->
            libraryPageMsg m model

        NowPlayingMsg m ->
            nowPlayingMsg m model

        SettingsMsg m ->
            settingsMsg m model

        PlaylistsMsg m ->
            playlistsMsg m model

        ProcessItemWS str ->
            case (Data.MpdResponse.decodeString str) of
                Ok (Data.MpdResponse.Result ans) ->
                    let
                        (mp, cp) = handleAnswerPages ans model
                        (m_, cg) = handleAnswerGlobal ans mp
                    in
                        m_ ! [cp, cg]

                Ok (Data.MpdResponse.Error ack) ->
                    handleMpdError ack model

                Err err ->
                    let
                        x = Debug.log "Decoding error: " (str ++ err)
                    in
                        model ! []

        CurrentScroll scroll ->
            libraryPageMsg (Pages.Library.Data.CurrentScroll scroll) model

        Tick time ->
            let
                conn = model.settingsModel.settings.mpdConn
                {- send a status command every 20s -}
                cmd = if (floor (Time.inSeconds time)) % 20 == 0
                      then Requests.send conn model.baseUrl Data.MpdCommand.Status
                      else Cmd.none
                (m1, c1) = nowPlayingMsg (Pages.NowPlaying.Data.Tick time) model
            in
                m1 ! [cmd, c1]

        SeekClick perc ->
            nowPlayingMsg (Pages.NowPlaying.Data.Seek perc) model

        SettingsLoad settings ->
            settingsMsg (Pages.Settings.Data.ReceiveSettings settings) model

handleAnswerPages: Answer -> Model -> (Model, Cmd Msg)
handleAnswerPages ans model =
    let
        (m1, c1) = libraryPageMsg (Pages.Library.Data.HandleAnswer ans) model
        (m2, c2) = nowPlayingMsg (Pages.NowPlaying.Data.HandleAnswer ans) m1
        (m3, c3) = settingsMsg (Pages.Settings.Data.HandleAnswer ans) m2
        (m4, c4) = playlistsMsg (Pages.Playlists.Data.HandleAnswer ans) m3
    in
        m4 ! [c1, c2, c3, c4]

handleAnswerGlobal: Answer -> Model -> (Model, Cmd Msg)
handleAnswerGlobal ans model =
    case ans of
        StatusInfo status ->
            {model|status = status} ! []

        Idle evs ->
            let
                conn = model.settingsModel.settings.mpdConn
                cmds = List.foldr
                       (\ev -> \cs ->
                            case ev of
                                Data.Subsystem.Player -> Data.MpdCommand.CurrentSong :: Data.MpdCommand.Status :: cs
                                Data.Subsystem.Mixer -> Data.MpdCommand.Status :: cs
                                Data.Subsystem.Playlist -> Data.MpdCommand.PlaylistInfo :: Data.MpdCommand.Status :: Data.MpdCommand.CurrentSong :: cs
                                Data.Subsystem.Options -> Data.MpdCommand.Status :: cs
                                _ -> cs
                       ) [] evs |> Util.List.distinct
            in
                model ! [Requests.sendAll conn model.baseUrl cmds]

        _ ->
            model ! []

handleMpdError: Ack -> Model -> (Model, Cmd Msg)
handleMpdError ack model =
    let
        a = Debug.log "Mpd Error: " ack
    in
        model ! []

libraryPageMsg: Pages.Library.Data.Msg -> Model -> (Model, Cmd Msg)
libraryPageMsg lmsg model =
    let
        (lm, lc, mpd1, def1) = Pages.Library.Update.update lmsg model.libraryModel
        model_ = {model|libraryModel = lm, deferredCmds = (Cmd.map LibraryMsg def1) :: model.deferredCmds}
        (model2, cmd2) = sendMpd model_ mpd1
    in
        model2 ! [Cmd.map LibraryMsg lc, cmd2]

nowPlayingMsg: Pages.NowPlaying.Data.Msg -> Model -> (Model, Cmd Msg)
nowPlayingMsg nmsg model =
    let
        (nm, nc, mpd2, def2) = Pages.NowPlaying.Update.update nmsg model.nowPlayingModel
        model_ = {model|nowPlayingModel = nm, deferredCmds = (Cmd.map NowPlayingMsg def2) :: model.deferredCmds}
        (model2, cmd2) = sendMpd model_ mpd2
    in
        model2 ! [Cmd.map NowPlayingMsg nc, cmd2]

settingsMsg: Pages.Settings.Data.Msg -> Model -> (Model, Cmd Msg)
settingsMsg smsg model =
    let
        (m, c, mpd) = Pages.Settings.Update.update smsg model.settingsModel
        model_ = {model|settingsModel = m}
        (m2, c2) = sendMpd model_ mpd
    in
        m2 ! [Cmd.map SettingsMsg c, c2]

playlistsMsg: Pages.Playlists.Data.Msg -> Model -> (Model, Cmd Msg)
playlistsMsg pmsg model =
    let
        (m, c, mpd) = Pages.Playlists.Update.update pmsg model.playlistsModel
        model_ = {model|playlistsModel = m}
        (m2, c2) = sendMpd model_ mpd
    in
        m2 ! [Cmd.map PlaylistsMsg c, c2]


sendMpd: Model -> List MpdCommand -> (Model, Cmd msg)
sendMpd model cmds =
    let
        conn = model.settingsModel.settings.mpdConn
        send = Requests.sendAll conn model.baseUrl
    in
        if cmds == [] then (model, Cmd.none)
        else model ! [send (Debug.log "send mpd: " cmds)]
