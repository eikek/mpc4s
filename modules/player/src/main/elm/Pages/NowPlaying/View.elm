module Pages.NowPlaying.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onInput)
import Http
import Pages.NowPlaying.Data exposing (..)
import Pages.NowPlaying.Messages exposing (..)
import Util.String exposing ((#>), (%>))
import Data.PlaylistSong exposing (PlaylistSong)
import Data.Range exposing (Range)
import Data.Song
import Data.Status exposing (Status)
import Data.AudioFormat exposing (AudioFormat)
import Data.Tag exposing (Tag(..))
import Data.MpdCommand exposing (MpdCommand(..))
import Data.SingleState
import Data.CoverUrls exposing (..)
import Util.Html
import Util.Time

view: CoverUrls -> String -> Model -> Html Msg
view covers lang model =
    let
        msg = getMessages lang
    in
    div[class "main-content"]
       [div [class "ui container"]
            [div [class "ui grid"]
                 [div [class "seven wide column"]
                      [currentCover covers model
                      ]
                 ,div [class "nine wide column"]
                      [currentControls msg model
                      ]
                 ]
            ]
       ,div [class "ui container"]
            [playlist covers msg model
            ]
       ]

coverUrl: CoverUrls -> PlaylistSong -> String
coverUrl covers ps =
    case (Data.Song.findTag Album ps.song) of
        Just album ->
            covers.forAlbum album
        Nothing ->
            covers.forFile ps.song.file


currentCover: CoverUrls -> Model -> Html Msg
currentCover covers model =
    case model.current of
        Just song ->
            img [(Util.Html.width "100%")
                , src (coverUrl covers song)
                ][]
        Nothing ->
            div [][]

currentControls: Messages -> Model -> Html Msg
currentControls msg model =
    case model.current of
        Just song ->
            div []
                [div [class "ui basic segment no-margin-bottom"]
                     [h1 [class "ui header"]
                         [tagValue Title song msg.noTitle |> text
                         ,div [class "sub header"]
                              [(tagValue Genre song "") #> (tagValue Date song "") |> text
                              ]
                         ]
                     ]
                ,(composerSegment msg song)
                ,div [class "ui basic red attached segment noborder no-padding-bottom pointer"]
                     [h4 [class "ui header", onClick (GotoArtist (tagValue Artist song ""))]
                         [tagValue Artist song msg.noArtist |> text
                         ,div [class "sub header"]
                              [msg.forTag Artist |> text
                              ]
                         ]
                     ]
                ,div [class "ui basic attached segment noborder no-padding-bottom pointer"]
                     [h4 [class "ui header", onClick (GotoAlbum (tagValue Album song ""))]
                         [tagValue Album song msg.noAlbum |> text
                         ,div [class "sub header"]
                              [msg.forTag Album |> text
                              ]
                         ]
                     ]
                ,(discTrackSegment msg song)
                ,div []
                     [(playtimeSegment model)
                     ]
                ]
        Nothing ->
            div [][]

playlist: CoverUrls -> Messages -> Model -> Html Msg
playlist covers msg model =
    let
        activePos =
            Maybe.withDefault -1 model.status.song
        plsize =
            List.length model.playlist
    in
    div []
        [table [class "ui table"]
             [thead []
                    [tr []
                        [th [colspan 5]
                            [div [class "ui secondary menu"]
                                 [div [class "item"]
                                      [plsize |> toString |> text
                                      ,text (" " ++ msg.items)
                                      ,text (", " ++ msg.total ++ ": ")
                                      ,playlistLength model |> text
                                      ]
                                 ,div [classList [("item", True)
                                                 ,("nodisplay", model.status.repeat || model.status.random)
                                                 ]]
                                      [text (msg.played ++ ": ")
                                      ,playlistPlayedTime model |> text
                                      ,text (", " ++ msg.remaining ++ ": ")
                                      ,playlistRemainTime model |> text
                                      ]
                                 ,div [classList [("item", True)
                                                 ,("nodisplay", model.status.repeat || model.currentTime <= 0)
                                                 ]]
                                      [text "Ends: "
                                      ,playlistEndsAt model |> text
                                      ]
                                 ,div [class "right menu"]
                                      [a [class "item", onClick (Run (DeleteRange (Range 0 activePos)))]
                                           [div [class "icons"]
                                                [i [class "alternate trash icon"][]
                                                ,i [class "corner long alternate arrow up icon"][]
                                                ]
                                           ]
                                      ,a [class "item", onClick (Run (DeleteRange (Range (activePos + 1) plsize)))]
                                          [div [class "icons"]
                                               [i [class "alternate trash icon"][]
                                               ,i [class "corner long alternate arrow down icon"][]
                                               ]
                                          ]
                                      ,a [class "item", onClick (Run Clear)]
                                           [i [class "ui trash icon"][]
                                           ]
                                      ,a [class "item", onClick (Run Shuffle)]
                                         [i [class "ui random icon"][]
                                         ]
                                      ,a [class "disabled item"]
                                          [i [class "ui save icon"][]
                                          ]
                                      ,a [class "item", onClick ToggleAddUri]
                                          [i [class "ui plus icon"][]
                                          ]
                                      ]
                                 ]
                            ]
                        ]
                    ,tr [classList [("nodisplay", not model.addUriVisible)]
                        ]
                        [th [colspan 5]
                            [div [class "right aligned"]
                                 [div [class "ui fluid action input"]
                                      [input [type_ "text"
                                             ,placeholder "File/Uri..."
                                             ,onInput AddUriChange
                                             ][]
                                      ,button [class "ui button", onClick AddUri][text "Add"]
                                      ]
                                 ]
                            ]
                        ]
                    ,tr []
                        [th [][text "#"]
                        ,th [][]
                        ,th [][text "Title"]
                        ,th [][text "Time"]
                        ,th [][]
                        ]
                    ]
             ,tbody []
                    (List.map (playlistItem covers activePos plsize) model.playlist)
             ]

        ]

playlistItem: CoverUrls -> Int -> Int -> PlaylistSong -> Html Msg
playlistItem covers active size ps =
    tr [classList [("positive", ps.pos == active)]]
       [td [class "collapsing"]
           [ps.pos |> toString |> text]
       ,td [class "collapsing"]
           [a [class "", onClick (Run (Play (Just ps.pos)))]
              [img [class "ui mini image"
                   ,src (coverUrl covers ps)
                   ][]
              ]
           ]
       ,td []
           [tagValue Title ps ps.song.file |> text]
       ,td [class "collapsing"]
           [playtime ps |> text
           ]
       ,td [class "collapsing"]
           [a [class "small basic ui icon button", onClick (Run (Delete ps.pos))]
              [i [class "ui trash icon"][]
              ]
           ,a [classList [("small basic ui icon button", True)
                         ,("nodisplay", ps.pos == 0)
                         ]
              ,onClick (Run (Swap ps.pos (ps.pos - 1)))]
               [i [class "ui arrow up icon"][]
               ]
           ,a [classList [("small basic ui icon button", True)
                         ,("nodisplay", ps.pos >= (size - 1))
                         ]
              ,onClick (Run (Swap ps.pos (ps.pos + 1)))]
               [i [class "ui arrow down icon"][]
               ]
           ]
       ]

playtimeSegment: Model -> Html Msg
playtimeSegment model =
    case model.status.time of
        Just range ->
            div [class "ui basic center aligned segment no-horizontal-padding"]
                [span [class "current-time"]
                      [range.start |> Util.Time.formatSeconds |> text
                      ]
                ,span [classList [("end-time", True)
                                 ,("nodisplay", range.end <= 0)
                                 ]
                      ]
                      [text "/"
                      ,range.end |> Util.Time.formatSeconds |> text
                      ]
                ,div [class "ui small red progress play-progress"]
                     [div [class "bar"]
                          []
                     ,div [class "label"]
                          [span [][audioFormatLabel (fileType model) model.status |> text]
                          ]
                     ]
                ,div [class "ui fluid buttons"]
                     [button [class "ui button", onClick (Run Prev)]
                         [i [class "ui step backward icon"][]
                         ]
                     ,button [class "ui button", onClick (Run (Play Nothing))]
                         [i [class "ui play icon"][]
                         ]
                     ,button [class "ui button", onClick (Run (Pause True))]
                         [i [class "ui pause icon"][]
                         ]
                     ,button [class "ui button", onClick (Run Next)]
                         [i [class "ui step forward icon"][]
                         ]
                     ]
                ,div [class "ui fluid buttons"]
                     [button [classList [("ui toggle button", True)
                                        ,("active", model.status.random)
                                        ]
                             ,onClick ToggleRandom
                             ]
                          [i [class "ui random icon"][]
                          ]
                     ,button [classList [("ui toggle button", True)
                                        ,("active", model.status.repeat)
                                        ]
                             ,onClick ToggleRepeat
                             ]
                          [i [class "ui sync icon"][]
                          ]
                     ,button [classList [("ui toggle button", True)
                                        ,("active", model.status.consume)
                                        ]
                             ,onClick ToggleConsume
                             ]
                          [i [class "ui eraser icon"][]
                          ]
                     ,button [classList [("ui toggle button", True)
                                        ,("active", model.status.single == Data.SingleState.On)
                                        ]
                             ,onClick ToggleSingle
                             ]
                          [i [class "ui chess board icon"][]
                          ]
                     ]
                ]
        Nothing ->
            div [][]

fileType: Model -> String
fileType model =
    model.current
        |> Maybe.map .song
        |> Maybe.andThen Data.Song.findFiletype
        |> Maybe.withDefault ""

audioFormatLabel: String -> Status -> String
audioFormatLabel filetype status =
    Maybe.map2 (makeAudioLabel filetype) status.bitrate status.audio
        |> Maybe.withDefault ""


makeAudioLabel: String -> Int -> AudioFormat -> String
makeAudioLabel filetype bitrate af =
    let
        concat = Util.String.concatWith " • "
    in
    concat filetype
        <| concat ((toString af.bits) ++ "bit")
        <| concat ((toString ((toFloat af.freq) / 1000.0)) ++ "kHz") ((toString bitrate) ++ "kbps")

composerSegment: Messages -> PlaylistSong -> Html Msg
composerSegment msg ps =
    case (Data.Song.findTag Composer ps.song) of
        Just val ->
            div [class "ui basic attached segment noborder pointer"]
                [h4 [class "ui header", onClick (GotoComposer val)]
                    [val |> text
                    ,div [class "sub header"]
                         [msg.forTag Composer |> text
                         ]
                    ]
                ]
        Nothing ->
            div [][]

discTrackSegment: Messages -> PlaylistSong -> Html Msg
discTrackSegment msg ps =
    let
        disc = Data.Song.findTag Disc ps.song
                      |> Maybe.map (String.append (msg.disc ++ " "))
                      |> Maybe.withDefault ""
        track = trackName msg ps
        value = if disc == "" then track else disc #> track
    in
        case value of
            "" -> div [][]
            _ ->
                div [class "ui basic attached segment noborder"]
                    [h5 [class "ui sub header"]
                        [value |> text
                        ]
                    ]

tagValue: Tag -> PlaylistSong -> String -> String
tagValue tag ps default =
    Data.Song.findTag tag ps.song
        |> Maybe.withDefault default

playtime: PlaylistSong -> String
playtime ps =
    Util.Time.formatSeconds ps.song.time

trackName: Messages -> PlaylistSong -> String
trackName msg ps =
    Data.Song.findTag Track ps.song
        |> Maybe.map (String.append ((msg.forTag Track) ++ " "))
        |> Maybe.withDefault ""
