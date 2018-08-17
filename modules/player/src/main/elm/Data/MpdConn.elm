module Data.MpdConn exposing (..)

import Json.Decode as Decode exposing (field)

type alias MpdConn =
    { id: String
    , title: String
    , host: String
    , port_: Int
    }

jsonDecode: Decode.Decoder MpdConn
jsonDecode =
    Decode.map4 MpdConn
        (field "id" Decode.string)
        (field "title" Decode.string)
        (field "host" Decode.string)
        (field "port" Decode.int)
