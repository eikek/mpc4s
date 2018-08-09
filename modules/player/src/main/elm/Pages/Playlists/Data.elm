module Pages.Playlists.Data exposing (..)

import Data.Answer exposing (Answer)
import Data.PlaylistName exposing (PlaylistName)
import Data.Song exposing (Song)
import Data.MpdCommand exposing (MpdCommand)

type alias Model =
    { playlists: List PlaylistName
    , current: Maybe StoredPlaylist
    , lastMsg: Maybe Msg
    , rename: Maybe RenameData
    , addUriVisible: Bool
    , uriToAdd: String
    , addPlaylistVisible: Bool
    , playlistToAdd: String
    }

type alias StoredPlaylist =
    { name: PlaylistName
    , songs: List Song
    }

type alias RenameData =
    { original: String
    , newName: String
    }

empty: Model
empty =
    { playlists = []
    , current = Nothing
    , lastMsg = Nothing
    , rename = Nothing
    , addUriVisible = False
    , uriToAdd = ""
    , addPlaylistVisible = False
    , playlistToAdd = ""
    }

type Msg
    = HandleAnswer Answer
    | ShowPlaylist PlaylistName
    | DeletePlaylistSong PlaylistName Int
    | DeletePlaylist PlaylistName
    | LoadPlaylist PlaylistName
    | PlayPlaylist PlaylistName
    | MoveSong PlaylistName Int Int
    | EnableRename
    | RenameChange String
    | RenamePlaylist
    | ToggleAddUri
    | AddUriChange String
    | AddUri
    | ToggleAddPlaylist
    | AddPlaylistChange String
    | AddPlaylist
