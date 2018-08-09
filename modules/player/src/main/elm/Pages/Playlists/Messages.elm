module Pages.Playlists.Messages exposing (Messages, getMessages)

import Pages.Library.Messages as LM
import Data.Tag exposing (Tag)

type alias Messages =
    { headline: String
    , forTag: Tag -> String
    , time: String
    , songs: String
    , name: String
    , lastMod: String
    , rename: String
    , loadPlaylistTitle: String
    , clearAndPlay: String
    , removePlaylist: String
    , renamePlaylist: String
    , addFileToPlaylist: String
    , add: String
    , addPlaylist: String
    }

messagesDE: Messages
messagesDE =
    let
        lm = LM.getMessages "de"
    in
    { headline = "Gespeicherte Playlists"
    , forTag = lm.forTag
    , time = "Zeit"
    , songs = "Titel"
    , name = "Name"
    , lastMod = "Letzte Änderung"
    , rename = "Umbenennen"
    , loadPlaylistTitle = "Playlist an aktuelle anghängen"
    , clearAndPlay = "Diese Liste abspielen, aktuelle ersetzen"
    , removePlaylist = "Playliste entfernen"
    , renamePlaylist = "Playliste umbenennen"
    , addFileToPlaylist = "Datei/Uri hinzufügen"
    , add = "Hinzufügen"
    , addPlaylist = "Neue playlist erzeugen"
    }

messagesEN: Messages
messagesEN =
    let
        lm = LM.getMessages "de"
    in
    { headline = "Stored Playlists"
    , forTag = lm.forTag
    , time = "Time"
    , songs = "Tracks"
    , name = "Name"
    , lastMod = "Last Modified"
    , rename = "Rename"
    , loadPlaylistTitle = "Append this to the current playlist"
    , clearAndPlay = "Play this playlist, clearing current"
    , removePlaylist = "Delete this playlist"
    , renamePlaylist = "Rename playlist"
    , addFileToPlaylist = "Add a file or uri"
    , add = "Add"
    , addPlaylist = "Add new empty playlist"
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN
