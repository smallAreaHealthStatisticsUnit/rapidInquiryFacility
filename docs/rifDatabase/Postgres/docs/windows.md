---
layout: default
title: Windows installation notes
---

Postgres is best downloaded from Enterprise DB: http://www.enterprisedb.com/products-services-training/pgdownload.
The Postgres installer then runs stack builder to download and install the additional packages. The following additional packages need to be installed:

* PostGres (database, PG Admin III administration tool, and common extensions)
* PostGIS (Geospatial integration)
* PostGIS (Geospatial integration)
* pgJDBC (Java database connector for PostGres)

The following are optional:

* pgAgent (batch engine)
* pgBouncer (load balancer; for use if you have a synchronous or near synchronous replica database)
* pgODBC (ODBC database connector for PostGres)

## Fixing Windows Code page errors

```
H:\>psql
psql (9.3.2)
WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
SSL connection (cipher: DHE-RSA-AES256-SHA, bits: 256)
Type "help" for help.
```
* Type:
```
cmd.exe /c chcp 1252
```
* Or: modify the cmd shortcut to run *cmd.exe /k chcp 1252*

