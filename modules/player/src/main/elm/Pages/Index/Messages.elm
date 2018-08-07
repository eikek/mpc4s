module Pages.Index.Messages exposing (Messages, getMessages)

type alias Messages =
    { title: String
    , headline: String
    , subhead: String
    , moreOnGithub: String
    , photosFrom: String
    , takenBy: String
    }

messagesDE: Messages
messagesDE =
    { title = "mpc4s"
    , headline = "mpc4s Player"
    , subhead = "Ein einfaches Frontent zu MPD"
    , moreOnGithub = "Mehr bei Github"
    , photosFrom = "Fotos von"
    , takenBy = "von"
    }

messagesEN: Messages
messagesEN =
    { title = "mpc4s"
    , headline = "mpc4s Player"
    , subhead = "A simple frontend to MPD"
    , moreOnGithub = "More on Github"
    , photosFrom = "Photos from"
    , takenBy = "taken by"
    }

getMessages: String -> Messages
getMessages lang =
    if lang == "de" then
        messagesDE
    else
        messagesEN
