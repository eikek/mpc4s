---
layout: docs
title: "Installation and running"
---

# {{ page.title }}

The [Http](../http.html) and [Player](../player.html) are
applications. A debian package and a universal zip package are
[provided](../index.html#getting-it).

## Debian

The provided `deb` file can be installed on most Linuxes that are
dervied from Debian (like Debian itself, obviously, Ubuntu, Raspbian,
…).  Install the `deb` file using this command:

```bash
dpkg -i mpc4s-http_{{ site.version }}_all.deb
```

The deb package includes a systemd unit. After installing the package,
you can find the configuration file in
`/etc/mpc4s-http/mpc4s.conf`. After a change, you need to restart
mpc4s using systemd:

```bash
systemctl restart mpc4s-http
```

The log output can be viewed with `journalctl`, for example:

```bash
journalctl -efu mpc4s-http
```

## Zip

Simply unpack and start the file in the `bin/` folder. The
configuration file can be given as an argument.

```bash
./bin/mpc4s-http ./conf/mpc4s.conf
```

The default config file is in the `conf/` subdirectory. Adopt it to
your needs.

With the argument `--console`, the server can be stopped with the
_RETURN_ key.

## Note

If you use the [Player](../player.html) application, replace `mpc4s-http`
by `mpc4s-player` in this guide.
