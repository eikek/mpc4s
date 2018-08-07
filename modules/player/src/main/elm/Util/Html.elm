module Util.Html exposing (..)

import Html.Attributes
import Html exposing (Attribute)

width: String -> Attribute msg
width w =
    Html.Attributes.attribute "width" w
