# Peter Hambly Progress Report RIF 4.0

Principal Work Areas: **Postgres Database, node, system integration**

## 2015
### May
#### 10th-15 May

Alter script #7

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
 
#### 18th-22nd May

* rif40_GetMapAreas() fix - the following query works at LEVEL4 but returns NULL at other levels

```sql
SELECT SUBSTRING(rif40_xml_pkg.rif40_GetMapAreas(
			'SAHSU' 	/* Geography */, 
			'LEVEL3' 	/* geolevel view */, 
			55.5268097::REAL /* y_max */, -4.88653803 /* x_max */, 52.6875343 /* y_min */, -7.58829451 /* x_min - Bounding box - from cte */)::Text
				FROM 1 FOR 160) AS json 
LIMIT 4;
```

* topojson_convert.js - add name, gid and area_id as properties; validated link

#### 26th-29th May

Alter script #7 continued.

Clean up rif40_columns, rif40_tables_and_views, rif40_triggers. This is so the DDL tests now pass

Done:

* DDL test suite fixes:

	1. Tidy up RIF40_TABLES_AND_VIEWS table_or_view column, drop column TABLE_OR_VIEW_NAME_HREF, add primary key and check constraint
	2. Tidy up RIF40_COLUMNS, drop columns: TABLE_OR_VIEW_NAME_HREF, COLUMN_NAME_HREF; add primary and foreign key and check constraint 
	3. Remove existing ontology tables (keep icd9/10 until new ontology middleware is ready)
	4. Remove columns dropped from RIF40_OUTCOMES and T_RIF40_INV_CONDITIONS
	5. Add new view: RIF40_NUMERATOR_OUTCOME_COLUMNS 
	6. Add new columns for: RIF40_INV_CONDITIONS and T_RIF40_INV_CONDITIONS
    7. RIF40_OUTCOMES trigger removed from rif40_triggers
    8. Add primary and foreign keys to rif40_triggers
	
* Regression tested OK. Still expecting some problems on wpea-rif1. Still no support for old studies
* Improved make targets - made database aware
* RIF startup procedure: added application name support, no check mode (for middleware testing) 

### June

#### 1st-5th June

* Full regression test of database build
* Port to MacOS, regression test
* Install Tomcat 8 on Windows, MacOS, Tomcat 6 on Redhat Enterprise Linux 5. Checked Tomcat 6 will work.
* Started work on unified build using make in rifBuild tree
* Tested Middleware WAR build from github, dependency issues found 

#### 8th-12th June

* Middleware WAR build now OK (mainly thanks to Kev)
* Integrated with make using Powershell on Windows
* Install.ps1 created that gains the elevated privileges required. Install RIF middleware and test WAR unpack.

#### 15th-19th June

* Simple test harness for SELECT and INSERT/UPDATE/DELETE. Select uses arrays to check return data; exceptions are verified.
* Improve exception handlers and error messages:

Error context and message >>>
Message:  rif40_sql_pkg.rif40_sql_test(): Test case: TRIGGER TEST #2: rif40_studies.suppression_value IS NULL FAILED, invalid statement type: %%SQL> INSERT;
Hint:
Detail:   -71065
Context:  SQL statement "SELECT rif40_log_pkg.rif40_error(-71065, 'rif40_sql_test',
                        'Test case: % FAILED, invalid statement type: %%SQL> %;',
                        test_case_title::VARCHAR, UPPER(SUBSTRING(LTRIM(test_stmt) FROM 1 FOR 6))::VARCHAR, E'\n'::VARCHAR, test_stmt::VARCHAR)"
PL/pgSQL function rif40_sql_test(character varying,character varying,anyarray,character varying,boolean) line 396 at PERFORM

i.e. internal error code retuned in detail

#### 22th-26th June

Immediate TODO list: 

* Add control table (rif40_test_harness) for test harness, _rif40_test_sql_template() function to simplify creation, add common datatype shareable with SQL server:
  XML; SQL server does not understand arrays)
* Trigger test harness: RIF40_COVARIATES, RIF40_ERROR_MESSAGES, RIF40_GEOGRAPHIES, RIF40_PREDEFINED_GROUPS, RIF40_STUDY_SHARES, RIF40_TABLE_OUTCOMES,
  RIF40_TABLES, RIF40_VERSION, T_RIF40_COMPARISON_AREAS, T_RIF40_CONTEXTUAL_STATS, T_RIF40_GEOLEVELS, T_RIF40_INV_CONDITIONS, T_RIF40_INV_COVARIATES,
  T_RIF40_INVESTIGATIONS, T_RIF40_NUM_DENOM, T_RIF40_RESULTS, T_RIF40_STUDIES, T_RIF40_STUDY_AREAS, T_RIF40_USER_PROJECTS 
* Modify alter 7 so that existing conditions become rif40_error(...); /* Existing condition */ e.g.

SELECT 1 AS x
 WHERE 1 = rif40_log_pkg.rif40_error(-90125, 'rif40_error', 'Dummy error: %', 'Test'::VARCHAR);

or

SELECT rif40_log_pkg.rif40_error(-90125, 'rif40_error', 'Dummy error: %', 'Test'::VARCHAR) AS x;

Raises an exception!

#### 29th June-3rd July

No work on the RIF40_COLUMNS

### July

#### 5th July-10th July

* RIF covariates example data 
* Specification for individual level SAHSULAND_CANCER example data

#### 13th - 14th July

* Test harness tables (alter_8.sql)

#### 15th July - 31st July

* Caving in Slovenia: https://en.wikipedia.org/wiki/Migovec_System 
  and: https://www.union.ic.ac.uk/rcc/caving/slovenia/intro/slov_intro.php 

### August
  
#### 3rd - 7th August 

Created test harness test data (to test the functionality); the basic principle is that each test 
(or series of linked tests) is a single transaction. This is not possible in Plpgsql; so needs
one of:

a) dblink (tested, works, no support for debugging or output capture;
b) Foreign data wrappers, no support for output capture;
c) Java, considered, choose;
d) Node.js to avoid confusion with middleware

#### 10th to 14th August
  
Convert test data from Postgres array to XML format. 

#### 17th to 21st August

New node based test harness

* Support for Postgres debugging
* Replace dblink test harness runner with Node.js version so that debugging and output can be controlled.

#### 24th to 28th August

* CDC teleconference
* Farr conference in St Andrews [IG]

### September

#### 1st to 4th September

Note: to convert ESRI proj files to PostGIS SRIDs use: http://prj2epsg.org/search

Test harness refactor; Node.js version working
  
* EXPLAIN plan support
* Linked tests (based on the study 1 test scenario)
  
#### 7th to 11th September 

* Debug functions
* Instrumnentation
* Conversion to Mutex locked version to prevent stack overload with waterfall of cascading callback functions

#### 14th to 18th September

#### 21st to 25th September

* Regression test build
* User setup documentation
* Support in test harness for anonymous PL/pgSQL
* State machine verification tests
* Timestamps in test machine output connection trace

#### 28th September to 2nd October

* Build documentation
* Small fixes to build (e.g. ::1 localhost fix, Node make fixes), regression tests complete opn Windows and Linux

### October

#### 5th to 9th October

* Test harness documentation

#### 12th to 16th October

* Remove old dblink Test harness

#### 19th to 23rd October

* Complete build instructions 

#### 26th October to 6th November

* Complete and test partitioning

### November
 
#### 9th November to 21st November

* Testing confirmed susecpted double execution of triggers. Triggers were disabled at the master level [This code did not work,
  to be fixed with trigger tune up]
* Sequence NEXTVAL is only calling once for master and partition; the lack of test 1 is caused by a logic fault in alter 4. Fixed
* Add check constraints for partition value; this was not needed as done already

#### 23rd to 27th November

* Clean build on Windows and Linux; re-patch now OK with partitions. Some issues with Windows 8.1 breaking Node.js; Node.js makefiles 
  need to be able to install/update as appropriate

## 2016
  
### January  
 
#### 8th to 15th January
  
* Port to Postgres 9.5;
* RIF Node Services - build enviornment;
* RIF Node Services - topoJSON convertor;

  * Restructure of code;
  * Convert to test_6_sahsu_4_level4_0_0_0.js (level 4, zoomnlevel 0 sahusland example, 3.6M);
  * Convert to support form fields as parameteres and return;
  * Return debug output from topoJSON module;

#### 18th January to 5th February
	
* Compressed attachments using zlib; multi attachment support; browser verification

### February
	
#### 8th to 17th February

* Error handlers, busboy limit handlers, failure cases:
  * GeoJSON syntax issues
  * Failures in file stream
  * General syntax errors
* Support properties, property-transform and my_id topoJSON module options;
* Security as per recommendations - added Helmet. Helmet helps you secure your Express apps by setting various HTTP headers. It's not a silver bullet, but it can help;
* Full client logging;
* Instrumentation;
* Size limits (100M); 100 files - this is be BusBoy processing exception filesLimit/fieldsLimit/partsLimit; 
	
#### 19th to 25th February

Analysis of current geospatial presentation, design meeting with Kev. Principle decision - to build a self contained Node 
service (NodeGeoSpatialServices). 

NodeGeoSpatialServices:

NodeGeoSpatialServices integrates a number of Node modules as web services to provide remote web services to:

* Convert shapefiles to GeoJSON;
* Validate, clean and simplify converted shapefile data suitable for use in maptiles;
* Convert GeoJSON to a) TopoJSON and b) Well known text;
* Create hierarchy tables;
* Create maptiles;
* Create polygonal and population weighted centroids.

NodeGeoSpatialServices will require PostGres and PostGIS in the server for access to OGSS functionality. It will also be 
designed to be portable to SQL server.

The inputs to NodeGeoSpatialServices will be one or more shapefiles; the outputs a set of maptiles and a set of  
portable geospatial data.

The NodeGeoSpatialServices client would normally store data either in flat files (e.g. maptiles) or a GeoSpatial database
(e.g. PostGIS). The client is not required to carry out any additional geosptial processing. Since multiple processing 
steps are envisaged thence the server will need to maintain state. It will do this by saving data as GeoJson in a file or in 
a PostGIS table. This will reduce the data be transferred to a minimum.

It is not intended at present to support:

* Shapefile formats not supported by Node SHP/MapShaper;
* To convert a) TopoJSON and b) Well known text to GeoJSON;
* To convert a) TopoJSON or b) GeoSON to shapefiles.

This is technically possible, but requires GDAL.

Population weighted centroids requires population data and additional non standard processing (i.e. there is no OGSS 
standard function) in PostGIS.

NodeGeoSpatialServices should not be confused with GeoNode (http://geonode.org/)

GeoNode is a platform for the management and publication of geospatial data. It brings together mature and stable open-source 
software projects under a consistent and easy-to-use interface allowing users, with little training, to quickly and easily 
share data and create interactive maps.	

GeoNode is essentially Open Google Earth/Maps with an emphasis on accessibility via easy to use interfaces and good metadata.

### March

#### 28th February to 11th March
	
* Code refactor for multiple services;
* shp2GeoJSON service;
* Portable make_bighwelloworld.js script;
* Async file save;
* UUID generation;
* Shapefile to geoJSON converion;
* Project planning
	
#### 14th to 18th March	
	
* shp2GeoJSON.html HTML 5 browser tester; works in Chrome and firefox.  1.3G coa2011 shapefile cracks chrome; also breaks 
  the synchronous file write in Node. Code needs restructuring to be fully async with streaming everywhere.
* Leaflet display of returned data
* Basic projection support (converted to 4326 for leaflet)

#### 21st to 24th March

* Multiple shapefile support (using Async module)
* Full leaflet support
* UI tidy; cross browser fix. Decided to use JQuery-UI going forwards
	
#### 26th March to 1st April
	
* Rename shp2geojson to shpConvert, remove store parameter
* Geeolevel detection: numbers of areas, names of fields 
* Async write support: shpConvertFileProcessor(), shpConvertWriteFile() (still to test on coa2011 - times out). 
  Also added file size checks after coa2011 tests. Hilarious bug where I was writing out ther whole buffer rather 
  than a 1MB chunk tried to create a 1TB coa2011 shapefile. Desktop very unhappy; laptop with SSDs filled them up very quickly 
  but without distress...
	  
### April 
	
#### 4th to 8th April 	

* Completed garbage collection tracing and improvements to reduce memory footprint
* Created shapefile reader function. Reads shapefile record by record; converting to WGS84 if required to minimise meory footprint
* Added check that all bounding boxes are the same
* Tested 1.1G coa2011 shapefile on 8G laptop - ran out of memory at around 62,000 records (out of 181,408). Json size = 273,018,806; 
  total available size = 1,486,054,944. Although uses process.nextTick() it is recursive; could rewrite as async queue to reduce 
  memory usage. Will add 8GB more RAM. Memory from file processing stage has been released.
* Test with more memory: --max-old-space-size==8192 (8GB); ran out at 206,000 records, JSON: 1,509,263,516 (file must be UK not England!) 
* Tests showed that:
	* shpcat can convert coa2011 to json; it puts lines in it (so it can be parsed by record) but this breaks JSON.parse();
	* topojson runs out of memory (works in the samne way as shpConvert;
	* geo2topojson cannot parse >1G because Buffer.toString() fails, likewise for attempts to remove linefeeds; needs fixing using json stream;
	This is to see if shpConvert is better writing to a file and then reading in the file; it probably is *not*.
* Add XML configuration using xml2js
* Check that minimum resolution shapefile has only 1 area  
* Move shpConvert.html to Node express static server; make so does not need network, and use relative paths. Also needed for IE
		
#### 11th to 15th April

* Read coa2011.shp: 227,759 recoerds, 203,930,998 points. Required 11G memory; broke shpConvertWriteFile() as expected, 
  needs to write in blocks; JSON size: 1,666,421,470 ~ 16GB. shpConvertWriteFile() needs to write in sections (i.e. per record)
  max 10.2G memory (11G limit on laptop)
	  
```	  
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:565
						shpConvertWriteFile(shapefileData["jsonFileName"], JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson), 
						                                                        ^
RangeError: Invalid string length
    at Object.stringify (native)
    at C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:565:63
    at FSReqWrap.oncomplete (fs.js:82:15)
```
	
* Added CRLF removal support. coa2011.js (from Mike Bostock shapefile to JSON program) now fails converting to a string; 
  a streaming parser is needed; Strings (not buffers) limited to 256M (-1 byte)! This is causing much grief and will probably be fixed soon; 
  not fixed in Node 5.10; buffers are good for at least 1.5G.
* Added topoJSON support, with a quick bodge sterians calulation for zoomlevel 9 (1 pixel = 300m x 300m = 1.451e-11 steradians 
  This takes no account of latitude; will calculate accurately using PostGIS rif40_geo_pkg.rif40_zoom_levels() function 
  Current US map is 1:500,000 and is OK at zoomlevel 11; zoomlevel 9 is 1:1 million; JSON compression is 5.06MB compared to: 28.55MB
  Quantization set to 1e6; 1e4 was far too coarse; 83% compression.
* SAHSUland works in IE, firefox and Chrome; IE is very slow. US to counties works in Chrome and IE. UK census output areas will only work in Firefox
		
#### 20th April - Judy Qualters - CDC Visit

* Demo of shpConvert
		
#### 25th to 29th April
	
* Large file support (coa2011) - shpConvertWriteFile() needs to write in sections (i.e. per record), remove geoJSON (this will help IE)
	
### May

#### 3rd to 6th May

* Code refactor, optimization, instrumentation
	
#### 9th to 13th May
	
* Control of data delete on stream write
* Scopechecker exception tester; test all async queues; to top level 
* Moved diagnostics to top level
* Top level refactor
* Status trace
* Save final response
* Merge responseProcessing
* Added complete set of SRIDs
		
#### 16th to 20th May

* Add more status
* Data loader documentation
* CDC webinar
* Single log entry per run
* Zip support: compressed file processing moved to an async loop in req.busboy.on('finish'). This will make the input file processing fully async.
* Convert zlib file support to async
* Test JSZip with async loop; tested OK with Node buffer and large files. Documentation is particularily rubbish for such a well used module
* Confirmed JSZip is still on 2.6; will wait for 3.0 for full async code; current code is synchronous!!!!
		
#### 23rd to 27th May

* Zip file support using JSZip implemented using 2.6 interface; minimal changes for 3.0
* geotoTopoJSON.html converted to use Ajax POST; common front end code with ShpConvert.html
* Jquery.form.js http://malsup.com/jquery/form/ ajaxForm POST appears to have fixed memory crash problems in Chrome with very large files
* Fixed json parser in geo2topoJSON to handle files > 255M; parsed 1,674,722,608 bytes to JSON; topoSJON: 73,988,023 bytes; Chrome died, 
  firefox hung (probably because of the swapping). Really needs batch mode because Node will not release the meory before sending the 
  response back to the client. Node NODE_MAX_MEMORY=7168 (MB!)
* Simplify: test pre-quantisation at 10e6/10e7 with and without simplification to 9.01x10-13 steradians. 	
* Zoomlevel 1-11 support; topojson now becomes an array, single topoJSON function. Notes: a) simplify-proportion not working; b) performance 
  issues because of this; c) being run on too many files in geo2TopoJSON. 
* Create geo2TopoJSON.geo2TopoJSON()

#### 31st May to 3rd June

* Async map loading, full screen. Still not displaying layer by layer
* Front end multi zoomlevel support. Old layers being left behind, over simplification (as expected)
* Async map display
* Add intra zoomlevel simplify percentage target parameter: simplificationFactor
* Old layers being left behind
* max/min zoomlevel. Max zoomlevel not enforced in browser until the calucation of quantization and the max zoomnlevel is better; this needs area,

#### 6th to 10th June

* Refactored front end code so there is no duplicate code
* Potential fix for Firefox ajaxForm size issues
* Quantization and simplification factor now working. Arcs and points added to topojson object in convertedTopojson array
* Perormance improvements
* Common CSS
* Add legend, fixed timings
* Firefox bug traced to: Bug 1274010 - xmlhttprequest post randomly failing - https://bugzilla.mozilla.org/show_bug.cgi?id=1274010
  Do not use Firefox 47.0. ^ hours to trace and clear Ajax.
* Added file list load and unzip (start of support for XML config file)

#### 13th to 17th June

* Setup EBS progress for team
* Data loader design documentation
* Reserved tile-maker for Node.js geo spatial services  
* Replaced traditional html with JQuery-UI; added unzip and accordion panels for sorting out 

#### 20th to 24th June

* Completed accordion panel; parsing DBF files for the fields 
* Front end zip progress
* Tooltips
* Area_id and description support using .SHP.EA.ISO.XML 

#### 27th June to 1st July

* RIF progress meeting via Skype
* RIF progress meeting with CDC
* Project planning for July to December
* Java SSL integration

#### 4th to 8th July
 
* Project planning for July to December
* Set geolevel_name, area_id, area_name from front end
* Add all fields to topojson

#### 11th to 15th July

* Project management for no cost extension
* Add geography name, description to form, field processing and XML file
* Common progrss bar
* Replaced area count with area description in legend

#### 18th to 22nd July

* Status update using uuidV1; batch mode (returns in onBusboyFinish())
* Convert toTopoJSONZoomlevels() for loops to async
* Add addStatus() callbacks, tidy messages
* Colurs wrong in legend

#### 25th to 29th July

* geojson to wellknown text conversion; added framework for processing from intermediare point to end
* Batch mode now complete; firefox and chrome are OK. IE has intermittent problems

#### 1st to 5th August

* XML and ZIP download buttons; only XML method complete.
* Resizing much better (i.e. map stays within the viewport); much work still to do (deffered to later) 
* IE problems only when NOT in developer mode. This is related to a) console.log has been wrapped and
  b) Ajax get() caching. Disabling Ajax get caching did not work; added an lstart parameter (so each call is different)
  did
* CDC monthly report and project planning

#### 8th to 12th August  
  
* De-duplication processing, closed polygon loops if needed
* Improved error handling
* RIF meeting
* Added area in square KM and geographic centroid to data
* Detect duplicate area name; can be a warning
* ST_Union and area calculations were done geoJSON using turf: as it is a geometry collection. 
* WKT support using Wellknown
* Id generator; gid support (especially in topojson)

#### 15th to 19th August  

* Eurospeleo 2016
* CSV file save 
* Load to Postgres
* Check area calculation - it is wrong for Conneticut: 14,357 km2 verses 12941.42 Javascript, 12932.58 PostGIS
  Florida 139,760.29 verses: 151512.42 Javascript 151029.30 PostGIS
  Texas 678,051.12 verses: 689860.28 Javascript 688466.71 PostGIS. This appears to be a projection issue as I have used UTM zones
  Likewise centroids: Alabama 32.834722, -86.633333; -86.7665,31.7691 Javascript -86.8284,32.7898 PostGIS; 19.37km out for PostGIS
* area_id and gid uniqueness tests to shapefile checks and tests added to SQL load script. Area name will need to be unqiue 
  within the confines on the next lower resolution layer

#### 22nd to 26th August  

* Auto generate Postgres and MS SQL server scripts; both work OK
* SQL Servers lacks ST_Transform() so geom_orig cannot be set (not used by RIF)
* Confirmed areas and centroids are the same in the US SRID projection

#### 30th August to 2nd September

* Area caclulation problem with SQL Server confirmed as being caused by ring orientation (Postgis does not care!), and can be fixed. 
  It appears it can only be detected when the SQL Server calculated area is 1000s times more than the Turf area. Detection threshold 
  set at 200%; areas will need to agree to within 5% at the maximum geolevel, ignoring small areas <= 10 km2.
* Rose Island, Charleston has zero area at geolevel 6. 38° 25′ 49.69″ N, 85° 36′ 56.06″ W; 38.430468, -85.615571. This is indicative 
  of oversimplification; but is not a problem at level 6.
* Postgres is more accurate and will accept 1% error (Rose Island has a real area of 0.1035 km2 in Postgres).
* SQL load script generator for Postgres and SQL Server:
  * Load shapefile derived CSV files;
  * Convert well known text to geometry;
  * Fix and validate geometry data, make all polygons right handed;
  * Test Turf and DB areas agree to within 1% (3% for SQL Server);
  * Spatially index;
  * Add geography, geolevels meta data;
  
#### 5th to 9th September

* SQL load script generator:
  * Lookup tables; 
  * Hierarchy table - geolevel intersction generator (DB version);
  * SQL Server version needed to be rewritten to use global temporary tables because a) SQL Server always
    unnests common table expressions (WITH clause) b) cannot be hinted to not do this and c) you have to
	use global temporary tables because dynamic SQL is in a different logon context to the script T-SQL
  * Hierarchy checks (as for Postgres)
 
#### 12th to 16th September
 
