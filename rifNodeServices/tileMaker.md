Tile Maker
==========

# Contents

- [1. Overview](#1-overview)
  - [1.1 Software Requirements](#11-Software Requirements)
  - [1.2 Issues](#12-issues)
    - [1.2.1 Memory Requirements](#121-memory-requirements)
    - [1.2.2 SQL Server Connection Error](#122-sql-server-connection-error)
- [2. Running the Tile Maker](#2-running-the-tile-maker)
  - [2.1 Setup](#21-setup)
  - [2.2 Processing Overview](#22-processing-overview)
  - [2.3 Running the Front End](#23-running-the front-end)
    - [2.3.1 Shapefile Format](#231-shapefile -ormat)
    - [2.3.2 Processing Huge Shapefiles](#232-processing-huge-shapefiles)
  - [2.4 Post Front End Processing](#24-post front-end-processing)
    - [2.4.1 Geospatial Data Load](#241-geospatial-data-load)
    - [2.4.2 Tile Manufacture](#242-tile-manufacture)
    - [2.4.3 Load Production Data into the RIF](#243-load-production-data-into-the-rif)
    - [2.4.4 Example of Post Front End Processing](#244-example-of-post-front-end-processing)

# 1. Overview

This document details the loading of the administrative geography data and the geo-coding requirements of the RIF. 

## 1.1 Software Requirements

## 1.2 Issues

### 1.2.1 Memory Requirements
### 1.2.2 SQL Server Connection Error

Symptom; SQL Severer connect error ```Error: None of the binaries loaded successfully. Is your node version either >= 0.12.7 or >= 4.2.x or >= 5.1.1 or >= 6.1.0```

* SQL server connect error caused by a version mismatch between the Node.js packages *mssql* and *msnodesqlv8*; 
  ```
  C:\Users\phamb\OneDrive\SEER Data\Tile maker USA>node C:\Users\%USERNAME%\Documents\GitHub\rapidInquiryFacility\rifNodeServices\mssqlTileMaker.js -U peter --password retep
	Created info log file: mssqlTileMaker.log
	About to connected to SQL server using: {
		"driver": "msnodesqlv8",
		"server": "localhost",
		"options": {
			"trustedConnection": false,
			"useUTC": true,
			"appName": "mssqlTileMaker.js"
		},
		"user": "peter",
		"password": "XXXXXXXXXXXXXXXXX"
	}
	error [mssqlTileMaker:212:mssql_db_connect()] Could not connect to SQL server client using: {
		"driver": "msnodesqlv8",
		"server": "localhost",
		"options": {
			"trustedConnection": false,
			"useUTC": true,
			"appName": "mssqlTileMaker.js"
		},
		"user": "peter",
		"password": "retep",
		"port": 1433,
		"stream": false,
		"parseJSON": false
	}
  Error: None of the binaries loaded successfully. Is your node version either >= 0.12.7 or >= 4.2.x or >= 5.1.1 or >= 6.1.0
  ```
   
* Check you have [Visual Studio 2015/2017](https://www.visualstudio.com/downloads/) installed; launch it and logon to the Microsoft DEveloper network so the developer license is active;
* Remove *mssql* and *msnodesqlv8* from *package.json*;
* re-run: ```npm install msnodesqlv8 --save```:
  ```
  C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install msnodesqlv8 --save
  npm WARN optional SKIPPING OPTIONAL DEPENDENCY: fsevents@1.2.4 (node_modules\fsevents):
  npm WARN notsup SKIPPING OPTIONAL DEPENDENCY: Unsupported platform for fsevents@1.2.4: wanted {"os":"darwin","arch":"any"} (current: {"os":"win32","arch":"x64"})

  + msnodesqlv8@0.1.46
  updated 1 package in 10.388s   
  ```
* re-run: ```npm install mssql --save```:
  ```
  C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices>npm install mssql --save
  npm WARN optional SKIPPING OPTIONAL DEPENDENCY: fsevents@1.2.4 (node_modules\fsevents):
  npm WARN notsup SKIPPING OPTIONAL DEPENDENCY: Unsupported platform for fsevents@1.2.4: wanted {"os":"darwin","arch":"any"} (current: {"os":"win32","arch":"x64"})

  + mssql@4.1.0
  added 12 packages in 11.196s
  ```
   
# 2. Running the Tile Maker

## 2.1 Setup
## 2.2 Processing Overview
## 2.3 Running the Front End
### 2.3.1 Shapefile Format
### 2.3.2 Processing Huge Shapefiles
## 2.4 Post Front End Processing
### 2.4.1 Geospatial Data Load

Load data into non RIF40 Schema.

### 2.4.2 Tile Manufacture

### 2.4.3 Load Production Data into the RIF

Load data into RIF40 Schema.

### 2.4.4 Example of Post Front End Processing

* Loads tilemaker output into both *sahsuland* and *sahsuland_dev*;
* Processes *sahsuland_dev* schema to tiles;
* Loads production data into *rif40* schema on  both *sahsuland* and *sahsuland_dev*.

The SEER pre-processing script *pg_load_seer_covariates.sql* has a dependency on the *cb_2014_us_nation_5m*, *cb_2014_us_state_500k* and *cb_2014_us_county_500l* that are part of the 
tilemaker pre-processing. The FIPS code is required to make the join and this field is not in the standard lookup tables. For this reason it is necessary to build 
the covariates table on *sahsuland_dev*. In the longer term the FIPS codes should be added to the lookup tables. 

```
cd C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifNodeServices
make
C:\Users\phamb\OneDrive\SEER Data\Tile maker USA
psql -d sahsuland_dev -w -e -f pg_USA_2014.sql
psql -d sahsuland -w -e -f pg_USA_2014.sql
sqlcmd -U peter -P retep -d sahsuland_dev -b -m-1 -e -r1 -i mssql_USA_2014.sql  -v pwd="%cd%"
sqlcmd -U peter -P retep -d sahsuland -b -m-1 -e -r1 -i mssql_USA_2014.sql  -v pwd="%cd%"
node C:\Users\%USERNAME%\Documents\GitHub\rapidInquiryFacility\rifNodeServices\pgTileMaker.js --database sahsuland_dev
node C:\Users\%USERNAME%\Documents\GitHub\rapidInquiryFacility\rifNodeServices\mssqlTileMaker.js -U peter --password peter --database test
psql -U rif40 -d sahsuland_dev -w -e -f rif_pg_usa_2014.sql
psql -U rif40 -d sahsuland -w -e -f rif_pg_usa_2014.sql
sqlcmd -U rif40 -P rif40 -d sahsuland -b -m-1 -e -r1 -i rif_mssql_usa_2014.sql -v pwd="%cd%"
sqlcmd -U rif40 -P rif40 -d sahsuland_dev -b -m-1 -e -r1 -i rif_mssql_usa_2014.sql -v pwd="%cd%"
```