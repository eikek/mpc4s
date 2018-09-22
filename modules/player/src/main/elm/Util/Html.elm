module Util.Html exposing (..)

import Html.Attributes
import Html exposing (Attribute)
import Base64

width: String -> Attribute msg
width w =
    Html.Attributes.attribute "width" w

makeId: String -> String
makeId str =
    Base64.encode str
