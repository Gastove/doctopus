# DATABASS<a id="sec-1" name="sec-1"></a>

Doctopus leans heavily on Postgres &#x2013; currently for storing its configuration,
but soon also for storing compiled HTML and resources.

# Schema<a id="sec-2" name="sec-2"></a>

## `heads`<a id="sec-2-1" name="sec-2-1"></a>

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="left" />

<col  class="left" />

<col  class="left" />

<col  class="left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="left">Column Name</th>
<th scope="col" class="left">Data Type</th>
<th scope="col" class="left">Spec</th>
<th scope="col" class="left">Comments</th>
</tr>
</thead>

<tbody>
<tr>
<td class="left">name</td>
<td class="left">varchar</td>
<td class="left">"PRIMARY KEY"</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">created</td>
<td class="left">timestamp</td>
<td class="left">"NOT NULL DEFAULT NOW"</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">updated</td>
<td class="left">timestamp</td>
<td class="left">"NOT NULL"</td>
<td class="left">&#xa0;</td>
</tr>
</tbody>
</table>

## `tentacles`<a id="sec-2-2" name="sec-2-2"></a>

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="left" />

<col  class="left" />

<col  class="left" />

<col  class="left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="left">Column Name</th>
<th scope="col" class="left">Data Type</th>
<th scope="col" class="left">Spec</th>
<th scope="col" class="left">Comments</th>
</tr>
</thead>

<tbody>
<tr>
<td class="left">name</td>
<td class="left">varchar(50)</td>
<td class="left">"PRIMARY KEY"</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">output<sub>root</sub></td>
<td class="left">varchar(50)</td>
<td class="left">&#xa0;</td>
<td class="left">This is where doctopus will</td>
</tr>


<tr>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">look for the output of html</td>
</tr>


<tr>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">generation.</td>
</tr>


<tr>
<td class="left">html<sub>commands</sub></td>
<td class="left">varchar(250)</td>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">source<sub>control</sub></td>
<td class="left">varchar(50)</td>
<td class="left">&#xa0;</td>
<td class="left">Which VCS to use</td>
</tr>


<tr>
<td class="left">source<sub>location</sub></td>
<td class="left">varchar(250)</td>
<td class="left">&#xa0;</td>
<td class="left">VCS URI to clone from</td>
</tr>


<tr>
<td class="left">entry<sub>point</sub></td>
<td class="left">varchar(50)</td>
<td class="left">&#xa0;</td>
<td class="left">HTML entrypoint for your app</td>
</tr>


<tr>
<td class="left">created</td>
<td class="left">timestamp</td>
<td class="left">"NOT NULL</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">DEFAULT NOW"</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">updated</td>
<td class="left">timestamp</td>
<td class="left">"NOT NULL"</td>
<td class="left">&#xa0;</td>
</tr>
</tbody>
</table>

## `head_tentacle_mappings`<a id="sec-2-3" name="sec-2-3"></a>

This table creates mappings between `heads` and `tentacles`; it relies on Postgres
to to enforce A) that both names exist, and B) that when a `head` or `tentacle` is
deleted, its mappings must be deleted as well.

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="left" />

<col  class="left" />

<col  class="left" />

<col  class="left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="left">Column Name</th>
<th scope="col" class="left">Data Type</th>
<th scope="col" class="left">Spec</th>
<th scope="col" class="left">Comments</th>
</tr>
</thead>

<tbody>
<tr>
<td class="left">head<sub>name</sub></td>
<td class="left">varchar(50)</td>
<td class="left">references heads(name)</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">on delete cascade</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">tentacle<sub>name</sub></td>
<td class="left">varchar(50)</td>
<td class="left">references tentacles(name)</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">on delete cascade</td>
<td class="left">&#xa0;</td>
</tr>
</tbody>

<tbody>
<tr>
<td class="left">Extra:</td>
<td class="left">&#xa0;</td>
<td class="left">primary key(head<sub>name</sub>,tentacle<sub>name</sub>)</td>
<td class="left">&#xa0;</td>
</tr>
</tbody>
</table>

## `documents`<a id="sec-2-4" name="sec-2-4"></a>

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="left" />

<col  class="left" />

<col  class="left" />

<col  class="left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="left">Column Name</th>
<th scope="col" class="left">Data Type</th>
<th scope="col" class="left">Spec</th>
<th scope="col" class="left">Comments</th>
</tr>
</thead>

<tbody>
<tr>
<td class="left">name</td>
<td class="left">varchar</td>
<td class="left">PRIMARY KEY</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">uri</td>
<td class="left">varchar</td>
<td class="left">NOT NULL</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">body</td>
<td class="left">text</td>
<td class="left">NOT NULL</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">search<sub>vector</sub></td>
<td class="left">ts<sub>vector</sub></td>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
</tr>


<tr>
<td class="left">tentacle<sub>name</sub></td>
<td class="left">varchar</td>
<td class="left">references tentacles(name)</td>
<td class="left">Foreign key; which</td>
</tr>


<tr>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">on delete cascade</td>
<td class="left">`tentacle` does this</td>
</tr>


<tr>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">&#xa0;</td>
<td class="left">belong to?</td>
</tr>
</tbody>
</table>
