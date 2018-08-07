module Util.Maybe exposing (..)

isDefined: Maybe a -> Bool
isDefined mb =
    case mb of
        Just _ -> True
        Nothing -> False

isEmpty: Maybe a -> Bool
isEmpty mb =
    not (isDefined mb)

filter: (a -> Bool) -> Maybe a -> Maybe a
filter pred ma =
    case ma of
        Just a ->
            if pred a then Just a else Nothing
        Nothing ->
            Nothing

orElse: Maybe a -> Maybe a -> Maybe a
orElse m1 m2 =
    case m1 of
        Just _ -> m1
        Nothing -> m2

-- toggle: (Maybe a) -> a -> Maybe a
-- toggle ma a =
--     case ma of
--         Just e ->
--             if (a == e) then Nothing
--             else Just a
--         Nothing -> Just a
