module Data.Song exposing (..)

import Json.Decode as Decode exposing (field)
import Dict exposing (Dict)
import Data.Tag exposing (Tag)

type alias Song =
    { file: String
    , lastModified: String
    , time: Int
    , tags: Dict String String
    }

empty: Song
empty =
    { file = ""
    , lastModified = ""
    , time = 0
    , tags = Dict.empty
    }

jsonDecode: Decode.Decoder Song
jsonDecode =
    Decode.map4 Song
        (field "file" Decode.string)
        (field "Last-Modified" (Decode.map (Maybe.withDefault "") (Decode.nullable Decode.string)))
        (field "time" (Decode.map (Maybe.withDefault 0) (Decode.nullable Decode.int)))
        (field "tags" (Decode.dict Decode.string))

findTag: Tag -> Song -> Maybe String
findTag tag song =
    let
        tn = Data.Tag.encode tag
    in
        Dict.get tn song.tags

findFiletype: Song -> Maybe String
findFiletype song =
    String.split "." song.file
        |> List.reverse
        |> List.head
        |> Maybe.map String.toUpper
