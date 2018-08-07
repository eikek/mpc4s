module Pages.Settings.Messages exposing (Messages, getMessages)

type alias Messages =
    { title: String
    , lang: String
    , showVol: String
    , database: String
    , cover: String
    , dbUpdating: String
    , volumeStep: String
    , mpdConnections: String
    }

messagesDE: Messages
messagesDE =
    { title = "Einstellungen"
    , lang = "Sprache"
    , showVol = "Lautstärke Regler einblenden"
    , database = "Media Datenbank"
    , cover = "Cover"
    , dbUpdating = "MPD's media Datenbank wird momentan aktualisiert."
    , volumeStep = "Lautstärke Schritte"
    , mpdConnections = "MPD Verbindungen"
    }

messagesEN: Messages
messagesEN =
    { title = "Settings"
    , lang = "Language"
    , showVol = "Show volume controls"
    , database = "Media database"
    , cover = "Cover art"
    , dbUpdating = "MPD's media database is currently updating."
    , volumeStep = "Volume step"
    , mpdConnections = "MPD connections"
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN
