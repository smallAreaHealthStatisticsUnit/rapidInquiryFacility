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

Kevin:  Postgres data loader as per document with agreed restrictions (I would like a revised copy so I can safely remove all the junk). Assist Margaret and David as required. In the new year I will aim to be around for 3-4 days to help you get to SQL Server (i.e. don’t panic). I could do with copies of your database function code so I can think about and pre port them beforehand. I appreciate that how much you have to help Margaret and David impacts on the work you can do but they must complete both tasks;
Brandon: Regression test posterior probability fix with David;
Margaret: Achieve middleware database logon on SQL server using front end; understand cause of first non-logon related error. If possible get further, but I  have to be realistic here;
David: Implement three remaining middleware methods (year periods, study geography – data from RIF40_STUDIES);
Peter: database fixes (known bugs, sahsuland_empty), database preparation work for geospatial data loader. Integration scripts for SQL server (and possibly Postgres);


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

- Added support for Posterior probbility

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter


## Databases

### Postgres, integration - Peter

* 

### Microsoft SQL server - Margaret/Peter

* Tested the tile maker generated database script to create geolevel geometry, intersection and tiles tables.



 

 
