module Pages.NowPlaying.Messages exposing (Messages, getMessages)

import Pages.Library.Messages as LM
import Data.Tag exposing (Tag)

type alias Messages =
    { title: String
    , noTitle: String
    , noArtist: String
    , noAlbum: String
    , items: String
    , total: String
    , played: String
    , remaining: String
    , forTag: Tag -> String
    , disc: String
    , ends: String
    , add: String
    , save: String
    , time: String
    , playElsewhere: String
    , addToPlaylist: String
    , viewBooklet: String
    }

messagesDE: Messages
messagesDE =
    let
        lm = LM.getMessages "de"
    in
    { title = "mpc4s"
    , noTitle = "<Kein Titel>"
    , noArtist = "<Kein Künstler>"
    , noAlbum = "<Kein Album>"
    , items = "Items"
    , total = "Total"
    , played = "Abgespielt"
    , remaining = "Übrig"
    , forTag = lm.forTag
    , disc = lm.disc
    , ends = "Endet"
    , add = "Hinzufügen"
    , save = "Speichern"
    , time = "Zeit"
    , playElsewhere = "Woanders abspielen"
    , addToPlaylist = "Zur Playlist hinzufügen"
    , viewBooklet = lm.viewBooklet
    }

messagesEN: Messages
messagesEN =
    let
        lm = LM.getMessages "en"
    in
    { title = "mpc4s"
    , noTitle = "<No Title>"
    , noArtist = "<No Artist>"
    , noAlbum = "<No Album>"
    , items = "Items"
    , total = "Total"
    , played = "Played"
    , remaining = "Remaining"
    , forTag = lm.forTag
    , disc = lm.disc
    , ends = "Ends"
    , add = "Add"
    , save = "Save"
    , time = "Time"
    , playElsewhere = "Play elsewhere"
    , addToPlaylist = "Add to playlist"
    , viewBooklet = lm.viewBooklet
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN
