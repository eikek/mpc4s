module App.View exposing (view)

import Html exposing (Html, div, h1, text)
import App.Data exposing (..)
import Route exposing (..)
import Requests

import Util.Maybe
import Data.CoverUrls exposing (..)
import Pages.Index.View
import Pages.Library.View
import Pages.NowPlaying.View
import Pages.Settings.View
import Pages.Playlists.View
import Pages.Search.View
import App.Layout.Default exposing (render)

view: Model -> Html Msg
view model =
    let
        settings = model.settingsModel.settings
        conn = settings.mpdConn
        lang = getLanguage model
        covers = CoverUrls
                   (Requests.albumCoverUrl conn model.baseUrl (Just settings.thumbnailSize))
                   (Requests.fileCoverUrl conn model.baseUrl (Just settings.thumbnailSize))
                   (Requests.albumCoverUrl conn model.baseUrl Nothing)
                   (Requests.fileCoverUrl conn model.baseUrl Nothing)
    in
    case model.page of
        IndexPage ->
            Html.map IndexMsg (Pages.Index.View.view lang model.indexModel)
                |> App.Layout.Default.render model

        LibraryPage ma mf ->
            Html.map LibraryMsg (Pages.Library.View.view covers settings lang model.libraryModel)
                |> App.Layout.Default.render model

        NowPlayingPage ->
            Html.map NowPlayingMsg (Pages.NowPlaying.View.view covers settings lang model.nowPlayingModel)
                |> App.Layout.Default.render model

        SettingsPage ->
            Html.map SettingsMsg (Pages.Settings.View.view model.settingsModel)
                |> App.Layout.Default.render model

        PlaylistsPage mname ->
            Html.map PlaylistsMsg (Pages.Playlists.View.view covers lang model.playlistsModel)
                |> App.Layout.Default.render model

        SearchPage query page ->
            let
                q = Maybe.withDefault "" query
                p = Util.Maybe.filter (gt 0) page
                    |> Maybe.withDefault 1
            in
                Html.map SearchMsg (Pages.Search.View.view covers settings lang model.searchModel)
                    |> App.Layout.Default.render model

gt: Int -> Int -> Bool
gt a b =
    b > a