* Fixed Postgres study creation bugs:
  
  * INV_1 hard coded as extract table investigate name (should be rif40_investigations.inv_name);
  * Missing primary key index on Postgres s<study_id>_extract;
  * Missing GRANT UPDATE on map table (e.g s1_map);
  * Check node is setup correctly (node_check target;
  * Simple enable debug function for rif40_run_study;
  * Database load code restructure to use template SQL scripts;
  * Band id bug (causing map map PK to fail). Area id added via update so it is clear where the problem lies;
* Refactor SQL code using template SQL files where possible; 
* Introduction SQL Server database creation scripts with MD;

#### 19th to 23rd September

* Port Latitude/Longitude to tile number and vice versa dfunctions from Postgres to SQL Server; confirmed results are the same;
* SQL Server port testing - building of SAHSULAND:
  * DB creation; user creation scripts slighty modified
  * There is an issue if you have already created and Windows database user; you need to grant access to sahsuland_dev; however 
    how you do this is very unobvious and hard to find in the documentation.  I used SQL Server Management Studio to do this (so 
	you obviously can!), but forget to get it to show me the SQL command;
  * Rebuilt sequences and tables scripts (and later all other install SQL scripts) so they are:
    a) Run from one script
    b) Are transactional
    c) Stop on error
    d) Table creation order is correct (i.e. referenced objects must exist)
    e) Recreates OK
    I suspect their will be more problems if there is data in the tables;
  * Added functions and views. The function rif40_sequence_current_value() is created earlier by the sequences SQL script and 
    cannot be recreated once tables have been created;
  * Added log error handling, table and view triggers; data load tables and data load for SAHUSLAND data; 
  * Removed [Postgres] foreign data wrapper (FDW) tables from SQL Server as obsolete ;
  * Single script to (re-)create sahsuland_dev database objects and install data: rif40_sahsuland_dev_install.bat/
    rif40_sahsuland_dev_install.sql
  * Report on SQL Server and Postgres RIF40 database table and column differences

#### 26th to 30th September

* Prototyped tile manufacture using Node.js and Turf.
  * Simplified geojson contains self intersection and side location conflict errors and needs to be made valid. There is 
    no ST_MakeValid() for Turf and the kinks() function is not sufficent;
  * The lack of a spatial index makes intersects (i.e. ST_Intersects) slow;
  * ST_Intersction() or clipping functions are not available to chop off the surplus outside the tile;
  * There is no ST_Contains() etc to detect if any of the boundaries are within the tile;  
  * The was a clear need for an algorithm which excluded tiles where the parent (next lower zoomlevel) did not intersect;
  * To easy way to crete PNG tiles; SVG was possible;
  * Concluded it would have t o be done on the database as in the original Postgres prototype.
  
#### 3rd to 7th October  

* Prototyped tile manufacture using PostGIS:
  * New efficient tile intersect algorithm saving up to 94% of tiles:
  
| zoomlevel | xmin | ymin | xmax | ymax | possible tiles | tiles | % saving |
|-----------|------|------|------|------|----------------|-------|----------|
|         0 |    0 |    0 |    0 |    0 |              1 |     1 |     0.00 |
|         1 |    0 |    0 |    1 |    1 |              4 |     3 |    25.00 |
|         2 |    0 |    0 |    3 |    2 |             12 |     5 |    58.33 |
|         3 |    0 |    1 |    7 |    4 |             32 |    10 |    68.75 |
|         4 |    0 |    3 |   15 |    8 |             96 |    22 |    77.08 |
|         5 |    0 |    6 |   31 |   17 |            384 |    46 |    88.02 |
|         6 |    0 |   13 |   63 |   29 |           1088 |   112 |    89.71 |
|         7 |    0 |   27 |  127 |   59 |           4224 |   338 |    92.00 |
|         8 |    0 |   54 |  255 |  118 |          16640 |  1139 |    93.16 |
|         9 |    1 |  108 |  511 |  237 |          66430 |  4093 |    93.84 |
|        10 |    2 |  217 | 1023 |  474 |         263676 | 15308 |    94.19 |
|        11 |    4 |  435 | 2046 |  948 |        1050102 | 58968 |    94.38 |
		
  * Re-wrote functions to be simpler (for SQL Server porting) and provide running updates;
  * Tile intersection (i.e. adding data, cropping to tile boundary) is time expensive but 
    geolevel 1 takes 210s to level 9 (to level 11 ~20x longer, estimated at: 70 minutes); to 
	US county level will be several hours!
  * Architecture will be as in the prototype: SQL script and a Node.js tile creation script which will:
    * Convert geoJSON/(Well known text for SQL Server) to topoJSON;
    * PNG tile dump to files (Postgres only - no raster support in SQL Server);	
  * Image of US outline tiles at zoomlevel 8: 
    ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Screenshots/US Outline.png "US outline tiles at zoomlevel 8")

#### 10th to 15th October
 
* Postgres tile maker:
  * Concluded that the old way of creating logical tiles was the best. Any trimming of PNG tiles must be done in SVG;
  * The problem with trimming using PostGIS ST_INtersection is shown in this image of US county tiles at zoomlevel 7 in Florida: 
    ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Screenshots/Florida2.png "US county tiles at zoomlevel 11 in Florida")
	Note the bounding box has been merged into the tile; so whilst it is possible to remove the "at sea" portion of the bounding box, 
	the "landside" portion effectively divides counties which will cause all sorts of problems to the front end;
  * Added within optimisation: by excluding any tile bounding completely within the area;
  * Fix to insert any missing area ids if possible (i.e. have non empty geometry). This is caused by small areas, usually islands,
    being simplified out of existance at a lower zoomlevel; 
  * Optimsed code to use older, simpler algorithm. Need to test index usage and add partitioning;
  * Tested to zoomlevel 11 in 2 1/2 hours;
  * It would be possible to speed up the process by ~25% if zoomlevel 1 is assumed to contain one area;
  * Image of US county tiles at zoomlevel 11 in Florida: 
    ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Screenshots/Florida4.png "US county tiles at zoomlevel 11 in Florida")
	This clearly exhibits all the alogorithmic optimisations:
	* No tiles where the entire tile contains no part of the US landmass;
	* No tiles where the area boundary is outside of the tile

#### 17th to 21st October

* Add max zoomlevel to UI to speed up demos and testing;  
* SQL Server tile maker:
  * Ported tiles table and view, geometry table, tile limits table:
  * Partition geometry tables (Postgres only)
* RIF team meeting;
	
#### 24th to 28th October

* SQL Server tile maker: tile intersects complete. Still unhappy about the performance; also second part of insert 
  (Insert tile area id intersections missing where not in the previous layer; 
  this is usually due to it being simplified out of existance) is not working.
* One table partition per geolevel; add tile table to geography; and schema
* Resolved SQL Server use of geography verses geometry and SQL Server geometry to geography casting; 
* Found status bug in UI (race in last two status updates, BATCH_END is missing). Need to find non async status update near the end...
  
#### 31st October to 4th November

* Fix for SQL Server QGIS (add geometry_columns table) as fix #8525 does not work (half works!) - add geometry_columns (PostGIS control table)
* SQL Server map tiles mssing a few at all levels. Tiles are in the database and are valid, appears to be a bug with QGIS which is complaining of broken polygons:
```Exception: IllegalArgumentException: Invalid number of points in LinearRing found 3 - must be 0 or >= 4;```
  * Technically these are triangles, and will be small offshore islands that have been oversimplified (and almost certainly invisible at this scale);
  * Highlights the problems of standards for GIS, QGIS uses the same library as Postgres/PostGIS (GeOS); SQL Server is more relaxed;
  * Should convert to topoJSON fine;
* Tile intersection (i.e. adding data, cropping to tile boundary) is time expensive but acceptable to US county level takes 90 minutes in PostGIS!
  
| Zoomlevel | PostGIS  | SQL Server |
| ----------| ---------|------------|
|         7 | 75 secs  | 393 secs   |
|         8 | 166 secs | 27 mins    |
|         9 | 8 mins   |            |
|        10 | 24 mins  |            |  
|        11 | 80 mins  |            |

  * SQL Server requires more tuning! After a good tune:
  
| Zoomlevel | PostGIS  | SQL Server |
| ----------| ---------|------------|
|         7 | 75 secs  | 51 secs    |
|         8 | 166 secs | 143 secs   |
|         9 | 8 mins   | 8 mins     |
|        10 | 24 mins  |            |  
|        11 | 80 mins  |            |

  * Postgres also aggregates GeoJSON into collections and still has the older NOT EXISTS code to eliminate tiles with no parent
* Added Wellknown text output to tile intersects table for topoJSON conversion program
 
#### 7th to 11th November

* Map tile generator prototype; topoJSON tiles and SVG tile creation

#### 14th to 18th November

* Map tile generator: connect to SQL server
* XML configuration file support

#### 21st to 25th November

* RIF team meeting, project planning, SQL server tile maker porting
* RIF project meeting, fix extract bug
* SQL server tile maker bulk insert

#### 28th November to 2nd December

* Comiment geielvel data tables
* Add support for description on DBF fields to XML config
* Added logging to tile maker

#### 5th to 9th December

* Project meetings;
* Empty database for dataloader (sahsuland_empty);
* Data loading integration;
* Rationalize geoDataLoader.xml with parameter and data loader sections, remove duplicates

#### 12th to 16th December

* Add remaining geography, geolevels fields
* Export tiles and geometry tables
* sahsuland_empty improvements, install for KG
* geoDataLoader.xml support in front end
* Removal of hard coded table names
* tile maker - add parameter to vary number of tiles processed at once (default: 10). This is used to control memory

#### 19th to 23rd December

* Created logger object, got winston to work properly; added file/line/function, overall timing
* Refactor dbLoad.js for production load script

#### 28th to 30th December

* Map tile generator; RIF integration preparation
* dbLoad.js production load script: Postgres
    * DELETE/INSERT rif40_geographies/geolevels
	* Add tile table to geolevels;
* dbLoad.js production load script: SQL Server - set schema in production script to cope with no ability 
  to change the schema on a per session basis

#### 3rd to 6th January

* Complete dbLoad.js production load script: SQL Server;
* Regression tests.
* Build times (desktop):

| Test dataset        | Web Front End | Zoomlevel | Postgres DB Build  | MS SQL Server DB Build | Postgres Tile Build | SQL Server tile build |
|---------------------|---------------|-----------|--------------------|------------------------|---------------------|-----------------------|
| USA to County level | 113.8s        | 11        | Tiles: 1h:27, 4:07 |                        | 5290 tiles in 2h:48 |                       |
| SAHSULand           |               | 11        |                    |                        |                     |                       |
 
* Build times (laptop):

| Test dataset        | Web Front End | Zoomlevel | Postgres DB Build  | MS SQL Server DB Build | Postgres Tile Build | SQL Server tile build |
|---------------------|---------------|-----------|--------------------|------------------------|---------------------|-----------------------|
| USA to County level | 113.8s        | 11        | Tiles: 1h:27, 4:07 |                        | 5290 tiles in 2h:48 |                       |
| SAHSULand           | 18.4s         | 11        |                    |                        |                     |                       |
  
#### 9th to 13th January

* SAHSULAND tests; fixes for numgeolevels, precision fuuzy match in proj4 data; event race, PG schema issues; 
  removed optimized_geojson from tiles table
* Installed new sahsuland geometry, tiles etc.
* Grants
* RIF and non RIF path fixes for Postgres

#### 16th to 20th January

* Race fixes in block code
* Check tiles all generated and not null, no extra, none missing 
* Zoomlevel and geolevel report (null tiles/total tiles)
* USA tests to zoomlevel 8 OK. Improved SQL Server tile making efficency and reduced table size to get under 10K limit
* Fix SQL server load script etc
* Integration to sahsuland_empty build. DO NOT RUN ALTER SCRIPTS OR REBUILD SAHSULAND UNTIL FULL STACK TESTING IS COMPLETE.
  Notes:
  * v4_0_alter_5.sql had to be extensively modified to:
    a) reflect changes from: t_rif40_sahsu_geometry to: geometry_sahsuland. shapefile_geometry is obsoleted and mulitple 
	   zoomlevels are supported. All data is in 4326. In particular area_id has been deliberately changed to areaid to cause 
	   parse errors!
	b) areaid_count added to rif40_geolevels/t_rif40_geolevels
	c) _rif40_getGeoLevelExtentCommon() to support rif40_geography.geometrytable and zoomlevels; pre tileMaker support retained
	d) rif40_GetMapAreas.sql() to support rif40_geography.geometrytable and zoomlevels; fiz for old sahsuland hard coding; pre 
	   tileMaker support retained
  * Tiles view and table compared to previous; after slight index and view tune efficeny as before; no missing/extra tiles
  * The adajacency matrix function needs to be checked.
  * Check json format in tiles tables. New tiles have a BBOX! Changed for full compatibility: 
	  id to gid,
	  areaID to area_id,
	  areaName to name
	The default names are not quite the same: "Kozniewska LEVEL4(01.013.016800.3)" as opposed to "01.013.016800.3"
  * Added GID to lookup tables
  
#### 23rd to 27th January
  
* Converted remaining rif40_xml_pkg functions to support tilemaker table names 
* Allow non study or health data related test scripts (1, 2, 3 and 6) to run on sahusland_empty
* Found bug in MS SQL hierarchy table: row numbers are the same but the smaller areas are being picked.
* Separate Postgres and SQL server tiles, hierarchy and lookup CSV files (so they can be compared)
* Lookup files are the same, and ordered
* Added comment to Postgres geometry partition
* Fixed SQL Server heirarchy bug. Caused by geography datatype. Fixed geom_orig to be geometry datatype and used that. Also  
  ordered hierarchy CSV files. Postgres and SQL Server hierarchy and lookup tables now exactly the same; and agree with old PostGIS
  tile build. Regression tests OK.
* QGIS integration for SQL Server: ..\rifDatabase\GeospatialData\create_mssql_geometry_columns.sql. Note that the original sp_executesql()
  behaved differently with -E (windows authentication) to -U/P (username/password authentication). Solution is to use sp_executesql() if later SQL 
  needs access to variables. This is an execution context related issue which has yet to be explained to me. (per tx temporary tables behave the 
  same way). This has the potential to case bugs in the SQL Server scripts if you run the build phase not as a Windows authenticated user.
* MS Sahsuland projection problem; parked: see below.

#### 30th January to 3rd February 

* TileViewer (tile-viewer.html) web screen by DB/geography; DB web service. Currently slow because using geoJSON.
  Both SQL server and Postgres tiles display for both SAHSULAND and USA to county level.
* Note resizing bug in tile-maker.html is probably caused by setting the height of the map div in html once 
  leaflet is initialized.
* All tiles confirmed OK
* Converted to topoJSON/Leaflet 1.0.3
* Removed spurious points (using tile-viewer)
* Made some lovely slides for next week

#### 6th to 10th February 

