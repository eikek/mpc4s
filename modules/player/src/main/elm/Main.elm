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
import Data.Flags exposing (..)

init: Flags -> Navigation.Location -> (Model, Cmd Msg)
init flags loc =
    let
        emptyModel = makeModel flags loc
        (m1, c1) = App.Update.update (UrlChange loc) emptyModel
        infoCmd = Requests.info m1.baseUrl InfoLoad
        model = {m1|baseUrl = flags.baseUrl}
    in
        model ! [c1, infoCmd, Ports.loadSettings ()]


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
