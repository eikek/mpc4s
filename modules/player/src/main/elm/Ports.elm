port module Ports exposing (..)

import App.Data exposing (Scroll)
import Data.Settings exposing (Settings)

port initElements: () -> Cmd msg
port setTitle: String -> Cmd msg
port updateProgress: (String, {start: Int, end: Int}) -> Cmd msg

port getScroll: () -> Cmd msg
port scrollTo: Scroll -> Cmd msg
port currentScroll: (Scroll -> msg) -> Sub msg

port seekClick: (Float -> msg) -> Sub msg

port storeSettings: Settings -> Cmd msg
port loadSettings: () -> Cmd msg
port receiveSettings: (Settings -> msg) -> Sub msg
