module Data.Info exposing (..)

import Json.Decode as Decode exposing (field)
import Data.MpdConn exposing (MpdConn)

type alias Info =
    { name: String
    , version: String
    , gitCommit: String
    , builtAtMillis: Float
    , mpd: List MpdConn
    }

jsonDecode: Decode.Decoder Info
jsonDecode =
    Decode.map5 Info
        (field "name" Decode.string)
        (field "version" Decode.string)
        (field "gitCommit" Decode.string)
        (field "builtAtMillis" Decode.float)
        (field "mpdConnections"
             (Decode.list (Data.MpdConn.jsonDecode)))
