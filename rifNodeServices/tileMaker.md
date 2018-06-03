Tile Maker
==========

# Contents

- [1. Overview](#1-overview)
  - [1.1 Software Requirements](#11-software-requirements)
  - [1.2 Issues](#12-issues)
    - [1.2.1 Memory Requirements](#121-memory-requirements)
    - [1.2.2 SQL Server Connection Error](#122-sql-server-connection-error)
	- [1.2.3 JSZip 3.0 Error](#123-jszip-30-error)
	- [1.2.4 BULK INSERT Permission](#124-bulk-insert-permission)
	- [1.2.5 MSSQL Timeout: Request failed to complete in XXX000ms](#125 mssql-timeout-request-failed-to-complete-in-xxx000ms)
- [2. Running the Tile Maker](#2-running-the-tile-maker)
  - [2.1 Setup](#21-setup)
  - [2.2 Processing Overview](#22-processing-overview)
  - [2.3 Running the Front End](#23-running-the-front-end)
  - [2.3.1 Shapefile Format](#231-shapefile-format)
    - [2.3.2 Processing Huge Shapefiles](#232-processing-huge-shapefiles)
  - [2.4 Post Front End Processing](#24-post-front-end-processing)
    - [2.4.1 Geospatial Data Load](#241-geospatial-data-load)
    - [2.4.2 Tile Manufacture](#242-tile-manufacture)
    - [2.4.3 Load Production Data into the RIF](#243-load-production-data-into-the-rif)
- [3. TileMaker Source Code](#3-tilemaker-source-code)
- [4. TileMaker TODO](#4-tilemaker-todo)

# 1. Overview

This document details the loading of the administrative geography data and the geo-coding requirements of the RIF. 

The RIF web front end uses [leaflet](https://leafletjs.com/), which requires map tiles; these are squares that follow certain Google Maps conventions:

* Tiles are 256x256 pixels;
* At the outer most zoom level, 0, the entire world can be rendered in a single map tile;
* Each [zoom level](https://wiki.openstreetmap.org/wiki/Zoom_levels) doubles in both dimensions, so a single tile is replaced by 4 tiles when zooming in. This 
  means that about 22 zoom levels are sufficient for most practical purposes; the RIF uses 0-11;
* The Web Mercator ([WGS84](https://en.wikipedia.org/wiki/World_Geodetic_System), as used by GPS) projection is used, with latitude limits of around 85 degrees. Care 
  needs to be taken during simplification with 
  Northern latitudes in the USA because of the distortions in the Mercator projection (e.g. the size of Greenland); with each pixel being much shorter in
  length than in latitudes near to the equator.
  
Leaflet uses de facto OpenStreetMap standard, known as [Slippy Map Tilenames](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames) or XYZ, follows these and adds more:

* An X and Y numbering scheme; with Z for the zoomlevels;
* PNG images for tiles. In the RIF Leaflet also uses topoJSON tles for the administrative geography.

Background map images are served direct from the source through a REST API, with a URL like ```http://.../Z/X/Y.png``` where Z is the zoom level, and X and Y identify the tile. 

For example a zoomlevel 8 tile, X=123, Y=82; covering the Irish Sea, Liverpool and the Lancashire and Cumbrian coasts:
![alt text](http://a.tile.openstreetmap.org/8/125/82.png "Zoomlevel 8, X=123, Y=82; Irish Sea, Liverpool and the Lancashire and Cumbrian coasts")
 
The RIF does *NOT* cache background maps so on a private air-gapped network you will not get background maps. 

The administrative geography
uses [GeoJSON Layers](https://leafletjs.com/reference-1.3.0.html#geojson) server by the *rifServices* REST api. 
For performance reasons the RIF uses a modified 
[Leaflet.GeoJSONGridLayer](https://github.com/ebrelsford/leaflet-geojson-gridlayer) originally created by 
Eric Brelsford. The modification is to use TopoJSON grids to reduce the REST GET JSON size
and then internally converts to TopoJSON to GeoJSON for the Leaflet gridlayer. Some experiments have been 
carried out with local caching and there is a tile viewer program to test this.

The caching code is in the main RIF application but is disabled due to issues with browser support.

The job of the Tile Maker is to process a hierarchy of administrative shapefiles to:

* Generate the TopoJSON tiles required by the RIF and store them in the database;
* Generate the required data tables, scripts and setup data for a RIF administrative geography;
* Provide installer scripts for both Postgres and SQL Server.

Performance:

* SAHSULAND takes about 15 minutes to processing in total.
* USA to county level with only 9 zoomlevels:
  * Shapefile conversion and simplification: 115s.
  * Database load/clean etc: 1 hour 27 minutes
  * Tile manufacture: 5290 tiles in 2 hours  48 minutes
  About 5 hours in total.

The principal limitation is memory:
 
* UK Census output area 2011 is 1GB in size and has 227,759 records and 203,930,998 points! 
* This requires a machine with 32 or 63G memory.
* A pre-simplification program will be created to reduce the resolution suitably.

## 1.1 Software Requirements

* [Node.js 8](https://nodejs.org/en/);
* [Python 2.7](https://www.python.org/downloads/release/python-2714/);
* GNU Make (part of [Mingw](http://www.mingw.org/wiki/Getting_Started) MSYS on Windows) [Optional];
* Postgres 9.3 onwards and/or SQL Server 2012 onwards;

The software has not been tested on Linux or MacOS but should work.

## 1.2 Issues

### 1.2.1 Memory Requirements

A minimum of 16GB of RAM is required; if you are processing high resolution geographies (e.g. US census 
block groups or UK census output areas) you will require 32 to 64GB of RAM.

The memory requirement comes from the need to read an entire shapefile, convert each area to [GeoJson](http://geojson.org/), and finally progressively simplify the GeoJSON to be 
suitable for each zoomlevel.  

### 1.2.2 SQL Server Connection Error

Symptom; SQL Severer connect error ```Error: None of the binaries loaded successfully. Is your node version either >= 0.12.7 or >= 4.2.x or >= 5.1.1 or >= 6.1.0```

* SQL server connect error caused by a version mismatch between the Node.js packages *mssql* and *msnodesqlv8*; 
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
		"password": "XXXXXXXXXXXXXXXXX"
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
   
* Check you have [Visual Studio 2015/2017](https://www.visualstudio.com/downloads/) installed; launch it and logon to the Microsoft DEveloper network so the developer license is active;
* Remove *mssql* and *msnodesqlv8* from *package.json*;
* re-run: ```npm install msnodesqlv8 --save```:
  ```
  C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install msnodesqlv8 --save
  npm WARN optional SKIPPING OPTIONAL DEPENDENCY: fsevents@1.2.4 (node_modules\fsevents):
  npm WARN notsup SKIPPING OPTIONAL DEPENDENCY: Unsupported platform for fsevents@1.2.4: wanted {"os":"darwin","arch":"any"} (current: {"os":"win32","arch":"x64"})

  + msnodesqlv8@0.1.46
  updated 1 package in 10.388s   
  ```
* re-run: ```npm install mssql --save```:
  ```
  C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install mssql --save
  npm WARN optional SKIPPING OPTIONAL DEPENDENCY: fsevents@1.2.4 (node_modules\fsevents):
  npm WARN notsup SKIPPING OPTIONAL DEPENDENCY: Unsupported platform for fsevents@1.2.4: wanted {"os":"darwin","arch":"any"} (current: {"os":"win32","arch":"x64"})

  + mssql@4.1.0
  added 12 packages in 11.196s
  ```
 
### 1.2.3 JSZip 3.0 Error

The error log forever.err contains:
```
Error(Error): The constructor with parameters has been removed in JSZip 3.0, please check the upgrade guide.
Stack >>>
Error: The constructor with parameters has been removed in JSZip 3.0, please check the upgrade guide.
    at Object.JSZip (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\JSZip\lib\index.js:14:15)
    at zipProcessingSeriesAddStatus1 (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServices.js:765:17)
    at addStatusWriteDiagnosticFileRename (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServicesCommon.js:923:10)
    at FSReqWrap.oncomplete (fs.js:123:15)<<<

* LOG END ***********************************************************************

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServicesCommon.js:932
									throw e;
									^

Error: The constructor with parameters has been removed in JSZip 3.0, please check the upgrade guide.
    at Object.JSZip (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\JSZip\lib\index.js:14:15)
    at zipProcessingSeriesAddStatus1 (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServices.js:765:17)
    at addStatusWriteDiagnosticFileRename (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServicesCommon.js:923:10)
    at FSReqWrap.oncomplete (fs.js:123:15)
``` 

Ypou must install a version 2.6.N JSZip: ```npm install JSZip@2.6.1```

If you get:

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install JSZip@2.6.1
RIF40-geospatial@0.0.1 C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices
`-- jszip@2.6.1  invalid
```
Chnage the *package.json line from the version 3 JSZip to the 2.6.N version:
```
    "jszip": "^2.6.1",
```
The code does need to be updated to version 3.

A correct install looks like:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install JSZip@2.6.1
RIF40-geospatial@0.0.1 C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices
`-- jszip@2.6.1

npm WARN optional SKIPPING OPTIONAL DEPENDENCY: fsevents@^1.0.0 (node_modules\chokidar\node_modules\fsevents):
npm WARN notsup SKIPPING OPTIONAL DEPENDENCY: Unsupported platform for fsevents@1.2.4: wanted {"os":"darwin","arch":"any"} (current:
 {"os":"win32","arch":"x64"})
```

### 1.2.4 BULK INSERT Permission

SQL Server needs access permission granted to the directories used to `BULK INSERT` files, the files are not copied from the client to the 
server as in the *Postgres* *psql* ```\copy` command and the *Oracle* *sqlldr* command.

SQL Server needs access to the directories containing the data loaded by the scripts. The simplest
way is to allow read/execute access to the local users group (e.g. PH-LAPTOP\Users or USERS depending on your Windows version).

*DO NOT TRY TO RUN BULK INSERT FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).* Use a local directory which SQL Server has
access to; e.g. somewhere on the C: drive. Note that SQL Server *BULK LOAD* behaves deterrently if you logon using Windows authentication (where it will use your credentials 
to access the files) to using a username and password (where it will use the Server's credentials to access the file).

```
-- SQL statement 23: Load table from CSV file >>>
BULK INSERT cb_2014_us_county_500k
FROM 'C:\Users\Peter\OneDrive\SEER Data\Tile maker USA/cb_2014_us_county_500k.csv'	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line
WITH
(
	FORMATFILE = 'C:\Users\Peter\OneDrive\SEER Data\Tile maker USA/mssql_cb_2014_us_county_500k.fmt',		-- Use a format file
	TABLOCK					-- Table lock
);

Msg 4861, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 3
Cannot bulk load because the file "C:\Users\Peter\OneDrive\SEER Data\Tile maker USA/cb_2014_us_county_500k.csv" could not be opened. Operating system error code 5(Access is denied.).
```

### 1.2.5 MSSQL Timeout: Request failed to complete in XXX000ms

Timeout in Node [mssql](https://www.npmjs.com/package/mssql) package. Edit *mssqlTileMaker.js*  and set
the the requestTomeout in the MSSQL connection config:

```js
	var config = {
		driver: 'msnodesqlv8',
		server: p_hostname,
		requestTimeout: 300000, // 5 mins. Default 15s per SQL statement
		options: {
			trustedConnection: false,
			useUTC: true,
			appName: 'mssqlTileMaker.js',
			encrypt: true
		}
	};
```

```
error [events:96:dbErrorHandler()] dbErrorHandler() Error: dbErrorHandler(no callback): Timeout: Request failed to complete in 90000ms
Stack:
RequestError: Timeout: Request failed to complete in 90000ms
    at Request.tds.Request.err [as userCallback] (c:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\mssql\lib\tedious.js:578:19)
    at Request._this.callback (c:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\tedious\lib\request.js:60:27)
    at Connection.message (c:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\tedious\lib\connection.js:1936:24)
    at Connection.dispatchEvent (c:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\tedious\lib\connection.js:992:38)
    at MessageIO.<anonymous> (c:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\tedious\lib\connection.js:886:18)
    at emitNone (events.js:86:13)
    at MessageIO.emit (events.js:185:7)
    at ReadablePacketStream.<anonymous> (c:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\tedious\lib\message-io.js:102:16)
    at emitOne (events.js:96:13)
    at ReadablePacketStream.emit (events.js:188:7)
```
	
# 2. Running the Tile Maker

## 2.1 Setup

Install the required *Node.js* modules. Change directory into the *rapidInquiryFacility\rifNodeServices*:

```
cd C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices
make
```

If you do not have make, type:

```
mkdir node_modules/pg-native
npm install --save pg-native
npm install JSZip@2.6.0
npm install --save request JSZip turf geojson2svg clone object-sizeof form-data magic-globals helmet pg pg-native mssql msnodesqlv8 srs xml2js async reproject mapshaper forever shapefile node-uuid chroma-js jszip express morgan topojson request-debug cjson wellknown svg2png svg2png-many connect-busboy winston
```

To update the modules type ```npm update --save```.

The tile Maker is a web application, so you need to start the server. The Makefile has a number of targets to help with this:

- all: Build modules, run the complete database test harness
- modules: Build required Node.js modules using npm install --save to update dependencies in package.json
- clean: clean Node modules; avoid Windows path length stupidities with rimraf
- install:  No install target (dummy)
- server-start: start server
- server-restart: restart server
- server-restart-debug: restart server with debugging
- server-status: status of running server
- server-stop: stop server
- server-log: display server logs
- test\make_bighelloworld.js: create >2G data\bighelloworld.js
- update: Update required Node.js modules using npm install --save to update dependencies in package.json
- test: Run the test harness
- help: Display makefile help, rifNode.js help

Again, these commands can be run by hand; 

- Start:
  ```
  rm -f forever.err forever.log
  node node_modules\forever\bin\forever start -c "node --max-old-space-size=4096 --expose-gc" -verbose -l forever.log -e forever.err -o forever.log --append ./expressServer.js
  ```
- Stop:
  ```
  node node_modules\forever\bin\forever stop -verbose -l forever.log -e forever.err -o forever.log --append ./expressServer.js
  ```
- Restart:
  ```
  rm -f forever.err forever.log
  node node_modules\forever\bin\forever stop -verbose -l forever.log -e forever.err -o forever.log --append ./expressServer.js
  node node_modules\forever\bin\forever start -c "node --max-old-space-size=4096 --expose-gc" -verbose -l forever.log -e forever.err -o forever.log --append ./expressServer.js
  ```

Stop example:  
```
C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>make server-start
Debug level set to default: 1
node_modules\\forever\\bin\\forever start -c "node --max-old-space-size=4096 --expose-gc" -verbose -l forever.log -e forever.err -o forever.log --append ./expressServer.js
warn:    --minUptime not set. Defaulting to: 1000ms
warn:    --spinSleepTime not set. Your script will exit if it does not stay up for at least 1000ms
info:    Forever processing file: ./expressServer.js
```

Stop example:
```
C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>  node node_modules\forever\bin\forever stop -verbose -l forever.log -e forever.err -o forever.log --append ./expressServer.js
info:    Forever stopped process:
    uid  command                                    script                                                                                forever pid   id logfile                             uptime
[0] b_rz node --max-old-space-size=4096 --expose-gc C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices\expressServer.js 37284   33616    C:\Users\phamb\.forever\forever.log 0:0:9:31.853
```

Finally, start the *tile maker* application in a browser [http://127.0.0.1:3000/tile-maker.html](http://127.0.0.1:3000/tile-maker.html)
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/tile_maker_start.PNG?raw=true "Tile Maker Start Screen")

The following browsers have been tested:

* Chrome;
* Mozilla Firefox [recommended as it handles large memory requirements well];
* Internet Explorer 11 [not recommended as it is very bad at handling large memory requirements];
* Microsoft Edge

## 2.2 Processing Overview

GUI phase:

1. The *tile maker* web application is used to upload and convert shapefiles and simplifies the GeoJSON 
   geometry. The web application generates scripts and the *tile maker* configuration file: *geoDataLoader.xml*;
2. The user then downloads the processed data from server;

GUI phase then proceeds to script phase:

3. Run a SQL script. This loads the processed CSV data into the database (both Postgres and SQL Server are supported). The data is then cleaned and processed by the script into geospatial 
   data tables, data for tiles and the hierarchy and lookup tables needed by the RIF;
4. A tile manufacture node script is then run. This makes the topoJSON tiles; dumps geospatial table data to CSV files for the load scripts.
5. The RIF production load SQL script can now be run. This script loads and configure the geospatial data into your RIF database;
6. You can view the tiles using the *tile viewer* application.

The first load/clean/setup SQL script and the tile maker will be integrated into the web services at a later date. All the processing will then be in the front end and this leaves 
the user only needing to install the processed data into the database.

Processing concepts:

* Geography: The name of an administrative geography; e.g. USA_2014, EW_2001 (the 2011 census for England 
  and Wales);
* Geolevel: The name of a level in the hierarchy of shapefiles that make up the geography;
* Area ID: A code given to an area ID by the administrative authority (e.g. ONS for the 2011 Census);
* Area Name: A name corresponding to an *area ID*. 

## 2.3 Running the Front End

The *tile maker* web application is used to:

1. Upload a set of shapefiles (see next section for format), this optionally contains the *tile maker* 
   configuration file: *geoDataLoader.xml*. For first run through setup:
   
   * Enter a geography name and description;
   * For each administrative geography starting from the highest resolution:
     * Enter a description;
     * Select an *Area_ID* from the list of features in the shapefile; 
     * Enter a description for the *Area_id*;
     * Select an *Area_Name* from the list of features in the shapefile; 
     * Enter a description for the *Area_Name*;
   * When setup the user presses the **Upload file(s)** button. You then get a lot of processing messages.
   
   The descriptions may be pre-entered  for you if you have an ESRI extended attributes file (.shp.ea.iso.xml)
   
   ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/sahsuland_setup.PNG?raw=true "SAHSUland setup")

   So as a worked example for the United States to county level:
   
   * Geography name: USA_2014;
   * Description: US 2014 Census geography to county level;
   * Shapefile: cb_2014_us_nation_5m.shp:
     * Description: The nation at a scale of 1:5,000,000;
     * Area_ID: GEOID - Nation identifier;
	 * Area_Name: NAME - Nation name;
   * Shapefile: cb_2014_us_state_500k.shp:
     * Description: The State at a scale of 1:500,000;
     * Area_ID: STATENS - Current state Geographic Names Information System (GNIS) code;
	 * Area_Name: NAME - Current State name;
   * Shapefile: cb_2014_us_county_500k.shp:
     * Description: The County at a scale of 1:500,000;
     * Area_ID: COUNTYNS - Current county Geographic Names Information System (GNIS) code;
	 * Area_Name: NAME - Current county name;
   
   ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/USA_county_setup.PNG?raw=true "USA County setup")
  
   Note that you can also change:
   
   * The quantization. To be able to simplify the geoJSON the data must be quantized. The default is 
     1:1,000,000. This means that the smallest value is 1 millionth of the largest value. You will need to 
	 increase this quantization to 1:10,000,000 or 1:100,000,000 if you use more than 11 zoom levels or you cover are 
	 very large area. Quantization is applied on a per shapefile basis so there is no potential for slivers between areas.
	 However, if the shapefiles are not on the same scale (as in the US data) then you will see differences between 
	 the national data at 1:5,000,000 and the state and county data at 1:500,000;
   * The simplification factor. The *tile maker* uses Visvalingam algorithm 
     [Line generalisation by repeated elimination of the smallest area; Visvalingam, Maheswari; Whyatt, J. D. (James Duncan)Cartography -- Data processing; Computer science; July 1992](https://hydra.hull.ac.uk/resources/hull:8338)
     This is superior to the Ramer–Douglas–Peucker algorithm generally leaving no "cocked hat" artefacts. The 
	 effect of this parameter can be seen at [Mike Bostock's Line Simplification Example](https://bost.ocks.org/mike/simplify/)
	 Set a low value (e.g. 0.3) will result in modern art (i.e. triangles). Generally it needs to be increased slightly from the default (0.75)
	 for large areas and high resolutions to improve quality. This is especially true if you have high latitudes in your map 
	 and the mercator distortions are greater;
   * Maximum zoomlevel. The default is 11; reducing to 9 will reduce the processing time by a factor of 16. Every 
     increase quadruples processing time. !1 gives good quality even with fine census tracts/output areas.
   * Enables more diagnostics in the log
 
   The web application then:
   
   2. Converts the shapefiles to first GeoJSON the TopoJSON format in the WGS84 projection;
   3. Simplifies the GeoJSON geometry using the *Visvalingam* algorithm;
   4. Generates SQL scripts and the *tile maker* configuration file: *geoDataLoader.xml*;

   Informative message appear at the bottom of the screen:
   ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/tile_maker_processing.PNG?raw=true "Tile maker processing messages")
    
   Tile maker processing messages are also found in the *forever.err* log, e.g:
   
   * ```Processed zip file 1: SAHSULAND.zip; size: 6.73MB; added: 33 file(s)```
   * ```SAHSU_GRD_Level4: simplified topojson for zoomlevel: 7```
   * ```Created database load scripts: pg_SAHSULAND.sql and mssql_SAHSULAND.sql```
   
   [Tile-maker example log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/tile_maker_log.md)
   
   Finally a map is displayed of the adminstrative geography:
   ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/tile_maker_map.PNG?raw=true "Tile maker map")
   
5. The user then downloads the processed data from server using the two download buttons in the *shapefile 
   selector* tab. The *Download configuration" button returns an XML file e.g. 
   *shpConvertGetConfig_66d8a532-bb2c-4304-8e2b-ffde330b88fa.xml*; this is the *geoDataLoader.xml* for the run.

   The *Download processed files* button is currently not worked ans the underlying ZIP file is **NOT**
   implemented and it will produce an error:
   
   ```
   * LOG START *********************************************************************

   Fri Jun 01 2018 17:25:23 GMT+0100 (GMT Daylight Time)
   [httpErrorResponse:143; function: httpErrorResponseAddStatusCallback();
   Url: /shpConvertGetResults.zip?uuidV1=1dca95fe-7f68-4edc-9bc6-56dc76130920; ip: ::ffff:127.0.0.1]
   httpErrorResponse sent; size: 532 bytes: 
   Output: {"error":"ENOENT: no such file or directory, stat 'C:\\Users\\Peter\\AppData\\Local\\Temp\\shpConvert\\1dca95fe-7f68-4edc-9bc6-56dc76130920\\geoDataLoader.zip'","no_files":0,"field_errors":0,"file_errors":0,"file_list":[],"message":"shpConvertGetResults(): \nresults ZIP file: C:/Users/Peter/AppData/Local/Temp/shpConvert/1dca95fe-7f68-4edc-9bc6-56dc76130920/geoDataLoader.zip does not exist","diagnostic":"\n\nIn: shpConvertGetResults()","fields":{"uuidV1":"1dca95fe-7f68-4edc-9bc6-56dc76130920","xmlFileName":"geoDataLoader.zip"}}

   No errors

   * LOG END ***********************************************************************
   ```
   
   The files cab be found in your TMP directory, in Windows:
   C:\Users\&lt;Windows user&gt;\AppData\Local\Temp\shpConvert\&lt;unique file name&gt;
   
   e.g. ```C:\Users\Peter\AppData\Local\Temp\shpConvert\66d8a532-bb2c-4304-8e2b-ffde330b88fa```
   
   The structure of archive is:
   
   * Data directory contains the Postgres and SQL Server scripts and data. These are named:
   
     * *pg_/mssql_&lt;Geography&gt;.sql* - load for tile processing;
     * *rif_pg_/mssql_&lt;Geography&gt;.sql* - production load script;
	 * *&lt;Geolevel&gt;.csv* - geospatial data;
	 * *mssql_sahsu_&lt;Geolevel&gt;.fmt* - SQL Server bulk load format;
	 
	 E.g. 
	 
     * ```mssql_SAHSULAND.sql```;
     * ```mssql_sahsu_grd_level1.fmt```;
     * ```mssql_sahsu_grd_level2.fmt```;
     * ```mssql_sahsu_grd_level3.fmt```;
     * ```mssql_sahsu_grd_level4.fmt```;
     * ```pg_SAHSULAND.sql```;
     * ```rif_mssql_SAHSULAND.sql```;
     * ```rif_pg_SAHSULAND.sql```;
     * ```SAHSU_GRD_Level1.csv```;
     * ```SAHSU_GRD_Level2.csv```;
     * ```SAHSU_GRD_Level3.csv```;
     * ```SAHSU_GRD_Level4.csv```;
   * *&lt;Shapefile directory&gt;*: contains the shapefile data. One per geolevel;
   * *&lt;Geography&gt;* directory: contains the input data. E.g *SASULAND.zip*;
   * *diagnostics.log*: the log trace;
   * *geoDataLoader.xml*: configuration file;
   * *response.json.N*: internal JSON status at stages 1 to 3 of the processing;
   * *status.json*: processing statii in JSON format.
   
### 2.3.1 Shapefile Format

The best approach is to have each administrative geography in your hierarchy as single ZIP file containing a set of shapefiles. The tile maker requires two or more shapefiles with:

* A shapefile (.shp). This contains the geometric data in a proprietary ESRI format;
* dBASE III/IV file (.dbf). The contains the attributes records for each area in a dBASE table;
* An ESRI projection file (.prj). You must have a projection file, use: http://spatialreference.org/ to search for it.
  Using *OSGB* as the search term returns the UK national grid http://spatialreference.org/ref/epsg/4277/. You
  can then download the relevant .prj file. This actually only contains the ESRI WKT (wellknown text):
  ```
  GEOGCS["OSGB 1936",DATUM["D_OSGB_1936",SPHEROID["Airy_1830",6377563.396,299.3249646]],PRIMEM["Greenwich",0],UNIT["Degree",0.017453292519943295]]
  ```

There is a one-to-one relationship between geometry and attributes, which is based on record number. Attribute records in the dBASE file must be in the same order as records in the main file.

Optional files:

* Tile maker configuration file: *geoDataLoader.xml*. This contains the setup for your hierarchy and can be downloaded after your run;
* ESRI extended attributes XML file for a shapefile (.shp.ea.iso.xml). The contains principally the names of the attributes and their description.

Other files not required by the tile Maker:

* .shx — The index file that stores the index of the feature geometry; only required by ARCGIS;
* .sbn and .sbx — The files that store the spatial index of the features;
* .fbn and .fbx — The files that store the spatial index of the features for shapefiles that are read-only;
* .ain and .aih — The files that store the attribute index of the active fields in a table or a theme's attribute table;
* .atx — An .atx file is created for each shapefile or dBASE attribute index created in ArcCatalog. ArcView GIS 3.x attribute indexes for shapefiles and dBASE files are not used by ArcGIS. 
   A new attribute indexing model has been developed for shapefiles and dBASE files;
* .ixs — Geocoding index for read/write shapefiles;
* .mxs — Geocoding index for read/write shapefiles (ODB format);
* .cpg — An optional file that can be used to specify the codepage for identifying the character-set to be used.

The usual layout for the tilemaker ZIP file is:

-  *geoDataLoader.xml* [not needed first time around; generated by the *tile maker* web application]
- Directory containing the shapefiles. These *may* be contained in sub-directories:
  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/zip_file_structure_1.PNG?raw=true "ZIP file base directory")
  - The shapefiles themselves:
    ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/zip_file_structure_2.PNG?raw=true "ZIP file directory containing the shapefiles")

This allows the *tile maker* runs to be re-produced exactly.

Make sure:

* You follow the ESRI naming convention exactly;
* Do **NOT** use the same file name in multiple sub-directories or you will get:
  ```
  Unable to process list of filess
  Duplicate file: sahsu_grd_level1.dbf; shape file: sahsu_grd_level1.shp already processed
  ```

### 2.3.2 Processing Huge Shapefiles

To be added.

## 2.4 Post Front End Processing

Typical post front end processing:

* Loads tile maker output into both *sahsuland* and *sahsuland_dev* in a non RIF40 Schema. This can be any 
  database;
* Processes *sahsuland_dev* schema to tiles;
* Loads production data into the *rif40* schema on both *sahsuland* and *sahsuland_dev*.

The SEER pre-processing script *pg_load_seer_covariates.sql* has a dependency on the *cb_2014_us_nation_5m*, 
*cb_2014_us_state_500k* and *cb_2014_us_county_500l* that are part of the 
*tile maker* pre-processing. The FIPS code is required to make the join and this field is not in the standard 
lookup tables. For this reason it is necessary to build 
the covariates table on *sahsuland_dev*. In the longer term the FIPS codes should be added to the lookup tables. 

### 2.4.1 Geospatial Data Load

1. Place the files in the archive *data* in a new directory together with the *geoDataLoader.xml* configuration 
   file
2. Load data into a non RIF40 Schema:
   - Postgres: ```psql -U <username> -d <database name> -w -e -f pg_USA_2014.sql```
     Flags:
     * ```-U <username>```: connect as user &lt;username&gt; **NOT** *rif40*;
     * ```-d <database name>```: connect to database &lt;database name&gt;
     * ```-w```: never issue a password prompt. If the server requires password authentication and a password is not available by other means 
       such as a .pgpass file, the connection attempt will fail;
     * ```-e```: copy all SQL commands sent to the server to standard output as well;
     * ```-f pg_USA_2014.sql```: run SQL script pg_USA_2014.sql
  
     For information on [Postgres passwords](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/databaseManagementManual.md#221-postgres)

     E.g: ```psql -U peter -d sahsuland_dev -w -e -f pg_USA_2014.sql```

	[Postgres data processing example log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/postgres_data_processing.md)
	
   - SQL Server: ```sqlcmd -U <username> -P <password> -d <database name> -b -m-1 -e -r1 -i mssql_USA_2014.sql  -v pwd="%cd%"```
     Flags:
     * ```-U <username>```: connect as user &lt;username&gt; **NOT** *rif40*;
     * ```-P <password>```: the &lt;password&gt; for user &lt;username&gt;;
     * ```-d <database name>```: connect to database &lt;database name&gt;;
     * ```-b```: terminate batch job if there is an error;
     * ```-m-1```: all messages including informational messages, are sent to stdout;
     * ```-e```: echo input;
     * ```-r1```: redirects the error message output to stderr;
     * ```-i mssql_USA_2014.sql```: run SQL script mssql_USA_2014.sql;
     * ```-v pwd="%cd%"```: set script variable pwd to %cd% (current working directory). So bulk 
       load can find the CSV files.
	
	E.g:
	```sqlcmd -U peter -P XXXXXXXXXXX -d sahsuland_dev -b -m-1 -e -r1 -i mssql_USA_2014.sql  -v pwd="%cd%"```	

	[SQL Server data processing example log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/sql_server_data_processing.md)
	
* For each shapefile geolevel:
  * Create the table, comment;
  * Load data;
  * check rows;  
  * Add primary key;
  * Add geometry columns (1/geolevel + the original shapefile geometry), geographic centroid column;
  * Update geographic centroid, geometry columns, handle polygons and mutlipolygons,
    convert highest zoomlevel to original;
	- Postgres
	  ```SQL
	  UPDATE cb_2014_us_county_500k
	   SET geographic_centroid = ST_GeomFromText(geographic_centroid_wkt, 4326),
		   geom_6 = 
				CASE ST_IsCollection(ST_GeomFromText(wkt_6, 4326)) /* Convert to Multipolygon */
					WHEN true THEN 	ST_GeomFromText(wkt_6, 4326)
					ELSE 			ST_Multi(ST_GeomFromText(wkt_6, 4326))
				END,
		   geom_7 = 
				CASE ST_IsCollection(ST_GeomFromText(wkt_7, 4326)) /* Convert to Multipolygon */
					WHEN true THEN 	ST_GeomFromText(wkt_7, 4326)
					ELSE 			ST_Multi(ST_GeomFromText(wkt_7, 4326))
				END,
		   geom_8 = 
				CASE ST_IsCollection(ST_GeomFromText(wkt_8, 4326)) /* Convert to Multipolygon */
					WHEN true THEN 	ST_GeomFromText(wkt_8, 4326)
					ELSE 			ST_Multi(ST_GeomFromText(wkt_8, 4326))
				END,
		   geom_9 = 
				CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
					WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
					ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
				END,
		   geom_orig = ST_Transform(
				CASE ST_IsCollection(ST_GeomFromText(wkt_9, 4326)) /* Convert to Multipolygon */
					WHEN true THEN 	ST_GeomFromText(wkt_9, 4326)
					ELSE 			ST_Multi(ST_GeomFromText(wkt_9, 4326))
				END, 4269);
	  ```
	- SQL Server
	  ```SQL
	  UPDATE cb_2014_us_county_500k
	     SET geographic_centroid = geography::STGeomFromText(geographic_centroid_wkt, 4326),
		     geom_6 = geography::STGeomFromText(wkt_6, 4326).MakeValid(),
		     geom_7 = geography::STGeomFromText(wkt_7, 4326).MakeValid(),
		     geom_8 = geography::STGeomFromText(wkt_8, 4326).MakeValid(),
		     geom_9 = geography::STGeomFromText(wkt_9, 4326).MakeValid(),
		     geom_orig = geometry::STGeomFromText(geometry::STGeomFromText(wkt_9, 4326).MakeValid().STAsText(), 4269);
	  ```
  * Make geometry columns valid (Postgres only):
    ```SQL
	UPDATE cb_2014_us_county_500k
	   SET
		   geom_6 = CASE ST_IsValid(geom_6)
					WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_6), 3 /* Remove non polygons */)
					ELSE geom_6
				END,
		   geom_7 = CASE ST_IsValid(geom_7)
					WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_7), 3 /* Remove non polygons */)
					ELSE geom_7
				END,
		   geom_8 = CASE ST_IsValid(geom_8)
					WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_8), 3 /* Remove non polygons */)
					ELSE geom_8
				END,
		   geom_9 = CASE ST_IsValid(geom_9)
					WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_9), 3 /* Remove non polygons */)
					ELSE geom_9
				END,
		   geom_orig = CASE ST_IsValid(geom_orig)
				WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_orig), 3 /* Remove non polygons */)
				ELSE geom_orig
			END;
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -85.2045985857456 46.044431737627747
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -84.769219239639256 45.839784716078725
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -84.439364648483661 45.996148099462111
	psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -89.528959972298978 29.651621096712105
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -149.31411756797058 59.956820817190831
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -68.983577325577343 44.175116080674087
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -162.79221305489506 55.324770369325378
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -131.38639491395594 55.250884374979393
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -80.296620697321714 25.326798923010927
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -156.76186812997113 56.928697844261862
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -68.269311127101147 44.285601416405427
	psql:pg_USA_2014.sql:632: NOTICE:  Too few points in geometry component at or near point -162.87907356747357 64.452955401038409
	psql:pg_USA_2014.sql:632: NOTICE:  Self-intersection at or near point -105.05294356541158 39.913698399795408
	```
  * Check validity of geometry columns;
  * Make all polygons right handed:
    - Postgres
      ```SQL 
	  UPDATE cb_2014_us_county_500k
	   SET       geom_6 = ST_ForceRHR(geom_6),
		   geom_7 = ST_ForceRHR(geom_7),
		   geom_8 = ST_ForceRHR(geom_8),
		   geom_9 = ST_ForceRHR(geom_9),
		   geom_orig = ST_ForceRHR(geom_orig);
	  ```
	- SQL Server
	  ```SQL
	  WITH a AS (
			SELECT gid, geom_6,
				   CAST(area_km2 AS NUMERIC(21,6)) AS area_km2,
				   CAST((geom_6.STArea()/(1000*1000)) AS NUMERIC(21,6)) AS area_km2_calc
			  FROM cb_2014_us_county_500k
		), b AS (
			SELECT a.gid,
				   a.geom_6,
				   a.area_km2,
				   a.area_km2_calc,
				  CASE WHEN a.area_km2 > 0 THEN CAST(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2) AS NUMERIC(21,6))
						WHEN a.area_km2 = a.area_km2_calc THEN 0
						ELSE NULL
				   END AS pct_km2_diff 
		  FROM a
		)
		UPDATE cb_2014_us_county_500k
		   SET geom_6 = c.geom_6.ReorientObject()
		  FROM cb_2014_us_county_500k c
		 JOIN b ON b.gid = c.gid
		 WHERE b.pct_km2_diff > 200 /* Threshold test */;
	  ```
  * Create spatial indexes; 
* Create, populate and comment geography meta data table compatible with RIF40_GEOGRAPHIES 
  (called geography_&lt;geography name&gt;);
* Create, populate and comment geography meta data table compatible with RIF40_GEOLEVELS 
  (called geolevels_&lt;geography name&gt;);
* For each shapefile geolevel:  
  * Create, populate and comment lookup tables tables (called lookup_&lt;geography name&gt;);
* Create, populate and comment hierarchy table (called hierarchy_&lt;geography name&gt;);
* Create, populate and comment geometry table (called geometry_&lt;geography name&gt;);
* Partition geometry table (PostGres only);
* Create, populate and comment adjacency table (called adjacency_&lt;geography name&gt;);
* Create required functions:
  * tileMaker_longitude2tile(longitude DOUBLE PRECISION, zoom_level INTEGER)
  * tileMaker_latitude2tile(latitude DOUBLE PRECISION, zoom_level INTEGER)
  * tileMaker_tile2longitude(x INTEGER, zoom_level INTEGER)
  * tileMaker_tile2latitude(y INTEGER, zoom_level INTEGER)
  * tileMaker_intersector_usa_2014(
		l_geolevel_id INTEGER, 
		l_zoomlevel INTEGER, 
		l_use_zoomlevel INTEGER, 
		l_debug BOOLEAN DEFAULT FALSE)
  * tileMaker_intersector2_usa_2014(
		l_geolevel_id INTEGER, 
		l_zoomlevel INTEGER, 
		l_use_zoomlevel INTEGER, 
		l_debug BOOLEAN DEFAULT FALSE)
  * tileMaker_aggregator_usa_2014(
		l_geolevel_id INTEGER, 
		l_zoomlevel INTEGER,  
		l_debug BOOLEAN DEFAULT FALSE)
* Create and comment tiles table (called t_tiles_&lt;geography name&gt;); 
* Create and comment tiles view (called tiles_&lt;geography name&gt;); 
* Create and comment tile limits table (called tile_limits_&lt;geography name&gt;); 
* Create and comment tile intersects table (called tile_intersects_&lt;geography name&gt;); 
* Partition tile intersects  table (PostGres only);
* Populate and index tile intersects table (called tile_intersects_&lt;geography name&gt;); 
  ```
	psql:pg_USA_2014.sql:6039: INFO:  Processed 57+0 total areaid intersects for geolevel id 2/3 zoomlevel: 1/9 in 0.8+0.0s+0.0s, 0.8s total; 71.2 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 67+0 total areaid intersects for geolevel id 2/3 zoomlevel: 2/9 in 1.2+0.0s+0.0s, 2.0s total; 57.5 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 81+0 total areaid intersects for geolevel id 2/3 zoomlevel: 3/9 in 1.6+0.0s+0.0s, 3.6s total; 50.6 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 95+0 total areaid intersects for geolevel id 2/3 zoomlevel: 4/9 in 3.1+0.0s+0.0s, 6.7s total; 30.9 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 142+0 total areaid intersects for geolevel id 2/3 zoomlevel: 5/9 in 5.4+0.0s+0.0s, 12.1s total; 26.3 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 242+0 total areaid intersects for geolevel id 2/3 zoomlevel: 6/9 in 12.4+0.0s+0.0s, 24.5s total; 19.6 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 487+0 total areaid intersects for geolevel id 2/3 zoomlevel: 7/9 in 31.7+0.0s+0.0s, 56.2s total; 15.4 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 1020+0 total areaid intersects for geolevel id 2/3 zoomlevel: 8/9 in 101.4+0.0s+0.0s, 157.6s total; 10.1 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 2248+0 total areaid intersects for geolevel id 2/3 zoomlevel: 9/9 in 313.8+0.0s+0.0s, 471.4s total; 7.2 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 3233+0 total areaid intersects for geolevel id 3/3 zoomlevel: 1/9 in 7.0+0.0s+0.0s, 478.5s total; 459.1 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 3291+0 total areaid intersects for geolevel id 3/3 zoomlevel: 2/9 in 6.9+0.0s+0.0s, 485.3s total; 480.2 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 3390+0 total areaid intersects for geolevel id 3/3 zoomlevel: 3/9 in 4.9+0.0s+0.0s, 490.2s total; 693.6 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 3440+0 total areaid intersects for geolevel id 3/3 zoomlevel: 4/9 in 4.2+0.0s+0.0s, 494.4s total; 821.5 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 3658+0 total areaid intersects for geolevel id 3/3 zoomlevel: 5/9 in 4.6+0.0s+0.0s, 499.1s total; 795.8 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 4065+3 total areaid intersects for geolevel id 3/3 zoomlevel: 6/9 in 4.7+0.0s+0.0s, 503.8s total; 866.9 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 4989+1 total areaid intersects for geolevel id 3/3 zoomlevel: 7/9 in 7.8+0.0s+0.0s, 511.7s total; 636.6 intesects/s
	psql:pg_USA_2014.sql:6039: INFO:  Processed 7127+4 total areaid intersects for geolevel id 3/3 zoomlevel: 8/9 in 19.1+0.1s+0.0s, 530.8s total; 373.4 intesects/s
  ```
* Create statistics on all tables;
* For each shapefile geolevel:  
  * Test Turf (Node.js processing) and database calculated areas agree to within 1% (Postgres)/5% (SQL server)
  
### 2.4.2 Tile Manufacture

In the same directory as before run the *tileMaker* manufacturer. This has separate Postgres and SQL Server stubs calling a common 
*tileMaker.js* node.js core:

- ```node <full path to script> --database <flags>```
     Flags:
	 * ```-D, --database  <database name>```: Name of the database.          
	   [default: <user default>];
	 * ```-U, --username <username>```: Connect as user &lt;username&gt; **NOT** *rif40*.         
	   [default: NONE (use MSSQL trusted connection/psql style default)];
	 * ```--password, --pw <password>```: The &lt;password&gt; for user &lt;username&gt;
	 * ```-H, --hostname```: &lt;hostname&gt; of the database. 
	   [default: "localhost"];
	 * ```-V, --verbose```: Verbose mode. 
	   [default: 0: false; 1 or 2];
	 * ```-X, --xmlfile <XML file>```: XML Configuration file &lt;XML file&gt;.
	   [default: "geoDataLoader.xml"];
	 * ```-p, --pngfile```: Make SVG/PNG files.                     
	   [default: false]
	 * ```-h, --help```: display this helpful message and exit.
	   [default: false]

- Postgres: ```node C:\Users\%USERNAME%\Documents\GitHub\rapidInquiryFacility\rifNodeServices\pgTileMaker.js --database sahsuland_dev```
  [Postgres tile manufacture example log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/postgres_tile_manufacture.md)

- SQL Server: ```node C:\Users\%USERNAME%\Documents\GitHub\rapidInquiryFacility\rifNodeServices\mssqlTileMaker.js -U peter --password peter --database sahsuland_dev```
  [SQL Server tile manufacture example log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/sql_server_tile_manufacture.md)
	
* Parse XML configuration file;
* Connect to database;
* Create hierarchy CSV file;
* Create lookup CSV file for each geolevel;
* Create adjacency CSV file for geography;
* Create geometry CSV file for geography;
* Create tiles 10 at a time for each zoomlevel and geography;
* Creating tile CSV file for each geolevel;
* Created dataLoader XML config file
* All 6 tests passed, none failed
* Produce Zoomlevel and geolevel report (null tiles/total tiles):

  |          zoomlevel |          geolevel_1 |          geolevel_2 |          geolevel_3 |
  |--------------------|---------------------|---------------------|---------------------|
  |                   0                  0/1 |                 0/1 |                 0/1 |
  |                   1                      |                 0/3 |                 0/3 |
  |                   2                      |                 0/5 |                 0/5 |
  |                   3                      |                0/10 |                0/10 |
  |                   4                      |                0/22 |                0/22 |
  |                   5                      |                0/47 |                0/48 |
  |                   6                      |               0/111 |               0/118 |
  |                   7                      |               0/281 |               0/333 |
  |                   8                      |               0/665 |               0/992 |
  |                   9                      |              0/1568 |              0/3137 |
	
### 2.4.3 Load Production Data into the RIF

1. Load production data into RIF40 Schema.
   - Postgres: ```psql -U rif40 -d <database name> -w -e -f rif_pg_usa_2014.sql```
     Flags:
     * ```-U rif40```: connect as *rif40*;
     * ```-d <database name>```: connect to database &lt;database name&gt;
     * ```-w```: never issue a password prompt. If the server requires password authentication and a password is not available by other means 
       such as a .pgpass file, the connection attempt will fail;
     * ```-e```: copy all SQL commands sent to the server to standard output as well;
     * ```-f rif_pg_usa_2014.sql```: run SQL script rif_pg_usa_2014.sql
  
     For information on [Postgres passwords](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/databaseManagementManual.md#221-postgres)

     E.g: ```psql -U rif40 -d sahsuland -w -e -f rif_pg_usa_2014.sql```
	 
	[Postgres data load log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/postgres_load.md)
	 
   - SQL Server: ```sqlcmd -U rif40 -P <password> -d <database name> -b -m-1 -e -r1 -i rif_mssql_usa_2014.sql  -v pwd="%cd%"```
     Flags:
     * ```-U rif40```: connect as *rif40*;
     * ```-P <password>```: the &lt;password&gt; for user *rif40*;
     * ```-d <database name>```: connect to database &lt;database name&gt;;
     * ```-b```: terminate batch job if there is an error;
     * ```-m-1```: all messages including informational messages, are sent to stdout;
     * ```-e```: echo input;
     * ```-r1```: redirects the error message output to stderr;
     * ```-i rif_mssql_usa_2014.sql```: run SQL script rif_mssql_usa_2014.sql;
     * ```-v pwd="%cd%"```: set script variable pwd to %cd% (current working directory). So bulk 
       load can find the CSV files.
	
	E.g:
	```sqlcmd -U rif40 -P XXXXXXXXXXX -d sahsuland -b -m-1 -e -r1 -i mssql_USA_2014.sql  -v pwd="%cd%"```

 	[SQL Server load example log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/sql_server_load.md)
	
2. Test the RIF is setup correctly:
	
- Check the *rif40_geographies* table;
- Check the *rfi40_geolevels* view;
- Check geography is selectable in the initial study submission screen;

Add data, then:

- Check study and comparison area selection works OK;
- Setup and run a study.

# 3. TileMaker Source Code

TO BE ADDED.

# 4. TileMaker TODO

TileMaker is currently working with some minor faults but needs to have:

1. Support for population weighted centroids];
2. Run the generated scripts. This requires the ability to logon and PSQL copy needs to be replaced to SQL COPY from STDIN/to STDOUT with STDIN/STOUT
   file handlers in Node.js;
3. UTF8/16 support (e.g. Slättåkra-Kvibille should not be mangled as at present);
4. Support very large shapefiles (e.g. COA2011);
5. Make ZIP file download work;
6. GUI's needs to be merged and brought up to same standard as the rest of the RIF. The TileViewer screen is in better shape
   than the TileMaker screen. Probably the best solution is to use Angular;
7. Support for database logons;
8. Needs to calculate geographic centroids using the database;
9. Add all DBF fields in shapefile to lookup table (i..e add FIPS codes).

Peter Hambly
June 2018