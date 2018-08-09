module Pages.Library.Messages exposing (Messages, getMessages)

import Data.Tag exposing (Tag(..))
import Util.String

type alias Messages =
    { title: String
    , songs: String
    , albums: String
    , forTag: Tag -> String
    , noName: String
    , discs: String
    , disc: String
    , tracks: String
    , added: String
    , clearPlAndPlayAll: String
    , appendAll: String
    , insertAll: String
    , time: String
    , currentPlaylist: String
    }

messagesDE: Messages
messagesDE =
    { title = "mpc4s"
    , songs = "Titel"
    , albums = "Alben"
    , forTag = tagStringDE
    , noName = "<Kein Name>"
    , discs = "CD(s)"
    , disc = "CD"
    , tracks = "Titel"
    , added = "Hinzugefügt"
    , clearPlAndPlayAll = "Leeren und alle abspielen"
    , appendAll = "Alle Titel anhängen"
    , insertAll = "Alle Titel einfügen"
    , time = "Zeit"
    , currentPlaylist = "Aktuelle Playlist"
    }

messagesEN: Messages
messagesEN =
    { title = "mpc4s"
    , songs = "Songs"
    , albums = "Albums"
    , forTag = tagStringEN
    , noName = "<No Name>"
    , discs = "Discs(s)"
    , disc = "Disc"
    , tracks = "Track(s)"
    , added = "Added"
    , clearPlAndPlayAll = "Clear playlist and play all"
    , appendAll = "Append all tracks"
    , insertAll = "Insert all tracks"
    , time = "Time"
    , currentPlaylist = "Current Playlist"
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN

tagStringDE: Tag -> String
tagStringDE tag =
    case tag of
        Composer -> "Komponist"
        Album -> "Album"
        Artist -> "Künstler"
        Albumartist -> "Künstler"
        Genre -> "Genre"
        Title -> "Titel"

        _ -> Data.Tag.encode tag |> Util.String.firstUpper

tagStringEN: Tag -> String
tagStringEN tag =
    case tag of
        Composer -> "Composer"
        Albumartist -> "Artist"

        _ -> Data.Tag.encode tag |> Util.String.firstUpper
