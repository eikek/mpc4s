module Data.PlaylistName exposing (..)

import Time exposing (Time)
import Json.Decode as Decode exposing (field)

type alias PlaylistName =
    { name: String
    , lastModified: String
    }

jsonDecode: Decode.Decoder PlaylistName
jsonDecode =
    Decode.map2 PlaylistName
        (field "playlist" Decode.string)
        (field "Last-Modified" Decode.string)
