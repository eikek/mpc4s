module Requests exposing (..)

import Http
import WebSocket
import Json.Decode as Decode
import Json.Encode as Encode

import Data.MpdCommand exposing (..)
import Data.Info exposing (Info)
import Data.MpdConn exposing (MpdConn)
import Data.AlbumFile exposing (AlbumFile)
import Ports

send: MpdConn -> String -> MpdCommand -> Cmd msg
send conn baseUrl cmd =
    let
        jsonCmd = Data.MpdCommand.jsonEncode cmd |> Encode.encode 0
        wsurl = baseUrlToWs conn baseUrl
    in
        WebSocket.send wsurl jsonCmd

sendAll: MpdConn -> String -> List MpdCommand -> Cmd msg
sendAll conn baseUrl cmds =
    Cmd.batch <|
        (List.map (send conn baseUrl) cmds |> List.reverse)

sendList: MpdConn -> String -> List MpdCommand -> Cmd msg
sendList conn baseUrl cmds =
    let
        jsonCmd = Data.MpdCommand.jsonEncodeList cmds |> Encode.encode 0
        wsurl = baseUrlToWs conn baseUrl
    in
        WebSocket.send wsurl jsonCmd

info: String -> ((Result Http.Error Info) -> msg) -> Cmd msg
info baseurl receive =
    Http.get (baseurl ++ "/api/v1/info") Data.Info.jsonDecode
        |> Http.send receive

albumCoverUrl: MpdConn -> String -> Maybe Int -> String -> String
albumCoverUrl conn baseurl thumbSize albumName =
    let
        base = if String.endsWith "/" baseurl then baseurl else baseurl ++ "/"
        path = base ++ "api/v1/cover/" ++ conn.id ++ "/album"
        size = "size=" ++ (Maybe.map toString thumbSize |> Maybe.withDefault "")
        name = "name=" ++ Http.encodeUri(albumName)
    in
        path ++ "?" ++ size ++ "&" ++ name

fileCoverUrl: MpdConn -> String -> Maybe Int -> String -> String
fileCoverUrl conn baseurl thumbSize file =
    let
        base = if String.endsWith "/" baseurl then baseurl else baseurl ++ "/"
        path = base ++ "api/v1/cover/" ++ conn.id ++ "/file/"
        size = "size=" ++ (Maybe.map toString thumbSize |> Maybe.withDefault "")
        filename = Http.encodeUri(file)
    in
        path ++ filename ++ "?" ++ size

clearCoverCache: String -> (Result Http.Error () -> msg) -> Cmd msg
clearCoverCache baseurl f =
    Http.post (baseurl ++ "/api/v1/cover/clearcache")  Http.emptyBody (Decode.succeed ())
        |> Http.send f

testBooklet: String -> String -> (Result Http.Error AlbumFile -> msg) -> Cmd msg
testBooklet baseurl albumName f =
    Http.get (baseurl ++ "/api/v1/booklet/test/album?name=" ++ Http.encodeUri(albumName)) Data.AlbumFile.jsonDecode
        |> Http.send f


baseUrlToWs: MpdConn -> String -> String
baseUrlToWs conn url =
    let
        wsbase = if (String.startsWith "https:" url)
                 then "wss:" ++ (String.dropLeft 6 url)
                 else if (String.startsWith "http" url)
                      then "ws:" ++ (String.dropLeft 5 url)
                      else url
    in
        if (String.endsWith "/" wsbase)
        then wsbase ++ "api/v1/mpd/" ++ conn.id
        else wsbase ++ "/api/v1/mpd/" ++ conn.id
