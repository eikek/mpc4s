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
    , playElsewhere: String
    , enabled: String
    , timeOffset: String
    , searchPage: String
    , searchLimit: String
    , searchView: String
    , searchAlbumView: String
    , searchSongView: String
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
    , playElsewhere = "Woanders abspielen"
    , enabled = "Aktiviert"
    , timeOffset = "Sekunden Offset"
    , searchPage = "Suche"
    , searchLimit = "Limit"
    , searchView = "Ansicht"
    , searchAlbumView = "Alben"
    , searchSongView = "Tracks"
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
    , playElsewhere = "Play elsewhere"
    , enabled = "Enabled"
    , timeOffset = "Seconds offset"
    , searchPage = "Search"
    , searchLimit = "Limit"
    , searchView = "View"
    , searchAlbumView = "Albums"
    , searchSongView = "Tracks"
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN
