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

            ReceiveInfo info ->
                ({model|mpdConns = info.mpd}, Cmd.none, [])

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

            TogglePlayElsewhere ->
                let
                    settings_ = {settings|playElsewhereEnabled = not settings.playElsewhereEnabled}
                in
                    ({model|settings = settings_}, Ports.storeSettings settings_, [])

            PlayElsewhereOffsetDec ->
                let
                    cur = settings.playElsewhereOffset
                    new = if cur > 0 then cur - 1 else 0
                    sett_ = {settings|playElsewhereOffset = new}
                in
                    ({model|settings = sett_}, Ports.storeSettings sett_, [])

            PlayElsewhereOffsetInc ->
                let
                    cur = settings.playElsewhereOffset
                    new = if cur < 60 then cur + 1 else 60
                    sett_ = {settings|playElsewhereOffset = new}
                in
                    ({model|settings = sett_}, Ports.storeSettings sett_, [])

            SearchPageSizeInc ->
                let
                    cur = settings.searchPageSize
                    new = if cur < 1000 then cur + 50 else cur
                    sett = {settings|searchPageSize = new}
                in
                    ({model|settings = sett}, Ports.storeSettings sett, [])

            SearchPageSizeDec ->
                let
                    cur = settings.searchPageSize
                    new = if cur >= 100 then cur - 50 else 50
                    sett = {settings|searchPageSize = new}
                in
                    ({model|settings = sett}, Ports.storeSettings sett, [])

            SetSearchView n ->
                let
                    sett = {settings|searchView = n}
                in
                    ({model|settings = sett}, Ports.storeSettings sett, [])


initCommands: Model -> List MpdCommand
initCommands model =
    [Status]
