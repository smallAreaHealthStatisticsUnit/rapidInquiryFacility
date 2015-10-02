# README file for the RIF Database layer Postgres port.

This README assumes a knowledge of how V3.1 RIF works.

See [BUILD notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/psql_scripts/BUILD.md) 
for the installation instructions. The original instructions in Install.docx are no longer maintained and the contents have been moved to 
the port specific markdown files. This file contains a description of the detailed steps used in the build.

WARNING: The RIF requires Postgres 9.3 or 9.4 to work. 9.1 and 9.2 will not work. In particular PL/pgsql GET STACKED DIAGNOSTICS is used which 
is a post 9.2 option. 

The new V4.0 RIF uses either Postgres or Microsoft SQL server as a database backend.

## Development History

See also the [development log] 
(https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/Database%20Development%20Log.md)

Considerable work was done on improving RIF extract performance for the SAHSU Environment and Health Atlas; which the current RIF could not 
realistically cope  with as studies were taking days to run. After some experiment a single denominator driven extract was decided on, 
supporting multiple investigations (hence numerators) and covariates. The table structure is similar to the SAHSU population tables and 
suffers from the same performance problems. Essentialy, Oracle will do a full table scan of the population table unless it is forced not 
to use IOTs and common table expressions. The RIF extract tables will behave in the same manner; and Postgres behaves in the same way. The 
root cause of this is high speed of sequential disk scans, Oracle and Postgres both assume than it is always quicker to read the whole table 
in and not use the indexes. Using an Oracle 10053 trace to look at the cost based optimiser decison tree shows that it was close, but wrong. 
Using an Oracle Index organised table (cluster in Postgres land) forces the use of the index and the queries speeds up many, many times faster 
(20 minimum normally). As stated before Postgres behaves exactly the same. Adjusting the balance between sequential and random IO would help; 
but always run the risk that out of date table and index statistics or loosing the adjusting the balance between sequential and random IO will 
cause a sever performance problem. Clustering is far simpler. This wil be the subject of a blog. Final Oracle performance - Engand and Wales at 
Census output area level 1974-2009, 5 investigations, 58 million rows (2.2 GB data as a CSV) in 23 mins at a parallelisation of 2. Postgres will 
be at least 30% slower on similar hardware until the current parallelisation development is implemented.

The version 4.0 (V4.0) RIF database was created from version 3.1 in Oracle (itself a port of the V3.0 Access version). At this point primary, foreign 
keys and triggers were added and a number of improvements:

* Full support for multiple users (this was added via a column default to version 3.1). T_ tables have an associated view so the user can 
  only see their own data, or data shared to them by the RIF_MANAGER role
* Support for RIF_USER, RIF_MANAGER and RIF_STUDENT (restricted geolevels, low cell count suppression) roles added
* Investigation conditions and covariates were normalised
* Age, sex field support was added
* ICD9/10 support was made configurable and ICD oncology, UK HES operational codes added
* Dummy geospatial support added (i.e. the columns only)
* Automatic denominator/numerator support was added
* Basic auditing support
* Support for multiple denominators accross a study was discontinued. A study may use multiple numerators accross it investigations but 
  only one denominator.

The V3.0 data was then imported into V4.0 to test the triggers.

The Postgres port was created using ora2pg; which initally created foreign data wrapper tables for all the Oracle tables. This is the migration 
database sahsuland_v3_v4. The data was then dumped to CSV files.

The full V4.0 RIF schema was created using ora2pg and scripts. The triggers were ported and the basic PL/pgsql support added. The data 
was then imported. 

The new RIF database is being targeted as PostGres 9.3 or later; and will use features requiring this version.

Windows and most Linux distributions have pre built packages for Postgres. Building from source is also covered in the Linux build notes 

