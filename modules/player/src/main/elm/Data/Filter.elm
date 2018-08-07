module Data.Filter exposing (..)

import Data.TagValue exposing (TagValue)
import Data.Tag exposing (Tag(..))
import Util.Maybe

type alias Filter = List TagValue

encode: Filter -> String
encode filter =
    List.foldl (\tv -> \s -> s ++ " " ++ (Data.TagValue.encode tv)) "" filter

empty: Filter
empty =
    []

add: TagValue -> Filter -> Filter
add tv filter =
    if (member tv.tag filter)
    then List.map (\e -> if (e.tag == tv.tag) then tv else e) filter
    else tv :: filter

remove: Tag -> Filter -> Filter
remove tag filter =
    List.filter (\tv -> tv.tag /= tag) filter

keepOnly: Tag -> Filter -> Filter
keepOnly tag filter =
    List.filter (\tv -> tv.tag == tag) filter

toggle: TagValue -> Filter -> Filter
toggle tv filter =
    case (contains tv filter) of
        True -> remove tv.tag filter
        False -> add tv filter

isEmpty: Filter -> Bool
isEmpty filter =
    List.isEmpty filter

member: Tag -> Filter -> Bool
member tag filter =
    let
        tags = List.map (.tag) filter
    in
        List.member tag tags

findTag1: Tag -> Filter -> Maybe String
findTag1 tag filter =
    Maybe.map .value
        (List.filter (\e -> e.tag == tag) filter |> List.head)

findTag: Tag -> Filter -> Maybe String
findTag tag filter =
    if tag == Artist || tag == Albumartist then
        Util.Maybe.orElse (findTag1 Albumartist filter)
            (findTag1 Artist filter)
    else
        findTag1 tag filter

contains: TagValue -> Filter -> Bool
contains tv filter =
    List.member tv filter
