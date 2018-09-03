---
layout: docs
position: 2
title: Protocol
---

# {{page.title}}

This module implements [MPD's
protocol](https://www.musicpd.org/doc/protocol/) in terms of case
classes and conversions from/to MPD commands and responses.

It's only dependency is
[shapeless](https://github.com/milessabin/shapeless/).

Almost all commands and its corresponding answers are
implemented. Deprecated commands and some that I couldn't get to work
are missing.

## Structuring

Case classes for various data types are in package
`mpc4s.protocol`. Specific MPD command and response types in packages
`mpc4s.protocol.commands` and `mpc4s.protocol.answer`,
respectively. The package `mpc4s.protocol.codecs` contains the
conversion from/to MPD protocol.


## Examples

### Commands

For example, to parse a `find` command:

```tut:book
import mpc4s.protocol._, mpc4s.protocol.codec._

val cc = CommandCodec.defaultCodec

val cmd = cc.parseValue("find album Echoes")

cc.write(cmd.right.get)
```

The `CommandCodec.defaultCodec` can parse most mpd commands. In the
example above the command was parsed into an object of class
`Find`. Every command has its codec defined in its companion
object. And all of them are then pulled together in `CommandCodec`.

### Responses

This example parses the response to the `status` command:

```tut:book
import mpc4s.protocol.answer._

val mpdResponse = """volume: 90
repeat: 0
random: 0
single: 0
consume: 0
playlist: 2
playlistlength: 1
mixrampdb: 0.000000
state: play
song: 0
songid: 1
time: 36:780
elapsed: 36.223
bitrate: 457
duration: 780.000
audio: 44100:16:2
"""

val codec = StatusAnswer.codec

val answer = codec.parseValue(mpdResponse)

codec.write(answer.right.get)
```

`Answer` defines the payload returned from MPD, which are implemented
in `mpc4s.protocol.answer` package (that also contains
`StatusAnswer`). As with commands, the codec for an answer type is
defined in its companion object.

There is also an `Response` trait which represents the response from
MPD: either an error or some `Answer`. MPD responds with some payload
(which may be empty) terminated by `"OK\n"`, or with a so called `ACK`
response. A codec for a response can be looked up from implicit scope,
if a codec for `Answer` is available:

```tut:book
import mpc4s.protocol._, mpc4s.protocol.answer._

val codec = implicitly[LineCodec[Response[StatusAnswer]]]

val mpdResponse = """volume: 90
repeat: 0
random: 0
single: 0
consume: 0
playlist: 2
playlistlength: 1
mixrampdb: 0.000000
state: play
song: 0
songid: 1
time: 36:780
elapsed: 36.223
bitrate: 457
duration: 780.000
audio: 44100:16:2
OK
"""

codec.parseValue(mpdResponse)

val ack = "ACK [50@2] {play} file not found\n"
codec.parseValue(ack)
```

### Selecting the correct response codec

The correct codec for a mpd response depends on the command. The
commands specify their answer type using an implicit value of
`SelectAnswer`. For example, the `list` command expects its response
to be a `ListAnswer`. So it there is this line it the companion object
of `List`:

```scala
  implicit val selectAnswer = SelectAnswer[List, ListAnswer]
```

This can be used to let the compiler select the correct codec for a
response given the command:

```tut:book
import mpc4s.protocol._, mpc4s.protocol.commands._

def chooseCodec[C <: Command, A <: Answer](cmd: C)(implicit s: SelectAnswer[C, A]) =
  s.codec

chooseCodec(Status)

```

All this is pulled together in a map in `CommandCodec` that can be
used to select codecs at runtime. It is called `ProtocolConfig` which
is a type alias:

```scala
type ProtocolConfig = Map[CommandName, CommandName.Config]
```

The `CommandName.Config` has everything needed to parse/write a
command and its response. Using this map, a codec for all commands can
be created. `CommandCodec.defaultConfig` is the default
`ProtocolConfig` containing all commands from this module. And
`CommandCodec.defaultCodec` is the codec created using
`CommandCodec.defaultConfig`.

Using that, a response and command codec can be looked up at runtime:

```tut:book
import mpc4s.protocol.commands._, mpc4s.protocol.codec._

// Using type Command, simulating we do not know the concrete type
val cmd: Command = Status

// Looking up a CommandName.Config object using the command name
val cfg = CommandCodec.defaultConfig(cmd.name)

cfg.commandCodec.parseValue("status")

cfg.responseCodec.parseValue("""volume: 90
repeat: 0
random: 0
single: 0
consume: 0
playlist: 2
playlistlength: 1
mixrampdb: 0.000000
state: play
song: 0
songid: 1
time: 36:780
elapsed: 36.223
bitrate: 457
duration: 780.000
audio: 44100:16:2
OK
""")
```

As you can see all the concrete types are gone, since we only have
that information at runtime.
