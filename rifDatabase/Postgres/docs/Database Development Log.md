# RIF database Deveopment Log

Peter Hambly.
Last update: 25/3/2015

Current highest priority is [Urgent] and [Enhancements - GIS]


## RIF development blocks

### RIF Block I

Aug-Sept

FF - Rif manager, leaflet geoJSON performance issues, chart prototypes
KG - Java frontend/middleware, test cases
PH - Study extract to observed, triggers, GIS simplification

### RIF Block II

Was Dec-Jan, then Mar-Apr, now Jun-Jul

FF - Rif manager database/middleware integration; RIF study creation
     prototype, charting
KG - Java frontend/middleware working demo, web services
PH - TopoJSON support, GIS enhancements, partitioning
     and related performance enhancements
MD - Expected, R integration
AL - R calculations from expected
RH - SQL server port to sahsuland

#### Database work queue

This is work in progres

* R integration - calculate expected etc
* Partitioning and cluster support
* GIS enhancements (simplification, geolevel intersection)
* TopoJSON support

May be deferred to block III:

* Define map table layout; create as copy from rif40_results etc
* Error (and information) message table, error messages and call by code 
* Freeze database for SQL server port; export Postgres DB (only) code to Sourceforge/Github [Done]
* Hole detector (missing numerator/denominator/covariate data) AND BETTER WARNING MESSAGES!
	1. Holes in denominator data [before extract]. 
           Checks: PARTIAL - missing years, detect all NULLs in filter fields (AGE_GROUP, SEX, YEAR) 
                   FULL - missing age sex groups within a year, NULL geolevels (Study level parameter)
	   Missing denominator data should be an error
	2. Holes in numerator/covariate data [after extract]
           Missing covariates, no numerators should be an error either globally or within a year.
* Make state machine capable of handling year/age group gaps at the investigation level.

### RIF Block III

Aug-Sep. Expected to include:

* Completion of R integration
* Remote health database support (Oracle only)
* Full support for age/sex/age_sex_group)

## Current Faults  

### Fixed

1. rif40_sql_log and t_rif40_sql_log need to use the same enum for statement_type (so you can INSERT into rif40_sql_log) [FIXED]
2. AUDSID is missing from rif_studies view [FIXED]. 
4. remove age_group from t_rif40_studies and add aggregation code to rif40_studies view. Likewise year. [FIXED]
5. No comments rif40_results [FIXED]
9. SAHSUland.dump:
   * Must NOT contain: ew01/uk91 GIS tables (far, far too big); ICD9/10 tables (WHO copyright issues). ICD9/10 reduced to Lung Cancer rows
   * Also applies to github source files [FIXED]

### Outstanding

3. rif40_sm_pkg.rif40_study_ddl_definer() needs to be split into 2 functions and the SQL generator moved from 
   rif40_sm_pkg.rif40_create_extract() to these functions (before/after INSERT). rif40_study_ddl_definer() 
   replacements changed to use views if possible. [Security bug]6. Fix documentation fault in dbmstools (see Documentation)
7. Make INV_1 INV_<inv_id> in results and results maps
8. Make sure user cannot change AUDSID
10. The following SQL snippet from rif40_geo_pkg.populate_hierarchy_table() 
    causes ERROR:  invalid join selectivity: 1.000000 
    in PostGIS 2.1.1 (fixed in 2.2.1/2.1.2 - to be release May 3rd 2014)
11. Missing comments in RIF40_STUDIES (year_stop, start etc)
12. Rename "offset" in RIF40_AGE_GROUPS to age_group_offset

See: http://trac.osgeo.org/postgis/ticket/2543

SELECT a2.area_id AS level2, a3.area_id AS level3,
       ST_Area(a3.optimised_geometry) AS a3_area,
       ST_Area(ST_Intersection(a2.optimised_geometry, a3.optimised_geometry)) AS a23_area
  FROM t_rif40_geolevels_geometry_sahsu_level3 a3, t_rif40_geolevels_geometry_sahsu_level2 a2  
 WHERE ST_Intersects(a2.optimised_geometry, a3.optimised_geometry);

Currently sahsuland_geography is loaded from the CSV file as a workaround

#### Issues to be handled better on startup

