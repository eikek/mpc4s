module Pages.Index.Data exposing (..)

import Data.Info exposing (..)

type alias Model =
    {info: Maybe Info
    }

emptyModel: Model
emptyModel =
    { info = Nothing
    }

type Msg =
    ReceiveInfo Info
