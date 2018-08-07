module Data.SingleState exposing (..)

import Json.Decode as Decode
import Util.Json

type SingleState
    = On
    | Off
    | Oneshot

encode: SingleState -> String
encode state =
    case state of
        On -> "1"
        Off -> "0"
        Oneshot -> "oneshot"

decode: String -> Maybe SingleState
decode str =
    case (String.toLower str) of
        "1" -> Just On
        "0" -> Just Off
        "oneshot" -> Just Oneshot
        _ -> Nothing

jsonDecode: Decode.Decoder SingleState
jsonDecode =
    Util.Json.decodeMaybe decode
