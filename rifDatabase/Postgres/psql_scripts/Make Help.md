# Make help

* \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts\Makefile

```
P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>make help
Debug level set to default: 0
findstr "#-" Makefile
#-
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
#-
#- DO NOT RUN THE SUBDIRECTORY MAKEFILES DIRECTLY; THEY REQUIRE ENVIRONMENT SETUP TO WORK CORRECTLY
#-
        HELP=findstr "\#-" Makefile
        HELP=grep "\#-" Makefile
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
#- all: Run all completed alter scripts and test [DEFAULT]
#- patch: Run all completed alter scripts on both sahsuland_dev and sahusland
#- repatch: Re-run all in-complete alter scripts on both sahsuland_dev and sahusland
#- dev: Run all alter scripts in development
#-
#- 2. build
#-
#- sahsuland_dev_no_alter: Rebuild sahsuland_dev, test [State of SAHSULAND at port to SQL server], finally VACUUM ANALYSE
#- sahsuland_dev: Rebuild sahsuland_dev, test, then patch dev only, retest, finally VACUUM ANALYZE
#- Does not run all alter scripts in development
#- topojson_convert: GeoJSON to topoJSON converter
#-
#- 3. installers
#-
#- sahsuland_dump: Dump sahsuland database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#- sahsuland_dev_dump: Dump sahsuland_dev database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#-                     Used to create sahsuland
#-
#- 4. test
#-
#- test: Run all test scripts [Non verbose, no debug]
#- retest: Re-run incomplete test scripts [Non verbose, no debug]
#- test_no_alter: Run test scripts able to be run before the alter scripts [Non verbose, no debug]
#- test: Run all test scripts [debug_level=1]
#- test: Run all test scripts [debug_level=1]
#- test: Run all test scripts [Verbose, debug_level=2, echo=all]
#-
#- 5. cleanup
#-
#- clean: Remove logs so completed scripts can be re-run
#- devclean: Remove logs so alter scripts in development can be r-run
#-           Not normally needed as they abort.
#-
#- 7. Database setup. Needs to be able to connect to postgresDB as postgres
#-
#- db_setup: Re-create empty sahsuland, sahsuland_dev; build sahusland_dev from scripts;
#-           build dev dump files; restore sahsuland from dev dump; patch sahsuland to dev standard;
#-           build production dump file; rebuild ERD model
#- ERD: remake ERD
#- db_install: Re-create empty sahsuland, sahsuland_dev; restore from production pg_dump
#-
#- 7. miscellaneous
#-
#- v4_0_vacuum_analyse_dev: VACUUM ANALYZE sahsuland dev database
#- help: Display this help
#- recurse: Recursive make target: make recurse <recursive target>
#-          e.g. make recurse alter_1.rpt
#-
```
