# Doctopus

## High-level goals:

 - pull code from Some Source
 - find the docs within That Source
 - Execute Some Command to generate the docs
 - put the docs in the Right Place.

Note: The Right Place is on the web (we want an HTTP server -- HTTP Kit)

### Differentiation from existing tools:

1. Separation of concerns from Sphinx
 - Sphinx/RST is an implementation, not a protocol, so will need to wrap it, rather than implement it.
 - Doctopus doesn't care what kind of documentation system you're using because document generation is pluggable.

2. Doesn't support editing or previewing documentation -- just aggregation.


## Structure

### File Parsing

Doctopus takes a configuration file that contains project roots, or documentation roots that you wish to walk. Doctopus begins this walk in the ``source`` directory.

As Doctopus walks the directory tree it will parse, and convert each file that matches the documentation format that you've configured for the repository in question.

**note:** Multiple documentation formats within a single source repository are not a specific goal of Doctopus.

### HTML output

Files that match the type of markup language you've configured for your source project will get converted to HTML in a directory structure mirroring the original, but situated in the ``output`` directory root.


### Routing HTTP to documentation sections

Early versions will most likely to the static compilation to a central directory, namespaced by repository which documentation comes from.

Later versions may begin to explore dynamically building index pages that display the full tree of documentation regardless of the input markup language.

# Thoughts

## Heroku
Here's an idea: I wonder if using Heroku as a use-case makes sense. That is: what if you could host Doctopus directly on a heroku instance? This would require a couple things:

1. *Limited use of the local file system* -- Instead of a flow from, say, git -> local tmp -> local FS, we'd need to go git -> < maybe local tmp, if needed?> -> some database.
1. *We could, potentially, _also_ host docs, a la RTD*
2. *We have to be sure a Heroku dyno actually has git installed*. This is a damn fine question.
1. Ponies?

My thought goes something like: we have a postgres instance as an authoritative Repo Storage service -- it stores everything we need to go generate a set of docs. We use local tmp to clone the repo in
to, then put the generated content in to Redis, from whence the docs are served.

The biggest thing I don't know at this point is this: _what size limits would this impose on a repo we want to pull in?_ Heroku provides what they call an Ephemeral File System; it is writable, I
assume through the Jave temporary file API (which the filesystem util we're using wraps). So. Questions about.

## Pandoc

Vendorizing pandoc would be the sweetest goddamn thing, and I have no idea how to do it. But [this dude](https://github.com/toshgoodson/pandoc-bin) apparently figured it out? IDEK.


# Known Things that Need to Get Done

Currently, we've made some headway in to a) filtering a directory for docs that match a predicate (currently markdown), b) converting that markdown to html, c) writing that out. This leaves.... some major pieces left to go after.

1. **Setting up an HTTP server and Routes** -- This involves roughly one easy part and one hard part. The easy part is the HTTP server; the much harder part is exposing routes and responding to requests. When this task is done, it should implement some version of this workflow: `Incoming request received -> appropriate document loaded -> appropriate document returned`.
2. **HTML Parsing / Templating ** -- It's not clear that every tool will produce consistent HTML -- and it sure would be nice for a Doctopus page to have a consistent look.
3. **Solidify the clone / parse / store workflow** -- Pretty self-explanatory. This is, after all, the core thing that ties this all together. 
