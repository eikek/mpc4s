module Util.Parsers exposing (..)

import Parser exposing (..)
import Data.Tag exposing (Tag(..))
import Data.TagValue exposing (TagValue)
import Data.Filter exposing (Filter)
import Util.String

parseFilter: String -> Maybe Filter
parseFilter str =
    case (run filter (str ++ " ")) of
        Ok f -> Just f
        Err err ->
            let
                x = Debug.log "Error parsing filter: " err
            in
                Nothing

tagParser: Tag -> Parser Tag
tagParser tag =
    map (\_ -> tag) (Data.Tag.encode tag |> keyword)

tag: Parser Tag
tag =
    oneOf (List.map tagParser (List.reverse Data.Tag.all))

valueString: Parser String
valueString =
    map Util.String.crazyDecode
        <| keep zeroOrMore (\c -> c /= ' ')

tagValue: Parser TagValue
tagValue =
    succeed TagValue
        |= tag
        |. symbol " "
        |= valueString
        |. symbol " "

filter: Parser Filter
filter =
    repeat zeroOrMore tagValue
