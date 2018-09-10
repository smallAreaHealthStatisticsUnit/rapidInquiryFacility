---
layout: default
title: Extra Postgres Configuration
---

## Config File location

A standard configuration file is supplied with PostgreSQL, called `postgresql.conf`. You can find it in the installation or data directory. The following PG SQL command will give the location:

```SQL
SHOW config_file;
```

Example locations are `C:\Program Files\PostgreSQL\9.3\data\postgresql.conf` on Windows, or  `/usr/local/var/postgres/postgresql.conf` on MacOS.

## Values to Add

At the end of the file there is a section for custom settings. Add the following values here:

```
#------------------------------------------------------------------------------
# CUSTOMIZED OPTIONS
#------------------------------------------------------------------------------

# Add settings for extensions here

# RIF
rif40.send_debug_to_info = off
rif40.debug = ''
```
