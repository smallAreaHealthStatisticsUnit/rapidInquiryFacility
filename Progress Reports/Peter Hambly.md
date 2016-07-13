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

#### 4th to 8th July
 
* Project planning for July to December
* Set geolevel_name, area_id, area_name from front end
* Add all fields to topojson

#### Current TODO list (July):

* Add geography name, description to form, field processing and XML file
* Add geolevel, goelevel name to legend
* Status update using uuidV1; batch mode (returns in onBusboyFinish()); add timeout recovery (switches to batch mode).
* ST_Union and area calculations can be done in geoJSON using turf: as it is a geomtery collection. 
* Add area_id and id uniqueness tests to shapefile checks and tests
* WKT support using Wellknown
* Id generator; gid support (especially in topojson)
	
#### August List:

* Map tile generator
* Geolevel intersction generator

#### September TODO list:

* Get methods

SQL Server porting expected to start in July if required (may be able to use Turf).
Note: no bounding box (bbox) in tiles.

#### Parked TODO list (as required):

* Database connection; clean, check OK and ST_Union(); area support [and checks]; PK support. Turf probably removes requirement 
  for any DB port, subject to acceptable performance
* Handle when areaID == name; allow NULL name
* Detect area mismatch between shapefiles	

* Prevent tab change during map draw and aoccordion setup or JQuery UI and Leaflet do bad things unless tkey have focus
* Fix customFileUpload styling so it uses the correct JQuery UI class style; the .css() function won't work on form file upload buttons
* Add support for XML config file so shpConvert can do all processing without further input;
* Restrict geolevels to a minimum 3, or more if the total topojson_size < ~20-30M (possibly browser dependent). 

* Add areaKm2 (using bounding box) as jsonfile property. Needs turf.
* Calucation of quantization and the max zoomlevel using area. Enforcement in browser. 
* Hover support for area name, area_km2 and shapefile supplied data at highest resolution

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
	
##	General RIF database Todo:

1. Documentation [in progress];
2. JSON injection protection [will be a regexp];
3. Connection timeout (connect/post connect) limits - as previous;
4. Integration to build, severeal options:
   i.   Replace topojson_convert.js;
   ii.  New node program to replace existing functionality;
   iii. Awaiting data loader.
	
## 2016 Plans: 


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

## Bugs

None outstanding.