* CDC Visit;
* Remove spurious points using tilemaker. These are caused by the simplification; remove block attribute;
* TopoJSON support (Needs Leaflet gridlayer, Leaflet 1.0+ upgrade, leaflet-geojson-gridlayer);
* Specify Java middleware;
* Porting notes for dataLoader;
* Added geography meta data XML: sahsuland_geography_metadata.xml for data loader;
* Prevent geography reload in production scripts if in use on a study.
* Rebuild production import scripts: rebuild rif40_geolevels view
* SQL Server multi DB support
* Bug in rif40 Postgres and SQL server geolevels select (all, not geography specified!)
* Fix for SQL Server production import (double ""'s are present). Curiously it was not bothering Leaflet!
 
#### 13th to 17th February 

* Add geographic centroid to lookup table
* Change db/geography/geomlevel made a popup dialog box; auto sizing now OK
* Added Google maps, OSM satellite and terrain data
* Use onEachFeature to call createPopup() - much more efficient
* Fixed feature gid to be gid of areaID
* Sync sahsuland_empty: tiles and lookup tables 
 
#### 20th to 24th February

* Re-plan for Feb 22nd meeting
* Alter 9:

  1. Replace old geosptial build code with new data loader. Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; use rif40_geolevels lookup_table/tile_table
  2. Make RIF40_TABLES.THEME nullable for denominators
  3. INSERT INTO rif40_table_outcomes wrong OUTCOME_GROUP_NAME used in v4_0_postgres_sahsuland_imports.sql, suspect ICD hard coded. [Not a bug]
  4. Fix:
     * RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMNN_EXISTS to COLUMN_EXISTS
  5. Resolve: RIF40_PARAMETERS.DESCRIPTION (SQL Server) or PARAM_DESCRIPTION (Postgres)
     Stick with Postgres

* sahsuland_empty fix in alter 5 for:
```
	ALTER TABLE rif40_columns
	  ADD CONSTRAINT rif40_columns_pk PRIMARY KEY (table_or_view_name_hide, column_name_hide);
	psql:alter_scripts/v4_0_alter_7.sql:715: ERROR:  could not create unique index "rif40_columns_pk"
	DETAIL:  Key (table_or_view_name_hide, column_name_hide)=(RIF40_GEOLEVELS, AREAID_COUNT) is duplicated.
```
* SQL Server data loader script integration
* SQL server faults in insert triggers: tr_covariates_check, tr_geolevel_check, tr_rif40_tables_checks;
```
Msg 8114, Level 16, State 5, Server PH-LAPTOP\SQLEXPRESS, Procedure tr_covariates_check, Line 12
Error converting data type varchar to numeric.
```

```
Msg 51146, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Procedure tr_geolevel_check, Line 36
Table name: [rif40].[t_rif40_geolevels], Cannot DELETE from T_RIF40_GEOLEVELS
```

```
Msg 51147, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Procedure tr_rif40_tables_checks, Line 38
Table name: [rif40].[rif40_tables], Cannot DELETE from RIF40_TABLES
```

* Fix for T_RIF40_CONTEXTUAL_STATS/RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION to
  TOTAL_COMPARISON_POPULATION not done due to script interaction.

#### 27th February to 3rd March

* Added caching to base layers and topoJSON tile layer; fixed baselayer max zooms; allow test harness to go the 
  zoomlevel 19 
* Cache ageing, global auto compaction
* SQL Server test builds
* SQL Server database build: sahsuland, sahsuland_dev and test; fully scripted and documented (and added to wiki) with no need for Server Adminstrator
  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/installation/README.md
* Fixed private browsing mode issues
* Support database (i.e. SQL Server not present)

#### 6th to 10th March

* rif40_GetAdjacencyMatrix.sql: change ST_Touches() to ST_Intersects() to fix missing adjacencies caused by small slivers. Impact on sahsuland/USA to county level geographies:
  * Level 2:
  
	```  
	 areaid | areaname | num_adjacencies | extra_intersects |              adjacency_list
	--------+----------+-----------------+------------------+-------------------------------------------
	 01.004 |          |               6 |                2 | 01.001,01.002,01.003,01.005,01.008,01.009
	 01.005 |          |               4 |                1 | 01.001,01.004,01.006,01.009
	 01.008 |          |               5 |                1 | 01.003,01.004,01.007,01.009,01.011
	```
	
  * Level 3:
  
	```
	(3 rows)
		areaid     |   areaname    | num_adjacencies | extra_intersects |                                                                                    adjacency_list
	---------------+---------------+-----------------+------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 01.007.012500 | 01.007.012500 |               5 |                1 | 01.007.012300,01.007.012400,01.013.016100,01.013.016200,01.015.016200
	 01.015.016200 | 01.015.016200 |              13 |                1 | 01.007.012500,01.013.016200,01.013.016800,01.014.018000,01.014.018100,01.014.018300,01.014.018500,01.014.018600,01.015.016900,01.016.017000,01.017.019000,01.018.019100,01.018.019500
	(2 rows)
	```
	
  * Level 4: none
  
  * USA at state level none:
  
	```
	WITH a AS (
		SELECT a1.areaid, 
			   a2.areaid AS adjacent_area_id, 
			   ST_Touches(a1.geom, a2.geom) AS touches,
			   ST_Intersects(a1.geom, a2.geom) AS intersects
		  FROM geometry_usa_2014_geolevel_id_2_zoomlevel_9 a1, geometry_usa_2014_geolevel_id_2_zoomlevel_9 a2 
		 WHERE ST_Touches(a1.geom, a2.geom) OR ST_Intersects(a1.geom, a2.geom)
		   AND a1.areaid != a2.areaid
	), b AS (
		SELECT a.*, b1.areaname, b2.areaname AS adjacent_areaname
		  FROM a
			LEFT OUTER JOIN lookup_cb_2014_us_state_500k b1 ON (a.areaid = b1.cb_2014_us_state_500k)
			LEFT OUTER JOIN lookup_cb_2014_us_state_500k b2 ON (a.adjacent_area_id = b2.cb_2014_us_state_500k)
	)
	SELECT a.cb_2014_us_state_500k AS areaid, a.areaname, 
		   COUNT(b.areaid)::INTEGER AS num_adjacencies, 
		   SUM(COALESCE(b.intersects::INTEGER, 0)) - SUM(COALESCE(b.touches::INTEGER, 0)) AS extra_intersects, 
		   string_agg(b.adjacent_areaname, ',' ORDER BY b.adjacent_areaname)::VARCHAR AS adjacency_list
	  FROM lookup_cb_2014_us_state_500k a 
			LEFT OUTER JOIN b ON (a.cb_2014_us_state_500k = b.areaid)
	 GROUP BY a.cb_2014_us_state_500k, a.areaname
	 ORDER BY a.areaname;
	```  
  
	|                   areaname                   | num_adjacencies | exta_intersects |                                 adjacency_list                                 |
	|----------------------------------------------|-----------------|-----------------|--------------------------------------------------------------------------------|
	| Alabama                                      |               4 |               0 | Florida,Georgia,Mississippi,Tennessee                                          |
	| Alaska                                       |               0 |               0 |                                                                                |
	| American Samoa                               |               0 |               0 |                                                                                |  
	| Arizona                                      |               5 |               0 | California,Colorado,Nevada,New Mexico,Utah                                     |
	| Arkansas                                     |               6 |               0 | Louisiana,Mississippi,Missouri,Oklahoma,Tennessee,Texas                        |
	| California                                   |               3 |               0 | Arizona,Nevada,Oregon                                                          |
	| Colorado                                     |               7 |               0 | Arizona,Kansas,Nebraska,New Mexico,Oklahoma,Utah,Wyoming                       |
	| Commonwealth of the Northern Mariana Islands |               0 |               0 |                                                                                |  
	| Connecticut                                  |               3 |               0 | Massachusetts,New York,Rhode Island                                            |
	| Delaware                                     |               3 |               0 | Maryland,New Jersey,Pennsylvania                                               |
	| District of Columbia                         |               2 |               0 | Maryland,Virginia                                                              |
	| Florida                                      |               2 |               0 | Alabama,Georgia                                                                |
	| Georgia                                      |               5 |               0 | Alabama,Florida,North Carolina,South Carolina,Tenness                          |
	| Guam                                         |               0 |               0 |                                                                                |  
	| Hawaii                                       |               0 |               0 |                                                                                |  
	| Idaho                                        |               6 |               0 | Montana,Nevada,Oregon,Utah,Washington,Wyoming                                  |
	| Illinois                                     |               5 |               0 | Indiana,Iowa,Kentucky,Missouri,Wisconsin                                       |
	| Indiana                                      |               4 |               0 | Illinois,Kentucky,Michigan,Ohio                                                |
	| Iowa                                         |               6 |               0 | Illinois,Minnesota,Missouri,Nebraska,South Dakota,Wisconsin                    |
	| Kansas                                       |               4 |               0 | Colorado,Missouri,Nebraska,Oklahoma                                            |
	| Kentucky                                     |               7 |               0 | Illinois,Indiana,Missouri,Ohio,Tennessee,Virginia,West Virginia                |
	| Louisiana                                    |               3 |               0 | Arkansas,Mississippi,Texas                                                     |
	| Maine                                        |               1 |               0 | New Hampshire                                                                  |
	| Maryland                                     |               5 |               0 | Delaware,District of Columbia,Pennsylvania,Virginia,West Virginia              |
	| Massachusetts                                |               5 |               0 | Connecticut,New Hampshire,New York,Rhode Island,Vermont                        |
	| Michigan                                     |               3 |               0 | Indiana,Ohio,Wisconsin                                                         |
	| Minnesota                                    |               4 |               0 | Iowa,North Dakota,South Dakota,Wisconsin                                       |
	| Mississippi                                  |               4 |               0 | Alabama,Arkansas,Louisiana,Tennessee                                           |
	| Missouri                                     |               8 |               0 | Arkansas,Illinois,Iowa,Kansas,Kentucky,Nebraska,Oklahoma,Tennessee             |
	| Montana                                      |               4 |               0 | Idaho,North Dakota,South Dakota,Wyoming                                        |
	| Nebraska                                     |               6 |               0 | Colorado,Iowa,Kansas,Missouri,South Dakota,Wyoming                             |
	| Nevada                                       |               5 |               0 | Arizona,California,Idaho,Oregon,Utah                                           |
	| New Hampshire                                |               3 |               0 | Maine,Massachusetts,Vermont                                                    |
	| New Jersey                                   |               3 |               0 | Delaware,New York,Pennsylvania                                                 |
	| New Mexico                                   |               5 |               0 | Arizona,Colorado,Oklahoma,Texas,Utah                                           |
	| New York                                     |               5 |               0 | Connecticut,Massachusetts,New Jersey,Pennsylvania,Vermont                      |
	| North Carolina                               |               4 |               0 | Georgia,South Carolina,Tennessee,Virginia                                      |                                              |
	| North Dakota                                 |               3 |               0 | Minnesota,Montana,South Dakota                                                 |
	| Ohio                                         |               5 |               0 | Indiana,Kentucky,Michigan,Pennsylvania,West Virginia                           |
	| Oklahoma                                     |               6 |               0 | Arkansas,Colorado,Kansas,Missouri,New Mexico,Texas                             |
	| Oregon                                       |               4 |               0 | California,Idaho,Nevada,Washington                                             |
	| Pennsylvania                                 |               6 |               0 | Delaware,Maryland,New Jersey,New York,Ohio,West Virginia                       |
	| Puerto Rico                                  |               0 |               0 |                                                                                |  
	| Rhode Island                                 |               2 |               0 | Connecticut,Massachusetts                                                      |
	| South Carolina                               |               2 |               0 | Georgia,North Carolina                                                         |
	| South Dakota                                 |               6 |               0 | Iowa,Minnesota,Montana,Nebraska,North Dakota,Wyoming                           |
	| Tennessee                                    |               8 |               0 | Alabama,Arkansas,Georgia,Kentucky,Mississippi,Missouri,North Carolina,Virginia |
	| Texas                                        |               4 |               0 | Arkansas,Louisiana,New Mexico,Oklahoma                                         |
	| United States Virgin Islands                 |               0 |               0 |                                                                                |  
	| Utah                                         |               6 |               0 | Arizona,Colorado,Idaho,Nevada,New Mexico,Wyoming                               |
	| Vermont                                      |               3 |               0 | Massachusetts,New Hampshire,New York                                           |
	| Virginia                                     |               6 |               0 | District of Columbia,Kentucky,Maryland,North Carolina,Tennessee,West Virginia  |
	| Washington                                   |               2 |               0 | Idaho,Oregon                                                                   |
	| West Virginia                                |               5 |               0 | Kentucky,Maryland,Ohio,Pennsylvania,Virginia                                   |
	| Wisconsin                                    |               4 |               0 | Illinois,Iowa,Michigan,Minnesota                                               |
	| Wyoming                                      |               6 |               0 | Colorado,Idaho,Montana,Nebraska,South Dakota,Utah                              |
	
   * USA County level:
   
	```   
	  areaid  |   areaname   | num_adjacencies | extra_intersects |                            adjacency_list
	----------+--------------+-----------------+------------------+-----------------------------------------------------------------------
	 01710958 | Anne Arundel |               5 |                1 | Baltimore,Baltimore,Calvert,Howard,Prince George's
	 01702381 | Baltimore    |               2 |                1 | Anne Arundel,Baltimore
	 01696996 | Barbour      |               6 |                2 | Harrison,Preston,Randolph,Taylor,Tucker,Upshur
	 01448018 | Carbon       |               6 |                2 | Duchesne,Emery,Grand,Sanpete,Uintah,Utah
	 00347456 | Coffee       |               8 |                2 | Atkinson,Bacon,Ben Hill,Berrien,Irwin,Jeff Davis,Telfair,Ware
	 00342918 | Crawford     |               7 |                2 | Bibb,Houston,Macon,Monroe,Peach,Taylor,Upson
	 01448021 | Duchesne     |               6 |                2 | Carbon,Daggett,Summit,Uintah,Utah,Wasatch
	 01687999 | Jeff Davis   |               7 |                2 | Appling,Bacon,Coffee,Montgomery,Telfair,Toombs,Wheeler
	 00558088 | Madison      |               5 |                2 | East Carroll,Franklin,Richland,Tensas,Warren
	 01383963 | Nueces       |               4 |                1 | Aransas,Jim Wells,Kleberg,San Patricio
	 00343153 | Peach        |               4 |                2 | Crawford,Houston,Macon,Taylor
	 01558642 | Preston      |               7 |                2 | Barbour,Fayette,Garrett,Grant,Monongalia,Taylor,Tucker
	 01383990 | San Patricio |               6 |                1 | Aransas,Bee,Jim Wells,Live Oak,Nueces,Refugio
	 00344156 | Taylor       |               7 |                2 | Crawford,Macon,Marion,Peach,Schley,Talbot,Upson
	 00356958 | Telfair      |               6 |                2 | Ben Hill,Coffee,Dodge,Jeff Davis,Wheeler,Wilcox
	 00559509 | Tensas       |               8 |                2 | Adams,Catahoula,Claiborne,Concordia,Franklin,Jefferson,Madison,Warren
	 01689423 | Tucker       |               4 |                2 | Barbour,Grant,Preston,Randolph
	 01448038 | Utah         |               7 |                2 | Carbon,Duchesne,Juab,Salt Lake,Sanpete,Tooele,Wasatch
	 00695795 | Warren       |               7 |                2 | Claiborne,East Carroll,Hinds,Issaquena,Madison,Tensas,Yazoo
	(19 rows)
	```

* A pre-built ajacency table is required for performance reasons.
* SQL Server tile-vewer setup error handling
* Fixed SQL Server build directories with sahsuland_dev/master dependency issues, remaining hard coded USE sahsuland_dev; added checks for correct database and admin privileges.
  SQL Server sahusland now builds OK.
* Create and delete a study (i.e. fixed all the triggers used in study creation so they now work)
* Created a rebuild_all.bat script
* Made build scripts reliable
* Created a dummy rif40_run_study procedure and infrastructure to run
* SQL Server Installer documentation

#### 13th to 17th March

* Integrate geosptial and data loader data into sahsuland_dev, sahsuland
* SQL Server installer
* Swedish RIF meeting
* Data loader integration and testing. Issues resolved:
  * Source input data was wrong
  * PKs added
  * Age sex group derivation fixed
* Geospatial integration and testing outstanding issues: 
    * Comparision area extract:
	```
	SELECT study_or_comparison, area_id, COUNT(*) 
	  FROM v_test_4_study_id_1_extract GROUP BY area_id, study_or_comparison ORDER BY study_or_comparison, area_id; -- SAVED
	/*
	study_or_comparison |     area_id     | count
	--------------------+-----------------+-------
	C                   | 01              |     1
	C                   | 01.001          |   704 <== WRONG
	C                   | 01.002          |  1760 ...

	C                   | 01.017          |   704
	C                   | 01.018          |  1760 <== WRONG
	```
* TESTUSER support: TESTUSER can now be set in Postgres Makefile.overrides

#### 20th to 24th March

* Geospatial integration and testing outstanding issues: comparision area extract fixed; new data is correct
* Auto disable of basemap caching on error; caching stats fixes
* Java Developer job description
* Fixed SQL Server bug:
	```
	Msg 50029, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Procedure tr_geography, Line 257
	Table_name: [rif40].[rif40_geographies] , Default study area column not found in T_RIF40_GEOLEVELS: <geography>USA_2014</geography><
	defaultstudyarea>STATENS</defaultstudyarea>
	```
  Need to not set *defaultcomparea, defaultstudyarea* during initial INSERT into rif40_geographies; add UPDATE after t_rif40_geolevels 
  INSERT. Also geolevel name is wrong, STATENS should be CB_2014_US_STATE_500K; so fixed geoDataLoader.xml
``
UPDATE rif40_geographies
   SET defaultcomparea  = 'CB_2014_US_NATION_5M',
       defaultstudyarea = 'CB_2014_US_STATE_500K'
 WHERE geography = 'USA_2014';
GO
```
* Switch SQL Server to use username/password in tileMaker; updated installation/README

#### 27th to 31st March

* SQL Server and Postgres install on David's machine. Added Poweruser support; improved installation instructions. Added a per user 
  STUDY_STATUS table required by Java middleware. These will be re-engineered better at a later date.
* Review and revise Postgrees build instruction into single document with contents the same as SQL Server  
* Improved documentation on SQL Server BULK LOAD file permissions
* Fix Halland (Sweden) projection error in shapefile convertor
* Swedish shapefile data error - multiple SRID's?
* Test Halland shapefiles, need to fix:
  * Unicode characters;
    * Checked Postgres; all tables (shapefile, lookup) datatypes are OK. Windows displays them wrong (set to code page 1252); 
	  Notetab++ understands UTF-8 and the CSV file displays OK. SQL Server; convert data type from Text to NVARCHAR(1000);
	  corruption is occuring in both databases, probably in tileMaker SELECTs.
  * Default study/comnparision areas
  * Area tests (area_check.sql) is failing - suspect area is too small, could be projection ia wrong. Added max 10% of areas 
    can be wrong, otherwise warn. Moved to end after commit for improved diagnostics
* defaultcomparea, defaultstudyarea are wrong (defaulted to LEVEL1, not SAHSU_GRD_LEVEL1...), now set in XML config. 
  listing/comparea/resolution all set to 1 for all geolevels
* RIF meeting

#### 3rd to 7th April

* Monthly reports
* Wellcome application
* Add ajacency table. Some slight differences between PostGIS and SQL Server need to be investigated
	```
	C:\Users\Peter\Documents\Work\sahsuland>diff mssql_adjacency_sahsuland.csv pg_adjacency_sahsuland.csv
	65c65
	< "3","01.008.002900","6","01.008.006200,01.008.006300,01.008.006800,01.008.006900,01.008.007100,01.008.007500"
	---
	> "3","01.008.002900","7","01.008.006200,01.008.006300,01.008.006800,01.008.006900,01.008.007100,01.008.007400,01.008.007500"
	86c86
	< "3","01.008.005500","5","01.008.005300,01.008.005400,01.008.005700,01.008.007700,01.008.007800"
	---
	> "3","01.008.005500","6","01.008.005300,01.008.005400,01.008.005700,01.008.006000,01.008.007700,01.008.007800"
	91c91
	< "3","01.008.006000","11","01.008.003500,01.008.005100,01.008.005200,01.008.005300,01.008.006100,01.008.006700,01.008.006800,01.008
	.007700,01.008.008200,01.008.008400,01.008.009100"
	---
	> "3","01.008.006000","12","01.008.003500,01.008.005100,01.008.005200,01.008.005300,01.008.005500,01.008.006100,01.008.006700,01.008
	.006800,01.008.007700,01.008.008200,01.008.008400,01.008.009100"
	105c105
	< "3","01.008.007400","6","01.008.006700,01.008.006800,01.008.007300,01.008.007500,01.008.007600,01.008.008700"
	---
	> "3","01.008.007400","7","01.008.002900,01.008.006700,01.008.006800,01.008.007300,01.008.007500,01.008.007600,01.008.008700"

	C:\Users\Peter\Documents\Work\sahsuland>wc -l mssql_adjacency_sahsuland.csv pg_adjacency_sahsuland.csv
	  1447 mssql_adjacency_sahsuland.csv
	  1447 pg_adjacency_sahsuland.csv
	  2894 total
	```
* rif40_GetAdjacencyMatrix.sql limit line length to 8000 characters; adjacency table create script will raise truncation error if greater
* Use relative install path in tilemaker install script generator (i.e. ../../GeospatialData/tileMaker/ for sahsuland). No need 
  to edit path to CSV file by hand.
* Change rif40_GetAdjacencyMatrix.sql to support adjacency table; port to SQL Server.
* Add <geography>_GetAdjacencyMatrix() function to tilemaker

#### 10th to 14th April

* SQL server rif40_startup fixed, rif40_num_denom and rif40_num_denom_errors fixed
* Rebuild wpea-rif1; small TESTUSER fix; tomcat setup requires work

#### 18th to 21st April

* SQL Server install of *sahusland* via a backup and restore
* Ask for username and password
* Production installer scripts separate from github tree; install documented: 
  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/production/INSTALL.md
* Install on wpea-rif1 issues:
  * Logged on as a power user; run shell as adminstrator - shell is still in the user's name so SQL Server sqlcmd -E logs on as 
    guest. Added section to isntallation instructions.
  * Java is broken on server. Fully de-install, reboot; remove all Java file from C:\Program Files\Java and re-install afresh.
    Run servce.bat remove then install in %CATALINA_HOME%/bin directory to re-initialise tomcat.
  * Firewall issues - advice to use of localhost or 127.0.0.1 to avoid routing via ethernet for SQL Server and Postgres
  * Unsigned TLS being blocked by the Imperial Network IE11; other browsers work after the warnings have been accepted
    ```
	13:26:08.957 wpea-rif1.sm.med.ic.ac.uk:8080 uses an invalid security certificate.

	The certificate is not trusted because it is self-signed.
	The certificate is not valid for the name wpea-rif1.sm.med.ic.ac.uk.

	Error code: <a id="errorCode" title="SEC_ERROR_UNKNOWN_ISSUER">SEC_ERROR_UNKNOWN_ISSUER</a>
	 1 (unknown)
    ```	
  * ```The port number 1433/sahsuland_dev is not valid```. SQL Server set to use port 1433; issue is with configuration, will
    test further at home then document.
  ```
	  C A T A L I N A  H O M E==C:\Program Files\Apache Software Foundation\Tomcat 8.5==
	HealthOutcomeManager init targetPathValue==C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\
	\classes==
	com.microsoft.sqlserver.jdbc.SQLServerException: The port number 1433/sahsuland_dev is not valid.
			at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDriverError(SQLServerException.java:206)
			at com.microsoft.sqlserver.jdbc.SQLServerConnection.connectInternal(SQLServerConnection.java:1305)
			at com.microsoft.sqlserver.jdbc.SQLServerConnection.connect(SQLServerConnection.java:788)
			at com.microsoft.sqlserver.jdbc.SQLServerDriver.connect(SQLServerDriver.java:1187)
			at java.sql.DriverManager.getConnection(DriverManager.java:664)
			at java.sql.DriverManager.getConnection(DriverManager.java:208)
			at rifServices.dataStorageLayer.pg.PGSQLConnectionManager.createConnection(PGSQLConnectionManager.java:697)
			at rifServices.dataStorageLayer.pg.PGSQLConnectionManager.login(PGSQLConnectionManager.java:327)
			at rifServices.dataStorageLayer.pg.PGSQLAbstractStudyServiceBundle.login(PGSQLAbstractStudyServiceBundle.java:192)
			at rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyServiceBundle.login(PGSQLProductionRIFStudyServiceBundle.java:63)
			at rifServices.restfulWebServices.pg.PGSQLAbstractRIFWebServiceResource.login(PGSQLAbstractRIFWebServiceResource.java:171)
			at rifServices.restfulWebServices.pg.PGSQLRIFStudySubmissionWebServiceResource.login(PGSQLRIFStudySubmissionWebServiceResour
	ce.java:136)
			at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
			at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
			at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
			at java.lang.reflect.Method.invoke(Method.java:498)
			at com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)
			at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(Abst
	ractResourceMethodDispatchProvider.java:205)
			at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:
	75)
			at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)
			at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
			at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)
			at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
			at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)
			at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)
			at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)
			at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)
			at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)
			at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)
			at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)
			at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)
			at javax.servlet.http.HttpServlet.service(HttpServlet.java:742)
			at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231)
			at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
			at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)
			at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
			at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
			at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198)
			at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
			at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:478)
			at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140)
			at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:80)
			at org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:624)
			at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87)
			at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:342)
			at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:799)
			at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
			at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:861)
			at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1455)
			at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
			at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
			at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
			at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
			at java.lang.Thread.run(Thread.java:748)
	```
	Home PC runs with: 
	```
	#SQL SERVER
	database.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
	database.jdbcDriverPrefix=jdbc:sqlserver
	database.host=localhost\\SAHSU
	database.port=1433
	database.databaseName=sahsuland_dev
	database.databaseType=sqlServer	
	```
* Run study extract complete, map table to do

#### 29th to 28th April	

* Fixed production restore - complete
* Complete SQL Server run study port
* SQL Server full install testing
* Add checks to username, database name in scripts for lowercase DB name
* Tested installer on empty SQL Server DB
* Middleware testing
* Added middleware *java_build.bat* script
* Full ICD10 listing setup
* Web apps 7zip bundle
	
#### 1st to 5th May 

* Postgres install from export script
* Test can run a study on Postgres
* Study processed by not available to front end:
```
sahsuland=> select * from study_status;
 study_id | study_state |       creation_date        | ith_update |                                       message

----------+-------------+----------------------------+------------+-----------------------------------------------------------------
--------------------
        4 | C           | 2017-05-01 14:41:10.344285 |          0 | The study has been created but it has not been verified.
        4 | E           | 2017-05-01 14:44:32.595896 |          0 | Study extracted imported or created but neither results nor maps
 have been created.
        4 | R           | 2017-05-01 14:44:32.83881  |          0 | The study results have been computed and they are now ready to b
