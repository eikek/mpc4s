module Pages.Library.Data exposing (..)

import Http
import Data.Tag exposing (Tag)
import Data.TagValue exposing (TagValue)
import Data.Filter exposing (Filter)
import Data.Answer exposing (Answer(..))
import Data.Stats exposing (Stats)
import Data.SongCount exposing (SongCount)
import Data.Song exposing (Song)
import Data.Status exposing (Status)
import Pages.Library.AlbumInfo exposing (AlbumInfo)

{- Note: The albums are in a dict to be accessed via index. I first
used a List String which is much better since I can reference an album
detail without runtime state. But encoding current album in the url, I
ran into weird errors with url-parser and arbitrary text (chars like =
or : broke the parser) --> need custom “safe-string” codec
-}

type alias Model =
    { albums: List String
    , filter: Filter
    , stats: Stats
    , songCount: SongCount
    , selection: SelectionInfo
    , genres: List String
    , composers: List String
    , artists: List String
    , album: List Song
    , albumInfo: AlbumInfo
    , collapsedDiscs: List String
    , mode: ViewMode
    , scroll: Pos
    , status: Status
    }

emptyModel: Model
emptyModel =
    { albums = []
    , filter = Data.Filter.empty
    , stats = Data.Stats.empty
    , songCount = Data.SongCount.empty
    , selection = SelectionInfo 0 0 0
    , genres = []
    , composers = []
    , artists = []
    , album = []
    , albumInfo = Pages.Library.AlbumInfo.emptyInfo
    , collapsedDiscs = []
    , mode = AlbumList
    , scroll = Pos 0 0
    , status = Data.Status.empty
    }

type ViewMode
    = AlbumList
    | AlbumDetail
    | FilterView Tag

type alias SelectionInfo =
    { songs: Int
    , albums: Int
    , playtime: Int
    }

setFilter: Filter -> Model -> Model
setFilter filter model =
    {model|filter = filter}

discCollapsed: String -> Model -> Bool
discCollapsed disc model =
    List.member disc model.collapsedDiscs

statsToInfo: Stats -> SelectionInfo
statsToInfo stats =
    SelectionInfo stats.songs stats.albums stats.dbPlaytime

songCountToInfo: Model -> SongCount -> SelectionInfo
songCountToInfo model sc =
    SelectionInfo sc.songs (List.length model.albums) sc.playtime

selectionInfo: Model -> SelectionInfo
selectionInfo model =
    if (Data.Filter.isEmpty model.filter) then (statsToInfo model.stats)
    else model.selection

type alias Pos =
    { left: Float
    , top: Float
    }

type Msg
    = LoadAlbums
    | HandleAnswer Answer
    | ToggleFilter TagValue
    | ToggleFilterMenu Tag
    | ClearFilter
    | FindAlbum String
    | SwitchMode ViewMode
    | ClearPlayAll String
    | AppendAll String
    | CurrentScroll Pos
    | ToDetailPage String
    | ClearPlayDisc String String
    | AppendDisc String String
    | ToggleDiscCollapse String
    | ClearPlaySong String
    | AppendSong String
    | InsertAll String
    | InsertSong String
    | InsertDisc String String
