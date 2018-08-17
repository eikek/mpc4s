module Pages.Index.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Pages.Index.Data exposing (..)
import Util.Time
import Data.Info exposing (Info)
import Data.MpdConn exposing (MpdConn)
import Pages.Index.Messages exposing (..)

view: String -> Model -> Html Msg
view lang model =
    let
        msg = getMessages lang
    in
    div[class "main-content index-page"]
        [div [class "ui text container"]
             [div [class "ui very padded center aligned basic segment"]
                  [h1 [class "ui header"]
                      [text msg.headline
                      ]
                  ]
             ,div [class "ui very padded center aligned basic red segment"]
                  [em [][text msg.subhead
                       ]
                  ]
             ,div [class "ui very padded center aligned basic red segment"]
                  [a [class "basic primary large ui button"
                     , href "https://github.com/eikek/mpc4s"
                     ]
                     [i [class "ui github icon"][]
                     ,text msg.moreOnGithub
                     ]
                  ]
             ,div [class "ui padded center aligned mini basic red segment"]
                  ([Maybe.map makeInfoLabel model.info |> Maybe.withDefault (div [][])
                  ,text msg.photosFrom
                  ,text " "
                  ,a [href unsplashSite][text "unsplash.com"]
                  ,text (" " ++ msg.takenBy)
                  ] ++ unsplashCredits)
             ]
        ]

makeInfoLabel: Info -> Html Msg
makeInfoLabel info =
    let
        commit = String.slice 0 8 info.gitCommit
        built = Util.Time.formatDateTime info.builtAtMillis
    in
    div [class ""]
        [text "v"
        ,info.version |> text
        ,text " • #"
        ,commit |> text
        ,text " • "
        ,built |> text
        ,text " • MPD "
        ,makeMpdInfo info.mpd |> text
        ]

makeMpdInfo: List MpdConn -> String
makeMpdInfo conns =
    List.map (\a -> a.host ++ ":" ++ (toString a.port_)) conns
        |> List.intersperse ", "
        |> List.foldr String.append ""

unsplashAuthors: List String
unsplashAuthors =
    [ "@jamomca"
    , "@jplenio"
    ]

unsplashSite: String
unsplashSite =
    "https://unsplash.com"

unsplashCredits: List (Html Msg)
unsplashCredits =
    List.map
        (\name -> a [href (unsplashSite ++ "/" ++ name)][text name])
        unsplashAuthors
        |> List.intersperse (text ", ")
