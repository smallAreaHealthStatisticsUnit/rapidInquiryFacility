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
    ordered hierarchy CSV files. Postgres and SQL Server hierarchy and lookup tables now exactly the same. Regression tests OK.

#### Current TODO list (January 2017):

* Convert v4_0_create_sahsuland.sql to use tileMaker sahsuland, and remaining test scripts
* Alter 9:

  1. Replace old geosptial build code with new data loader. Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; use rif40_geolevels lookup_table/tile_table
  2. Make RIF40_TABLES.THEME nullable for denominators
  
* MS Sahsuland projection problem; must be fully contained within the projection or area calculations and intersections will fail. 
  This needs to be detected
* Check (warn/error) if geometry not within projection (sahsuland was using Nevada north 1927, now using OSGB at present)
* Tilemaker drop scripts
* Fix zoomlevel miss-set from config file (defaults are wrong)
* Add support for rif relative relative path
* QGIS integration for SQL Server: ..\rifNodeServices\sql\sqlserver\create_mssql_geometry_columns.sql
* TileViewer web screen by DB/geography; DB web service

### Database Bugs

* INSERT INTO rif40_table_outcomes wrong OUTCOME_GROUP_NAME used in v4_0_postgres_sahsuland_imports.sql, suspect ICD hard coded. [Not a bug]
* Fix:
  * RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMNN_EXISTS to COLUMN_EXISTS
  * T_RIF40_CONTEXTUAL_STATS/RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION to TOTAL_COMPARISON_POPULATION
* Resolve: RIF40_PARAMETERS.DESCRIPTION (SQL Server) or PARAM_DESCRIPTION (Postgres)

* Standard test configurations:
  * SAHSULAND: relocated to Utah: reprojected to 1983 North American Projection (EPSG:4269)
	* Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; use rif40_geolevels lookup_table/tile_table
  * DOGGERLAND: relocated to 54°20'0"N 5°42'59"E on the Dogger Bank. This is the site of wreck of SMS Blucher. 
    https://www.google.co.uk/maps/place/54%C2%B020'00.0%22N+5%C2%B042'59.0%22E/@54.3332107,0.9702213,5.92z/data=!4m5!3m4!1s0x0:0x0!8m2!3d54.3333333!4d5.7163889 
  * USA: USA to county level
* Bugs, general RIF database Todo
* SQL Server run study port
* SQL server fault in rfi40_geographies/geometry insert triggers

####  TODO list:

* Relative install path in tilemaker install script generator (i.e. ../../GeospatialData/tileMaker/ for sahsuland)
* JSZip 3.0 upgrade required (forced to 2.6.0) for present
* SQL load script generator: still todo, all can wait:
  * Add search path to user schema, check user schema exists, to Postgres version
  * Confirm Postgres and SQL Server geolevel intersections are the same;
* Get methods: 
  * ZIP results;
  * Run front end and batch from XML config file.
  * Add CSV files meta to XML config;
  * Drive database script generator from XML config file (not internal data structures);
  * Missing comments on other columns from shapefile via extended attributes XML file;
  * Dump SQL to XML/JSON files (Postgres and SQL Server) so Kevin does not need to generate it;
  * Add trigger verification code from Postgres;
  * Fix in Node:
    - Triangles (to keep QGIS happy)
    - Self-intersection at or near point -76.329400888614401 39.31505881204005
    - Too few points in geometry component at or near point -91.774770828512843 46.946012696542709
  * Check Turf JS centroid code (figures are wrong);
  * Compare Turf/PostGIS/SQL Server area and centroid caculations;

SQL Server porting started in August(nearly complete 27/10/2016); so far I was able to use Turf. The Node.js backend needs to be 
able to run the scripts so that the fixed and validated geometry data becomes available.

Note: no bounding box (bbox) in tiles.

#### Parked TODO (todon't) list (as required):

* Read DBF header so shapefile reader knows number of expected records; add to status update
* Timeout recovery (switches to batch mode).
* Favicon support: https://github.com/expressjs/serve-favicon
* Database connection; clean, check OK and ST_Union(); area support [and checks]; PK support. Turf probably removes requirement 
  for any DB port, subject to acceptable performance
* Handle when areaID == name; allow NULL name
* Detect area mismatch between shapefiles	
* Prevent submit whilst running shpConvert method
* Prevent tab change during map draw and aoccordion setup or JQuery UI and Leaflet do bad things unless tkey have focus
* Fix customFileUpload styling so it uses the correct JQuery UI class style; the .css() function won't work on form file upload buttons
* Add support for XML config file so shpConvert can do all processing without further input;
* Restrict geolevels to a minimum 3, or more if the total topojson_size < ~20-30M (possibly browser dependent). 

* Add areaKm2 (using bounding box) as jsonfile property. Needs turf.
* Calucation of quantization and the max zoomlevel using area. Enforcement in browser. 
* Hover support for area name, area_km2 and shapefile supplied data at highest resolution

