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
     - [2.3.1 Numerator](#231-numerator)
     - [2.3.2 Denominator](#232-denominator)
     - [2.3.3 Covariates](#233-covariates)
     - [2.3.4 Administrative Geography](#234-administrative-geography)
- [3. Load Processing](#3-load-processing)
  - [3.1 Numerator](#31-numerator)
  - [3.2 Denominator](#32-denominator)
  - [3.3 Covariates](#33-covariates)
  - [3.4 Administrative Geography](#34-administrative-geography)
- [4. Information Governance](#4-information-governance)
  - [4.1 Auditing](#41-auditing)
- [5. Flexible Configuration Support](#5-flexible-configuration-support)
  - [5.1 Age Groups](#51-age-groups)
  - [5.2 ICD field Name](#52-icd-field-name)
- [6. Quality Control](#6-quality-control)
  - [6.1 Extract Warnings](61-extract-warnings)
  
# 1. Overview

This document details the manual process for the loading of data into the RIF. The RIF requires the following types of data:

* Numerator data. This may be in individual record form or aggregated to a suitable administrative geography.
  Individual records are assumed to have been de-identified into a suitable pseudonymous form; all this is
  not a requirement.
* Denominator data. This is in aggregate form.
* Covariate data. This is again in aggregate form and has been quantilised
* Administrative geography. This is created by the Tile-Maker tool; this document details the loading of
  the administrative geography data and the geo-coding requirements of the RIF. See the [tile-maker manual](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/tileMaker.md) for how to create
  RIF administrative geographies.

RIF data loading occurs in two distinct phases;

* Load pre processing. The end product are CSV files suitable for use either in a RIF load script or for use
  by the RIF data loader tool. An example is provided using the US SEER Cancer Registry data with example
  scripts for both Postgres and SQL Server.
* RIF loading processing. The end result here is a fully configured RIF with the data loaded. Typically
  users need to be granted access to the dataset to be able to use the data. Data without access is not visible
  to users in the RIF. There are two routes to carry out this this processing:
  * Via the RIF data loader tool. This is the automated process for loading data into the RIF. The data loader tool carries out additional load pre-processing and then generates data and
    scripts for both SQL Server and Postgres. The current data loader documentation is not in this document; but in: 
	[RIF Data Loader Manual](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Documentation/RIF%20Data%20Loader%20Manual.pdf).
	The data loader tools is still in development and is not expected to be complete until 2019.
  * Manually via a user created script. Again an example is provided using the US SEER Cancer Registry data with example
    scripts for both Postgres and SQL Server.

# 1.1 RIF Capabilities

The RIF supports:

* Multiple geographies, numerators, covariates
* Complex condition logic
* User customizable age groups

In addition to the limitations detailed below which will be removed in future releases the following capabilities
may potentially be added in future releases:

* Multiple investigations
* Temporal analysis (run by year/groups of years)
* Covariates without years
* Study age bands, e.g. 0-19,20-29,60-74,75+

# 1.2 Limitations

Limitations in the current RIF

* No support form covariates embedded in numerator or denominator data. Data must be extracted into a separate
  covariate table. Covariates must be merged into a single table and disaggregated (if required) by year. It is not planned to remove these
  restrictions which were in the previous RIF.
* Covariates must be quantilised. Support discrete value covariates (i.e. on the fly quantilisation) may be added in future releases.
* Denominator data is always used in indirect standardisation. There is no support currently in the RIF for
  direct standardisation using standard populations. This was supported in previous versions of the RIF and will be put
  back if required;
* No support for ad-hoc SQL. This functionality will be partially re-implemented in future releases using user specified conditions 
  (pre defined groups). The feature was removed as it cannot be implemented in a secure manner (i.e. it permits SQL injection attacks);
* Numerators are currently limited to ICD 10 coding only. Support will be added for:

  * ICD 9
  * ICD 11 (subject to the release of the 11th Edition in June 2018)
  
  In the longer term it is expected that support will be added for:
  
  * ICD oncology O(ICD-O-1)
  * UK HES oper and A+E codes
  * User specified conditions (pre defined groups), e.g. Low birthweight, complex groups of ICD codes, the all record condition (the 
    *1=1* ad-hoc SQL filter in the previous RIF) 

* Single ICD field name. Unlike the old RIF the field name is configurable; 	
* The RIF currently lacks complex support for Information Governance beyond having strong role based permissions.

  **This document will detail how to audit access to the data.**. This is envisaged as being part of a separate
  information governance tool. When this tool is operation the RIF will allow users to extract more complex
  data than just the extraction required to calculate the results.

It is planned to remove the following restrictions progressively in future releases:

* One covariate only;
* One investigation only;
* Single ICD field names. Multiple field name support is in the database with partial support in the database;
* Covariate study_geolevel_name must be the same as rif40_studies.study_geolevel_name,
  i.e. covariate geolevel cannot be of lower resolution than study geolevel;
* Disease mapping only. Risk analysis will be added during summer 2018;
* AGE_SEX_GROUP column only (i.e. no separate AGE_GROUP/AGE/SEX columns);
* Support for more than current and previous version of a table outcome (e.g. ICD). This would allow ICD 9, 10 and 11 
  (or 8, 9 and 10) to be supported all at the same time together the the start and end year for each version in a numerator table.
  Currently the RIF applies the same ICD filter to all years. This approach may cause problems if there are coding incompatibilities
  between the version (i.e. the same code means something different in two or more version).

Like the old RIF there are field naming restrictions:

* AGE_SEX_GROUP. Use of AGE and SEX is not permitted; the column must be called *AGE_SEX_GROUP*;
* YEAR

THe ICD field name is now configurable. The default is *ICD_SAHSU_01*.

## 1.3 Data Loader Tool

The data loader tool in its current prototype form is severely limited in its ability to transform data by:

* Aggregation of data. The data loader tool requires aggregate data
* Re-gecoding data
* Performing complex cleaning and remapping of data (e.g. choosing between provisional and final causes of death)

The following are known issues with the RIF data loader.

* The properties file RIFDataLoaderToolStartupProperties.properties used is the hard coded one at compile time.
  Needs to be set to read this file from the current directory. This means the source has to be edited to change
  the database type and connection settings. The current default is *localhost* and Postgres.
* Numeric total fields to be supported  
* Numerators without a total field needs to be supported; exception is not handled correctly. Data loader
  tool only supports aggregate data.
  
It is envisaged that the data loader will become a browser based tool like the RIF of the RIF tool chain.

The current data loader documentation is in the 
[RIF Data Loader Manual](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Documentation/RIF%20Data%20Loader%20Manual.pdf)

# 2. RIF Data Loading Prerequisites

The following steps need to be carried out to load process the data:

* Separation of data into numerator, denominator and covariates.
* Collation of covariates into a single covariate source
* Geo-coding of data by joining to the administrative dataset.

The following are restrictions on the naming of columns:

* The names of the geolevels must match exactly and all geolevels must must be present in numerator and
  denominator data. For covariates geolevel names the name must match the geolevel the covariate is for.
* All names must be in lower case, up to 30 characters long, start with a lower case letter, and only contain
  lower case letters, digits (0-9) and underscore. This is a common database restriction. The 30 character
  limit comes from the earlier Oracle versions of the RIF.

## 2.1 Postgres

Postgres uses the *\copy* command to load and unload data. *\copy* cannot handle fixed length data; this
is loaded as a fixed length string and parsed using SQL.

The SEER data required USA load phase data pg_USQ_2014.sql to be loaded as a RIF user (not rif40) and
the production data (rif_pg_usa_2014.sql) needs to be loaded into the rif40 account in the rif_data schema.

## 2.2 SQL Server

SQL Server needs access permission granted to the directories used to `BULK INSERT` files, the files are not copied from the client to the 
server as in the *Postgres* *psql* ```\copy` command and the *Oracle* *sqlldr* command.

SQL Server needs access to these directories. The simplest
way is to allow read/execute access to the local users group (e.g. PH-LAPTOP\Users or USERS depending on your Windows version).

*DO NOT TRY TO RUN BULK INSERT FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).* Use a local directory which SQL Server has
access to; e.g. somewhere on the C: drive. Note that SQL Server *BULK LOAD* behaves deterrently if you logon using Windows authentication (where it will use your credentials 
to access the files) to using a username and password (where it will use the Server's credentials to access the file).

```
BULK INSERT rif_data.lookup_sahsu_grd_level1
FROM 'C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\GeospatialData\tileMaker/mssql_lookup_sahsu_grd_level1.csv'     -- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line
WITH
(
        FORMATFILE = 'C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\GeospatialData\tileMaker/mssql_lookup_sahsu_grd_level1.fmt',            -- Use a format file
        TABLOCK                                 -- Table lock
);

Msg 4861, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 7
Cannot bulk load because the file "C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\GeospatialData\tileMaker/mssql_lookup_sahsu_grd_level1.csv" could not be opened. Operating system error code 5(Access is denied.).
```

## 2.3 Data Structure

### 2.3.1 Numerator

The example numerator table is *num_sahsuland_cancer*:

|   column_name    |                                description                                |  data_type  |
|------------------|---------------------------------------------------------------------------|-------------|
| year             | year field                                                                | integer     |
| age_sex_group    | An integer field which represents a combination of codes for sex and age. | integer     |
| sahsu_grd_level1 | first level geographical resolution                                       | varchar(20) |
| sahsu_grd_level2 | second geographical resolution                                            | varchar(20) |
| sahsu_grd_level3 | third level of geographical resolution                                    | varchar(20) |
| sahsu_grd_level4 | fourth level geographical resolution                                      | varchar(20) |
| icd              | ICD code field                                                            | varchar(5)  |
| total            | total field                                                               | integer     |
 
The *num_sahsuland_cancer* table is aggregated to the highest geographic resolution. The table can disaggregated to individual level data 
(e.g. SAHSU cancer incidence data). Numerator table must have the following fields:

* *YEAR*. This must be an integer;
* *AGE_SEX_GROUP*. This contains an age sex group field name, e.g. ```M5_9```. See: ???;
* *&lt;outcome groups field name&gt;* e.g. ```icd``` for the OUTCOME_GROUP_NAME **SAHSULAND_ICD**. See: ???;
* *&lt;one geolevel field name for each geography geolevel&gt;* e.g. ```sahsu_grd_level1```. See: ???;
* *&lt;total field name&gt;* e.g. ```total```. If this is null, the &lt;outcome groups field name&gt; e.g. ```icd``` is **COUNT**ed (i.e. the table is disaggregated). 
  If not null, the &lt;total field name&gt; is summed.

### 2.3.2 Denominator

The example denominator table is *pop_sahsuand_pop*:

|   column_name    |                                description                                |  data_type  |
|------------------|---------------------------------------------------------------------------|-------------|
| year             | year field                                                                | integer     |
| age_sex_group    | An integer field which represents a combination of codes for sex and age. | integer     |
| sahsu_grd_level1 | first level geographical resolution                                       | varchar(20) |
| sahsu_grd_level2 | second geographical resolution                                            | varchar(20) |
| sahsu_grd_level3 | third level of geographical resolution                                    | varchar(20) |
| sahsu_grd_level4 | fourth level geographical resolution                                      | varchar(20) |
| total            | total field                                                               | integer     |
 
The *pop_sahsuand_pop* table is aggregated to the highest geographic resolution. The table **CANNOT** disaggregated to individual level data or rotated with *AGE_SEX_GROUP* as multiple columns. 
Denominator table must have the following fields:

* *YEAR*. This must be an integer;
* *AGE_SEX_GROUP*. This contains an age sex group field name, e.g. ```M5_9```. See: ???;
* *&lt;one geolevel field name for each geography geolevel&gt;* e.g. ```sahsu_grd_level1```. See: ???;
* *&lt;total field name&gt;* e.g. ```total```. This is summed.
  
### 2.3.3 Covariates

The example covariates tables are *covar_sahsuland_covariates3*: 

|   column_name    |              description               |  data_type
|------------------|----------------------------------------|-------------
| year             | year field                             | integer
| sahsu_grd_level3 | third level of geographical resolution | varchar(20)
| ses              | socio-economic status                  | integer
| ethnicity        | ethnicity                              | integer
 
And *covar_sahsuland_covariates4*: 

|   column_name    |             description              |  data_type  |
|------------------|--------------------------------------|-------------|
| year             | year field                           | integer     |
| sahsu_grd_level4 | fourth level geographical resolution | varchar(20) |
| ses              | socio-economic status                | integer     |
| areatri1km       | area tri 1 km covariate              | integer     |
| near_dist        | near distance covariate              | numeric     |
 
### 2.3.4 Administrative Geography

# 3. Load Processing

## 3.1 Numerator

## 3.2 Denominator

## 3.3 Covariates

## 3.4 Administrative Geography

# 4. Information Governance

## 4.1 Auditing

# 5. Flexible Configuration Support

## 5.1 Age Groups 

This is configured using the tables:

* **rif40.rif40_age_group_names**: This table defines age groups. Administrators may add new ones:

| age_group_id |          age_group_name           | 
|--------------|-----------------------------------|
|            1 | RIF Default                       |
|            2 | RIF Birth defects                 |
|            3 | RIF Childhood infectious diseases |
			
* **rif40.rif40_age_groups**: This defines the field name and age ranges for each age group name. In a numerator or denominator table the *field name* would have *M* or *F* appended.
  The name *field name* is an artefact from RIF2.0 when the denominator tables were rotated for efficiency with a separate field for each age sex group. Modern RIFs use the non rotated 
  form where the AGE_SEX_GROUP column contains the *fieldname* values.
  The RIF does **NOT** support intersex, unknown or personal gender choices/pronouns. So *age_group_id* 1, *offset* 6 would be ```M10_14``` for males aged 10 to 14 and
  ```F10_14``` for females aged 10 to 14 and would be found in the AGE_SEX_GROUP column.
  
  The standard table defines the age ranges for three age group names:
  
  * RIF Default: 0, 1, 2, 3, 4, 5 to 9 .. 80 to 84, 85 plus
  * RIF Birth defects: 0 only
  * RIF Childhood infectious diseases: 0 to 1, 2 to 3, 4 to 5, 6 to 7, 8 to 9, 10 to 19, 20 to 39, 40 to 79, 80 plus

| age_group_id | offset | low_age | high_age | fieldname |
|--------------|--------|---------|----------|-----------|
|            1 |      0 |       0 |        0 | 0         |
|            1 |      1 |       1 |        1 | 1         |
|            1 |      2 |       2 |        2 | 2         |
|            1 |      3 |       3 |        3 | 3         |
|            1 |      4 |       4 |        4 | 4         |
|            1 |      5 |       5 |        9 | 5_9       |
|            1 |      6 |      10 |       14 | 10_14     |
|            1 |      7 |      15 |       19 | 15_19     |
|            1 |      8 |      20 |       24 | 20_24     |
|            1 |      9 |      25 |       29 | 25_29     |
|            1 |     10 |      30 |       34 | 30_34     |
|            1 |     11 |      35 |       39 | 35_39     |
|            1 |     12 |      40 |       44 | 40_44     |
|            1 |     13 |      45 |       49 | 45_49     |
|            1 |     14 |      50 |       54 | 50_54     |
|            1 |     15 |      55 |       59 | 55_59     |
|            1 |     16 |      60 |       64 | 60_64     |
|            1 |     17 |      65 |       69 | 65_69     |
|            1 |     18 |      70 |       74 | 70_74     |
|            1 |     19 |      75 |       79 | 75_79     |
|            1 |     20 |      80 |       84 | 80_84     |
|            1 |     21 |      85 |      255 | 85PLUS    |
|            2 |      0 |       0 |        0 | 0         |
|            3 |      0 |       0 |        1 | 0_1       |
|            3 |      1 |       2 |        3 | 2_3       |
|            3 |      2 |       4 |        5 | 4_5       |
|            3 |      3 |       6 |        7 | 6_7       |
|            3 |      4 |       8 |        9 | 8_9       |
|            3 |      5 |      10 |       19 | 10_19     |
|            3 |      6 |      20 |       39 | 20_39     |
|            3 |      7 |      40 |       79 | 40_79     |
|            3 |      8 |      80 |      255 | 80PLUS    |
			

## 5.2 ICD field Name

This is configured using the tables:

* **rif40.rif40_table_outcomes**: This binds numerator tables to the outcome group name (e.g. ICD field name) for the table. The default outcome group name for the RIF
  is *SAHSULAND_ICD*. The field *CURRENT_VERSION_START_YEAR* refers to the start of the *CURRENT_VERSION* field for the *OUTCOME_TYPE* in the table *rif40.rif40_outcomes*.
  The *OUTCOME_TYPE* is from the table *rif40.rif40_outcome_groups*. In this case *CURRENT_VERSION_START_YEAR* refers to the year ICD 10 coding starts from in the numerator
  data. As the RIF only supports current and previous version ICD 11 (or ICD 8) support in the same table and ICD 9 and 10 will require a new table 
  **rif40.rif40_table_outcome_versions** in a future RIF. This is a limitation inherited from the old RIF.

| outcome_group_name |      numer_tab       | current_version_start_year |
|--------------------|----------------------|----------------------------|
| SAHSULAND_ICD      | NUM_SAHSULAND_CANCER |                       1989 |
  
  Normally, when adding a new numerator table to the RIF; the default outcome group name (*SAHSULAND_ICD*) is used.

* **rif40.rif40_outcomes**: This defines the outcome supported by the RIF. Currently only ICD (in version 10) is supported.

| outcome_type |                                       outcome_description                                       | current_version | current_sub_version  | previous_version | previous_sub_version |
|--------------|-------------------------------------------------------------------------------------------------|-----------------|----------------------|------------------|----------------------|
| ICD          | International Classification of Disease                                                         | 10              | 11th Revision - 2010 | 9                |                      |
| OPCS         | Office of Population Censuses and Surveys [OPCS] Classification of Interventions and Procedures | 4               | 4.6 1/11/2011        |                  |                      |
| ICD-O        | International Classification of Disease for Oncology                                            | 3               | 2 ?/2000             |                  |                      |
| A&E          | A&E clinical diagnosis (3 char)                                                                 | Unk             | N/A                  |                  |                      |
| BIRTHWEIGHT  | Birthweight (e.g. low <2500g)                                                                   | 1               |                      |                  |                      |
 
* rif40.rif40_outcome_groups: These defines field naming conventions for outcome types. Multiple field count is **NOT** currently supported. Currently only ICD (in version 10) is supported.
  When multiple field count is supported the field are named &lt;field name&gt;_01 to &lt;field name&gt;&lt;multiple field count padded to 2 digits&gt;. So for *MORTALITY_MULTIPLE_ICD* the ICD field names 
  would be ICD_SAHSU_01 to ICD_SAHSU_20. These field names **MUST** exist in the numerator table (this restriction may be removed).

| outcome_type |   outcome_group_name    |                      outcome_group_description                       |  field_name   | multiple_field_count |
|--------------|-------------------------|----------------------------------------------------------------------|---------------|----------------------|
| ICD          | SINGLE_ICD              | Single ICD                                                           | ICD_SAHSU_01  |                    0 |
| ICD          | MORTALITY_SECONDARY_ICD | Single ICD - Secondary cause of Death                                | ICD_SAHSU_01S |                    0 |
| ICD          | MORTALITY_MULTIPLE_ICD  | ONS Mortality multiple ICD                                           | ICD_SAHSU     |                   16 |
| ICD          | HES_MULTIPLE_ICD        | HES multiple ICD                                                     | ICD_SAHSU     |                   20 |
| A&E          | HES_SINGLE_A&E          | HES single A+E                                                       | DIAG          |                    0 |
| A&E          | HES_MULTIPLE_A&E        | HES multiple A+E                                                     | DIAG          |                   20 |
| OPCS         | HES_SINGLE_OPCS         | HES single OPCS                                                      | OPCS_SAHSU    |                    0 |
| OPCS         | HES_MULTIPLE_OPCS       | HES multiple OPCS                                                    | OPCS_SAHSU    |                   20 |
| ICD-O        | CANCER_ICD_O            | Cancer type of growth histology coded to Classification of Neoplasms | TYPE_GROWTH   |                    0 |
| ICD          | SAHSULAND_ICD           | Single ICD                                                           | ICD           |                    0 |
| BIRTHWEIGHT  | BIRTHWEIGHT             | Birthweight (e.g. low <2500g)                                        | BIRTHWEIGHT   |                    0 |

# 6. Quality Control

## 6.1 Extract Warnings  

The table/view *t_rif40_warnings/rif40_warnings* will contain contain warning messages on a study basis. These can be created 
by extract or R scripts. Traps will be added for:

* Out of range or null covariates by year;
* Missing years of numerator or denominator data;
* Males/females not present when requested in numerator or denominator; 
* ICD codes not present when requested in numerator;
* Mal-join detection

Peter Hambly
May 2018
