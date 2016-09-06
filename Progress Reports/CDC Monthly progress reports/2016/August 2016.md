# CDC RIF 4.0 Progress Report August 2016

## Highlight

No milestones.

## Planned work for July and August

| Week | Week Starting  | PH                                                     | KG                                               | DM                                                                   | BP                                         | MD                       | Milestone        | Notes |
|------|----------------|--------------------------------------------------------|--------------------------------------------------|----------------------------------------------------------------------|--------------------------------------------|--------------------------|------------------|-------|
| 27   | 04 July 2016   | Shapefile services: 3.1 meta data (6 days)             | 2.3 Taxonomy services (4 days)                   | Data Viewer                                                          | Integrate R into Java middleware (10 days) |                          |                  |       |
| 28   | 11 July 2016   |                                                        |                                                  | Exposomics                                                           |                                            |                          |                  |       |
| 29   | 18 July 2016   |                                                        | Integrate R into Java middleware (5 days)        | GIS short course                                                     | Holiday                                    |                          |                  |       |
| 30   | 25 July 2016   |                                                        | 2.1 Middleware services planning                 | Disease Mapping - m/w services (5 days)                              |                                            |                          | Study Submission |       |
| 31   | 01 August 2016 | Shapefile services: 3.2 Geospatial outputs (8 days)    | Integrate R into Java middleware (10 days)       | Handover to new GIS person                                           | Integrate R into Java middleware (10 days) |                          |                  |       |
| 32   | 08 August 2016 |                                                        |                                                  | Data Viewer - middleware services (5 days)                           |                                            |                          |                  |       |
| 33   | 15 August 2016 | Holiday                                                | 6.3 Middleware services - create study (14 days) | Holiday                                                              | Holiday                                    |                          |                  |       |
| 34   | 22 August 2016 | Shapefile services: 3.2 Geospatial outputs II (4 days) |                                                  | 6.1 Taxonomy services, 6.2 Database logon                            |                                            |                          |                  |       |
| 35   | 29 August 2016 |                                                        |                                                  | 6.3 Middleware services - other (4 days) [Potentially Kevin as well] | Not allocated                              | Holiday to 5th September | Results Viewer   |       |

## Data Loader - Kevin

No work

## Front end (webPlatform)

### Disease Mapping - David

- XXX

### Data viewer - David

- XXX

## Middleware

### Web services (rifServices) - Kevin/David

The main work on the RIF related to enabling middleware methods that help support visualising smoothed data extract results. 
The work done on that has not just involved coding new middleware methods but also making design decisions to help expedite 
the need to make the RIF work on both PostgreSQL and SQL Server systems. The need to find a simple solution was made more 
pressing because Kevin was ill for ten days.

Currently there are a number of database procedures in PostgreSQL which, given a study id, will retrieve smoothed results. 
These procedures have been designed to retrieve parts of the result sets to facilitate showing results as pages in the front end. 
However, considering the factors of effort needed to maintain the code for these features and to maintain that code for 
both PostgreSQL and SQL Server platforms, Kevin had to re-evaluate using them.

### Run study batch - Kevin


#### R - Brandon


### Ontology support - Kevin
 
- 

### Node geospatial services (tile-maker) - Peter

Major progress this month with both SQL Server and Postgres integration for the geospatial services.


- Create shapefile derived CSV files; de-duplicated. Geometric data interchnaged in well known text format;
* XML Configuration download support;
* SQL load script generator for Postgres and SQL Server:
  * Load shapefile derived CSV files;
  * Convert well known text to geometry (geography in SQL server);
  * Fix and validate geometry data, make all polygons right handed;
  * Tested Turf and DB areas agree to within 1% (3% for SQL Server);
  * Spatially index;
  * Add geography, geolevels meta data (i.e. data required for RIF integration);

Work now moxes onto to completing the SQL load script generator, creating the tiles and the results download.

## Databases

### Postgres, integration - Peter

* Tested tile maker database script to create geolevel geometry table.

### Microsoft SQL server - Margaret/Peter

* Tested tile maker database script to create geolevel geometry table;
* Need to resolve SQL Server use of geography verses geometry (this may be the cause of the lesser accuracy) and SQL Server geometry to geography casting; 
* SQL Servers lacks ST_Transform() so the database cannot transform GPS data back to national or state grids. 
  These are not used by RIF.



 

 
