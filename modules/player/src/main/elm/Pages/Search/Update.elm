module Pages.Search.Update exposing (update, initCommands)

import Ports
import Route exposing (Route(..))
import Pages.Search.Data exposing (..)
import Data.Answer exposing (Answer(..))
import Data.MpdCommand exposing (MpdCommand(..))
import Data.Settings exposing (Settings)
import Data.Range exposing (Range)
import Data.Tag exposing (Tag(..))
import Data.TagValue exposing (TagValue)

update: Settings -> Msg -> Model -> (Model, Cmd Msg, List MpdCommand)
update settings msg model =
    case msg of
        SetQuery text ->
            ({model|query = text, page = 1}, Cmd.none, [])

        DoSearch ->
            (model, Route.setPage (SearchPage (Just model.query) (Just model.page)), [])

        HandleAnswer (Songs sl) ->
            case model.currentCmd of
                Just (SearchAny _ _) ->
                    let
                        m_ = setSongs sl model
                    in
                        ({m_|currentCmd = Nothing}, Cmd.none, [])

                _ ->
                    (model, Cmd.none, [])

        HandleAnswer (PlaylistNameAnswer pl) ->
            ({model|playlists = pl}, Cmd.none, [])

        HandleAnswer (StatusInfo status) ->
            ({model|status = status}, Cmd.none, [])

        HandleAnswer _ ->
            (model, Cmd.none, [])

        ReceiveSettings settings ->
            ({model|viewMode = viewModeFromSettings settings, settings = settings}, Cmd.none, [])

        ToDetailPage name ->
            let
                cmd = if name == "" then Cmd.none
                      else Route.setPage (LibraryPage (Just name) [])
            in
                (model, cmd, [])

        SetViewMode vm->
            ({model|viewMode = vm}, Cmd.none, [])

        ClearPlaySong song ->
            (model, Cmd.none, [Clear, FindAdd [TagValue File song.file], Play Nothing])

        AppendSong song ->
            case model.selectedPlaylist of
                Just pn ->
                    (model, Cmd.none, [PlaylistAdd pn.name song.file])

                Nothing ->
                    (model, Cmd.none, [Add song.file])

        InsertSong song ->
            let
                cmd =
                    case model.status.song of
                        Just pos-> [AddId song.file (Just (pos + 1))]

                        Nothing -> []
            in
                (model, Cmd.none, cmd)

        SelectPlaylist pn ->
            ({model|selectedPlaylist = pn}, Cmd.none, [])

        PrevPage ->
            if model.page <= 1 then
                (model, Cmd.none, [])
            else
                (model, Route.setPage (SearchPage (Just model.query) (Just <| model.page - 1)), [])

        NextPage ->
            if model.songs == [] then
                (model, Cmd.none, [])
            else
                (model, Route.setPage (SearchPage (Just model.query) (Just <| model.page + 1)), [])

        ToggleIcons ->
            let
                next = if model.settings.libraryIcons == "medium" then "small" else "medium"
                sett = {settings|libraryIcons = next}
            in
                (model, Ports.storeSettings sett, [])



initCommands: String -> Int -> Settings -> Model -> (Model, List MpdCommand)
initCommands query page settings model =
    let
        m = {model|query = query, page = page}
        cmd = SearchAny query (findRange m settings)
    in
        if query == "" then
            ({m|currentCmd = Nothing}, [ListPlaylists])
        else
            ({m|currentCmd = Just cmd}, [cmd, ListPlaylists])

findRange: Model -> Settings -> Range
findRange model settings =
    let
        pageNum = model.page
        pageSiz = settings.searchPageSize
    in
        Range ((pageNum - 1) * pageSiz) (pageNum * pageSiz)
