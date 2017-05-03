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

#### Current TODO list (May 2017): SQL Server Port

* Fix missing level 4 geography tiles bug (areaid_count=0 in geolevels table)
* Harden SQL SErver port against SQL Injection getting past middleware into meta data
* Process Utah geography
* Disable guest logins on SQL Server
* Add localhost notes to tomcat install doc; add network setup to SQL Server install notes; 
  add notes on cross site scripting errors (caused by URL/webservices name mismatch); firefox example:
  ```
  GET XHR https://peter-pc:8080/rifServices/studySubmission/ms/getDatabaseType?userID=peter [HTTP/1.1 200  25ms]
  09:09:31.552 Cross-Origin Request Blocked: The Same Origin Policy disallows reading the remote resource at https://peter-pc:8080/rifServices/studySubmission/ms/getDatabaseType?userID=peter. (Reason: CORS header 'Access-Control-Allow-Origin' missing). 1 (unknown)
  ```
  
* Discuss changing passwords
* Geospatial SQL Server and Postgres install issue (caused by pre-exsiting studies). Add checks for studies:
```
	-- SQL statement 75: Remove old geolevels meta data table >>>
	DELETE FROM t_rif40_geolevels WHERE geography = 'SAHSULAND';

	Msg 547, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
	The DELETE statement conflicted with the REFERENCE constraint "rif40_covariates_geolevel_fk". The conflict occurred in database "sah
	suland_dev", table "rif40.rif40_covariates".
	Msg 3621, Level 0, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
	The statement has been terminated.
```
* Test database and user account creation with db_create target. Need to keep postgres admin logged on
* Assist with middleware (database fixes)
* Drop script for SQL server to all rif40_sahsuland_dev_install.bat/rif40_sahsuland_install.bat to be re-run without rebuilding the entire database

#### TileViewer TODO (deferred to June?):
 
* Area tests (area_check.sql) is failing for Halland - suspect area is too small, could be projection ia wrong 
* NVarchar support for areaName
* Get fetch views to handle zoomlevel beyond max zoomlevel (returning the usual NULL geojson)
* Add tileid to tile topoJSON/GeoJSON; include in error messages; add version number 
  (yyyymmddhh24mi) for caching (i.e. there is no need to age them out if auto compaction is running)
* Fix blank name properties
* Add all properties from lookup table
* Missing name in level2 sahsuland (caused by mixed case field names)
* Add parent area_id, name
* Resize
* Add all Shapefile DBF fields to lookup table;
* UUID support
* Add Winston logging
* Separate DB logons using UUID; add username/password support
* Convert v4_0_create_sahsuland.sql to use tileMaker sahsuland, and remaining test scripts)
* Tilemaker drop scripts. Probably needed for v4_0_drop.sql and hence sahsuland_dev rebuild
  
* SAHSULAND was using Nevada north 1927, now using OSGB at present. The original plan
  was for tandard test configurations:
  * SAHSULAND: relocated to Utah: reprojected to 1983 North American Projection (EPSG:4269)
	* Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; use rif40_geolevels lookup_table/tile_table
  * DOGGERLAND: relocated to 54°20'0"N 5°42'59"E on the Dogger Bank. This is the site of wreck of SMS Blucher. 
    https://www.google.co.uk/maps/place/54%C2%B020'00.0%22N+5%C2%B042'59.0%22E/@54.3332107,0.9702213,5.92z/data=!4m5!3m4!1s0x0:0x0!8m2!3d54.3333333!4d5.7163889 
  * USA: USA to county level [this is OK]
  
  These will need to use a suitable projection within bounds and also be translated to the desired place. i.e. using proj4 in Node.
  
* Fix zoomlevel field miss-set from config file (defaults are wrong)

####  TODO list:

* Data loader to generate primary keys. PK on pop_sahsuland_pop_pk + cluster (see: v4_0_create_sahsuland.sql)
* Fix for T_RIF40_CONTEXTUAL_STATS/RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION
* Convert remaining use of geography:: datatype in SQL Server to geometry::. The geography:: datatype is used in the build
  to intersect tiles and will may have issues. Production SQL Server is using the geometry:: datatype. This will be parked if 
  it is not a problem.
* JSZip 3.0 upgrade required (forced to 2.6.0) for present
* SQL load script generator: still todo, all can wait:
  * Add search path to user schema, check user schema exists, to Postgres version
* Get methods: 
  * ZIP results;
  * Run front end and batch from XML config file.
  * Add CSV files meta to XML config;
* Tilemaker etc:
  * Add old fileds in DBF file to lookup tables;  
  * Drive database script generator from XML config file (not internal data structures);
  * Missing comments on other columns from shapefile via extended attributes XML file;
  * Dump SQL to XML/JSON files (Postgres and SQL Server) so Kevin does not need to generate it;
  * Add trigger verification code from Postgres to tilemaker build tables (t_rif40_geolelvels, rif40_geographies);
  * Fix in Node:
    - Triangles (to keep QGIS happy)
    - Self-intersections, e.g. at or near point -76.329400888614401 39.31505881204005
    - Too few points in geometry component at or near point -91.774770828512843 46.946012696542709
  * Check Turf JS centroid code (figures are wrong);
  * Compare Turf/PostGIS/SQL Server area and centroid caculations;

Note: no bounding box (bbox) in tiles.

#### Parked TODO (todon't) list (as required):

* MS Sahsuland projection problem; must be fully contained within the projection or area calculations and intersections will fail. 
  This needs to be detected. Currently geography datatype caluclates area wrong even after the polygon has been reorientated (i.e. 
  follow the right hand rule). The script: area_check.sql fails on SQL Server at this point. PostGIS will complain:

	transform: couldn't project point (-77.0331 -12.1251 0):
		latitude or longitude exceeded limits (-14)
  
   PostGIS, SQL Server and PROJ.4 (i.e. the projection file)  does not  have these bounds. Each projection's bounds are unique, and are 
   traditionally published by the authority that designed the projection. One of the primary sources for this data is from 
   https://www.epsg-registry.org. This would require an additional table. 

   So it is not easy to check if the geometry not within projected bounds, although possible.
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
  
### Database Bugs (deffered)

* AreaName duplicates to be allowed; key enforcementment to be in the heirarchy table; this allows 
  for duplicate county names within a state
* Change CREATE study to run in own schema; create procedure to transfer study/map tables to correct schema 
  and grant back permissions [i.e. remove security issue with current code]
* Add t_rif40_study_areas trigger check (once per INSERT/UPDATE) for correct use of band_id in rif40_study_shares. 
  Alternatively check in rif40_run_study
* Rename offset in age_sex_groups (reserved keyword)
  
### New features (deffered)

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
