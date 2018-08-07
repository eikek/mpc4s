module Util.Date exposing (..)

import Date exposing (Date)

formatDate: Date -> String
formatDate date =
    let
        year = Date.year date |> toString
        month = Date.month date |> toString
        dow = Date.dayOfWeek date |> toString
        day = Date.day date |> format2
    in
        dow ++ ", " ++ day ++ ". " ++ month ++ " " ++ year

formatTime: Date -> String
formatTime date =
    let
        hour = Date.hour date |> format2
        min = Date.minute date |> format2
    in
        hour ++ ":" ++ min

formatDateTime: Date -> String
formatDateTime date =
    (formatDate date) ++ ", " ++ (formatTime date)

parse: String -> Maybe Date
parse dateStr =
    if dateStr == "" then Nothing
    else case Date.fromString dateStr of
        Ok date ->
            Just date
        Err err ->
            let
                x = Debug.log "error parsing date: " err
            in
                Nothing

parseFormat: (Date -> String) -> String -> String
parseFormat fmt dateStr =
    parse dateStr
        |> Maybe.map fmt
        |> Maybe.withDefault dateStr


format2: Int -> String
format2 n =
    if n < 10 then "0" ++ (toString n)
    else toString n
