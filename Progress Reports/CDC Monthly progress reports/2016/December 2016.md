# CDC RIF 4.0 Progress Report December 2016

## Highlight

* Good progress on dataload and geospatial tilemaker.

## December Summary

| Week | Week Starting     | PH                                                                       | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                             | Milestone                                                 | Notes |
|------|-------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|------------------------------------------------|-----------------------------------------------------------|-------|
| 49   | 05 December 2016  | SQL Server tile integration                                              | Data Loader                                                                                       | Remaining data display methods, Java                                                           | Other SAHSU                                                         | SQL Server middleware                          |                                                           |       |
| 50   | 12 December 2016  | SQL Server tile integration                                              | Data Loader                                                                                       | Remaining data display methods, Java                                                           | Other SAHSU                                                         | Other SAHSU                                    |                                                           |       |
| 51   | 19 December 2016  | Holiday                                                                  | Holiday                                                                                           | Holiday                                                                                        | Holiday                                                             | Holiday                                        |                                                           |       |
| 52   | 26 December 2016  | Christmas                                                                | Christmas                                                                                         | Christmas                                                                                      | Christmas                                                           | Christmas                                      |                                                           |       |

## Planned work for January through February

### January Plans

Work plan until January 6th agreed 5/12/2016:

1. Kevin:  Postgres data loader as per document with agreed restrictions. Assist Margaret and David as required. In the new year I will aim to be around for 3-4 days to help you get to SQL Server (i.e. don’t panic). I could do with copies of your database function code so I can think about and pre port them beforehand. I appreciate that help for Margaret and David will impacts on the work you can do but both David and Margret  must complete both tasks so they are not running over into January;
2. Brandon: Regression test posterior probability fix with David;
3. Margaret: Achieve middleware database logon on SQL server using front end; understand cause of first non-logon related error. If possible get further, but I  have to be realistic here;
4. David: Implement three remaining middleware methods (year periods, study geography – data from RIF40_STUDIES);
5. Peter: database fixes (known bugs, sahsuland_empty), database preparation work for geospatial data loader. Geospatial integration scripts for SQL server (and possibly Postgres);

Expected highlights this month:

• Implement three remaining middleware methods 
• Achieve middleware database logon on SQL server using front end
• Geospatial integration scripts for SQL server

| Week | Week Starting     | PH                                                            | KG                                                                                                | DM                                                                                             | BP                                                                  | MD                                                                                        | Milestone                                                              | Notes |
|------|-------------------|---------------------------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|-------------------------------------------------------------------------------------------|------------------------------------------------------------------------|-------|
| 1    | 02 January 2017   | Postgres and SQL Server tile integration and testing          | 7. Data Loader (35 days) - start liable for 2/3 weeks delays caused by more work on GET methods   | Documentation, manual                                                                          |                                                                     | Assist Kevin: SQL Server porting issues - middleware query formatter, data loader scripts |                                                                        |       |
| 2    | 09 January 2017   |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 3    | 16 January 2017   |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 4    | 23 January 2017   |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 5    | 30 January 2017   |                                                               | Holiday                                                                                           |                                                                                                |                                                                     |                                                                                           | Postgres Data Loader, Postgres install; tileMaker Postgres intrgration |       |
| 6    | 06 February 2017  | CDC Visit                                                     |                                                                                                   |                                                                                                |                                                                     |                                                                                           | Postgres RIF complete                                                  |       |
| 7    | 13 February 2017  | SQL Server study submission (run study)                       | SQL Server data loader script integration                                                         | Documentation, manual                                                                          |                                                                     | Transferred to COSMOS/SCAMP                                                               | tileMaker SQL Server Integration                                       |       |
| 8    | 20 February 2017  |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 9    | 27 February 2017  |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           | Data loader SQL Server install                                         |       |
 
## Data Loader - Kevin

The data loader tool has undergone a major rework to enforce a more controlled order of specifying configuration aspects that 
supports one aspect depending on another.  Initially, the DL tool required that you processed your shape files before defining
any of the population health data sets.  This was because the naming of table columns in the numerator, denominator and covariate
data sets depended on geographical resolution levels whose definitions were buried inside of shape files. Now we need to support
additional dependencies.  A numerator data set now must specify a corresponding denominator data set, which therefore requires
that the denominators have already been defined.  Numerators also depend on a health theme, which must also be defined before hand.
All denominator, numerator and covariate data sets must also refer to a Geography.  All of these dependencies are required to be
maintained in order to properly register data sets in the RIF production database.  

Supporting these dependencies has made some aspects of processing less generic and more complicated. For example, we now have to 
consider what happens if we define a denominator, then a numerator, then delete the denominator on which it depends.  The integrity
of links must now be checked and the tool needs to be able to serialise and deserialise the links in XML files.

Supporting new dependencies has shown how the architecture responds to change.  First, important business classes such as
the DataSetConfiguration class need to have new fields added to express dependencies.  This causes a knock-on in their methods for cloning objects, and the hasIdenticalContents(...) feature which can tell if two objects have the same contents.  We clone 
objects to promote safe data entry where changes are made on a copy of a record before they are committed back to the original.  Comparing original and modified versions also supports aspects of change management in the system.

When the data model classes change (eg: DataSetConfiguration), there is a ripple effect on classes that read and write the data
to XML files.  Finally the changes have to be supported through more guided data entry features that prevent links being 
compromised.

The parts of the application that reflect these changes still need a few more minor features added and testing.  Significant work 
remains on creating scripts that can fully load denominators, numerators and covariates into the RIF.  We also need to think about 
how the Data loading process become aware of data sets that have already been loaded, and those that have been updated enough to 
warrant reloading.  For now we are assuming everything is loaded once, in a single configuration file.  Modifications to areas
of configuration are now updated with a time stamp in order to facilitate a solution that can compare differences between the
state of the data loader tool configuration file and the state of the production database.

## Front end (webPlatform): Disease Mapping, Data viewer - David

- Marquee style progress button for long login process
- Added initial methods to import a AOI shapefile for disease mapping
- Posterior Probability graph in disease mapper
- Export D3 graphs to png buttons (not yet for IE11)
- Atlas colour scheme options for choropleth mapping
- Study status modal linked to database

## Middleware

### Web services (rifServices) - Kevin/David

* Now down to one service with SAHSUland hard coded!

### Run study batch - Kevin

- No progress required.

#### R - Brandon
 
- No progress required.

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter

* Data loading integration;
* Removal of hard coded table names
* Created logger object, got winston to work properly; added file/line/function, overall timing
* Refactor dbLoad.js for production load script
* Map tile generator; RIF integration preparation
* dbLoad.js production load script: Postgres
    * DELETE/INSERT rif40_geographies/geolevels
	* Add tile table to geolevels;
	
## Databases

### Postgres, integration - Peter

* Empty database for dataloader (sahsuland_empty);
* Data loading integration;
* Rationalize geoDataLoader.xml with parameter and data loader sections, remove duplicates, support in front end
* dbLoad.js production load script: SQL Server - set schema in production script to cope with no ability 
  to change the schema on a per session basis

### Microsoft SQL server - Margaret

* No progress on: Achieve middleware database logon on SQL server using front end; understand cause of first non-logon related error. 
  If possible get further, but I  have to be realistic here; Work transferred to Peter from March;
  
* Considerable progress on tileMaker scripts for SQL Server [Peter]
  
## Documentation [18th March target]

### User manual - Brandon

* Started work on documentation

### Data Loading - Kevin

### Data Loading (Geospatial) - Peter

* No progress (March 2017)

### SQL Server Install - Peter

* No progress (March 2017)

 

 
