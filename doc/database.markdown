-   DATABASS

Doctopus leans heavily on Postgres -- currently for storing its configuration, but soon also for storing compiled HTML and resources.

-   Schema

\*\* <sub>heads</sub> |-------------+-----------+------------------------+----------| | Column Name | Data Type | Spec | Comments | |-------------+-----------+------------------------+----------| | name | varchar | "PRIMARY KEY" | | | created | timestamp | "NOT NULL DEFAULT NOW" | | | updated | timestamp | "NOT NULL" | | |-------------+-----------+------------------------+----------|

\*\* <sub>tentacles</sub>

|-----------------+--------------+---------------+------------------------------| | Column Name | Data Type | Spec | Comments | |-----------------+--------------+---------------+------------------------------| | name | varchar(50) | "PRIMARY KEY" | | | output\_root | varchar(50) | | This is where doctopus will | | | | | look for the output of html | | | | | generation. | | html\_commands | varchar(250) | | | | source\_control | varchar(50) | | Which VCS to use | | source\_location | varchar(250) | | VCS URI to clone from | | entry\_point | varchar(50) | | HTML entrypoint for your app | | created | timestamp | "NOT NULL | | | | | DEFAULT NOW" | | | updated | timestamp | "NOT NULL" | | |-----------------+--------------+---------------+------------------------------|

\*\* <sub>head\_tentacle\_mappings</sub>

This table creates mappings between <sub>heads</sub> and <sub>tentacles</sub>; it relies on Postgres to to enforce A) that both names exist, and B) that when a <sub>head</sub> or <sub>tentacle</sub> is deleted, its mappings must be deleted as well.

|---------------+-------------+--------------------------------------+----------| | Column Name | Data Type | Spec | Comments | |---------------+-------------+--------------------------------------+----------| | head\_name | varchar(50) | references heads(name) | | | | | on delete cascade | | | tentacle\_name | varchar(50) | references tentacles(name) | | | | | on delete cascade | | |---------------+-------------+--------------------------------------+----------| | Extra: | | primary key(head\_name,tentacle\_name) | | |---------------+-------------+--------------------------------------+----------|

\*\* <sub>documents</sub> |---------------+-----------+----------------------------+--------------------| | Column Name | Data Type | Spec | Comments | |---------------+-----------+----------------------------+--------------------| | name | varchar | PRIMARY KEY | | | uri | varchar | NOT NULL | | | body | text | NOT NULL | | | search\_vector | ts\_vector | | | | tentacle\_name | varchar | references tentacles(name) | Foreign key; which | | | | on delete cascade | <sub>tentacle</sub> does this | | | | | belong to? | |---------------+-----------+----------------------------+--------------------|
