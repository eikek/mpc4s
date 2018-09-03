---
layout: docs
title: "Configuration"
---

# {{ page.title }}

A configuration can be given as argument to the application or via a
system property `config.file`. Please see the
[Spec](https://github.com/typesafehub/config/blob/master/HOCON.md) for
the config file format.

It is recommended to copy the `reference.conf` file (which is shown
below) and comment everything and then uncomment and change things you
want to be different.

The packaged application already contains this file. All settings not
specified in the configuration file falls back to the appropriate
setting in `reference.conf`.

## Reference.conf

```
{% include_relative _reference.conf %}
```

## Noteworthy settings

There is a required setting (the app won't start without):

- `mpc4s.http.music-directory`: The music directory used by MPD. This
  is used to serve cover files from.

Other important ones include:

- `mpc4s.http.baseurl`: The base url to the application. This is used
  to construct urls.
- `mpc4s.http.bind.{host|port}`: The host and port to bind the http
  server to.
- `mpc4s.http.mpd.default.{host|port}`: Connection info to MPD.
- `mpc4s.http.mpd.default.max-connections`: The maximum allowed connections to
  MPD. This must be lower than the corresponding value in `mpd.conf`.

Regarding `max-connections` setting: MPD has the same setting that
specifies how many concurrent connections are allowed. This value
should always be lower than the one in your MPD config file! It can be
quite low. But: When the covers are initially loaded, one mpd request
is issued for each cover. So there are many multiple concurrent
requests. A high `max-connections` value here will speed up loading
covers initially. However, after first load, the paths to the cover
files are cached (the images are also cached in the browser) and
subsequent requests won't ask mpd again.
