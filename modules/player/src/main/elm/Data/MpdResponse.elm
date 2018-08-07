module Data.MpdResponse exposing (MpdResponse(..), Code, Ack, decoder, decodeString)

import Json.Decode as Decode exposing (field)

import Data.Answer exposing (..)

type MpdResponse
    = Result Answer
    | Error Ack

type alias Code =
    { value: Int
    , name: String
    }

type alias Ack =
    { code: Code
    , position: Int
    , command: String
    , message: String
    }

decoder: Decode.Decoder MpdResponse
decoder =
    Decode.andThen decodeResponse decodeMeta

decodeString: String -> Result String MpdResponse
decodeString str =
    Decode.decodeString decoder str


{-- Internal --}

type alias ResponseMeta =
    { success: Bool
    , kind: String
    }

decodeMeta: Decode.Decoder ResponseMeta
decodeMeta =
    Decode.map2 ResponseMeta
        (field "success" Decode.bool)
        (field "type" Decode.string)

decodeResponse: ResponseMeta -> Decode.Decoder MpdResponse
decodeResponse meta =
    if (meta.success) then Decode.map Result (answerDecoder meta.kind)
    else decodeError

decodeError: Decode.Decoder MpdResponse
decodeError =
    Decode.map Error (field "ack" decodeAck)

decodeAck: Decode.Decoder Ack
decodeAck =
    Decode.map4 Ack
        (field "code" decodeCode)
        (field "position" Decode.int)
        (field "command" Decode.string)
        (field "message" Decode.string)

decodeCode: Decode.Decoder Code
decodeCode =
    Decode.map2 Code
        (field "value" Decode.int)
        (field "name" Decode.string)
