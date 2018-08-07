module Data.MpdCommand exposing (..)

import Json.Decode as Decode exposing (field)
import Json.Encode as Encode

import Util.String exposing (..)
import Data.Tag exposing (Tag)
import Data.Filter exposing (Filter)
import Data.Range exposing (Range)
import Data.SingleState exposing (SingleState)

type MpdCommand
    = Play (Maybe Int)
    | Pause Bool
    | Stop
    | Next
    | Prev
    | Random Bool
    | Repeat Bool
    | Consume Bool
    | Single SingleState
    | SetVol Int
    | CurrentSong
    | Stats
    | Status
    | ListTags Tag Filter
    | CountSongs Filter
    | Find Filter
    | FindAdd Filter
    | Clear
    | PlaylistInfo
    | Delete Int
    | DeleteRange Range
    | Shuffle
    | SeekCur Int
    | AddId String (Maybe Int)
    | Add String
    | Swap Int Int
    | Rescan
    | Update

jsonEncode: MpdCommand -> Encode.Value
jsonEncode cmd =
    Encode.object
    [ ("command", Encode.string (encode cmd))
    ]

encode: MpdCommand -> String
encode cmd =
    case cmd of
        Play mi ->
            "play" %> (Maybe.map toString mi |> Maybe.withDefault "")

        Pause f ->
            "pause" %> (encodeBool f)

        Stop ->
            "stop"

        Next ->
            "next"

        Prev ->
            "previous"

        SetVol n ->
            "setvol" %> (toString n)

        CurrentSong ->
            "currentsong"

        Stats ->
            "stats"

        Status ->
            "status"

        Clear ->
            "clear"

        Rescan ->
            "rescan"

        Update ->
            "update"

        Random f ->
            "random" %> (encodeBool f)

        Repeat f ->
            "repeat" %> (encodeBool f)

        Consume f ->
            "consume" %> (encodeBool f)

        Single f ->
            "single" %> (Data.SingleState.encode f)

        PlaylistInfo ->
            "playlistinfo"

        ListTags tag filter ->
            let
                ts = Data.Tag.encode tag
                fs = Data.Filter.encode filter
            in
                "list" %> ts %> fs

        CountSongs filter ->
            "count" %> (Data.Filter.encode filter)

        Find filter ->
            "find" %> (Data.Filter.encode filter)

        FindAdd filter ->
            "findadd" %> (Data.Filter.encode filter)

        Delete pos ->
            "delete" %> (toString pos)

        Shuffle ->
            "shuffle"

        SeekCur secs ->
            "seekcur" %> (toString secs)

        DeleteRange range ->
            "delete" %> (Data.Range.encode range)

        AddId file pos ->
            "addid" %> (Util.String.quote file) %> (Maybe.map toString pos |> Maybe.withDefault "")

        Add uri ->
            "add" %> (Util.String.quote uri)

        Swap p1 p2 ->
            "swap" %> (toString p1) %> (toString p2)

encodeBool: Bool -> String
encodeBool flag =
    if (flag) then "1"
    else "0"
