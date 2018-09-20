module Util.Html exposing (..)

import Html.Attributes
import Html exposing (Attribute)

width: String -> Attribute msg
width w =
    Html.Attributes.attribute "width" w

makeId: String -> String
makeId str =
    let
        chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-"
    in
        "Album-" ++ (String.foldr (appendIf <| String.toList chars) "" str)

appendIf: List Char -> Char -> String -> String
appendIf allowed ch str =
    let
        pre = if List.member ch allowed then ch else '-'
    in
        if pre == '-' && String.startsWith "-" str then str
        else String.cons pre str