These are problem with the startup procedure: rif40_sql_pkg.rif40_startup()

ERROR(1):

C:\Users\pch\Documents\database\postgres\psql_scripts>psql
You are connected to database "sahsuland" as user "pch" on host "localhost" at port "5432".
SSL connection (cipher: DHE-RSA-AES256-SHA, bits: 256)
psql:C:/Program Files/PostgreSQL/9.3/etc/psqlrc:36: ERROR:  unrecognized configuration parameter "rif40.send_debug_to_info"
psql (9.3.2)
SSL connection (cipher: DHE-RSA-AES256-SHA, bits: 256)
Type "help" for help.

CAUSE: No parameter set in postgresql.conf

FIX: Add rif40.* to postgresql.conf (See install.docx)

ERROR(2): Unable to find rif40 tables.

CAUSE: RIF40 not in search path

FIX: ALTER DATABASE sahsuland SET search_path TO rif40,public,topology,gis,pop,rif40_sql_pkg;

ERROR(3) Schema error on startup:

psql:C:/Program Files/PostgreSQL/9.3/etc/psqlrc:36: ERROR:  schema "pch" does not exist

CAUSE: User owned schema not created

FIX: Create

ERROR(4): psql:C:/Program Files/PostgreSQL/9.3/etc/psqlrc:36: WARNING:  rif40_ddl(): SQL in error> CREATE TABLE pch.t_rif40_num_denom (
        geography              VARCHAR(50)     NOT NULL,
        numerator_table        VARCHAR(30)     NOT NULL,
        denominator_table      VARCHAR(30)     NOT NULL);
psql:C:/Program Files/PostgreSQL/9.3/etc/psqlrc:36: ERROR:  permission denied for schema pch

CAUSE: Schema pch not granted ro role pch

FIX: GRANT ALL ON SCHEMA pch TO pch;

### Schema Issues

MIN/MAX on RIF40_INV_COVARIATES - can this be removed
Also, RIF40_INV_COVARIATES.study_geolevel_name/geography - is it really needed - it is used for enforce as foreign key (it is/should be an error in the state machine and triggers to attempt change either)

## Current State of the Development

### Disabled functions

* Functionality reduction restrictions. Restricted to:
	One covariate
 	No direct standardisation (rif40_studies.direct_stand_tab IS NULL)
	Covariate study_geolevel_name must be the same as rif40_studies.study_geolevel_name -
        Covariate geolevel can be of lower resolution than study geolevel.
	Disease mapping only
	AGE_SEX_GROUP column only (i.e. no separate AGE_GROUP/AGE/SEX columns)

[This is a statement and not a priority]

### Projected functionality

* Temporal analysis (run by year/groups of years)
* Covariates without years
* Study age bands, e.g. 0-19,20-29,60-74,75+

### R Integration

To be defined

## Code layout 

a) Private Network

* create/Oracle to only contain Oracle build code. V3-4 autogenerated scripts to be in a common directory (sahsuv3_4)
* create/sahsuland
* create/sahsuv3_4
* create/sahsuv3_4\data
* v4_0\database\postgres with

	* bat_scripts		- Windows bat CMD and ps1 powershell scripts
	* conf			- Exmaples of varaious configuration files
	* example_data		- Example extracts and results creating as part of SAHSUland build
	* install		- Install scripts and documentation
	* java			- Database tools directory
	* java/rif40_db		- rif40_db Java library
	* logs			- Logs from psql_scripts runs
	* PLpgsql		- PL/pgsql scripts
	* psql_scripts		- PSQL scripts
	* sahsuland		- SAHSUland creation psql scripts
	* sahsuland\data	- SAHSUland data
	* shapefiles		- Postgres psql SQL scripts derived from shapefiles, creation scripts
	* shapefiles\data	- Shapefiles
	* sh_scripts		- Shell scripts

