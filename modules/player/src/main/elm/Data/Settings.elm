module Data.Settings exposing (..)

import Data.MpdConn exposing (MpdConn)

type alias Settings =
    { lang: String
    , showVolume: Bool
    , volumeStep: Int
    , mpdConn: MpdConn
    }

empty: Settings
empty =
    { lang = "en"
    , showVolume = True
    , volumeStep = 5
    , mpdConn = MpdConn "default" "Default" "" 0
    }
