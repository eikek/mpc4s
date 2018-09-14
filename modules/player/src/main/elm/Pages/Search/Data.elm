module Pages.Search.Data exposing (..)

import Util.List
import Data.Answer exposing (Answer)
import Data.Song exposing (Song)
import Data.MpdCommand exposing (MpdCommand)
import Data.Tag exposing (Tag(..))
import Data.PlaylistName exposing (PlaylistName)
import Data.Status exposing (Status)
import Data.Settings exposing (Settings)

type alias Model =
    { songs: List Song
    , albums: List String
    , query: String
    , page: Int
    , viewMode: ViewMode
    , currentCmd: Maybe MpdCommand
    , selectedPlaylist: Maybe PlaylistName
    , playlists: List PlaylistName
    , status: Status
    , settings: Settings
    }

create: String -> Int -> Settings -> Model
create query page settings =
    setPage page {empty | query = query
                 , viewMode = viewModeFromSettings settings
                 , settings = settings
                 }

empty: Model
empty =
    { songs = []
    , albums = []
    , query = ""
    , page = 1
    , viewMode = AlbumView
    , currentCmd = Nothing
    , selectedPlaylist = Nothing
    , playlists = []
    , status = Data.Status.empty
    , settings = Data.Settings.empty
    }

setPage: Int -> Model -> Model
setPage page model =
    let
        p = if page > 0 then page else 1
    in
        {model|page = p}

setSongs: List Song -> Model -> Model
setSongs songs model =
    {model|songs = songs, albums = makeAlbums songs}

makeAlbums: List Song -> List String
makeAlbums songs =
    List.filterMap (Data.Song.findTag Album) songs
        |> Util.List.distinct

type ViewMode
    = SongView
    | AlbumView

viewModeFromSettings: Settings -> ViewMode
viewModeFromSettings settings =
    case settings.searchView of
        0 -> AlbumView
        1 -> SongView
        _ -> AlbumView

type Msg
    = HandleAnswer Answer
    | SetQuery String
    | DoSearch
    | ToDetailPage String
    | SetViewMode ViewMode
    | ClearPlaySong Song
    | AppendSong Song
    | InsertSong Song
    | SelectPlaylist (Maybe PlaylistName)
    | PrevPage
    | NextPage
    | ReceiveSettings Settings
    | ToggleIcons
