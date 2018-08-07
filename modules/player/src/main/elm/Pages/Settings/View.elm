module Pages.Settings.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onCheck)

import Pages.Settings.Messages exposing (..)
import Pages.Settings.Data exposing (..)
import Data.MpdCommand exposing (MpdCommand(..))
import Data.MpdConn exposing (MpdConn)
import Util.Maybe

view: Model -> Html Msg
view model =
    let
        msg = getMessages model.settings.lang
    in
    div [class "main-content"]
        [div [class "ui container"]
             [h1 [class "ui header"]
                  [msg.title |> text]
             ,div [class "ui middle aligned grid container"]
                 [(languageRow msg model)
                 ,(mpdSelectRow msg model)
                 ,(showVolumeRow msg model)
                 ,(volumeStepRow msg model)
                 ,(mpdDatabaseRow msg model)
                 ,(coverRow msg model)
                 ]
             ]
        ]


languageRow: Messages -> Model -> Html Msg
languageRow msg model =
    div [class "row"]
        [div [class "four wide column"]
             [div [class ""]
                  [msg.lang |> text
                  ]
             ]
        ,div [class "twelve wide column"]
            [div [class "ui selection dropdown"]
                 [model.settings.lang |> text
                 ,i [class "dropdown icon"][]
                 ,div [class "menu"]
                     (List.map languageMenuItem (otherLanguages model))
                 ]
            ]
        ]

languageMenuItem: String -> Html Msg
languageMenuItem lang =
    a [class "item", onClick (SetLanguage lang)]
        [text lang]

showVolumeRow: Messages -> Model -> Html Msg
showVolumeRow msg model =
    div [class "row"]
        [div [class "four wide column"]
             [div [class ""]
                  [msg.showVol |> text
                  ]
             ]
        ,div [class "twelve wide column"]
            [div [classList [("ui toggle checkbox", True)
                            ,("checked", model.settings.showVolume)
                            ]]
                 [input [type_ "checkbox"
                        ,onCheck (\_ -> ToggleShowVol)
                        ,checked model.settings.showVolume][]
                 ,label [][]
                 ]
            ]
        ]

mpdDatabaseRow: Messages -> Model -> Html Msg
mpdDatabaseRow msg model =
    div [class "row"]
        [div [class "four wide column"]
             [div [class ""]
                  [msg.database |> text
                  ]
             ]
        ,div [class "twelve wide column"]
            [button [classList [("ui button", True)
                               ,("disabled loading", Util.Maybe.isDefined model.status.updatingDb)
                               ]
                    ,onClick (RunCmd [Update])
                    ]
                 [text "MPD Update"
                 ]
            ,button [classList [("ui button", True)
                               ,("disabled loading", Util.Maybe.isDefined model.status.updatingDb)
                               ]
                    ,onClick (RunCmd [Rescan])
                    ]
                 [text "MPD Rescan"
                 ]
            ,div [classList [("nodisplay", Util.Maybe.isEmpty model.status.updatingDb)]
                 ]
                 [msg.dbUpdating |> text
                 ]
            ]
        ]

coverRow: Messages -> Model -> Html Msg
coverRow msg model =
    div [class "row"]
        [div [class "four wide column"]
             [div [class ""]
                  [msg.cover |> text
                  ]
             ]
        ,div [class "twelve wide column"]
            [button [class "ui button", onClick ClearCache]
                 [text "Clear Cover Cache"
                 ]
            ]
        ]

volumeStepRow: Messages -> Model -> Html Msg
volumeStepRow msg model =
    div [class "row"]
        [div [class "four wide column"]
             [div [class ""]
                  [msg.volumeStep |> text
                  ]
             ]
        ,div [class "twelve wide column"]
            [div [class "ui action input"]
                 [input [type_ "text"
                        ,model.settings.volumeStep |> toString |> value
                        ][]
                 ,button [class "ui button", onClick VolumeStepInc][text "+"]
                 ,button [class "ui button", onClick VolumeStepDec][text "-"]
                 ]
            ]
        ]

mpdSelectRow: Messages -> Model -> Html Msg
mpdSelectRow msg model =
    div [class "row"]
        [div [class "four wide column"]
             [div [class ""]
                  [msg.mpdConnections |> text
                  ]
             ]
        ,div [class "twelve wide column"]
            [div [class "ui selection dropdown"]
                 [model.settings.mpdConn.title |> text
                 ,i [class "dropdown icon"][]
                 ,div [class "menu"]
                     (List.filter (.id >> ((/=) model.settings.mpdConn.id)) model.mpdConns
                          |> List.map makeMpdItem)
                 ]
            ]
        ]


makeMpdItem: MpdConn -> Html Msg
makeMpdItem conn =
    a [class "item", onClick (SetMpdConn conn)]
        [text conn.title]
