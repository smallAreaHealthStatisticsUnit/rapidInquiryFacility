# RIF database Deveopment Log

Peter Hambly.
Last update: 1/10/2015

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

## RIF development blocks

### RIF Block I

Aug-Sept 2014

FF - Rif manager, leaflet geoJSON performance issues, chart prototypes
KG - Java frontend/middleware, test cases
PH - Study extract to observed, triggers, GIS simplification

### RIF Block II

Was Dec-Jan 2015, then Mar-Apr 2015, now Jul-Aug 2015

FF - Rif manager database/middleware integration; RIF study creation
     prototype, charting
KG - Java frontend/middleware working demo, web services
PH - TopoJSON support, GIS enhancements, partitioning
     and related performance enhancements; database test harness
AL - R calculations from expected
MD - SQL server port to sahsuland

all completed.

#### Database work queue

This is work in progres

* R integration - calculate expected etc
* Partitioning and cluster support
* GIS enhancements (simplification, geolevel intersection)
* TopoJSON support

### RIF Block III (up to 2016; to be defined):

* Completion of R integration
* Full support for age/sex/age_sex_group)

Defferred:

* Remote health database support (Oracle only - to use data loader for the moment)
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

CAUSE: No parameter set in postgresql.conf [fixed by Postgres 9.3]

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

This is put on hold permanently; data will be copied in using the data loader.

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

Misc schema design changes:

a) gid_rowindex (i.e 0000000001_0000000001). Where gid corresponds to gid in geometry table
   row_index is an incremental serial aggregated by gid ( starts from one for each gid). 
   GID is unique for the geolevel
b) Remove foreign key t_rif40_inv_geography_fk from t_rif40_investigations, NULL data, remove column
c) Add middleware support functions

Completed 1/7/2014

### Alter 2

Misc data viewer changes:

a) Add covariates to comparision area extract
b) GID, GID_ROWINDEX support in extracts/maps
c) Make INV_1 INV_<inv_id> in results and results maps

### Alter 3

* Range partitioning (e.g. by year).

### Alter 4

* Hash partitioning (e.g. by study_id).

### Alter 5

* Zoomlevel support
* Rebuilds all geolevel tables with full partitioning (alter #3 support):

Done:

1. Convert to 4326 (WGS84 GPS projection) after simplification. Optimised geometry is 
   always in 4326.
2. Zoomlevel support. Optimised geometry is level 6, OPTIMISED_GEOMETRY_2 is level 8,
   OPTIMISED_GEOMETRY_3 is level 11; likewise OPTIMISED_GEOJSON (which will become a JOSN type). 
   TOPO_OPTIMISED_GEOJSON is removed.
3. Add t_rif40_sahsu_maptiles for zoomlevels 6, 8, 11, rif40_sahsu_maptiles for other zoomlevels.
4. Calculate the latitude of the middle of the total map bound; use this as the latitude
   in if40_geo_pkg.rif40_zoom_levels() for the correct m/pixel.
5. Partition t_rif40_sahsu_maptiles; convert partition to p_ naming convention, move to
    rif40_partitions schema, added indexes and constraints as required.
6. Convert rif40_get_geojson_tiles to use t_rif40_sahsu_maptiles tables.
7. Re-index partition indexes.
8. Add support for regionINLA.txt on a per study basis as rif40_GetAdjacencyMatrix().

<total area_id>
<area_id> <total (N)> <adjacent area 1> .. <adjacent area N>

9. populate_rif40_tiles() to correctly report rows inserted (using RETURNING); make
   more efficient; create EXPLAIN PLAN version.
10. Fix sahsuland projection (i.e. it is 27700; do the export using GDAL correctly).
11. Use Node.js topojson_convert.js GeoJSON to topoJSON conversion.  
12. Remove ST_SIMPLIFY_TOLERANCE from T_RIF40_GEOLEVELS; replace with m/pixel for zoomlevel.
13. Move all geospatial data to rif_data schema.
14. Map tiles build to warn if bounds of map at zoomlevel 6 exceeds 4x3 tiles.
15. Map tiles build  to fail if a zoomlevel 11 maptile(bound area: 19.6x19.4km) > 10% of the area bounded by the map; 
    i.e. the map is not projected correctly (as sahsuland was at one point). 
	There area 1024x as many tiles at 11 compared to 6; 10% implies there could be 1 tile at zoomlevel 8.
	This means that the Smallest geography supported is 3,804 km2 - about the size of Suffolk (1,489 square miles)
	so the Smallest US State (Rhode Island @4,002 square km) can be supported.
	
Not done:

16. Intersection to use shapefile SRID projection; after simplification to be tested against intersections 
    using zoomlevel 11.
	
### Alter 6

* PL/R support [Work in progress]

### Alter 7

Support for taxonomies/ontologies (e.g. ICD9, 10); removed previous table based support.
Modify t_rif40_inv_conditions to remove SQL injection risk

Done:

* rif40_outcomes - list of ontologies to remain - remove all field except for:
	outcome_type, outcome_description, current_version, current_sub_version, previous_version
* Add new outcome_group to rif40_outcome_groups for SAHUSLAND_CANCER
* Fix rif40_tables, rif40_table_outcomes, rif40_outcome_groups join for SAHUSLAND_CANCER
  - Add view: rif40_numerator_outcome_columns
  - Add checks: to rif40_inv_conditions, rif40_num_denom, rif40_num_denom_errors
* Drop existing ontology tables (keep icd9/10 until new ontology middleware is ready)
* Modify t_rif40_inv_conditions to remove SQL injection risk:
  - Rename column condition to min_condition
  - Add columns: max_condition, predefined_group_name, outcome_group_name
  - Add foreign key constraint on rif40_predefined_groups(predefined_group_name)
  - Add foreign key constraint on rif40_outcome_groups(outcome_group_name)
  - Add check constraints: 
    1. min_condition or predefined_group_name
    2. max_condition may be null, but if set != min_condition
* Rebuild rif40_inv_conditions:
  - Add back condition, derive from: min_condition, max_condition, predefined_group_name, outcome_group_name
  - Add numer_tab, field_name, column_exists and column_comments fields for enhanced information
 * Load new rif40_create_disease_mapping_example()
 * Load new rif40_startup() with ability to disable most checks for middleware testing
   Note: must be run once to create TEMPORARY TABLES
   
### Alter 8

* Database test harness
 
### Alter 9

* Alter 9: Misc integration fixes

  1. Replace old geosptial build code with new data loader. Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; 
     use rif40_geolevels lookup_table/tile_table
  2. Make RIF40_TABLES.THEME nullable for denominators
  3. INSERT INTO rif40_table_outcomes wrong OUTCOME_GROUP_NAME used in v4_0_postgres_sahsuland_imports.sql, suspect ICD hard coded. [Not a bug]
  4. Fix:
     * RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMNN_EXISTS to COLUMN_EXISTS
     * T_RIF40_CONTEXTUAL_STATS/RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION to TOTAL_COMPARISON_POPULATION
  5. Resolve: RIF40_PARAMETERS.DESCRIPTION (SQL Server) or PARAM_DESCRIPTION (Postgres)
  6. rif40_GetAdjacencyMatrix.sql: change ST_Touches() to ST_Intersects() to fix missing adjacencies caused by small slivers
  7. Add t_rif40_study_status/rif40_study_status
  8. Add stats_method to rif40_studies