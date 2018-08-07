module Pages.Library.AlbumInfo exposing (..)

import Util.List
import Util.String exposing ((#>))
import Data.Song exposing (Song)
import Data.Tag exposing (Tag(..))

type alias AlbumTrack =
    { file: String
    , no: String
    , title: String
    , artist: String
    , composer: String
    , time: Int
    , filetype: String
    }

type alias AlbumDisc =
    { year: String
    , genre: String
    , time: Int
    , artist: String
    , composer: String
    , tracks: List AlbumTrack
    , filetype: String
    , disc: String
    , lastModified: String
    }

type alias AlbumInfo =
    { name: String
    , time: Int
    , songs: Int
    , discs: List AlbumDisc
    , lastModified: String
    }

emptyDisc: AlbumDisc
emptyDisc =
    { year = ""
    , genre = ""
    , time = 0
    , artist = ""
    , composer = ""
    , tracks = []
    , filetype = ""
    , disc = ""
    , lastModified = ""
    }

emptyTrack: AlbumTrack
emptyTrack =
    { file = ""
    , no = ""
    , title = ""
    , artist = ""
    , composer = ""
    , time = 0
    , filetype = ""
    }

emptyInfo: AlbumInfo
emptyInfo =
    { name = ""
    , time = 0
    , songs = 0
    , discs = []
    , lastModified = ""
    }

makeDetail: List Song -> AlbumInfo
makeDetail songs =
    case songs of
        [] -> AlbumInfo "" 0 0 [] ""

        song :: _ ->
            let
                name = Data.Song.findTag Album song
                       |> Maybe.withDefault ""
                time = List.sum (List.map .time songs)
                count = List.length songs
                discs = groupDiscs songs
                lastmod = List.sortBy .lastModified discs
                        |> List.head
                        |> Maybe.map .lastModified
                        |> Maybe.withDefault ""
            in
            AlbumInfo name time count discs lastmod


groupDiscs: List Song -> List AlbumDisc
groupDiscs songs =
    let
        discs = getDiscNames songs
    in
        List.map (getSongsForDisc songs) discs
            |> List.map makeAlbumDisc

getDiscNames: List Song -> List String
getDiscNames songs =
    List.map (Data.Song.findTag Disc) songs
        |> List.map (Maybe.withDefault "")
        |> Util.List.distinct

getSongsForDisc: List Song -> String -> (String, (List Song))
getSongsForDisc songs disc =
    (disc, List.filter (\s -> (Data.Song.findTag Disc s |> Maybe.withDefault "") == disc) songs)

makeAlbumDisc: (String, (List Song)) -> AlbumDisc
makeAlbumDisc discsongs =
    case discsongs of
        (disc, songs) ->
            let
                zero = {emptyDisc | disc = disc}
                album = List.foldl mergeAlbumDisc zero songs
                tracks = List.map makeTrack songs
            in
                {album|tracks = tracks}


mergeAlbumDisc: Song -> AlbumDisc -> AlbumDisc
mergeAlbumDisc song ad =
    {ad | year = chooseTagValue song Date ad.year
    , genre = chooseTagValue song Genre ad.genre
    , artist = chooseTagValue song Artist ad.artist
    , composer = chooseComposer song ad.composer
    , time = ad.time + song.time
    , filetype = chooseFiletype song ad.filetype
    , lastModified = Util.String.nonEmptyMin song.lastModified ad.lastModified
    }

chooseTagValue: Song -> Tag -> String -> String
chooseTagValue song tag preferred =
    if (preferred /= "") then preferred
    else Data.Song.findTag tag song |> Maybe.withDefault preferred

chooseComposer: Song -> String -> String
chooseComposer song current =
    let
        next = Data.Song.findTag Composer song |> Maybe.withDefault current
    in
        if (String.contains next current) then current
        else current #> next

chooseFiletype: Song -> String -> String
chooseFiletype song pref =
    if (pref /= "") then pref
    else Data.Song.findFiletype song
        |> Maybe.withDefault pref

makeTrack: Song -> AlbumTrack
makeTrack song =
    let
        zero = {emptyTrack|file = song.file, time = song.time}
    in
        {zero|no = chooseTagValue song Track ""
             ,title = chooseTagValue song Title ""
             ,artist = chooseTagValue song Artist ""
             ,composer = chooseTagValue song Composer ""
             ,filetype = chooseFiletype song ""
        }
