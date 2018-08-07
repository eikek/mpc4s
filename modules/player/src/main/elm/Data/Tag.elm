module Data.Tag exposing (..)

import Json.Decode as Decode
import Util.Json

type Tag
    = Disc
    | Date
    | Name
    | Genre
    | Track
    | Title
    | Album
    | Artist
    | Comment
    | Composer
    | Performer
    | Albumsort
    | Artistsort
    | Albumartist
    | Albumartistsort
    | MusicbrainzWorkid
    | MusicbrainzTrackid
    | MusicbrainzAlbumid
    | MusicbrainzArtistid
    | MusicbrainzAlbumartistid
    | MusicbrainzReleasetrackid
    | File

encode: Tag -> String
encode tag =
    case tag of
        Disc -> "disc"
        Date -> "date"
        Name -> "name"
        Genre -> "genre"
        Track -> "track"
        Title -> "title"
        Album -> "album"
        Artist -> "artist"
        Comment -> "comment"
        Composer -> "composer"
        Performer -> "performer"
        Albumsort -> "albumsort"
        Artistsort -> "artistsort"
        Albumartist -> "albumartist"
        Albumartistsort ->"albumartistsort"
        MusicbrainzWorkid ->"musicbrainzworkid"
        MusicbrainzTrackid ->"musicbrainztrackid"
        MusicbrainzAlbumid ->"musicbrainzalbumid"
        MusicbrainzArtistid ->"musicbrainzartistid"
        MusicbrainzAlbumartistid ->"musicbrainzalbumartistid"
        MusicbrainzReleasetrackid ->"musicbrainzreleasetrackid"
        File -> "file"

decode: String -> Maybe Tag
decode str =
    case (String.toLower str) of
        "disc" -> Just Disc
        "date" -> Just Date
        "name" -> Just Name
        "genre" -> Just Genre
        "track" -> Just Track
        "title" -> Just Title
        "album" -> Just Album
        "artist" -> Just Artist
        "comment" -> Just Comment
        "composer" -> Just Composer
        "performer" -> Just Performer
        "albumsort" -> Just Albumsort
        "artistsort" -> Just Artistsort
        "albumartist" -> Just Albumartist
        "albumartistsort" -> Just Albumartistsort
        "musicbrainzworkid" -> Just MusicbrainzWorkid
        "musicbrainztrackid" -> Just MusicbrainzTrackid
        "musicbrainzalbumid" -> Just MusicbrainzAlbumid
        "musicbrainzartistid" -> Just MusicbrainzArtistid
        "musicbrainzalbumartistid" -> Just MusicbrainzAlbumartistid
        "musicbrainzreleasetrackid" -> Just MusicbrainzReleasetrackid
        "file" -> Just File
        _ -> Nothing

jsonDecode: Decode.Decoder Tag
jsonDecode =
    Util.Json.decodeMaybe decode

all: List Tag
all =
    [ Disc
    , Date
    , Name
    , Genre
    , Track
    , Title
    , Album
    , Artist
    , Comment
    , Composer
    , Performer
    , Albumsort
    , Artistsort
    , Albumartist
    , Albumartistsort
    , MusicbrainzWorkid
    , MusicbrainzTrackid
    , MusicbrainzAlbumid
    , MusicbrainzArtistid
    , MusicbrainzAlbumartistid
    , MusicbrainzReleasetrackid
    , File
    ]
