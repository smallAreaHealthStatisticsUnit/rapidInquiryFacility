# README file for the RIF Database layer

This README assumes a knowledge of how V3.1 RIF works.

See the TODO.txt file for the current state of the development.

See Install.docx for installation instructions. You will need to get a clean dump of the database from SAHSU or create it yourself from sahsuland_dev

WARNING: The RIF requires Postgres 9.3 or 9.4 to work. 9.1 and 9.2 will not work. In particular PL/pgsql GET STACKED DIAGNOSTICS is used which is a post 9.2 option.

## Development History


Considerable work was done on improving RIF extract performance for the SAHSU Environment and Health Atlas; which the current RIF could not realistically cope  with as studies were taking days to run. After some experiment a single denominator driven extract was decided on, supporting multiple investigations (hence numerators) and covariates. The table structure is similar to the SAHSU population tables and suffers from the same performance problems. Essentialy, Oracle will do a full table scan of the population table unless it is forced not to use IOTs and common table expressions. The RIF extract tables will behave in the same manner; and Postgres behaves in the same way. The root cause of this is high speed of sequential disk scans, Oracle and Postgres both assume than it is always quicker to read the whole table in and not use the indexes. Using an Oracle 10053 trace to look at the cost based optimiser decison tree shows that it was close, but wrong. Using an Oracle Index organised table (cluster in Postgres land) forces the use of the index and the queries speeds up many, many times faster (20 minimum normally). As stated before Postgres behaves exactly the same. Adjusting the balance between sequential and random IO would help; but always run the risk that out of date table and index statistics or loosing the adjusting the balance between sequential and random IO will cause a sever performance problem. Clustering is far simpler. This wil be the subject of a blog. Final Oracle performance - Engand and Wales at Census output area level 1974-2009, 5 investigations, 58 million rows (2.2 GB data as a CSV) in 23 mins at a parallelisation of 2. Postgres will be at least 30% slower on similar hardware until the current parallelisation development is implemented.

The version 4.0 (V4.0) RIF database was created from version 3.1 in Oracle (itself a port of the Access version). At this point primary, foreign keys and triggers were added and a number of improvements:

* Full support for multiple users (this was added via a column default to version 3.1). T_ tables have an associated view so the user can only see their own data, or data shared to them by the RIF_MANAGER role
* Support for RIF_USER, RIF_MANAGER and RIF_STUDENT (restricted geolevels, low cell count suppression) roles added
* Investigation conditions and covariates were normalised
* Age, sex field support was added
* ICD9/10 support was made configurable and ICD oncology, UK HES operational codes added
* Dummy geospatial support added (i.e. the columns only)
* Automatic denominator/numerator support was added
* Basic auditing support
* Support for multiple denominators accross a study was discontinued. A study may use multiple numerators accross it investigations but only one denominator.

The V3.0 data was then imported into V4.0 to test the triggers.

The Postgres port was created using ora2pg; which initally created foreign data wrapper tables for all the Oracle tables. This is the migration database sahsuland_v3_v4. The data was then dumped to CSV files.

The full V4.0 RIF schema was created using ora2pg and scripts. The triggers were ported and the basic PL/pgsql support added. The data was then imported.

This is the rif40 database on the SAHSU private network and will be the live SAHSU RIF database when complete. SAHSUland was also ported to create SAHSUland.

Geospatial support was then added; initially merely imported. Support was progressively added for geoJSON, simplification and geolevel intersection. A simple study extract was performed.

## Database development environment

Current two databases are used: sahsuland and sahsuland_dev. Sahsuland is kept stable for long periods, sahsuland_dev is the working database. After the move from the SAHSU private network to the main Imperial network the database structure is only to be modifed by alter scripts so that the Private network can be kept up to date.

The directory structure is rifDatabase\Postgres\:

* conf			- Exmaples of varaious configuration files
* example_data		- Example extracts and results creating as part of SAHSUland build
* logs			- Logs from psql_scripts runs
* PLpgsql		- PL/pgsql scripts
* psql			- PSQL scripts
* sahsuland		- SAHSUland creation psql scripts
* sahsuland\data	- SAHSUland data
* shapefiles		- Postgres psql SQL scripts derived from shapefiles, creation scripts
* shapefiles\data	- Shapefiles

