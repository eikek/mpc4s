module Util.Json exposing (..)

import Json.Decode as Decode
import Util.Maybe
import Util.String

decodeMaybe: (String -> Maybe a) -> Decode.Decoder a
decodeMaybe dec =
    Decode.andThen jsonDecodeMaybe (Decode.map dec Decode.string)


jsonDecodeMaybe: Maybe a -> Decode.Decoder a
jsonDecodeMaybe ms =
    case ms of
        Just a -> Decode.succeed a
        Nothing -> Decode.fail <|
                   "Cannot decode into constant"

decodeMaybeString: Decode.Decoder (Maybe String)
decodeMaybeString =
    Decode.map (Util.Maybe.filter Util.String.nonEmpty) (Decode.maybe Decode.string)
