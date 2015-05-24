Table of Contents
-----------------

-   [1. DATABASS](#sec-1)
-   [2. Schema](#sec-2)
    -   [2.1. `heads`](#sec-2-1)
    -   [2.2. `tentacles`](#sec-2-2)
    -   [2.3. `head_tentacle_mappings`](#sec-2-3)
    -   [2.4. `documents`](#sec-2-4)

1 DATABASS
----------

Doctopus leans heavily on Postgres – currently for storing its configuration, but soon also for storing compiled HTML and resources.

2 Schema
--------

### 2.1 `heads`

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|name|varchar|"PRIMARY KEY"| |
|created|timestamp|"NOT NULL DEFAULT NOW"| |
|updated|timestamp|"NOT NULL"| |

### 2.2 `tentacles`

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|name|varchar(50)|"PRIMARY KEY"| |
|output<sub>root</sub>|varchar(50)| |This is where doctopus will|
| | | |look for the output of html|
| | | |generation.|
|html<sub>commands</sub>|varchar(250)| | |
|source<sub>control</sub>|varchar(50)| |Which VCS to use|
|source<sub>location</sub>|varchar(250)| |VCS URI to clone from|
|entry<sub>point</sub>|varchar(50)| |HTML entrypoint for your app|
|created|timestamp|"NOT NULL| |
| | |DEFAULT NOW"| |
|updated|timestamp|"NOT NULL"| |

### 2.3 `head_tentacle_mappings`

This table creates mappings between `heads` and `tentacles`; it relies on Postgres to to enforce A) that both names exist, and B) that when a `head` or `tentacle` is deleted, its mappings must be deleted as well.

Column Name

Data Type

Spec

Comments

head<sub>name</sub>

varchar(50)

references heads(name)

 

 

 

on delete cascade

 

tentacle<sub>name</sub>

varchar(50)

references tentacles(name)

 

 

 

on delete cascade

 

Extra:

 

primary key(head<sub>name</sub>,tentacle<sub>name</sub>)

 

### 2.4 `documents`

|Column Name|Data Type|Spec|Comments|
|:----------|:--------|:---|:-------|
|name|varchar|PRIMARY KEY| |
|uri|varchar|NOT NULL| |
|body|text|NOT NULL| |
|search<sub>vector</sub>|ts<sub>vector</sub>| | |
|tentacle<sub>name</sub>|varchar|references tentacles(name)|Foreign key; which|
| | |on delete cascade|`tentacle` does this|
| | | |belong to?|

Author: Ross Donaldson

Created: 2015-05-24 Sun 16:57

[Emacs](http://www.gnu.org/software/emacs/) 24.5.1 ([Org](http://orgmode.org) mode 8.2.10)

[Validate](http://validator.w3.org/check?uri=referer)