b) GitHub integration on pubblic network (https://github.com/kgarwood/rapidInquiryFacility)

The directory structure is rifDatabase\Postgres\ with the same file type as above:

* conf			- Exmaples of varaious configuration files
* example_data		- Example extracts and results creating as part of SAHSUland build
* logs			- Logs from psql_scripts runs
* PLpgsql		- PL/pgsql scripts
* psql			- PSQL scripts
* sahsuland		- SAHSUland creation psql scripts
* sahsuland\data	- SAHSUland data
* shapefiles		- Postgres psql SQL scripts derived from shapefiles, creation scripts
* shapefiles\data	- Shapefiles

As of March 2014, the Java test programs (dumpdata) and the installation notes and scripts are not in the repository. They will be added when tidied come more.

## Documentation

* Fix constraint ordering bug in dbmstools
* Add/via another route document triggers, trigger functions, schemas and
  schema functions
* Use function schema comment to describe library overall
* Tidy ERD

## Enhancements

### ISS [Italian] Enhancements

Project and theme specific permissions: project/theme/user/time intersection

* rif40_project_themes (project, theme)
* Add to rif40_num_denom, rif40_num_denom_errors; 
* check valid rif40_num_denom on insert into rif40_studies.denominator_table/investigations.numerator_table

Population weight exposure functionality

To be defined

### CDC Enhancements

To be defined
 
### Enhancements - GIS

In progress

* TopoJSON support [could use node.js]
* Complete and integrate new simplification code [DONE for SAHSUland]
* Integrate geolevel intersection code (preserving old intersection table;
  verify in rif40/sahsuland builds) [DONE for SAHSUland]
* All processed geometry to be in 4326 [DONE]
* Add additional tables/columns as required by Fred (being specified):
 
    Add: gid_rowindex (i.e 0000000001_0000000001). Where gid corresponds to gid in geometry table
         row_index is an incremental serial aggregated by gid ( starts from one for each gid). 
         GID is unique for the geolevel [Done]

* Add default_study_geolevel to RIF40_GEOGRAPHY [DONE]
* PNG selection map (by study ID and by array list)
* Add male, female population totals to geolevel lookup tables [DONE for SAHSUland]
* Default geolvel description (Description of first non NULL geolevel 
  description + area_id code (needs a defaulted column) [DONE]
* Add BAND_ID to area_id translation for disease mapping extract/results tables (also name, geometry, geojson to results)

### Enhancements - for plugins

* Study extract preview
* Charts

### Enhancements - General

* Middleware enhancements as required by Kev (being specified):

  Remove foreign key t_rif40_inv_geography_fk from t_rif40_investigations, NUll data, remove column [Done]

* Check for comments on views [Done]
* VACUUM After Kerberos username update
* IG verification triggers
* Auto setup rif40_geolevels.listing =1 if name is not null and not the same and the area_id value
* Move schema checks to a separate package.
* Order in themes?
* PK constraints on G_ tables. Remove "GLOBAL" keyword [Done]
* It is possible to remove resolution, comparea, listing from rif40_geolevels
  if topoJSON performs as well as expected
* Add: RIF40_INV_HEALTH_OUTCOMES
	- outcome_group_name
        - outcome_type
        - version
        - code

### Performance enhancements

* Partitioning support - SAHSULAND example tables (Cancer+pop), study_id tables
* Cluster population tables (i.e. IOT) [DONE for SAHSUland]
* Check partition elimination works [OK for ranges, not for hashes]
* Determine the cost of single year extraction vs 2/4/all 
* Test cluster of SAHSULAND example tables, RIF40_RESULTS, extract and map
  tables
* Use OFFSET 0 parser trick to disable nested loop unrolling (i.e. the Postgres
  equivilent of the MATERIALIZE Oracle hint) if required
* Check for required indexes on tables in rif40_run_Study(); check ANALYZE
* Method4 print - use TEMPORARY VIEW
* method4 CSV - use TEMPORARY TABLE, batch multi CSVlines into 1 output row - 
  e.g. 10,000 lines = 100x100
* Set EFFECTIVE_CACHE_SIZE to 70% of RAM, SHARED_BUFFERS to 20% (max 8GB) - 
  i.e. use the SLAB buffering; shared buffers are for insert/update/delete only
* Extracts - add RIF_ROW support (order by highest resolution study geolevel, event 
  date, postcode, record order number) and index 

### RIF Auditing Enhancements

* Enable pg_stat_statement; max=50000; save=true

track_activity_query_size=4096
track_io_timing=on
track_functions=pl
update_process_table=true

* Create batch job (1/day) to copy pg_stat_statement to auto partitioned table
  (by year, month). Create function to prevent rif40_run_study() from running
  if pg_stat_statement > 70% full
* The module pg_log_userqueries is also a possibility. 

* User cannot change AUDSID. All triggers must explicitly test this.
* Check AUDSID is correct in state machine definer functions
* Audit failures and major events to separate trail
* Use NSYSLOG to log send syslog events to DB
* Use PG Foiune for log tracer analysis

### Security Enhancements

* Remove INSERT/UPDATE/DELETE permissions from t_ tables. This has been tested and works on Postgres. [DONE]
  This will be used to implement Fine grained access control (but not the auditing)
* Check CREATE FUNCTION system() LIBRARY libc.so ... cannot be run as a normal user
* Re-write state machine definer (ddl creating) functions so they query and create DDL they
  execute and do NOT call sub functions to execute. Current protections are
  not adequate.
* chroot() Postgres. Note this will need a C program to run InitDB/Postmaster
  etc and is very uncommon [UNLIKELY; requires further assessment]
* SET search_path in all definer (ddl creating) functions
* Log call and args in all definer (ddl creating) functions
* Java application errors must be handled by and then ROLLBACK called to 
  remove the risk of recursive SQL (i.e. running SQL injection attacks via 
  parse errors)
* Quote enclose all tables/view/column names throughout the PL/pgsql
* Use bind variables as much as possible during extract INSERT
* Use of proxy logons to run schema owner code; carry out privileged
  operations.
* Review and minimise function permissions (especially of internal functions)
* Detect Kerberos uses; disable (parameterised) non kerberos, non SSL users if required
* EXPLAIN PLAN is a potential security risk; needs to have the have SQL injection
  security controls as prepare/execute SQL

#### Security Enhancements - unresolved issues

* Proactive un detected SQL injection attack [i.e. kill session on detecting
  suspicious errors]. This could be done as part of pg_stat_statement audit
* Fine grained auditing. Not supported in Postgres. Could be implemented using a 
  VIEW with a sub query returning 1 (ignored) row that calls a loggging function or 
  as a dummy column calling a logging function or a per row basis. Could not access 
  the row actually fetched in sub queries as this would cause performance problems. 
  Really CREATE RULE needs extending to support FGA/FGAC.

### SAHSUland data Enhancements 

* SAHSLAND cancer, deaths, episodes; extend period to 1974-2010
* ICD9/10, ICD-O-1, HES oper codes, HES A+E
* Standardized populations
* Anonymise data by mapping OA2001 to (sahsuland) LEVEL4 grouped by carstairs 
  quintiles. Discard OA at random until 1 max per WARD, then discard to 50% at 
  random; aggregate the rest to LEVEL4 (intended to be a super "ward"). Name 
  levels 3 and 4 using gazettear. Build all tables using the same mapping; 
  destroy mapping after use.
* Add GID_<fgeolevel>, GID_ROWINDEX support (order by highest resolution study geolevel, event date, 
  postcode, record order number) and index 

### Remote Health Database Support

#### Oracle FDW (foreign data wrapper) port

* Try using a dblink direct cursor to extract data from remote databases, rather than created a 
  local copy of health data via a FDW select. Use async query so can call session longops
* Add parallelisation hints
* Add remote dbms_xplan/EXPLAIN PLAN support 
* [Auto?] map user remote tables into schema

#### SQL Server remote access port

TO BE DEFINED; expect to be the same method as Oracle FDW port

## Tools

### IG tool

To be defined

* Support for PHE approvals process; dataset permissions
* Create and manage users; manage projects and themes
* Manage FDW interconnect
* rif_user/manager runs study; rif_student needs permission
* Set by the Parameters extract_hold and run_hold - CSV list of roles to hld run and extract

### Dump data replacement

Dumpdata is a high speed data extract tool (originally an Oracle C/Pro*C program). It is written in Java 
so it can be called from the middleware. 

* RIF40_DMP_PKG
* Prototype CSV/TSV/PSV support works fine in Java (Javas slow it down by ~25%, PL/pgSQL by 4x)
  Uses a default multi row buffer size of 100 (i.e. batches up 100 rows into one row) to make 
  life easier for pure Java JDBC drivers
* Use Apache Posix java parameterisation library
* Could use RIF_ROW to remove need for cursor FOR loop (i.e. speed up PL/pgSQL)
* Needs to communicate number of data rows etc via listen()
* To support IG extract permissions; must have access to study or numerator tables
* PL/pgSQL to send % complete messages ever second (estimated from first block) via listen()
  This will need a worker/manager thread
* Projected parameters:

	--table/view/materialised_view=<object>
	--schema=<schema onwer>
	--dir=<spool directory>
	--debug=<level: 1-4>
	--format=<tab|pipe|comma|dbf>
	--test
		Test will do 2 passes (the first is to populate the buffer cache)
		The second will find the the optimal multi row buffer size starting from the 
		default and going down (then up if required) in halves until the optimal size
		is found. This will then be saved together with the object name, machine
		and average row length in a table. This will eventually be used to autotune
		multi row buffer size using linear regression (assuming it is linear - it 
                appears to be). May also need to understand FDW, have a has rif_row flag. 

* Shapefile support needs to be investigated (i.e. can you interface to the Postgis load/unload program)

### Load data

Loaddata is a high speed data load tool (originally an C program/shell script). It is written in Java 
so it can be called from the middleware. 

* Replacement for verdata/load_data.sh
* RIF40_VLD_PKG
* Similar to dumpdata

### RIF Batch

* Use pgbatch module (part of pgadminIII)
* Java program, calls PL/pgsql
* Create pgbatch role as a superuser, password in .pgpass
* Test pgbatch proxy logon as a Kerberos user. This postentially allows
  pgbatch to be started from the command line like Oracle TDE and run
  "forever" using the Kerberos keepalive service in the new Redhat and 
  remove the need for a .pgpass password file.

a) rif40_run_study()

* Use SET ROLE to change userid to study username and then call
  rif40_run_study(). This needs to use a full path to rif40_run_study() to prevent
  a code injection attach; rif40_run_study() needs to verify the osers owns the study
* Errors must be handled by being caught and then ROLLBACK called to remove the risk of
  recursive SQL (i.e. running SQL injection attacks via parse errors)
* N jobs per minute (N=1 by default). Minimum latency 1 minute. To 
  run multiple jobs the batch program will need to fork()/exec()
* When no jobs, run for 1 minute checking every 5 seconds (i.e. reduce latency to 5 seconds)

* SET SESSION AUTHORISATION and dblink was also considered, but it was decided
  to keep to the standard daemon secuirity model (i.e. become the user then
  exit) and remove the need to manage FDW server setup/password issues on non
  Oracle interconnected systems.

* Other batch jobs:
  - Vacuum and index rebuild (see below)
  - Hot backup
  - Database dump

* Java tool to a) list recently completed, running and queued studies 
  b) real time updates from the batch

