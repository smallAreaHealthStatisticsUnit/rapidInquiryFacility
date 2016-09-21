# CDC RIF 4.0 Progress Report Start September 2016

## Highlight

Results Viewer (expected 26th September) will be delayed by Kev work on ALSPAC as of 16th September it needed 
another 3-5 days work on integrating R into Java so that disease mapping will run to completion and the results 
can be viewed.

Kevin was off sick for effectively three weeks in August. To balance the workload work on SQL Server study 
creation and data extraction has now been moved to Peter towards the end of October. 

## August Summary

| Week | Week Starting     | PH                                                                   | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                             | Milestone                                                 | Notes |
|------|-------------------|----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|------------------------------------------------|-----------------------------------------------------------|-------|
| 31   | 01 August 2016    | Shapefile services: 3.2 Geospatial outputs (8 days)                  | Integrate R into Java middleware (10 days)                                                        | Handover to new GIS person                                                                     | Integrate R into Java middleware (10 days)                          |                                                |                                                           |       |
| 32   | 08 August 2016    |                                                                      |                                                                                                   | Data Viewer - middleware services (5 days)                                                     |                                                                     |                                                |                                                           |       |
| 33   | 15 August 2016    | Holiday                                                              | 6.3 Middleware services - create study (14 days) NO PROGRESS DUE TO ILLNESS; Transferred to Peter | Holiday                                                                                        | Holiday                                                             |                                                |                                                           |       |
| 34   | 22 August 2016    | Shapefile services: 3.2 Geospatial outputs II (4 days)               |                                                                                                   | 6.1 Taxonomy services, 6.2 Database logon                                                      |                                                                     |                                                |                                                           |       |
| 35   | 29 August 2016    | Shapefile services: 3.3 Get Methods I (6 Days)                       |                                                                                                   | 6.3 Middleware services - other (4 days) [Potentially Kevin as well]                           | Not allocated                                                       | Holiday to 5th September                       | Results Viewer (expected 26th September)                  |       |

## Planned work for September and October

| Week | Week Starting     | PH                                                                   | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                             | Milestone                                                 | Notes |
|------|-------------------|----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|------------------------------------------------|-----------------------------------------------------------|-------|
| 36   | 05 September 2016 |                                                                      | Not allocated (for overrun - 10 days); now: Data viewer get methods                               | Allocated to JG                                                                                | 8.1 US Test dataset, 8.2 Test Plan, 8.3 Manual calculation of tests | 6.1 Schema comparison (3 days)                 |                                                           |       |
| 37   | 12 September 2016 | 6.2 SQL server schema completion (5 days)                            |                                                                                                   | 6.3 Middleware services - other (10 days) [Potentially Kevin as well]: data viewer get methods |                                                                     | 6.2 SQL server schema completion (as required) |                                                           |       |
| 38   | 19 September 2016 |                                                                      | ALSPAC                                                                                            |                                                                                                |                                                                     |                                                | Results Viewer                                            |       |
| 39   | 26 September 2016 | Shapefile services: 3.3 Get Methods II (6 Days)                      |                                                                                                   |                                                                                                |                                                                     |                                                |                                                           |       |
| 40   | 03 October 2016   |                                                                      |                                                                                                   | Allocated to JG                                                                                |                                                                     |                                                |                                                           |       |
| 41   | 10 October 2016   | Not allocated (for overrun - 9 days); now: Map tile generation       | 7. Data Loader (35 days) - start liable for 2/3 weeks delays caused by more work on GET methods   | 6.3 Middleware services - other (10 days) [Potentially Kevin as well]: data viewer get methods |                                                                     |                                                |                                                           |       |
| 42   | 17 October 2016   |                                                                      |                                                                                                   |                                                                                                |                                                                     |                                                |                                                           |       |
| 43   | 24 October 2016   |                                                                      |                                                                                                   |                                                                                                |                                                                     |                                                |                                                           |       |
| 44   | 31 October 2016   | SQL Server study submission and data extraction                      |                                                                                                   | Allocated to JG                                                                                | Documentation, manual                                               |                                                |                                                           |       |
 
## Data Loader - Kevin

No work (as planned)

## Front end (webPlatform)

### Disease Mapping, Data viewer - David

- Client can produce chloropleth maps or population and rates etc. Needs data from server before all 
  parts of client can be completed

## Middleware

### Web services (rifServices) - Kevin/David

- XXX

### Run study batch - Kevin

- Completed R script integration from Java

#### R - Brandon

- No progress required.

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter

Further major progress this month with both SQL Server and Postgres integration for the geospatial services.
  
* De-duplication processing, closed polygon loops if needed. Detect duplicate area names
* ST_Union (creating multipolygons) and area calculations were done geoJSON using turf
* WKT support using Wellknown to allow interchnage of geoJSON with databases
* Id generator; gid support (especially in topojson)
* area_id and gid uniqueness tests to shapefile checks and tests added to SQL load script. Area name will need to be unqiue 
  within the confines on the next lower resolution layer  
* Auto generate Postgres and MS SQL server scripts; both work OK
* SQL Servers lacks ST_Transform() so geom_orig cannot be set (not used by RIF). No other problems with SQL Server geospatial functionality
* Confirmed areas and centroids are the same in the US SRID projection
* SQL load script generator for Postgres and SQL Server:
  * Load shapefile derived CSV files;
  * Convert well known text to geometry;
  * Fix and validate geometry data, make all polygons right handed;
  * Test Turf and DB areas agree to within 1% (3% for SQL Server);
  * Spatially index;
  * Add geography, geolevels meta data;

Work now moxes onto to completing the creating the tiles, the results download and fully integrating to the RIF. SQL Server RIF 
integration on track for mid October

## Databases

### Postgres, integration - Peter

* Tested tile maker database script to create geolevel geometry table.

### Microsoft SQL server - Margaret/Peter

* Work to start mid September on checking the SQL Server database. Margaret is available to apply the patches. Peter to build
  a SQL Server database on his laptop and make it rebuild reliably.



 

 
