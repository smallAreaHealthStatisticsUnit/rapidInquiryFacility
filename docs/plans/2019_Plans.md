---
layout: default
title: 2019 Plans
---

1. Contents
{:toc}

# Introduction

RIF Priorities for the first quarter of 2019 are:

1. Make the RIF Usable within SAHSU [PH; in priority order];
   * [Risk analysis specific D3 graphs](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/127);
   * [Multiple (used in statistical calculations) and additional (for use outside of the RIF) covariate support](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/124). One primary 
      covariate, multiple additional covariates, No support for multiple covariates in the calculation or results 
     (this reduce resource risk). Multiple additional covariates available in the extract;
   * [Pooled or individual analysis for multiple risk analysis points/shapes (e.g COMARE postcodes)](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/129).
     Currently the RIF analyses there in N bands with all the sites as on. It is proposed to extend the RIF to support:
     * Individual site analysis;
     * Pooled analysis (1 or more groups of sites). Groups would be defined from a categorisation variable in the shapefile. Would require 
	   changes to:
       * Shapefile load screen and controller;
       * JSON study definition format;  
       * Study extract and result tables;
	   * R risk analysis code.
   * [Oracle interconnect](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/126). IF access to remove 
     numerator data in Oracle.
 
  
2. Improve the installation process [MM]