sahsuland and sahsuland_dev are built using make. On Windows install MinGW with the full development environment. Add c:\MinGW\msys\1.0\bin to path.

To build sahsuland_dev:

```
cd rifDatabase\Postgres\psql
c:\MinGW\msys\1.0\bin\make sahsuland_dev
```

The Makefile has help:

```
C:\Users\pch\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>make help
findstr "#-" Makefile
#-
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
#-
#- DO NOT RUN THE SUBDIRECTORY MAKEFILES DIRECTLY; THEY REQUIRE ENVIRONMENT SETUP TO WORK CORRECTLY
#-
HELP=findstr "\#-" Makefile
#-
#- PL/pgsql debug levels (DEBUG_LEVEL);
#-
#- 0 - Suppressed, INFO only
#- 1 - Major function calls
#- 2 - Major function calls, data
#- 3 - Reserved for future used
#- 4 - Reserved for future used
#-
#- PSQL verbosity (VERBOSITY):
#-
#- verbose      - Messages/errors with full context
#- terse        - Just the error or message
#-
#- PSQL echo (ECHO)
#-
#- all:                 - All SQL
#- none:                - No SQL
#-
#- Targets
#-
#- 1. patching
#-
#- all: Run all completed alter scripts [DEFAULT]
#- patch: Run all completed alter scripts on both sahsuland_dev and sahusland
#- dev: Run all alter scripts in development
#-
#- 2. build
#-
#- sahsuland_dev_no_alter: Rebuild sahsuland_dev, test [State of SAHSULAND at port to SQL server], finally VACUUM ANALYZE
#- sahsuland_dev: Rebuild sahsuland_dev, test, then patch dev only, retest, finally VACUUM ANALYZE
#-
#- 3. installers
#-
#- sahsuland_dump: Dump sahsuland database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#- sahsuland_dev_dump: Dump sahsuland_dev database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#-                     Used to create sahsuland
#-
#- 4. test
#-
#- test: Run test scripts [Non verbose, no debug]
#- test: Run test scripts [debug_level=1]
#- test: Run test scripts [Verbose, debug_level=2, echo=all]
#-
#- 5. cleanup
#-
#- clean: Remove logs so completed scripts can be re-run
#- devclean: Remove logs so alter scripts in development can be r-run
#-           Not normally needed as they abort.
#-
#- 6. miscellaneous
#-
#- v4_0_vacuum_analyse_dev: VACUUM ANALYZE sahsuland dev database
#- help: Display this help
#- recurse: Recursive make target: make recurse <recursive target>
#-          e.g. make recurse alter_1.rpt
#-
```

The principal build script is v4_0_create_sahsuland.sql, It must be run as the schema owner (rif40) on sahsuland_dev only. sahusland is always created afresh from an empty database using pg_restore. E.g

cd rifDatabase\Postgres\psql

psql -U postgres -d postgres

Create the SAHSULAND_DEV tablespace:

CREATE TABLESPACE sahsuland_dev LOCATION 'C:\\PostgresDB\\sahsuland_dev';

(Re-)create sahsland_Dev database. This requires sahsuland_dev.dump, otherwise edit create_sahsuland_dev_db.sql to build direct from scripts:

psql -U postgres -d postgres -w -e -f create_sahsuland_dev_db.sql

Check sahsuland builds from the scripts:

psql -U rif40 -d sahsuland_dev -w -e -f v4_0_create_sahsuland.sql

To create sahsuland.dmp, excluding UK91, EW01 shapefiles from non dev dumps:

pg_dump -U postgres -w -F custom -T '*x_uk*' -T '*.x_ew01*' -v sahsuland > C:\Users\pch\sahsuland.dump

As of March 2014, the Java test programs (dumpdata) and the installation notes and scripts are not in the repository. They will be added when tidied some more.

## Design Documenation

The RIF was reverse engineered, intially using the Oracle database modeler, and with the current Github release usinh pgmodeler (https://github.com/pgmodeler/pgmodeler).
 

