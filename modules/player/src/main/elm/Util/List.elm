module Util.List exposing
    (distinct
    , takeWhile
    , dropWhile
    , toDict
    , find
    , isSingleOrEmpty
    )

import Dict exposing (Dict)

distinct: List a -> List a
distinct list =
    List.reverse <|
        List.foldl (\a -> \r -> if (List.member a r) then r else a :: r) [] list

takeWhile: (a -> Bool) -> List a -> List a
takeWhile pred list =
    takeWhile1 list [] pred

takeWhile1: List a -> List a -> (a -> Bool) -> List a
takeWhile1 list result pred =
    case list of
        [] ->
            result
        e :: es ->
            if pred e then takeWhile1 es (e :: result) pred
            else result

dropWhile: (a -> Bool) -> List a -> List a
dropWhile pred list =
    case list of
        [] -> []
        e :: es ->
            if pred e then (dropWhile pred es)
            else list

toDict: List a -> Dict Int a
toDict list =
    List.indexedMap (,) list
        |> List.foldr (\(idx, a) -> \dict -> Dict.insert idx a dict) Dict.empty

find: (a -> Bool) -> List a -> Maybe a
find pred list =
    List.filter pred list
        |> List.head

isSingleOrEmpty: List a -> Bool
isSingleOrEmpty list =
    case list of
        [] -> True
        a :: [] -> True
        _ -> False
