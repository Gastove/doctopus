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

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|`name`|varchar|PRIMARY KEY| |
|`uri`|varchar|NOT NULL| |
|`body`|text|NOT NULL| |
|`search_vector`|ts<sub>vector</sub>| | |
|`tentacle_name`|varchar|references tentacles(name) on delete cascade|Foreign key; which `tentacle` does this belong to?|

Author: Ross Donaldson

Created: 2015-05-24 Sun 17:19

[Emacs](http://www.gnu.org/software/emacs/) 24.5.1 ([Org](http://orgmode.org) mode 8.2.10)

[Validate](http://validator.w3.org/check?uri=referer)
