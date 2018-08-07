module Main exposing (..)

import Navigation
import AnimationFrame
import WebSocket
import Time

import Ports
import Requests
import Route exposing (Route(..), setPage)
import Data.PlayState
import Data.MpdConn exposing (MpdConn)
import Pages.Index.Data
import App.Data exposing (..)
import App.Update
import App.View

type alias Flags =
    { baseUrl: String
    , mpdConns: List MpdConn
    }

init: Flags -> Navigation.Location -> (Model, Cmd Msg)
init flags loc =
    let
        emptyModel = makeModel flags.baseUrl flags.mpdConns loc
        (m1, c1) = App.Update.update (UrlChange loc) emptyModel
        infoCmd = Requests.info m1.baseUrl Pages.Index.Data.ReceiveInfo
        model = {m1|baseUrl = flags.baseUrl}
    in
        model ! [c1, Cmd.map IndexMsg infoCmd, Ports.loadSettings ()]


subscriptions: Model -> Sub Msg
subscriptions model =
    let
        conn = model.settingsModel.settings.mpdConn
        wsurl = Requests.baseUrlToWs conn model.baseUrl
    in
    Sub.batch
        [ if model.deferredCmds == [] then Sub.none else AnimationFrame.times DeferredTick
        , WebSocket.listen wsurl ProcessItemWS
        , Ports.currentScroll CurrentScroll
        , Ports.seekClick SeekClick
        , Ports.receiveSettings SettingsLoad
        , if model.status.state == Data.PlayState.Play then Time.every (1 * Time.second) Tick else Sub.none
        ]


main =
    Navigation.programWithFlags UrlChange
        { init = init
        , subscriptions = subscriptions
        , view = App.View.view
        , update = App.Update.update
        }
