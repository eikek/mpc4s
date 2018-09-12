module Data.AlbumFile exposing (..)

import Json.Decode as Decode exposing (field)

type alias AlbumFile =
    { exists: Bool
    , fileUrl: String
    }

empty: AlbumFile
empty =
    AlbumFile False ""


jsonDecode: Decode.Decoder AlbumFile
jsonDecode =
    Decode.map2 AlbumFile
        (field "exists" Decode.bool)
        (field "fileUrl" Decode.string)
