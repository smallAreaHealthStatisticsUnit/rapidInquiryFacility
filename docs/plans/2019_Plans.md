---
layout: default
title: 2019 Plans
---

1. Contents
{:toc}

# Introduction

[RIF Roadmap](https://trello.com/b/CTTtyxJR/the-rif-roadmap) or as a graphic: ![2019 RIF Roadmap]({{ site.baseurl }}/plans/2019_RIF_Roadmap.png)

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

### Make the RIF Usable within SAHSU

* Issue #? risk analysis maps;
* Issue #126 Oracle interconnect;
* Issue #68 risk analysis selection at high resolution does not work acceptably:
  * PNG tile support in test;
  * Mouseover support required (so you can see the area names) - issue #66;
* Issue #121 Add priors;
* Issue #67 print state support for saving user print selection;
* Issue #63 extend PNG tile support and restructed mapping code to mapping;
* Issue #80 optimise performance on large datasets;

### Improve the Installation Process

* Issue #118 Improve the installation process;

### Additional Covariates

* Issue #124 Multiple covariates (first part for March 2019);

## Next (Q2-Q4 2019)

### Support Multiple Covariates

* Issue #124 Multiple covariates (first part for March 2019);

### Information 

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

### Logging and AuditingGovernance

* [Issue #81 improve the audit trail](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/81);
  Add t_rif40_warnings/rif40_warnings table and view to contain warning messages on a study basis. Can be created by the 
  extract or R the scripts. Add traps for:

  * Out of range or null covariates by year;
  * Missing years of numerator or denominator data;
  * Males/females not present when requested in numerator or denominator;
  * ICD codes not present when requested in numerator;
  * Maljoin detection
  
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
  1. [Scripts - issue #88](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/88);
  2. [US Maps - issue #92](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/92)
  3. [Printing - issue #93](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/93);
  4. [Allow use of more fields - issue #94](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/94);
  5. [Changing resolution - issue #95](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/95);
  6. [Additional items - issue #96}(https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/96);
* [Issue #85 local caching of basemaps](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/85);

### Further making the RIF Usable within SAHSU

* Issue #65 sort mapping info boxes;
* Issue #46 exception handling requires improvement to make errors clearer to user and avoid multiple nested errors in the log;
* Issue #123 improve search in taxonomies;
* Issue #117 Add JavaDoc;
* Issue #110 Re-start RIF services;
* Issue #97 Add additional information to circles and shapes (number of intersections, distance to nearest source);

### Cluster Analysis

### Investigate threading Issues in R service

## Later

### Support New Interfaces

* [Issue #82 BREEZE AERMOD / ISC and Wind roses](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/82)
* [Issue #83 SATScan](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/83)

### TileMaker Improvements

* [Issue #91 Tilemaker updates](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/91)
* Convert Tile Maker to Java application; integrate to main RIF front end.

  This would be a longer term objective to fully integrate Tile manufacturing into the main RIF. It requires support for 
  simplification and TopoJSON generation in one of:
  
  * The database (SQL Server does not)
  * Geotools: no support for TopoJSON
  * Use of [Node.js from with Java](https://eclipsesource.com/blogs/2016/07/20/running-node-js-on-the-jvm/). This requires J2V8 
    and is probably the best option as it reuses the Node.js code  

### Performance Improvements

* Partitioning support in data loading
* Parallel Queries
* Tuning manual
* Front end tuning
* Parallelise R

### Generalise User Management

* [Issue #102 Generalise user management](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/102)

