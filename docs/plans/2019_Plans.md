---
layout: default
title: 2019 Plans
---

1. Contents
{:toc}

# Introduction

RIF Priorities for the first quarter of 2019 are:

1. Make the RIF Usable within SAHSU;
2. Improve the installation process

See the [RIF Roadmap](https://trello.com/b/CTTtyxJR/the-rif-roadmap) on Trello or as a graphic: 
![2019 RIF Roadmap]({{ site.baseurl }}/plans/2019_RIF_Roadmap.png)

# Bug Fixes

## High priority

* SQL Server SAHSU Database not linked to geography;

## To be allocated - low priority

* Issue #79 Unicode area names (SQL Server only);
* Issue #75 IE support;
* Issue #57 front end mapping synchronisation (map auto draw disabled, #issue #67 print state support depends);
* Issue #56 error loading studty from database generated JSON;
* Issue #113 refresh logs you off;.

# Enhancements

## Now (Q1 2019)

### Additional Covariates

* [Issue #124 Multiple covariates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/124) (first part for March 2019). 
  This will be implemented this in two stages:

  * One primary covariate, multiple additional covariates, No support for multiple covariates in the calculation or results 
    (this reduce resource risk). Multiple additional covariates available in the extract.
  * Full multiple covariate support;
  
This is a priority for end of February 2019.
  
### Make the RIF Usable within SAHSU

These are a priority for end of February 2019

* [Issue #? risk analysis maps](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/?);
* [Issue #126 Oracle interconnect](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/126);

These are not:

* [Issue #68 risk analysis selection at high resolution does not work acceptably](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/68):
  * PNG tile support in test;
  * [Mouseover support required](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/66) (so you can see the area names) - issue #66;
* [Issue #121 Add priors](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/121);
* [Issue #67](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/67) print state support for saving user print selection;
* [Issue #118 extend PNG tile support to mapping](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/118). Adds the ability to use tiles instead of GeoJSON in the front end, Currently only 
  integrated to the when load a large area study from a JSON file.

  For geolevels with more than 5000 areas the RIF middleware can auto generate PNG tiles on startup. Tiles are then cached;
* [Issue #80 optimise performance on large datasets](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/80);

### Improve the Installation Process

* [Issue #118 Improve the installation process](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/118);

## Next (Q2-Q4 2019)

### Support Multiple Covariates

* [Issue #124 Multiple covariates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/124) (first part for March 2019);
  See above.
  
### Information Governance

* [Issue #85 IG tool](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/85);
  The is carried out by a user with the *RIF_MANAGER* role
  * Add new users;
  * Manage table permissions (grant SELECT on table/view to role
  * Add new roles, manage what users have what role, including rif_manager;
  * Display user view of privileges. An Oracle example is: [Pete Finnigan find_all_privs.sql](http://www.petefinnigan.com/find_all_privs.sql)
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
into the database. It would be able to:

* Convert age and sex to AGE_SEX_GROUP;
* Add additional required geography fields as long as the highest resolution is provided;
* Verify the defined primary key;
* Partition and index

Longer term the data loader tool could support more formats, e.g. typical SAHSU study extracts or CDC datasets and the loading of 
covariates and denominator data

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

### Cluster Analysis

**Issue to be created**

Reference: 

  Li G, Best N, Hansell AL, Ahmed I and Richardson S. 2012. 
  
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
  
  * The database (SQL Server does not)
  * Geotools: no support for TopoJSON
  * Use of [Node.js from within Java](https://eclipsesource.com/blogs/2016/07/20/running-node-js-on-the-jvm/). This requires [J2V8](https://eclipsesource.com/blogs/tutorials/getting-started-with-j2v8/) 
    and is probably the best option as it reuses the Node.js code  

### Performance Improvements

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
