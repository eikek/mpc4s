module Data.AudioFormat exposing (..)

import Json.Decode as Decode exposing (field)

type alias AudioFormat =
    { freq: Int
    , bits: Int
    , channels: Int
    }

jsonDecode: Decode.Decoder AudioFormat
jsonDecode =
    Decode.map3 AudioFormat
        (field "freq" Decode.int)
        (field "bits" Decode.int)
        (field "channels" Decode.int)
