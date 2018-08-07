module Util.String exposing (..)

import Base64

(%>): String -> String -> String
(%>) str1 str2 =
    concatWith " " str1 str2

(#>): String -> String -> String
(#>) str1 str2 =
      concatWith ", " str1 str2

concatWith: String -> String -> String -> String
concatWith sep str1 str2 =
    if (String.isEmpty str2) then str1
    else if (String.isEmpty str1) then str2
    else str1 ++ sep ++ str2


quote: String -> String
quote str =
    let
        q = if (String.contains "\"" str) then replace "\"" "\\\"" str
            else str
    in
    if (String.contains " " str || String.contains "\"" str) then "\"" ++ q ++ "\""
    else str

replace: String -> String -> String -> String
replace what repl input =
    let
        splits = String.split what input
               |> List.intersperse repl
    in
        List.foldr (++) "" splits

firstUpper: String -> String
firstUpper str =
    case str of
        "" -> ""
        _ ->
            (String.left 1  str |> String.toUpper) ++ String.dropLeft 1 str

dots: Int -> String -> String
dots max str =
    let
        len = String.length str
    in
        if len <= max then str
        else (String.left (max - 3) str) ++ "..."

nonEmpty: String -> Bool
nonEmpty str =
    String.isEmpty >> not
        <| str

nonEmptyMin: String -> String -> String
nonEmptyMin s1 s2 =
    if s1 == "" then s2
    else if s2 == "" then s1
    else min s1 s2

{- Encodes as string into its base64 representation and then replaces
  '=' characters with a number.

   The reason is that the url parser has problems with some chars.
-}
crazyEncode: String -> String
crazyEncode str =
    let
        b64 = Base64.encode str
        len = String.length b64
    in
        case (String.right 2 b64 |> String.toList) of
            '=' :: '=' :: [] ->
                (String.dropRight 2 b64) ++ "$$"

            _ :: '=' :: [] ->
                (String.dropRight 1 b64) ++ "$"

            _ ->
                b64

crazyDecode: String -> String
crazyDecode str =
    let
        b64 =
            case (String.right 2 str |> String.toList) of
                '$' :: '$' :: [] ->
                    (String.dropRight 2 str) ++ "=="
                        |> Base64.decode

                _ :: '$' :: [] ->
                    (String.dropRight 1 str) ++ "="
                        |> Base64.decode

                _ ->
                    Base64.decode str
    in
      case b64 of
        Ok p -> p
        Err err ->
            let
                x = Debug.log ("Error crazy decoding: " ++ str) err
            in
                str
