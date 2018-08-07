module Data.Range exposing (..)

import Json.Decode as Decode exposing (field)

type alias Range =
    { start: Int
    , end: Int
    }

jsonDecode: Decode.Decoder Range
jsonDecode =
    Decode.map2 Range
        (field "start" Decode.int)
        (field "end" Decode.int)

encode: Range -> String
encode range =
    (toString range.start) ++ ":" ++ (toString range.end)
