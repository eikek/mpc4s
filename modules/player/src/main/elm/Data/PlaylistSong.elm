module Data.PlaylistSong exposing (..)

import Json.Decode as Decode exposing (field)
import Data.Song exposing (Song)

type alias PlaylistSong =
    { song: Song
    , pos: Int
    , id: String
    }

empty: PlaylistSong
empty =
    { song = Data.Song.empty
    , pos = -1
    , id = ""
    }


jsonDecode: Decode.Decoder PlaylistSong
jsonDecode =
    Decode.map3 PlaylistSong
        (field "song" Data.Song.jsonDecode)
        (field "pos" Decode.int)
        (field "id" Decode.string)
            |> Decode.nullable
            |> Decode.map (Maybe.withDefault empty)