e used.
(3 rows)
```

Fixed by:
```
UPDATE study_status SET ith_update = 2 WHERE study_state = 'R';
UPDATE study_status SET ith_update = 1 WHERE study_state = 'E';
```
* Missing level 4 geography tiles caused by areaid_count=0 in geolevels_sahsuland (SQL statement 327: Update areaid_count 
  column in geolevels table using geometry table); removed areaid_count tests from tiles_sahsuland view
  temporarily. Temporary fix applied

#### 8th to 12th May 

* CDC visit in Atlanta: CDC meetings at Roybal, EPHT conference including RIF demo

#### 15th to 19th May 

* Fix tile viewer screen sizing
* Send out Atlanta comments
* R test script, study_status integration

#### 22nd to 26th May

* RIF meeting
* rif40_GetAdjacencyMatrix port for R
* R script now works on Postgres.
* rif40_run_study.bat provided for Postgres aswell
* Data needs to be checked in both ports; port ports give different answers
* SQL Server middleware tests and fault diagnosis
* Uppercase logons test; middleware testing

#### 29th May to 2nd June

* Automatic denominators: USA data not appearing (rif40_num_denom issue). USA data using wrong column 
  names - should use geolevel_name. Added to help trace [rif40].[rif40_num_denom_validate2](); 
* Speed of automatic denominators on Postgres is slow; fine on SQL Server. Speeded up roughly three times by supressing
  log messages to sahsuland_dev only in the validator functions 
  e.g. rif40_sql_pkg.rif40_num_denom_validate();
* Improvements to rif40_sequence_current_value transactional robustness

#### 5th to 9th June

* rif40 run study procedural stub for SQL Server
* Run rif40_run_study procedure in SQL Server, added JDBC call() support to MSSQL
* Added SQL warnings (print messages) to Java in both ports
* Test for no rows in extract
* Always commit on run study even if there is an error (to make the database state visible)
* Added rif40_study_status support for SQL Server
  * Add extra statii to studies/investigations for R success/failure/warning. Current states:
    * C: created, not verified; 
    * V: verified, but no other work done; 
    * E: extracted imported or created, but no results or maps created; 
    * R: initial results population, create map table; 
    * U: upgraded record from V3.1 RIF (has an indeterminate state; probably analogous to R.
  * New states:
    * G: Extract failure, extract, results or maps not created;  
    * S: R success;
    * F: R failure, R has caught one or more exceptions [depends on the exception handler design]
    * W: R warning.  
* SQL Logging added to SQL Server port.
* Managed to run a study through the SQL Server middleware. It died in R
  ```
  [1] "Creating temporary table: peter.tmp_s3_map"
  Error in sqlSave(connDB, data, tablename = temporarySmoothedResultsTableName,  :
    [RODBC] Failed exec in Update
  42000 8023 [Microsoft][SQL Server Native Client 11.0][SQL Server]The incoming tabular data stream (TDS) remote procedure call (RPC)
  protocol stream is incorrect. Parameter 15 (""): The supplied value is not a valid instance of data type float. Check the source data for invalid values. An example of an invalid value is data of numeric type with scale greater than precision.
  Calls: saveDataFrameToDatabaseTable -> sqlSave
  ```
* Added rif40_study_status support for Postgres
* Added stats_method to rif40_studies

#### 12th to 16th June

* CDC password tests: Peter!@$^~ was OK
* Fix missing level 4 geography tiles bug (areaid_count=0 in geolevels table) 
* No area names in sahsuland geography. Setup is correct; mixed and lowercase names not being handled correctly. Forced 
  to uppercase.
* Data loader scripts disable/enable the foreign key constraint rif40_covariates_geolevel_fk (SQL Server only for the moment).
  Resolves geospatial SQL Server and Postgres install issue (caused by pre-exsiting studies). Modified checks for studies:
  ```
	-- SQL statement 75: Remove old geolevels meta data table >>>
	DELETE FROM t_rif40_geolevels WHERE geography = 'SAHSULAND';

	Msg 547, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
	The DELETE statement conflicted with the REFERENCE constraint "rif40_covariates_geolevel_fk". The conflict occurred in database "sah
	suland_dev", table "rif40.rif40_covariates".
	Msg 3621, Level 0, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
	The statement has been terminated.
  ```	
* Fixed trigger issues with SQL Server geospatial re-installs (DELETE FROM t_rif40_geolevels):
  ```
  -- SQL statement 95: Remove old geolevels meta data table >>>
  DELETE FROM t_rif40_geolevels WHERE geography = 'SAHSULAND';

  (1 rows affected)
  Msg 51146, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Procedure tr_geolevel_check, Line 43
  Table name: [rif40].[t_rif40_geolevels], Cannot DELETE from T_RIF40_GEOLEVELS
  ```  
  
#### 19th to 23rd June

* RIF work estimates going forward into 2018, RIF middlewre logging
* Test 1001 comparisons, Postgres and SQL Server sahsuland loads confirmed correct after fix to total_field.
* Checked SAHSULAND dataloaded data is the same
* Removed denominator gender data when not required by numerator
* Fix to Postgres run study error handler
* 2017/18 forward plan. Highlight impact of no Java developer
* Regression test of both ports for PHE conference demo
 
#### 26th to 30th June

* Install current version on RIF laptop
* Integrated and tested Postgres JRI
* Integrated and tested MS SQL Server JRI: same fault as before
* Added dump of extract and results frames to CSV (on by default)
* Confirmed that control of signal handling is not supported on R windows ports (i.e. control-C handling has to be solved in 
  JRI). Basically JRI cannot handle control-C (SIGINT)
* Confirmed that tomcat service in unaffacted by JRI; so the work around is to use bash which does handle control-C and 
  will stop tomcat.
* SQL Server save bug is caused by INF values in R data frames: ```42000 8023 [Microsoft][SQL Server Native Client 11.0][SQL Server]The incoming tabular data stream (TDS) remote procedure call (RPC)
  protocol stream is incorrect. Parameter 15 (""): The supplied value is not a valid instance of data type float. Check the source dat
  a for invalid values. An example of an invalid value is data of numeric type with scale greater than precision.```

  Fails in row 2, column 15:
  ```
  30-Jun-2017 08:38:09.340 INFO [http-nio-8080-exec-1] rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole.rWriteConsole rWriteConsole: no: 15: upper95 inf/***/
  ```
  Fixed.
* Fixed MSSQL run study bug - 2nd run in middleware (need to test for ##g_rif40_study_areas, ##g_rif40_comparison_areas):
  ```
  SQL[rif40] OK> GRANT SELECT,INSERT ON rif_studies.s6_extract TO peter;
  Function: [rif40].[rif40_ddl], SQL statement had error: There is already an object named '##g_rif40_study_areas' in the database.
  SQL[peter]> SELECT study_id, area_id, band_id
    INTO ##g_rif40_study_areas
    FROM rif40.rif40_study_areas
   WHERE study_id = @study_id /* Current study ID */
   ORDER BY study_id, area_id, band_id;
  Caught error in rif40.rif40_run_study2(6)
  Error number: 3930; severity: 16; state: 1
  Procedure: rif40_ddl;  line: 79
  Error message: The current transaction cannot be committed and cannot support operations that write to the log file. Roll back the transaction.
  Caught error handler error in rif40.rif40_run_study2(6)
  Error number: 3930; severity: 16; state: 1
  Procedure: rif40_run_study;  line: 33
  Error message: The current transaction cannot be committed and cannot support operations that write to the log file. Roll back the t ransaction.

  java.util.MissingResourceException: Can't find resource for bundle java.util.PropertyResourceBundle, key general.db.error.unableToCommit
        at java.util.ResourceBundle.getObject(ResourceBundle.java:450)
  ```		 
Todo:

* JRI causing <control-C> to be intercepted and not stop tomcat. Workaround is to run tomcat from bash.
* SQL Server tiles appears to be missing some names. Looks OK in the tiles table
* Process SEER data on desktop using Postgres.
* Automatic denominators: USA data not appearing (rif40_num_denom issue). USA data using wrong column 
  names - should use geolevel_name. Bug therefore is in data loader setup. 
* Test plan

#### 1st July to 31st July

* Holiday
 
#### 1st to 4th August

* RIF meeting
* New RIF laptop build, Pippa laptop build
* Document setup and services logging

#### 7th to 11th

* Regression testing
* Build SQL Server DB for CDC
* SEER data loading

#### 14th to 18th August

* SEER Covariates
* CDC Support
* USA_2014/SEER data integration and testing:
  * RIF40_GEOGRAPHIES set up wrong: default study and comnparison area names use original field name, no setup field names
  * Script fixed, reload needed ```DELETE FROM rif40_covariates WHERE geography = 'USA_2014'``` adding.
  Now able to setp and GA Lung Cancer study aprt from the investigation section with the error:
  ```
  Error: Numerator-Denominator Pair 'SEER_CANCER - POP_SAHSULAND_POP' not found in database
  ```
  Running the middleware query:
  ```
  http://localhost:8080/rifServices/studySubmission/pg/getNumerator
	?userID=peter
	&geographyName=USA_2014
	&healthThemeDescription=covering%20various%20types%20of%20cancers
  ```
  Returns the correct data:
  ```
  [{"numeratorTableName":"NUM_SAHSULAND_CANCER","numeratorTableDescription":"cancer numerator","denominatorTableName":"POP_SAHSULAND_POP","denominatorTableDescription":"population health file"},
   {"numeratorTableName":"SEER_CANCER","numeratorTableDescription":"SEER Cancer data 1973-2013. 9 States in total","denominatorTableName":"SEER_POPULATION","denominatorTableDescription":"SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total"}]
  ```
  There are no exceptions in the Java so probably a front end issue processing the above JSON. I could find no other get methods
  that would cause this.

#### 21st to 25th August

* Data loader documentation
* Data loader testing on SEER data
* Fix for numerator-denoominator pair. Able to submit study
  * Fix for default study/comparison area
  * -20207 Error caused by comparea=1 for state! (fixed by hand; setup needs to be fixed)
  * Fixed COVARIATE_NAME in rif40_inv_covariates: made same as COVARIATE table. SQL SERVER did not need 
    changing.
  * Need to change the centroid code to use ST_Centroid rather than the Turf library. i.e. Computes the geometric
    center of a geometry, or equivalently, the center of mass of the geometry as a POINT. Turf takes one or more 
	features and calculates the centroid using the mean of all vertices. This causes areas with a mix of straight
    and wiggly boundaries to have a centroid baised toward the wiggly.	
  * Fixed cast issue in R. US FIPS codes contain leading 0-2 leading zeros and are stored as strings in the 
    database. These are auto casted to integers in R; causing the update of the results map table to fail.
	R is bad at detecting integers; so a complex multi test check.integer() function was created. If integers
	are detected the database update join on area_id is cast to integers.
  * Managed to crash inla on US Atlanta five counties when using median income quintiles as a covariate. 
  * R syntax fault: ```Error in if (y == "NULL") { : missing value where TRUE/FALSE needed``` in GA lung cancer 
    with covariates and no smoothing.
  * Fixed issue running Tomcat at the command line on Windows 10 (caused by new Unix like copy paste preventing
    the buffer from scrolling. Added notes to install instructions)
  * Year filter fix
  * Updates to SEER script to support dataloader. Need to add in more SEER data
  * Run all SEER states data study for Lung Cancer, 2000 onwards, no covariates
  * I also advise waiting for it to complete looking at the tomcat logs to avoid complaints from the data 
    viewer whilst studies are running. I suspect there is a middleware transaction control bug which cans the 
	study at this point.
* Added support to rif40_database_install.bat for the sahsuland.sql SQL dump (which can be edited unlike the 
  dump file)
* SQL Server regression test: A cursor with the name 'c1_creex' already exists (2nd run))
  
#### 28th August to 1st September

* Add more SEER data
* Fix for missing SQL Server deallocate cursor: 'c1_creex' already exists (rif40_create_extract.sql)

#### 4th to 8th September

* Caving in the Vercours

#### 11th to 15th September

* Add log4j to Java middleware, setup; 
* Rengine not being shutdown correctly on reload of service:
  ```
  Cannot find JRI native library!
  Please make sure that the JRI native library is in a directory listed in java.library.path.

  java.lang.UnsatisfiedLinkError: Native Library C:\Program Files\R\R-3.4.0\library\rJava\jri\x64\jri.dll already loaded in another classloader
        at java.lang.ClassLoader.loadLibrary0(Unknown Source)
        at java.lang.ClassLoader.loadLibrary(Unknown Source)
        at java.lang.Runtime.loadLibrary0(Unknown Source)
        at java.lang.System.loadLibrary(Unknown Source)
        at org.rosuda.JRI.Rengine.<clinit>(Rengine.java:19)
        at rifServices.dataStorageLayer.pg.PGSQLSmoothResultsSubmissionStep.performStep(PGSQLSmoothResultsSubmissionStep.java:183)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.smoothResults(PGSQLRunStudyThread.java:257)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.run(PGSQLRunStudyThread.java:176)
        at java.lang.Thread.run(Unknown Source)
        at rifServices.dataStorageLayer.pg.PGSQLAbstractRIFStudySubmissionService.submitStudy(PGSQLAbstractRIFStudySubmissionService
  ```
  The solution is to restart tomcat.
  
  1. Server reload needs to stop R. This requires a @WebListener [Context Listener (javax.servlet.ServletContextListener)]
  2. R crashes (usually inla) and ideally script errors need to stop R
* Tested log4j integration, all RIF middleware calls works OK on Postgres and SQL Server and log correctly
* Tomcat to do! [some R messages still go to the console!]
  
#### 18th to 22nd September
 
* Configuration using the %CATALINA_HOME%/conf directory; support for conf based RIF properties, 
  common log4j2 setup
* Meetings (Hima, DM, FP)
* R exception handling and errors
* Study state machine and transactional control: obsolete study_status, move to rif40_study_status, trace 
  error support. State machine now handles errors correctly
* Fixed inability to detect errors correctly in run study, R, CVS extract (TODO: report to user). SQL Server 
  does detect but does not trap the error correctly, i.e. the **tomcat** log has:
  
  ```
  15-Sep-2017 10:55:04.188 SEVERE [https-jsse-nio-8080-exec-7] com.sun.jersey.spi.container.ContainerResponse.mapMappableContainerException The exception contained within MappableContainerException could not be mapped to a response, re-throwing to the HTTP container
  java.lang.NoSuchMethodError
        at java.lang.Thread.destroy(Thread.java:990)
        at rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep.performStep(MSSQLSmoothResultsSubmissionStep.java:290)
        at rifServices.dataStorageLayer.ms.MSSQLRunStudyThread.smoothResults(MSSQLRunStudyThread.java:262)
        at rifServices.dataStorageLayer.ms.MSSQLRunStudyThread.run(MSSQLRunStudyThread.java:181)
        at java.lang.Thread.run(Thread.java:745)
        at rifServices.dataStorageLayer.ms.MSSQLAbstractRIFStudySubmissionService.submitStudy(MSSQLAbstractRIFStudySubmissionService.java:1067)
        at rifServices.restfulWebServices.ms.MSSQLAbstractRIFWebServiceResource.submitStudy(MSSQLAbstractRIFWebServiceResource.java:1000)
        at rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceResource.submitStudy(MSSQLRIFStudySubmissionWebServiceResource.java:1178)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)  
  ```
  and:
  ```
  10:42:00.274 [https-jsse-nio-8080-exec-10] ERROR: [rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager]:
  MSSQLStudyExtractManager ERROR
	getMessage:          SQLServerException: Invalid object name 'rif40_dmp_pkg.csv_dump'.
	getRootCauseMessage: SQLServerException: Invalid object name 'rif40_dmp_pkg.csv_dump'.
	getThrowableCount:   1
	getRootCauseStackTrace >>>
	com.microsoft.sqlserver.jdbc.SQLServerException: Invalid object name 'rif40_dmp_pkg.csv_dump'.
        at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDatabaseError(SQLServerException.java:232)
        at com.microsoft.sqlserver.jdbc.SQLServerStatement.getNextResult(SQLServerStatement.java:1672)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.doExecutePreparedStatement(SQLServerPreparedStatement.java:460)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement$PrepStmtExecCmd.doExecute(SQLServerPreparedStatement.java:405)
        at com.microsoft.sqlserver.jdbc.TDSCommand.execute(IOBuffer.java:7535)
        at com.microsoft.sqlserver.jdbc.SQLServerConnection.executeCommand(SQLServerConnection.java:2438)
        at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeCommand(SQLServerStatement.java:208)
        at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeStatement(SQLServerStatement.java:183)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.executeQuery(SQLServerPreparedStatement.java:317)
        at rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager.dumpDatabaseTableToCSVFile(MSSQLStudyExtractManager.java:537)
  ```

#### 25th to 29th September

* Remove old study_status table creates, add support for ri40_study_status
* Add trace to getCurrentStatusAllStudies method (safely formatted), fixed creation date to add time
* Added error logging to all GET methods with exception handlers (others are super classes)
* Added trace to study status; checked can maintain Angular code. Now have basic understanding of what it is doing.
* Completed results reporting from run study and R code.
* Create separate createZipFile and getZipFile methods. 
  
#### 2nd to 6th October

* Major documentation/FAQ sort to make consistent for job advert (and new starter!)
* Factor out R ODBC code into separate R file (for JDBC conversion, and CSV version for tracing R faults)
* Factor out R smoothing code into separate file (for tracing R faults)
* Per study scratch directory
* Create scripts in scratch directory to re-run R
* Now able to re-run R analysis from scratch directory without needing tomcat/Java etc
 
#### 9th to 13th October

* Added print of memory in use by thread, with warning is available memory drops below 500M
* Added JConsole support
* Added print of R process ID. It is not possible to access procxcess related information for this sub process in Java.
* Document R debugging and memory management.
* Rewrote ZIP file extract to use all files in R temporary directory, changed ZIP file extension to .zip so windows understands it, 
  supported directory trees, fixed error handling, added support for separate *createZipFile* and *getZipFile* methods [both do 
  create at present]
* Added getZipFile
* Removed timestamp from zip file names to remove midnight uncertaincy
* Fixed BP's github

#### 16th to 20th October

* Only create ZIP file once, create as .sav to avoid file races
* File download(getZipFile) complete
* getExtractStatus web service - to remove the need to do unnecessary ZIP creates. Will also be used in future 
  to enforce the IG roadblock (i.e. so you cannot create an extract unless IG permission has been granted)
* Document adding a new restfull web service: Creating-a-new-restful-web-service.md in developer FAQ
* Investigated generating maps in R, created a PNG using Leaflet!
* Implemented numbered directories (1-100 etc) to reduce the number of files/directories per directory to 100. This is to improve filesystem 
  performance on Windows Tomcat servers. 
* Reloaded SEER data, replicated SAHSULAND BYM fault with no covariates
* Ported SEER load script to MSSQL
* Fixed status messages so you only get one and all but the most important disapper after ~5 seconds
* Regression tested noth ports. Maps broken on Postgres with >1 geography.
* Issue with SEER data in R; (character) areaIDs are cast to numeric by R and therefore do not match 
  textual string list of areaIDs in the adjacency matrix.

#### 23rd to 27th October

* R ERROR: argument is of length zero ; call stack: if scale.model confirmed fixed by INLA upgrade to 
  INLA_17.06.20 or greater
  Fails: R version 3.4.0 (2017-04-21) -- "You Stupid Darkness"/This is INLA 0.0-1485844051, dated 2017-01-31 (09:14:12+0300).
* Re-instsall SEER test data, add SQL Server SEER test data
* Test study completion code
* Works on 3.4.2; SEER data has brought up an issue with character adjacency column being cast to INTEGER
  Fix to integer areaIds in adjcency matrix
* RIF meeting
* Do not map stuudies with errors (generates errors in the middleware)
* Improved and refactored alert messages - log to console, common code, array of messages for future display, auto disappear, stack trace
* REXCEPTION test exception. Also refactored R parameter code to use names and not index numbers. Can now add new parameters without 
  consequences. Batch and R scripts need to be made parameter name independent for map code

#### 30th October to 3rd November
  
* Fixed Postgres tiles display issue in maps. No tiles are being displayed for level 4. SAHSULAND has been 
  caught by the USA areaid_count upgrade; scripts and tile-maker fixed.
* Shortlisting Java developer (new Kev)
* CDC update by end of October
* Integrated new SAHSULAND data extended to 2016; complete with clusters.
* Minor R script issues caused by tidy last week
* Testing front end
* Fixed null mapID for US maps on SQL server only. Something is not initialising correctly in: 
  rifc-util-mapping.js. Made front end ignore the error!
  ```	
  11:59:14.121 +793.6: [DUPLICATE: 119, msgInterval=0] ERROR: Null mapID 1 rifc-util-alert.js:88:6
  ```
* SQL Server and Postgres US SEER data confirmed working!
* Export error handler; traps exceptions OK; added handlers for errors not detected by the global get handler. Added 100s timeout.
  Intend to trap 403 timeout error if it occurs
* Fixed "Export Study Tables"/"Download Study Export" does not change to "Download ... " on Microsoft Edge/Explor/Chrome. Caused by 
  string returns instead of JSON;
* Console messages suppressed on Explorer unless the console is enabled

#### 6th to 10th November 

* Firefox crashed using nearly 6G of RAM. null mapID in rifc-util-mapping.js 
  ```$scope.handleLayer = function (layer) {}``` is the direct cause. This was fixed
  by removing the layer:
  ```
  	if (mapID === undefined) { // Occurs only on SQL Server!
  //							Do nothing!						
  		$scope.consoleError("Null mapID; layer options: " + JSON.stringify(layer.options, null, 2));
  		if (layer !== undefined) {
  			layer.remove(); 	// Remove 
  		}
  	}
  ```
  This has dealt with the symptoms, **not the cause**. The direct cause is the layer add 
  function is being called before the map has initialised properly. Taken with the 
  *Null zoomlevel error* there is a synchronisation bug in the leaflet code. This issue
  was experienced with the TileMaker code and the fix - stronger asynchronous control is 
  the same (i.e do not rely on Leaflet to do it all). This will have the effect of 
  some counties not being displayed (say 40/3233).
* Complete TODO update

#### 13th to 17th November

* Fix for covariate join problem causing wrong expected values in US data;
* Temporary fix to rif40_execute_insert_statement.sql. Table t_rif40_study_sql needs to have sql_text type 
  set to Text (i.e. remove 4000 character limit)
* Performance tune fixes for Postgres. Issues with lack of bind peaking (fully expand INSERT sql), global temporary tables; tune rif40_log()
* SAHSU 30th  
 
#### 20th to 24th November

* Added tracing to tileLayer; update to same (caching code) as TileMaker, caching disabled on IE. Sub 
  layers not being removed, too many layers in disease mapping (i.e. 60K instead of 2x1280). Layers being added about 
  one per minute;
* Add traps for middleware call failure and out of sequence events;
* Port upgraded extract code to SQL Server;
* Testing of SQL Server code: minor fixes to status list, handle longer SQL statements and in covariate SQL generation, LF issues with new SAHSULAND data;
* Delivered database to CDC;
* Completed security testing, all issues resolved by editing Tomcat config (web.xml) or index.html;

#### 27th November to 1st December

* Fixed inability to detect non default URLs (e.g. HTTPS://);
* Added Front end logging to reduce browser stress when debugging map leaks;
* Removed mixed content for background tiles (i.e. download fromn HTTPS:: if needed);
* Fixed taxonomy service configuration to use Tomcat conf directory (i.e. upgrades do not loose confiruation)
* Added map layer remove code to logoff; memory is freed so the leak is in the GeoJSON gridlayer;

#### 4th to 8th December

* Fixed map bounds and center synchrojnisation issues. Map setup is now occuring in the correct order;
* Improvements to TopoJSONGridLayer.js error handling;
* Leaks if pouchDB is/is not enabled, map sync errors; if disabled still leaks!
* Probed memory leak: not caused by sync;
* Document update conflict issue with PouchDB, requires handlers
  ```
  	  07:33:43.110 [http-nio-8080-exec-7] ERROR rifGenericLibrary.util.FrontEndLogger : 
  	userID:       peter
  	browser type: Firefox; v57
  	iP address:   0:0:0:0:0:0:0:1
  	message:      [TopoJSONGridLayer.js] _db.put() error: {
  	  "status": 409,
  	  "name": "conflict",
  	  "message": "Document update conflict",
  	  "error": true
  	}
  	error stack>>>
  	$scope.consoleError@http://localhost:8080/RIF4/utils/controllers/rifc-util-alert.js:215:10
  	consoleError@http://localhost:8080/RIF4/libs/TopoJSONGridLayer.js:74:6
  	fetchTile/request.onload/<@http://localhost:8080/RIF4/libs/TopoJSONGridLayer.js:266:11
  	<<<
  	actual time:  12/6/2017 7:33:43 AM
  	relative:     +53.2
  ```
* Test memory leak [FAILED!], add frontEndParameters service, SQL install instructions for CDC;
* getFrontEndParameters service (i.e. remove peter username default, disable front end debugging by default, prove JSON5 parser)

#### 11th to 15th December

* OK, so disase map sync OK; rrDropLineRedraw/syncMapping2Events are not being fired; so it must be the D3 maps that are leaking. 
  This agrees with the memory map comparisons. 
* Centering fixes (map zoom, lat and long not set on initial map load). Should also fix null zoomlevel errors;
* Table data being fetched after map; hence display issues;
* Fixed $Watch synchronmisation - now correct; warns on no data for D3 graphs. Diseasemap1 not being displayed until refresh
* disableMapLocking now works correctly, added disableSelectionLocking parameter;
* RIF meeting
* Fixed $watch synchronmisation - now correct; warns on no data for D3 graphs
* Fix $digest issues, started added default mapping support
* Refactor map utils to make more efficent, completed default mapping support, fix missing D3 pane
* Fixed error trap for incorrect mapping column name
* Improved choropleth feature list for viewer
* Added reset button to choropleth popup, fixed scales not being recaculated, fixed exit button to restore old state
* Added JSON5 support to load file, and save

#### 18th to 22nd December

* Add completed study save stub to export (for database generated JSON5)
* Fixed statistics manual link (to main manual)
* Removed leak in study area/comparison area maps (was not released on logoff)
* Added PouchDB conflict resolution code
* Added getJsonFile middleware call
* Common GetStudyJSON object for getJsonFile middleware call

#### 2nd to 5th January 2018

* Locale support, added health codes to getJsonFile
* Added covariates, study and comparison geoelvels; commented; re-order information
  Database does not store geolevel select so the result is the same but the selection method different
  and the file larger!
* Add statistical method to rif40_studies[already done; added middleware support]
* Loaded and ran study using database generagted getJsonFile.
  getJsonFile is incomplete, requires:
  * comparison_geolevel_select, study_geolevel_select in rif40_studies
  * t_rif40_study_select/rif40_study_select, t_rif40_comparison_select/rif40_comparison_select
    for storing areas/bands actually seleted by user
    i.e. currently uses study/comparision areas IDs (mapping level), not as user selected (select level)
  * ICD 10 descriptions from taxonomy service
* Added SQL/SQL log/status data

#### 8th to 12th January 2018

* Get ICD 10 descriptions from taxonomy service
* Fix for https://localhost:8080 to disable TLS security checking [just this address!]. This is to 
  reduce configuration.
* Detect network issues (mainly HTTPS issues) and the taxonomy service initialising
* Restructure ZIP file code as common code; add JSON dump to ZIP file
* Create HTML Report

#### 15th to 19th January 2018

* Added table and column comments, navigation, taxonomy data to HTML Report
* Fix for multi geographies bug. 
* Fix for null covariate issue:
  ```
  Caught error in rif40.rif40_run_study2(10)
  Error number: 56699; severity: 16; state: 1
  Procedure: rif40_execute_insert_statement;  line: 128
  Error message: Function: [rif40].[rif40_execute_insert_statement],  SQL statement had error: Cannot insert the value NULL into column 'median_hh_income_quin', table 'sahsuland.rif_studies.s10_extract'; column does not allow nulls. INSERT fails.
  ```
  This is caused by Cibola county in New Mexico only having median_hh_income_quin from 1973 to 1996. A
  database change is required to make the covariate columns NULL able. SQL Server and Postgres affected. Run:
  ```
  C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\production>
  sqlcmd -U rif40 -P rif40 -d sahsuland -b -m-1 -e -r1 -i  ..\sahsuland\rif40\functions\rif40_create_extract.sql
  ```
  Test SQL: 
  ```
  SELECT s.area_id, a1.areaname AS county, a2.cb_2014_us_state_500k, MIN(c1.year) AS min_year, MAX(c1.year) AS max_year
    FROM rif_data.cov_cb_2014_us_county_500k c1, rif40.rif40_study_areas s
		LEFT OUTER JOIN rif_data.lookup_cb_2014_us_county_500k a1 ON (s.area_id = a1.cb_2014_us_county_500k)
		LEFT OUTER JOIN rif_data.hierarchy_usa_2014 a2 ON (s.area_id = a2.cb_2014_us_county_500k)
  WHERE s.study_id = 10 AND c1.cb_2014_us_county_500k = s.area_id AND median_hh_income_quin IS NULL
  GROUP BY s.area_id, a1.areaname, a2.cb_2014_us_state_500k;
  ```
  This fixes the extract this then causes and R issue (replicates on both systems):
  ```
  Covariates: MEDIAN_HH_INCOME_QUIN
	Stack tracer >>>

	 performSmoothingActivity.R#710: .handleSimpleError(function (obj) 
	{
		ca FUN(X[[i]], ...) lapply(X = X, FUN = FUN, ...) performSmoothingActivity.R#709: sapply(x, FUN = function(y) {
		ans = y
	  performSmoothingActivity.R#106: findNULL(data[, i.d.adj[i]]) performSmoothingActivity(data, AdjRowset) Adj_Cov_Smooth_JRI.R#369: withVisible(expr) Adj_Cov_Smooth_JRI.R#369: withCallingHandlers(withVisible(expr), error = er withErrorTracing({
		data = fetchExtractTable()
		AdjRowset = getAdjace doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(expr, names, parentenv, handlers[[1]]) tryCatchList(expr, names[-nh], parentenv, handlers[-nh]) doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(tryCatchList(expr, names[-nh], parentenv, handlers[-nh]), names tryCatchList(expr, classes, parentenv, handlers) tryCatch({
		withErrorTracing({
			data = fetchExtractTable()
		   eval(expr, pf) eval(expr, pf) withVisible(eval(expr, pf)) evalVis(expr) Adj_Cov_Smooth_JRI.R#390: capture.output({
		tryCatch({
			withError runRSmoothingFunctions() 
	<<< End of stack tracer.
	callPerformSmoothingActivity() ERROR:  missing value where TRUE/FALSE needed ; call stack:  if 
	callPerformSmoothingActivity() ERROR:  missing value where TRUE/FALSE needed ; call stack:  y == "NULL" 
	callPerformSmoothingActivity() ERROR:  missing value where TRUE/FALSE needed ; call stack:  {
		ans = 0
	} 
	callPerformSmoothingActivity exitValue: 1
  ```

#### 22nd to 26th January 2018
  
* Added JPEG/PNG Batik support at correct resolution;
* TIFF is not available due to a bug: https://mail-archives.apache.org/mod_mbox/xmlgraphics-batik-users/201708.mbox/%3CCY4PR04MB039071041456B1E485DCB893DDB40@CY4PR04MB0390.namprd04.prod.outlook.com%3E
* EPS needs FOP; never shifted to XML graphics (as promised in 2009);
* Issues with IE image change;
* Fixed missing text on scaled images, completely styled by CSS;
* Restructured print output code;
* Possible fix for TIFF issues; Batik 1.7 is OK, 1.8 is unusable (too many dependencies);
  RifTIFFTranscoder.java created to try to fix error but failed due to interface dependencies, 
  i.e you need a local org.apache.batik.ext.awt.image.codec.imageio.TIFFTranscoderImageIOWriteAdapter
  and then a local ImageWriter. Needs next release or build from source. 1.9.1 release is July 2017
* Changed print setup from pixel/mm to DPI;
* Added EPS/PS; issue with output (it appears blank in Ghostscript but the data is present!). Cannot set page size in FOP
  transcoder; PS bars are in black and white (Google drive and Dropbox can visualise ps, EPS will probably work in Open 
  Office - has been disabled in Office);

#### 29th January to 2nd February

* Added jfreechart charts; issues with:
  * Sizing and aspect ratio [Fixed]
  * SVG needs to be stylable
  * Title centering
  * PS bar in black and white
  * EPS could not be tested (needs Linux)
* SAHSULAND start year issue (probably missing data)
* SEER data extract population mal join (at least 20x expected); data OK
* RIF Java developer interview; hired!
* Fixed PS bar in black and white
* Code restructure - improved common SQL code, separate graphics directory, generic parameter code
* Implement user configurable table SQL for HTML reports
* Added numerator report
* Formatted dates and numbers correctly
* Export study tables button disabled during export. Complete message made permanent
* Fixed JSON study and comparison area dumps, added geotools to build
  Used http://geojson.io/#map=2/20.0/0.0
  
#### 5th to 9th February

* Added maps.
* Checked results - PG/MSSQLSmoothedResultManager.java population_per_area CTE needs:
  ```WHERE study_or_comparison = 'S'``` adding or you will get the wrong results if study and 
  comparison areas have the same geolevel. Added.
* Examination show that adjusted=1 rows are filled when covariates are used, adjusted=0 when
  not, so non need for extra filters on map tables 
* When testing with volume there is a timeout issue in the export, pushing the export button before
  the screen has finished startup porocessing causing it to forget it is running. Pushing it again
  can result is a corrupt export. May create and detect .sav ZIP file early in the export processing.
* Added shapefile support, columns needed to be renamed to 10 characters. Tested OK on both ports,
  added multipolygon/polygon support for SQL Server. Current in WGS84; can support original projection
  as the SRID is stored in RIF40_GEOGRAPHIES.
* Code restructure and comment; investigated using rif40_geographies SRID - needs more work beacuse of:
  ```NoSuchAuthorityCodeException: No code "EPSG:27700" from authority "EPSG" found for object of type "EngineeringCRS"```
	
  a) Could use SRID from database
  b) Probably needs to access an [external?] geotools datbase
  c) Some defaults may be hard codable

  No support at present for anything other than WGS85 in the shapefile code (i.e. re-projection required)
* Added CRS support, shapefile now in rif40_geographies SRID projection
* Added bounding box

#### 12th to 16th February

* Creation of test SVG map; 8 quantiles, PuOr colorbrewer pallette
* Added support for the usual output types; fixed issue with bounding box (database code is quick but wrong!)
* Add map legend [code running, not displaying]
* Maps now projected OK, but there are issues with resolution at high latititudes; possibly either a SQL Server
  issue or a bounds issue. Shapefile data is fine.
* Bounding box issue with database calulated BBOX.
* Makefile fixed
* Fixed grid and legend, solved resizing issue
* Fixed SQL Server mixed Polygon Mutlipolygon data issues (SQL SErver STasText() simplifies single polygon Multipolygon data to Polygon); 

#### 19th to 23rd February

* move shapefile creation to front of web service (so .shp.sav file is created as soon as possible);
* Expand map to rounded grid size, grey gridlines, add: projection and square sizes to legend; 
* Test margin expansion and limits; 
* Setup grid size correctly; 
* Create SLD for each map so styles can be used in GIS tools;
* Handle null data/R error values (-1);
* Add maps to report;
* Add geolevel 2 to complete boundaries for the extent of the map; 
* Zip file errors produce .err file for getExtractStatus() [zip file status] to detect;

#### 26th February to 2nd March

* Setup map scaling and pixel size correctly - use aspect ratio to decide % left expansion;
* Create RIFMapsParameters object with default derived from frontEndParameters.json5;
* RIFMapsParameters comment removing regex ((?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)) can use stack errors
  handlers installed.
* GeoTIFF support
* Report timeouts in front end export, add <export>.err file so failed exports can be detected;
* Add parameters to "disable" map vector grid (enableMapGrids), define copyright (copyrightInfo);
* Add title to TAG_IMAGE_DESCRIPTION TIFF metadata (and other images if supported) and on legend;
* Support .tfw file (plain text files that store X and Y pixel size, rotational information, and 
  world coordinates for a map); and .prj projection files
* Map grid background now white

#### 5th to 9th March

* Welcome Martin McCallion
* RIF deployment meeting with Hima
* RIF team meeting 
* Makefile improvements
* Rounding control, improvements to range displays
* Issues around geotools label positioning prevent its use. More work needed.

#### 12th to 16th March

* Added gender and investigation to maps;
* Support for user defined styles in maps;
* Investigate adding gender filter to Styling - not possible
* Fixed regex C style comment remover stack overflow issues. 

#### 19th to 23rd March

* Make mapping defaults configurable. Remaining issues:
  * Preset feature does not work. 
  * Color brewer does not change when expected going between user methods
  * Breaks are editable (should not be!) 
* Test MM changes and run study faults (both Postgres and SQL Server):
  1. A covariate must be used or you will get:

    ```
    ARWS - getRIFSubmissionFromXMLSource stop
    09:54:45.390 [https-jsse-nio-8080-exec-9] ERROR rifGenericLibrary.util.RIFLogger : [rifGenericLibrary.system.RIFServiceException]:
    Record "Investigation" field "Covariates" cannot be empty.
    ```
    This is not failing correctly so the user sees no error. I need to review all the code in this area so 
	that it fails properly and then fix the spurious error. (covariates can be empty!)  

    It is not obvious were the error crept in since it was working 2-3 weeks ago when I last created a test 
	study and no one has touched code in this area for a very long time.

    The JSON submitted looks like:
    ```
				"investigation": [{
                                                 ...
 
						"sex": "Both",
						"covariates": []
					}
				]
    ```
    2. R failure

	```
     Connect to database: SQLServer11
     R Error/Warning/Notice: Error in odbcSetAutoCommit(connDB, autoCommit = FALSE) : 
       first argument is not an open RODBC channel
    ```
    This I suspect is caused by the R interface is using the username as the password and should be easy to fix!

* Imperial network login tests
* Fix for R username=password failure

#### 26th to 30th March

* Fix for no covariates error; also fix for untrapped error and improved messages
* Suppressed Kev's irritating messages caused by logout not waiting for the tab control status timer to stop:
  * ERROR: API method "isLoggedIn" has a null "userID" parameter.
  * ERROR: Record "User" field "User ID" cannot be empty.
  * ERROR: Unable to roll back database transaction.
* Alerts:
  * Handle study state change without mal-reporting previous studies
  * Limit new/completed messages to within 14 days of message time
* Update TODO list; plans for next period

#### 12th to 13th April

* Review and test Postgres database build instructions; merging in Windows specific instructions and build README.md comments
* Review and test RIF Web Application and Middleware Installation instructions
* Fix log4j logging hangs (added separate taxonomyservices logger to avoid thread clash bug inm log4j)
* Fix for whitespace in properties file causing crashes
* Text VPN connection
* Download sample ICD9 codes: from https://raw.githubusercontent.com/drobbins/ICD9/master/icd9.txt; reformatted 
  from: https://www.cms.gov/ICD9ProviderDiagnosticCodes/downloads/cmsv29_master_descriptions.zip

#### 16th to 21st April

* Test running as daemon. Unable to export study data due to running out of memory
  - Error being trapped by front end but NOT in log.
  - HTTP Status 500 Internal Server Error
    ```
	Type Exception Report

	Message java.lang.OutOfMemoryError: Java heap space

	Description The server encountered an unexpected condition that prevented it from fulfilling the request.

	Exception


	java.lang.OutOfMemoryError: 	
		com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:420)
		com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)
		com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)
		javax.servlet.http.HttpServlet.service(HttpServlet.java:742)
		org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)\r\n</pre><p><b>Root Cause</b></p><pre>java.lang.OutOfMemoryError: Java heap space
		java.awt.image.DataBufferInt.&lt;init&gt;(DataBufferInt.java:75)
		java.awt.image.Raster.createPackedRaster(Raster.java:467)
		java.awt.image.DirectColorModel.createCompatibleWritableRaster(DirectColorModel.java:1032)
		java.awt.image.BufferedImage.&lt;init&gt;(BufferedImage.java:324)
		org.apache.batik.transcoder.image.JPEGTranscoder.createImage(JPEGTranscoder.java:55)
		org.apache.batik.transcoder.image.ImageTranscoder.transcode(ImageTranscoder.java:116)
		org.apache.batik.transcoder.XMLAbstractTranscoder.transcode(XMLAbstractTranscoder.java:142)
		org.apache.batik.transcoder.SVGAbstractTranscoder.transcode(SVGAbstractTranscoder.java:156)
		rifServices.graphics.RIFGraphics.graphicsTranscode(RIFGraphics.java:250)
		rifServices.graphics.RIFGraphics.addGraphicsFile(RIFGraphics.java:413)
		rifServices.graphics.RIFGraphics.addGraphicsFile(RIFGraphics.java:307)
		rifServices.dataStorageLayer.common.RifZipFile.addDenominator(RifZipFile.java:1102)
		rifServices.dataStorageLayer.common.RifZipFile.createStudyExtract(RifZipFile.java:443)
		rifServices.dataStorageLayer.pg.PGSQLStudyExtractManager.createStudyExtract(PGSQLStudyExtractManager.java:485)
		rifServices.dataStorageLayer.pg.PGSQLAbstractRIFStudySubmissionService.createStudyExtract(PGSQLAbstractRIFStudySubmissionService.java:1475)
		rifServices.restfulWebServices.pg.PGSQLAbstractRIFWebServiceResource.createZipFile(PGSQLAbstractRIFWebServiceResource.java:965)
		rifServices.restfulWebServices.pg.PGSQLRIFStudySubmissionWebServiceResource.createZipFile(PGSQLRIFStudySubmissionWebServiceResource.java:1239)
		sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
		sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
		java.lang.reflect.Method.invoke(Method.java:498)
		com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)
		com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)
		com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)
		com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)
		com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
		com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)
		com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
		com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)
		com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1542)
		com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1473)
		com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)
	Note The full stack trace of the root cause is available in the server logs.
	Apache Tomcat/8.5.29
    ```
  - Added trap for OutOfMemoryError; added instructions to setup on howto resolve	
* Test of fixing test 3 branch fails with:
  ```
  11:38:15.412 [http-nio-8080-exec-21] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLConnectionManager]:
  Unable to register user peter.
  getMessage:          SQLException: No suitable driver found for jdbc:postgresql://localhost:5432;databaseName=sahsuland
  getRootCauseMessage: SQLException: No suitable driver found for jdbc:postgresql://localhost:5432;databaseName=sahsuland
  getThrowableCount:   1
  getRootCauseStackTrace >>>
  java.sql.SQLException: No suitable driver found for jdbc:postgresql://localhost:5432;databaseName=sahsuland
	at java.sql.DriverManager.getConnection(DriverManager.java:689)
	at java.sql.DriverManager.getConnection(DriverManager.java:208)
	at rifServices.datastorage.ms.MSSQLAbstractSQLManager.createConnection(MSSQLAbstractSQLManager.java:297)
	at rifServices.datastorage.ms.MSSQLAbstractSQLManager.login(MSSQLAbstractSQLManager.java:213)
	at rifServices.datastorage.ms.MSSQLAbstractStudyServiceBundle.login(MSSQLAbstractStudyServiceBundle.java:113)
	at rifServices.datastorage.ms.MSSQLProductionRIFStudyServiceBundle.login(MSSQLProductionRIFStudyServiceBundle.java:63)
	at rifServices.rest.WebService.login(WebService.java:148)
  ```
* Test covariate extracts and maps with and without additional covariate (SES); OK: issues is with the reports. First year is not being inserted (caused by Postgres EXPLAIN VERBOSE); fixed;
  Rechecked all counts in extract and map tables, all OK
* Fix for incorrect denominator totals in report
* 2nd round of Postgres port tests.
* Added standalone documentation
* Upgrade Postgres install script to same capability as SQL Server script (can define DB and user); added support for pgpass.conf 
* Configured tomcat on wsrifdb1;
* wsrifdb1 is slow:
  * Network speed is around 10-20Mb/s (slower than my home broadband in rural Norfolk);
  * test 1002 took 120.7 compared to 83.1/37.6 on my desktop/laptop;
  * test 1003 took 281.1 compared to 167.1/82.5 on my desktop/laptop;
* SQL Server build and then install on wsrifdb2.
  * test 1002 took 54.8 compared to 41.4.6 on my desktop;
  * test 1003 took 108.6 compared to 92.7 on my desktop;
  [Note: no server memory tuning. SQL Server uses 20-100x more memory]
   
#### 23rd to 27th April
 
* ODBC setup issues - fully documented
* SQL Server build sahsuland OK, issues with installer sahsuland (no projects listed).
* XXE Security Vulnerability (prohibit external file parsing raised. Fixed.
  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/f75b538058fd43dc124660ad1bdf9290a259f23f/taxonomyServices/src/main/java/taxonomyServices/ICD10TaxonomyTermParser.java#L156
  https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
* Fix for missing rif40_projects/rif40_user_projects data
* Check R environment is setup correctly on rifServices start to assist tracing installation faults:
  - R_HOME in PATH
  - %R_HOME%/bin/x64 in PATH
  - %R_HOME%/library/rJava/jri/x64 in PATH
* Added ICD 11 support. Requires ClaML file from WHO, expected June 2018. Tested with duplicate of ICD 10. Will work if CLaML format is the same
* RIF meeting
* Test install on Fred's laptop. Issues with Postgres installer crashes:
  * Run as administrator needs to be clearer
  * How to edit the Windows environment
  * Postgres installer crashed caused by Windows 8.1/10 upgrade issue; needed to create a new Administrator account
* Patch demo laptop. Java upgrade had failed and Java needed to be installed;
* Test then merge rationalise_api into master.
  - Fixed R file separator escaping issue
  - Tidy logs
  - Put back covariate fix
  - Password is null; R does not handle this well
* Front end issues:
  - Package front end as WAR
  - Login screen: focus on user name field: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/22
  - Able to load, save and run risk analysis studies
  - Able to choose continuous covariate variables (not currently supported) https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/20
* Urgent database:
  - Fix predefined_groiup name issues - should be length 30 in t_rif40_inv_conditions: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/21
  - Save risk analysis bands as geojson in the database and the save file. Circle may need conversion to geojson (as a polygon of many, many short, linestrings).
  - Save selection options (study and comparison resolution and selection geolevels, geolevels selected.
  
#### 30th April to 4th May

* Fix for null R passwords; improved ODBC connect handler in R;
* Merge "Crossing the database streams". Tested on PG/SQl Server. R null password patch was missed;
* Finish Taxonomy services documentation;
* Dataloading documentation;
* Update TODO;
* Fix for PG/MSSQL JDBC driver incompatibilities;
* Build issue: Tests in error:
  ```
  getAgeGroups_EMPTY1(rifServices.dataStorageLayer.ms.AgeGenderYearManagerTest)
  getAgeGroups_EMPTY2(rifServices.dataStorageLayer.ms.AgeGenderYearManagerTest)
  getAgeGroups_EMPTY3(rifServices.dataStorageLayer.ms.AgeGenderYearManagerTest)
  getAgeGroups_NONEXISTENT1(rifServices.dataStorageLayer.ms.AgeGenderYearManagerTest)
  getAgeGroups_MALICIOUS1(rifServices.dataStorageLayer.ms.AgeGenderYearManagerTest)
  getAgeGroups_MALICIOUS2(rifServices.dataStorageLayer.ms.AgeGenderYearManagerTest)
  getAgeGroups_MALICIOUS3(rifServices.dataStorageLayer.ms.AgeGenderYearManagerTest)
  ```
* Tested pull 26: *Replaced use of hard-coded Windows paths in R with file.path 
  Tried to create:  rifDemo/scratchSpace/d/401/-/500/s/409/datarif40_run_R_env.bat on Windows; caused extract to fail:
  ```
  R temporary directory: c:\rifDemo\scratchSpace\d401-500\s409 was not created by Adj_Cov_Smooth_JRI.R
  ```
  Actually created: ```C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin\rifDemo\scratchSpace\d\401\-\500\s\409```
* Tested pull 30: Some more database-specific classes made common 
  - Added fix for PG/MSSQL JDBC driver incompatibilities;
  - Same basic problems as pull 26 with R directory paths on Windows;
  - Added NPE handler for issues coming from R directory path issue.
  
#### 7th to 11th May

* Data loading documentation; 
* Database administration manual;
* Merge R fixes;
* RIF meeting:
  * Will complete data loading manual and partial database administration guide [sections 2 and 3, PG 4]; then start on tile-maker;
  * Auto setup of R/tomcat environment and path to be added later;
  * Fixing risk analysis front end issues is a priority. Shapefile functionality needs to be investigated.
    Circles to not be saved as geojson; arbitrary shapes and plumes probably will. Which shapefle reading
    library has been used (Mike Bostock's?) needs to be determined. Method for determining geographic centroids
	(hopefully the ones built-in to the geoJSON)
  * Need for population weighted centroids: test on England data in 6-8 weeks time;
  
#### 14th to 18th May

* On holiday

#### 21st to 25th May

* Tilemaker fix when OS username != tilemaker user username;
* Tuning and performance for Postgrss in database management manual;
* Test USA tilemaker;
 - Fixes for unhandled file/directory errors causing hangs
 - Fix for xmlFileDir not longer exiting; use current working directory instead;
 - Made FMT file creation SQL Server only;
 - Support for latest node mssql library (breaking changes in recordSet structure). Should be backwards compatible; 
 - SQL server connect error caused by mssql/msnodesqlv8 version mismatch; 
   ```
   C:\Users\phamb\OneDrive\SEER Data\Tile maker USA>node C:\Users\%USERNAME%\Documents\GitHub\rapidInquiryFacility\rifNodeServices\mssqlTileMaker.js -U peter --password retep
	Created info log file: mssqlTileMaker.log
	About to connected to SQL server using: {
		"driver": "msnodesqlv8",
		"server": "localhost",
		"options": {
			"trustedConnection": false,
			"useUTC": true,
			"appName": "mssqlTileMaker.js"
		},
		"user": "peter",
		"password": "retep"
	}
	error [mssqlTileMaker:212:mssql_db_connect()] Could not connect to SQL server client using: {
		"driver": "msnodesqlv8",
		"server": "localhost",
		"options": {
			"trustedConnection": false,
			"useUTC": true,
			"appName": "mssqlTileMaker.js"
		},
		"user": "peter",
		"password": "retep",
		"port": 1433,
		"stream": false,
		"parseJSON": false
	}
	Error: None of the binaries loaded successfully. Is your node version either >= 0.12.7 or >= 4.2.x or >= 5.1.1 or >= 6.1.0
   ```
   Check you have [Visual Studio 2015/2017](https://www.visualstudio.com/downloads/) installed; launch it and logon to the Microsoft DEveloper network so the developer license is active;
   Remove *mssql* and *msnodesqlv8* from *package.json*;
   re-run: ```npm install msnodesqlv8 --save```:
   ```
   C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install msnodesqlv8 --save
	npm WARN optional SKIPPING OPTIONAL DEPENDENCY: fsevents@1.2.4 (node_modules\fsevents):
	npm WARN notsup SKIPPING OPTIONAL DEPENDENCY: Unsupported platform for fsevents@1.2.4: wanted {"os":"darwin","arch":"any"} (current: {"os":"win32","arch":"x64"})

	+ msnodesqlv8@0.1.46
	updated 1 package in 10.388s   
   ```
   re-run: ```npm install mssql --save```:
   ```
   C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install mssql --save
	npm WARN optional SKIPPING OPTIONAL DEPENDENCY: fsevents@1.2.4 (node_modules\fsevents):
	npm WARN notsup SKIPPING OPTIONAL DEPENDENCY: Unsupported platform for fsevents@1.2.4: wanted {"os":"darwin","arch":"any"} (current: {"os":"win32","arch":"x64"})

	+ mssql@4.1.0
	added 12 packages in 11.196s
   ```
* Test SEER pre-processing and load scripts;
* Test and merge pull requests #32, #33. Issues with #34;
* Basic Tile Maker documentation;
* More work on data loading documentation
* Need to remove check:
  ```
  Msg 51147, Level 16, State 1, Server DESKTOP-4P2SA80, Procedure tr_rif40_tables_checks, Line 45
  Table name: [rif40].[rif40_tables], Cannot DELETE from RIF40_TABLES
  ```
* RIF meeting. Need to discuss instsallation with CDC;
* Test and merge pull #34 rename all packages to follow Java conventions
  - Whilst resolving issues (malformed lowercase URL - http://localhost:8080/taxonomyservices/taxonomyservices/getMatchingTerms)
    It was found that getJsonFile() was calling the *taxonomyservices* using *wpea-rif1* which it was not obtaining from 
    the configs but the calling URL hostname. This is suspected to be a Windows bug, but it is difficult to 
    re-create. The workaround is set the parameter *taxonomyServicesServer* to *http://localhost:8080*;
    Middleware:	
	```
	Call getJsonFile() using URL: http://wpea-rif1:8080
    13:09:31.774 [http-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.RIFLogger : [org.sahsu.rif.services.datastorage.common.GetStudyJSON]:
    Using taxonomyServicesServer parameter for base URL: http://localhost:8080
	```
	Front end:
	```
	11:18:36.779 [http-nio-8080-exec-7] WARN  org.sahsu.rif.generic.util.FrontEndLogger : 
	userID:       peter
	browser type: Firefox; v60
	iP address:   0:0:0:0:0:0:0:1
	message:      RIF services to taxonomy services link had error; please see logs
	error stack>>>
	rifMessage@http://localhost:8080/RIF4/utils/controllers/rifc-util-alert.js:293:10
	$scope.showWarning@http://localhost:8080/RIF4/utils/controllers/rifc-util-alert.js:366:5
	$scope.getBlobJob/$scope.extractStatus<@http://localhost:8080/RIF4/dashboards/export/controllers/rifc-expt-save.js:76:9
	e/<@http://localhost:8080/RIF4/libs/standalone/angular.min.js:131:20
	$eval@http://localhost:8080/RIF4/libs/standalone/angular.min.js:145:343
	$digest@http://localhost:8080/RIF4/libs/standalone/angular.min.js:142:412
	$apply@http://localhost:8080/RIF4/libs/standalone/angular.min.js:146:111
	l@http://localhost:8080/RIF4/libs/standalone/angular.min.js:97:320
	J@http://localhost:8080/RIF4/libs/standalone/angular.min.js:102:34
	gg/</t.onload@http://localhost:8080/RIF4/libs/standalone/angular.min.js:103:4
	<<<
	actual time:  24/05/2018 11:18:36
	relative:     +135.4

	11:18:36.801 [http-nio-8080-exec-7] INFO  org.sahsu.rif.generic.util.FrontEndLogger : 
	userID:       peter
	browser type: Firefox; v60
	iP address:   0:0:0:0:0:0:0:1
	message:      Taxonomy services error message: http://wpea-rif1:8080/taxonomyservices/taxonomyservices/getMatchingTerms failed: HTTP error code : 404
	actual time:  24/05/2018 11:18:36
	relative:     +135.4
	```
* Test and merge pull #37: Creating the web application as a war file, pull #36 error loading a saved 
  study with no Bayesian Smoothing
 
#### 29th May to 1st June
 
* Test and merge pull #39: added version numbering;
* Complete database management manual;
* Test SEER data; completed data loading manual;

#### 4th to 8th June

* TileMaker manual, testing, handling huge shapefiles;
* Shapefile pre-processing; EWS2011 as an example;
* Processed EWS2011 to LSOA OK, COA will crash below [batch ran to completion, problems occurred returning the run data]; documented memory management and 
  issues; fix DBF parsing fault for very small DBF files;
* Strange bug when processing COA as well:
  ```
  C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServicesCommon.js:932
									throw e;
									^

	RangeError: Invalid string length
		at JSON.stringify (<anonymous>)
		at responseProcessingAddStatusCallback (C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServicesCommon.js:299:23)
		at addStatusWriteDiagnosticFileRename (C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServicesCommon.js:923:10)
		at FSReqWrap.oncomplete (fs.js:135:15)
  ```
  Memory seems OK at: 24G!
* QGIS edit of DBF or mapshaper added ASCII NUL characters to pad strings to the same length. These had to be removed by ```string.replace(/\0/g, '')``` to remove an error the 
  ```psql \copy``` command;
* Performance problem traced to size and validity of the input shapefile:
  Take care to ensure the the input shapefile is valid (i.e. use the ```-clean``` flag in *mapshaper*); and keep the boundary maps simple. If they are mapped at high scale 
  they tend to contain many small islands which can become invalid during simplification. These take a long time to fix; for the UK no pre-simplification took two hours to  
  fix, 98% simplification took 7 seconds;
* Need to add invalid geometries report prior to *Make geometry columns valid*:
  ```SQL
  SELECT SUM(CASE WHEN ST_IsValid(geom_6) THEN 1 ELSE 0 END) AS geom_6_valid,
       SUM(CASE WHEN ST_IsValid(geom_7) THEN 1 ELSE 0 END) AS geom_7_valid,
       SUM(CASE WHEN ST_IsValid(geom_8) THEN 1 ELSE 0 END) AS geom_8_valid,
       SUM(CASE WHEN ST_IsValid(geom_9) THEN 1 ELSE 0 END) AS geom_9_valid,
       SUM(CASE WHEN ST_IsValid(geom_orig) THEN 1 ELSE 0 END) AS geoom_orig_valid,
	   COUNT(*) total
  FROM cntry2011;
  ```  
* Make post processing script multi transactional (hit by deadlock problems running overnight;
* Postgres processing failed after 2:26 (hours) at statement 421/601 hierarchy checks (check_intersections.sql):
  ```
  psql:pg_EWS2011.sql:6524: WARNING:  Geography: EWS2011 geolevel 7: [coa2011] spurious additional codes: 2
  ...
  psql:pg_EWS2011.sql:6524: INFO:  Geography: EWS2011 geolevel 1: [scntry2011] no multiple hierarchy codes
  psql:pg_EWS2011.sql:6524: INFO:  Geography: EWS2011 geolevel 2: [cntry2011] no multiple hierarchy codes
  psql:pg_EWS2011.sql:6524: INFO:  Geography: EWS2011 geolevel 3: [gor2011] no multiple hierarchy codes
  psql:pg_EWS2011.sql:6524: INFO:  Geography: EWS2011 geolevel 4: [ladua2011] no multiple hierarchy codes
  psql:pg_EWS2011.sql:6524: INFO:  Geography: EWS2011 geolevel 5: [msoa2011] no multiple hierarchy codes
  psql:pg_EWS2011.sql:6524: INFO:  Geography: EWS2011 geolevel 6: [lsoa2011] no multiple hierarchy codes
  psql:pg_EWS2011.sql:6524: ERROR:  Geography: EWS2011 codes check 0 missing, 1 spurious additional, 0 hierarchy fails
  CONTEXT:  PL/pgSQL function inline_code_block line 41 at RAISE
  Time: 1714.103 ms
  ```
  Hierarchy insert took 83 mins. Two  rows missing romn the hierarchy are:
  ```
  sahsuland_dev=> SELECT coa2011 FROM lookup_coa2011
  sahsuland_dev->                 EXCEPT 
  sahsuland_dev->                 SELECT coa2011 FROM hierarchy_ews2011;
    coa2011
  -----------
   W00010143
   W00010161
  (2 rows)
  ```
  These are in Cardiff and are small:
  ```
  SELECT coa2011, lsoa11_1, lad11nm, msoa11nm, area_km2, geographic_centroid_wkt, ST_ASText(ST_Transform(geographic_centroid, 27700)) AS osgb
    FROM coa2011
   WHERE coa2011 IN ('W00010143', 'W00010161');
    coa2011  | lsoa11_1  | lad11nm |  msoa11nm   |    area_km2     |            geographic_centroid_wkt            |                   osgb
  -----------+-----------+---------+-------------+-----------------+-----------------------------------------------+------------------------------------------
   W00010161 | W01001945 | Cardiff | Cardiff 048 | 0.0147534816105 | POINT (-3.1781555521064697 51.45499419539898) | POINT(318235.967585802 173549.01243282)
   W00010143 | W01001945 | Cardiff | Cardiff 048 |  0.009281476807 | POINT (-3.1768272276630127 51.45381197706475) | POINT(318326.146578801 173416.053359773)
  (2 rows)
  ```
  That COA2011 only is affected means that the upper intersections are fine.
  
#### 11th to 15th June 
  
The hierarchy check failure is probably caused by oversimplification of higher layers (SCNTRY, CNTRY):

* CNTRY2011 in purple;
* GOR2011 (not oversimplified) in green;
* COA2001 in hashing;
  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/cardiff_COA_issue.png?raw=true "Cardiff COA2001 issue map")
  To be in the hierarchy the intersection code *insert_hierarchy.sql* selects the intersection with the largest intersection by area for each (higher resolution). This 
  eliminates duplicates and picks the most likely intersection on the basis of area. There are two possible reasons for this failure:

* An intersection was not found. Visually this appears to not be the case;
* The area is zero. This seems unlikely and would need to be tested in SQL.

The following SQL was derived from code generated by *insert_hierarchy.sql*:

```SQL  
WITH x12 AS ( /* Subqueries x12 ... x67: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a1.areaid AS scntry2011, a2.areaid AS cntry2011,
       ST_Area(a2.geom_9) AS a2_area,
       ST_Area(ST_Intersection(a1.geom_9, a2.geom_9)) AS a12_area
  FROM scntry2011 a1 CROSS JOIN cntry2011 a2
 WHERE ST_Intersects(a1.geom_9, a2.geom_9)
), x23 AS ( /* Subqueries x23 ... x67: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a2.areaid AS cntry2011, a3.areaid AS gor2011,
       ST_Area(a3.geom_9) AS a3_area,
       ST_Area(ST_Intersection(a2.geom_9, a3.geom_9)) AS a23_area
  FROM cntry2011 a2 CROSS JOIN gor2011 a3
 WHERE ST_Intersects(a2.geom_9, a3.geom_9)
), x34 AS ( /* Subqueries x34 ... x67: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a3.areaid AS gor2011, a4.areaid AS ladua2011,
       ST_Area(a4.geom_9) AS a4_area,
       ST_Area(ST_Intersection(a3.geom_9, a4.geom_9)) AS a34_area
  FROM gor2011 a3 CROSS JOIN ladua2011 a4
 WHERE ST_Intersects(a3.geom_9, a4.geom_9)
), x45 AS ( /* Subqueries x45 ... x67: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a4.areaid AS ladua2011, a5.areaid AS msoa2011,
       ST_Area(a5.geom_9) AS a5_area,
       ST_Area(ST_Intersection(a4.geom_9, a5.geom_9)) AS a45_area
  FROM ladua2011 a4 CROSS JOIN msoa2011 a5
 WHERE ST_Intersects(a4.geom_9, a5.geom_9)
), x56 AS ( /* Subqueries x56 ... x67: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a5.areaid AS msoa2011, a6.areaid AS lsoa2011,
       ST_Area(a6.geom_9) AS a6_area,
       ST_Area(ST_Intersection(a5.geom_9, a6.geom_9)) AS a56_area
  FROM msoa2011 a5 CROSS JOIN lsoa2011 a6
 WHERE ST_Intersects(a5.geom_9, a6.geom_9)
), x67 AS ( /* Subqueries x67 ... x67: intersection aggregate geometries starting from the lowest resolution.
	       Created using N-1 geoevels cross joins rather than 1 to minimise cross join size and hence improve performance.
	       Calculate the area of the higher resolution geolevel and the area of the intersected area */
SELECT a6.areaid AS lsoa2011, a7.areaid AS coa2011,
       ST_Area(a7.geom_9) AS a7_area,
       ST_Area(ST_Intersection(a6.geom_9, a7.geom_9)) AS a67_area
  FROM lsoa2011 a6 CROSS JOIN coa2011 a7
 WHERE ST_Intersects(a6.geom_9, a7.geom_9)
   AND a7.coa2011 IN ('W00010143','W00010161')
)
SELECT x12.scntry2011, 
       x12.cntry2011, 
       x23.gor2011, 
       x34.ladua2011, 
       x45.msoa2011, 
       x56.lsoa2011, 
       x67.coa2011, 
       CASE WHEN x12.a2_area > 0 THEN x12.a12_area/x12.a2_area ELSE NULL END test12,
       MAX(x12.a12_area/x12.a2_area) OVER (PARTITION BY x12.cntry2011) AS max12,
       CASE WHEN x23.a3_area > 0 THEN x23.a23_area/x23.a3_area ELSE NULL END test23,
       MAX(x23.a23_area/x23.a3_area) OVER (PARTITION BY x23.gor2011) AS max23,
       CASE WHEN x34.a4_area > 0 THEN x34.a34_area/x34.a4_area ELSE NULL END test34,
       MAX(x34.a34_area/x34.a4_area) OVER (PARTITION BY x34.ladua2011) AS max34,
       CASE WHEN x45.a5_area > 0 THEN x45.a45_area/x45.a5_area ELSE NULL END test45,
       MAX(x45.a45_area/x45.a5_area) OVER (PARTITION BY x45.msoa2011) AS max45,
       CASE WHEN x56.a6_area > 0 THEN x56.a56_area/x56.a6_area ELSE NULL END test56,
       MAX(x56.a56_area/x56.a6_area) OVER (PARTITION BY x56.lsoa2011) AS max56,
       CASE WHEN x67.a7_area > 0 THEN x67.a67_area/x67.a7_area ELSE NULL END test67,
       MAX(x67.a67_area/x67.a7_area) OVER (PARTITION BY x67.coa2011) AS max67
  FROM x12, x23, x34, x45, x56, x67
 WHERE x12.cntry2011 = x23.cntry2011
   AND x23.gor2011 = x34.gor2011
   AND x34.ladua2011 = x45.ladua2011
   AND x45.msoa2011 = x56.msoa2011
   AND x56.lsoa2011 = x67.lsoa2011
 ORDER BY 1, 2, 3, 4, 5, 6, 7; 
```

This, unsurprisingly, returned no rows, suggesting the problem is with the intersection and not the area:
```
  scntry2011 | cntry2011 | gor2011 | ladua2011 | msoa2011 | lsoa2011 | coa2011 | test12 | max12 | test23 | max23 | test34 | max34 | test45 | max45 | test56 | max56 | test67 | max67
 ------------+-----------+---------+-----------+----------+----------+---------+--------+-------+--------+-------+--------+-------+--------+-------+--------+-------+--------+-------
 (0 rows)

```
This in turn implies the problem may be with the COA2011, LSOA2011 intersection, common table expression: *x67*; as shown by the map. The records will be manually inserted to fix the problem.
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/cardiff_COA_issue2.png?raw=true "Cardiff COA2001 intersection issue map")

```SQL
WITH a AS (
	SELECT DISTINCT scntry2011,cntry2011, gor2011, ladua2011, msoa2011, lsoa2011
	  FROM hierarchy_ews2011
	 WHERE lsoa2011 = 'W01001945' /* Where it should be */
), b AS (
	SELECT coa2011, lsoa11_1
      FROM coa2011
     WHERE coa2011 IN ('W00010143', 'W00010161') 
)
INSERT INTO hierarchy_ews2011 (scntry2011, cntry2011, gor2011, ladua2011, msoa2011, lsoa2011, coa2011)
SELECT a.*, b.coa2011
  FROM a, b
 WHERE a.lsoa2011 = b.lsoa11_1
   AND b.coa2011 NOT IN (SELECT coa2011 FROM hierarchy_ews2011);   

   INSERT 0 2
```

* Fix for: Focus should be on username field on the login screen #22;
* Can now reload saved risk analysis study;
* Found shapefile risk analysis areas codes;
* Added test shapefiles for exposure point and surfaces;
* WARNING: Could not find (weighted) centroids stored in database - using geographic centroids on the fly
  call to user.getTileMakerCentroids() in rifd-dsub-maptable.js is producing a HTTP 404:
  ```http://localhost:8080/rifServices/studyResultRetrieval/getTileMakerCentroids?userID=peter&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL1```. 
  Submitted an as issue;
* RIF meeting;
* Buffer cache tuning for Postgres;
* Document pgTileMaker or mssqlTileMaker JavaScript heap out of memory error;
* Created add_study_selection_to_json branch. Added ability to view JSOBN shapes used in study selection (e.g. the concentric circle);
* Run EWS2011 pre processing script pg_EWS2011.sql to end after hierarchy fix. Took about a day and a half. Ran pgTileMaker -needed more memory. Took 3 hours.

#### 18th to 22nd June

* Test and merge *Tidies up the query formatters #43*;
* Document *tilemaker* hierarchy issues;
* Apply Postgres fix for EWS2011 hierarchy issues to SQL Server;
* Resolve adjacency list tuning issues >1 day to 3 minutes;
* Update SQL Server tuning comments;
* Create 2x test noise band shapefiles for sahsuland from Heathrow 2013 day noise;
* Resolve front end shapefile loading issues for banded data;
* Received population weighted centroids from Aina;
* Run EWS2011 pre processing script mssql_EWS2011.sql to end after hierarchy and performance fixes overnight. Ran mssqlTileMaker -needed more memory. Took 1 hour;
* Loaded EWS2011 into SQL Server and PostGres;
* *TileViewer* example - Lower super output area in south east London:
  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/TileViewer_example.PNG?raw=true "TileViewer example - Lower super output area in south east London")  

