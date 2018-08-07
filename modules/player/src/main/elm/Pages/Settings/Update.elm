module Pages.Settings.Update exposing (update, initCommands)

import Ports
import Requests
import Pages.Settings.Data exposing (..)
import Data.Answer exposing (Answer(..))
import Data.MpdCommand exposing (MpdCommand(..))

update: Msg -> Model -> (Model, Cmd Msg, List MpdCommand)
update msg model =
    let
        settings = model.settings
    in
        case msg of
            SetLanguage lang ->
                let
                    settings_ = {settings|lang = lang}
                in
                    ({model|settings = settings_}, (Ports.storeSettings settings_), [])

            ToggleShowVol ->
                let
                    settings_ = {settings|showVolume = not settings.showVolume}
                in
                    ({model|settings = settings_}, Ports.storeSettings settings_, [])

            ReceiveSettings set ->
                ({model|settings = set}, Cmd.none, [])

            HandleAnswer (StatusInfo status) ->
                ({model|status = status}, Cmd.none, [])

            HandleAnswer _ ->
                (model, Cmd.none, [])

            RunCmd cmds ->
                (model, Cmd.none, cmds)

            ClearCache ->
                (model, Requests.clearCoverCache model.baseurl ClearCacheResult, [])

            ClearCacheResult _ ->
                (model, Cmd.none, [])

            VolumeStepInc ->
                let
                    cur = settings.volumeStep
                    new = if cur < 20 then cur + 1 else 20
                    sett_ = {settings|volumeStep = new}
                in
                    ({model|settings = sett_}, Ports.storeSettings sett_, [])

            VolumeStepDec ->
                let
                    cur = settings.volumeStep
                    new = if cur > 1 then cur - 1 else 1
                    sett_ = {settings|volumeStep = new}
                in
                    ({model|settings = sett_}, Ports.storeSettings sett_, [])

            SetMpdConn conn ->
                let
                    sett_ = {settings|mpdConn = conn}
                in
                    ({model|settings = sett_}, Ports.storeSettings sett_, [])

initCommands: Model -> List MpdCommand
initCommands model =
    [Status]
