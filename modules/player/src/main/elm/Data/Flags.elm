module Data.Flags exposing (..)

import Data.MpdConn exposing (MpdConn)

type alias Flags =
    { baseUrl: String
    , mpdConns: List MpdConn
    }
