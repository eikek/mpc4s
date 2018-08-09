module Pages.Playlists.Update exposing (update, initCommands)

import Route exposing (Route(..))
import Pages.Playlists.Data exposing (..)
import Data.MpdCommand exposing (MpdCommand(..))
import Data.Answer exposing (Answer(..))
import Data.PlaylistName exposing (PlaylistName)
import Data.Subsystem

update: Msg -> Model -> (Model, Cmd Msg, List MpdCommand)
update msg model =
    case msg of
        HandleAnswer (PlaylistNameAnswer pl) ->
            ({model|playlists = pl}, Cmd.none, [])

        HandleAnswer (Songs sl) ->
            case model.lastMsg of
                Just (ShowPlaylist pn) ->
                    ({model|current = Just (StoredPlaylist pn sl), lastMsg = Nothing}
                    ,Cmd.none
                    ,[])

                _ ->
                    (model, Cmd.none, [])

        HandleAnswer (Idle evs) ->
            case (List.member Data.Subsystem.StoredPlaylist evs) of
                True ->
                    case model.current of
                        Just sp ->
                            ({model|lastMsg = Just (ShowPlaylist sp.name)}, Cmd.none, [ListPlaylistInfo sp.name.name])

                        Nothing ->
                            (model, Cmd.none, [ListPlaylists])
                _ ->
                    (model, Cmd.none, [])

        HandleAnswer _ ->
            (model, Cmd.none, [])

        ShowPlaylist pn ->
            ({model|lastMsg = Just (ShowPlaylist pn)}
            ,Route.setPage (PlaylistsPage <| Just pn.name)
            ,[]
            )

        DeletePlaylist pn ->
            (model, Route.setPage (PlaylistsPage Nothing), [PlaylistDelete pn.name])

        DeletePlaylistSong pn pos ->
            (model, Cmd.none, [PlaylistDeleteSong pn.name pos])

        LoadPlaylist pn ->
            (model, Cmd.none, [PlaylistLoad pn.name])

        PlayPlaylist pn ->
            (model, Cmd.none, [Clear, PlaylistLoad pn.name, Play Nothing])

        MoveSong pn from to ->
            (model, Cmd.none, [PlaylistMove pn.name from to])

        EnableRename ->
            case (model.current, model.rename) of
                (Just sp, Nothing) ->
                    ({model|rename = Just (RenameData sp.name.name sp.name.name)}, Cmd.none, [])

                (Just _, Just _) ->
                    ({model|rename = Nothing}, Cmd.none, [])

                _ ->
                    (model, Cmd.none, [])

        RenameChange str ->
            case model.rename of
                Just rn ->
                    ({model|rename = Just (RenameData rn.original str)}, Cmd.none, [])

                Nothing ->
                    (model, Cmd.none, [])

        RenamePlaylist ->
            case (model.current, model.rename) of
                (Just sp, Just {original, newName}) ->
                    let
                        npn = PlaylistName newName sp.name.lastModified
                    in
                        if original == newName then ({model|rename = Nothing}, Cmd.none, [])
                        else ({model|rename = Nothing, current = Just {sp|name = npn}, lastMsg = Just (ShowPlaylist npn)}, Cmd.none, [PlaylistRename original newName])

                _ ->
                    (model, Cmd.none, [])

        ToggleAddUri ->
            case model.current of
                Just _ ->
                    ({model|addUriVisible = not model.addUriVisible}, Cmd.none, [])
                Nothing ->
                    (model, Cmd.none, [])

        AddUriChange str ->
            ({model|uriToAdd = str}, Cmd.none, [])

        AddUri ->
            case (model.current, model.uriToAdd) of
                (Just sp, uri) ->
                    let
                        cmd = PlaylistAdd sp.name.name uri
                    in
                        if uri == "" then (model, Cmd.none, [])
                        else ({model|uriToAdd = "", addUriVisible = False}, Cmd.none, [cmd])

                _ ->
                    (model, Cmd.none, [])

        ToggleAddPlaylist ->
            case model.current of
                Nothing ->
                    ({model|addPlaylistVisible = not model.addPlaylistVisible}, Cmd.none, [])
                _ ->
                    (model, Cmd.none, [])

        AddPlaylistChange str ->
            ({model|playlistToAdd = str}, Cmd.none, [])

        AddPlaylist ->
            ({model|addPlaylistVisible = False, playlistToAdd = ""}
            ,Cmd.none
            ,[SaveCurrentPlaylist model.playlistToAdd, PlaylistClear model.playlistToAdd]
            )

initCommands: Maybe String -> Model -> (Model, List MpdCommand)
initCommands mname model =
    case mname of
        Just name ->
            ({model|lastMsg = Just (ShowPlaylist (PlaylistName name ""))}, [ListPlaylistInfo name])

        Nothing ->
            ({model|current = Nothing}, [ListPlaylists])
