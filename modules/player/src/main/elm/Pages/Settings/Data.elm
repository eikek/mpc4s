module Pages.Settings.Data exposing (..)

import Http
import Data.Settings exposing (Settings)
import Data.Status exposing (Status)
import Data.MpdCommand exposing (MpdCommand)
import Data.Answer exposing (Answer)
import Data.MpdConn exposing (MpdConn)

type alias Model =
    { settings: Settings
    , status: Status
    , baseurl: String
    , mpdConns: List MpdConn
    }

empty: Model
empty =
    { settings = Data.Settings.empty
    , status = Data.Status.empty
    , baseurl = ""
    , mpdConns = []
    }

makeModel: String -> List MpdConn -> Model
makeModel baseurl conns =
    {empty|baseurl = baseurl, mpdConns = conns}

allLanguages: List String
allLanguages = ["de", "en"]

otherLanguages: Model -> List String
otherLanguages model =
    let
        current = model.settings.lang
    in
        if current == "de" then
            ["en"]
        else
            ["de"]

type Msg
    = SetLanguage String
    | ToggleShowVol
    | ReceiveSettings Settings
    | HandleAnswer Answer
    | RunCmd (List MpdCommand)
    | ClearCache
    | ClearCacheResult (Result Http.Error ())
    | VolumeStepInc
    | VolumeStepDec
    | SetMpdConn MpdConn
