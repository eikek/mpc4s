module Data.TagValue exposing (..)

import Json.Decode as Decode exposing (field)
import Util.String exposing (quote)
import Data.Tag exposing (Tag)

type alias TagValue =
    { tag: Tag
    , value: String
    }

encode: TagValue -> String
encode tv =
    (Data.Tag.encode tv.tag ++ " " ++ (quote tv.value))

jsonDecode: Decode.Decoder TagValue
jsonDecode =
    Decode.map2 TagValue
        (field "tag" Data.Tag.jsonDecode)
        (field "value" Decode.string)
