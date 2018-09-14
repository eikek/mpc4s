module Pages.Search.Messages exposing (Messages, getMessages)

import Pages.Library.Messages as LM
import Data.Tag exposing (Tag)

type alias Messages =
    { forTag: Tag -> String
    , time: String
    , currentPlaylist: String
    , songView: String
    , albumView: String
    , search: String
    , smallCovers: String
    }

messagesDE: Messages
messagesDE =
    let
        lm = LM.getMessages "de"
    in
    { forTag = lm.forTag
    , time = "Zeit"
    , currentPlaylist = lm.currentPlaylist
    , songView = "Tracks"
    , albumView = "Alben"
    , search = "Suche ..."
    , smallCovers = lm.smallCovers
    }

messagesEN: Messages
messagesEN =
    let
        lm = LM.getMessages "en"
    in
    { forTag = lm.forTag
    , time = "Time"
    , currentPlaylist = lm.currentPlaylist
    , songView = "Tracks"
    , albumView = "Albums"
    , search = "Search ..."
    , smallCovers = lm.smallCovers
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN
