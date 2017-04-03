# CDC RIF 4.0 Progress Report February 2017

## Highlights

* Data loader script port to SQL Server; full geospatial loader port to SQL Server.
* Completed first draft of user guide.

## February Summary

**Week**|**Week Starting**|**PH**|**KG**|**DM**|**BP**|**MD**|**Milestone**|**Notes**
:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:
 | | | | | | | | 
6|06 February 2017|CDC Visit|CDC Visit|CDC Visit|CDC Visit| |Postgres RIF complete| 
7|13 February 2017|SQL Server data loader script integration|SQL Server data loader script integration|TopoJSON tiles|Documentation, manual|Transferred to COSMOS/SCAMP|tileMaker SQL Server Integration| 
8|20 February 2017|SQL Server data loader script integration|SQL Server data loader script integration|Risk analysis area selection| | | | 
9|27 February 2017|SQL Server Installer documentation|SQL Server data loader script integration|Code refactoring, testin|US SEER (Cancer) example data| |Data loader SQL Server install|                                                                                         |                                                                                                |                                                                     |                                                                                           | Postgres Data Loader, Postgres install; tileMaker Postgres intrgration |       |

## Planned work for March through May

### March Plans

Work plan for March agreed 29/2/2017:

1. Brandon: Documentation with David; test datasets: US SEER cancer data 
2. David: re-implement one tile get middleware method; code refactor; documentation,
   risk analysis.
3. Peter: SQL Server run study (early March), geospatial and data loader integration testing;
4. Kevin Data loader integration testing

Expected highlights this month:

• Implement three remaining middleware methods 
• Data loader script port to SQL Server
• Geospatial integration scripts for SQL server

**Week**|**Week Starting**|**PH**|**KG**|**DM**|**BP**|**MD**|**Milestone**|**Notes**
:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:
 | | | | | | | | 
10|06 March 2017|SQL Server data loader script integration|SQL Server data loader script integration|US SEER (Cancer) example data| | |wpea-traffic/wpea-darwin resdy for install testing| 
11|13 March 2017|Data loader and tileMaker inegration and tesing|Middleware build|Code refactoring, testing|US SEER (Cancer) example data| | | 
12|20 March 2017|Data loader and tileMaker inegration and tesing|Leaves for Cabinet Office|Data loader and middleware handover|Data loader and middleware handover| | | 
13|27 March 2017|SQL Server study submission (run study)| |Data loader and middleware handover|Data loader and middleware handover| | | 
14|03 April 2017| | |SQL Server middleware|SQL Server middleware| |CDC sandbox ready| 
15|10 April 2017| | | | | |Study submission running on SQL server| 
16|17 April 2017|Assist with middleware (database fixes); SQL Server full install testing| | | | | | 
17|24 April 2017| | | | | |SQL Server RIF complete| 
18|01 May 2017|CDC Install| |CDC Install| | |CDC Install| 
19|08 May 2017|Background tile download (csache support)| |Risk analysis sstaitical requirements| | | | 
20|15 May 2017| | | | | | | 
21|22 May 2017|UK COA 2011 tiles| |Risk analysis| | | | 
22|29 May 2017| | | | | | | 
23|05 June 2017| | | | | | | 
24|12 June 2017|SAHSU RIF install| |SAHSU RIF install|SAHSU RIF install| | | 
25|19 June 2017|SAHSU RIF data load| |SAHSU RIF data load| | | | 
26|26 June 2017| | | | | | | 

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
* Support database (i.e. SQL Server) not being present

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


 

 
