module Pages.Library.View exposing (view)

import Http
import Dict
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)

import Util.Html
import Util.Maybe
import Util.Time
import Util.Date
import Util.List
import Util.String exposing ((#>))
import Pages.Library.Data exposing (..)
import Pages.Library.AlbumInfo exposing (AlbumInfo, AlbumDisc, AlbumTrack)
import Data.Tag exposing (Tag(..))
import Data.TagValue exposing (TagValue)
import Data.Filter
import Data.CoverUrls exposing (..)
import Pages.Library.Messages exposing (..)

view: CoverUrls -> String -> Model -> Html Msg
view covers lang model =
    let
        msg = getMessages lang
    in
    div [class "main-content"]
        [(albumView covers model)
        ,(albumDetail covers msg model)
        ,(filterSelect msg model)
        ,(bottomMenu lang model)
        ]

bottomMenu: String -> Model -> Html Msg
bottomMenu lang model =
    let
        msg = getMessages lang
        si = model.selection
        label = (toString si.songs) ++ " " ++ msg.songs ++ ", " ++
                (toString si.albums) ++ " " ++ msg.albums ++
                ", " ++ (Util.Time.formatSeconds si.playtime)
    in
        case model.mode of
            AlbumDetail ->
                div [][]

            _ ->
                div [class "ui fixed bottom sticky attached menu filter-menu"]
                    [div [class "ui fluid container"]
                         [a [class "item", onClick ClearFilter]
                            [i [class "ui trash alternate icon"][]
                            ]
                         ,a [class "item", onClick (ToggleFilterMenu Genre)]
                            [filterMenuLabel msg model Genre |> text
                            ]
                         ,a [class "item", onClick (ToggleFilterMenu Composer)]
                            [filterMenuLabel msg model Composer |> text
                            ]
                         ,a [class "item", onClick (ToggleFilterMenu Albumartist)]
                            [filterMenuLabel msg model Albumartist |> text
                            ]
                         ,div [class "right menu"]
                              [div [class "item"]
                                   [text label
                                   ]
                              ]
                         ]
                    ]

filterMenuLabel: Messages -> Model -> Tag -> String
filterMenuLabel msg model tag =
    case (Data.Filter.findTag tag model.filter) of
        Just val ->
            Util.String.dots 25 val

        Nothing ->
            msg.forTag tag


filterSelect: Messages -> Model -> Html Msg
filterSelect msg model =
    div [classList [("ui filter-view", True)
                   ,("nodisplay", model.mode == AlbumList || model.mode == AlbumDetail)
                   ]
        ]
        [(case model.mode of
        FilterView Genre ->
            div [class "ui large fluid vertical menu"]
                (List.map (filterSelectItem msg model Genre) model.genres)

        FilterView Composer ->
            div [class "ui large fluid vertical menu"]
                (List.map (filterSelectItem msg model Composer) model.composers)

        FilterView Albumartist ->
            div [class "ui large fluid vertical menu"]
                (List.map (filterSelectItem msg model Albumartist) model.artists)

        _ ->
            div [][]
              )
        ]

isFiltered: Model -> Tag -> Bool
isFiltered model tag =
    Data.Filter.member tag model.filter

filterSelectItem: Messages -> Model -> Tag -> String -> Html Msg
filterSelectItem msg model tag name =
    let
        css = case (Data.Filter.contains (TagValue tag name) model.filter) of
                    True -> "active red item"
                    False -> "item"
    in
    a [class css, onClick (ToggleFilter (TagValue tag name))]
      [text (if (name == "") then msg.noName else name)
      ]

albumView: CoverUrls -> Model -> Html Msg
albumView covers model =
    div [classList [("ui album-list", True)
                   ,("nodisplay", model.mode /= AlbumList)]
             ]
             [ div [class "ui medium images"]
                   (List.map (albumItem covers) model.albums)
             ]

albumItem: CoverUrls -> String -> Html Msg
albumItem coverurls name =
    a [class "ui image pointer", onClick (ToDetailPage name)]
        [ img [(Util.Html.width "100%")
              , src (coverurls.forAlbum name)
              ][]
        ]

albumDetail: CoverUrls -> Messages -> Model -> Html Msg
albumDetail covers msg model =
    let
        info = model.albumInfo
    in
    div [classList [("ui album-detail", True)
                   ,("nodisplay", model.mode /= AlbumDetail)
                   ]
        ]
        [div [class "ui grid container"]
             [div [class "sixteen wide tablet five wide computer column"]
                  [div []
                       [img [class "ui image"
                            ,Util.Html.width "100%"
                            ,src (covers.forAlbum info.name)][]
                       ]
                  ,div [class "text-right"]
                       [List.length info.discs |> toString |> text
                       ,text msg.discs
                       ,text " , "
                       ,info.songs |> toString |> text
                       ,text msg.tracks
                       ,text " , "
                       ,Util.Time.formatSeconds info.time |> text
                       ,br [][]
                       ,text (" " ++ msg.added ++ ": ")
                       ,Util.Date.parseFormat (Util.Date.formatDate) info.lastModified |> text
                       ]
                  ,div [class "ui fluid vertical secondary menu"]
                       [a [class "item", onClick (ClearPlayAll info.name)]
                          [i [class "ui play icon"][]
                          ,text msg.clearPlAndPlayAll
                          ]
                       ,a [class "item", onClick (AppendAll info.name)]
                          [i [class "ui plus icon"][]
                          ,text msg.appendAll
                          ]
                       ,a [class "item", onClick (InsertAll info.name)]
                          [i [class "ui arrow right icon"][]
                          ,text msg.insertAll
                          ]
                       ,a [class "disabled item"]
                          [i [class "ui download icon"][]
                          ,text "Download"
                          ]
                       ]
                  ]
             ,div [class "sixteen wide tablet eleven wide computer column"]
                  [h1 [class "ui header"]
                      [info.name |> text
                      ]
                  ,div []
                      (List.map (albumDiscDetail msg info model) info.discs)
                  ]
             ]
        ]

albumDiscDetail: Messages -> AlbumInfo -> Model -> AlbumDisc -> Html Msg
albumDiscDetail msg info model disc =
    div [class "ui grid"]
        [div [class "sixteen wide column"]
             [h2 [class "ui header"]
                 [(case info.discs of
                       [] -> div [][]
                       a :: [] -> div [][]
                       _ ->
                           div [class "ui right floated secondary mini menu"]
                               [a [class "item", onClick (ClearPlayDisc info.name disc.disc)]
                                  [i [class "ui play icon"][]
                                  ]
                               ,a [class "item", onClick (AppendDisc info.name disc.disc)]
                                  [i [class "ui plus icon"][]
                                  ]
                               ,a [class "item", onClick (InsertDisc info.name disc.disc)]
                                  [i [class "ui arrow right icon"][]
                                  ]
                               ]
                  )
                 ,span [classList [("pointer", not <| Util.List.isSingleOrEmpty info.discs)]
                       , onClick (ToggleDiscCollapse disc.disc)
                       ]
                      [text (if (disc.disc == "") then "" else (msg.disc ++ " " ++ disc.disc))
                      ]
                 ,div [class "sub header"]
                      [disc.genre #>
                           disc.year #>
                           disc.composer #>
                           disc.filetype #>
                           (Util.Time.formatSeconds disc.time) |> text
                      ]
                 ]
             ]
        ,div [classList [("sixteen wide column", True)
                        ,("nodisplay", discCollapsed disc.disc model)
                        ]]
             [div [class "ui basic red centered segment header"]
                  [text disc.artist
                  ]
             ]
        ,div [classList [("sixteen wide column", True)
                        ,("nodisplay", discCollapsed disc.disc model)
                        ]]
             [table [class "ui selectable table track-table"]
                  [thead []
                       [tr []
                           [th [][msg.forTag Track |> text]
                           ,th [][msg.forTag Title |> text]
                           ,th [][msg.forTag Artist |> text]
                           ,th [][msg.forTag Composer |> text]
                           ,th [][msg.time |> text]
                           ,th [][]
                           ]
                       ]
                  ,tbody []
                      (List.map albumTrackDetail disc.tracks)
                  ]
             ]
        ]


albumTrackDetail: AlbumTrack -> Html Msg
albumTrackDetail track =
    tr []
       [td [class "collapsing"][text track.no]
       ,td [][text track.title]
       ,td [][text track.artist]
       ,td [][text track.composer]
       ,td [class "collapsing"][Util.Time.formatSeconds track.time |> text]
       ,td [class "collapsing"]
           [a [class "small basic ui icon button", onClick (ClearPlaySong track.file)]
              [i [class "ui play icon"][]
              ]
           ,a [class "small basic ui icon button", onClick (AppendSong track.file)]
              [i [class "ui plus icon"][]
              ]
           ,a [class "small basic ui icon button", onClick (InsertSong track.file)]
              [i [class "ui arrow right icon"][]
              ]
           ]
       ]
