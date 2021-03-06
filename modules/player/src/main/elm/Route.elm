module Route exposing (Route(..), findPage, setPage)

import Http
import Navigation exposing (Location)
import UrlParser exposing (Parser, QueryParser, (</>), (<?>), s, int, string, stringParam, intParam, map, oneOf, parsePath, customParam)
import Util.String
import Util.Maybe
import Util.Parsers
import Data.Tag
import Data.Filter exposing (Filter)

type Route
    = IndexPage
    | LibraryPage (Maybe String) Filter
    | NowPlayingPage
    | SettingsPage
    | PlaylistsPage (Maybe String)
    | SearchPage (Maybe String) (Maybe Int)


findPage: Location -> Route
findPage location =
    massageLocation location
        |> parsePath routeParser
        |> Maybe.withDefault IndexPage


setPage: Route -> Cmd msg
setPage page =
    case page of
        IndexPage ->
            newUrl "/"
        LibraryPage ma mf ->
            let
                albumName = Maybe.map Util.String.crazyEncode ma
                            |> Maybe.withDefault ""
                filter = encodeFilter mf
            in
                newUrl ("library?album=" ++ albumName ++ "&filter=" ++ filter)
        NowPlayingPage ->
            newUrl "playing"

        SettingsPage ->
            newUrl "settings"

        PlaylistsPage mname ->
            newUrl ("playlists" ++ (Maybe.map Util.String.crazyEncode mname |> Maybe.map (\n -> "?name=" ++ n) |> Maybe.withDefault ""))

        SearchPage query page ->
            newUrl ("search" ++
                        (Maybe.map Util.String.crazyEncode query |> Maybe.map (\n -> "?q=" ++ n) |> Maybe.withDefault "?q=") ++
                        "&p=" ++ (Maybe.withDefault 1 page |> toString))


routeParser: Parser (Route -> a) a
routeParser =
    oneOf
        [ map IndexPage (oneOf [(s ""), (s "/")]),
          map NowPlayingPage (s "playing"),
          map LibraryPage (s "library" <?> crazyParamNonEmpty "album" <?> filterParam "filter"),
          map SettingsPage (s "settings"),
          map PlaylistsPage (s "playlists" <?> crazyParamNonEmpty "name"),
          map SearchPage (s "search" <?> crazyParam "q" <?> intParam "p")
        ]

decodedString: Parser (String -> a) a
decodedString =
    map decodeString string

decodeString: String -> String
decodeString uri =
    Http.decodeUri uri
        |> Maybe.withDefault uri

newUrl: String -> Cmd msg
newUrl hash =
    Navigation.newUrl ("#" ++ hash)

defaultStringParam: String -> String -> QueryParser (String -> a) a
defaultStringParam name default =
    customParam name (Maybe.withDefault default)

massageLocation: Location -> Location
massageLocation location =
    let
        hash = if String.startsWith "#" location.hash then
                   String.dropLeft 1 location.hash
               else
                   location.hash
    in case String.split "?" hash of
        path :: query :: [] ->
            {location|pathname=path, search="?"++query}
        path :: [] ->
            {location|pathname=path,search=""}
        _ ->
            {location|pathname="",search=""}

crazyParam: String -> QueryParser (Maybe String -> a) a
crazyParam name =
    UrlParser.customParam name (\ma -> Maybe.map Util.String.crazyDecode ma)


crazyParamNonEmpty: String -> QueryParser (Maybe String -> a) a
crazyParamNonEmpty name =
    UrlParser.customParam name
        (\ma -> Maybe.map Util.String.crazyDecode ma
                |> Util.Maybe.filter (String.isEmpty >> not))


filterParam: String -> QueryParser (Filter -> a) a
filterParam name =
    UrlParser.customParam name
        (\ma -> Maybe.map Util.Parsers.parseFilter ma
                |> Maybe.map (Maybe.withDefault [])
                |> Maybe.withDefault [])

encodeFilter: Filter -> String
encodeFilter filter =
    List.map (\tv -> (Data.Tag.encode tv.tag) ++ " " ++ (Util.String.crazyEncode tv.value)) filter
        |> List.intersperse " "
        |> List.foldr (++) ""
