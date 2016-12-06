# CDC RIF 4.0 Progress Report November 2016

## Highlight

* Results viewer improving through testing; R integration fully complete.

## November Summary

| Week | Week Starting     | PH                                                                       | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                             | Milestone                                                 | Notes |
|------|-------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|------------------------------------------------|-----------------------------------------------------------|-------|
| 45   | 07 November 2016  |                                                                          | Java handover, fix submission feature                                                             | Disease Mapping                                                                                |                                                                     |                                                |                                                           |       |
| 46   | 14 November 2016  |                                                                          | Holiday                                                                                           |                                                                                                |                                                                     | Java handover                                  |                                                           |       |
| 47   | 21 November 2016  |                                                                          | Java handover, fix submission failures                                                            |                                                                                                |                                                                     | SQL Server middleware                          | Results Viewer                                            |       |
| 48   | 28 November 2016  | SQL Server tile integration                                              | 7. Data Loader (35 days) - start liable for 2/3 weeks delays caused by more work on GET methods   | Allocated to JG                                                                                |                                                                     |                                                |                                                           |       |

## Planned work for December through February

### December Plans

Work plan until January 6th agreed 5/12/2016:

a)	Kevin:  Postgres data loader as per document with agreed restrictions. Assist Margaret and David as required. In the new year I will aim to be around for 3-4 days to help you get to SQL Server (i.e. don’t panic). I could do with copies of your database function code so I can think about and pre port them beforehand. I appreciate that help for Margaret and David will impacts on the work you can do but both David and Margret  must complete both tasks so they are not running over into January;
b)	Brandon: Regression test posterior probability fix with David;
c)	Margaret: Achieve middleware database logon on SQL server using front end; understand cause of first non-logon related error. If possible get further, but I  have to be realistic here;
d)	David: Implement three remaining middleware methods (year periods, study geography – data from RIF40_STUDIES);
e)	Peter: database fixes (known bugs, sahsuland_empty), database preparation work for geospatial data loader. Geospatial integration scripts for SQL server (and possibly Postgres);

Expected highlights this month:

•	Implement three remaining middleware methods 
•	Achieve middleware database logon on SQL server using front end
•	Geospatial integration scripts for SQL server

| Week | Week Starting     | PH                                                                       | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                             | Milestone                                                 | Notes |
|------|-------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|------------------------------------------------|-----------------------------------------------------------|-------|
| 49   | 05 December 2016  |                                                                          |                                                                                                   | Reamaining data display methods, Java                                                          |                                                                     |                                                |                                                           |       |
| 50   | 12 December 2016  |                                                                          |                                                                                                   |                                                                                                |                                                                     |                                                |                                                           |       |
| 51   | 19 December 2016  | Holiday                                                                  | Holiday                                                                                           | Holiday                                                                                        | Holiday                                                             | Holiday                                        |                                                           |       |
| 52   | 26 December 2016  | Christmas                                                                | Christmas                                                                                         | Christmas                                                                                      | Christmas                                                           | Christmas                                      |                                                           |       |
| 1    | 02 January 2017   | SQL Server study submission and data extraction; installer documentation | 7. Data Loader (35 days) - start liable for 2/3 weeks delays caused by more work on GET methods   | Documentation, manual                                                                          |                                                                     | SQL Server middleware                          |                                                           |       |
| 2    | 09 January 2017   |                                                                          |                                                                                                   |                                                                                                |                                                                     |                                                |                                                           |       |
| 3    | 16 January 2017   |                                                                          |                                                                                                   |                                                                                                |                                                                     |                                                |                                                           |       |
| 4    | 23 January 2017   |                                                                          |                                                                                                   |                                                                                                |                                                                     |                                                | Data Loader                                               |       |
| 5    | 30 January 2017   |                                                                          | Holiday                                                                                           |                                                                                                |                                                                     |                                                | Study submission and Results viewer running on SQL server |       |
| 6    | 06 February 2017  | CDC Visit                                                                | CDC Visit                                                                                         | CDC Visit                                                                                      | CDC Visit                                                           | CDC Visit                                      |                                                           |       |
| 7    | 13 February 2017  |                                                                          |                                                                                                   |                                                                                                |                                                                     |                                                |                                                           |       |                                                                                                | Allocated to JG                                                                                | Documentation, manual                                               |                                                |                                                           |       |
 
## Data Loader - Kevin

*

## Front end (webPlatform)

### Disease Mapping, Data viewer - David

- New disease mapper with two 'Atlas' style maps
- Update from leaflet 0.7 to 1.0
- Export map to png feature
- Export as CSV option in results viewer
- Started looking at RIF Java classes

## Middleware

### Web services (rifServices) - Kevin/David

### Run study batch - Kevin

Created middleware methods to support the data viewer tool. Trying to fix bugs related to the overall process of submitting 
a study, having it register in the database, having it successfully smoothed by the R program, and advertising that it has been 
done to the data viewer. All of this is working, except for two problems that we expected would come up during test. First, the 
run_study method in the database has not been well tested with various study submission test data. Second, we have experienced some 
integration issues in coordinating the activities of the R-based smoothing code with the extract and map tables that are created 
the process of running the study.

#### R - Brandon

* Added support for Posterior probbility

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter

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
* Map tile generator prototype; topoJSON tiles and SVG tile creation
* Map tile generator: connect to SQL server
* XML configuration file support
* RIF team meeting, project planning, SQL server tile maker porting
* SQL server tile maker bulk insert
* Comiment geielvel data tables
* Add support for description on DBF fields to XML config
* Added logging to tile maker

## Databases

### Postgres, integration - Peter

* Fixed extract bug

### Microsoft SQL server - Margaret/Peter

* 



 

 
