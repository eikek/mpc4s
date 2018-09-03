---
layout: docs
position: 3
title: Client
---

# {{page.title}}

Building on the [protocol](./protocol.html) module, this is a mpd
client library using
[FS2](https://github.com/functional-streams-for-scala/fs2).

The entry point is `MpdClient` which provides ways to send commands
and receive responses.

A `MpdClient` can be created like this:

```tut:book
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent._
import scala.concurrent.ExecutionContext
import cats.effect.IO
import mpc4s.client._

implicit val EC = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
implicit val ACG = AsynchronousChannelGroup.withThreadPool(EC)

val mpc = MpdClient[IO](Connect("localhost", 6600))
```

`MpdClient` allows to `connect` to mpd returning a single element
`Stream` of a `MpdConnection`. The connection is closed, once the
`Stream` terminates, so the connection must be accessed “inside” that
Stream, for example using `flatMap`.

Additionally there are `send` methods defined for conveniently
issueing commands.

Here is an example to send a simple command:

```scala
scala> val resp = mpc.send(Search(Filter.tags(Tag.Album -> "plays"), None, None), 5.seconds)
resp: fs2.Stream[cats.effect.IO,mpc4s.protocol.Response[mpc4s.protocol.answer.SongListAnswer]] = Stream(..)
scala> resp.compile.toVector.unsafeRunSync
res1: Vector[mpc4s.protocol.Response[mpc4s.protocol.answer.SongListAnswer]] = Vector(MpdResult(SongListAnswer(SongList(Vector(Song(Uri(Eike/classic/Ladislav Jelinek/Ladislav Jelinek plays Beethoven/01-Sonata C Major Op53 Waldstein Allegro con brio (L van Beethoven)-Ladislav Jelinek.flac),Some(2018-07-16T18:45:13Z),Some(Seconds(683)),Some(683.0),ListMap(Map(Title -> Sonata C Major Op53 Waldstein, Allegro con brio (L van Beethoven), Composer -> Ludwig van Beethoven, Album -> Ladislav Jelinek plays Beethoven, Track -> 1, Albumartist -> Ladislav Jelinek, Artist -> Ladislav Jelinek, Date -> 2011, Comment -> http://magnatune.com/artists/ladislav_jelinek, Genre -> Classical))), Song(Uri(Eike/classic/Ladislav Jelinek/Ladislav Jelinek plays Beethoven/02-Sonata C Major O...
```

In this example the codec for the response is chosen at compile
time. When the concrete command is not known at compile time, one can
use a runtime registry. Then a different `send` method is used:

```scala
scala> val resp = mpc.send1(Search(Filter.tags(Tag.Album -> "plays"), None, None), 5.seconds)
resp: fs2.Stream[cats.effect.IO,mpc4s.protocol.Response[mpc4s.protocol.Answer]] = Stream(..)
scala> resp.compile.toVector.unsafeRunSync
res2: Vector[mpc4s.protocol.Response[mpc4s.protocol.Answer]] = Vector(MpdResult(SongListAnswer(SongList(Vector(Song(Uri(Eike/classic/Ladislav Jelinek/Ladislav Jelinek plays Beethoven/01-Sonata C Major Op53 Waldstein Allegro con brio (L van Beethoven)-Ladislav Jelinek.flac),Some(2018-07-16T18:45:13Z),Some(Seconds(683)),Some(683.0),ListMap(Map(Title -> Sonata C Major Op53 Waldstein, Allegro con brio (L van Beethoven), Composer -> Ludwig van Beethoven, Album -> Ladislav Jelinek plays Beethoven, Track -> 1, Albumartist -> Ladislav Jelinek, Artist -> Ladislav Jelinek, Date -> 2011, Comment -> http://magnatune.com/artists/ladislav_jelinek, Genre -> Classical))), Song(Uri(Eike/classic/Ladislav Jelinek/Ladislav Jelinek plays Beethoven/02-Sonata C Major Op53 Waldstein I...
```

In the first example, the result type was a concrete anwser type
(`Response[SongListAnswer]`), because it could be found at compile
time. In the second example the result type is `Response[Answer]`
which must be casted to a concrete type first to be useful.

These commands open each a new connection to MPD and so the mpd
commands `idle`/`noidle` are not allowed. If you want to listen for
events, use the `idle` method on `MpdClient`.

## MPD's idle mode

When a connection is opened to MPD it expects a command in a certain
time window. If that passes, the connection is closed by MPD due to a
timeout.

To be notified by MPD events and to reuse a single connection more
efficiently, the `idle` command can be used. Read more about this
[here](https://www.musicpd.org/doc/protocol/command_reference.html#status_commands).

The `idle` method on `MpdClient` opens a new connection to MPD and
immediatly sends the `idle` command, effectively disabling the server
timeout.

Here is an example. Note that it requires a running MPD to be useful:

```tut:silent
import cats.effect.{Effect, IO}
import fs2._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicLong
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent._

import mpc4s.protocol._
import mpc4s.protocol.commands._
import mpc4s.client._

implicit val EC = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
implicit val ACG = AsynchronousChannelGroup.withThreadPool(EC)

val mpc = MpdClient[IO](Connect("localhost", 6600))

def everySecond[F[_]: Effect]: Stream[F, Long] = {
  Stream.eval(Effect[F].delay(new AtomicLong(0))).
    flatMap(counter => Stream.every(1.seconds).
      filter(_ == true).
      evalMap(_ => Effect[F].delay(counter.getAndIncrement)))
}

def print: Sink[IO, Response[Answer]] =
  _.evalMap(ans => IO(println(s">>>> $ans")))

val connect = Connect("127.0.0.1", 6700)

val read = MpdClient[IO](connect).idle.
  flatMap { m =>
    val actions = everySecond[IO].
      take(6).
      evalMap({
        case 0 => m.write(CommandOrList(Status))
        case 1 => m.write(CommandOrList(Clear))
        case 2 => m.write(CommandOrList(SearchAdd(Filter.tags(Tag.Composer -> "Beethoven"))))
        case 3 => m.write(CommandOrList(Play(None)))
        case 4 => m.write(CommandOrList(Status))
        case _ => IO(())
      })

    actions.concurrently(m.read.to(print))
  }

read.compile.drain //.unsafeRunSync // only works if mpd is running
```

In this example every second a command is send to mpd using a single
connection. The same connection is used to read the responses. Both
things happen concurrently. At first, the current status is
fetched. Then the playlist is cleared and filled with all songs from
the database that have a _Composer_ tag including the value
"Beethoven". Then playback is started. And at last, the current status
is fetched again.

This prints something like this:

```
>>>> MpdResult(StatusAnswer(90,false,false,Off,false,1,0,Stop,None,None,None,None,None,None,None,None,None,Some(0.0),None,Some(),Some()))
>>>> MpdResult(Empty)
>>>> MpdResult(IdleAnswer(Vector(ChangeEvent(Playlist))))
>>>> MpdResult(Empty)
>>>> MpdResult(IdleAnswer(Vector(ChangeEvent(Playlist))))
>>>> MpdResult(Empty)
>>>> MpdResult(IdleAnswer(Vector(ChangeEvent(Player))))
>>>> MpdResult(StatusAnswer(90,false,false,Off,false,3,254,Play,Some(0),Some(Id(1)),Some(1),Some(Id(2)),Some(Range(1,407)),Some(1.137),Some(406.8),Some(198),None,Some(0.0),Some(AudioFormat(44100,16,2)),Some(),Some()))
>>>> MpdResult(IdleAnswer(Vector(ChangeEvent(Player))))
```

The code above sent 5 commands, but 9 responses have been
recorded. The reason is that MPD sent events about state changes
through this connection – the purpose of the `idle` command. The state
changes were caused by the commands themselves.

You can see a more realistic use case in the [http
module](./http.html) where this is used to back the websockets
connection.

## Dependencies

This module has the following dependencies:

- [mpc4s-protocol](protocol.html)
- [fs2-core and fs2-io](https://github.com/functional-streams-for-scala/fs2)
- [log4s](https://github.com/Log4s/log4s)
