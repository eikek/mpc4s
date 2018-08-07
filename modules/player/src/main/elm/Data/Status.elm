module Data.Status exposing (..)

import Json.Decode as Decode exposing (field)
import Json.Decode.Pipeline as DP

import Util.Json
import Data.Range exposing (Range)
import Data.AudioFormat exposing (AudioFormat)
import Data.PlayState exposing (PlayState)
import Data.SingleState exposing (SingleState)
import Data.SongState exposing (SongState)

type alias Status =
    { volume: Int
    , repeat: Bool
    , random: Bool
    , single: SingleState
    , consume: Bool
    , state: PlayState
    , playlist: Int
    , playlistlength: Int
    , song: Maybe Int
    , songId: Maybe String
    , time: Maybe Range
    , bitrate: Maybe Int
    , audio: Maybe AudioFormat
    , nextSong: Maybe Int
    , nextSongId: Maybe String
    , updatingDb: Maybe String
    }

empty: Status
empty =
    { volume = 0
    , repeat = False
    , random = False
    , single = Data.SingleState.Off
    , consume = False
    , state = Data.PlayState.Stop
    , playlist = 0
    , playlistlength = 0
    , song = Nothing
    , songId = Nothing
    , time = Nothing
    , bitrate = Nothing
    , audio = Nothing
    , nextSong = Nothing
    , nextSongId = Nothing
    , updatingDb = Nothing
    }

isEmpty: Status -> Bool
isEmpty status =
    status == empty

jsonDecode: Decode.Decoder Status
jsonDecode =
    DP.decode Status
        |> DP.required "volume" Decode.int
        |> DP.required "repeat" Decode.bool
        |> DP.required "random" Decode.bool
        |> DP.required "single" Data.SingleState.jsonDecode
        |> DP.required "consume" Decode.bool
        |> DP.required "state" Data.PlayState.jsonDecode
        |> DP.required "playlist" Decode.int
        |> DP.required "playlistlength" Decode.int
        |> DP.required "song" (Decode.maybe Decode.int)
        |> DP.required "songid" (Util.Json.decodeMaybeString)
        |> DP.required "time" (Decode.maybe Data.Range.jsonDecode)
        |> DP.required "bitrate" (Decode.maybe Decode.int)
        |> DP.required "audio" (Decode.maybe Data.AudioFormat.jsonDecode)
        |> DP.required "nextsong" (Decode.maybe Decode.int)
        |> DP.required "nextsongid" (Util.Json.decodeMaybeString)
        |> DP.required "updating_db" (Util.Json.decodeMaybeString)

volumeStep: Status -> Int -> Int
volumeStep status step =
    let
        nv = status.volume + step
    in
        if (nv < 0) then 0
        else if (nv > 100) then 100
        else nv
