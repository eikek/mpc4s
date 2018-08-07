module App.Data exposing (..)

import Time exposing (Time)
import Http
import Html exposing (Html)
import Navigation

import Route exposing (Route(..))
import Data.MpdCommand exposing (MpdCommand)
import Data.Status exposing (Status)
import Data.Settings exposing (Settings)
import Data.MpdConn exposing (MpdConn)
import Pages.Index.Data
import Pages.Library.Data
import Pages.NowPlaying.Data
import Pages.Settings.Data

type alias Model =
    { location: Navigation.Location
    , page: Route
    , baseUrl: String
    , deferredCmds: List (Cmd Msg)
    , status: Status
    , indexModel: Pages.Index.Data.Model
    , nowPlayingModel: Pages.NowPlaying.Data.Model
    , libraryModel: Pages.Library.Data.Model
    , settingsModel: Pages.Settings.Data.Model
    }

type alias Scroll =
    { left: Float
    , top: Float
    }

makeModel: String -> List MpdConn -> Navigation.Location -> Model
makeModel baseurl conns location =
    { location = location
    , page = IndexPage
    , baseUrl = ""
    , deferredCmds = []
    , status = Data.Status.empty
    , indexModel = Pages.Index.Data.emptyModel
    , nowPlayingModel = Pages.NowPlaying.Data.emptyModel
    , libraryModel = Pages.Library.Data.emptyModel
    , settingsModel = Pages.Settings.Data.makeModel baseurl conns
    }

getLanguage: Model -> String
getLanguage model =
    model.settingsModel.settings.lang

type Msg
    = UrlChange Navigation.Location
    | DeferredTick Time
    | ProcessItemWS String
    | SwitchPage Route
    | RunCommand MpdCommand
    | IndexMsg Pages.Index.Data.Msg
    | LibraryMsg Pages.Library.Data.Msg
    | NowPlayingMsg Pages.NowPlaying.Data.Msg
    | SettingsMsg Pages.Settings.Data.Msg
    | CurrentScroll Scroll
    | Tick Time
    | SeekClick Float
    | SettingsLoad Settings
