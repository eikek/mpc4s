module App.Update exposing (update)

import App.Data exposing (..)
import App.Messages exposing (getTitle)
import Data.Answer exposing (Answer(..))
import Data.MpdCommand exposing (MpdCommand)
import Data.MpdResponse exposing (..)
import Data.Subsystem
import Navigation
import Pages.Index.Data
import Pages.Index.Update
import Pages.Library.Data
import Pages.Library.Update
import Pages.NowPlaying.Data
import Pages.NowPlaying.Update
import Pages.Playlists.Data
import Pages.Playlists.Update
import Pages.Settings.Data
import Pages.Settings.Update
import Pages.Search.Data
import Pages.Search.Update
import Ports
import Requests
import Route exposing (Route(..), findPage, setPage)
import Time
import Util.List
import Util.Html

initPage: Route -> Model -> (Model, Cmd Msg)
initPage page model =
    let
        conn = model.settingsModel.settings.mpdConn
        globalInit = [Requests.send conn model.baseUrl Data.MpdCommand.Status, Requests.info model.baseUrl InfoLoad]
        send = Requests.sendAll conn model.baseUrl
    in
        case (Debug.log "Page is " page) of
            LibraryPage ma filter ->
                case ma of
                    Just album ->
                        let
                            (m1, c1) = libraryPageMsg (Pages.Library.Data.FindAlbum album) model
                            libraryInit = Pages.Library.Update.initCommands m1.libraryModel
                            bookletCmd = let cmd = Requests.testBooklet model.baseUrl album Pages.Library.Data.AlbumBookletResp in
                                         Cmd.map LibraryMsg cmd
                        in
                            m1 ! ((libraryInit |> send) :: c1 :: bookletCmd :: globalInit)

                    Nothing ->
                        let
                            m_ = {model|libraryModel = Pages.Library.Data.setAlbumList filter model.libraryModel}
                            scroll = Ports.scrollToElem (Util.Html.makeId m_.libraryModel.albumInfo.name)
                            libraryInit = Pages.Library.Update.initCommands m_.libraryModel
                        in
                            {m_|deferredCmds = scroll :: m_.deferredCmds} ! ((libraryInit |> send) :: globalInit)

            NowPlayingPage ->
                let
                    nowPlayingInit = Pages.NowPlaying.Update.initCommands model.nowPlayingModel
                    progress = Cmd.map NowPlayingMsg
                                 <| (Pages.NowPlaying.Update.initProgress model.nowPlayingModel)
                in
                    {model|deferredCmds = progress :: model.deferredCmds} ! ((nowPlayingInit |> send) :: globalInit)

            SettingsPage ->
                let
                    settingsInit = Pages.Settings.Update.initCommands model.settingsModel
                in
                   model ! ((settingsInit |> send) :: globalInit)

            PlaylistsPage mn ->
                let
                    (m_, mpd) = Pages.Playlists.Update.initCommands mn model.playlistsModel
                in
                    {model|playlistsModel = m_} ! ((mpd |> send) :: globalInit)

            SearchPage query page ->
                let
                    p = Maybe.withDefault 1 page
                    q = Maybe.withDefault "" query
                    (m_, mpd) = Pages.Search.Update.initCommands q p model.settingsModel.settings model.searchModel
                in
                    {model|searchModel = m_} ! ((mpd |> send) :: globalInit)

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
           , deferredCmds = Ports.initElements() :: model_.deferredCmds} ! [Ports.setTitle (getTitle lang page), cmd_]


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
            indexMsg m model

        LibraryMsg m ->
            libraryPageMsg m model

        NowPlayingMsg m ->
            nowPlayingMsg m model

        SettingsMsg m ->
            settingsMsg m model

        PlaylistsMsg m ->
            playlistsMsg m model

        SearchMsg m ->
            searchMsg m model

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
            let
                (m1, c1) = settingsMsg (Pages.Settings.Data.ReceiveSettings settings) model
                (m2, c2) = nowPlayingMsg (Pages.NowPlaying.Data.ReceiveSettings settings) m1
                (m3, c3) = searchMsg (Pages.Search.Data.ReceiveSettings settings) m2
            in
                (m3, Cmd.batch [c1, c2, c3])

        InfoLoad (Ok info) ->
            let
                (m1, c1) = settingsMsg (Pages.Settings.Data.ReceiveInfo info) model
                (m2, c2) = indexMsg (Pages.Index.Data.ReceiveInfo info) m1
                (m3, c3) = nowPlayingMsg (Pages.NowPlaying.Data.ReceiveInfo info) m2
            in
                m3 ! [c1, c2, c3]

        InfoLoad (Err err) ->
            let
                x = Debug.log "Error receiving info:" err
            in
                model ! []

        SetMpdConn conn ->
            let
                (m1, c1) = settingsMsg (Pages.Settings.Data.SetMpdConn conn) model
                (m2, c2) = initPage m1.page m1
            in
                m2 ! [c1, c2]

handleAnswerPages: Answer -> Model -> (Model, Cmd Msg)
handleAnswerPages ans model =
    let
        (m1, c1) = libraryPageMsg (Pages.Library.Data.HandleAnswer ans) model
        (m2, c2) = nowPlayingMsg (Pages.NowPlaying.Data.HandleAnswer ans) m1
        (m3, c3) = settingsMsg (Pages.Settings.Data.HandleAnswer ans) m2
        (m4, c4) = playlistsMsg (Pages.Playlists.Data.HandleAnswer ans) m3
        (m5, c5) = searchMsg (Pages.Search.Data.HandleAnswer ans) m4
    in
        m5 ! [c1, c2, c3, c4, c5]

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
        (lm, lc, mpd1, def1) = Pages.Library.Update.update lmsg model.settingsModel.settings model.libraryModel
        model_ = {model|libraryModel = lm, deferredCmds = (Cmd.map LibraryMsg def1) :: model.deferredCmds}
        (model2, cmd2) = sendMpd model_ mpd1
    in
        model2 ! [Cmd.map LibraryMsg lc, cmd2]

nowPlayingMsg: Pages.NowPlaying.Data.Msg -> Model -> (Model, Cmd Msg)
nowPlayingMsg nmsg model =
    let
        (nm, nc, mpd2, def2) = Pages.NowPlaying.Update.update model.settingsModel.settings nmsg model.nowPlayingModel
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

indexMsg: Pages.Index.Data.Msg -> Model -> (Model, Cmd Msg)
indexMsg msg model =
    let
        (m1, c1) = Pages.Index.Update.update msg model.indexModel
    in
        {model|indexModel = m1} ! [Cmd.map IndexMsg c1]

searchMsg: Pages.Search.Data.Msg -> Model -> (Model, Cmd Msg)
searchMsg msg model =
    let
        (m1, c1, mpd) = Pages.Search.Update.update model.settingsModel.settings msg model.searchModel
        model_ = {model|searchModel = m1}
        (m2, c2) = sendMpd model_ mpd
    in
        m2 ! [Cmd.map SearchMsg c1, c2]

sendMpd: Model -> List MpdCommand -> (Model, Cmd msg)
sendMpd model cmds =
    let
        conn = model.settingsModel.settings.mpdConn
        send = Requests.sendAll conn model.baseUrl
    in
        if cmds == [] then (model, Cmd.none)
        else model ! [send (Debug.log "send mpd: " cmds)]
