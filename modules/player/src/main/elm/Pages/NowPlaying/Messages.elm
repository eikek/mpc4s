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
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN
