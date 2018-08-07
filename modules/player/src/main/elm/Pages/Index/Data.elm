module Pages.Index.Data exposing (..)

import Http
import Data.Info exposing (..)

type alias Model =
    {info: Maybe Info
    }

emptyModel: Model
emptyModel =
    { info = Nothing
    }

type Msg =
    ReceiveInfo (Result Http.Error Info)