3. One high priority bug. [Issue #128 SQL Server SAHSU Database not linked to geography](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/128). 
   SQL Server SAHSU Database not linked to geography. This is a column length issue (i.e. there is a spurious space or two). Postgres is 
   fully functional [PH];

For the rest of 2019 the focus is currently expected to be on:

* Full multiple covariate support;
* Information governance;
* Data loading;
* Logging and auditing;
* Data extract improvements;
* Cluster analysis;
* R scalability.
   
This is likely to change to adapt to funder requirements.
   
See the [RIF Roadmap](https://trello.com/b/CTTtyxJR/the-rif-roadmap) on Trello or as a graphic: 
![2019 RIF Roadmap]({{ site.baseurl }}/plans/2019_RIF_Roadmap.png)

# Enhancements

## Now (Q1 2019)
  
### Make the RIF Usable within SAHSU

These are a priority for end of February 2019, in priority order:

* [Issue #127 risk analysis D3 maps](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/127). These will 
  replace the posterior probability J curve and the frequency count the FP defined D3 charts displaying the homogeneity data.

  This will be based on the visible displays in the RIF 3.2, for point source exposures (minimum displayed dataset to include 
  observed counts, expected counts, relative risk, trend test for each site, and adjusted by region with heterogeneity testing and 
  meta-analysis function. The content and layout should be discussed with FP:
  * Covariate loss report. [This will be added to study summary report (the (i) button in the data viewer. Study summary is currently 
    broken:
    ![Covariate data loss RIF 3.2 popups]({{ site.baseurl }}/plans/Covariate data loss RIF 3.2 popups.png)
  * RIF 3.2 risk analysis results [replacement for the frequency count]:
    ![RIF 3.2 risk analysis results]({{ site.baseurl }}/plans/RIF 3.2 risk analysis results.png)
  * RIF 3.2 risk analysis graph [replacement for the posterior probability J curve]:
    ![RIF 3.2 risk analysis graph]({{ site.baseurl }}/plans/RIF 3.2 risk analysis graph.png).
	
  Study summary error appears to be a porting fault and is trivial to fix (rif40 schema is missing):
  ```
  11:19:09.860 [http-nio-8080-exec-6] ERROR org.sahsu.rif.generic.util.CommonLogger : [org.sahsu.rif.services.datastorage.common.SmoothedResultManager]:
  SmoothedResultManager.getHealthCodesForProcessedStudy error
  getMessage:          SQLServerException: Invalid object name 'rif40_inv_conditions'.
  getRootCauseMessage: SQLServerException: Invalid object name 'rif40_inv_conditions'.
  getThrowableCount:   1
  getRootCauseStackTrace >>>
  com.microsoft.sqlserver.jdbc.SQLServerException: Invalid object name 'rif40_inv_conditions'.
  	at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDatabaseError(SQLServerException.java:259)
  	at com.microsoft.sqlserver.jdbc.SQLServerStatement.getNextResult(SQLServerStatement.java:1547)
  	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.doExecutePreparedStatement(SQLServerPreparedStatement.java:548)
  	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement$PrepStmtExecCmd.doExecute(SQLServerPreparedStatement.java:479)
  	at com.microsoft.sqlserver.jdbc.TDSCommand.execute(IOBuffer.java:7344)
  	at com.microsoft.sqlserver.jdbc.SQLServerConnection.executeCommand(SQLServerConnection.java:2713)
  	at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeCommand(SQLServerStatement.java:224)
  	at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeStatement(SQLServerStatement.java:204)
  	at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.executeQuery(SQLServerPreparedStatement.java:401)
  	at org.sahsu.rif.services.datastorage.common.SmoothedResultManager.getHealthCodesForProcessedStudy(SmoothedResultManager.java:387)
  	at org.sahsu.rif.services.datastorage.common.StudyRetrievalService.getHealthCodesForProcessedStudy(StudyRetrievalService.java:834)
  	at org.sahsu.rif.services.rest.StudyResultRetrievalServiceResource.getHealthCodesForProcessedStudy(StudyResultRetrievalServiceResource.java:697)
  	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)`
  ```
  Tasks:

  * Modify front end to support new D3 graphs and the covariate loss report;
  * Create a new REST middleware service *getHomogeneity* to return the rif40_homegeneity data for the study with parameters: username 
    and studyID. I to 3 JSON records expected containing:
    * genders
    * homogeneity_dof
    * homogeneity_chi2
    * homogeneity_p
    * linearity_chi2
    * linearity_p
    * explt5;
  * Fix study summary report;
  * Create a new REST middleware service *getCovariateLossReport* to return the covariate loss report; this will *LEFT OUTER JOIN* the 
    numerator and covariate tables, filter by the study and comparison areas respectively, and filter the covariates the max and min 
	ranges defined for the covariate to produce:
	* Study or Comparision areas (S or C);
	* Covariate name;
	* Number of areas at mapping (covariate table) geolevel;
	* Number of areas that join the numerator to the study or Comparision area for the study defined year and age sex group range;
	* Number of areas that join the covaite to the study or Comparision area for defined coviate max/min limits;
  REST parameters: username and studyID, returns two x number of covariates records for a risk analysis study using covariates; 
  otherwise an error; 	
* [Issue #124 Multiple covariates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/124) (first part for 
  March 2019). This will be implemented this in two stages:
  * One primary covariate, multiple additional covariates, No support for multiple covariates in the calculation or results 
    (this reduce resource risk). Multiple additional covariates available in the extract.
  * Full multiple covariate support;
  
  Tasks, stage one (one primary covariate, multiple additional covariates):
  * Create table/view pair *t_rif40_inv_additional_covariates/rif40_inv_additional_covariates*;
  * Add support for additional covariates to the investigations screen and to the JSON study defintion in the front end;
  * Middleware support for additional covariates as a) objects, b) JSON study defintion c) database and d) extract reports;
  * Add support for additional covariates in Postgres and SQL Server study extraction SQL;
  * Confirm additional covariates appear in the extract and in the data viewer extract table;
  
  Tasks, stage two (multiple covariates):
  * Add support for multiple covariates to the investigations screen and to the JSON study defintion in the front end;
  * Middleware support for multiple covariates as a) objects, b) JSON study defintion c) database and d) extract reports;
  * Add support for multiple covariates in Postgres and SQL Server study extraction SQL;
  * Add support for multiple covariates in the R code;
  * Confirm multiple covariates appear in the extract and in the data viewer extract table;
  
* [Issue #129 Pooled or individual analysis for multiple risk analysis points/shapes (e.g COMARE postcodes)](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/129).
  Currently the RIF analyses there in N bands with all the sites as one. It is proposed to extend the RIF to support:
  * Individual site analysis;
  * Pooled analysis (1 or more groups of sites). Groups would be defined from a categorisation variable in the shapefile. Would require 
    changes to:
    * Shapefile load screen and controller;
    * JSON study definition format;  
    * Study extract and result tables;
	* R risk analysis code.
* [Issue #126 Oracle interconnect](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/126). Functionality is already
  in place, need to confirm that SQL Server works and both ports perform acceptably. Note that this is not considered suitable for denominator
  or covariate data;

These are not:

* [Issue #68 risk analysis selection at high resolution does not work acceptably](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/68):
  * PNG tile support in test;
  * [Issue #66 Mouseover support](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/66) (so you can see 
    the area names). Reference: [https://gist.github.com/perliedman/84ce01954a1a43252d1b917ec925b3d](https://gist.github.com/perliedman/84ce01954a1a43252d1b917ec925b3dd);
* [Issue #121 Add prior sensitivity analysis](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/121);
* [Issue #67](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/67) print state support for saving user print selection;
* [Issue #118 extend PNG tile support to mapping](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/118). Adds the ability to use tiles instead of GeoJSON in the front end, Currently only 
  integrated to the when load a large area study from a JSON file.

  For geolevels with more than 5000 areas the RIF middleware can auto generate PNG tiles on startup. Tiles are then cached;

### Improve the Installation Process

* [Issue #115 Improve the installation process](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/115). There 
  should be as few manual interactions as possible. Preferably none, though obviously the user giving details at the start is fine. A 
  middleware installer executable would be excellent;

## Next (Q2-Q4 2019)

This is likely to change to adapt to funder requirements.

### Support Multiple Covariates

* [Issue #124 Multiple covariates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/124) (first part for March 2019);
  See above. This will focus on the statistical support.
  
### Information Governance

* [Issue #85 IG tool](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/85);
  The is carried out by a user with the *RIF_MANAGER* role
  * Add new users;
  * Manage table permissions (grant SELECT on table/view to role
  * Add new roles, manage what users have what role, including rif_manager;
  * Display per user view of privileges. An Oracle example is: [Pete Finnigan find_all_privs.sql](http://www.petefinnigan.com/find_all_privs.sql)
    ```
	SQL> @find_all_privs.sql 
    FIND_ALL_PRIVS: Release 1.3.0.0.0 - Production - (http://www.petefinnigan.com) 
    Copyright (c) 2004 PeteFinnigan.com Limited. All rights reserved.

    get user input

    NAME OF USER TO CHECK [ORCL]: OUTLN 
    OUTPUT METHOD Screen/File [S]: 
    FILE NAME FOR OUTPUT [priv.lst]: 
    OUTPUT DIRECTORY [/tmp]:

        USER => OUTLN has ROLE CONNECT which contains =>
                SYS PRIV =>ALTER SESSION grantable => NO
                SYS PRIV =>CREATE CLUSTER grantable => NO
                SYS PRIV =>CREATE DATABASE LINK grantable => NO
                SYS PRIV =>CREATE SEQUENCE grantable => NO
                SYS PRIV =>CREATE SESSION grantable => NO
                SYS PRIV =>CREATE SYNONYM grantable => NO
                SYS PRIV =>CREATE TABLE grantable => NO
                SYS PRIV =>CREATE VIEW grantable => NO
        USER => OUTLN has ROLE RESOURCE which contains =>
                SYS PRIV =>CREATE CLUSTER grantable => NO
                SYS PRIV =>CREATE INDEXTYPE grantable => NO
                SYS PRIV =>CREATE OPERATOR grantable => NO
                SYS PRIV =>CREATE PROCEDURE grantable => NO
                SYS PRIV =>CREATE SEQUENCE grantable => NO
                SYS PRIV =>CREATE TABLE grantable => NO
                SYS PRIV =>CREATE TRIGGER grantable => NO
                SYS PRIV =>CREATE TYPE grantable => NO
        SYS PRIV =>EXECUTE ANY PROCEDURE grantable => NO
        SYS PRIV =>UNLIMITED TABLESPACE grantable => NO
        TABLE PRIV =>EXECUTE table_name => OUTLN_PKG grantable => NO

    PL/SQL procedure successfully completed.
    ```
  * Ability to submit but not run a study; running requires approval;
  * Per user/role low cell counts restrictions;
  * Per user/role resolution restrictions;
  * Create restricted views of tables

### Logging and Auditing

* [Issue #81 improve the audit trail](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/81);
  Add t_rif40_warnings/rif40_warnings table and view to contain warning messages on a study basis. Can be created by the 
  extract or R the scripts. Add traps for:

  * Out of range or null covariates by year;
  * Missing years of numerator or denominator data;
  * Males/females not present when requested in numerator or denominator;
  * ICD codes not present when requested in numerator;
  * Mal-join detection (no rows/too many rows that break the primary key index)
  
  Add Java processing log, SQL statement and warnings logs to extract.
  
* [Issue #86 logging](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/81);
  Modernise the logging, switching to SLF4J and Logback; and also vastly improve the internal handling of, particularly, Exceptions 
  [see also issue #46](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/46). Logging of SQL Exceptions 
  needs to include:

  * SQL Statement;
  * Bind values;
  * Row number;
  * Log console output in batches to Front End logger;

### Data Loading Improvements

[Data loader tools - issues #84](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/84)

A new simple loading tool that is part of the main RIF web application that just loads data in a predefined format directly 
into the database. The tool would be a "Data Loading" tab on the main RIF screen, visible only the users with the *rif_manager* role.
It would have four new icons similar to the tree focused on:
* Geographies;
* Denominator data;
* Covariates;
* Numerator data;

It would be able to:
* Convert age and sex to AGE_SEX_GROUP;
* Add additional required geography fields to data as long as the highest resolution is provided;
* Verify the defined primary key;
* Partition and index

Longer term the data loader tool could support more formats, e.g. typical SAHSU study extracts or CDC datasets and the loading of 
covariates and denominator data.

Support for geographies would require the tileMaker functionality to be moved into the RIF - see below and 
[Issue #91 Tilemaker updates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/91).

### Data Extract Improvements

* Data extract ZIP:
  1. [Scripts - issue #88](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/88). Check 
     manual R script using CSV files still work, and support for Unixen;;
  2. [US Maps - issue #92](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/92). Improve 
      map layout; the US maps are wrong;
  3. [Printing - issue #93](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/93). Add support for 
      printing what the user selects in the mapping screens. Initial (default) setup and been done in the database. This will also include map centre and bounds;.
  4. [Allow use of more fields - issue #94](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/94). Allow 
     the use of more fields from multiple and additional covariates; handle fields with no data;
  5. [Changing resolution - issue #95](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/95). Allow 
     the user to change the resolution of the images;
  6. [Additional items - issue #96](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/96) Additional 
     outputs as required and;
     * Maps if feasible:
       * Investigate vector grid styling, e.g. grid labels (I don't think it is possible)
       * Get SVG to support layers: http://batik.2283329.n4.nabble.com/Combine-SVG-out-of-single-SVG-files-td3616984.html
     * Graphs if feasible:
       * Add support for CSS <style> tags in *jfreechart* SVG generator;
	   * Support for RGB to hex conversion for end colour when graphic bar renderer used.
* [Issue #89 local caching of basemaps](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/89). This is so 
  that we display the underlying map details when the RIF is running on a secure network without access to the internet. We will need to 
  add local basemap cache to RIF for standard Openstreetmap basemap. Will need a webapp to deploy the files and the URL changed to use the 
  local version;

### Further making the RIF Usable within SAHSU

* [Issue #65 sort mapping info boxes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/65). Sort (ex disease) 
  map info boxes - merge into 1 box, add support for homogeneity, exposure covariates in risk analysis. Additionally fix the 
  geography support as the twin maps must be the same geography;
* [Issue #46 exception handling requires improvement to make errors clearer to user and avoid multiple nested errors in the log](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/46);
* [Issue #123 improve search in taxonomies](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/123). The 
  taxonomy search can be confusing when users search by an ICD code. The search currently finds any occurrence of the entered string 
  anywhere in either the code or the description.

  Proposal - split the search into two:
  1. A search of the code field (or "Term Name", as it appears on screen.
  2. A full-text search of the description field.
 
  That should be two separate search boxes, aligned above the corresponding columns in the results table. If the user enters values 
  in both boxes they will be AND-ed together.

  Behaviour:
  
  MM thinks both fields should use simple "contains" searches, like the present one. If the entered value occurs anywhere in the 
  corresponding field, the data will be returned.

  An alternative is to use some system of wildcards, even going as far as supporting regular expressions, but providing the separated 
  boxes will remove the need for that, and supporting those kinds of features would make things more complex for users.
* [Issue #117 Add JavaDoc](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/117). We should automatically 
  build the RIF's JavaDoc, and from time to time deploy it to the GitHub Pages documentation site. At the time of writing, though, 
  the JavaDoc does not build because of malformed HTML in some classes. So that needs to be fixed first.;
* [Issue #110 Re-start RIF services](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/110); This is
  partially implemented as part of the PNG tiles and separate R service implementations. Logging is still an issue; see 
  ({{ site.baseurl }}/plans/2019_Plans.html#logging-and-auditing)
* [Issue #97 Add additional information to circles and shapes such as number of intersections, distance to nearest source](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/97);
  This is mainly complete as of 24/1/2019; freehand circles do not support multiple intersections and support added for analysing multiple 
  shapes in a group or individually 

### Add Prior sensistivity analysis 
  
*  [Issue #121 Add Prior sensistivity analysis](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/121)

### Cluster Analysis

**Issue to be created**

Reference: 

*  Li G, Best N, Hansell AL, Ahmed I and Richardson S. 2012. 
   BaySTDetect: detecting unusual temporal patterns in small area data via Bayesian model choice. 
   Biostatistics 13(4):695-710. DOI: 10.1093/biostatistics/kxs005.

The original code used WinBugs; would require an R version.
		   
### Investigate threading Issues in R service

**Issue to be created**

R service is single threaded. Possible options are:

* Replace *RJava* with an alternative interface;
* Support multiple remote R services in a pool of servers.

## Later

### Support New Interfaces

* [Issue #82 Support New Interfaces](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/82)

  New technical features will include 
  
  * Enhancement of flexibility by clearly defined XML interfaces, giving the RIF a batch mode for 
    the first time and allowing for the export of the data into other tools. Statistical processing will be built in modular manner so it 
    can be easily extended. The save/load study functionality is sufficient for a batch mode, although long term it would be good for 
	regression testing;
  * [BREEZE AERMOD / ISC](http://www.breeze-software.com/aermod/) air quality modelling system.;
  * Wind roses;
  * Multi-layer shapefiles;
  * The ability to re-project user loaded exposure shapefiles from National grid to WGS84 automatically.

  BREEZE AERMOD / ISC and Wind roses are new input forms for risk analysis band selection. 

* [Issue #83 SATScan](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/83).
  Satscan and LinBUGS/WinBUGS be supported via the extract ZIP file - i.e. create a script to run them and produce worked examples. 
  Satscan is available as an R package [rstatscan](https://www.satscan.org/rsatscan/rsatscan.html) but this calls statscan and 
  therefore is limited to Windows only Tomcats. The R package [SpatialEpi](https://cran.r-project.org/web/packages/SpatialEpi/SpatialEpi.pdf) 
  contains a function called *kulldorff*, which performs the purely spatial scan statistic with either the Poisson or 
  Bernoulli probability model. The package also contains many other useful methods that are unrelated to scan statistics and not 
  part of the SaTScan software.

### TileMaker Improvements

* [Issue #91 Tilemaker updates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/91). 

  TileMaker is currently working with some minor faults but needs to:
  * Run the generated scripts. This requires the ability to log on and PSQL copy needs to be replaced to SQL COPY from STDIN/to 
    STDOUT with STDIN/STOUT file handlers in Node.js;
  * UTF8/16 support (e.g. Slättåkra-Kvibille should not be mangled as at present) #79;
  * GUI needs to be merged and brought up to same standard as the rest of the RIF. The TileViewer screen is in better shape than the 
    TileMaker screen. Probably the best solution is to use Angular;
  * Support for database logons;
  * Needs to calculate geographic centroids using the database.

* Convert Tile Maker to Java application; integrate to main RIF front end.

  This would be a longer term objective to fully integrate Tile manufacturing into the main RIF. It requires support for 
  simplification and TopoJSON generation in one of:
  
  * The database (SQL Server does not);
  * Geotools: no support for TopoJSON;
  * Use of [Node.js from within Java](https://eclipsesource.com/blogs/2016/07/20/running-node-js-on-the-jvm/). This requires [J2V8](https://eclipsesource.com/blogs/tutorials/getting-started-with-j2v8/) 
    and is probably the best option as it reuses the Node.js code;
	
  To be in the hierarchy the intersection code insert_hierarchy.sql selects the intersection with the largest intersection by area for 
  each (higher resolution). This eliminates duplicates and picks the most likely intersection on the basis of area. There are two 
  possible reasons for this failure:
  * An intersection was not found. Visually this appears to be the case;
  * The area is zero. This seems unlikely and would need to be tested in SQL.
	
  Currently the hierarchy intersector intersects by the largest area; so where there is an overlap the correct area is chosen. However missing
  intersctions, typically missing Islands (e.g. Lidingö Kommun does not intersect: https://en.wikipedia.org/wiki/Liding%C3%B6 Stockholm county) or
  reclaimed ground (e.g. Cardiff docks COAs W00010161 and W00010143 are missing from the LSOA intersction). These have to be fixed by hand 
  by inserting the correct intersction; an algorithm to pick the nearest shape by centroid is required.
  
  ![Cardiff docks COA issue]({{ site.baseurl }}/rifNodeServices/cardiff_COA_issue2.png){:width="100%"}

   Gottröra parish in STOCKHOLM (a level3) is not included because the level4 area includes two level3's and the neighbour was picked as 
   it was bigger, so was deleted. This has to be the only action when this occurs and is likely to cause issues with geocoded data where
   of course both level3's can be used. Fixed by deleting the smaller parish. This can be logically detected and added to the processing.
   
### Performance Improvements

* [Issue #80 optimise performance on large datasets](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/80). 
  This work is envisaged to require:

  * Use of partitioning for Health data (especially denominators);
  * Potential for the tuning of extraction SQL, especially on SQL Server. If partitioning is used it is essential to verify that 
    partition elimination occurs so that the database only fetches the years of data actually required by the study, indexes 
	are not disabled and the query plan remains structurally the same;
  * Partitioning support in data loading
  * Parallel Queries
  * Tuning manual
  * Front end tuning
  * Parallelise R

### Generalise User Management

* [Issue #102 Generalise user management](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/102)

  This is to add documentation to the manuals for the use of:
  * LDAP
  * Kerberos/GSSAPI
  This usually does not require a change to the front end or the middleware. Normally it is set up in the database, e.g. 
  [LDAP Authentication](https://www.postgresql.org/docs/11/static/auth-ldap.html). SQL Server only supports username/password 
  and windows (Kerberos). Kerberos in a browser requires GSSAPI as the authentication token has to move across the network. 
  The private network used to use this - its is hard to setup, requires configuration of the browser and a Kerberos type 
  connection in the Java which the Microsoft JDBC driver apparently support [Using Kerberos Integrated Authentication to 
  connect to SQL Server](https://docs.microsoft.com/en-us/sql/connect/jdbc/using-kerberos-integrated-authentication-to-connect-to-sql-server?view=sql-server-2017) 

# Potential Additional Functionality

## CDC

* Loading of local data extracts and covariates directly into the RIF; e.g. you could load the SEER data directly into the RIF in a single 
  operation with no other data required other than the geography;

## PHE

* Encrypted data extract ZIP files;
* Prevention of any data download from the RIF except via the encrypted data extract ZIP files. Currently the RIF displays data in 
  tables populated by REST calls, if the user is authorised by information governance they can be downloaded or copied from the screen. 
  They would be prevented even if information governance permits so the only method to download the data is via the encrypted data extract. 
  The RIF front end uses [UI-Grid](http://ui-grid.info/) for tabular data and this support selection and export controls (i.e. they can 
  be disabled). Disabling web browser copy everywhere is likely to be difficult and a satisfactory solution may not be possible. 

## SAHSU

* Health Atlas Integration. Map data from the RIF could be exported as tiles in GeoJSON, TopoJSON or PNG format for use in a Health Atlas.

# Bug Fixes

## High priority

* [Issue #128 SQL Server SAHSU Database not linked to geography](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/128). SQL Server SAHSU Database not linked to geography. This is a column length issue (i.e. there is a spurious space or two).;

## To be allocated - low priority

* [Issue #79 TileMaker Unicode area names](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/79) - SQL Server 
  only. Some examples from the US geography in Puerto Rico:

  * Postgres works fine. Previous fix to NVARCHAR(1000) has worked. SQL Server is still wrong. Problems is therefore in the SQL Server 
    database. Effects database, hopefully not the various Java/Javascript drivers. 

    Postgres (correct):
    ```
    sahsuland=> SELECT areaname from lookup_cb_2014_us_county_500k where cb_2014_us_county_500k = '01804540';
      areaname
    ------------
     Río Grande
    (1 row)
    ```
    
    SQL Server (wrong):
    ```
    1> SELECT areaname from rif_data.lookup_cb_2014_us_county_500k where cb_2014_us_county_500k = '01804540';
    2> go
    areaname                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
    ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    R+-ío Grande                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    
    (1 rows affected)
    ```
    ![sql server rio grande](https://user-images.githubusercontent.com/6932261/45543945-dad0c200-b80d-11e8-85c9-5f9497bd1256.PNG){:width="100%"}
	
	The columns are all NVARCHAR(1000) all the way back to the initial load table. They indicates an issue with *BULK INSERT* the 
	SQL Server load command [Use Unicode Character Format to Import or Export Data](https://docs.microsoft.com/en-us/sql/relational-databases/import-export/use-unicode-character-format-to-import-or-export-data-sql-server?view=sql-server-2017):;
* [Issue #75 IE support - IE only works in debug mode](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/75). This is caused by 
  ```console.log```, ```console.debug``` calls not testing if the browser is open. Most RIF and library code is safe; believed to be the 
  proj4.js Javascript library [http://proj4js.org/](http://proj4js.org/);
* [Issue #57 front end mapping synchronisation (map auto draw disabled](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/57), 
  [issue #67 print state support](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/75) depends. This is caused by the map being rendered before the Choropleth map is setup in the Choroscope 
  service. Work around is to do this manually using the setup icon.;
* [Issue #56 error loading study from database generated JSON](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/56). Error 
  loading study from database via middleware generated file; **ERROR: Could not set study state: No comparison area polygons:** this is 
  caused by no area_ids in the database generated JSON;
* [Issue #113 refresh logs you off](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/113). When the user refreshes 
  angular goes to state 0 which effectively logs you off. State 0 needs to ask the database if the user (saved as a cookie) is still 
  logged on the session will resume in state1 (study submission screen).
* [Issue #130 Risk analysis fault/issues (branch: risk-analysis-fixes-2)](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/130). 
  1. Multiple Health outcomes produces errors
  2. Health outcome may require a geography change - if it is wrong you get:
     ```
     ERROR: No health data for theme "SAHSU land cancer incidence example data", geography "EWS2011". 
     ERROR: Could not retrieve your project information from the database: unable to get numerator/denominator pair 
     ```
  3. Using add by postcode produces errors on its own, but works;
  4. Errors if nonsensical exposure bands are selected;
  5. Clear does not work after restore from file;
  6. Adding a point produces errors after restore from file;
  7. Add disableMouseClicksAt from frontEndParameters.json5 to replace hard coded 5000 in Tile generation;
  8. Load list from text file loads OK but does not display correctly;
  9. Need a file type filter when loading JSON files;
  10. Zip shapefile load to be able to cope with projections other than 4326 (e.g. local grid).