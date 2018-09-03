---
layout: home
position: 1
section: home
title: Home
technologies:
 - first: ["Scala", "This is a Scala library"]
 - second: ["Elm", "The web application is written in Elm"]
 - third: ["FS2", "Client and HTTP module are based on FS2"]
---

# Scala- and Web-Client for MPD

This project provides a Scala library for connecting to
[MPD](http://www.musicpd.org) and a web based user interface for
controlling MPD and browsing your music library.

- [protocol](./protocol.html): Library implementing MPD's protocol in
  Scala
- [client](./client.html): Library for interacting with MPD using
  [FS2](https://github.com/functional-streams-for-scala/fs2)
- [http](./http.html): A http server that allows to connect to MPD via
  Websockets or REST-like endpoints.
- [player](./player.html): A web-based user interface for MPD.


## Getting it

You can use the first three as libraries in your projects:

```
"com.github.eikek" %% "mpc4s-protocol" % "{{site.version}}"
"com.github.eikek" %% "mpc4s-client" % "{{site.version}}"
"com.github.eikek" %% "mpc4s-http" % "{{site.version}}"
```

The *player* and *http* application are distributed as a `zip` or
`deb` archive. Please refer to the [release
page](https://github.com/eikek/mpc4s/releases) for downloads. The
current version can be found here:

- Player {{site.version}}: [zip](https://github.com/eikek/mpc4s/releases/download/v{{site.version}}/mpc4s-player-{{site.version}}.zip), [deb](https://github.com/eikek/mpc4s/releases/download/v{{site.version}}/mpc4s-player_{{site.version}}_all.deb)
- Http {{site.version}}: [zip](https://github.com/eikek/mpc4s/releases/download/v{{site.version}}/mpc4s-http-{{site.version}}.zip), [deb](https://github.com/eikek/mpc4s/releases/download/v{{site.version}}/mpc4s-http_{{site.version}}_all.deb)

## What is MPD?

[MPD](http://www.musicpd.org) is an excellent music player software,
that has no user interface but runs as a daemon instead. That allows
to create [many different clients](https://www.musicpd.org/clients/)
and [all sorts of other
things](http://shoaib-ahmed.com/2017/mpd-client-list/).


## License

This project is distributed under the
[GPLv3](http://www.gnu.org/licenses/gpl-3.0.html)