* SQL server RHR force to support mixed LH and RH in multipolygons
  [c.%1.ReorientObject() is used as c.%1.STUnion(%1.STStartPoint()) does NOT work] where %1 is the geometry column 
  [Needs Turf support]
	
* Display of zoomlevel contextual information: total topojson size, suppressed or not. 
* Status in write JSON file Re-test COA2011: json memory and timeout issues are solved
* Add simplify to zoomlevel 11, spherical simplify limit (in Steraradians) [Probably no, only use quantization at max zoomlevel] .
* Duplicate file names in zip files. Flattening of directory structure causes duplicates which are not detected

* Add convertedTopojson array meta data to topojson config
* Add startup parameterisation (db, if, port etc) using cjson
* Test json file
* Change audit trail: Unions, linestring to polygon conversions, ST_invalid => ST_MakeValid geomtery validators; 
* Add tests:
	i. Unsupported projection files (modify proj data slightly...)
	ii. Wrong shapefile (by bounds) in set
	iii. No shapefile with only 1 area if > 1 shapefile
	iv. Total area mismatch between shapefiles
* Add GID, shapefile fields to lookup tables;
* Add areaid as well as <geolevel_name> in lookup tables;
  
##	General RIF database Todo (parked):
  
### Database Bugs

* AreaName duplicates to be allowed; key enforcementment to be in the heirarchy table; this allows 
  for duplicate county names within a state
* Change CREATE study to run in own schema; create procedure to transfer study/map tables to correct schema 
  and grant back permissions [i.e. remove security issue with current code]
* Add t_rif40_study_areas trigger check (once per INSERT/UPDATE) for correct use of band_id in rif40_study_shares. 
  Alternatively check in rif40_run_study
* Rename offset in age_sex_groups (reserved keyword)
  
### Early December

2. New study state "S" - Smoothed; new method: setStudyState(study_id, state) {…}
2. New study status table: t_rif40_study_status(username, study_id, study_state, creation_date, ith_update, message); ith_update is auto increment
   and updateable view rif40_study_status of the users own studies
3. Separate test/build in makefile; remove Node dependency
  
### Park

1. Documentation [in progress];
2. JSON injection protection [will be a regexp];
3. Web front end connection timeout (connect/post connect) limits - as previous;
4. Integration to build, severeal options:
   i.   Replace topojson_convert.js;
   ii.  New node program to replace existing functionality [see 4 early December above];
   iii. Awaiting data loader.

## 2016/7 Plans: 

1. Build and integrate Node.js middleware server:

  * GeoJSON to TopoJSON conversion; by converting existing Node.js program to using HTTP POST methods to integrate  with the middleware
  * Test and demonstrate secure logons using session_ids, time stamps and elliptic curve cryptography (public/private keys). Assist with
    integration into middleware and JavaScript frontends.
  * Assist with Node.js service integration into middleware, data loader and JavaScript frontends.
  * Shapefile simplification
  * Shapefile conversion to WKT (Well known text) format

2. Assist as required in the coding, integration and testing of: 

  * RIF batch: Java code calling a) database procedural code for study data extraction and b) R code [created by the Statistical team] 
    to calculate statistical results from the study data extraction. Includes work to: 
    * Extend the study extraction code to use separate age sex group field named; 
    * Add code to validate RIF setup;
    * Performance tune study extract code; in particular the triggers; SQL injection detection may need to be changed from a function to domain regular expression check;
    * Complete harmonisation and documentation of error and trace messages.
  * Complete webserver integration for SAHSU [Remote access using my old desktop will be required]
    * Liaise with RIF middleware and JavaScript frontend to fix pernicious mixed content issues (mixtures of HTTP/HTTPS traffic);
    * Test SAHSU RIF testbed wpea-darwin remotely;  
    * Set up httpd as a reverse proxy so that Tomcat (running the Java middleware) traffic is forwarded securely to the tomcat server.
    * Secure wpea-darwin httpd by jailing it; 
    * Add HTTP caching to enhance web page load performance; 
    * Preparation for penetration testing by an accredited third party; SQL injection testing.
  * Provision and tuning of database services to the middleware and data loader. In particular ensure that partitioning is being used.

3. Assist with porting to Microsoft SQL server.

4. Information Governance module: Complete database support as required by the middleware.

5. Build and revision control.

6. Test harness:

  *	Add tests for all triggers. errors and to exercise a range of study choices;
  *	Per test logging to separate files;
  *   Remove *rif40_test_runs_.number_test_cases_registered*;
  *	Add *rif40_test_harness.port_specific_test*; either: P (Postgres only) or: S (SQL Server only);
  *	Auto registering of error and trace messages.

7. Additional RIF development tasks as required by Dr. Anna Hansell; 
