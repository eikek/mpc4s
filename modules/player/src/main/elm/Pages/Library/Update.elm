module Pages.Library.Update exposing (update, initCommands)

import Json.Decode as Decode exposing (field)
import Ports
import Pages.Library.Data exposing (..)
import Pages.Library.AlbumInfo exposing (AlbumInfo, AlbumDisc, AlbumTrack)
import Route exposing (Route(..))
import Requests
import Ports
import Util.Maybe
import Util.List
import Data.MpdCommand exposing (MpdCommand(..))
import Data.Tag exposing (Tag(..))
import Data.TagValue exposing (TagValue)
import Data.Filter
import Data.Answer exposing (Answer(..))
import Data.Settings exposing (Settings)

update: Msg -> Settings -> Model -> (Model, Cmd Msg, List MpdCommand, Cmd Msg)
update msg settings model =
    case msg of
        LoadAlbums ->
           (model, Cmd.none, loadAlbums model, Cmd.none)

        HandleAnswer (TagVals vals) ->
            case vals of
                [] -> ({model | albums = []}, Cmd.none, [], Cmd.none)

                {tag, value} :: vs ->
                    let
                        l = List.map .value vals
                        m_ =
                            if (tag == Album) then updateSelection {model| albums = l}
                            else if (tag == Genre) then {model|genres = l}
                            else if (tag == Composer) then {model|composers = l}
                            else if (tag == Artist || tag == Albumartist) then {model|artists = l}
                            else model
                        mpd = if tag == Album then [listComposer model, listArtists model] else []
                    in
                        (m_, Cmd.none, mpd, Cmd.none)

        HandleAnswer (SongCountInfo sc) ->
            case sc of
                a :: _ ->
                    ({model|selection = songCountToInfo model a, songCount = a}, Cmd.none, [], Cmd.none)

                _ ->
                    (model, Cmd.none, [], Cmd.none)

        HandleAnswer (StatsInfo stats) ->
            let
                m0 = {model|stats = stats}
                m = updateSelection m0
            in
                (m, Cmd.none, [], Cmd.none)

        HandleAnswer (Songs sl) ->
            case model.currentCmd of
                Just (Find [{tag, value}]) ->
                    ( {model|album = sl, albumInfo = Pages.Library.AlbumInfo.makeDetail sl, mode = AlbumDetail, currentCmd = Nothing}
                    , Cmd.none
                    , []
                    , (Ports.scrollTo (Pos 0 0))
                    )
                _ ->
                    (model, Cmd.none, [], Cmd.none)

        HandleAnswer (StatusInfo status) ->
            ({model|status = status}, Cmd.none, [], Cmd.none)

        HandleAnswer (PlaylistNameAnswer pl) ->
            ({model|playlists = pl}, Cmd.none, [], Cmd.none)

        HandleAnswer _ ->
            (model, Cmd.none, [], Cmd.none)

        ToggleFilterMenu tag ->
            case model.mode of
                AlbumList ->
                    ({model|mode = FilterView tag}, Cmd.none, [], Cmd.none)

                FilterView et ->
                    let
                        m = if (et == tag) then {model|mode = AlbumList}
                            else {model|mode = FilterView tag}
                    in
                        (m, Cmd.none, [], Cmd.none)

                AlbumDetail ->
                    (model, Cmd.none, [], Cmd.none)


        ToggleFilter tv ->
            -- genre resets other filters; other filter options are always restricted
            let
                nf = let ff = Data.Filter.toggle tv model.filter in
                     if tv.tag == Genre then Data.Filter.keepOnly Genre ff
                     else if tv.tag == Albumartist then Data.Filter.remove Artist ff
                     else if tv.tag == Artist then Data.Filter.remove Albumartist ff
                     else ff
                m = {model|filter = nf, mode = AlbumList}
                cmd = Route.setPage (LibraryPage Nothing nf)
            in
                (model, cmd, (listFilters m), Cmd.none)

        ClearFilter ->
            let
                m = {model|filter = Data.Filter.empty, selection = statsToInfo model.stats, mode = AlbumList}
            in
                (m, Route.setPage (LibraryPage Nothing []), [], Cmd.none)

        FindAlbum albumName ->
            let
                cmd = Find [TagValue Album albumName]
            in
                ({model|collapsedDiscs = [], currentCmd = Just cmd}, Ports.getScroll (), [cmd], Cmd.none)

        SwitchMode mode ->
            ({model|mode = mode}, Cmd.none, [], Ports.scrollTo (Debug.log "Scroll to: " model.scroll))

        ClearPlayAll name ->
            (model, Cmd.none, [Clear, FindAdd [TagValue Album name], Play Nothing], Cmd.none)

        ClearPlayDisc name disc ->
            (model, Cmd.none, [Clear, FindAdd [TagValue Album name, TagValue Disc disc], Play Nothing], Cmd.none)

        ClearPlaySong file ->
            (model, Cmd.none, [Clear, FindAdd [TagValue File file], Play Nothing], Cmd.none)

        AppendAll name ->
            case model.selectedPlaylist of
                Just pn ->
                    let
                        cmd = List.map .file model.album
                              |> List.map (\f -> PlaylistAdd pn.name f)
                    in
                        (model, Cmd.none, cmd, Cmd.none)

                Nothing ->
                    (model, Cmd.none, [FindAdd [TagValue Album name]], Cmd.none)

        AppendDisc name disc ->
            case model.selectedPlaylist of
                Just pn ->
                    let
                        cmd = Util.List.find (\d -> d.disc == disc) model.albumInfo.discs
                              |> Maybe.map .tracks
                              |> Maybe.withDefault []
                              |> List.map .file
                              |> List.map (\f -> PlaylistAdd pn.name f)
                    in
                        (model, Cmd.none, cmd, Cmd.none)

                Nothing ->
                    (model, Cmd.none, [FindAdd [TagValue Album name, TagValue Disc disc]], Cmd.none)

        AppendSong file ->
            case model.selectedPlaylist of
                Just pn ->
                    (model, Cmd.none, [PlaylistAdd pn.name file], Cmd.none)

                Nothing ->
                    (model, Cmd.none, [Add file], Cmd.none)

        CurrentScroll pos ->
            ({model|scroll = Debug.log "pos = " pos}, Cmd.none, [], Cmd.none)

        ToDetailPage album ->
            (model, Route.setPage (LibraryPage (Just album) []), [], Cmd.none)

        ToggleDiscCollapse disc ->
            let
                info = Pages.Library.AlbumInfo.makeDetail model.album
            in
                case info.discs of
                    [] -> (model, Cmd.none, [], Cmd.none)
                    a :: [] -> (model, Cmd.none, [], Cmd.none)
                    _ ->
                        let
                            model_ = case (discCollapsed disc model) of
                                         True -> {model|collapsedDiscs = List.filter (\e -> e /= disc) model.collapsedDiscs}
                                         False -> {model|collapsedDiscs = disc :: model.collapsedDiscs}
                        in
                            (model_, Cmd.none, [], Cmd.none)

        InsertSong file ->
            let
                cmd =
                    case model.status.song of
                        Just pos-> [AddId file (Just (pos + 1))]

                        Nothing -> []
            in
                (model
                , Cmd.none
                , cmd
                , Cmd.none
                )

        InsertDisc album disc ->
            let
                cmd =
                    case model.status.song of
                        Just pos->
                            Util.List.find (\d -> d.disc == disc) model.albumInfo.discs
                                |> Maybe.map .tracks
                                |> Maybe.withDefault []
                                |> List.map .file
                                |> List.map (\f -> AddId f (pos + 1 |> Just))
                                |> List.reverse

                        Nothing -> []
            in
                (model
                , Cmd.none
                , cmd
                , Cmd.none
                )

        InsertAll album ->
            let
                cmd =
                    case model.status.song of
                        Just pos->
                            List.map .file model.album
                                |> List.map (\f -> AddId f (pos + 1 |> Just))
                                |> List.reverse

                        Nothing -> []
            in
                (model
                , Cmd.none
                , cmd
                , Cmd.none
                )

        SelectPlaylist pn ->
            ({model|selectedPlaylist = pn}, Cmd.none, [], Cmd.none)

        ToggleLibraryIcons ->
            let
                next = if settings.libraryIcons == "medium" then "small" else "medium"
                sett = {settings|libraryIcons = next}
            in
            (model, Ports.storeSettings sett, [], Cmd.none)

