module Pages.Search.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onClick, onSubmit, onCheck)

import Util.Html
import Util.Time
import Util.Maybe
import Pages.Search.Messages exposing (Messages, getMessages)
import Pages.Search.Data exposing (..)
import Data.CoverUrls exposing (..)
import Data.MpdConn exposing (MpdConn)
import Data.Settings exposing (Settings)
import Data.Tag exposing (Tag(..))
import Data.Song exposing (Song)
import Data.PlaylistName exposing (PlaylistName)

view: CoverUrls -> Settings -> String -> Model -> Html Msg
view covers settings lang model =
    let
        msg = getMessages lang
    in
    div[class "main-content"]
       [div [class "ui container"]
            [Html.form [class "ui form search-form", onSubmit DoSearch]
                 [div [class "ui segments"]
                      [(createSearchInput msg model)
                      ,(createSearchControls msg model)
                      ]
                 ]
            ]
       ,case model.viewMode of
            SongView ->
                createSongList msg covers model
            AlbumView ->
                createAlbumList covers settings model
       ]

createSearchInput: Messages -> Model -> Html Msg
createSearchInput msg model =
    div [class "ui basic center aligned segment"]
        [div [class "ui large fluid icon input"]
             [input [type_ "text"
                    , placeholder msg.search
                    , value model.query
                    , onInput SetQuery
                    ][]
             ,i [class "circular search link icon"
                ,onClick DoSearch
                ][]
             ]
        ]


createSearchControls: Messages -> Model -> Html Msg
createSearchControls msg model =
    div [class "ui horizontal segments"]
        [div [class "ui basic segment"]
             [div [class "inline fields"]
                  [div [class "field"]
                       [div [class "ui radio checkbox"]
                            [input [type_ "radio"
                                   , name "albumview"
                                   , checked (model.viewMode == AlbumView)
                                   , onCheck (\_ -> SetViewMode AlbumView)
                                   ][]
                            ,label [][text msg.albumView]
                            ]
                       ,div [class "ui radio checkbox"]
                           [input [type_ "radio"
                                  , name "songview"
                                  , checked (model.viewMode == SongView)
                                  , onCheck (\_ -> SetViewMode SongView)
                                  ][]
                           ,label [][text msg.songView]
                           ]
                       ]
                  ]
             ]
        ,div [class "ui basic center aligned segment"]
             [a [class "ui basic icon button", onClick PrevPage]
                [i [class "ui angle left icon"][]
                ]
             ,a [class "ui disabled basic button"]
                [model.page |> toString |> text
                ]
             ,a [class "ui basic icon button", onClick NextPage]
                [i [class "ui angle right icon"][]
                ]
             ]
        ,div [class "ui basic center aligned segment"]
            [div [classList [("ui selection dropdown", True)
                            ,("nodisplay", model.viewMode /= SongView)
                            ]]
                 [Maybe.map .name model.selectedPlaylist |> Maybe.withDefault msg.currentPlaylist |> text
                 ,i [class "dropdown icon"][]
                 ,div [class "menu"]
                     (makePlaylistList model |> List.map (selectPlaylistItem msg))
                 ]
            ,div [classList [("ui toggle checkbox", True)
                            ,("checked", model.settings.libraryIcons == "small")
                            ,("nodisplay", model.viewMode /= AlbumView)
                            ]]
                [input [type_ "checkbox"
                       ,onCheck (\_ -> ToggleIcons)
                       ,checked (model.settings.libraryIcons == "small")][]
                ,label [][text msg.smallCovers]
                ]
            ]
        ]

makePlaylistList: Model -> List (Maybe PlaylistName)
makePlaylistList model =
    Nothing :: (List.map Just model.playlists)

selectPlaylistItem: Messages -> Maybe PlaylistName -> Html Msg
selectPlaylistItem msg mpn =
    a [class "item", onClick (SelectPlaylist mpn)]
      [Maybe.map .name mpn |> Maybe.withDefault msg.currentPlaylist |> text]


createSongList: Messages -> CoverUrls -> Model -> Html Msg
createSongList msg covers model =
    div [class "ui container"]
        [table [class "ui selectable table track-table"]
               [thead []
                    [tr []
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
                   (List.map (songRow msg covers model) model.songs)
               ]
        ]

songRow: Messages -> CoverUrls -> Model -> Song -> Html Msg
songRow msg covers model song =
    let
        albumName = Data.Song.findTag Album song
        coverUrl = albumName
                   |> Maybe.map covers.forAlbum
                   |> Maybe.withDefault (covers.forFile song.file)
    in
    tr []
        [td [class "collapsing"]
            [case albumName of
                 Just name ->
                     a [class "pointer", onClick (ToDetailPage name)]
                       [img [class "ui tiny image", src coverUrl][]
                       ]
                 Nothing ->
                     img [class "ui tiny image",src coverUrl][]
            ]
        ,td [][tagValue Title song song.file |> text]
        ,td [][tagValue Artist song "" |> text]
        ,td [][tagValue Composer song "" |> text]
        ,td [][tagValue Album song "" |> text]
        ,td [class "collapsing"][Util.Time.formatSeconds song.time |> text]
        ,td [class "collapsing"]
            (songActions model song)
        ]

songActions: Model -> Song -> List (Html Msg)
songActions model song =
    [a [class "small basic ui icon button", onClick (ClearPlaySong song)]
       [i [class "ui play icon"][]
       ]
    ,a [class "small basic ui icon button", onClick (AppendSong song)]
       [i [classList [("ui plus icon", True)
                     ,("blue", Util.Maybe.isDefined model.selectedPlaylist)]][]
       ]
    ,a [class "small basic ui icon button", onClick (InsertSong song)]
       [i [class "ui arrow right icon"][]
       ]
    ]

createAlbumList: CoverUrls -> Settings -> Model -> Html Msg
createAlbumList covers settings model =
    div [classList [("ui album-list", True)]
        ]
        [ div [class ("ui " ++ settings.libraryIcons ++ " images")]
              (List.map (albumItem covers) model.albums)
        ]

albumItem: CoverUrls -> String -> Html Msg
albumItem coverurls name =
    a [class "ui image pointer", onClick (ToDetailPage name)]
        [ img [(Util.Html.width "100%")
              , src (coverurls.forAlbum name)
              ][]
        ]

tagValue: Tag -> Song -> String -> String
tagValue tag song default =
    Data.Song.findTag tag song
        |> Maybe.withDefault default
