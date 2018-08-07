module Pages.Index.Update exposing (update)

import Pages.Index.Data exposing (..)

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        ReceiveInfo (Ok info) ->
            {model|info = Just info} ! []

        ReceiveInfo (Err _) ->
            model ! []
