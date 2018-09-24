module Data.Settings exposing (..)

import Data.MpdConn exposing (MpdConn)

{- Settings that are stored in local storage of the browser.
-}
type alias Settings =
    { lang: String
    , showVolume: Bool
    , volumeStep: Int
    , mpdConn: MpdConn
    , playElsewhereEnabled: Bool
    , playElsewhereOffset: Int
    , libraryIcons: String
    , searchPageSize: Int
    , searchView: Int
    , thumbnailSize: Int
    }

empty: Settings
empty =
    { lang = "en"
    , showVolume = True
    , volumeStep = 5
    , mpdConn = MpdConn "default" "Default" "" 0
    , playElsewhereEnabled = True
    , playElsewhereOffset = 5
    , libraryIcons = "medium"
    , searchPageSize = 300
    , searchView = 0
    , thumbnailSize = 500
    }
