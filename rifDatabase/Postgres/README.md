# README file for the RIF Database layer Postgres port.

This README assumes a knowledge of how V3.1 RIF works.

See [BUILD notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/psql_scripts/BUILD.md) 
for the installation instructions. The original instructions in Install.docx are no longer maintained and the contents have been moved to 
the port specific markdown files. This file contains a description of the detailed steps used in the build.

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
 	  * *v4_0_alter_8.sql*: Database test harness; see [test harness README](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/TestHarness/db_test_harness/README.md)
	  
	  Under development [not run by *db_setup*; use *make dev* to build]:
	  
	  * *v4_0_alter_3.sql*: Range partitioning (e.g. by year).
 	  * *v4_0_alter_4.sql*: Hash partitioning (e.g. by study_id).
	  
	  Completed alter script logs are named *v4_0_alter_N.&lt;database&gt;_rpt*; and renamed to *v4_0_alter_N.&lt;database&gt;_rpt.err* on error.
	  This gives make a dependency so it can re-run.
	  
	* Run test scripts (in directory *test_scripts*) make targets: *clean all*; parameters: *DEBUG_LEVEL=1 ECHO=all*
	  Test scripts that **do not** require alter scripts to be run (*no_alter* target above): 
	  * *test_1_sahsuland_geography.sql*: Test sahsuland geography is setup correctly.
	  * *test_2_ddl_checks.sql*: Check all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted
	  * *test_3_user_setup.sql*:Check &lt;test user&gt; is setup correctly.
	  
	  Test scripts that **do** require alter scripts to be run:
	  * *test_8_triggers.sql*: Test trest harness, load and run trigger tests.
	  * *test_6_middleware_1.sql*: Middleware tests 1 for *v4_0_alter_1.sql*.
	  * *test_4_study_id_1.sql*: This creates the standard study_id 1.
	  * *test_5_clone_delete_test.sql*: Clone delete test for standard study_id 1.
	  * *test_7_middleware_2.sql*: Test 7: Middleware tests 2 for *v4_0_alter_2.sql*
	  
	  Completed alter script logs are named *&lt;script&gt;.&lt;database&gt;_rpt*; and renamed to *&lt;script&gt;.&lt;database&gt;_rpt.err* on error.
	  This gives make a dependency so it can re-run.
	  
	  There is no concept of test scripts for alter scripts under development.
	  
	  Test scripts are run in sequence apart from test 8 which is run first to test the test harness.
	  
  * Run local make target: *v4_0_vacuum_analyse_dev*
    * Run psql script to *VACUUM ANALYZE* the *sahsuland_dev* database *v4_0_vacuum_analyse.sql*; log: *v4_0_vacuum_analyse.rpt*  
* Run local make target: *sahsuland_dev_dump*; parameters: *PGDATABASE=sahsuland_dev*
  * Dump *sahsuland_dev* database using *pg_dump* in custom format, excluding UK geography tables, 
    parameters: -U postgres -w -F custom -T '\*x_uk\*' -T '\*.x_ew01\*' -v sahsuland_dev > ../install/sahsuland_dev.dump
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
    parameters: *-U postgres -w -F custom -T '\*x_uk\*' -T '\*.x_ew01\*' -v sahsuland > ../install/sahsuland.dump*
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

### 1. Lack of networking with cause Node.js makefiles to fail:

* Node.js program (in directory *../Node*) *topojson_convert.js*. This will be replaced by a 
  web service. This causes a web service testing error later in the build:
```
<DETECT FAILURE EARLIER; caused by psql not exiting on shell errors>
```
* Node.js program (in directory *../../TestHarness/db_test_harness*): *db_test_harness.js*

**IMPROVE TEST 8 ERROR MESSAGE**
```
ADD
```