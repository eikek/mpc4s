module Data.CoverUrls exposing (..)

type alias CoverUrls =
    { forAlbum: String -> String
    , forFile: String -> String
    }
