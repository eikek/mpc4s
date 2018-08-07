module Util.Time exposing (..)

import Date
import Time exposing (Time)
import Util.Date

formatSeconds: Int -> String
formatSeconds secs =
    formatDuration (Time.second * (toFloat secs))

{-| Format to h:mm:ss.
-}
formatDuration: Time -> String
formatDuration time =
    let
        h = floor (Time.inHours time)
        hms = Time.hour * (toFloat h)
        m = floor (Time.inMinutes (time - hms))
        mms = Time.minute * (toFloat m)
        s = round (Time.inSeconds (time - hms -  mms))
    in
        if (h <= 0) then (toString m) ++ ":" ++ (Util.Date.format2 s)
        else (toString h) ++ ":" ++ (Util.Date.format2 m) ++ ":" ++ (Util.Date.format2 s)

{- Format millis into "Wed, 10. Jan 2018, 18:57"
-}
formatDateTime: Time -> String
formatDateTime millis =
    Date.fromTime millis
        |> Util.Date.formatDateTime

{- Format millis into "18:57"
-}
formatTime: Time -> String
formatTime millis =
    Date.fromTime millis
        |> Util.Date.formatTime

{- Format millis into "Wed, 10. Jan 2018"
-}
formatDate: Time -> String
formatDate millis =
    Date.fromTime millis
        |> Util.Date.formatDate
