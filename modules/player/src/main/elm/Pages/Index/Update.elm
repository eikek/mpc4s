module Pages.Index.Update exposing (update)

import Pages.Index.Data exposing (..)

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        ReceiveInfo info ->
            {model|info = Just info} ! []