## Testing

Schema verification (rif40_tables_and_columns, rif40_triggers)
PG tap (Unit testing)

## Database Administration

### Load Balancing

The new SAHSU Private Network RIF server will be replicated, and the replica is 
powerful enough to support load balancing.  The Java Middleware is intended to have 
4 workers per user connection; 1x writer and 3x readers. this is to parallelise web 
browser request servicing. The 3x readers can be load balanced as long a the replica 
is no more than a few seconds behind. Study submission and study running are intend to 
be single atomic transactions; so the issue is waiting for replication.

Investigate:

* pgpool-II/pgBouncer
* PL/Proxy (distribution)

This should provide excelent scaleability.

### Backup and Recovery

Use PG Barman for physical backups
pg_dump for logical
Use Slony replication (logical standby) in preference physical standby

### Vacuum and index rebuild

* Turn on auto vacuum
* Create batch job to rebuild indexes

## Alter scripts

### Alter 1

a) gid_rowindex (i.e 0000000001_0000000001). Where gid corresponds to gid in geometry table
   row_index is an incremental serial aggregated by gid ( starts from one for each gid). 
   GID is unique for the geolevel
b) Remove foreign key t_rif40_inv_geography_fk from t_rif40_investigations, NUll data, remove column
c) Add middleware support functions

Completed 1/7/2014

### Alter 2

a) Add covariates to comparision area extract
b) GID, GID_ROWINDEX support in extracts/maps
c) Make INV_1 INV_<inv_id> in results and results maps