updateSelection: Model -> Model
updateSelection model =
    if (Data.Filter.isEmpty model.filter) then {model|selection = statsToInfo model.stats}
    else {model|selection = songCountToInfo model model.songCount}

initCommands: Model -> List MpdCommand
initCommands model =
    (loadAlbums model) ++ (listFiltersInit model) ++ [Stats, ListPlaylists]

listGenre: Model -> MpdCommand
listGenre model =
    ListTags Genre []

listComposer: Model -> MpdCommand
listComposer model =
    ListTags Composer model.filter

listArtists: Model -> MpdCommand
listArtists model =
    ListTags Albumartist model.filter

listFilters: Model -> List MpdCommand
listFilters model =
    [listGenre model, listComposer model, listArtists model]

listFiltersInit: Model -> List MpdCommand
listFiltersInit model =
    if model.mode /= AlbumList then []
    else (if model.genres == [] then [listGenre model] else []) ++
         (if model.composers == [] then [listComposer model] else []) ++
         (if model.artists == [] then [listArtists model] else [])

loadAlbums: Model -> List MpdCommand
loadAlbums model =
    [ListTags Album model.filter] ++ (countSongs model)

countSongs: Model -> List MpdCommand
countSongs model =
    if (Data.Filter.isEmpty model.filter) then []
    else [CountSongs model.filter]

albumListDecoder: Decode.Decoder (List String)
albumListDecoder =
    (field "result" (field "tags" (Decode.list (field "value" Decode.string))))
