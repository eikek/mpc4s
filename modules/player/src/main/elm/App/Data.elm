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
import Data.Flags exposing (Flags)
import Data.Info exposing (Info)
import Pages.Index.Data
import Pages.Library.Data
import Pages.NowPlaying.Data
import Pages.Settings.Data
import Pages.Playlists.Data
import Pages.Search.Data

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
    , playlistsModel: Pages.Playlists.Data.Model
    , searchModel: Pages.Search.Data.Model
    }

type alias Scroll =
    { left: Float
    , top: Float
    }

makeModel: Flags -> Navigation.Location -> Model
makeModel flags location =
    { location = location
    , page = IndexPage
    , baseUrl = flags.baseUrl
    , deferredCmds = []
    , status = Data.Status.empty
    , indexModel = Pages.Index.Data.emptyModel
    , nowPlayingModel = Pages.NowPlaying.Data.makeModel flags.baseUrl
    , libraryModel = Pages.Library.Data.emptyModel
    , settingsModel = Pages.Settings.Data.makeModel flags.baseUrl
    , playlistsModel = Pages.Playlists.Data.empty
    , searchModel = Pages.Search.Data.empty
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
    | PlaylistsMsg Pages.Playlists.Data.Msg
    | SearchMsg Pages.Search.Data.Msg
    | CurrentScroll Scroll
    | Tick Time
    | SeekClick Float
    | SettingsLoad Settings
    | InfoLoad (Result Http.Error Info)
    | SetMpdConn MpdConn
