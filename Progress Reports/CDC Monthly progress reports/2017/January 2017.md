# CDC RIF 4.0 Progress Report January 2017

## Highlight

* Data.loader and tilemaker integration on Postgres, SQL Server: tilemaker only.

## January Summary

| Week | Week Starting     | PH                                                            | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                                                                        | Milestone                                                              | Notes |
|------|-------------------|---------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|-------------------------------------------------------------------------------------------|------------------------------------------------------------------------|-------|
| 1    | 02 January 2017   | Postgres and SQL Server tile integration and testing          | 7. Data Loader (35 days) - start liable for 2/3 weeks delays caused by more work on GET methods   | Documentation, manual                                                                          |                                                                     | Assist Kevin: SQL Server porting issues - middleware query formatter, data loader scripts |                                                                        |       |
| 2    | 09 January 2017   |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 3    | 16 January 2017   |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 4    | 23 January 2017   |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 5    | 30 January 2017   |                                                               | Holiday                                                                                           |                                                                                                |                                                                     |                                                                                           | Postgres Data Loader, Postgres install; tileMaker Postgres intrgration |       |

## Planned work for January through March

### January Plans

Work plan until February 6th agreed 12/6/2016:

1. Kevin:  Postgres data loader as per document with agreed restrictions; generates scripts for SQL Server. Test integration in 
   sahusland_empty, then SQL server from february. Assist David as required. Then, from mid february SQL Server middleware 
   (with Peter/David). SQL Server data loader
2. Brandon: Documentation with David;
3. Margaret: Assist Kevin with data loader. No further work on SQL Server port from 6/2/2017 - transferred to COSMOS.
   * Finish testing as best you can the MS SQL versions of the query formatter classes;  
   * SQL server porting issues;
   * Write down full step-by-step for connecting JDBC to an SQL Server db;
   * Write down any other suggestions you think might be relevant.
4. David: Implement one remaining middleware methods (study geography – data from RIF40_STUDIES); code refactor; documentation
5. Peter: Geospatial integration scripts for SQL server and Postgres; testing. Then SQL Server run study (early February);

Expected highlights this month:

• Implement three remaining middleware methods 
• Achieve middleware database logon on SQL server using front end
• Geospatial integration scripts for SQL server

| Week | Week Starting     | PH                                                            | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                                                                        | Milestone                                                              | Notes |
|------|-------------------|---------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|-------------------------------------------------------------------------------------------|------------------------------------------------------------------------|-------|
| 6    | 06 February 2017  | CDC Visit                                                     |                                                                                                   |                                                                                                |                                                                     |                                                                                           | Postgres RIF complete                                                  |       |
| 7    | 13 February 2017  | SQL Server study submission (run study)                       | SQL Server data loader script integration                                                         | Documentation, manual                                                                          | Documentation, manual                                               | Transferred to COSMOS/SCAMP                                                               | tileMaker SQL Server Integration                                       |       |
| 8    | 20 February 2017  |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 9    | 27 February 2017  |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           | Data loader SQL Server install                                         |       |
| 10   | 06 March 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 11   | 13 March 2017     | SQL Server Installer documentation                            | SQL Server middleware build                                                                       |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 12   | 20 March 2017     | Assist with middleware                                        | Leaves for Cabinet Office                                                                         |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 13   | 27 March 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 14   | 03 April 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 15   | 10 April 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           | Study submission and Results viewer running on SQL server              |       |
| 16   | 17 April 2017     | Assist with SQL Server dataloader                             |                                                                                                   | SQL Server dataloader driving tables                                                           |                                                                     |                                                                                           |                                                                        |       |
| 17   | 24 April 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 18   | 01 May 2017       |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           | SQL Server RIF complete                                                |       |

## Data Loader - Kevin


## Front end (webPlatform): Disease Mapping, Data viewer - David

- Refactoring of mapping and ui-grid code
- Added middle ware method to get geography info for complete study
- Polling of study status using $interval to notify on completion of submitted job
- Modified getSmoothedResults method to not need a year
- Population pyramids plotable by year
- Adding colour swatches to choropleth maps
- UK postcode base layers

## Middleware

### Web services (rifServices) - Kevin/David

### Run study batch - Kevin

- No progress required.

#### R - Brandon

- Minor changes & documentation

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter

* Completed production load script: SQL Server;
* Regression tests.
* SAHSULAND tests; fixes for numgeolevels, precision fuuzy match in proj4 data; event race, PG schema issues; 
  removed optimized_geojson from tiles table
* Installed new sahsuland geometry, tiles etc.
* RIF and non RIF path fixes for Postgres
* Check tiles all generated and not null, no extra, none missing 
* Zoomlevel and geolevel report (null tiles/total tiles)
* USA tests to zoomlevel 8 OK. Improved SQL Server tile making efficency and reduced table size to get under 10K limit
* Fix SQL server load script etc
* Integration to sahsuland_empty build. 
  * Tiles view and table compared to previous; after slight index and view tune efficeny as before; no missing/extra tiles
  * The adajacency matrix function needs to be checked.
  * Check json format in tiles tables. New tiles have a BBOX! Changed for full compatibility: 
	  id to gid,
	  areaID to area_id,
	  areaName to name
	The default names are not quite the same: "Kozniewska LEVEL4(01.013.016800.3)" as opposed to "01.013.016800.3"
  * Added GID to lookup tables
* Converted remaining rif40_xml_pkg functions to support tilemaker table names 
* Allow non study or health data related test scripts (1, 2, 3 and 6) to run on sahusland_empty
* Found bug in MS SQL hierarchy table: row numbers are the same but the smaller areas are being picked.
* Separate Postgres and SQL server tiles, hierarchy and lookup CSV files (so they can be compared)
* Lookup files are the same, and ordered
* Fixed SQL Server heirarchy bug. Caused by geography datatype. Fixed geom_orig to be geometry datatype and used that. Also  
  ordered hierarchy CSV files. Postgres and SQL Server hierarchy and lookup tables now exactly the same; and agree with old PostGIS
  tile build. Regression tests OK.
* MS Sahsuland projection problem; parked: see below.

#### 30th January to 3rd February 

* TileViewer (tile-viewer.html) web screen by DB/geography; DB web service. Currently slow because using geoJSON.
  Both SQL server and Postgres tiles display for both SAHSULAND and USA to county level.
* Note resizing bug in tile-maker.html is probably caused by setting the height of the map div in html once 
  leaflet is initialized.
* All tiles confirmed OK

## Databases

### Postgres, integration - Peter

### Microsoft SQL server - Peter

* TileMaker integration started
* Run study from february
* Add remaining tile fetch functions required by Middleware from mid March (Kev can do these directly in middleware if he has the
  tile zoomlevel/x/y)

## Documentation[18th March target]

### User manual - Brandon

* Started work on documentation 

### Data Loading - Kevin

### Data Loading (Geospatial) - Peter

* No progress (March 2017)

### SQL Server Install - Peter

* No progress (March 2017)


 

 
