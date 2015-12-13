DATABASS
--------

Doctopus leans heavily on Postgres – currently for storing its configuration, but soon also for storing compiled HTML and resources.

Schema
------

### `heads`

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|`name`|varchar|"PRIMARY KEY"| |
|`created`|timestamp|"NOT NULL DEFAULT NOW"| |
|`updated`|timestamp|"NOT NULL"| |

### `tentacles`

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|`name`|varchar(50)|"PRIMARY KEY"| |
|`output_root`|varchar(50)| |This is where doctopus will look for the output of html generation.|
|`html_commands`|varchar(250)| | |
|`source_control`|varchar(50)| |Which VCS to use|
|`source_location`|varchar(250)| |VCS URI to clone from|
|`entry_point`|varchar(50)| |HTML entrypoint for your app|
|`created`|timestamp|"NOT NULL DEFAULT NOW"| |
|`updated`|timestamp|"NOT NULL"| |

### `head_tentacle_mappings`

This table creates mappings between `heads` and `tentacles`; it relies on Postgres to to enforce A) that both names exist, and B) that when a `head` or `tentacle` is deleted, its mappings must be deleted as well.

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|`head_name`|varchar(50)|references heads(name) on delete cascade| |
|`tentacle_name`|varchar(50)|references tentacles(name) on delete cascade| |
|Primary Key:| |primary key(`head_name`, `tentacle_name`)| |

### `documents`

This one has some real complexity.

1.  The documents table needs to be indexed for full text search.
2.  *But only the html docs*.
3.  Also, docs can contain images.

The images bit is the weebly-woobly one right now. *Could* store them as base64 encoded strings in the text column; probably better to expand the schema to support an `images` key and table. Or, a `resources` table that can key to *either* a text doc or an image? Inclined to the first one, right now; would look something like:

-   Relax the `NOT NULL` constraint and use `COALESCE(body, '')` where needed and accept `nil` everywhere else.
-   If `body` is `nil` coming out of the database, decode the joined binary body and `assoc` it on as the body.

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|`name`|varchar|PRIMARY KEY| |
|`uri`|varchar|NOT NULL| |
|`body`|text|NOT NULL| |
|`search_vector`|tsvector| | |
|`tentacle_name`|varchar|references tentacles(name) on delete cascade|Foreign key; which `tentacle` does this belong to?|
|`created`|timestamp|NOT NULL DEFAULT NOW()| |
|`updated`|timestamp|NOT NULL| |
|Index:|`GIN`| | |

The table cannot easily be created (using Clojure DSLs) to have `search_vector` in place as we'll want it; instead, the following should be used:

``` {.src .src-sql}
        /* Called any time the index needs updating*/
UPDATE documents SET search_vector = to_tsvector('english', COALESCE(body, ''));

       /*Called once on table bootstrap to make the index*/
CREATE INDEX fts_idx ON documents USING GIN(search_vector);
```

Author: Ross Donaldson

Created: 2015-05-26 Tue 23:14

[Emacs](http://www.gnu.org/software/emacs/) 24.5.1 ([Org](http://orgmode.org) mode 8.2.10)

[Validate](http://validator.w3.org/check?uri=referer)
