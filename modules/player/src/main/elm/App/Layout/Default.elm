module App.Layout.Default exposing (render)

import App.Data exposing (..)
import App.Messages exposing (getTitle)
import Data.MpdCommand exposing (MpdCommand(..))
import Data.MpdConn exposing (MpdConn)
import Data.PlayState
import Data.Status
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Route exposing (Route(..))


render : Model -> Html Msg -> Html Msg
render model inner =
    div [ class "default-layout" ]
        [ headerMenu model
        , inner
        ]


headerMenu : Model -> Html Msg
headerMenu model =
    let
        currMpd =
            model.settingsModel.settings.mpdConn

        nums =
            List.length model.settingsModel.mpdConns
    in
    div [ class "ui fixed top sticky attached inverted large menu black-bg" ]
        [ div [ class "ui fluid container" ]
            [ a [ class "header item narrow-item", onClick (SwitchPage IndexPage) ]
                [ i
                    [ classList
                        [ ( "large music icon", True )
                        ]
                    ]
                    []
                ]
            , div
                [ classList
                    [ ( "ui dropdown item mpd-menu narrow-item", True )
                    , ( "nodisplay", nums <= 1 )
                    ]
                ]
                [ div
                    [ classList
                        [ ( "ui small red label", True )
                        , ( "nodisplay", nums <= 1 )
                        ]
                    ]
                    [ text currMpd.title ]
                , div [ class "menu" ]
                    (List.filter (.id >> (/=) currMpd.id) model.settingsModel.mpdConns
                        |> List.map selectMpdItem
                    )
                ]
            , createLibraryLink model
            , createNowPlayingLink model
            , createPlaylistsLink model
            , createSearchLink model
            , div [ class "right menu" ]
                (playControlMenu model ++ volumeControlMenu model ++ [ settingsMenu model ])
            ]
        ]


selectMpdItem : MpdConn -> Html Msg
selectMpdItem conn =
    a [ class "item", onClick (SetMpdConn conn) ]
        [ text conn.title ]


settingsMenu : Model -> Html Msg
settingsMenu model =
    a [ class "item", onClick (SwitchPage SettingsPage) ]
        [ i [ class "ui settings icon" ] []
        ]


createLibraryLink : Model -> Html Msg
createLibraryLink model =
    let
        lang =
            getLanguage model
    in
    a
        [ class (activePage2 isLibraryPage model)
        , onClick (SwitchPage (LibraryPage Nothing model.libraryModel.filter))
        , getTitle lang (LibraryPage Nothing model.libraryModel.filter) |> title
        ]
        [ i [ class "book icon" ] []
        ]


createNowPlayingLink : Model -> Html Msg
createNowPlayingLink model =
    let
        lang =
            getLanguage model
    in
    a
        [ class (activePage NowPlayingPage model)
        , onClick (SwitchPage NowPlayingPage)
        , getTitle lang NowPlayingPage |> title
        ]
        [ i [ class "headphones icon" ] []
        ]


createPlaylistsLink : Model -> Html Msg
createPlaylistsLink model =
    let
        lang =
            getLanguage model
    in
    a
        [ class (activePage2 isPlaylistsPage model)
        , onClick (SwitchPage (PlaylistsPage Nothing))
        , getTitle lang (PlaylistsPage Nothing) |> title
        ]
        [ i [ class "list alternate outline icon" ] []
        ]


createSearchLink : Model -> Html Msg
createSearchLink model =
    a [ class (activePage2 isSearchPage model), onClick (SwitchPage (SearchPage Nothing Nothing)) ]
        [ i [ class "ui search icon" ] []
        ]


playControlMenu : Model -> List (Html Msg)
playControlMenu model =
    [ a
        [ classList
            [ ( "active red", model.status.state == Data.PlayState.Play )
            , ( "item", True )
            ]
        , onClick (RunCommand (Play Nothing))
        ]
        [ i [ class "ui play icon" ] []
        ]
    , a
        [ classList
            [ ( "active red", model.status.state == Data.PlayState.Pause )
            , ( "item", True )
            ]
        , onClick (RunCommand (Pause True))
        ]
        [ i [ class "ui pause icon" ] []
        ]
    , a
        [ classList
            [ ( "active red", model.status.state == Data.PlayState.Stop )
            , ( "item", True )
            ]
        , onClick (RunCommand Stop)
        ]
        [ i [ class "ui stop icon" ] []
        ]
    ]


volumeControlMenu : Model -> List (Html Msg)
volumeControlMenu model =
    let
        step =
            model.settingsModel.settings.volumeStep
    in
    [ a
        [ classList
            [ ( "nodisplay", not model.settingsModel.settings.showVolume )
            , ( "item", True )
            ]
        , onClick (RunCommand (SetVol (Data.Status.volumeStep model.status -step)))
        ]
        [ i [ class "ui volume down icon" ] []
        ]
    , a
        [ classList
            [ ( "nodisplay", not model.settingsModel.settings.showVolume )
            , ( "item", True )
            ]
        ]
        [ span [] [ model.status.volume |> toString |> text ]
        ]
    , a
        [ classList
            [ ( "nodisplay", not model.settingsModel.settings.showVolume )
            , ( "item", True )
            ]
        , onClick (RunCommand (SetVol (Data.Status.volumeStep model.status step)))
        ]
        [ i [ class "ui volume up icon" ] []
        ]
    ]


activePage : Route -> Model -> String
activePage page model =
    activePage2 (\p -> p == page) model


activePage2 : (Route -> Bool) -> Model -> String
activePage2 rf model =
    if rf model.page then
        "active item"

    else
        "item"


isLibraryPage : Route -> Bool
isLibraryPage route =
    case route of
        LibraryPage _ _ ->
            True

        _ ->
            False


isPlaylistsPage : Route -> Bool
isPlaylistsPage route =
    case route of
        PlaylistsPage _ ->
            True

        _ ->
            False


isSearchPage : Route -> Bool
isSearchPage route =
    case route of
        SearchPage _ _ ->
            True

        _ ->
            False