The RIF database design was reverse engineered, intially using the Oracle database modeler, and with the current Github release using 
[pgmodeler](https://github.com/pgmodeler/pgmodeler).

Geospatial support was then added; initially merely imported. Support was progressively added for geoJSON, simplification and geolevel 
intersection. A simple study extract was performed.

## Postgres Setup

Postgres is usually setup in one of three ways:
 
* Standalone mode on a Windows firewalled laptop. This uses local database MD5 passwords and no SSL and is not considered secure for network use.
* Secure mode on a Windows server and Active directory network. This uses remote database connections using SSL; with SSPI (Windows GSS 
  connectivity) for psql and secure LDAP for Java connectivity.
* Secure mode on a Linux server and Active directory network. This uses remote database connections using SSL; with GSSAPI/Kerberos for 
  psql and secure LDAP for Java connectivity.

Postgres can proxy users (see ident.conf examples in the [build notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/build.md)). 
Typically this is used to allow remote postgres administrator user authentication and to logon as the schema owner (rif40).

## Postgres Database Build from scripts (db_setup make target)

The principal build target (*make db_setup*) runs the following Makefiles/targets/scripts:

* Clean build targets (log files); local make target: clean
* Run local make target: *db_create*; parameters: *PSQL_USER=postgres PGDATABASE=postgres DEBUG_LEVEL=1 ECHO=all*
  * Run psql script *db_create.sql*; log: *db_create.rpt*
* Install required Node.js locally (in directory *../Node*) make target: *install*	
* Run local make target: *sahsuland_dev*; parameters: *PGDATABASE=sahsuland_dev*
  * Run local make target to create **sahsuland_dev**:
    * Run local make target: *clean*
	* Run local make target: *$(PLR_DIRS)*
	* Run main **sahsuland_dev** creation psql script: *v4_0_create_sahsuland.sql*; log: *v4_0_create_sahsuland.rpt* 
	* Run test scripts not dependent on alter scripts (in directory *test_scripts*) make target: *no_alter*; *parameters: DEBUG_LEVEL=1 ECHO=all*
	  **This is the build state at the point the SQL Server port was started**.
	* Run alter scripts (in directory *alter_scripts*) make targets: *clean all*; parameters: *DEBUG_LEVEL=1 ECHO=all*
	  * *v4_0_alter_1.sql*: Misc schema design changes
 	  * *v4_0_alter_2.sql*: Misc data viewer changes.
 	  * *v4_0_alter_5.sql*: Zoomlevel support. Rebuilds all geolevel tables with full partitioning (alter #3 support).
 	  * *v4_0_alter_6.sql*: R support [optional script if PL/R integration is enabled with *USE_PLR=Y*].
 	  * *v4_0_alter_7.sql*: Support for taxonomies/ontologies (e.g. ICD9, 10); removed previous table based support.
                            Modify t_rif40_inv_conditions to remove SQL injection risk.
 	  * *v4_0_alter_8.sql*: Database test harness; see [test harness README](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/test_harness/db_test_harness/README.md)
	  
	  Under development [not run by *db_setup*; use *make dev* to build]:
	  
	  * *v4_0_alter_3.sql*: Range partitioning (e.g. by year).
 	  * *v4_0_alter_4.sql*: Hash partitioning (e.g. by study_id).
	  
	  Completed alter script logs are named *v4_0_alter_N.<database>_rpt*; and renamed to *v4_0_alter_N.<database>_rpt.err* on error.
	  This gives make a dependency so it can re-run.
	  
	* Run test scripts (in directory *test_scripts*) make targets: *clean all*; parameters: *DEBUG_LEVEL=1 ECHO=all*
	  Test scripts that **do not** require alter scripts to be run (*no_alter* target above): 
	  * *test_1_sahsuland_geography.sql*:
	  * *test_2_ddl_checks.sql*:
	  * *test_3_user_setup.sql*:
	  
	  Test scripts that **do** require alter scripts to be run:
	  * *test_8_triggers.sql*:
	  * *test_6_middleware_1.sql*: Middleware tests 1 for *v4_0_alter_1.sql*.
	  * *test_4_study_id_1.sql*: This creates the standard study_id 1.
	  * *test_5_clone_delete_test.sql*:
	  * *test_7_middleware_2.sql*:
	  
	  Completed alter script logs are named *<script>.<database>_rpt*; and renamed to *<script>.<database>_rpt.err* on error.
	  This gives make a dependency so it can re-run.
	  
	  There is no concept of test scripts for alter scripts under development.
	  
	  Test scripts are run in sequence apart from test 8 which is run first to test the test harness.
	  
  * Run local make target: *v4_0_vacuum_analyse_dev*
    * Run psql script to *VACUUM ANALYZE* the *sahsuland_dev* database *v4_0_vacuum_analyse.sql*; log: *v4_0_vacuum_analyse.rpt*  
* Run local make target: *sahsuland_dev_dump*; parameters: *PGDATABASE=sahsuland_dev*
  * Dump *sahsuland_dev* database using *pg_dump* in custom format, excluding UK geography tables, 
    parameters: -U postgres -w -F custom -T '*x_uk*' -T '*.x_ew01*' -v sahsuland_dev > ../install/sahsuland_dev.dump
    **This is used to create the *sahsuland* database**.	
* Run in *../install* *pg_restore*; log; *pg_restore.rpt*; parameters: *-d sahsuland -U postgres -v ../install/sahsuland_dev.dump* 
* Run alter scripts (in directory alter_scripts) make targets: *clean all*; parameters: *PGDATABASE=sahsuland*		
* Run Node.js program (in directory *../Node*) make target: *topojson_convert*; parameters: *PGDATABASE=sahsuland*
  This runs the Node.js program *topojson_convert.js* to convert GeoJSON to TopoJSON. This will be replaced by a web service.
* Run local make targets: *clean v4_0_vacuum_analyse_dev*; parameters: *PGDATABASE=sahsuland*
    * Run psql script to *VACUUM ANALYZE* the *sahsuland* database *v4_0_vacuum_analyse.sql*; log: *v4_0_vacuum_analyse.rpt*
* Run test scripts (in directory *test_scripts*) make targets: *test_scripts clean all*; parameters: *PGDATABASE=sahsuland*	
* Run local make target: *sahsuland_dump*
  * Dump *sahsuland* database using *pg_dump* in custom format, excluding UK geography tables, 
    parameters: *-U postgres -w -F custom -T '*x_uk*' -T '*.x_ew01*' -v sahsuland > ../install/sahsuland.dump*
    **This is used as the *sahsuland* database* installer.**	
* Run Entity Relationship Diagrams build (in directory *../../ERD*) make target: *dbms_tools*
  <ADD>
	
End of a successful db_setup make run:
```
pg_dump: setting owner and privileges for FK CONSTRAINT table_or_view_name_hide_fk
pg_dump: setting owner and privileges for FK CONSTRAINT table_or_view_name_hide_fk
make[1]: Leaving directory `/c/Users/pch/Documents/GitHub/rapidInquiryFacility/rifDatabase/Postgres/psql_scripts'
make -C ../../ERD dbms_tools
Debug level set to default: 0
make[1]: Entering directory `/c/Users/pch/Documents/GitHub/rapidInquiryFacility/rifDatabase/ERD'
Makefile:184: *** commands commence before first target.  Stop.
make[1]: Leaving directory `/c/Users/pch/Documents/GitHub/rapidInquiryFacility/rifDatabase/ERD'
make: [db_setup] Error 2 (ignored)
SAHSULAND and SAHSULAND_DEV setup completed OK
```

## Issues with the build

### Lack of networking with cause Node.js makefiles to fail:

* Node.js program (in directory ../Node) topojson_convert.js. This will be replaced by a 
  web service. This causes a web service testing error later in the build:
```
<DETECT FAILURE EARLIER; caused by psql not exiting on shell errors>
```
* Node.js program (in directory ../../test_harness/db_test_harness): db_test_harness.js

<IMPROVE TEST 8 ERROR MESSAGE>