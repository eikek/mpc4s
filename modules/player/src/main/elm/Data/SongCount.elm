module Data.SongCount exposing (..)

import Json.Decode as Decode exposing (field)
import Data.Tag exposing (Tag)

type alias SongCount =
    { songs: Int
    , playtime: Int
    , group: Maybe Tag
    }

empty: SongCount
empty =
    { songs = 0
    , playtime = 0
    , group = Nothing
    }

jsonDecode: Decode.Decoder SongCount
jsonDecode =
    Decode.map3 SongCount
        (field "songs" Decode.int)
        (field "playtime" Decode.int)
        (field "group" (Decode.maybe Data.Tag.jsonDecode))
