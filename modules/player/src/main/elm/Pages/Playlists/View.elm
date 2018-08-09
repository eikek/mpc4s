module Pages.Playlists.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onInput)

import Pages.Playlists.Data exposing (..)
import Pages.Playlists.Messages exposing (..)
import Util.Time
import Data.CoverUrls exposing (CoverUrls)
import Data.PlaylistName exposing (PlaylistName)
import Data.Tag exposing (Tag(..))
import Data.Song exposing (Song)

view: CoverUrls -> String -> Model -> Html Msg
view covers lang model =
    let
        msg = getMessages lang
    in
        div[class "main-content playlists-page"]
           [div [class "ui container"]
                (case model.current of
                     Just sp ->
                         [(Maybe.map (renameEnabledHeader msg) model.rename |> Maybe.withDefault (renameDisabledHeader sp))
                         ,(songlistTable covers msg sp model)
                         ]
                     Nothing ->
                         [h1 [class "ui header"][text msg.headline]
                         ,(playlistTable msg model)
                         ]
                )
           ]

renameEnabledHeader: Messages -> RenameData -> Html Msg
renameEnabledHeader msg rename =
    h3 [class "ui header"]
       [div [class "ui fluid action input"]
            [input [type_ "text", value rename.newName, onInput RenameChange][]
            ,button [class "ui button", onClick RenamePlaylist][text msg.rename]
            ]
       ]

renameDisabledHeader: StoredPlaylist -> Html Msg
renameDisabledHeader sp =
    h1 [class "ui header"]
       [text sp.name.name
       ]

songlistTable: CoverUrls -> Messages -> StoredPlaylist -> Model -> Html Msg
songlistTable covers msg sp model =
    let
        nsongs = List.length sp.songs
        ptime = List.map .time sp.songs |> List.sum |> Util.Time.formatSeconds
    in
    table [class "ui selectable table track-table"]
        [thead []
             [tr []
                  [th [colspan 7, class "no-padding"]
                      [div [class "ui secondary menu"]
                           [a [class "small basic ui icon item button"
                              , onClick (PlayPlaylist sp.name)
                              , title msg.clearAndPlay]
                              [i [class "ui play icon"][]
                              ]
                           ,a [class "small basic ui icon item button"
                              , onClick (LoadPlaylist sp.name)
                              , title msg.loadPlaylistTitle]
                              [i [class "ui folder open outline icon"][]
                              ]
                           ,a [class "small basic ui icon item button"
                              , onClick EnableRename
                              , title msg.renamePlaylist
                              ]
                              [i [class "ui edit icon"][]
                              ]
                           ,a [class "small basic ui icon item button"
                              ,onClick ToggleAddUri
                              ,title msg.addFileToPlaylist
                              ]
                              [i [class "ui plus icon"][]
                              ]
                           ,div [class "right menu"]
                                [a [class "small basic ui icon item button"
                                   ,onClick (DeletePlaylist sp.name)
                                   ,title msg.removePlaylist
                                   ]
                                   [i [class "ui trash icon"][]
                                   ]
                                ]
                           ]
                      ]
                  ]
             ,tr [classList [("nodisplay", not model.addUriVisible)]]
                 [th [colspan 7]
                     [div [class "right aligned"]
                          [div [class "ui fluid action input"]
                               [input [type_ "text"
                                      ,placeholder "File/Uri..."
                                      ,onInput AddUriChange
                                      ][]
                               ,button [class "ui button", onClick AddUri][text msg.add]
                               ]
                          ]
                     ]
                 ]
             ,tr []
                 [th [colspan 7, class "right aligned"]
                     [span []
                          [toString nsongs |> text
                          ,text (" " ++ msg.songs ++ ", ")
                          ,text ptime
                          ]
                     ]
                 ]
             ,tr []
                  [th [][]
                  ,th [][msg.forTag Title |> text]
                  ,th [][msg.forTag Artist |> text]
                  ,th [][msg.forTag Composer |> text]
                  ,th [][msg.forTag Album |> text]
                  ,th [][msg.time |> text]
                  ,th [][]
                  ]
             ]
        ,tbody []
            (List.indexedMap (songlistRow msg sp covers) sp.songs)
        ]

