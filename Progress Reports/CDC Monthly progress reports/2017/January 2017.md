# CDC RIF 4.0 Progress Report January 2017

## Highlight

* Results viewer improving through testing; R integration fully complete.

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
| 7    | 13 February 2017  | SQL Server study submission (run study)                       | SQL Server data loader script integration                                                         | Documentation, manual                                                                          |                                                                     | Transferred to COSMOS/SCAMP                                                               | tileMaker SQL Server Integration                                       |       |
| 8    | 20 February 2017  |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 9    | 27 February 2017  |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           | Data loader SQL Server install                                         |       |
| 10   | 06 March 2017     |                                                               | SQL Server middleware testing (including run study)                                               |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 11   | 13 March 2017     | SQL Server Installer documentation                            |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 12   | 20 March 2017     | Assist with middleware                                        |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 13   | 27 March 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 14   | 03 April 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 15   | 10 April 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           | Study submission and Results viewer running on SQL server              |       |
| 16   | 17 April 2017     | Assist with SQL Server dataloader                             | SQL Server dataloader driving tables                                                              |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 17   | 24 April 2017     |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           |                                                                        |       |
| 18   | 01 May 2017       |                                                               |                                                                                                   |                                                                                                |                                                                     |                                                                                           | SQL Server RIF complete                                                |       |

## Data Loader - Kevin


## Front end (webPlatform): Disease Mapping, Data viewer - David

- Refactoring of mapping and ui-grid code
- Added middle ware method to get geography info for complete study
- Polling of study status using $interval to notify on completion of submitted job
- Modified getSmoothedResults method to not need a year
- Population pyramids plotable by year

## Middleware

### Web services (rifServices) - Kevin/David

### Run study batch - Kevin


#### R - Brandon

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter


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


 

 
