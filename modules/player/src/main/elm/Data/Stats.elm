module Data.Stats exposing (..)

import Json.Decode as Decode exposing (field)

type alias Stats =
    { artists: Int
    , albums: Int
    , songs: Int
    , uptime: Int
    , dbPlaytime: Int
    , dbUpdate: Int
    , playtime: Int
    }

empty: Stats
empty =
    { artists = 0
    , albums = 0
    , songs = 0
    , uptime = 0
    , dbPlaytime = 0
    , dbUpdate = 0
    , playtime = 0
    }

jsonDecode: Decode.Decoder Stats
jsonDecode =
    Decode.map7 Stats
        (field "artists" Decode.int)
        (field "albums" Decode.int)
        (field "songs" Decode.int)
        (field "uptime" Decode.int)
        (field "db_playtime" Decode.int)
        (field "db_update" Decode.int)
        (field "playtime" Decode.int)
