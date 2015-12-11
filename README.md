# Doctopus

[![Build Status](https://travis-ci.org/Gastove/doctopus.svg?branch=master)](https://travis-ci.org/Gastove/doctopus)

Read The Docs is a terrific project... as long as you want to use
Sphinx. In our day-to-day as coders, we might write all manner of things -- Java
with JavaDocs, folders full of `.markdown`, custom JavaScript analyzers. They're
all perfectly capable of being typeset to HTML. It's time to make it a heck of a
lot easier to get them on the web.

Doctopus is a framework for taking a project full of something-HTML-able,
generating that HTML, and serving it on the Internet. Or, if you like, it's like
RTD without the RST dependence. Also: it's trivial to host. (Not everything
should be public, y'know?)

It's also... a work in progress. Like.... very in progress. Go easy on it.

## Installation

This bit is simple, as long as you've got [lein](http://leiningen.org/) 2+ and
git installed:

```bash
        > git clone doctopus
        > cd doctopus
        > lein deps
```

This'll get almost everything you need downloaded.

You will also need an instance of [Postgres](http://www.postgresql.org/) 9.3 or
higher running somewhere with a fresh database to use.

Now you have a choice:

### Local Configs

You'll want to mosey on over to the `resources` directory and make a
`configuration-local.edn` by copying the template. Get a value or two configured
in there and you'll be good to go to get the whole thing spun up:

```bash    
    > NOMAD_ENV=DEV lein trampoline run
```

(You can, of course, set `NOMAD_ENV` in the `rc` file of your choice.)

### ClojureScript

ClojureScript is build with [cljsbuild][], which has a few different profiles
that you can use to build. If you're only interested in trying things out, the
simplest is to build with the default profile:

```bash
lein cljsbuild once
```

This will get you set up to run the local server.

For development, [figwheel][] is included. Do the following to get the figwheel
server running and autobuilding:

```bash
lein with-profile figwheel figwheel dev dev-omni # yes, figwheel twice!
```

### SASS

The stylesheets are currently checked into the repository as compiled CSS and as
the source SASS, since they cannot be compiled without the help of an external
tool. If you're not working on styles, you can just use what's included, but if
you'd like to make style changes, you'll need either `node-sass` or the `sass`
Ruby Gem.

To compile with `node-sass`:

```bash
npm install -g node-sass
cd resources/public/assets/styles
node-sass --include-path ./sass --watch ./sass --output ./css ./sass
```

This will set up an auto-watching build server for CSS.

## Usage

Right now: you don't.

### Free-range TODO List and Known Improvements

See the [issues][].

## License

Copyright © 2015 Ross Donaldson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[cljsbuild]: https://github.com/emezeske/lein-cljsbuild
[figwheel]: https://github.com/bhauman/lein-figwheel
[issues]: https://github.com/Gastove/doctopus/issues