#### 25th to 29th June

* Upgraded wsrifdb1 and 2 to latest patch level; secured tomcat to OWASP guidelines; added US SEER dataset; fixed stop/start_rif.bat; made to start on boot;
* Made SQL Server SEER load script re-runnable using MERGE statements (removed DELETE FROM/INSERT rif40 tables);
* Fix for lack of polygons on returning to the study areas screen; fix for map synchronisation problems (slowed it down 0.5s as usual - map.whenReady() isn't!);
* Clear selection now zooms back to full extent; shapefile selection only zooms to study extent;
* R error on SQL Server port only with SEER data:
  ```
  getRootCauseMessage: RIFServiceException: R script execution error; trace:
	EXTRACT TABLE NAME: rif_studies.s4_extract
	Saving extract frame to: scratchSpace/d1-100/s4/datatmp_s4_extract.csv
	rif_studies.s4_extract numberOfRows=745322==
	rif40_GetAdjacencyMatrix numberOfRows=712==
	Saving adjacency matrix to: scratchSpace/d1-100/s4/datatmp_s4_adjacency_matrix.csv
	Covariates: MEDIAN_HH_INCOME_QUIN
	Stack tracer >>>

	 performSmoothingActivity.R#710: .handleSimpleError(function (obj) 
	{
		ca FUN(X[[i]], ...) lapply(X = X, FUN = FUN, ...) performSmoothingActivity.R#709: sapply(x, FUN = function(y) {
		ans = y
	  performSmoothingActivity.R#106: findNULL(data[, i.d.adj[i]]) performSmoothingActivity(data, AdjRowset) Adj_Cov_Smooth_JRI.R#372: withVisible(expr) Adj_Cov_Smooth_JRI.R#372: withCallingHandlers(withVisible(expr), error = er withErrorTracing({
		data = fetchExtractTable()
		AdjRowset = getAdjace doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(expr, names, parentenv, handlers[[1]]) tryCatchList(expr, names[-nh], parentenv, handlers[-nh]) doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(tryCatchList(expr, names[-nh], parentenv, handlers[-nh]), names tryCatchList(expr, classes, parentenv, handlers) tryCatch({
		withErrorTracing({
			data = fetchExtractTable()
		   eval(expr, pf) eval(expr, pf) withVisible(eval(expr, pf)) evalVis(expr) Adj_Cov_Smooth_JRI.R#411: capture.output({
		tryCatch({
			withError runRSmoothingFunctions() 
	<<< End of stack tracer.
	callPerformSmoothingActivity() ERROR:  missing value where TRUE/FALSE needed ; call stack:  if 
	callPerformSmoothingActivity() ERROR:  missing value where TRUE/FALSE needed ; call stack:  y == "NULL" 
	callPerformSmoothingActivity() ERROR:  missing value where TRUE/FALSE needed ; call stack:  {
		ans = 0
	} 
	callPerformSmoothingActivity exitValue: 1
  ```
  Traced to missing patch applied to Postgres (rif40_create_insert_statement.sql). Mal-join of denominator covariate data in extract;
  **Checked numbers were the same for both ports!**
* Mysterious error on wsrifdb2:
  ```
  10:49:56.827 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [TaxonomyLogger]: Created TaxonomyLogger: org.sahsu.rif.generic.util.TaxonomyLogger
  10:49:56.843 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [TaxonomyLogger]: Set java.util.logging.manager=org.apache.logging.log4j.jul.LogManager
  10:49:56.843 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.RIFTaxonomyWebServiceApplication]:
  !!!!!!!!!!!!!!!!!!!!! RIFTaxonomyWebServiceApplication !!!!!!
  10:49:58.155 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfigurationXMLReader]:
  TaxonomyService configuration file: C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\TaxonomyServicesConfiguration.xml
  10:49:58.186 [https-jsse-nio-8080-exec-5] ERROR org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.claMLTaxonomyService]:
  ICD10/11 taxonomy service: ICD Taxonomy Service initialiseService() error
  getMessage:          RIFServiceException: ICD10/11 taxonomy service: ICD Taxonomy Service file: "org.sahsu.rif.generic.concepts.Parameter@131041c4" not found.
  getRootCauseMessage: RIFServiceException: ICD10/11 taxonomy service: ICD Taxonomy Service file: "org.sahsu.rif.generic.concepts.Parameter@131041c4" not found.
  getThrowableCount:   1
  getRootCauseStackTrace >>>
  1 error(s). Error code is 'HEALTH_CODE_TAXONOMY_SERVICE_ERROR'. Message list is: 'ICD10/11 taxonomy service: ICD Taxonomy Service file: "org.sahsu.rif.generic.concepts.Parameter@131041c4" not found.'
  	at org.sahsu.taxonomyservices.claMLTaxonomyService.initialiseService(claMLTaxonomyService.java:170)
  	at org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfigurationXMLReader.readFile(TaxonomyServiceConfigurationXMLReader.java:206)
  	at org.sahsu.rif.generic.taxonomyservices.FederatedTaxonomyService.initialise(FederatedTaxonomyService.java:115)
  	at org.sahsu.taxonomyservices.RIFTaxonomyWebServiceResource.initialiseService(RIFTaxonomyWebServiceResource.java:83)
	...
	<<< End getRootCauseStackTrace.
	10:55:25.306 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [TaxonomyLogger]: Created TaxonomyLogger: org.sahsu.rif.generic.util.TaxonomyLogger
	10:55:25.322 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [TaxonomyLogger]: Set java.util.logging.manager=org.apache.logging.log4j.jul.LogManager
	10:55:25.322 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.RIFTaxonomyWebServiceApplication]:
	!!!!!!!!!!!!!!!!!!!!! RIFTaxonomyWebServiceApplication !!!!!!
	10:55:26.587 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfigurationXMLReader]:
	TaxonomyService configuration file: C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\TaxonomyServicesConfiguration.xml
	10:55:27.134 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.ICD10TaxonomyTermParser]:
	ICD10TaxonomyTermParser 2
	10:55:49.312 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.claMLTaxonomyService]:
	icd101/1TaxonomyParser: ICD Taxonomy Service read: "C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\icdClaML2016ens.xml".
	10:55:49.312 [https-jsse-nio-8080-exec-5] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.claMLTaxonomyService]:
	icd101/1TaxonomyParser: ICD Taxonomy Service initialised: International classification of diseases and related health problems 10th revision (2016 version)..
	11:01:46.608 [https-jsse-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [TaxonomyLogger]: Created TaxonomyLogger: org.sahsu.rif.generic.util.TaxonomyLogger
	11:01:46.639 [https-jsse-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [TaxonomyLogger]: Set java.util.logging.manager=org.apache.logging.log4j.jul.LogManager
	11:01:46.639 [https-jsse-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.RIFTaxonomyWebServiceApplication]:
	!!!!!!!!!!!!!!!!!!!!! RIFTaxonomyWebServiceApplication !!!!!!
	11:01:48.076 [https-jsse-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfigurationXMLReader]:
	TaxonomyService configuration file: C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\TaxonomyServicesConfiguration.xml
	11:01:49.795 [https-jsse-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.ICD10TaxonomyTermParser]:
	ICD10TaxonomyTermParser 2
	11:02:13.651 [https-jsse-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.claMLTaxonomyService]:
	icd101/1TaxonomyParser: ICD Taxonomy Service read: "C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\icdClaML2016ens.xml".
	11:02:13.651 [https-jsse-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.TaxonomyLogger : [org.sahsu.taxonomyservices.claMLTaxonomyService]:
	icd101/1TaxonomyParser: ICD Taxonomy Service initialised: International classification of diseases and related health problems 10th revision (2016 version)..	
  ```
* Add shapefile properties data to selector modal;

#### 2nd to 6th June

* Add support for *hierarchy_post_processing_sql*, *population_weighted_centroids*;
* Risk analysis front end:
  * Check for no bands setup with limits;
  * Reverse shapefile band columns so the same as points (red innermost);
  * Colour shapefile bands; 
  * Check all bands etc restore correctly;
  * Added shapefile properties to map; 
  * Switch to mouseover/mouseout to be consistent; improve info box to help. Confirmed not possible to share mouseover/mouseout between layers
  
#### 9th to 13th June  
  
* Risk analysis front end:
  * Fixed (freehand diease mapping) shape selector;
  * Added area to all shapes;
  * Emphasise centroid point when area selected;
  * Save/restore user selection methods to/from file;
  * Add user selection methods to study submission XML;
* Risk analysis middleware todo:
  * Add user selection methods to XML parse: ```Unable to convert JSON stream to XML stream```; middleware trace. JSON is valid, probably causing by $$hashkey properties copying as part of UI grid: 
    ```
	14:41:16.428 [http-nio-8080-exec-3] ERROR org.sahsu.rif.generic.util.RIFLogger : [org.sahsu.rif.services.fileformats.RIFStudySubmissionXMLReader]:
	Caught exception in RIFStudySubmissionXMLReader.readFile(InputStream)
	getMessage:          SAXParseException: The content of elements must consist of well-formed character data or markup.
	getRootCauseMessage: SAXParseException: The content of elements must consist of well-formed character data or markup.
	getThrowableCount:   1
	getRootCauseStackTrace >>>
	org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 106494; The content of elements must consist of well-formed character data or markup.
		at com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper.createSAXParseException(ErrorHandlerWrapper.java:203)
		at com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper.fatalError(ErrorHandlerWrapper.java:177)
		at com.sun.org.apache.xerces.internal.impl.XMLErrorReporter.reportError(XMLErrorReporter.java:400)
		at com.sun.org.apache.xerces.internal.impl.XMLErrorReporter.reportError(XMLErrorReporter.java:327)
		at com.sun.org.apache.xerces.internal.impl.XMLScanner.reportFatalError(XMLScanner.java:1472)
		at com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl$FragmentContentDriver.startOfMarkup(XMLDocumentFragmentScannerImpl.java:2635)
		at com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl$FragmentContentDriver.next(XMLDocumentFragmentScannerImpl.java:2732)
		at com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl.next(XMLDocumentScannerImpl.java:602)
		at com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.scanDocument(XMLDocumentFragmentScannerImpl.java:505)
		at com.sun.org.apache.xerces.internal.parsers.XML11Configuration.parse(XML11Configuration.java:841)
		at com.sun.org.apache.xerces.internal.parsers.XML11Configuration.parse(XML11Configuration.java:770)
		at com.sun.org.apache.xerces.internal.parsers.XMLParser.parse(XMLParser.java:141)
		at com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser.parse(AbstractSAXParser.java:1213)
		at com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl$JAXPSAXParser.parse(SAXParserImpl.java:643)
		at com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl.parse(SAXParserImpl.java:327)
		at javax.xml.parsers.SAXParser.parse(SAXParser.java:195)
		at org.sahsu.rif.services.fileformats.RIFStudySubmissionXMLReader.readFile(RIFStudySubmissionXMLReader.java:164)
		at org.sahsu.rif.services.rest.WebService.getRIFSubmissionFromXMLSource(WebService.java:1207)
		at org.sahsu.rif.services.rest.WebService.getRIFSubmissionFromJSONSource(WebService.java:1183)
		at org.sahsu.rif.services.rest.WebService.submitStudy(WebService.java:1126)
		at org.sahsu.rif.services.rest.StudySubmissionServiceResource.submitStudy(StudySubmissionServiceResource.java:1132)
    
	14:41:16.440 [http-nio-8080-exec-3] ERROR org.sahsu.rif.generic.util.RIFLogger : [org.sahsu.rif.services.rest.StudySubmissionServiceResource]:
	StudySubmissionServiceResource.submitStudy error
	getMessage:          RIFServiceException: Unable to convert JSON stream to XML stream.
	getRootCauseMessage: RIFServiceException: Unable to convert JSON stream to XML stream.
	getThrowableCount:   1
	getRootCauseStackTrace >>>
	1 error(s). Error code is 'UNABLE_TO_PARSE_JSON_SUBMISSION'. Message list is: 'Unable to convert JSON stream to XML stream.'
		at org.sahsu.rif.services.rest.WebService.getRIFSubmissionFromJSONSource(WebService.java:1190)
		at org.sahsu.rif.services.rest.WebService.submitStudy(WebService.java:1126)
		at org.sahsu.rif.services.rest.StudySubmissionServiceResource.submitStudy(StudySubmissionServiceResource.java:1132)	
	```
  * Add user selection methods to database insert;
  * Add user selection methods to DB study save;
  * Add population weighted centroid support (pop_x, pop_y) to getTileMakerCentroids();
  * Create and run PG/MSSQL scripts to load population weighted centroid and update lookup tables; MSSQL script is dependent on the PG script;
  
#### 16th to 20th July

* Fix unable to convert JSON stream to XML stream error;
* Add support for risk analysis type;
* Select by freehand polygons disabled for risk analysis;
* Add band and number of selected polygons to info control;
* For no numerator/denominators setup errors so intelligible;
* Add support for population weighted centroids; in green;
* Fix inferior (i.e. within or on edge of) bands;
* Remove points from study selection; 
* Test EWS2011 geography and centroids in RIF:
  * OK to MSOA level (~9000 centroids) with acceptable performance.

#### 23rd to 27th July

* Added selection by postal code; WGS 84 lat/long SRID X/Y;
* Regression test pull #47 (Change database access in R to use RJDBC) and add_study_selection_to_json;
* Progress of shapefile display setup after "apply" button in shapefile load modal;
  
#### 30th July to 3rd August

* Regression and fix test pull #47 (Change database access in R to use RJDBC) and merge branch add_study_selection_to_json:
  * Had to use *RODBC* on SQL Server due to *RJDBC* error:
    ``` 
	saveDataFrameToDatabaseTable() ERROR: execute JDBC update query failed in dbSendUpdate (The incoming tabular data stream (TDS) remote procedure call (RPC) protocol stream is incorrect. Parameter 5 (""): The supplied value is not a valid instance of data type float. Check the source data for invalid values. An example of an invalid value is data of numeric type with scale greater than precision.
	```
  * Fixed error handlers;
  * Improved tests for *odbcDataSourceName*.  
* "Green" display shapefile selection and centroids display when selected;  
* Remove disabled AOI code and clear AOI button;
* Fixed missing zoom to extent button;
* Disable "apply" button in shapefile load modal after pushed (so you don't do it twice while waiting);
* Check the following selection methods are saved to study selection:
  * (e.g. by attribute: DB; bands 69, 63, 0);
  * file names;
  * geolevel select and list;
* Merge *Add study selection to json #54*. One issue with SQL Server: *warning: You must enter a valid Postal Code* with validated postcode. 
  Also need to run validation if you click in the other fields.
* Risk analysis front end issues/todo:
  * Fix height interaction with shapefile selector modal for smaller heights then fixed modal height;
  * Improve scaling in shapefile properties table, 40:60 split;
  * Issue if with selectionState if "cancel" button used - shape remains! Needs a transaction time so the shape tree can be pruned;
  * Separate in selectionState map display methods from maptable directive;
  * On-screen toggle between disease mapping and risk analysis doesn't work properly #52: 
    https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/52. Tested OK on RA front end branch (add_study_selection_to_json). 
	Added tracer. 
  * SQL Server: *warning: You must enter a valid Postal Code* with validated postcode. 
    Also need to run validation if you click in the other fields.
  
#### 6th to 10th August

* Alter 10 risk analysis database changes. (See TODO](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/wiki/TODO#database-issues):	 
  1. Save/restore user selection methods to/from database;	
  2. Save user print selection to/from database;	
  3. The column predefined_group_name in the table t_rif40_inv_conditions is defined as varchar(5) in Postgres. It should be varchar(30);
  4. *rif40_homogeneity*:
  
	| Column name      | Column description                                                                  |
	|------------------|-------------------------------------------------------------------------------------| 
	| study_id[PK][FK] | rif40_studies.study_id                                                              | 
	| inv_id[PK][FK]   | rif40_investigations.inv_id                                                         | 
	| adjusted[PK]     | 0 or 1 indicating adjusted/unadjusted results                                       | 
	| genders[PK]      | 1, 2 or 3, indicating Males, Females or Both                                        | 
	| homogeneity_dof  | the number of degrees of freedom                                                    | 
	| homogeneity_chi2 | the chi2-value for the homogeneity test                                             | 
	| homogeneity_p    | the p-value for the homogeneity test                                                | 
	| linearity_chi2   | the chi2-value for the linearity test                                               | 
	| linearity_p      | the p-value for the linearity test                                                  | 
	| explt5           | the number of bands in the study which have an expected number of cases less than 1 | 
  
  5. Add unique keys to description files on rif tables/projects/health themes to protect against the middleware using them as a key;
  6. Add default background layer support for geography (so sahsuland has no background);  
* Alter 10 new REST calls:
  * getMapBackground
  * getSelectState
  * getPrintState
  * setPrintState  
* Set map background by geography, constrain map background filed; parse testing study_selection in middleware;
* Added select/print (initialisation) state update via study submission; test alter 10 on SQL Server;

#### 13th to 17th August
 
* Print state testing
* Study download/reload testing
* Disable DB upload for pre alter 10 studies, test getSelectState, refactor SelectState service to make common;
* Add selected shapes (risk analysis definition shapefiles);
 
#### 20th to 24th August

* Experimented with mouse click through. Unable to get to work without adverse performance implications:
  * TopoJSON grid layer blocks mouse clicks unless it is the in view pane;
  * Using the mouse position to find the nearest TopoJSON grid layer does work, but you only get one mouse event per layer boundary cross. 
    Would need to modify the map shape pane to transmit more events, this has performance implications;
* Created example UK 2011 census numerator/denominator tables to shut front end up!
* Fix for spurious WARNING: Unable to verify study area selection: upload no longer supported for pre-alter 10 studies;
* Improvements to file load messages;
* Add showShapes button to viewer and diseasemap;
* Make diseasemap just a map;
* Start risk analysis support in viewer;
* Misc logging and IE fixes. IE works, but only with a browser window - suspect proj4 library;
* Added study type and new polygon name for viewer and mapper, de-disease mapped; start of choropleth map risk analysis awareness

#### 27th to 31st August

* Added study type check between rif40_studies.study_type and rif40_studies.study_selection JSON study type;
* Fix null breaks problems in Choropleth maps (0 was being interpreted as undefined!);
* Fix synchronisation issues with legend and D3 charts;
* Added support for default map background by geography for maps;
* Leak test: PASSED!
* Regression test to SQL Server;
* Created and merged pull request #55 (https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/pull/55) for RA front end and DB;
* Merged pull request #60 (https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/pull/60) for RA middleware;
* Suppressed: WARNING: studyType mismatch for map: viewermap; study ID: 448 rifc-util-alert.js:220:7
  ```
  C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF40\utils\controllers\rifc-util-mapping.js (1 hit)
  Line 1594: 									$scope.showWarning("studyType mismatch for map: " + mapID + 
  ```
  Need to fix rif40_studies.study_type to be as per study_selection JSON;
* Added notes on "alter_10.sql (post 3rd August 2018 changes for risk analysis) not run" message;   
* Created and outstanding issues in priority order:
  * On-screen toggle between disease mapping and risk analysis doesn't work properly #52;
  * Front end mapping synchronisation fails sometimes when you change the study #57;
  * Error handling ICD10 chapters and ranges as investigation conditions #58;
  * Add support for risk analysis in map export #61;
  * Cannot view error messages that disappear #59;
  * studyType mismatch for map: viewermap; study ID: ... #62;
  * ICD9 support #64;
  * Sort (ex disease) map info boxes - merge into 1 box, add support for homogeneity, exposure covariates in risk analysis #65;
  * GeoJSON hover support with shapefile shapes #66. This will allow the hover mode to display data from the area geoJSON below the shapefile shapes.
    I experimented with mouse click through but was unable to get to work without adverse performance implications:
    TopoJSON grid layer blocks mouse clicks unless it is the in view pane;
    Using the mouse position to find the nearest TopoJSON grid layer does work, but you only get one mouse event per layer boundary cross. We would need to modify the map shape pane to transmit more events (this may not be possible), this has performance implications as you will need to work out which GeoJSON shape is nearest the click. With tens of thousands of shapes this will be slow, requiring more complexity - e..g use 1km grids to reduce the search.
    * Fix IE logging **AGAIN**. IE works, but only with a browser window - suspect proj4 library;
    * Re-factor of Leaflet mapping code to ensure common core between mapping and selection screens; creation of new common Services to 
      reduce complexity (e.g. LayerOrderingService);
  * Print state support #67;
 
#### 3rd September to 7th September

* Front end bug fixes:
  * Basemap change on study ID now works in maps;
  * Export map now work (RA changes to REST calls);
  * Multi geography fix in study load;
  * Partial fix to not zooming to study extent in changing study ID;
  * Diagnosis and fix of: On-screen toggle between disease mapping and risk analysis doesn't work properly: #52
  * Add: cannot view error messages that disappear #59;
  * Revised RIF 4.0 User Manual
 

#### 10th September to 14th September

* Fix for: WARNING: Could not upload and check study selection: study comparison resolution not setup correctly
* Fix for: Warning: Unable to verify comparison area selection errors
* Applied temporary fix by disabling the ChloroService.doRenderSwatch(): 
  (https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/commit/d26ab96f001145f9d6d4e6a987acc94d62a3cbee)
* Create and apply pull #72 for Error handling ICD10 chapters and ranges as investigation conditions #58;
* Investigate and fix risk analysis export NPE crash: caused by smoothed_smr, posterior_probability fields with no data;
* RIF demo with Prof.  Elliott; meeting decided to plan for RIF testing using Using the Heathrow and/or Incinerators studies to test the RIF 
  [issue #74](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/74);
* Merge pull #73: Investigate and fix risk analysis export NPE crash;
* Update TODOs;
* Investigate UTF8/16 support (e.g. Slättåkra-Kvibille should not be mangled as at present). This affects SQL Server only. 
  Issue [#79](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/79). Effects database, hopefully not the various 
  Java/Javascript drivers.   

#### 17th to 21st September

* Risk analysis selection at the highest levels: MSOA, LSOA. 
  * Improve asynchronous code to better manage browser load;
  * Added spinners and shape processing feedback;
  * Fix shapefile table CSS problems;
  * Confirmed "Could not match polygons from database with selected polygons list" error effects main branch as well;
  * Fix for "Could not match polygons from database with selected polygons list"
    * Added getTileMakerAttributes rest call to replace getTileMakerTilesAttributes; some of the smaller areas where optimised out in the base tile.
  * RIF timescales meeting;
  * Restructure of map selection code to ensure it works in the correct order using promised;
  * Selection list improvements using object to remove need to scan list, implement bounding box exclusion check for shapes, logging improvements;
  * Tested to COA level: selected 54,998/227,750 COAs using COMARE nuclear installation shapefile in 11,289.3s on Edge. Probably need to remove 
    async.eachOfSeries(CentroidList,  ...) and replace with conventional for loop;
  * Edge crashes restoring COA COMARE test study but can create intersects, Firefox OK. Attempts to fix by reorder the code failed; it crashes soon 
    after loading COA GeoJSON tiles;
	```
	Faulting application name: MicrosoftEdgeCP.exe, version: 11.0.17134.48, time stamp: 0x5ae3f17b
	Faulting module name: EdgeContent.dll, version: 11.0.17134.285, time stamp: 0x9a1d5114
	Exception code: 0xc0000409
	Fault offset: 0x00000000000af52a
	Faulting process id: 0x74fc
	Faulting application start time: 0x01d452523afd7278
	Faulting application path: C:\Windows\SystemApps\Microsoft.MicrosoftEdge_8wekyb3d8bbwe\MicrosoftEdgeCP.exe
	Faulting module path: C:\WINDOWS\SYSTEM32\EdgeContent.dll
	Report Id: dbb8cbb2-4628-4664-b135-b5a4f803f5b7
	Faulting package full name: Microsoft.MicrosoftEdge_42.17134.1.0_neutral__8wekyb3d8bbwe
	Faulting package-relative application ID: ContentProcess
	```
  * Issues:
    * More browser testing at COA level: Firefox OK, but GC is annoying; Edge: very nearly. Chrome, Opera cannot handle COA (2GB/3GB memory limits). 
	  IE not tested;
	* Edge fault believed to be caused by debugger. Edge is slower but more reliable than Firefox;
    * "exit" button takes forever if centroids displayed. Needs spinner if possible;
	* Load saved study then clear selection fails to initialise areas correctly. Wrong areas being displayed;
    * "exit" should remove shapes;	
 * Performance to load 28 COMARE nuclear installation sites at 5Km and 25Km:

   | Geo Level   | Load from file | Intersect | Selected areas      |
   |-------------|----------------|-----------|---------------------| 
   | MSOA (Ward) | 6.8s           | 15.5s     | 2,040/8,480         |  
   | LSOA        | 21s            | 43.4s     | 10,124/41,729       |  
   | COA         | 177.2s         | 203.5s    | 54,998/227,750      |  
   
#### 24th to 28th September

* RIF demonstrations to CDC, Ulf Stromberg, meeting;
* Refactor to create CommonMappingStateService (for map data) and DrawSelectionService (for processing shapes);
* Add areaId filter to TopoJSONGridLayer;
* Testing;
 
#### 1st to 5th October

* Improved study type initialisation;
* Testing;
* Fix for use of zoomlevel 0 tiles; wait until map centered and zoomed;
* Added band support to middleware;
* Added new study extract trace. Risk analysis run bug is caused by comparison area overwriting the study area;
* Added set methods to state services; used angular copy to remove study area overwrite bug which was caused by scope issues; 
* Installed databases on new server (wsrif);
 
#### 8th to 12th October

* Merge: remove Code Duplication In Scripts #100; fix disease mapping issues with new band_id code; passes on both ports
* Fix Postgres risk analysis extract code;
* Fix R script trace
* Fix SQL Server risk analysis extract code. 
* Band ID issue removed; restore study shape issue removed
* Fix Risk Analysis map display, GANTT chart, export preview table data
* Pull #101 (Risk analysis selection at high resolution):
  This also include a first restructure of the areamap code. 
  This is a partial implementation of issue #78 (Risk Analysis selection at high resolution (e.g. MSOA) does not perform 
  acceptably); the conversion of geoJSON tiles to PNG tiles by the middleware has not yet been implemented, At high resolutions 
  (>5000 areas) selection by clicking on the area is not permitted and restore of saved study selections only show the selected 
  area (the PNG tile map will go below).
  The RIF will now work at COA level with fully 64 bit browsers (Firefox and Edge). Browsers limited to 32 bit address space 
  (all other Windows browser) will crash when memory exceeds 2GB.

  Testing showed up issues with Risk Analysis bands so these were fixed and full support for risk analysis added at the time.

  **You must re-run alter_10.sql**
  ```
  cd rifDatabase/Postgres/psql_scripts
  psql -d sahsuland -U rif40 -f alter_scripts/v4_0_alter_10.sql 
  ```
  Also included:

  * R script and SQL traces now work correctly
  * Risk analysis extract code, map display and export preview table data now work

Further changes are needed to support risk analysis:

* Risk analysis version needs to return 1/row band not area as at present but the front end needs a re -write first. Suggest:

    - Data viewer table displays banded results (not by area_id as at present);
    - Dual maps bottom chart displays homogeneity data;
    - Export needs to support risk analysis (it will crash at present);

* Add area name to all exports
* Convert map display SQL fetch to dynamic method 4 (i.e. works out the field names and datatypes automatically)
* Use a different list of fields for risk analysis and disease mapping and make easy to configure

#### 15th to 19th October

* Install SQL Server and Postgres databases on WSRIF; install and configure RIF tomcat application; 
* Install UK and US data; tune, test;
* Found issue with postcode points - not being added to the map;
* Fixed postcode lookup issues in *rif_pg/mssql_process_centroids.sql*;
* SQL Server reports the percentage of population weighted centroids incorrectly;
* Added support for multiple intersection tracing in shapes and selected polygons;
* Issues requiring fixing:
  * Add selector arrow to ICD selector;
  * Fix shape layer order (not ordered as createAreaNameList() not working);
  * Fix clear selection (not clearing selected shapes);
  * CSV area load button broken;
  
#### 22nd to 26th October

* Upgrade demo laptop to current patch level;
* Demo to Swedish RIF team, discussion of the way ahead;
* Further work on multiple intersection tracing in shapes and selected polygons;  
* Load UK EWS2011 population data
* Test pull #106: Adds ICD 9 support for branch issue#64_icd9_support. ICD9 functionality is OK. Have added CSS fix to restore 
  select arrow functionality. Risk analysis changes cause a tomcat server crash for both disease mapping and risk analysis;
  
#### 29th October to 2nd November

* Further work on multiple intersection tracing in shapes and selected polygons; 
* Test Postgres 10 and SQL Server partitioning on UK EWS2011 population data;
* Trace and fix pull #106 fault: Adds ICD 9 support for branch issue#64_icd9_support. Path to R scripts not being escaped correctly:
  Crashes:
  ```
  09:45:03.412 [http-nio-8080-exec-10] INFO  org.sahsu.rif.generic.util.RIFLogger : [org.sahsu.rif.services.datastorage.common.StatisticsProcessing]:
  Source: 'C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\JdbcHandler.R
  ```

  Does not crash (other branches):
  ```
  11:02:04.266 [http-nio-8080-exec-9] INFO  org.sahsu.rif.generic.util.RIFLogger : [org.sahsu.rif.services.datastorage.common.SmoothResultsSubmissionStep]:
  Source(\): 'C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\JdbcHandler.R
  ```
* Fixed:
  * Add selector arrow to ICD selector;
  * Fix shape layer order (not ordered as createAreaNameList() not working);
  * Fix clear selection (not clearing selected shapes);
* Unable to re-create
  * Fix shape layer order (not ordered as createAreaNameList() not working);  
* Add intersection code to middleware and database (alter_11.sql)
* Test Postgres 10 and SQL Server partitioning on UK EWS2011 population data

#### 5th to 11th November

* Test and merge #106: Adds ICD 9 support; #107: Improve taxonomy service startup;
* Complete intersection middleware insert to database; 
* RIF Meeting;
* Fix basemap selection: allow to change, no network support, saved state;
* Add support for exposure values to alter scripts, middleware and database;
* Test and merge #109: Improve file handling;

#### 12th to 16th November

* Complete alter 11:
  1. Support for Postgres 10 partitioning;
  2. Intersection counting (study areas only);	
  3. Exposure value support;
  4. Add intersection counting and exposure value support to extracts;
  5. View rif40_exposure_values;
* Create new view *rif40.rif40_exposure_values* has been created to support exposure covariates;
* The extract table is untouched for disease mapping and the extract has been fixed for risk analysis to support 
  exposure covariates and intersections;
* Fixed ZIP extract to support risk analysis correctly and new alter 11 columns:
  - intersect_count: Number of intersects with shapes
  - distance_from_nearest_source: Distance from nearest source (Km)
  - nearest_rifshapepolyid: Nearest rifshapepolyid (shape reference)
  - exposure_value: Exposure value (when bands selected by exposure values);
* Create pull #112: Risk analysis fixes required enhancements: add support for intersection between risk analysis 
  shapes and for exposure covariates;
* Patch wsrif, install and document remote Oracle database connectivity; 
* Documentation;
* Test R Risk analysis scripts;

#### 18th to 23rd November

* Tile caching: GeoJSON support;
* RIF meetings; sort move of *wsrif* to private network, COMARE extract for RIF. Test Heathrow noise model shapefile load at COA
* Tile caching: PNG support;
* Tile caching. Test pull #114;
* Add separate abstracted  logging to #114, tile generator thread;

#### 26th to 30th November

* Tile caching: auto generation on services startup using a tile generator thread;
* RIF extract planning;
* Removed duff lines/points from polygons to remove GeoTools SLD errors; also applied to extract
* Prepare paperwork for COMARE and Heathrow extracts
* Test pull #116, create pull #117 for tile generation; document tile generation

#### 3rd to 7th December

* Scripts for COMARE load, RIF meeting, data load meetings, issue with rif_user role
* Heathrow extract specificiation; derive COA list and noise controur from day and night noise rasters
* Issues with new users:
  * SQL Server needs to create required views automatically;
  * Scripts need to insert the *TEST* project into *t_rif40_user_projects*. The username must be in lowercase: Postgres errors:
    ```
	ERROR:  record "old" is not assigned yet
	DETAIL:  The tuple structure of a not-yet-assigned record is indeterminate.
	CONTEXT:  PL/pgSQL function rif40_trg_pkg.trigger_fct_t_rif40_user_projects_checks() line 43 at IF
	```
	Simple fix: ```ALTER TABLE t_rif40_user_projects DISABLE TRIGGER t_rif40_user_projects_checks;```.
	Trigger code needs to specify INSERT in -20522 check;
  * Lack of the *rif_manager* role (i.e. *rif_user* only) causes on SQL Server:
    ```
	09:47:44.143 [pool-7-thread-1] INFO  org.sahsu.rif.generic.util.CommonLogger : [org.sahsu.rif.services.datastorage.common.StudySubmissionStep]:
	BaseSQLManager logSQLQuery >>>
	QUERY NAME: addGeneralInformationToStudy
	PARAMETERS(16): 
		 1: 'SAHSULAND'
		 2: 'TEST'
		 3: 'TEST 1002'
		 4: '1'
		 5: 'SAHSU_GRD_LEVEL1'
		 6: 'SAHSU_GRD_LEVEL4'
		 7: 'POP_SAHSULAND_POP'
		 8: '1995'
		 9: '1996'
		 10: '21'
		 11: '0'
		 12: '0'
		 13: '0'
		 14: '0'
		 15: 'HET'
		 16: 'null'
	SQL QUERY TEXT: 
	INSERT INTO rif40.rif40_studies( 
	   geography,
	   project,
	   study_name,
	   study_type,
	   comparison_geolevel_name,
	   study_geolevel_name,
	   denom_tab,
	   year_start,
	   year_stop,
	   max_age_group,
	   min_age_group,
	   suppression_value,
	   extract_permitted,
	   transfer_permitted,
	   stats_method,
	   risk_analysis_exposure_field) 
	VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);


	<<< End BaseSQLManager logSQLQuery

	09:47:44.477 [pool-7-thread-1] WARN  org.sahsu.rif.generic.util.CommonLogger : [org.sahsu.rif.generic.datastorage.SQLQueryUtility]:
	SQL Error/Warning >>>
	Message:           The statement has been terminated.
	SQLState:          S0000
	Vendor error code: 3621	
	```
	Running the SQL manually gives:
	```
	1> INSERT INTO rif40.rif40_studies(
	2>    geography,
	3>    project,
	4>    study_name,
	5>    study_type,
	6>    comparison_geolevel_name,
	7>    study_geolevel_name,
	8>    denom_tab,
	9>    year_start,
	10>    year_stop,
	11>    max_age_group,
	12>    min_age_group,
	13>    suppression_value,
	14>    extract_permitted,
	15>    transfer_permitted,
	16>    stats_method,
	17>    risk_analysis_exposure_field)
	18> VALUES ('SAHSULAND',
	19>  'TEST',
	20>  'TEST 1002',
	21>  1,
	22>  'SAHSU_GRD_LEVEL1',
	23>  'SAHSU_GRD_LEVEL4',
	24>  'POP_SAHSULAND_POP',
	25>  1995,
	26>  1996,
	27>  21,
	28>  0,
	29>  0,
	30>  0,
	31>  0,
	32>  'HET',
	33>  'null');
	34> go

	(0 rows affected)

	(1 rows affected)
	Msg 51131, Level 16, State 1, Server DESKTOP-4P2SA80, Procedure tr_rif40_studies, Line 47
	View name: [rif40].[rif40_studies], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: aina	
	```
    and Postgres:  
    ```
	15:57:46.702 [pool-11-thread-1] ERROR org.sahsu.rif.generic.util.CommonLogger : [org.sahsu.rif.services.datastorage.common.StudySubmissionStep]:
	RIFSQLException: Warnings/Messages >>>
	No warnings/messages found.
	<<<
	BaseSQLManager logSQLQuery >>>
	QUERY NAME: addGeneralInformationToStudy
	PARAMETERS(16): 
		 1: 'SAHSULAND'
		 2: 'TEST'
		 3: 'TEST 1002'
		 4: '1'
		 5: 'SAHSU_GRD_LEVEL1'
		 6: 'SAHSU_GRD_LEVEL4'
		 7: 'POP_SAHSULAND_POP'
		 8: '1995'
		 9: '1996'
		 10: '21'
		 11: '0'
		 12: '0'
		 13: '0'
		 14: '0'
		 15: 'HET'
		 16: 'null'
	SQL QUERY TEXT: 
	INSERT INTO rif40.rif40_studies( 
	   geography,
	   project,
	   study_name,
	   study_type,
	   comparison_geolevel_name,
	   study_geolevel_name,
	   denom_tab,
	   year_start,
	   year_stop,
	   max_age_group,
	   min_age_group,
	   suppression_value,
	   extract_permitted,
	   transfer_permitted,
	   stats_method,
	   risk_analysis_exposure_field) 
	VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);


	<<< End BaseSQLManager logSQLQuery

	exception: ERROR: permission denied for relation rif40_study_shares
	getMessage:          PSQLException: ERROR: permission denied for relation rif40_study_shares
	getRootCauseMessage: PSQLException: ERROR: permission denied for relation rif40_study_shares
	getThrowableCount:   1
	getRootCauseStackTrace >>>
	org.postgresql.util.PSQLException: ERROR: permission denied for relation rif40_study_shares
		at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2198)
		at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:1927)
		at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:255)
		at org.postgresql.jdbc2.AbstractJdbc2Statement.execute(AbstractJdbc2Statement.java:561)
		at org.postgresql.jdbc2.AbstractJdbc2Statement.executeWithFlags(AbstractJdbc2Statement.java:419)
		at org.postgresql.jdbc2.AbstractJdbc2Statement.executeUpdate(AbstractJdbc2Statement.java:365)
		at org.sahsu.rif.services.datastorage.common.StudySubmissionStep.addGeneralInformationToStudy(StudySubmissionStep.java:502)
    ```
    Note that Postgres and SQL Server are different
* Heathrow covariates, fixes for rif_user role faults;
* Fix for *extract_permitted*, *printstate* and *selectstate* RIF_USER role support in T_RIF40_STUDIES table
* Fix for RIF_USER role so that RIF_MANAGER role is not required (faulty trigger logic in 10 views);
* Table t_rif40_user_projects - check the username is a valid user;
* Table rif40_study_shares: for insert, check that new values are valid:
  - Check all fields are not null: study_id, grantor,grantee_username
  - Check that the study_id exists
  - Grantee username is valid user
  - Grantor username is valid user
  - Grantee username has RIF_USER or RIF_MANAGER
  - If the Grantor is NOT owner of the study they are a RIF_MANAGER	
  - If the grantor != grantee_username then the grantor is a RIF_MANAGER;

#### 10th to 14th December
 
* Restructure tile generator;
* Load COMARE data, found fault in SQL Server denominator data, tile generator shutdown handler. Issues from load:
  * Missing GRANT from covariate scripts;
  * Postgres Update format for large/large table joins should use a Common Table Expression;
  * SQL Server population database - msoa2011 does not link to geography;
  * Server server hidden messages: "The statement has been terminated vensor error code 3621;
  * rif40_execute_insert_statement.sql no error with code: 56699. You get this is if study insert inserts no rows (probably a +100 error);
  * *pg_partitioned_table* issue on Postgres 9.x:
    ```
	psql:rif40-pg_sahsu_heathrow.sql:252: ERROR:  relation "pg_partitioned_table" does not exist
    LINE 10:      FROM pg_partitioned_table p, pg_attribute b, pg_class c...
	```
* Fix issue #119 for spurious ranges that break t_rif40_inv_conditions CONSTRAINT max_condition_ck; pull #122;

#### 17th to 20th December

* PE/BD meeting, Heathrow data loading;

# In progress work at contract end (20/12/2018)

## Risk analysis issues (branch: risk-analysis-fixes-2)

1. Multiple Health outcomes produces errors
2. Health outcome may require a geography change - if it is wrong you get:
   ```
   ERROR: No health data for theme "SAHSU land cancer incidence example data", geography "EWS2011". 
   ERROR: Could not retrieve your project information from the database: unable to get numerator/denominator pair 
   ```
3. Using add by postcode produces errors on its own, but works;
4. Errors if nonsensical exposure bands are selected;
5. Clear does not work after restore from file;
6. Adding a point produces errors after restore from file;
7. Add disableMouseClicksAt from frontEndParameters.json5 to replace hard coded 5000 in Tile generation;
8. Load list from text file loads OK but does not display correctly;
9. Need a file type filter when loading JSON files;
10. Zip shapefile load to be able to cope with projections other than 4326 (e.g. local grid)

## Heathrow Data Load Scripts

*rif40-pg_sahsu_heathrow.sql*, *rif40-mssql_sahsu_heathrow.sql* partially complete for one outcome: **stroke**. Need to test in RIF with mad up data to check ICD selection works. 
* Will require an "All ICD 10 codes (A% to Z%) filter added to WHO ICD 10 taxonomy file;
* Needs branch *risk-analysis-fixes-2* fix 1 (Multiple Health outcomes produces errors) to be able to test.

## RIF User role faults

* The RIF role *rif_user* cannot run studies. The branch *rif_user_role_fixes* repairs this. This is not fully tested. This also repairs a Postgres 9/10 
  incompatibility in *rif40_trg_pkg.trigger_fct_rif40_tables_checks()*.

## Use PNG tiles instead of GeoJSON

* This first patch add the ability to generate PNG tiles and to use them when you return to a previously selected study or comparison area.
  The branch *png_tiles_from_geojson* implements this and is complete apart from supporting tile generation below the maximum database 
  geolevel. (GeoJSON interpolates automatically; PNG tiles must be generated). 

# New contract extension Jan/Feb 2019

## Tasks:

19.	Support the development of a roadmap for RIF developments in 2019 in a 2019 development roadmap document – Oct-Jan 2019. 
    Estimated time: 1 day [DONE];
20.	Support the import of Swedish geographies and data into the RIF 4.0 for Ulf Stromberg and colleagues based on the agreed 
    timeframe – Jan 2019. Estimated time: 2-3 days [DONE: geographies: took 3 days];
21.	Plan and commence implantation of functionality so that all the covariates available can easily be added to the data extract and 
    hence exported from the RIF 4.0. The end user should be able to specify the content and the extract must be able to contain up to 
	10 continuous or discrete variables in compatible format for use in STATA/R or other appropriate software – Jan 2019. Estimated 
	time: 5-10 days;
22.	Support SAHSU’s database team as required to enable access of the RIF 4.0 to a pseudonymised version of HES and ultimately, other 
    health databases. – Jan 2019. Estimated time: 2-3 days;
23.	Set up the RIF 4.0 for linkage to SAHSU’s health database, for example through the use of template dummy data. – Jan 2019. 
    Estimated time: 2-3 days;
24.	Test and document end-users data access permissions and SQL statement auditing with middleware log examples – Jan 2019. Estimated 
    time: 2 days;
25.	Plan and commence implementation of a front end "report", based on the visible displays in the RIF 3.2, for point 
    source exposures  (minimum displayed dataset to include observed counts, expected counts, relative risk, trend 
    test for each site, and adjusted by region with heterogeneity testing and meta-analysis function. The content and layout should be discussed with FP – Jan 2019. 
	Estimated time: 5-10 days [DONE];
26.	Plan adding support for multiple covariates in the disease mapping and risk analysis functionalities of the RIF 4.0 – Jan 2019. 
    Estimated time: 1 day [DONE];
27.	Using a provided specification plan adding support for region-specific details around point sources – Jan 2019. Estimated time: 
    1 day;
28.	Ensure that all IG training is completed and up to date for potential future access to SAHSU’s private network – Jan 2019. Estimated 
    time: 1-2 days [DONE].

Total time estimates for additional items 16 to 28: 18-30 days; time available: 16 days.

## Weekly summary

### 21st to 25th January 2019

* RIF meeting;
* 2019 plans;
* Swedish geography build;
* IG training.

### 28th January to 1st February

* Plan [issue #124 Multiple covariates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/124) (first part for 
  March 2019) and [issue #127 risk analysis D3 maps](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/127). 
* RIF meeting;
* Create branch [risk_analysis_d3_graphs](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/tree/risk_analysis_d3_graphs);
* getHomoGeneity REST call

### 4th to 8th February

* Covariant Loss report SQL generator
* Covariant Loss report REST call, SQL Server fixes
* Covariate loss report and homogeneity front end, experimental D3 chart
* RIF meeting

### 11th to 15th February

* Multiple covariate database changes,. agree specification for study submission XML and enhanced REST calls;
* RIF meeting, multiple covariate mockups;
* Risk graph REST Call.

### 18th to 22nd February

* Risk graph D3/Angular integration;
* Fixed Angular $watch call to remove continuous rebuilds of SVGs (and therefore potential leaks);
* Scaling and size detection;

### 25th February to 1st March

* RIF meeting, mapping risk graphs integration;
* Mapping risk graph directive improvements, scalability fixes;
* Created pull #135 for issue #127 Risk Analysis specific D3 graphs. Have also fixed export info modal. D3 graphs todo 
  is issue #134:

  1. Convert to info and viewer risk graphs to using common version with a single data source. Rename rifd-util-d3riskGraph3.js to rifd-util-d3riskGraph.js and remove rifd-util-d3riskGraph2.js;
  2. Fix info scaler to use d3.select("#???????").node().getBoundingClientRect().height/width. This removes a known bug. Also test with window resizing;
  3. Remove non d3 code from rifs-util-d3charts.js (functionality now in rifd-util-d3riskGraph3.js), rename to rifs-util-d3riskgraph.js; 
  4. Convert rr-zoom, dist-histo and pyramid directive so the d3 code is in a utils service and now use same the methods as rifd-util-d3riskGraph.js. Resizing should now work!
  5. Remove rrZoomReset anti memory leak functionality. It should no longer be needed;
  6. Multiple redraws in the mapping panes should be remove when the fetch code is all converted to use promises;
  7. Add rr-zoom, dist-histo and pyramid to the info modal;
  8. Add "NONE" to second gender selector in info risk graph. The work around is to set both to the same;
* Merged pull #118 and #135. Updated multiple covariate branch: issue/#124_Add_support_for_multiple_covariates;
* Multiple covariates front end;

* Plans for March and April:
  
  Outstanding tasks:
  
  20. Support the import of Swedish geographies and data into the RIF 4.0 for Ulf Stromberg and colleagues based on 
      the agreed timeframe – Jan 2019. Estimated time: 2-3 days [TODO: Data];
  21. Plan and commence implantation of functionality so that all the covariates available can easily be added to the data extract and 
      hence exported from the RIF 4.0. The end user should be able to specify the content and the extract must be able to contain up to 
	  10 continuous or discrete variables in compatible format for use in STATA/R or other appropriate software – Jan 2019. Estimated 
	  time: 5-10 days [Priority; by 13th March];
  22. Support SAHSU’s database team as required to enable access of the RIF 4.0 to a pseudonymised version of HES and ultimately, other 
      health databases. – Jan 2019. Estimated time: 2-3 days [Priority; by 20th March];
  23. Set up the RIF 4.0 for linkage to SAHSU’s health database, for example through the use of template dummy data. – Jan 2019. 
      Estimated time: 2-3 days [Priority; by 20th March];
  24. Test and document end-users data access permissions and SQL statement auditing with middleware log examples – Jan 2019. Estimated 
      time: 2 days [March/April];
  27. Using a provided specification plan adding support for region-specific details around point sources – Jan 2019. Estimated time: 
      1 day [March/April];
	  This is [Issue #129 Pooled or individual analysis for multiple risk analysis points/shapes (e.g COMARE postcodes)](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/129).
	  Currently the RIF analyses there in N bands with all the sites as one. It is proposed to extend the RIF to support:
	  * Individual site analysis;
	  * Pooled analysis (1 or more groups of sites). Groups would be defined from a categorisation variable in the shapefile. Would require 
		changes to:
		* Shapefile load screen and controller;
		* JSON study definition format;  
		* Study extract and result tables;
		* R risk analysis code.
	  
* For the work queue when the above are complete: 
  * Postcode point/area data integration;
  * Issue #89 Local caching of base maps 
  * Issue #134 D3 graphs todo; 
  * Issue #130 Risk analysis fault/issues (branch: risk-analysis-fixes-2); 
  * Issue #66 GeoJSON mouse over support with shapefile shapes
  * Issue #75 IE 11 only works with a browser console only;
  * Issue #66 GeoJSON mouse over support with shapefile shapes;
  
### 4th to 8th March

TODO this week:
* Swedish Data;
   
 

