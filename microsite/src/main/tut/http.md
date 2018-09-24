---
layout: docs
position: 3
title: Http
---

# {{page.title}}

This module buidls on [mpc4s-client](./client.html) to create a http
interface to MPD. You can control MPD using simple REST-like endpoints
or via a websocket connection (utilising MPD's idle
concept). Responses from MPD are returned as JSON. The commands are
normal MPD commands (simple strings) just wrapped in a basic JSON
structure.

It also deals with cover images and booklet files from the file
system. This requires that `mpc4s-http` can access the same music
directory as MPD.

It is designed to run with low memory and cpu constraints and works
quite well on the raspberry pi.

## Usage

The http module can be used in two ways:

- as a library to include it in your project
- as a standalone application

The idea is to provide a good foundation to create web based clients
for MPD.

### Library

This may be useful if you want to create your client application and
want it to be ready-to-run packaged. Just add it as a dependency to
your project. This requires that you use
[fs2-http](https://github.com/Spinoco/fs2-http) in your project, too.

You can reuse the `Main` class or build your own maybe utilizing the
`Server` and `App` class. The constructor for `App` allows to specify
a custom route that is then served behind the path `/player`.

See the [Player](./player.html) module for an example. It's main class
looks like this:

```tut:silent
import cats.effect.{Sync, IO}
import spinoco.fs2.http.routing.Route
import mpc4s.http.{Main => HttpServer}
import mpc4s.http.config.AppConfig

object Endpoint {
  def apply[F[_]: Sync](cfg: AppConfig): Route[F] = ???
}

object Main {
  def main(args: Array[String]): Unit = {
    new HttpServer(cfg => Some(Endpoint[IO](cfg))).run(args)
  }
}
```

The `Endpoint` in this example is creating the route for the player
web application. The `cfg` parameter is the `AppConfig`, which might
be useful for creating the route.


### Standalone

Another scenario may be that you have some HTML or Javascript that
only requires to access the endpoints. Then simply
[download](index.html#getting-it) a `zip` or `deb` package and start
the application.

It can even serve the static content by specifying a directory in the
configuration file that will be served (recursively). It is behind the
url path `/custom`. See the [configuration](http/configuration.html)
section for details.


## Dependencies

- [mpc4s-client](client.html)
- [fs2-http](https://github.com/Spinoco/fs2-http)
- [pureconfig](https://github.com/Spinoco/fs2-http)
- [logback-classic](https://logback.qos.ch/)
- [circe (core, parser, generic)](https://github.com/circe/circe)
- [tika-core](https://tika.apache.org/)
- [log4s](https://github.com/Log4s/log4s)
