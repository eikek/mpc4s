module Data.SongState exposing (SongState, empty)

import Json.Decode as Decode exposing (field)
import Data.Range exposing (Range)
import Data.AudioFormat exposing (AudioFormat)

{-- Song information in status response
 --}
type alias SongState =
    { song: Int
    , songId: Int
    , time: Range
    , bitrate: Int
    , audio: AudioFormat
    }

empty: SongState
empty =
    { song = 0
    , songId = 0
    , time = Range 0 0
    , bitrate = 0
    , audio = AudioFormat 0 0 0
    }
