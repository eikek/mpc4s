module App.Messages exposing (Messages, getMessages, getTitle)

import Route exposing (Route(..))

type alias Messages =
    { indexTitle: String
    , libraryTitle: String
    , nowPlayingTitle: String
    , settingsTitle: String
    , playlistsTitle: String
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN

messagesDE: Messages
messagesDE =
    { indexTitle = "Index"
    , libraryTitle = "Bibliothek"
    , nowPlayingTitle = "Gerade"
    , settingsTitle = "Einstellungen"
    , playlistsTitle = "Playlists"
    }

messagesEN: Messages
messagesEN =
    { indexTitle = "Index"
    , libraryTitle = "Library"
    , nowPlayingTitle = "Now Playing"
    , settingsTitle = "Settings"
    , playlistsTitle = "Playlists"
    }

getTitle: String -> Route -> String
getTitle lang page =
    let
        msg = getMessages lang
    in case page of
        IndexPage -> msg.indexTitle
        LibraryPage _ _ -> msg.libraryTitle
        NowPlayingPage -> msg.nowPlayingTitle
        SettingsPage -> msg.settingsTitle
        PlaylistsPage _ -> msg.playlistsTitle
