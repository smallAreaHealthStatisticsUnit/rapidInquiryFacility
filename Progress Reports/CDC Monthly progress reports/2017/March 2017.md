# CDC RIF 4.0 Progress Report March 2017

## Highlights

* Data loader integration and testing on Postgres and SQL Server complete. 
* SQL Server database build and installation instructions complete.

## March Summary

**Week**|**Week Starting**|**PH**|**KG**|**DM**|**BP**|**MD**|**Milestone**|**Notes**
:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:
 | | | | | | | | 
10|06 March 2017|SQL Server data loader script integration|SQL Server data loader script integration|US SEER (Cancer) example data| | |wpea-traffic/wpea-darwin resdy for install testing| 
11|13 March 2017|Data loader and tileMaker inegration and tesing|Middleware build|Code refactoring, testing|US SEER (Cancer) example data| | | 
12|20 March 2017|Data loader and tileMaker inegration and tesing|Leaves for Cabinet Office|Data loader and middleware handover|Data loader and middleware handover| | | 
13|27 March 2017|SQL Server study submission (run study)| |Data loader and middleware handover|Data loader and middleware handover| | | 

## Planned work for April through June

### April Plans

Work plan for April to be agreed 5/4/2017:

1. Brandon: Documentation with David; test datasets: US SEER cancer data 
2. David: re-implement one tile get middleware method; code refactor; documentation,
   risk analysis.
3. Peter: SQL Server , run study (early March);

Expected highlights this month:

1. Brandon: Documentation with David; test datasets: US SEER cancer data 
2. David: re-implement one tile get middleware method; code refactor; documentation,
   risk analysis.
3. Peter: SQL Server database build and installation instructions.
4. Kevin: Data loader complete and integration tested.

**Week**|**Week Starting**|**PH**|**DM**|**BP**|**Milestone**|**Notes**
:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:
 | | | | | | 
14|03 April 2017|SQL Server study submission (run study)|SQL Server middleware|SQL Server middleware|CDC sandbox ready| 
15|10 April 2017|SQL Server study submission (run study)|SQL Server middleware|SQL Server middleware|Study submission running on SQL server| 
16|17 April 2017|Assist with middleware (database fixes); SQL Server full install testing|SQL Server middleware|SQL Server middleware| | 
17|24 April 2017|Assist with middleware (database fixes); SQL Server full install testing|SQL Server middleware|SQL Server middleware|SQL Server RIF complete| 
18|01 May 2017|CDC Install|CDC Install| |CDC Install| 
19|08 May 2017|Background tile download (cache support)|Risk analysis statistical requirements|Risk analysis statistical requirements| | 
20|15 May 2017|Background tile download (cache support)|Risk analysis statistical requirements|Risk analysis statistical requirements| | 
21|22 May 2017|UK COA 2011 tiles|Risk analysis|Risk analysis statistical requirements| | 
22|29 May 2017|UK COA 2011 tiles|Risk analysis|Not allocated| | 
23|05 June 2017|UK COA 2011 tiles|Risk analysis|Not allocated| | 
24|12 June 2017|Not yet allocated (fault fixing)|Risk analysis R scripts|Risk analysis R scripts| | 
25|19 June 2017|UK COA 2011 tiles|Risk analysis| | | 
26|26 June 2017|UK COA 2011 tiles|Risk analysis| | | 
27|03 July 2017|Holiday|Holiday|SAHSU RIF data statistical testing| | 
28|10 July 2017|Holiday|Holiday|SAHSU RIF data statistical testing| | 
29|17 July 2017|Holiday|Risk analysis integration|Risk analysis integration| | 
30|24 July 2017|Holiday|Risk analysis integration|Risk analysis integration| | 
31|31 July 2017|Holiday|Risk analysis integration|Risk analysis integration| | 
32|07 August 2017| | | |Risk analysis| 
33|14 August 2017|SAHSU RIF install|SAHSU RIF install|SAHSU RIF install| | 
34|21 August 2017|SAHSU RIF data load| |SAHSU RIF data load| | 
35|28 August 2017|SAHSU RIF data load| |SAHSU RIF data load|SAHSU RIF operational| 

## Data Loader - Kevin

Transfer development to Brandon. Integration testing with Peter.

