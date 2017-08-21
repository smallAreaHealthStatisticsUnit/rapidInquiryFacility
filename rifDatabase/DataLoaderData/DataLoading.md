RIF Data Loading
================

# Contents

- [1. Overview](#1-overview)
  - [1.1 RIF Capabilities](#11-rif-capabilities)
  - [1.2 Limitations](#12-limitations)
  - [1.3 Data Loader Tool](#13-data-loader-tool)
- [2. RIF Data Loading Prerequistes](#2-rif-data-loading-prerequistes)
  - [2.1 Postgres](#21-postgres)
  - [2.2 SQL Server](#22-sql-server)
  - [2.3 Data Structure](#23-data-structure)
- [3. Load Processing](#3-load-processing)
  - [3.1 Numerator](#31-numerator)
  - [3.2 Denominator](#32-denominator)
  - [3.3 Covariates](#33-covariates)
  - [3.4 Administrative Geography](#34-administrative-geography)
- [4. Information Governance](#4-information-governance)
  - [4.1 Auditing](#41-auditing)

# 1. Overview

This document details both the manual process for the loading of data into the RIF and the automated process 
using the data loader tool. The RIF requires the following types of data:

* Numerator data. This may be in indivual record form or aggregated to a suitable administrative geography. 
  Indvidual records are assumed to have been de-indentified into a suitable pseudonymous form; all this is
  not a requirement.
* Denominator data. This is in aggregate form.
* Covariate data. This is again in aggregate form and has been quantilised
* Administrative geography. This is created by the Tile-Maker tool; this document details the loading of 
  this data and the geocoding requirements of the RIF. See [Add tile-maker manual] for how to create
  RIF administrative geographiues.
  
RIF data loading occurs in two distinct phases; 

* Load pre processing. The end product are CSV files suitable for use either in a RIF load script or for use
  by the RIF data loader tool. An example is provided using the US SEER Cancer Registry data with example 
  scripts for both Postgres and SQL Server.
* RIF loading processing. The end result here is a fully configured RIF with the data loaded. Typically 
  users need to be granted access to the dataset to be able to use the data. Data without access is not visible
  to users in the RIF. There are two routes to carry out this this processing:
  * Via the RIF data loader tool. This carries out additional load pre-processing and then generates data and 
    scripts for both SQL Server and Postgres
  * Manually via a user created script. Again an example is provided using the US SEER Cancer Registry data with example 
    scripts for both Postgres and SQL Server.	
  
# 1.1 RIF Capabilities
  
To be added. To include:

* Multiple geographies, numerators, covariates
* Complex condition logic
* ...

In addition to the limitations detailed below which will be removed in fiture releases the following capabilities
may potentially be added in future releases:

* Temporal analysis (run by year/groups of years)
* Covariates without years
* Study age bands, e.g. 0-19,20-29,60-74,75+
  
# 1.2 Limitations

Limiations in the current RIF

* No support form covariates embedded in numerator or denominator data. Data must be extracted into a separate 
  covariate table. Must covariates must be merged into a single table. It is not planned to remove these 
  restrictions.
* Covariates must be quantilised. Support on the fly quantilisation may be added in future releases.
* Denomionator data is always used in indirect standardisation. There is no support currently in the RIF for
  direct standardisation using standard populations. This was supported in previous versions and will be put 
  back if required.
* Numarators are currently limited to ICD 10 coding only. Support will be progressively added for:
  * ICD 9
  * ICD 11
  * ICD oncology O(ICD-O-1)
  * UK HES oper and A+E codes
  * User specified conditions, e.g. Low birthweight, complex groups of ICD codes
* The RIF currently lack complex support for Information Governance beyond having strong role based permissions.
  
  **This document will detail how to audit access to the data.**. This is envisaged as being part of a separate
  information governance tool. When this tool is operatoon the RIF will allow users to extract more complex 
  data than just the extracton required to calculate the results.
  
It is planned to remove the following restrictions progressively in future releases:

* One covariate only. 
* One investigation only
* Covariate study_geolevel_name must be the same as rif40_studies.study_geolevel_name, 
  i.e. covariate geolevel cannot be of lower resolution than study geolevel.
* Disease mapping only.
* AGE_SEX_GROUP column only (i.e. no separate AGE_GROUP/AGE/SEX columns).

1.3 Data Loader Tool
  
The data loader tool in its current prototype form is severly limited in its ability to transform data by:

* Aggregation
* Re-gecoding data
* Performing complex cleaning and remapping of data (e.g. chosing between provisiomnal and final causes of death)

It is envisaged that the data loader will become a browser based tool like the RIF of the RIF toolchain.
 
# 2. RIF Data Loading Prerequistes

The following steps need to be carried out to load process the data:

* Separation of data into numerator, denominator and covariates. 
* Collation of covariates into a single covariate source
* Geo-coding of data by joining to the adminstrative dataset.

The following are restrictions on the naming of columns:

* The names of the geolevels must match exactly and all geolevels must must be present in numerator and 
  denominator data. For covariates geolevel names the name must match the geolevel the covariate is for.
* All names must be in lower case, up to 30 characters long, start with a lower case letter, and only contain
  lower case letters, digits (0-9) and underscore. This is a common database restriction. The 30 character 
  limit comes from the earlier Oracle versions of the RIF.
  
## 2.1 Postgres

Postgres uses the *\copy* command to load and unload data. *\copy* cannot handle fixed length data; this 
is loaded as a fioxed length string and parsed using SQL.

The SEER data required USA load phase data pg_USQ_2014.sql to be loaded as a RIF user (not rif40) and 
the production data (rif_pg_usa_2014.sql) needs to be loaded into the rif40 account in the rif_data schema.

## 2.2 SQL Server

To be added.

## 2.3 Data Structure

# 3. Load Processing 

## 3.1 Numerator

## 3.2 Denominator

## 3.3 Covariates

## 3.4 Administrative Geography

# 4. Information Governance

## 4.1 Auditing

## 5. Data Loader Issues

The folowing are known issues with the RIF data loader. 

* The properties file RIFDataLoaderToolStartupProperties.properties used is the hard coded one at compile time.
  Needs to be set to read this file from the current directory. This means the soujrce has to be edited to change
  the database type and connection settings. The current default is localhost and Postgres.
  
  
Peter Hambly
17th August 2017