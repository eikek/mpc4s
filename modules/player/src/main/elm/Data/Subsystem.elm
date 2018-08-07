module Data.Subsystem exposing (..)

import Json.Decode as Decode
import Util.Json exposing (..)

type Subsystem
    = Database
    | Update
    | StoredPlaylist
    | Playlist
    | Mixer
    | Output
    | Options
    | Partition
    | Sticker
    | Player
    | Message

encode: Subsystem -> String
encode s =
    case s of
        Database -> "database"
        Update -> "update"
        StoredPlaylist -> "storedplaylist"
        Playlist -> "playlist"
        Mixer -> "mixer"
        Output -> "output"
        Options -> "options"
        Partition -> "partition"
        Sticker -> "sticker"
        Player -> "player"
        Message -> "message"


decode: String -> Maybe Subsystem
decode str =
    case (String.toLower str) of
        "database" -> Just Database
        "update" -> Just Update
        "storedplaylist" -> Just StoredPlaylist
        "playlist" -> Just Playlist
        "mixer" -> Just Mixer
        "output" -> Just Output
        "options" -> Just Options
        "partition" -> Just Partition
        "sticker" -> Just Sticker
        "player" -> Just Player
        "message" -> Just Message
        _ -> Nothing

jsonDecode: Decode.Decoder Subsystem
jsonDecode =
    Util.Json.decodeMaybe decode

member: Subsystem -> List Subsystem -> Bool
member s list =
    List.member s list

memberAny: List Subsystem -> List Subsystem -> Bool
memberAny test all =
    List.foldl (\s -> \b -> b || (member s all)) False test