## Front end (webPlatform): Disease Mapping, Data viewer - David

- getTileMakerTiles middle ware method finalised for new DB schema
- Fixing various rifService bugs with KG
- Risk analysis methods for selecting areas from shapefiles started
- Removal of old middleware methods
- Migrate all CDN libraries to standalone scripts
- Started study information recall method

## Middleware

### Web services (rifServices) - Kevin/David

* Resolved Maven build issue. Now builds cleanly from github source.

### Run study batch - Kevin

- No progress required.

#### R - Brandon

- Minor changes & documentation

### Ontology support - Kevin
 
- No progress required.

### Node geospatial services (tile-maker) - Peter

* Testing of caching. This is to allow the RIF40 to work unconnected to the Internet (e.g. on a Laptop; the SAHSU 
  secure private network).

#### 30th January to 3rd February 

* TileViewer (tile-viewer.html) web screen by DB/geography; DB web service. Currently slow because using geoJSON.
  Both SQL server and Postgres tiles display for both SAHSULAND and USA to county level.
* Note resizing bug in tile-maker.html is probably caused by setting the height of the map div in html once 
  leaflet is initialized.
* All tiles confirmed OK

## Databases

### Postgres - Peter

* Fixed tilemaker script issues with studies existing; default study/comparison area checks; fixed issue with comparison area 
  extract.
* Added TESTUSER support to Postgres build system (i.e. can define the testing user).
* Integrate geosptial and data loader data into sahsuland_dev, sahsuland
* Fix to *getAdjacencyMatrix()* funcion change ST_Touches() to ST_Intersects() to fix missing adjacencies caused by small slivers.
  Impact on sahsuland/USA to county level geographies:
  * Level 2:
  
	```  
	 areaid | areaname | num_adjacencies | extra_intersects |              adjacency_list
	--------+----------+-----------------+------------------+-------------------------------------------
	 01.004 |          |               6 |                2 | 01.001,01.002,01.003,01.005,01.008,01.009
	 01.005 |          |               4 |                1 | 01.001,01.004,01.006,01.009
	 01.008 |          |               5 |                1 | 01.003,01.004,01.007,01.009,01.011
	```
	
  * Level 3:
  
	```
	(3 rows)
		areaid     |   areaname    | num_adjacencies | extra_intersects |                                                                                    adjacency_list
	---------------+---------------+-----------------+------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 01.007.012500 | 01.007.012500 |               5 |                1 | 01.007.012300,01.007.012400,01.013.016100,01.013.016200,01.015.016200
	 01.015.016200 | 01.015.016200 |              13 |                1 | 01.007.012500,01.013.016200,01.013.016800,01.014.018000,01.014.018100,01.014.018300,01.014.018500,01.014.018600,01.015.016900,01.016.017000,01.017.019000,01.018.019100,01.018.019500
	(2 rows)
	```
	
### Microsoft SQL server - Peter

* TileMaker integration complete.
* Triggers fully tested and fixed where necessary.
* Create and delete a study (i.e. fixed all the triggers used in study creation so they now work)
* Run study started.

## Documentation [18th March target]

### User manual - Brandon

* No progress.

### Data Loading - Kevin

- Data loader integration and testing. Issues resolved:
  * Source input data was wrong
  * Primary keys added
  * Age sex group derivation fixed
  
### Example Data - Brandon

- Downloaded and prepared SEER data for the data loader.
- Ran SEER data through data loader

### Geospatial data (Peter)

- Tested Swedish Halland county data; resolved UTM multiple projection issues, investigated UTF-8 issues (UTF-8 chacracters being 
  corrupted somewhere in the database layer)
- Tested USA county data to level 11. Same UTF-8 issues in Puerto Rico; Postgres: OK; SQL Server: requires licensed version (Express
  used in development limited to 10GB) 
- Data loader integration and testing. 
  
### Data Loading (Geospatial) - Peter

* No progress (March 2017)

### SQL Server Install - Peter

* SQL Server database builder complete and rolled out; Installer (from export) to follow (and Postgres version). 
  Requires adminstrator or Power User privilege. Creratred and tested installation instructions: (Building the RIF SQL Server database from Github) 
  [https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/installation/README.md]  Upgraded Postgres
  to same standard.