songlistRow: Messages -> StoredPlaylist -> CoverUrls -> Int -> Song -> Html Msg
songlistRow msg sp covers index song =
    let
        coverUrl = Data.Song.findTag Album song
                   |> Maybe.map covers.forAlbum
                   |> Maybe.withDefault (covers.forFile song.file)
    in
    tr []
        [td [class "collapsing"]
            [img [class "ui tiny image"
                 ,src coverUrl][]
            ]
        ,td [][tagValue Title song song.file |> text]
        ,td [][tagValue Artist song "" |> text]
        ,td [][tagValue Composer song "" |> text]
        ,td [][tagValue Album song "" |> text]
        ,td [class "collapsing"][Util.Time.formatSeconds song.time |> text]
        ,td [class "collapsing"]
            (songActions sp index song)
        ]

songActions: StoredPlaylist -> Int -> Song -> List (Html Msg)
songActions sp index song =
    [a [class "basic ui icon button", onClick (DeletePlaylistSong sp.name index)]
       [i [class "ui trash icon"][]
       ]
    ,a [classList [("basic ui icon button", True)
                  ,("nodisplay", index == 0)
                  ]
       ,onClick (MoveSong sp.name index (index - 1))
       ]
       [i [class "ui arrow up icon"][]
       ]
    ,a [classList [("basic ui icon button", True)
                 ,("nodisplay", (index + 1) == (List.length sp.songs))
                 ]
       ,onClick (MoveSong sp.name index (index + 1))
       ]
       [i [class "ui arrow down icon"][]
       ]
    ]

playlistTable: Messages -> Model -> Html Msg
playlistTable msg model =
    table [class "ui table"]
        [thead []
             [tr []
                 [th [colspan 3, class "no-padding"]
                     [div [class "ui secondary menu"]
                          [a [class "small basic ui icon item button"
                             , onClick ToggleAddPlaylist
                             , title msg.addPlaylist]
                               [i [class "ui plus icon"][]
                               ]
                          ]
                     ]
                 ]
             ,tr [classList [("nodisplay", not model.addPlaylistVisible)]]
                 [th [colspan 3]
                     [div [class "right aligned"]
                          [div [class "ui fluid action input"]
                               [input [type_ "text"
                                      ,placeholder "Name"
                                      ,onInput AddPlaylistChange
                                      ][]
                               ,button [class "ui button", onClick AddPlaylist][text msg.add]
                               ]
                          ]
                     ]
                 ]
             ,tr []
                  [th [][msg.name |> text]
                  ,th [][msg.lastMod |> text]
                  ,th [][]
                  ]
             ]
        ,tbody []
            (List.map (playlistTableRow msg) model.playlists)
        ]

playlistTableRow: Messages -> PlaylistName -> Html Msg
playlistTableRow msg pn =
    tr [class "pointer"]
        [td [onClick (ShowPlaylist pn)][text pn.name]
        ,td [class "collapsing", onClick (ShowPlaylist pn)][text pn.lastModified]
        ,td [class "collapsing"]
            [a [class "small basic ui icon button"
               , onClick (PlayPlaylist pn)
               , title msg.clearAndPlay]
               [i [class "ui play icon"][]
               ]
            ,a [class "small basic ui icon button"
               , onClick (LoadPlaylist pn)
               , title msg.loadPlaylistTitle]
               [i [class "ui folder open outline icon"][]
               ]
            ]
        ]


tagValue: Tag -> Song -> String -> String
tagValue tag song default =
    Data.Song.findTag tag song
        |> Maybe.withDefault default
