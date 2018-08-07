module Data.Answer exposing (Answer(..), answerDecoder)

import Json.Decode as Decode
import Data.Subsystem exposing (..)
import Data.TagValue exposing (TagValue)
import Data.Status exposing (Status)
import Data.SongCount exposing (SongCount)
import Data.Stats exposing (Stats)
import Data.Song exposing (Song)
import Data.PlaylistSong exposing (PlaylistSong)

type Answer
    = Empty
    | Idle (List Subsystem)
    | TagVals (List TagValue)
    | StatusInfo Status
    | SongCountInfo (List SongCount)
    | StatsInfo Stats
    | Songs (List Song)
    | Playlist (List PlaylistSong)
    | CurrentSongInfo PlaylistSong
    | AddIdAnswer String
    | JobIdAnswer String


answerDecoder: String -> Decode.Decoder Answer
answerDecoder kind =
    case kind of
        "Empty" ->
            Decode.succeed Empty

        "IdleAnswer" ->
            Decode.at ["result", "changes"] <|
                Decode.map Idle decodeSubsystems

        "ListAnswer" ->
            Decode.at ["result", "tags"] <|
                Decode.map TagVals decodeTagValues

        "StatusAnswer" ->
            Decode.at ["result"] <|
                Decode.map StatusInfo Data.Status.jsonDecode

        "SongCountAnswer" ->
            Decode.at ["result", "songCounts"] <|
                Decode.map SongCountInfo decodeSongCounts

        "StatsAnswer" ->
            Decode.at ["result"] <|
                Decode.map StatsInfo Data.Stats.jsonDecode

        "SongListAnswer" ->
            Decode.at ["result", "songs"] <|
                Decode.map Songs decodeSongs

        "PlaylistAnswer" ->
            Decode.at ["result", "songs"] <|
                Decode.map Playlist decodePlaylist

        "CurrentSongAnswer" ->
            Decode.at ["result", "song"] <|
                Decode.map CurrentSongInfo Data.PlaylistSong.jsonDecode

        "AddIdAnswer" ->
            Decode.at ["result", "id" ] <|
                Decode.map AddIdAnswer Decode.string

        "JobIdAnswer" ->
            Decode.at ["result", "jobId"] <|
                Decode.map JobIdAnswer Decode.string

        _ -> Decode.fail <|
               "No decoder for answer: " ++ kind


decodeSubsystems: Decode.Decoder (List Subsystem)
decodeSubsystems =
    Decode.list Data.Subsystem.jsonDecode

decodeTagValues: Decode.Decoder (List TagValue)
decodeTagValues =
    Decode.list Data.TagValue.jsonDecode

decodeSongCounts: Decode.Decoder (List SongCount)
decodeSongCounts =
    Decode.list Data.SongCount.jsonDecode

decodeSongs: Decode.Decoder (List Song)
decodeSongs =
    Decode.list Data.Song.jsonDecode

decodePlaylist: Decode.Decoder (List PlaylistSong)
decodePlaylist =
    Decode.list Data.PlaylistSong.jsonDecode
