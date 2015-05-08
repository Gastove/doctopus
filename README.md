# Doctopus

[![Build Status](https://travis-ci.org/Gastove/doctopus.svg?branch=master)](https://travis-ci.org/Gastove/doctopus)

Read The Docs is a terrific project... as long as you want to use Sphinx. But
how often is that, really? In our day-to-day as documenters of technical
projects, we might write all manner of things -- Java with JavaDocs, folders
full of `markdown`, custom JavaScript analyzers, Scaladoc -- it's a big list of
thingies. They're all perfectly capable of being typeset to HTML. It's time to
make it a heck of a lot easier to get them on the web, in an orchestrated,
easy-to-serve way.

Doctopus is a framework for taking a project full of something-HTML-able,
generating that HTML, and serving it on the Internet. Or, if you like, it's like
RTD without the RST dependency. Also: it's trivial to host. (Not everything
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

### External Dependencies and Services

#### PostgreSQL
You will need an instance of [Postgres](http://www.postgresql.org/) 9.3 or
higher running somewhere with a fresh database to use. Get that spun up, log in,
and get a little `CREATE DATABASE doctopus;` going. If you want to run the test
suite, `CREATE DATABASE doctopus_test` is your friend. Note: these things are
both configurable, if you really wanna (see below).


## Project Configuration

### Local Configs

You'll want to mosey on over to the `resources` directory and make a
`configuration-local.edn` by copying the template. Get a value or two configured
in there and you'll be good to go to get the whole thing spun up:

```bash
        > NOMAD_ENV=DEV lein run
```

(You can, of course, set `NOMAD_ENV` in the `rc` file of your choice.)

## Usage

Right now: you don't.

### Free-range TODO List and Known Improvements


## License

Copyright © 2015 Ross Donaldson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
