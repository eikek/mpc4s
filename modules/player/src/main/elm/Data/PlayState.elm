module Data.PlayState exposing (..)

import Json.Decode as Decode
import Util.Json

type PlayState
    = Play
    | Pause
    | Stop

encode: PlayState -> String
encode state =
    case state of
        Play -> "play"
        Pause -> "pause"
        Stop -> "stop"

decode: String -> Maybe PlayState
decode str =
    case (String.toLower str) of
        "play" -> Just Play
        "pause" -> Just Pause
        "stop" -> Just Stop
        _ -> Nothing

jsonDecode: Decode.Decoder PlayState
jsonDecode =
    Util.Json.decodeMaybe decode
