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

I’ve been doing knowledge transfer and I’ve spent time testing, fixing and enhancing the RIF Data Loader Tool with SAHSULAND CSV files. 
Initially, work on the RIF was divided by layer in a three-tier architecture style that made separation of concerns between the front-end 
development of JavaScript-based web applications, Java-based middleware and PostgreSQL-based back-end work. As the web applications reach a 
greater level of maturity and as the PostgreSQL-based code to support the back-end stabilises, the work needed to maintain and enhance the 
large codebase is changing in three important respects.

First, more effort is shifting to sharing maintenance of the Java-based middleware across more members of the team. That has required me to 
identify logical divisions in the architecture that can support parallelisation of work amongst team members in a way that maximises the 
autonomy of their work. I’ve divided the Java part of the codebase into two independent areas: code that assumes the RIF database is 
populated and is used to support the web application used by scientists and code that assumes the RIF database is empty and supports the 
Data Loader Tool that will be used by RIF managers. I’ve spent time identifying a distinct work path for two people on the team who don’t 
have a strong previous background in either Java or the architecture and coding patterns used to support the middleware. So some of the 
recent work I’ve been doing has focused on knowledge transfer to other members of the team.

Second, the architecture of the middleware has been modified to support database porting activities. I’ve gone through the rifGenericLibrary 
and the rifDataLoaderTool subpackages, and isolated parts of the code that may have to be modified to support both SQL Server and PostgreSQL back-ends. 
An important design decision has been how to set up the code so that database porting efforts can minimise my own efforts to modify data loader tool 
code that may need to be changed in response to testing it with SAHSULAND CSV files (see next item). I’ve gone through the classes and signposted code 
fragments that may warrant special attention for porting efforts. I’ve then created mirrored packaging for both both SQL Server and PostgreSQL.

Margaret, who has more extensive knowledge of differences between SQL Server and PostgreSQL, should now be able to modify and test code in the SQL Server 
packages without much affecting modifications I may have to make in testing and fixing the Data Loader Tool. With respect to code used to support the web 
applications, I’m preparing to hand over code maintenance of middleware methods to Dave, who has been spending time making a great set of interactive web 
forms. Rather than trying to create new middleware methods myself to support web app features, I’m in the process of making knowledge transfer materials 
that can make it easier for him to make these changes himself.

Third, the Data Loader Tool is now being tested, fixed and enhanced as we try to use it to populate an empty version of the RIF database with a set of 
CSV files that contain all the data for SAHSULAND. Currently, these files are being used as part of Peter’s database build script. However, we need to 
test that the tool is fit for purpose by creating the same SAHSULAND data set as if we’re loading it from scratch.

This activity has revealed some important enhancements that will need to be made to the Data Loader Tool. The tool’s feature for loading geospatial data 
will be simplified so that it imports metadata about geographies from a metadata file produced by Peter’s shapefile simplification service. 
RIF managers will now first process geospatial data sets using his service, and the geography and geographical resolutions it identifies will be 
identified so that the metadata can tell the Data Loader Tool what geographical resolutions can be associated with data set configuration options.

Up until now, the Data Loader Tool has been designed to process any one CSV file independently of any CSV file that comes before or after it in processing. 
However, the needs for numerators to be linked to a corresponding denominator, and to link notions of geography and health theme for these kinds of data 
sets – now require us to impose an order of creating the health-related data sets. The tool will now enforce an ordering of specifying records in the 
following order: health themes, denominators, numerators and covariates.

Providing explicit support for these mechanisms requires that the DL need to check for and serialise dependencies. For example, suppose a RIF manager 
creates a denominator and then a numerator that refers to it. The tool would need to prevent the RIF manager from removing the denominator when other data 
sets refer to it. Because the Data Loader Tool stores configuration details of data sets in an XML file, we need to modify the way serialisation is done 
so that dependencies can be reconstituted when configuration files are loaded.

Prescribing explicit support for dependencies also helps answer an important design issue: should the tasks of loading the RIF database be spread out in 
multiple configuration files or one? For example, one configuration file could load files for a specific set of years or for only numerator tables. By 
requiring more explicit links between descriptive parts of the data loading activity, it is most convenient that all data sets used for the RIF database 
are specified in a single configuration file where the integrity of link references can be most easily managed.

Supporting dependency management will require enhancements to multiple parts of the Data Loader Tool, and add to feature enhancements that are not related 
to the more pressing task of porting the SQL fragments that are created by the middleware.

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

* Enhancement to populate the posterior probability field after performing the smoothing. Writing to the database

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

* Work stsrts with the front/end and middleware in December.



 

 
