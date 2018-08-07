module Data.Info exposing (..)

import Json.Decode as Decode exposing (field)

type alias Info =
    { name: String
    , version: String
    , gitCommit: String
    , builtAtMillis: Float
    , mpd: MpdConfig
    }

type alias MpdConfig =
    { host: String
    , port_: Int
    }

jsonDecode: Decode.Decoder Info
jsonDecode =
    Decode.map5 Info
        (field "name" Decode.string)
        (field "version" Decode.string)
        (field "gitCommit" Decode.string)
        (field "builtAtMillis" Decode.float)
        (field "mpdConfig" (Decode.map2 MpdConfig
             (field "host" Decode.string)
             (field "port" Decode.int)))
