# CDC RIF 4.0 Progress Report February 2017

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

## Planned work for March through May

### March Plans

Work plan agreed 30/2/2017:

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

- More refactoring
- Browser compatibility fixes
- Middle ware method getTileMakerTiles to get topojson by leaflet gridLayer
- Tiled topojson now used in front end
- Debugging of R smoothing script to work with RIF submission options

## Middleware

### Web services (rifServices) - Kevin/David

### Run study batch - Kevin

- No progress required.

#### R - Brandon

- Minor changes & documentation

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter

* Remove spurious points using tilemaker. These are caused by the simplification; remove block attribute;
* TopoJSON support (Needs Leaflet gridlayer, Leaflet 1.0+ upgrade, leaflet-geojson-gridlayer);
* Added geography meta data XML: sahsuland_geography_metadata.xml for data loader;
* Prevent geography reload in production scripts if in use on a study.
* Add geographic centroid to lookup table
* Change db/geography/geomlevel made a popup dialog box; auto sizing now OK
* Added Google maps, OSM satellite and terrain data to map list
* Use onEachFeature to call createPopup() - much more efficient
* Fixed feature gid to be gid of areaID
* Sync sahsuland_empty: tiles and lookup tables
* Added caching to base layers and topoJSON tile layer; fixed baselayer max zooms; allow test harness to go the 
  zoomlevel 19 
* Cache ageing, global auto compaction
* Fixed private browsing mode issues
* Support database (i.e. SQL Server not present)

#### 30th January to 3rd February 

* TileViewer (tile-viewer.html) web screen by DB/geography; DB web service. Currently slow because using geoJSON.
  Both SQL server and Postgres tiles display for both SAHSULAND and USA to county level.
* Note resizing bug in tile-maker.html is probably caused by setting the height of the map div in html once 
  leaflet is initialized.
* All tiles confirmed OK

## Databases

### Postgres, integration - Peter

### Microsoft SQL server - Peter

* SQL Server test builds
* SQL Server database build: sahsuland, sahsuland_dev and test; fully scripted and documented (and added to wiki) with no need for Server Adminstrator
  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/installation/README.md
* Resolved geospatial setup data trigger issues

## Documentation [18th March target]

### User manual - Brandon

* Completed first draft of user guide 

### Data Loading - Kevin

* Completed data loader ready for integration testing. SQL Server scripts generated and tested.

### Data Loading (Geospatial) - Peter

* No progress (March 2017)

### SQL Server Install - Peter

* No progress (March 2017)


 

 
