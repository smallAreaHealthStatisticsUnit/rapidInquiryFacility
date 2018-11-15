---
layout: default
title: RIF Data Loading
---

1. Contents
{:toc}

# Overview

This document details the manual process for the loading of data into the RIF. The RIF requires the following types of data:

* Numerator data. This may be in individual record form or aggregated to a suitable administrative geography.
  Individual records are assumed to have been de-identified into a suitable pseudonymous form; all this is
  not a requirement.
* Denominator data. This is in aggregate form.
* Covariate data. This is again in aggregate form and has been quantilised
* Administrative geography. This is created by the Tile-Maker tool; this document details the loading of
  the administrative geography data and the geo-coding requirements of the RIF. See the [tile-maker manual]({{ site.baseurl }}/rifNodeServices/tileMaker) for how to create
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
	[RIF Data Loader Manual]({{ site.baseurl }}/source-documents/RIF%20Data%20Loader%20Manual.pdf).
	The data loader tools is still in development and is not expected to be complete until 2019.
  * Manually via a user created script. Again an example is provided using the US SEER Cancer Registry data with example
    scripts for both Postgres and SQL Server.

# RIF Capabilities

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

# Limitations

Limitations in the current RIF

* No support for covariates embedded in numerator or denominator data. Data must be extracted into a separate
  covariate table. Covariates must be merged into a single table and disaggregated (if required) by year. It is not planned to remove these
  restrictions which were in the previous RIF.
* Covariates must be quantilised. Support for continuous variable covariates (i.e. on the fly quantilisation) may be added in future releases.
* Denominator data is always used in indirect standardisation. There is no support currently in the RIF for
  direct standardisation using standard populations. This was supported in previous versions of the RIF and will be put
  back if required;
* No support for ad-hoc SQL. This functionality will be partially re-implemented in future releases using user specified conditions
  (pre defined groups). The feature was removed as it cannot be implemented in a secure manner (i.e. it permits SQL injection attacks);
* Numerators are currently limited to ICD 9 and 10 coding only. Support will be added for ICD 11 (subject to the release of the 11th Edition in June 2018)

  In the longer term it is expected that support will be added for:

  * ICD oncology O(ICD-O-1)
  * UK HES oper and A+E codes
  * User specified conditions (pre defined groups), e.g. Low birthweight, complex groups of ICD codes, the all record condition (the
    *1=1* ad-hoc SQL filter in the previous RIF)

* Single ICD field name. Unlike the old RIF the field name is configurable;
* The RIF currently lacks complex support for Information Governance beyond having strong role based permissions.
* No support for population weighted centroids.

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
* Population weighted centroids; also add the ability to import population weighted or spatially processed (centroid pulled to within
  the centroid boundary) centroids into an administrative geography.

Like the old RIF there are field naming restrictions:

* AGE_SEX_GROUP. Use of AGE and SEX is not permitted; the column must be called *AGE_SEX_GROUP*;
* YEAR

THe ICD field name is now configurable. The default is *ICD_SAHSU_01*.

## Data Loader Tool

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
[RIF Data Loader Manual]({{ site.baseurl }}/source-documents/RIF%20Data%20Loader%20Manual.pdf)

# RIF Data Loading Prerequisites

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

## Postgres

Postgres uses the *\copy* command to load and unload data. *\copy* cannot handle fixed length data; this
is loaded as a fixed length string and parsed using SQL.

The SEER data load requires USA load phase data pg_USQ_2014.sql to be loaded as a RIF user (not rif40) and
the production data (rif_pg_usa_2014.sql) needs to be loaded into the rif40 account in the rif_data schema.

## SQL Server

### BCP Access Permissions

SQL Server needs access permission granted to the directories used to `BULK INSERT` files, the files are not copied from the client to the
server as in the *Postgres* *psql* ```\copy` command and the *Oracle* *sqlldr* command. This also implies that a full file path is required and the file name must be accessible on the
SQL Server server; it does **NOT** have to be accessible on the client.

SQL Server needs access to these directories. The simplest
way is to allow read/execute access to the local users group (e.g. PH-LAPTOP\Users or USERS depending on your Windows version).

**DO NOT TRY TO RUN BULK INSERT FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).** Use a local directory which SQL Server has
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

### BCP Format Issues

BULK LOAD and BCP cannot handle:

* Text fields with a mixture of quote enclosing and no quote enclosing;
* Exponential notation e.g. ```3.12E-5```
* A database of *SQLDECIMAL* with no *PRECISION* and *SCALE* specified will convert the input to an **INTEGER** transparently! Make 
  sure to use:
  ```
   <COLUMN SOURCE="4" NAME="population" xsi:type="SQLDECIMAL" PRECISION="7" SCALE="2" />```
  ```
  
## Data Structure

All RIF data tables are located in the *rif_data* schema. They must be located in *rif_data* because SQL Server does not have the concept of a schema search
path; the search paths have been hard coded. Tables may be located in any tablespace and you may use views which again must be located in the *rif_data* schema.

The RIF also support views for numerator and denominator tables. This allows for considerable flexibility in configuration as:

* Tables can be UNIONed together;
* Views can aggregate and join data;
* Field can be renamed and data types coerced;
* Tables can be located in different schemas;
* Impose Information Governance restrictions

All table and column names must be a valid [Oracle] database name - 30 characters, uppercase, A-Z, 0-9 and underscore (_) and start with a letter. In some cases
the length is reduced to 20 characters so that derived names of indexes, primary and foreign keys are under the 30 character limit.

The middleware translates these names into the correct format for the database software port. Names must *NOT* have schemas appended.

### Numerator

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
* *AGE_SEX_GROUP*. This contains an age sex group field name, e.g. ```M5_9```. See: [Age groups](#age-groups);
* *&lt;outcome groups field name&gt;* e.g. ```icd``` for the OUTCOME_GROUP_NAME **SAHSULAND_ICD**. See: [ICD field name](#icd-field-name);
* *&lt;one geolevel field name for each geography geolevel&gt;* e.g. ```sahsu_grd_level1```. See: [Administrative geography](#administrative-geography);
* *&lt;total field name&gt;* e.g. ```total```. If this is null, the &lt;outcome groups field name&gt; e.g. ```icd``` is **COUNT**ed (i.e. the table is disaggregated).
  If not null, the &lt;total field name&gt; is summed.

To add a numerator table to the RIF if must be added to:

* *rif40_tables*:
  
  |       column_name        |                                                                                                                                                                                                                                                                 description                                                                                                                                                                                                                                                                  |          data_type          |
  |--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
  | theme                    | Health Study theme. Link to RIF40_HEALTH_STUDY_THEMES. See: [Health theme](#health-themes);                                                                                                                                                                                                                                                                                                                   | varchar(30)                 |
  | table_name               | RIF table name. Normally the schema owner will not be able to see the health data tables, so no error is raised if the table cannot be resolved to an acceisble object. The schema owner must have access to automatic indirect standardisation denominators.                                                                                                                                                                                                                                                                                | varchar(30)                 |
  | description              | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | varchar(250)                |
  | year_start               | Year table starts                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | smallint                    |
  | year_stop                | Year table stops                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | smallint                    |
  | total_field              | Total field (when used aggregated tables)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | varchar(30)                 |
  | isindirectdenominator    | Is table a denominator to be used in indirect standardisation (0/1). Must **ALWAYS** be 1 for denominators.                                                                                                                                                                                                                                                                                                                                                                                                                                  | smallint                    |
  | isdirectdenominator      | Is table a denominator to be used in direct standardisation (0/1). E.g. POP_WORLD, POP_EUROPE. Must **ALWAYS** be 0.                                                                                                                                                                                                                                                                                                                                                                                                                         | smallint                    |
  | isnumerator              | Is table a numerator  (0/1)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | smallint                    |
  | automatic                | Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). Cannot be applied to a direct standardisation denominator. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having &gt;1 pair per numerator. This restriction is actually enforced in RIF40_NUM_DENOM because of the &quot;ORA-04091: table RIF40.RIF40_TABLES is mutating, trigger/function may not see it&quot; error. A user specific T_RIF40_NUM_DENOM is supplied for other combinations. The default is 0 because of the restrictions | smallint                    |
  | sex_field_name           | Name of SEX field. No default. AGE_GROUP_FIELD_NAME must be set, AGE_SEX_GROUP_FIELD_NAME must not be set. Not currently used.                                                                                                                                                                                                                                                                                                                                                                                                               | varchar(30)                 |
  | age_group_field_name     | Name of AGE_GROUP field. No default. SEX_FIELD_NAME must be set, AGE_SEX_GROUP_FIELD_NAME must not be set. Not currently used.                                                                                                                                                                                                                                                                                                                                                                                                               | varchar(30)                 |
  | age_sex_group_field_name | Name of AGE_SEX_GROUP field. Default: AGE_SEX_GROUP; AGE_GROUP_FIELD_NAME and SEX_FIELD_NAME must not be set.                                                                                                                                                                                                                                                                                                                                                                                                                                | varchar(30)                 |
  | age_group_id             | Type of RIF age group in use. Link to RIF40_AGE_GROUP_NAMES. No default. See: [Age groups](#age-groups);                                                                                                                                                                                                                                                                                                       | smallint                    |
  | validation_date          | Date table contents were validated OK. Availabel for use by quality control programs                                                                                                                                                                                                                                                                                                                                                                                                                                                         | timestamp without time zone |

  Example: 
  
  |  theme  |      table_name      |      description       | year_start | year_stop | total_field | isindirectdenominator | isdirectdenominator | isnumerator | automatic | age_sex_group_field_name | age_group_id |
  |---------|----------------------|------------------------|------------|-----------|-------------|-----------------------|---------------------|-------------|-----------|--------------------------|--------------|
  | cancers | NUM_SAHSULAND_CANCER | cancer numerator       |       1989 |      2016 | TOTAL       |                     0 |                   0 |           1 |         1 | AGE_SEX_GROUP            |            1 |

  So, for the example numerator table *num_sahsuland_cancer*:

  * The *theme* is a reference to *RIF40_HEALTH_STUDY_THEMES.THEME*. See:
    [Health theme](#health-themes);
  * The table name **MUST** be in upper case. Do not add the schema owner;
  * Year stop and start refer to the first and last years of numerator data;
  * The *total* field, if not null, **MUST** be in upper case;
  * *isindirectdenominator*, *isdirectdenominator* are "0"; *isnumerator* is "1";
  * *automatic* is normally "1" unless you have >1 denominator for the geography when it is "0".
    See: [Automatic numerator denominator pairs](#automatic-numerator-denominator-pairs);
  * *age_sex_group_field_name* **MUST** be *AGE_SEX_GROUP*;
  * The *age_group_id* refers to **rif40.rif40_age_group_names**. The standard 21 age groups used by SAHSU is "1".
    See: [Age groups](#age-groups);

* *rif40.rif40_table_outcomes**: see: [ICD field name](#icd-field-name);

| outcome_group_name |      numer_tab       | current_version_start_year |
|--------------------|----------------------|----------------------------|
| SAHSULAND_ICD      | NUM_SAHSULAND_CANCER |                       1989 |

### Denominator

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
* *AGE_SEX_GROUP*. This contains an age sex group field name, e.g. ```M5_9```. See: [Age groups](#age-groups);
* *&lt;one geolevel field name for each geography geolevel&gt;* e.g. ```sahsu_grd_level1```. See: [Administrative geography](#administrative-geography);
* *&lt;total field name&gt;* e.g. ```total```. This field is summed.

To add a denominator table to the RIF if must be added to:

* *rif40_tables*:

|  theme  |      table_name      |      description       | year_start | year_stop | total_field | isindirectdenominator | isdirectdenominator | isnumerator | automatic | age_sex_group_field_name | age_group_id |
|---------|----------------------|------------------------|------------|-----------|-------------|-----------------------|---------------------|-------------|-----------|--------------------------|--------------|
| cancers | POP_SAHSULAND_POP    | population health file |       1989 |      2016 | TOTAL       |                     1 |                   0 |           0 |         1 | AGE_SEX_GROUP            |            1 |

So, for the example denominator table *pop_sahsuand_pop*:

* The *theme* is a reference to *RIF40_HEALTH_STUDY_THEMES.THEME*. See:
  [Health theme](#health-themes);
* The table name **MUST** be in upper case. Do not add the schema owner;
* Year stop and start refer to the first and last years of denominator data;
* The *total* field, if not null, **MUST** be in upper case;
* *isindirectdenominator* is "1", *isdirectdenominator* and *isnumerator* are "0";
* *automatic* is normally "1" unless you have >1 denominator for the geography when it is "0".
  See: [Automatic numerator denominator pairs](#automatic-numerator-denominator-pairs);
* *age_sex_group_field_name* **MUST** be *AGE_SEX_GROUP*;
* The *age_group_id* refers to **rif40.rif40_age_group_names**. The standard 21 age groups used by SAHSU is "1".
  See: [Age groups](#age-groups);

### Covariates

The covariate tables with one table per geolevel for additional covariates within a geography (e.g. social exclusion scores).
The table is defined in *rif40_geolevels.covariate_table*.

Covariate tables must contain the following fields (as in the *covar_sahsuland_covariates3* example below):

* Year this should cover the range of the associated denominator and must be an integer;
* *&lt;geolevel field name&gt;* e.g. ```sahsu_grd_level4```.
  See: [Administrative geography](#administrative-geography);
* One or more *&lt;covariate name&gt;* fields e.g. ```ses```. These can be either an integer or a numeric. Currently only quantilised fields are supported; continuous variables are not supported.

Unlike other administrative geography tables; RIF managers **MUST** create these tables for themselves. A full range of all possible values **MUST** be provided,
even if the covariate data is null. So in the example below

|   column_name    |              description               |  data_type  |
|------------------|----------------------------------------|-------------|
| year             | year field                             | integer     |
| sahsu_grd_level3 | third level of geographical resolution | varchar(20) |
| ses              | socio-economic status                  | integer     |
| ethnicity        | ethnicity                              | integer     |

As an example the covariate table *covar_sahsuland_covariates3* contains (with rows truncated):

| year | sahsu_grd_level3 | ses | ethnicity |
|------|------------------|-----|-----------|
| 1989 | 01.001.000100    |   4 |         1 |
| 1989 | 01.001.000200    |   5 |         1 |
| 1989 | 01.001.000300    |   5 |         3 |
| 1989 | 01.002.000300    |   2 |         2 |
| 1989 | 01.002.000400    |   5 |         3 |
| 1989 | 01.002.000500    |   5 |         3 |
| 1989 | 01.002.000600    |   4 |         3 |
| 1989 | 01.002.000700    |   5 |         3 |
| 1989 | 01.002.000800    |   4 |         3 |
| 1989 | 01.002.000900    |   1 |         2 |
| ...  | ...              | ... | ...       |

### Administrative Geography

RIF administrative geography has nine components:

* [Geography](#geography): name of the administrative geography;
* [Geolevels](#geolevels): a hierarchy of administrative areas that define a geography where each higher resolution contains one or more areas that fit exactly within the lower resolution;
* [Lookup tables](#lookup-tables): for the names of the administrative area id codes within a geolevel;
* [Tile tables](#tile-tables): the WGS84 topoJSON and geoJSON tiles for a geography used by the RIF front end and extract utilities to display administrative geography geolevels;
* [Adjacency tables](#adjacency-tables): a list of adjacent areas for each geolevel and area id in a geography used in BAtesian smoothing;
* [Hierarchy tables](#-hierarchy-tables): one table per administrative geography; contains a hierarchy of higher resolution of one or more areas that fit exactly within the lower resolution

The following components are used by the administrative geography preprocessing (tile maker):

* [Geometry tables](#geometry-tables): geometric data for a geography to allow the database to perform spatial queries with the administrative geometry;
* [Shapefile, shapefile tables](#shapefile-shapefile-tables): Storage for the original shapefile data and name used as part of the administrative geography preprocessing (tile maker);

The final component is in the TODO list for future additions:

* [Centroids tables](#centroids-tables): tables of centroids tables. Used to import population weighted or spatially processed (centroid pulled to within the centroid boundary) centroids into an administrative geography;

**All components are created by the administrative geography preprocessing (tile maker) and loaded by a SQL Server ```sqlcmd``` or Postgres ```psql``` script.**

#### Geography

The table *rif40.rif40_geographies* is used to define one or more administrative geographies

|       column_name       |                                                                                                                                                                                                                           description                                                                                                                                                                                                                           |  data_type   |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| geography               | Geography name                                                                                                                                                                                                                                                                                                                                                                                                                                                  | varchar(50)  |
| description             | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                     | varchar(250) |
| hierarchytable          | Hierarchy table                                                                                                                                                                                                                                                                                                                                                                                                                                                 | varchar(30)  |
| srid                    | Postgres projection SRID                                                                                                                                                                                                                                                                                                                                                                                                                                        | integer      |
| defaultcomparea         | Default comparison area                                                                                                                                                                                                                                                                                                                                                                                                                                         | varchar(30)  |
| defaultstudyarea        | Default study area                                                                                                                                                                                                                                                                                                                                                                                                                                              | varchar(30)  |
| postal_population_table | Postal population table. Table of postal points (e.g. postcodes, ZIP codes); geolevels; X and YCOORDINATES (in projection SRID); male, female and total populations. Converted to SRID points by loader [not in 4326 Web Mercator lat/long]. Used in creating population weight centroids and in converting postal points to geolevels. Expected columns &lt;postal_point_column&gt;, XCOORDINATE, YCOORDINATE, 1+ &lt;GEOLEVEL_NAME&gt;, MALES, FEMALES, TOTAL | varchar(30)  |
| postal_point_column     | Column name for postal points (e.g. POSTCODE, ZIP_CODE)                                                                                                                                                                                                                                                                                                                                                                                                         | varchar(30)  |
| partition               | Enable partitioning. Extract tables will be partition if the number of years >= 2x the RIF40_PARAMETERS parameters Parallelisation [which has a default of 4, so extracts covering 8 years or more will be partitioned].                                                                                                                                                                                                                                        | smallint     |
| max_geojson_digits      | Max digits in ST_AsGeoJson() [optimises file size by removing unnecessary precision, the default value of 8 is normally fine.]                                                                                                                                                                                                                                                                                                                                  | smallint     |
| geometrytable           | Geometry table name                                                                                                                                                                                                                                                                                                                                                                                                                                             | varchar(30)  |
| tiletable               | Tile table name                                                                                                                                                                                                                                                                                                                                                                                                                                                 | varchar(30)  |
| minzoomlevel            | Minimum zoomlevel                                                                                                                                                                                                                                                                                                                                                                                                                                               | integer      |
| maxzoomlevel            | Maximum zoomlevel                                                                                                                                                                                                                                                                                                                                                                                                                                               | integer      |
| adjacencytable          | Adjacency table                                                                                                                                                                                                                                                                                                                                                                                                                                                 | varchar(30)  |

* Currently partitioning is not enabled on SQL Server due to licensing restrictions and on Postgres use is limited to the geometry table. Postgres partitioning uses the
  version 9 and before inheritance method. Partitioning is being progressively implemented in Postgres 10;
* *postal_population_table* and *postal_point_column* are for using postcodes or ZIP codes as the centre of a risk analysis circle and are not currently used.

The supplied *SAHUSLAND* test database is set up as follows:

| geography |       description       |   hierarchytable    | srid  | defaultcomparea  | defaultstudyarea | partition | max_geojson_digits |   geometrytable    |    tiletable    | minzoomlevel | maxzoomlevel |   adjacencytable    |
|-----------|-------------------------|---------------------|-------|------------------|------------------|-----------|--------------------|--------------------|-----------------|--------------|--------------|---------------------|
| SAHSULAND | SAHSU Example geography | HIERARCHY_SAHSULAND | 27700 | SAHSU_GRD_LEVEL1 | SAHSU_GRD_LEVEL3 |         1 |                  6 | GEOMETRY_SAHSULAND | TILES_SAHSULAND |            6 |           11 | ADJACENCY_SAHSULAND |

The example *SEER* test database is set up as follows:

| geography |       description                        |   hierarchytable    | srid  | defaultcomparea      | defaultstudyarea      | partition | max_geojson_digits |   geometrytable    |    tiletable    | minzoomlevel | maxzoomlevel |   adjacencytable    |
|-----------|------------------------------------------|---------------------|-------|----------------------|-----------------------|-----------|--------------------|--------------------|-----------------|--------------|--------------|---------------------|
| USA_2014  | US 2014 Census geography to county level | HIERARCHY_USA_2014  |  4269 | CB_2014_US_NATION_5M | CB_2014_US_STATE_500K |         1 |                  6 | GEOMETRY_USA_2014  | TILES_USA_2014  |            6 |            9 | ADJACENCY_USA_2014  |

#### Geolevels

The table and view *rif40.t_rif40_geolevels*/*rif40.rif40_geolevels* is used to define a hierarchy of administrative areas that define a geography where each higher resolution contains
one or more areas that fit exactly within the lower resolution. Users with the *rif_student* role cannot see the restricted geolevels.

|        column_name         |                                                                                                                                                                          description                                                                                                                                                                           |  data_type   |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| geography                  | Geography (e.g EW2001)                                                                                                                                                                                                                                                                                                                                         | varchar(50)  |
| geolevel_name              | Name of geolevel. This will be a column name in the numerator/denominator tables                                                                                                                                                                                                                                                                               | varchar(30)  |
| geolevel_id                | ID for ordering (1=lowest resolution). Up to 99 supported.                                                                                                                                                                                                                                                                                                     | smallint     |
| description                | Description                                                                                                                                                                                                                                                                                                                                                    | varchar(250) |
| lookup_table               | Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is &quot;Westminster&quot;                                                                                                                                                                                                                                         | varchar(30)  |
| lookup_desc_column         | Lookup table description column name.                                                                                                                                                                                                                                                                                                                          | varchar(30)  |
| centroidxcoordinate_column | Lookup table centroid X co-ordinate column name. Can also use CENTROIDSFILE instead.                                                                                                                                                                                                                                                                           | varchar(30)  |
| centroidycoordinate_column | Lookup table centroid Y co-ordinate column name.                                                                                                                                                                                                                                                                                                               | varchar(30)  |
| shapefile                  | Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead,                                                                                                                                                                                                                                                       | varchar(512) |
| centroidsfile              | Location of the GIS centroids file. Can also use CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN instead.                                                                                                                                                                                                                                               | varchar(512) |
| shapefile_table            | Table containing GIS shape file data (created using shp2pgsql).                                                                                                                                                                                                                                                                                                | varchar(30)  |
| shapefile_area_id_column   | Column containing the AREA_IDs in SHAPEFILE_TABLE                                                                                                                                                                                                                                                                                                              | varchar(30)  |
| shapefile_desc_column      | Column containing the AREA_ID descriptions in SHAPEFILE_TABLE                                                                                                                                                                                                                                                                                                  | varchar(30)  |
| centroids_table            | Table containing GIS shape file data with Arc GIS calculated population weighted centroids (created using shp2pgsql). PostGIS does not support population weighted centroids.                                                                                                                                                                                  | varchar(30)  |
| centroids_area_id_column   | Column containing the AREA_IDs in CENTROIDS_TABLE. X and Y co-ordinates columns are assumed to be named after CENTROIDXCOORDINATE_COLUMN and CENTROIDYCOORDINATE_COLUMN.                                                                                                                                                                                       | varchar(30)  |
| avg_npoints_geom           | Average number of points in a geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.                                                                                                                                                                                                                                               | bigint       |
| avg_npoints_opt            | Average number of points in a ST_SimplifyPreserveTopology() optimised geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.                                                                                                                                                                                                       | bigint       |
| file_geojson_len           | File length estimate (in bytes) for conversion of the entire geolevel geometry to GeoJSON. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.                                                                                                                                                                                                             | bigint       |
| leg_geom                   | The average length (in projection units - usually metres) of a vector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.                                                                                                                                                                                                                             | Numeric      |
| leg_opt                    | The average length (in projection units - usually metres) of a ST_SimplifyPreserveTopology() optimised geometry vector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.                                                                                                                                                                            | Numeric      |
| covariate_table            | Name of table used for covariates at this geolevel                                                                                                                                                                                                                                                                                                             | varchar(30)  |
| restricted                 | Is geolevel access restricted by Information Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. This is enforced by the RIF application. | smallint     |
| resolution                 | Can use a map for selection at this resolution (0/1)                                                                                                                                                                                                                                                                                                           | smallint     |
| comparea                   | Able to be used as a comparison area (0/1)                                                                                                                                                                                                                                                                                                                     | smallint     |
| listing                    | Able to be used in a disease map listing (0/1)                                                                                                                                                                                                                                                                                                                 | smallint     |
| areaid_count               | Area ID count                                                                                                                                                                                                                                                                                                                                                  | integer      |

* *centroids_table*, *centroids_area_id_column*, *centroidxcoordinate_column* and *centroidycoordinate_column* are used to import population weighted or spatially processed (centroid pulled to within
   the centroid boundary) centroids into an administrative geography. This is not currently supported;
* *avg_npoints_geom*, *avg_npoints_opt*, *file_geojson_len*, *leg_geom* and *leg_opt* are used as part of the administrative geography preprocessing (tile maker);
* Normally *resolution*, *comparea* and *listing* are all set to "1". Setting to "0" restricts the RIF front end as follows:

  * *resolution*: cannot use the map for selection at this resolution for either study or comparison area;                                                                                                                                                                                                                                                             
  * *comparea*:cannot be to be used as a comparison area;                                                                                                                                                                                                                                                                                                              
  * *listing*: cannot be in a list of areas when setting up a disease map study or comparison area.

The supplied *SAHUSLAND* test database is set up as follows:

| geography |  geolevel_name   | geolevel_id |     description     |      lookup_table       | lookup_desc_column |      shapefile       | centroidsfile | shapefile_table  | shapefile_area_id_column | shapefile_desc_column |       covariate_table       | resolution | comparea | listing | restricted | areaid_count |
|-----------|------------------|-------------|---------------------|-------------------------|--------------------|----------------------|---------------|------------------|--------------------------|-----------------------|-----------------------------|------------|----------|---------|------------|--------------|
| SAHSULAND | SAHSU_GRD_LEVEL4 |           4 | Level 4             | LOOKUP_SAHSU_GRD_LEVEL4 | AREANAME           | SAHSU_GRD_Level4.shp |               | SAHSU_GRD_LEVEL4 | LEVEL4                   | LEVEL4                | COVAR_SAHSULAND_COVARIATES4 |          1 |        1 |       1 |          0 |         1230 |
| SAHSULAND | SAHSU_GRD_LEVEL3 |           3 | Level 3             | LOOKUP_SAHSU_GRD_LEVEL3 | AREANAME           | SAHSU_GRD_Level3.shp |               | SAHSU_GRD_LEVEL3 | LEVEL3                   | LEVEL3                | COVAR_SAHSULAND_COVARIATES3 |          1 |        1 |       1 |          0 |          200 |
| SAHSULAND | SAHSU_GRD_LEVEL2 |           2 | Level 2             | LOOKUP_SAHSU_GRD_LEVEL2 | AREANAME           | SAHSU_GRD_Level2.shp |               | SAHSU_GRD_LEVEL2 | LEVEL2                   | NAME                  | COV_SAHSU_GRD_LEVEL2        |          1 |        1 |       1 |          0 |           17 |
| SAHSULAND | SAHSU_GRD_LEVEL1 |           1 | Level 1 (top level) | LOOKUP_SAHSU_GRD_LEVEL1 | AREANAME           | SAHSU_GRD_Level1.shp |               | SAHSU_GRD_LEVEL1 | LEVEL1                   | LEVEL1                |                             |          1 |        1 |       1 |          0 |            1 |

The field *geolevel_id* is used to order the geolevels by resolution. The highest *geolevel_id* is the highest resolution (i.e. has the most areas).

The example *SEER* test database is set up as follows:

| geography |  geolevel_name         | geolevel_id |     description                      |      lookup_table             | lookup_desc_column |      shapefile             | centroidsfile | shapefile_table        | shapefile_area_id_column | shapefile_desc_column |       covariate_table      | resolution | comparea | listing | restricted | areaid_count |
|-----------|------------------------|-------------|--------------------------------------|-------------------------------|--------------------|----------------------------|---------------|------------------------|--------------------------|-----------------------|----------------------------|------------|----------|---------|------------|--------------|
| USA_2014  | CB_2014_US_COUNTY_500K |           3 | The County at a scale of 1:500,000   | LOOKUP_CB_2014_US_COUNTY_500K | AREANAME           | cb_2014_us_county_500k.shp |               | CB_2014_US_COUNTY_500K | COUNTYNS                 | NAME                  | COV_CB_2014_US_COUNTY_500K |          1 |        1 |       1 |          0 |         3233 |
| USA_2014  | CB_2014_US_STATE_500K  |           2 | The State at a scale of 1:500,000    | LOOKUP_CB_2014_US_STATE_500K  | AREANAME           | cb_2014_us_state_500k.shp  |               | CB_2014_US_STATE_500K  | STATENS                  | NAME                  | COV_CB_2014_US_STATE_500K  |          1 |        1 |       1 |          0 |           56 |
| USA_2014  | CB_2014_US_NATION_5M   |           1 | The nation at a scale of 1:5,000,000 | LOOKUP_CB_2014_US_NATION_5M   | AREANAME           | cb_2014_us_nation_5m.shp   |               | CB_2014_US_NATION_5M   | GEOID                    | NAME                  |                            |            |          1 |        1 |       1 |            1 |

#### Lookup tables

Lookup tables contain the names of the administrative area id codes within a geolevel and must have:

* The area id column relevant to the geolevel. It must be named the same as the &lt'geolevel_name&gt;;
* A unique *gid* field;
* A descriptive field defined in *rif40_geolevels.lookup_desc_column*.
* A geographic centroid in GEoJSON format, SGS84 geometry;

Lookup table *lookup_sahsu_grd_level2*;

|     column_name     |     description     |   data_type   |
|---------------------|---------------------|---------------|
| sahsu_grd_level2    | Area ID field       | varchar(100)  |
| areaname            | Area Name field     | varchar(1000) |
| gid                 | GID field           | integer       |
| geographic_centroid | Geographic centroid | json          |

As an example the lookup table *lookup_sahsu_grd_level2* contains:

| sahsu_grd_level2 |  areaname  | gid |                         geographic_centroid                         |
|------------------|------------|-----|---------------------------------------------------------------------|
| 01.001           | Abellan    |   1 | {"type":"Point","coordinates":[-6.36447811663261,55.1846108882703]} |
| 01.002           | Cobley     |   2 | {"type":"Point","coordinates":[-6.72944498678687,54.9861042071018]} |
| 01.003           | Beale      |   3 | {"type":"Point","coordinates":[-7.30272703673231,54.8380270006204]} |
| 01.004           | Hambly     |   4 | {"type":"Point","coordinates":[-6.49752751099877,54.8431984603293]} |
| 01.005           | Briggs     |   5 | {"type":"Point","coordinates":[-6.12712384427312,54.9266239071054]} |
| 01.006           | Andersson  |   6 | {"type":"Point","coordinates":[-5.69842684960186,54.8976428162187]} |
| 01.007           | Hodgson    |   7 | {"type":"Point","coordinates":[-6.89721995908163,54.0867635909076]} |
| 01.008           | Jarup      |   8 | {"type":"Point","coordinates":[-6.54372797176735,54.4006825147175]} |
| 01.009           | Elliot     |   9 | {"type":"Point","coordinates":[-6.1142402340227,54.4434524682342]}  |
| 01.011           | Clarke     |  10 | {"type":"Point","coordinates":[-6.23842742452612,54.1627636409006]} |
| 01.012           | Tirado     |  11 | {"type":"Point","coordinates":[-5.77869692964237,54.2166512827777]} |
| 01.013           | Kozniewska |  12 | {"type":"Point","coordinates":[-6.52542157743406,53.8402864247415]} |
| 01.014           | Stordy     |  13 | {"type":"Point","coordinates":[-7.24669171811797,53.2995738304985]} |
| 01.015           | Maitland   |  14 | {"type":"Point","coordinates":[-6.68411239150798,53.6052365920332]} |
| 01.016           | De Hoogh   |  15 | {"type":"Point","coordinates":[-5.57639756386895,53.7060196095887]} |
| 01.017           | Savigny    |  16 | {"type":"Point","coordinates":[-6.99828886177757,53.0298596972378]} |
| 01.018           | Cockings   |  17 | {"type":"Point","coordinates":[-5.93617635675363,53.1514779844403]} |

And for USA states:

| cb_2014_us_state_500k |                   areaname                   | gid |                         geographic_centroid                          |
|-----------------------|----------------------------------------------|-----|----------------------------------------------------------------------|
| 00068085              | Arkansas                                     |   1 | {"type":"Point","coordinates":[-91.7460600676377,34.7989664318261]}  |
| 00294478              | Florida                                      |   2 | {"type":"Point","coordinates":[-82.191285629571,27.2641344923978]}   |
| 00448508              | Indiana                                      |   3 | {"type":"Point","coordinates":[-86.6396085844172,39.2583984174708]}  |
| 00481813              | Kansas                                       |   4 | {"type":"Point","coordinates":[-96.9603123072795,38.7478452695085]}  |
| 00606926              | Massachusetts                                |   5 | {"type":"Point","coordinates":[-70.8496779292619,41.9574785877662]}  |
| 00662849              | Minnesota                                    |   6 | {"type":"Point","coordinates":[-94.2946075529846,46.9142548993034]}  |
| 00767982              | Montana                                      |   7 | {"type":"Point","coordinates":[-112.440158950593,46.0953789167559]}  |
| 00897535              | New Mexico                                   |   8 | {"type":"Point","coordinates":[-106.210319618421,34.0796842247757]}  |
| 01027616              | North Carolina                               |   9 | {"type":"Point","coordinates":[-78.1838147583888,35.4144296092622]}  |
| 01085497              | Ohio                                         |  10 | {"type":"Point","coordinates":[-82.4309672991619,40.379873178333]}   |
| 01102857              | Oklahoma                                     |  11 | {"type":"Point","coordinates":[-97.1409457095718,34.712480112523]}   |
| 01155107              | Oregon                                       |  12 | {"type":"Point","coordinates":[-121.26568196262,44.5161240770227]}   |
| 01219835              | Rhode Island                                 |  13 | {"type":"Point","coordinates":[-71.3998545840162,41.5702071077978]}  |
| 01325873              | Tennessee                                    |  14 | {"type":"Point","coordinates":[-85.8408130481816,35.8337160114235]}  |
| 01455989              | Utah                                         |  15 | {"type":"Point","coordinates":[-111.621001625722,39.1152289226468]}  |
| 01629543              | Louisiana                                    |  16 | {"type":"Point","coordinates":[-91.2369599329216,30.3596077655874]}  |
| 01702382              | District of Columbia                         |  17 | {"type":"Point","coordinates":[-77.0161156802619,38.899287485358]}   |
| 01705317              | Georgia                                      |  18 | {"type":"Point","coordinates":[-82.8985467943147,32.6450260988439]}  |
| 01714934              | Maryland                                     |  19 | {"type":"Point","coordinates":[-76.5695402961508,38.7769267877812]}  |
| 01779775              | Alabama                                      |  20 | {"type":"Point","coordinates":[-86.7664800530139,31.7690551760402]}  |
| 01779777              | Arizona                                      |  21 | {"type":"Point","coordinates":[-113.396230811877,34.458696450714]}   |
| 01779778              | California                                   |  22 | {"type":"Point","coordinates":[-120.249510325325,36.2457149373203]}  |
| 01779779              | Colorado                                     |  23 | {"type":"Point","coordinates":[-104.808380671455,38.9255583779329]}  |
| 01779780              | Connecticut                                  |  24 | {"type":"Point","coordinates":[-72.8528546706635,41.3456700453746]}  |
| 01779781              | Delaware                                     |  25 | {"type":"Point","coordinates":[-75.4994397114792,39.2171211171611]}  |
| 01779782              | Hawaii                                       |  26 | {"type":"Point","coordinates":[-157.752347796731,21.0611974598024]}  |
| 01779783              | Idaho                                        |  27 | {"type":"Point","coordinates":[-114.675046469887,45.4195058476719]}  |
| 01779784              | Illinois                                     |  28 | {"type":"Point","coordinates":[-89.2220209830065,39.8410669123454]}  |
| 01779785              | Iowa                                         |  29 | {"type":"Point","coordinates":[-93.8227666555831,42.0221134891659]}  |
| 01779786              | Kentucky                                     |  30 | {"type":"Point","coordinates":[-85.2123467005517,37.7223857693082]}  |
| 01779787              | Maine                                        |  31 | {"type":"Point","coordinates":[-68.9047268601883,44.5331050274351]}  |
| 01779789              | Michigan                                     |  32 | {"type":"Point","coordinates":[-85.7125400934235,45.4651010832855]}  |
| 01779790              | Mississippi                                  |  33 | {"type":"Point","coordinates":[-90.2098738969815,32.2488403702241]}  |
| 01779791              | Missouri                                     |  34 | {"type":"Point","coordinates":[-92.1883046699325,38.3681761336563]}  |
| 01779792              | Nebraska                                     |  35 | {"type":"Point","coordinates":[-97.8496886533389,41.4426955679412]}  |
| 01779793              | Nevada                                       |  36 | {"type":"Point","coordinates":[-115.992474312302,37.9127709992178]}  |
| 01779794              | New Hampshire                                |  37 | {"type":"Point","coordinates":[-71.5978177402021,43.9585841263735]}  |
| 01779795              | New Jersey                                   |  38 | {"type":"Point","coordinates":[-74.7342060895472,40.1862212281874]}  |
| 01779796              | New York                                     |  39 | {"type":"Point","coordinates":[-74.931413218779,42.2529504249618]}   |
| 01779797              | North Dakota                                 |  40 | {"type":"Point","coordinates":[-97.7974492684418,47.3592469399905]}  |
| 01779798              | Pennsylvania                                 |  41 | {"type":"Point","coordinates":[-76.8260790277368,40.9801819217324]}  |
| 01779799              | South Carolina                               |  42 | {"type":"Point","coordinates":[-81.3588011364492,33.5808270966468]}  |
| 01779801              | Texas                                        |  43 | {"type":"Point","coordinates":[-98.7725068956115,29.8219670967323]}  |
| 01779802              | Vermont                                      |  44 | {"type":"Point","coordinates":[-72.436271076001,44.0669830822455]}   |
| 01779803              | Virginia                                     |  45 | {"type":"Point","coordinates":[-78.0541807437174,37.6982304423068]}  |
| 01779804              | Washington                                   |  46 | {"type":"Point","coordinates":[-122.640006855541,47.7959450287645]}  |
| 01779805              | West Virginia                                |  47 | {"type":"Point","coordinates":[-80.3551373923862,38.7506276616833]}  |
| 01779806              | Wisconsin                                    |  48 | {"type":"Point","coordinates":[-89.5095248112451,45.198644842279]}   |
| 01779807              | Wyoming                                      |  49 | {"type":"Point","coordinates":[-107.571827493527,42.9599050320965]}  |
| 01779808              | Puerto Rico                                  |  50 | {"type":"Point","coordinates":[-66.2423900561918,18.2032215322885]}  |
| 01779809              | Commonwealth of the Northern Mariana Islands |  51 | {"type":"Point","coordinates":[145.615819585475,16.1727270208791]}   |
| 01785533              | Alaska                                       |  52 | {"type":"Point","coordinates":[-150.41866893173,58.3829444770504]}   |
| 01785534              | South Dakota                                 |  53 | {"type":"Point","coordinates":[-98.1393052717026,43.8929744764808]}  |
| 01802701              | American Samoa                               |  54 | {"type":"Point","coordinates":[-170.302817441699,-14.2005732150488]} |
| 01802705              | Guam                                         |  55 | {"type":"Point","coordinates":[144.763252929807,13.4279675884233]}   |
| 01802710              | United States Virgin Islands                 |  56 | {"type":"Point","coordinates":[-64.8355292735655,18.1760172621614]}  |

#### Tile tables

The tile tables and views contain WGS84 topoJSON and geoJSON tiles for a geography used by the RIF front end and extract utilities to display administrative geography geolevels
The view is defined in *rif40_geolevels.tiletable* and adds valid empty tiles for the whole planet. The view uses the table which only contains tiles with data. The view is efficient.

Tiles tables must contain the following fields (*t_tiles_sahsuland* example):

|    column_name     |                                                        description                                                                                        |  data_type   |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| geolevel_id        | ID for ordering (1=lowest resolution). Up to 99 supported.                                                                                                | integer      |
| zoomlevel          | Zoom level: 0 to 11. Number of tiles is 2&ast;&ast;&lt;zoom level&gt; &ast; 2&ast;&ast;&lt;zoom level&gt;; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11 | integer      |
| x                  | X tile number. From 0 to (2&ast;&ast;&lt;zoom level&gt;)-1                                                                                                | integer      |
| y                  | Y tile number. From 0 to (2&ast;&ast;&lt;zoom level&gt;)-1                                                                                                | integer      |
| optimised_topojson | Tile multi-polygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.                                                                | json         |
| tile_id            | Tile ID in the format &lt;geolevel number&gt;_&lt;geolevel name&gt;_&lt;zoom level&gt;_&lt;X tile number&gt;_&lt;Y tile number&gt;                        | varchar(200) |
| areaid_count       | Total number of areaIDs (geoJSON features)                                                                                                                | integer      |

Example view SQL for *SAHSULAND*:

```sql
CREATE OR REPLACE VIEW rif_data.tiles_sahsuland
AS
WITH a AS (
         SELECT t_rif40_geolevels.geography,
                MAX(t_rif40_geolevels.geolevel_id) AS max_geolevel_id
           FROM t_rif40_geolevels
          WHERE t_rif40_geolevels.geography::text = 'SAHSULAND'::text
          GROUP BY t_rif40_geolevels.geography
), b AS (
         SELECT a.geography,
                generate_series(1, a.max_geolevel_id::integer, 1) AS geolevel_id
           FROM a
), c AS (
         SELECT b2.geolevel_name,
                b.geolevel_id,
                b.geography,
                b2.areaid_count
           FROM b,
                t_rif40_geolevels b2
          WHERE b.geolevel_id = b2.geolevel_id AND b.geography::text = b2.geography::text
), d AS (
         SELECT generate_series(0, 11, 1) AS zoomlevel
), ex AS (
         SELECT d.zoomlevel,
                generate_series(0,
						power(2::double precision, d.zoomlevel::double precision)::integer - 1,
						1) AS xy_series
           FROM d
), ey AS (
         SELECT c.geolevel_name,
                c.areaid_count,
                c.geolevel_id,
                c.geography,
                ex.zoomlevel,
                ex.xy_series
           FROM c,
                ex
)
SELECT z.geography,
       z.geolevel_id,
       z.geolevel_name,
       CASE
            WHEN h1.tile_id IS NULL AND h2.tile_id IS NULL THEN 1
            ELSE 0
       END AS no_area_ids,
       COALESCE(h1.tile_id,
			((((((((z.geolevel_id::character varying::text ||
				'_'::text) ||
				z.geolevel_name::text) ||
				'_'::text) ||
				z.zoomlevel::character varying::text) ||
				'_'::text) ||
				z.x::character varying::text) ||
				'_'::text) ||
				z.y::character varying::text)::character varying) AS tile_id,
       z.x,
       z.y,
       z.zoomlevel,
       COALESCE(h1.optimised_topojson,
			h2.optimised_topojson,
			'{"type": "FeatureCollection","features":[]}'::json) AS optimised_topojson
  FROM (
     SELECT ey.geolevel_name,
            ey.areaid_count,
            ey.geolevel_id,
            ey.geography,
            ex.zoomlevel,
            ex.xy_series AS x,
            ey.xy_series AS y
       FROM ey,
            ex
      WHERE ex.zoomlevel = ey.zoomlevel) z
     LEFT JOIN t_tiles_sahsuland h1 ON
			z.areaid_count > 1 AND
			z.zoomlevel = h1.zoomlevel AND
			z.x = h1.x AND z.y = h1.y AND
			z.geolevel_id = h1.geolevel_id
     LEFT JOIN t_tiles_sahsuland h2 ON
			z.areaid_count = 1 AND
			h2.zoomlevel = 0 AND
			h2.x = 0 AND
			h2.y = 0 AND
			h2.geolevel_id = 1;
```

#### Adjacency tables

The adjacency tables are a list of adjacent areas for each geolevel and area id in a geography used in BAtesian smoothing;
The table is defined in *rif40_geolevels.adjacencytable*.

Adjacency tables must contain the following fields (*adjacency_sahsuland* example):

|   column_name   |                        description                         |   data_type   |
|-----------------|------------------------------------------------------------|---------------|
| geolevel_id     | ID for ordering (1=lowest resolution). Up to 99 supported. | integer       |
| areaid          | Area Id                                                    | varchar(200)  |
| num_adjacencies | Number of adjacencies                                      | integer       |
| adjacency_list  | Adjacent area Ids                                          | varchar(8000) |

As an example the adjacency table *adjacency_sahsuland* contains (with rows truncated):

| geolevel_id | areaid          | num_adjacencies |              adjacency_list                                                                       |
|-------------|-----------------|-----------------|---------------------------------------------------------------------------------------------------|
|           2 | 01.001          |               3 | 01.002,01.004,01.005                                                                              |
|           2 | 01.002          |               3 | 01.001,01.003,01.004                                                                              |
|           2 | 01.003          |               3 | 01.002,01.004,01.008                                                                              |
|           2 | 01.004          |               6 | 01.001,01.002,01.003,01.005,01.008,01.009                                                         |
|           2 | 01.005          |               4 | 01.001,01.004,01.006,01.009                                                                       |
| ...         | ...             | ...             | ...                                                                                               |
|           3 | 01.001.000200   |               3 | 01.001.000100,01.001.000300,01.005.002400                                                         |
|           3 | 01.001.000300   |               1 | 01.001.000200                                                                                     |
|           3 | 01.002.000300   |               4 | 01.002.000400,01.002.000500,01.002.000600,01.002.001900                                           |
|           3 | 01.002.000400   |               4 | 01.002.000300,01.002.001600,01.002.001700,01.002.001900                                           |
|           3 | 01.002.000500   |               3 | 01.001.000100,01.002.000300,01.002.000600                                                         |
|           3 | 01.002.000600   |               7 | 01.001.000100,01.002.000300,01.002.000500,01.002.000700,01.002.000800,01.002.001100,01.002.001900 |
|           3 | 01.002.000700   |               3 | 01.001.000100,01.002.000600,01.002.000800                                                         |
| ...         | ...             | ...             | ...                                                                                               |
|           4 | 01.002.001300.2 |               4 | 01.002.001200.3,01.002.001200.7,01.002.001300.1,01.002.001300.4                                   |
|           4 | 01.002.001300.3 |               6 | 01.002.001200.5,01.002.001200.6,01.002.001200.7,01.002.001300.4,01.002.001300.9,01.002.001400.2   |
|           4 | 01.002.001300.4 |               6 | 01.002.001200.7,01.002.001300.1,01.002.001300.2,01.002.001300.3,01.002.001300.5,01.002.001300.9   |
|           4 | 01.002.001300.5 |               6 | 01.001.000100.1,01.002.001300.1,01.002.001300.4,01.002.001300.8,01.002.001300.9,01.002.001500.2   |
|           4 | 01.002.001300.6 |               4 | 01.002.001300.7,01.002.001300.9,01.002.001400.5,01.002.001400.7                                   |
| ...         | ...             | ...             | ...                                                                                               |

#### Hierarchy tables

Hierarchy tables have one table per administrative geography; contains a hierarchy of higher resolution of one or more areas that fit exactly within the lower resolution
The table is defined in *rif40_geographies*.hierarchytable*.

Hierarchy tables must contain the following fields (as in the *hierarchy_sahsuland* example below):

* Area id columns relevant to each geolevel in the geography. They must be named the same as the &lt'geolevel_name&gt;;

|   column_name    |               description                |  data_type   |
|------------------|------------------------------------------|--------------|
| sahsu_grd_level1 | Hierarchy lookup for Level 1 (top level) | varchar(100) |
| sahsu_grd_level2 | Hierarchy lookup for Level 2             | varchar(100) |
| sahsu_grd_level3 | Hierarchy lookup for Level 3             | varchar(100) |
| sahsu_grd_level4 | Hierarchy lookup for Level 4             | varchar(100) |

As an example the hierarchy table *hierarchy_sahsuland* contains (with rows truncated):

| sahsu_grd_level1 | sahsu_grd_level2 | sahsu_grd_level3 | sahsu_grd_level4 |
|------------------|------------------|------------------|------------------|
| 01               | 01.001           | 01.001.000100    | 01.001.000100.1  |
| 01               | 01.001           | 01.001.000100    | 01.001.000100.2  |
| 01               | 01.001           | 01.001.000200    | 01.001.000200.1  |
| 01               | 01.001           | 01.001.000300    | 01.001.000300.1  |
| 01               | 01.002           | 01.002.000300    | 01.002.000300.1  |
| 01               | 01.002           | 01.002.000300    | 01.002.000300.2  |
| 01               | 01.002           | 01.002.000300    | 01.002.000300.3  |
| ...              | ...              | ...              | ...              |

#### Geometry tables

The geometry tables contains geometric data for a geography to allow the database to perform spatial queries with the administrative geometry
The table is defined in *rif40_geolevels.geometrytable*.

Geometry tables must contain the following fields (*geometry_sahsuland* example):

| column_name |                                                               description                                                                                 |  data_type   |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| geolevel_id | ID for ordering (1=lowest resolution). Up to 99 supported.                                                                                                | integer      |
| areaid      | Area ID.                                                                                                                                                  | varchar(200) |
| zoomlevel   | Zoom level: 0 to 11. Number of tiles is 2&ast;&ast;&lt;zoom level&gt; &ast; 2&ast;&ast;&lt;zoom level&gt;; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11 | integer      |
| geom        | Geometry data in SRID 4326 (WGS84).                                                                                                                       | geometry     |
| wkt         | Well known text                                                                                                                                           | Text         |

Geometry tables are partitioned on Postgres ports.

#### Shapefile, shapefile tables

The Shapefile, shapefile tables contain the names of the original shapefile and the data in geoJSON format. They are used as part of the administrative geography preprocessing (tile maker)
The shapefile is defined in *rif40_geolevels.shapefile*.
The table is defined in *rif40_geolevels.shapefile_table*.

#### Centroids tables

Centroids tables are used to import population weighted or spatially processed (centroid pulled to within the centroid boundary) centroids into an administrative geography.
Centroids tables are not currently supported. The table is defined in *rif40_geolevels.centroids_table*.
This is in the TODO list for future additions

### Health Themes

The table *rif40.rif40_health_study_themes* is used to group numerators logically so the user is not presented with a long list of numerator/denominator pairs. Purpose could include:

* Management of datasets with annual reloads (e.g. split cancer in *current cancer* and *archived cancer*) so the user is not confused by large numbers of similarly named tables
* Management of datasets with large numbers of component parts (e.g. HES) and differing views onto the data
* Project specific datasets, e.g. *incinerators*, *CO* (Carbon monoxide poisoning)

| column_name | description |  data_type   |
|-------------|-------------|--------------|
| theme       | Theme       | varchar(30)  |
| description | Description | varchar(200) |

An example theme is provided:

|  theme  |            description
|---------|-----------------------------------|
| cancers | covering various types of cancers |

# Load Processing

Generally load processing requires three steps:

* Create and load the Administrative geography. **Always load the administrative geography first**. This is produced by the
  [Tile maker]({{ site.baseurl }}/rifNodeServices/tileMaker). See
  [Example of Post Front End Processing]({{ site.baseurl }}/rifNodeServices/tileMaker#post-front-end-processing)
* Pre-process the data from flat files, process and unloaded back into flat files that can be loaded into either Postgres or SQL Server.
  Typically this is done as a normal user on any database. Do **not** use the schema *rif40* or  administrative accounts (*postgres* or *administrator*):
  * Create numerator/denominator/covariate load tables to load data from CSV or Text files. Fixed length (mainframe) record files have to be loaded as a single string
    and then chopped converted into the relevant fields in stage four;
  * Load numerator/denominator/covariate tables from CSV or Text files. SQL Server records will need a format specification
    [Using a Format File to Bulk Import Data](https://docs.microsoft.com/en-us/sql/relational-databases/import-export/use-a-format-file-to-bulk-import-data-sql-server?view=sql-server-2017);
  * Check all numerator/denominator/covariate table data has been loaded;
  * Convert numerator/denominator/covariate fixed length string into new numerator/denominator/covariate load table with the correct columns and datatypes;
  * Create numerator/denominator/covariate table from load table. For denominator and covariate tables additional rows may need to be added to cope with holes in the data; e.g. re-use a later
    year of population or covariate data to replace missing earlier years. RIF covariates require annual data, if you do not have annual data you can use a view and the ```generate_series```
	function to add the years; see: [3.0.1 Generate series](#generate-series);
  * Add constraints to numerator/denominator/covariate table;
  * Add indexes to numerator/denominator/covariate table;
  * Comment numerator/denominator/covariate table and columns;
  * Unload numerator/denominator/covariate table for load processing.

  With the covariates data there are normally two phases:

  * Loading and cleaning the input data;
  * Creating production covariate tables for unloading and then subsequent use in the production load process.

* Load the processed data as a schema owner (e.g. *rif40*)into a target database:
  * Remove setup data, views and tables;
  * Create table;
  * Create views;
  * Load CSV data into table;
  * Check all table data has been loaded;
  * Add indexes to table;
  * Comment table and columns;
  * Grant grant access on tables and views to an appropriate role;
  * Setup numerator and denominator tables and views.
    - Numerator and denominator tables are setup in *rif40.rif40_tables*;
	- The numerator ICD field needs to be defined in: *rif40.rif40_outcome_groups* and *rif40.rif40_outcomes*,
	  see: [ICD field name](icd-field-name);
    - Covariate table names are predefined, see:
      [Covariates](#covariates);
  The RIF load scripts only require the administrative geography; and not the tilemaker pre-processing tables.

The following scripts are used in the examples:

| Action                                    | Postgres                            | SQL Server                          |
|-------------------------------------------|-------------------------------------|-------------------------------------|
| Load the Administrative geography         | *rif_pg_usa_2014.sql*               | *rif_mssql_usa_2014.sql*            |
| Pre-process the SEER data                 | *pg_load_seer.sql*                  | **TO FOLLOW**                       |
| &nbsp;&bull; Pre-process numerator data   | *pg_load_seer_cancer.sql*           | **TO FOLLOW**                       |
| &nbsp;&bull; Pre-process denominator data | *pg_load_seer_population.sql*       | **TO FOLLOW**                       |
| &nbsp;&bull; Pre-process covariates data  | *pg_load_seer_covariates.sql*       | **TO FOLLOW**                       |
| Load the processed SEER data              | *pg_rif40_load_seer.sql*            | *ms_rif40_load_seer.sql*            |
| &nbsp;&bull; Load numerator data          | *pg_rif40_load_seer_cancer.sql*     | *ms_rif40_load_seer_cancer.sql*     |
| &nbsp;&bull; Load denominator data        | *pg_rif40_load_seer_population.sql* | *ms_rif40_load_seer_population.sql* |
| &nbsp;&bull; Load covariate data          | *pg_rif40_load_seer_covariates.sql* | *ms_rif40_load_seer_covariates.sql* |

To install, change to the &lt;SEER Data directory, e.g. C:\Users\phamb\OneDrive\April 2018 deliverable for SAHSU\SEER Data\ &gt;;

The SEER pre-processing script *pg_load_seer_covariates.sql* has a dependency on the *cb_2014_us_nation_5m*, *cb_2014_us_state_500k* and *cb_2014_us_county_500l* that are part of the
tilemaker pre-processing. The FIPS code is required to make the join and this field is not in the standard lookup tables. For this reason it is necessary to build
the covariates table on *sahsuland_dev*. In the longer term the FIPS codes should be added to the lookup tables.

SQL Server pre-processing scripts have not been created due to the lack of an unload facility in *sqlcmd*. There are two basic options to resolve this problem without recourse
to C# or the many GUI wizards available:

* Use [bcp](https://docs.microsoft.com/en-us/sql/relational-databases/import-export/import-and-export-bulk-data-by-using-the-bcp-utility-sql-server?view=sql-server-2017)
* Use *sqlcmd* [SQL SERVER – Export Data AS CSV from Database Using SQLCMD](https://blog.sqlauthority.com/2013/11/25/sql-server-export-data-as-csv-from-database-using-sqlcmd/)

Other useful references:

* [Bulk Import and Export of Data (SQL Server)](https://docs.microsoft.com/en-us/sql/relational-databases/import-export/bulk-import-and-export-of-data-sql-server?view=sql-server-2017)
* [8 Ways to Export SQL Results To a Text File](http://www.sqlservercentral.com/articles/Export/147145/)

To run a script:

- Postgres: ```psql -U rif40 -d <database name> -w -e -f <script name>```
  Flags:
  * ```-U rif40```: connect as user *rif40*
  * ```-d <database name>```: connect to database &lt;database name&gt;
  * ```-w```: never issue a password prompt. If the server requires password authentication and a password is not available by other means
    such as a .pgpass file, the connection attempt will fail;
  * ```-e```: copy all SQL commands sent to the server to standard output as well;
  * ```-f <script name>```: run SQL script &lt;script name&gt;

  For information on [Postgres passwords]({{ site.baseurl }}/rifDatabase/databaseManagementManual#221-postgres)

  e.g.
  ```
  psql -U rif40 -d sahsuland -w -e -f pg_rif40_load_seer.sql
  ```
  An an example see: [Postgres data load]({{ site.baseurl }}/rifDatabase/DataLoaderData/Example_postgres_load);

- SQL Server: ```sqlcmd -U rif40 -P <password> -d <database name> -b -m-1 -e -r1 -i <script name> -v pwd="%cd%"```
  Flags:
  * ```-U rif40```: connect as user *rif40*;
  * ```-P <password>```: the &lt;password&gt; for user *rif40*;
  * ```-d <database name>```: connect to database &lt;database name&gt;;
  * ```-b```: terminate batch job if there is an error;
  * ```-m-1```: all messages including informational messages, are sent to stdout;
  * ```-e```: echo input;
  * ```-r1```: redirects the error message output to stderr;
  * ```-i <script name>```: run SQL script &lt;script name&gt;;
  * ```-v pwd="%cd%"```: set script variable pwd to %cd% (current working directory). So bulk
    load can find the CSV files.

  By default on SQL server the*rif40* password is set to random characters, to change the SQL server *rif40*
  password:

  ```sql
  ALTER LOGIN rif40 WITH PASSWORD = 'XXXXXXXX';
  GO
  ```

  e.g.
  ```
  sqlcmd -U rif40 -P rif40 -d sahsuland -b -m-1 -e -r1 -i ms_rif40_load_seer.sql -v pwd="%cd%"
  ```

  The production scripts *rif_pg_usa_2014.sql* and *rif_mssql_usa_2014.sql* require a role called *SEER_USER*. This needs to be created and then granted to each database user:
  ```sql
  psql -U postgres -d postgres
  CREATE ROLE seer_user;
  GRANT seer_user TO peter;
  ```

  ```sql
  sqlcmd -E
  USE sahsuland;
  IF DATABASE_PRINCIPAL_ID('seer_user') IS NULL
  	CREATE ROLE [seer_user];
  SELECT name, type_desc FROM sys.database_principals WHERE name LIKE '%seer_user%';
  ALTER ROLE [seer_user] ADD MEMBER [peter];
  GO
  ```

Once all the data has been loaded, check the table appears in a user (not rif40) *rif40_num_denom*

```
sahsuland=> select * from rif40_num_denom;
 geography |   numerator_table    |             numerator_description             |         theme_description         | denominator_table |                                 denominator_description                                  | automatic
-----------+----------------------+-----------------------------------------------+-----------------------------------+-------------------+------------------------------------------------------------------------------------------+-----------
 SAHSULAND | NUM_SAHSULAND_CANCER | cancer numerator                              | covering various types of cancers | POP_SAHSULAND_POP | population health file                                                                   |         1
 USA_2014  | SEER_CANCER          | SEER Cancer data 1973-2013. 9 States in total | covering various types of cancers | SEER_POPULATION   | SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total |         1
(2 rows)

```
If your data does not appear; see [Numerator Denominator Pair Errors](#numerator-denominator-pair-errors)

### Generate Series

The ```generate_series``` function is used in the RIF *rif_data.tiles_sahsuland* VIEW to add back tiles for which there is no data (i.e. outside of the extent of the geography so the tiles
cover the entire world. It is also very useful for converting non annual covariates to the annual form required by the RIF; e.g:

```sql
SELECT generate_series(2011, 2018);
 generate_series
-----------------
            2011
            2012
            2013
            2014
            2015
            2016
            2017
            2018
(8 rows)
```

Using *rif_data.covar_sahsuland_covariates3* year 2000 data only (i.e pretend it is not annualised!)
```sql
WITH a AS (
	SELECT generate_series(2011, 2018) AS year
)
SELECT a.year, b.sahsu_grd_level3, b.ses, b.ethnicity
  FROM a, covar_sahsuland_covariates3 b
 WHERE b.year = 2000 /* USe only one year! */
 ORDER BY 1, 2 LIMIT 20;
 year | sahsu_grd_level3 | ses | ethnicity
------+------------------+-----+-----------
 2011 | 01.001.000100    |   4 |         1
 2011 | 01.001.000200    |   5 |         1
 2011 | 01.001.000300    |   5 |         3
 2011 | 01.002.000300    |   2 |         2
 2011 | 01.002.000400    |   5 |         3
 2011 | 01.002.000500    |   5 |         3
 2011 | 01.002.000600    |   4 |         3
 2011 | 01.002.000700    |   5 |         3
 2011 | 01.002.000800    |   4 |         3
 2011 | 01.002.000900    |   1 |         2
 2011 | 01.002.001000    |   2 |         1
 2011 | 01.002.001100    |   3 |         3
 2011 | 01.002.001200    |   1 |         2
 2011 | 01.002.001300    |   2 |         3
 2011 | 01.002.001400    |   2 |         3
 2011 | 01.002.001500    |   1 |         2
 2011 | 01.002.001600    |   2 |         3
 2011 | 01.002.001700    |   1 |         1
 2011 | 01.002.001800    |   2 |         3
 2011 | 01.002.001900    |   1 |         1
(20 rows)
```

See [Postgres set returning functions](https://www.postgresql.org/docs/9.6/static/functions-srf.html)

The ```generate_series``` function is **NOT** part of SQL Server; however the following code provides this functionality. It is installed as a table valued function in the *rif40* schema.

```sql
/*
 * SQL statement name: 	generate_series.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:			None
 * Description:			Generate a series of values, from start to stop with a step size of step
 *						Original by: Simon Greener, Independent GeoSpatial Solutions Architect
 *						http://www.spatialdbadvisor.com/sql_server_blog/86/generate_series-for-sql-server-2008
 */
CREATE FUNCTION generate_series ( @p_start INT, @p_end INT, @p_step INT=1 )
RETURNS @Integers TABLE ( [IntValue] INT )
AS
BEGIN
    DECLARE
      @v_i                 INT,
      @v_step              INT,
      @v_terminating_value INT;
    BEGIN
      SET @v_i = CASE WHEN @p_start IS NULL THEN 1 ELSE @p_start END;
      SET @v_step  = CASE WHEN @p_step IS NULL OR @p_step = 0 THEN 1 ELSE @p_step END;
      SET @v_terminating_value =  @p_start + CONVERT(INT,ABS(@p_start-@p_end) / ABS(@v_step) ) * @v_step;
      -- Check for impossible combinations
      IF NOT ( ( @p_start > @p_end AND SIGN(@p_step) = 1 )
               OR
               ( @p_start < @p_end AND SIGN(@p_step) = -1 ))
      BEGIN
        -- Generate values
        WHILE ( 1 = 1 )
        BEGIN
           INSERT INTO @Integers ( [IntValue] ) VALUES ( @v_i )
           IF ( @v_i = @v_terminating_value )
              BREAK
           SET @v_i = @v_i + @v_step;
        END;
      END;
    END;
    RETURN
END;
```

## Administrative Geography

The *SAHSULAND* example geography is supplied as part of the RIF. To load the SEER test dataset you first
need to load the USA County level administrative geography. The scripts and the data are creared by the
[tile-maker]({{ site.baseurl }}/rifNodeServices/tileMaker) program:

To install, change to the &lt;tile maker directory, e.g. C:\Users\phamb\OneDrive\April 2018 deliverable for SAHSU\SEER Data\Tile maker USA&gt;

* Make sure nobody is logged onto the RIF;
* ```cd C:\Users\phamb\OneDrive\April 2018 deliverable for SAHSU\SEER Data\Tile maker USA```;
* Run *rif_pg_usa_2014.sql* or *rif_mssql_usa_2014.sql*;.

The scripts *rif_pg_usa_2014.sql* or *rif_mssql_usa_2014.sql* load processed geometry and tiles tables into production database:

a) integrate with RIF40 control tables, e.g:
   * geography_usa_2014
   * geolevels_usa_2014

b) Processed geometry data (partitioned in PostGres), e.g:
   * geometry_usa_2014

c) Hierarchy table, e.g:
   * hierarchy_usa_2014

d) Lookup tables, e.g:
   * lookup_cb_2014_us_county_500k
   * lookup_cb_2014_us_nation_5m
   * lookup_cb_2014_us_state_500k

e) Tiles table and view
   * t_tiles_usa_2014
   * tiles_usa_2014

## Postgres

Run: ```psql -U rif40 -w -e -f rif_pg_USA_2014.sql```

## SQL Server

Run: ```sqlcmd -U rif40 -d <database name> -b -m-1 -e -r1 -i rif_mssql_USA_2014.sql -v pwd="%cd%"```

## Numerator

## Pre Processing

* Create numerator load tables to load data from CSV or Text files. Fixed length (mainframe) record files have to be loaded as a single string
  and then chopped converted into the relevant fields in stage four:
  - Postgres:
    ```sql
	CREATE TABLE seer9_yr1973_2013_fixed_length (
		record_value VARCHAR(358)
	);
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Load numerator tables from CSV or Text files:
  - Postgres:
    ```sql
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\BREAST.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\COLRECT.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\DIGOTHR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\FEMGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\LYMYLEUK.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\MALEGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\OTHER.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\RESPIR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2005.lo_2nd_half\URINARY.TXT' WITH CSV;

	/*
	C:\Users\Peter\Documents\Local data loading\SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak>wc -l *.TXT
	   190126 BREAST.TXT
	   112982 COLRECT.TXT
		98671 DIGOTHR.TXT
		84681 FEMGEN.TXT
		93322 LYMYLEUK.TXT
	   161911 MALEGEN.TXT
	   193120 OTHER.TXT
	   122381 RESPIR.TXT
		70599 URINARY.TXT
	  1127793 total
	 */
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\BREAST.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\COLRECT.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\DIGOTHR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\FEMGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\LYMYLEUK.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\MALEGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\OTHER.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\RESPIR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1992_2013.sj_la_rg_ak\URINARY.TXT' WITH CSV;

	/*
	C:\Users\Peter\Documents\Local data loading>wc -l SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9*.TXT
		769261 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/BREAST.TXT
		528452 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/COLRECT.TXT
		364018 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/DIGOTHR.TXT
		434960 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/FEMGEN.TXT
		381579 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/LYMYLEUK.TXT
		636118 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/MALEGEN.TXT
		784697 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/OTHER.TXT
		643924 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/RESPIR.TXT
		320405 SEER_1973_2013_TEXTDATA/incidence/yr1973_2013.seer9/URINARY.TXT
	   4863414 total
	 */
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\BREAST.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\COLRECT.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\DIGOTHR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\FEMGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\LYMYLEUK.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\MALEGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\OTHER.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\RESPIR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr1973_2013.seer9\URINARY.TXT' WITH CSV;

	/*
	C:\Users\Peter\Documents\Local data loading\SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga>wc -l *.TXT
		505731 BREAST.TXT
		301745 COLRECT.TXT
		239579 DIGOTHR.TXT
		174656 FEMGEN.TXT
		247252 LYMYLEUK.TXT
		428840 MALEGEN.TXT
		618807 OTHER.TXT
		431674 RESPIR.TXT
		226912 URINARY.TXT
	   3175196 total
	 */
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\BREAST.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\COLRECT.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\DIGOTHR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\FEMGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\LYMYLEUK.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\MALEGEN.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\OTHER.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\RESPIR.TXT' WITH CSV;
	\copy seer9_yr1973_2013_fixed_length FROM 'SEER_1973_2013_TEXTDATA\incidence\yr2000_2013.ca_ky_lo_nj_ga\URINARY.TXT' WITH CSV;
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Check all numerator table data has been loaded:
  - Postgres:
    ```sql
	--
	-- Check rowcount
	--
	SELECT COUNT(*) AS total FROM seer9_yr1973_2013_fixed_length;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM seer9_yr1973_2013_fixed_length;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 9176963 THEN
			RAISE INFO 'Table: seer9_yr1973_2013_fixed_length has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: seer9_yr1973_2013_fixed_length has % rows; expecting 9176963', c1_rec.total;
		END IF;
	END;
	$$;
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Convert numerator fixed length string into new numerator load table with the correct columns and datatypes:
  - Postgres:
    ```sql
	--
	-- Convert datatypes
	--
	CREATE TABLE seer9_yr1973_2013
	AS
	SELECT SUBSTRING(record_value FROM 39 FOR 4)::INTEGER AS year_dx,	/* Year of diagnosis */
		   SUBSTRING(record_value FROM 24 FOR 1)::INTEGER AS sex,		/* Sex */
		   SUBSTRING(record_value FROM 25 FOR 3)::INTEGER AS age_dx, 	/* Age at diagnosis */
		   SUBSTRING(record_value FROM 246 FOR 2)::Text AS state_fips_code,	 /* State FIPS code */
		   SUBSTRING(record_value FROM 248 FOR 3)::Text AS county_fips_code, /* County FIPS code */
		   SUBSTRING(record_value FROM 208 FOR 4)::Text AS icdot10v, 	/* Recode ICD-O-2 to 10 */
		   SUBSTRING(record_value FROM 1 FOR 8)::INTEGER AS pubcsnum,	/* Patient ID */
		   SUBSTRING(record_value FROM 35 FOR 2)::INTEGER AS seq_num,  	/* Sequence number */
		   SUBSTRING(record_value FROM 53 FOR 4)::Text AS histo3v,		/* Histologic Type ICD-O-3 */
		   SUBSTRING(record_value FROM 57 FOR 1)::Text AS beho3v,		/* Behavior code ICD-O-3 */
		   SUBSTRING(record_value FROM 233 FOR 1)::INTEGER AS rac_reca,	/* Race recode A (WHITE, BLACK, OTHER) */
		   SUBSTRING(record_value FROM 234 FOR 1)::INTEGER AS rac_recy,	/* Race recode Y (W, B, AI, API) */
		   SUBSTRING(record_value FROM 235 FOR 1)::INTEGER AS origrecb,	/* Origin Recode NHIA (HISPANIC, NON-HISP) */
		   SUBSTRING(record_value FROM 255 FOR 5)::Text AS codpub,	  	/* Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html) */
		   SUBSTRING(record_value FROM 9 FOR 10)::INTEGER AS reg		/* SEER registry */
	  FROM seer9_yr1973_2013_fixed_length;
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Create numerator table from load table:
  - Postgres:
    ```sql
	/* Cancer registry codes:

	Code			Description (first year of data)
	0000001501		San Francisco-Oakland SMSA (1973)
	0000001502		Connecticut (1973)
	0000001520		Metropolitan Detroit (1973)
	0000001521		Hawaii (1973)
	0000001522		Iowa (1973)
	0000001523		New Mexico (1973)
	0000001525		Seattle (Puget Sound) (1974)
	0000001526		Utah (1973)
	0000001527		Metropolitan Atlanta (1975)
	0000001529		Alaska*
	0000001531		San Jose-Monterey*
	0000001535		Los Angeles*
	0000001537		Rural Georgia*
	0000001541		Greater California (excluding SF, Los Angeles & SJ)**
	0000001542		Kentucky**
	0000001543		Louisiana**
	0000001544		New Jersey**
	0000001547		Greater Georgia (excluding AT and RG)**
	(Year in parentheses refers to first diagnosis year data reported to SEER)

	*Note: The incidence/yr1992_2013.sj_la_rg_ak directory files contain cases for Alaska, San Jose-Monterey, Los Angeles and
		   Rural Georgia registries beginning in 1992. Cases have been collected by SEER for these registries prior to 1992 but have
		   been excluded from the SEER Research Data file.
	**Note: The incidence/yr2000_2013.ca_ky_lo_nj_ga directory files contain cases for Greater California, Kentucky, Louisiana,
			New Jersey and Greater Georgia registries beginning in 2000. For the year 2005, only January through June diagnoses are
			included for Louisiana. The July through December incidence cases can be found in the yr2005.lo_2nd_half directory.
	*/

	--
	-- Extract cancer
	-- * Convert age, sex to RIF age_sex_group 1
	-- * Convert FIPS codes to the geographic Names Information System (GNIS) codes used by the RIF
	-- * Enforce primary key (Patient ID, Sequence number)
	-- * Check AGE_SEX_GROUP, handle uncoded age
	-- * Convert unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g.
	--   900 series to represent county/independent city combinations in Virginia.
	-- * Use icdot10v (ICD 10 site code - recoded from ICD-O-2 to 10) as the ICD field
	--
	DROP TABLE IF EXISTS :USER.seer_cancer;
	CREATE TABLE seer_cancer
	AS
	SELECT a.year_dx AS year,
		   'US'::Text AS cb_2014_us_nation_5m,
		   d.statens AS cb_2014_us_state_500k,
		   COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code) AS cb_2014_us_county_500k,
		   (a.sex*100)+
				CASE WHEN a.age_dx = 999 THEN 99
				ELSE                     b.offset END AS age_sex_group,
		   a.icdot10v,		/* ICD 10 site code - recoded from ICD-O-2 to 10 */
		   a.pubcsnum,		/* Patient ID */
		   a.seq_num,		/* Sequence number */
		   a.histo3v,		/* Histologic Type ICD-O-3 */
		   a.beho3v,		/* Behavior code ICD-O-3 */
		   a.rac_reca,		/* Race recode A (WHITE, BLACK, OTHER) */
		   a.rac_recy,		/* Race recode Y (W, B, AI, API) */
		   a.origrecb,		/* Origin Recode NHIA (HISPANIC, NON-HISP) */
		   a.codpub,	  	/* Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html) */
		   a.reg-1500 AS reg		/* SEER registry (minus 1500 so same as population file) */
	  FROM seer9_yr1973_2013 a
			LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND a.age_dx BETWEEN b.low_age AND b.high_age /* limit 255! */)
			LEFT OUTER JOIN cb_2014_us_county_500k c ON (a.state_fips_code = c.statefp AND a.county_fips_code = c.countyfp)
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
	 ORDER BY a.pubcsnum, a.seq_num;
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Comment numerator load table:
  - Postgres:
    ```sql
	COMMENT ON TABLE seer_cancer IS 'SEER Cancer data 1973-2013. 9 States in total';
	COMMENT ON COLUMN seer_cancer.year IS 'Year';
	COMMENT ON COLUMN seer_cancer.cb_2014_us_nation_5m IS 'United States to county level including territories';
	COMMENT ON COLUMN seer_cancer.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
	COMMENT ON COLUMN seer_cancer.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
	COMMENT ON COLUMN seer_cancer.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
	COMMENT ON COLUMN seer_cancer.icdot10v IS 'ICD 10 site code - recoded from ICD-O-2 to 10';
	COMMENT ON COLUMN seer_cancer.pubcsnum IS 'Patient ID';
	COMMENT ON COLUMN seer_cancer.seq_num IS 'Sequence number';
	COMMENT ON COLUMN seer_cancer.histo3v IS 'Histologic Type ICD-O-3';
	COMMENT ON COLUMN seer_cancer.beho3v IS 'Behavior code ICD-O-3';
	COMMENT ON COLUMN seer_cancer.rac_reca IS 'Race recode A (WHITE, BLACK, OTHER)';
	COMMENT ON COLUMN seer_cancer.rac_recy IS 'Race recode Y (W, B, AI, API)';
	COMMENT ON COLUMN seer_cancer.origrecb IS 'Origin Recode NHIA (HISPANIC, NON-HISP)';
	COMMENT ON COLUMN seer_cancer.codpub IS 'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)';
	COMMENT ON COLUMN seer_cancer.reg IS 'SEER registry (minus 1500 so same as population file)';
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Check rowcount:
  - Postgres:
    ```sql
	SELECT COUNT(*) AS total FROM seer_cancer;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM seer_cancer;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 9176963 THEN
			RAISE INFO 'Table: seer_cancer has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: seer_cancer has % rows; expecting 9176963', c1_rec.total;
		END IF;
	END;
	$$;
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Add constraints:
  - Postgres:
    ```sql
	ALTER TABLE seer_cancer ALTER COLUMN year SET NOT NULL;
	ALTER TABLE seer_cancer ALTER COLUMN cb_2014_us_state_500k SET NOT NULL;
	ALTER TABLE seer_cancer ALTER COLUMN cb_2014_us_county_500k SET NOT NULL;

	ALTER TABLE seer_cancer ADD CONSTRAINT seer_cancer_asg_ck
		CHECK (age_sex_group BETWEEN 100 AND 121 OR age_sex_group BETWEEN 200 AND 221 OR age_sex_group IN (199, 299) /* No age coded */);

	ALTER TABLE seer_cancer ALTER COLUMN age_sex_group SET NOT NULL;
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Add indexes:
  - Postgres:
    ```sql
	ALTER TABLE seer_cancer ADD CONSTRAINT seer_cancer_pk
		PRIMARY KEY (pubcsnum, seq_num);
	CREATE INDEX seer_cancer_year ON seer_cancer (year);
	CREATE INDEX seer_cancer_cb_2014_us_nation_5m ON seer_cancer(cb_2014_us_nation_5m);
	CREATE INDEX seer_cancer_cb_2014_us_state_500k ON seer_cancer(cb_2014_us_state_500k);
	CREATE INDEX seer_cancer_cb_2014_us_county_500k ON seer_cancer(cb_2014_us_county_500k);
	CREATE INDEX seer_cancer_age_sex_group ON seer_cancer(age_sex_group);
	CREATE INDEX seer_cancer_icdot10v ON seer_cancer(icdot10v);
	CREATE INDEX seer_cancer_reg ON seer_cancer(reg);
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```
* Unload table:
  - Postgres:
    ```sql
	\copy seer_cancer TO 'seer_cancer.csv' WITH CSV HEADER
	```
  - SQL Server:
    ```sql
	TO BE ADDED
	```

## Load Processing

* Remove numerator setup data, views and tables;
  - Postgres:
    ```sql
	DROP TABLE IF EXISTS rif_data.t_seer_cancer;
	DROP VIEW IF EXISTS rif_data.seer_cancer;

	DELETE FROM rif40.rif40_table_outcomes
	 WHERE numer_tab='SEER_CANCER';

	DELETE FROM rif40.rif40_tables
	 WHERE table_name='SEER_CANCER';
    ```
  - SQL Server:
    ```sql
	IF OBJECT_ID('rif_data.t_seer_cancer', 'U') IS NOT NULL BEGIN
		DROP TABLE rif_data.t_seer_cancer;
		DROP VIEW rif_data.seer_cancer;
		DELETE FROM rif40.rif40_table_outcomes
		 WHERE numer_tab='SEER_CANCER';
		DECLARE c1 CURSOR FOR
			SELECT table_name
			  FROM rif40.rif40_tables
			 WHERE table_name='SEER_CANCER';
		DECLARE @c1_table AS VARCHAR(30);
		OPEN c1;
		FETCH NEXT FROM c1 INTO @c1_table;
		IF @c1_table = 'SEER_CANCER' BEGIN
			DELETE FROM rif40.rif40_tables
			 WHERE table_name='SEER_CANCER';
		END;
		CLOSE c1;
		DEALLOCATE c1;
	END;
	GO
    ```
	If studies have already been created you will get the error:
	```
	Msg 51147, Level 16, State 1, Server PETER-PC\SAHSU, Procedure tr_rif40_tables_checks, Line 45
	Table name: [rif40].[rif40_tables], Cannot DELETE from RIF40_TABLES
	```
* Create numerator table;
  - Postgres:
    ```sql
	CREATE TABLE rif_data.t_seer_cancer
	(
	  year integer NOT NULL, -- Year
	  cb_2014_us_nation_5m text, -- United States to county level including territories
	  cb_2014_us_state_500k text NOT NULL, -- State geographic Names Information System (GNIS) code
	  cb_2014_us_county_500k text NOT NULL, -- County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.
	  age_sex_group integer NOT NULL, -- RIF age_sex_group 1 (21 bands)
	  icdot10v text, -- ICD 10 site code - recoded from ICD-O-2 to 10
	  pubcsnum integer NOT NULL, -- Patient ID
	  seq_num integer NOT NULL, -- Sequence number
	  histo3v text, -- Histologic Type ICD-O-3
	  beho3v text, -- Behavior code ICD-O-3
	  rac_reca integer, -- Race recode A (WHITE, BLACK, OTHER)
	  rac_recy integer, -- Race recode Y (W, B, AI, API)
	  origrecb integer, -- Origin Recode NHIA (HISPANIC, NON-HISP)
	  codpub text, -- Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)
	  reg integer, -- SEER registry (minus 1500 so same as population file)
	  CONSTRAINT seer_cancer_pk PRIMARY KEY (pubcsnum, seq_num),
	  CONSTRAINT seer_cancer_asg_ck CHECK (
			(age_sex_group >= 100 AND age_sex_group <= 121) OR
			(age_sex_group >= 200 AND age_sex_group <= 221) OR
			(age_sex_group IN (199, 299))
		)
	);
    ```
  - SQL Server:
    ```sql
	CREATE TABLE rif_data.t_seer_cancer
	(
	  year integer NOT NULL, /* Year */
	  cb_2014_us_nation_5m VARCHAR(200), /* United States to county level including territories */
	  cb_2014_us_state_500k VARCHAR(200) NOT NULL, /* State geographic Names Information System (GNIS) code */
	  cb_2014_us_county_500k VARCHAR(200) NOT NULL, /* County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia. */
	  age_sex_group integer NOT NULL, /* RIF age_sex_group 1 (21 bands) */
	  icdot10v VARCHAR(200), /* ICD 10 site code - recoded from ICD-O-2 to 10 */
	  pubcsnum integer NOT NULL, /* Patient ID */
	  seq_num integer NOT NULL, /* Sequence number */
	  histo3v VARCHAR(200), /* Histologic Type ICD-O-3 */
	  beho3v VARCHAR(200), /* Behavior code ICD-O-3 */
	  rac_reca integer, /* Race recode A (WHITE, BLACK, OTHER) */
	  rac_recy integer, /* Race recode Y (W, B, AI, API) */
	  origrecb integer, /* Origin Recode NHIA (HISPANIC, NON-HISP) */
	  codpub VARCHAR(200), /* Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html) */
	  reg integer, /* SEER registry (minus 1500 so same as population file) */
	  CONSTRAINT seer_cancer_pk PRIMARY KEY (pubcsnum, seq_num),
	  CONSTRAINT seer_cancer_asg_ck CHECK (
			(age_sex_group >= 100 AND age_sex_group <= 121) OR
			(age_sex_group >= 200 AND age_sex_group <= 221) OR
			(age_sex_group IN (199, 299))
		)
	);
	GO
    ```
* Create numerator views:
  ```sql
  --
  -- Create a test view
  --
  CREATE OR REPLACE VIEW rif_data.seer_cancer AS
  SELECT * FROM rif_data.t_seer_cancer;
  ```
* Load CSV data into numerator table;
  - Postgres:
    ```sql
	\copy rif_data.t_seer_cancer FROM 'seer_cancer.csv' WITH CSV HEADER;
    ```
  - SQL Server:
    ```sql
	BULK INSERT rif_data.t_seer_cancer
	FROM '$(pwd)/seer_cancer.csv'	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line
	WITH
	(
		FIRSTROW = 2,
		FORMATFILE = '$(pwd)/seer_cancer.fmt',		-- Use a format file
		TABLOCK					-- Table lock
	);
	GO
    ```
* Check all numerator table data has been loaded;
  - Postgres:
    ```sql
	SELECT COUNT(*) AS total FROM t_seer_cancer;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM t_seer_cancer;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 9176963 THEN
			RAISE INFO 'Table: t_seer_cancer has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: t_seer_cancer has % rows; expecting 9176963', c1_rec.total;
		END IF;
	END;
	$$;
    ```
  - SQL Server:
    ```sql
	SELECT COUNT(*) AS total FROM rif_data.t_seer_cancer;
	DECLARE c1 CURSOR FOR
		SELECT COUNT(*) AS total FROM rif_data.t_seer_cancer;
	DECLARE @c1_total AS INTEGER;
	OPEN c1;
	FETCH NEXT FROM c1 INTO @c1_total;
	IF @c1_total = 9176963
		PRINT 'Table: t_seer_cancer has 9176963 rows';
	ELSE
		RAISERROR('Table: t_seer_cancer has %i rows; expecting 9176963', 16, 1, @c1_total);
	CLOSE c1;
	DEALLOCATE c1;
	GO
    ```
* Add indexes to numerator table:
  ```sql
	CREATE INDEX seer_cancer_age_sex_group
	  ON rif_data.t_seer_cancer
	  (age_sex_group);

	CREATE INDEX seer_cancer_cb_2014_us_county_500k
	  ON rif_data.t_seer_cancer
	  (cb_2014_us_county_500k);

	CREATE INDEX seer_cancer_cb_2014_us_nation_5m
	  ON rif_data.t_seer_cancer
	  (cb_2014_us_nation_5m);

	CREATE INDEX seer_cancer_cb_2014_us_state_500k
	  ON rif_data.t_seer_cancer
	  (cb_2014_us_state_500k);

	CREATE INDEX seer_cancer_icdot10v
	  ON rif_data.t_seer_cancer
	  (icdot10v);

	CREATE INDEX seer_cancer_reg
	  ON rif_data.t_seer_cancer
	  (reg);

	CREATE INDEX seer_cancer_year
	  ON rif_data.t_seer_cancer
	  (year);
  ```
* Comment numerator table and columns;
  - Postgres:
    ```sql
	COMMENT ON TABLE rif_data.t_seer_cancer
	  IS 'SEER Cancer data 1973-2013. 9 States in total';
	COMMENT ON COLUMN rif_data.t_seer_cancer.year IS 'Year';
	COMMENT ON COLUMN rif_data.t_seer_cancer.cb_2014_us_nation_5m IS 'United States to county level including territories';
	COMMENT ON COLUMN rif_data.t_seer_cancer.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
	COMMENT ON COLUMN rif_data.t_seer_cancer.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
	COMMENT ON COLUMN rif_data.t_seer_cancer.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
	COMMENT ON COLUMN rif_data.t_seer_cancer.icdot10v IS 'ICD 10 site code - recoded from ICD-O-2 to 10';
	COMMENT ON COLUMN rif_data.t_seer_cancer.pubcsnum IS 'Patient ID';
	COMMENT ON COLUMN rif_data.t_seer_cancer.seq_num IS 'Sequence number';
	COMMENT ON COLUMN rif_data.t_seer_cancer.histo3v IS 'Histologic Type ICD-O-3';
	COMMENT ON COLUMN rif_data.t_seer_cancer.beho3v IS 'Behavior code ICD-O-3';
	COMMENT ON COLUMN rif_data.t_seer_cancer.rac_reca IS 'Race recode A (WHITE, BLACK, OTHER)';
	COMMENT ON COLUMN rif_data.t_seer_cancer.rac_recy IS 'Race recode Y (W, B, AI, API)';
	COMMENT ON COLUMN rif_data.t_seer_cancer.origrecb IS 'Origin Recode NHIA (HISPANIC, NON-HISP)';
	COMMENT ON COLUMN rif_data.t_seer_cancer.codpub IS 'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)';
	COMMENT ON COLUMN rif_data.t_seer_cancer.reg IS 'SEER registry (minus 1500 so same as population file)';

	COMMENT ON VIEW rif_data.seer_cancer
	  IS 'SEER Cancer data 1973-2013 view test. 9 States in total';
	COMMENT ON COLUMN rif_data.seer_cancer.year IS 'Year';
	COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_nation_5m IS 'United States to county level including territories';
	COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
	COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
	COMMENT ON COLUMN rif_data.seer_cancer.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
	COMMENT ON COLUMN rif_data.seer_cancer.icdot10v IS 'ICD 10 site code - recoded from ICD-O-2 to 10';
	COMMENT ON COLUMN rif_data.seer_cancer.pubcsnum IS 'Patient ID';
	COMMENT ON COLUMN rif_data.seer_cancer.seq_num IS 'Sequence number';
	COMMENT ON COLUMN rif_data.seer_cancer.histo3v IS 'Histologic Type ICD-O-3';
	COMMENT ON COLUMN rif_data.seer_cancer.beho3v IS 'Behavior code ICD-O-3';
	COMMENT ON COLUMN rif_data.seer_cancer.rac_reca IS 'Race recode A (WHITE, BLACK, OTHER)';
	COMMENT ON COLUMN rif_data.seer_cancer.rac_recy IS 'Race recode Y (W, B, AI, API)';
	COMMENT ON COLUMN rif_data.seer_cancer.origrecb IS 'Origin Recode NHIA (HISPANIC, NON-HISP)';
	COMMENT ON COLUMN rif_data.seer_cancer.codpub IS 'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)';
	COMMENT ON COLUMN rif_data.seer_cancer.reg IS 'SEER registry (minus 1500 so same as population file)';
    ```
  - SQL Server:
    ```sql
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'SEER Cancer data 1973-2013. 9 States in total',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer';
	GO

	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Year',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'year';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'United States to county level including territories',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'cb_2014_us_nation_5m';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'State geographic Names Information System (GNIS) code',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'cb_2014_us_state_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'cb_2014_us_county_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'RIF age_sex_group 1 (21 bands)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'age_sex_group';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'ICD 10 site code - recoded from ICD-O-2 to 10',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'icdot10v';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Patient ID',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'pubcsnum';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Sequence number',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'seq_num';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Histologic Type ICD-O-3',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'histo3v';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Behavior code ICD-O-3',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'beho3v';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Race recode A (WHITE, BLACK, OTHER)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'rac_reca';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Race recode Y (W, B, AI, API)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'rac_recy';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Origin Recode NHIA (HISPANIC, NON-HISP)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'origrecb';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'codpub';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'SEER registry (minus 1500 so same as population file)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 't_seer_cancer',
		@level2type = N'Column', @level2name = 'reg';
	GO

	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'SEER Cancer data 1973-2013 test view. 9 States in total',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer';
	GO

	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Year',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'year';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'United States to county level including territories',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'cb_2014_us_nation_5m';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'State geographic Names Information System (GNIS) code',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'cb_2014_us_state_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'cb_2014_us_county_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'RIF age_sex_group 1 (21 bands)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'age_sex_group';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'ICD 10 site code - recoded from ICD-O-2 to 10',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'icdot10v';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Patient ID',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'pubcsnum';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Sequence number',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'seq_num';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Histologic Type ICD-O-3',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'histo3v';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Behavior code ICD-O-3',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'beho3v';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Race recode A (WHITE, BLACK, OTHER)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'rac_reca';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Race recode Y (W, B, AI, API)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'rac_recy';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Origin Recode NHIA (HISPANIC, NON-HISP)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'origrecb';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'codpub';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'SEER registry (minus 1500 so same as population file)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'View', @level1name = 'seer_cancer',
		@level2type = N'Column', @level2name = 'reg';
	GO
    ```
* Grant grant access on numerator tables and views to an appropriate role;
    ```sql
    GRANT SELECT ON rif_data.seer_cancer TO seer_user;
    ```
* Setup numerator tables and views:
  ```sql
  INSERT INTO rif40.rif40_tables (
	   theme,
	   table_name,
	   description,
	   year_start,
	   year_stop,
	   total_field,
	   isindirectdenominator,
	   isdirectdenominator,
	   isnumerator,
	   automatic,
	   sex_field_name,
	   age_group_field_name,
	   age_sex_group_field_name,
	   age_group_id)
	SELECT
	   'cancers',			/* theme */
	   'SEER_CANCER',		/* table_name */
	   'SEER Cancer data 1973-2013. 9 States in total',				/* description */
	   MIN(year),			/* year_start */
	   MAX(year),			/* year_stop */
	   NULL,				/* total_field */
	   0,					/* isindirectdenominator */
	   0,					/* isdirectdenominator */
	   1,					/* isnumerator */
	   1,					/* automatic */
	   NULL,				/* sex_field_name */
	   NULL,				/* age_group_field_name */
	   'AGE_SEX_GROUP',		/* age_sex_group_field_name */
	   1					/* age_group_id */
	  FROM rif_data.seer_cancer;

	--
	-- Setup ICD field (SEER_ICDOT10V)
	-- * ICD-O-3 histology (HISTO3V) to follow later
	--
	INSERT INTO rif40.rif40_outcome_groups(
	   outcome_type, outcome_group_name, outcome_group_description, field_name, multiple_field_count)
	SELECT
	   'ICD' AS outcome_type,
	   'SEER_ICDOT10V' AS outcome_group_name,
	   'SEER ICDOT10V' AS outcome_group_description,
	   'ICDOT10V' AS field_name,
	   0 AS multiple_field_count
	WHERE NOT EXISTS (SELECT outcome_group_name FROM  rif40.rif40_outcome_groups WHERE outcome_group_name = 'SEER_ICDOT10V');

	INSERT INTO rif40.rif40_table_outcomes (
	   outcome_group_name,
	   numer_tab,
	   current_version_start_year)
	SELECT
	   'SEER_ICDOT10V',
	   'SEER_CANCER',
	   MIN(year)
	FROM rif_data.seer_cancer;
  ```

## Denominator

## Pre Processing

* Create denominator load tables:
  - Postgres:
    ```sql
	DROP TABLE IF EXISTS :USER.seer_wbo_single_ages_fixed_length;
	DROP TABLE IF EXISTS :USER.seer_wbo_single_ages;

	--
	-- Load singleages.txt as a fixed length record
	--
	CREATE TABLE seer_wbo_single_ages_fixed_length (
		record_value VARCHAR(28)
	);
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Load singleages.txt as a fixed length record:
  - Postgres:
    ```sql
	\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\white_black_other\yr1973_2013.seer9\singleages.txt' WITH CSV;
	\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\expanded.race.by.hispanic\yr1992_2013.seer9.plus.sj_la_rg_ak\singleages.txt' WITH CSV;
	\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\expanded.race.by.hispanic\yr2000_2013.ca_ky_lo_nj_ga\singleages.txt' WITH CSV;
	\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\expanded.race.by.hispanic\yr2005.lo_2nd_half\singleages.txt' WITH CSV;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Check all denominator table data has been loaded:
  - Postgres:
    ```sql
	SELECT COUNT(*) AS total FROM seer_wbo_single_ages_fixed_length;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM seer_wbo_single_ages_fixed_length;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 10234237 THEN
			RAISE INFO 'Table: seer_wbo_single_ages_fixed_length has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: seer_wbo_single_ages_fixed_length has % rows; expecting 10234237', c1_rec.total;
		END IF;
	END;
	$$;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Convert denominator fixed length string into new numerator load table with the correct columns and datatypes:
  - Postgres:
    ```sql
	CREATE TABLE seer_wbo_single_ages
	AS
	SELECT SUBSTRING(record_value FROM 1 FOR 4)::INTEGER AS year,
		   SUBSTRING(record_value FROM 5 FOR 2) AS state_postal_abbreviation,
		   SUBSTRING(record_value FROM 7 FOR 2)::Text AS state_fips_code,
		   SUBSTRING(record_value FROM 9 FOR 3)::Text AS county_fips_code,
		   SUBSTRING(record_value FROM 12 FOR 2)::INTEGER AS registry,
		   SUBSTRING(record_value FROM 14 FOR 1)::INTEGER AS race,
		   SUBSTRING(record_value FROM 15 FOR 1)::INTEGER AS origin,
		   SUBSTRING(record_value FROM 16 FOR 1)::INTEGER AS sex,
		   SUBSTRING(record_value FROM 17 FOR 2)::INTEGER AS age,
		   SUBSTRING(record_value FROM 19 FOR 10)::NUMERIC AS population
	  FROM seer_wbo_single_ages_fixed_length;
	/*
	 * Registry codes:
	 *
		01 = San Francisco-Oakland SMSA
		02 = Connecticut
		20 = Detroit (Metropolitan)
		21 = Hawaii
		22 = Iowa
		23 = New Mexico
		25 = Seattle (Puget Sound)
		26 = Utah
		27 = Atlanta (Metropolitan)
		29 = Alaska Natives
		31 = San Jose-Monterey
		35 = Los Angeles
		37 = Rural Georgia
		41 = California excluding SF/SJM/LA
		42 = Kentucky
		43 = Louisiana
		44 = New Jersey
		47 = Greater Georgia
	 */
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Create denominator table from load table:
  - Postgres:
    ```sql
	--
	-- Extract population
	-- * Convert age, sex to RIF age_sex_group 1
	-- * Convert FIPS codes to the geographic Names Information System (GNIS) codes used by the RIF
	-- * Enforce primary key
	-- * Check AGE_SEX_GROUP
	-- * Convert unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g.
	--   900 series to represent county/independent city combinations in Virginia.
	--
	DROP TABLE IF EXISTS :USER.seer_population;
	CREATE TABLE seer_population
	AS
	SELECT a.year,
		   'US'::Text AS cb_2014_us_nation_5m,
		   d.statens AS cb_2014_us_state_500k,
		   COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code) AS cb_2014_us_county_500k,
		   (a.sex*100)+b.offset AS age_sex_group,
		   SUM(a.population) AS population
	  FROM seer_wbo_single_ages a
			LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND a.age BETWEEN b.low_age AND b.high_age)
			LEFT OUTER JOIN cb_2014_us_county_500k c ON (a.state_fips_code = c.statefp AND a.county_fips_code = c.countyfp)
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
	 GROUP BY a.year, d.statens, COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code), a.sex, b.offset
	 ORDER BY 1,2,3,4,5;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Comment numerator load table:
  - Postgres:
    ```sql
	COMMENT ON TABLE seer_population IS 'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total';
	COMMENT ON COLUMN seer_population.year IS 'Year';
	COMMENT ON COLUMN seer_population.cb_2014_us_nation_5m IS 'United States to county level including territories';
	COMMENT ON COLUMN seer_population.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
	COMMENT ON COLUMN seer_population.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
	COMMENT ON COLUMN seer_population.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
	COMMENT ON COLUMN seer_population.population IS 'Population';
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Check rowcount:
  - Postgres:
    ```sql
	SELECT COUNT(*) AS total FROM seer_population;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM seer_population;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 614360 THEN
			RAISE INFO 'Table: seer_population has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: seer_population has % rows; expecting 614360', c1_rec.total;
		END IF;
	END;
	$$;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Add constraints:
  - Postgres:
    ```sql
	ALTER TABLE seer_population ADD CONSTRAINT seer_population_pk
		PRIMARY KEY (year, cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k, age_sex_group);
	ALTER TABLE seer_population ADD CONSTRAINT seer_population_asg_ck
		CHECK (age_sex_group BETWEEN 100 AND 121 OR age_sex_group BETWEEN 200 AND 221);
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Add indexes:
  - Postgres:
    ```sql
	CLUSTER seer_population USING seer_population_pk;
	CREATE INDEX seer_population_year ON seer_population (year);
	CREATE INDEX seer_population_cb_2014_us_nation_5m ON seer_population(cb_2014_us_nation_5m);
	CREATE INDEX seer_population_cb_2014_us_state_500k ON seer_population(cb_2014_us_state_500k);
	CREATE INDEX seer_population_cb_2014_us_county_500k ON seer_population(cb_2014_us_county_500k);
	CREATE INDEX seer_population_age_sex_group ON seer_population(age_sex_group);
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Unload table:
  - Postgres:
    ```sql
	\copy seer_population TO 'seer_population.csv' WITH CSV HEADER;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```

## Load Processing

* Remove denominator setup data and tables;
  - Postgres:
    ```sql
	DROP TABLE IF EXISTS rif_data.seer_population;

	DELETE FROM rif40.rif40_tables
	WHERE table_name='SEER_POPULATION';
    ```
  - SQL Server:
    ```sql
	IF OBJECT_ID('rif_data.seer_population', 'U') IS NOT NULL BEGIN
		DROP TABLE rif_data.seer_population;
		DELETE FROM rif40.rif40_tables
		WHERE table_name='SEER_POPULATION';
	END;
	GO
    ```
	If studies have already been created you will get the error:
	```
	Msg 51147, Level 16, State 1, Server PETER-PC\SAHSU, Procedure tr_rif40_tables_checks, Line 45
	Table name: [rif40].[rif40_tables], Cannot DELETE from RIF40_TABLES
	```
* Create denominator table;
  - Postgres:
    ```sql
	CREATE TABLE rif_data.seer_population
	(
	  year integer NOT NULL, -- Year
	  cb_2014_us_nation_5m text NOT NULL, -- United States to county level including territories
	  cb_2014_us_state_500k text NOT NULL, -- State geographic Names Information System (GNIS) code
	  cb_2014_us_county_500k text NOT NULL, -- County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.
	  age_sex_group integer NOT NULL, -- RIF age_sex_group 1 (21 bands)
	  population numeric, -- Population
	  CONSTRAINT seer_population_pk PRIMARY KEY (year, cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k, age_sex_group),
	  CONSTRAINT seer_population_asg_ck CHECK (age_sex_group >= 100 AND age_sex_group <= 121 OR age_sex_group >= 200 AND age_sex_group <= 221)
	);
    ```
  - SQL Server:
    ```sql
	CREATE TABLE rif_data.seer_population
	(
	  year integer NOT NULL, -- Year
	  cb_2014_us_nation_5m VARCHAR(200) NOT NULL, -- United States to county level including territories
	  cb_2014_us_state_500k VARCHAR(200) NOT NULL, -- State geographic Names Information System (GNIS) code
	  cb_2014_us_county_500k VARCHAR(200) NOT NULL, -- County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.
	  age_sex_group integer NOT NULL, -- RIF age_sex_group 1 (21 bands)
	  population numeric, -- Population
	  CONSTRAINT seer_population_pk PRIMARY KEY (year, cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k, age_sex_group),
	  CONSTRAINT seer_population_asg_ck CHECK (age_sex_group >= 100 AND age_sex_group <= 121 OR
		age_sex_group >= 200 AND age_sex_group <= 221)
	);
	GO
    ```
* Load CSV data into denominator table;
  - Postgres:
    ```sql
	\copy rif_data.seer_population FROM 'seer_population.csv' WITH CSV HEADER;
    ```
  - SQL Server:
    ```sql
	BULK INSERT rif_data.seer_population
	FROM '$(pwd)/seer_population.csv'	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line
	WITH
	(
		FIRSTROW = 2,
		FORMATFILE = '$(pwd)/seer_population.fmt',		-- Use a format file
		TABLOCK					-- Table lock
	);
	GO
    ```
* Check all denominator table data has been loaded;
  - Postgres:
    ```sql
		SELECT COUNT(*) AS total FROM seer_population;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM seer_population;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 614360 THEN
			RAISE INFO 'Table: seer_population has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: seer_population has % rows; expecting 614360', c1_rec.total;
		END IF;
	END;
	$$;
    ```
  - SQL Server:
    ```sql
	SELECT COUNT(*) AS total FROM rif_data.seer_population;
	DECLARE c1 CURSOR FOR
		SELECT COUNT(*) AS total FROM rif_data.seer_population;
	DECLARE @c1_total AS INTEGER;
	OPEN c1;
	FETCH NEXT FROM c1 INTO @c1_total;
	IF @c1_total = 614360
		PRINT 'Table: seer_population has 614360 rows';
	ELSE
		RAISERROR('Table: seer_population has %i rows; expecting 614360', 16, 1, @c1_total);
	CLOSE c1;
	DEALLOCATE c1;
	GO
    ```
* Convert denominator table to index organised table;
  - Postgres:
    ```sql
	CLUSTER rif_data.seer_population USING seer_population_pk;
    ```
  - SQL Server: not needed; automatically created as an index organised table
* Add additional indexes to denominator table:
  ```sql
  CREATE INDEX seer_population_age_sex_group
	  ON rif_data.seer_population
	  (age_sex_group);

  CREATE INDEX seer_population_cb_2014_us_county_500k
	  ON rif_data.seer_population
	  (cb_2014_us_county_500k);

  CREATE INDEX seer_population_cb_2014_us_nation_5m
	  ON rif_data.seer_population
	  (cb_2014_us_nation_5m);

  CREATE INDEX seer_population_cb_2014_us_state_500k
	  ON rif_data.seer_population
	  (cb_2014_us_state_500k);

  CREATE INDEX seer_population_year
	  ON rif_data.seer_population
	  (year);
  ```
* Comment denominator table and columns;
  - Postgres:
    ```sql
	COMMENT ON TABLE rif_data.seer_population
	  IS 'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total';
	COMMENT ON COLUMN rif_data.seer_population.year IS 'Year';
	COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_nation_5m IS 'United States to county level including territories';
	COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
	COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
	COMMENT ON COLUMN rif_data.seer_population.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
	COMMENT ON COLUMN rif_data.seer_population.population IS 'Population';
    ```
  - SQL Server:
    ```sql
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'seer_population';
	GO

	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Year',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'seer_population',
		@level2type = N'Column', @level2name = 'year';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'United States to county level including territories',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'seer_population',
		@level2type = N'Column', @level2name = 'cb_2014_us_nation_5m';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'State geographic Names Information System (GNIS) code',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'seer_population',
		@level2type = N'Column', @level2name = 'cb_2014_us_state_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'seer_population',
		@level2type = N'Column', @level2name = 'cb_2014_us_county_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Population',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'seer_population',
		@level2type = N'Column', @level2name = 'population';
	GO
    ```
* Grant grant access on denominator tables and views to an appropriate role;
  ```sql
  GRANT SELECT ON rif_data.seer_population TO seer_user;
  ```
* Setup denominator tables and views:
  ```sql
  INSERT INTO rif40.rif40_tables (
	   theme,
	   table_name,
	   description,
	   year_start,
	   year_stop,
	   total_field,
	   isindirectdenominator,
	   isdirectdenominator,
	   isnumerator,
	   automatic,
	   sex_field_name,
	   age_group_field_name,
	   age_sex_group_field_name,
	   age_group_id)
	SELECT
	   'cancers',		/* theme */
	   'SEER_POPULATION',	/* table_name */
	   'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total',	/* description */
	   MIN(YEAR),		/* year_start */
	   MAX(YEAR),		/* year_stop */
	   'POPULATION',			/* total_field */
	   1,				/* isindirectdenominator */
	   0,				/* isdirectdenominator */
	   0,				/* isnumerator */
	   1,				/* automatic */
	   NULL,			/* sex_field_name */
	   NULL,			/* age_group_field_name */
	   'AGE_SEX_GROUP',	/* age_sex_group_field_name */
	   1				/* age_group_id */
	FROM rif_data.seer_population;
  ```

## Covariates

## Pre Processing

* Create covariate load tables:
  - Postgres:
    ```sql
	DROP TABLE IF EXISTS :USER.saipe_state_county_yr1989_2015_fixed_length;
	DROP TABLE IF EXISTS :USER.saipe_state_county_yr1989_2015;

	CREATE TABLE saipe_state_county_yr1989_2015_fixed_length (
		record_value VARCHAR(265),
		year INTEGER
	);
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Load US_census_county_poverty_estimates/est&lt;year&gt;ALL.txt as a fixed length record:
  - Postgres:
    ```sql
	/*
	wc -l US_census_county_poverty_estimates/est*.txt
		3192 US_census_county_poverty_estimates/est00ALL.txt
		3193 US_census_county_poverty_estimates/est01ALL.txt
		3193 US_census_county_poverty_estimates/est02ALL.txt
		3193 US_census_county_poverty_estimates/est03ALL.txt
		3193 US_census_county_poverty_estimates/est04ALL.txt
		3193 US_census_county_poverty_estimates/est05ALL.txt
		3193 US_census_county_poverty_estimates/est06ALL.txt
		3193 US_census_county_poverty_estimates/est07ALL.txt
		3194 US_census_county_poverty_estimates/est08ALL.txt
		3195 US_census_county_poverty_estimates/est09ALL.txt
		3195 US_census_county_poverty_estimates/est10ALL.txt
		3195 US_census_county_poverty_estimates/est11all.txt
		3195 US_census_county_poverty_estimates/est12ALL.txt
		3195 US_census_county_poverty_estimates/est13ALL.txt
		3194 US_census_county_poverty_estimates/est14ALL.txt
		3194 US_census_county_poverty_estimates/est15ALL.txt
		3193 US_census_county_poverty_estimates/est89ALL.txt
		3195 US_census_county_poverty_estimates/est93ALL.txt
		3194 US_census_county_poverty_estimates/est95ALL.txt
		3193 US_census_county_poverty_estimates/est97ALL.txt
		3193 US_census_county_poverty_estimates/est98ALL.txt
		3193 US_census_county_poverty_estimates/est99ALL.txt
	   70261 total
	 */
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est15all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2015 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est14all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2014 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est13all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2013 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est12all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2012 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est11all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2011 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est10all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2010 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est09all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2009 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est08all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2008 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est07all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2007 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est06all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2006 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est05all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2005 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est04all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2004 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est03all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2003 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est02all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2002 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est01all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2001 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est00all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2000 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est99all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1999 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est98all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1998 WHERE year IS NULL;
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est97all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1997 WHERE year IS NULL;
	-- No county level 96 data
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est95all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1995 WHERE year IS NULL;
	-- No county level 94 data
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est93all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1993 WHERE year IS NULL;
	-- No county level 90-92 data
	\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est89all.txt' WITH CSV;
	UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1989 WHERE year IS NULL;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Check all covariate table data has been loaded:
  - Postgres:
    ```sql
	SELECT COUNT(*) AS total FROM saipe_state_county_yr1989_2015_fixed_length;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM saipe_state_county_yr1989_2015_fixed_length;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 70261 THEN
			RAISE INFO 'Table: saipe_state_county_yr1989_2015_fixed_length has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: saipe_state_county_yr1989_2015_fixed_length has % rows; expecting 70261', c1_rec.total;
		END IF;
	END;
	$$;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Convert covariate fixed length string into new covariate load table with the correct columns and datatypes:
  - Postgres:
    ```sql
	CREATE TABLE saipe_state_county_yr1989_2015
	AS
	SELECT LPAD(LTRIM(SUBSTRING(record_value FROM 1 FOR 2)), 2, '0')::Text AS state_fips_code,	 	/* State FIPS code (00 for US record) */
		   LPAD(LTRIM(SUBSTRING(record_value FROM 4 FOR 3)), 3, '0')::Text AS county_fips_code, 	/* County FIPS code ( 0 for US or state level records) */
		   CASE
				WHEN LTRIM(SUBSTRING(record_value FROM 8 FOR 8)) IN ('.', '') THEN NULL::INTEGER
				ELSE LTRIM(SUBSTRING(record_value FROM 8 FOR 8))::INTEGER
		   END AS total_poverty_all_ages, 	/* Estimate of people of all ages in poverty */
		   CASE
				WHEN LTRIM(SUBSTRING(record_value FROM 35 FOR 4)) IN ('. ', '') THEN NULL::NUMERIC
				ELSE LTRIM(SUBSTRING(record_value FROM 35 FOR 4))::NUMERIC
		   END AS pct_poverty_all_ages, 	/* Estimate percent of people of all ages in poverty */
		   CASE
				WHEN LTRIM(SUBSTRING(record_value FROM 77 FOR 4)) IN ('. ', '') THEN NULL::NUMERIC
				ELSE LTRIM(SUBSTRING(record_value FROM 77 FOR 4))::NUMERIC
		   END AS pct_poverty_0_17, 	/* Estimated percent of people age 0-17 in poverty */
		   CASE
				WHEN LTRIM(SUBSTRING(record_value FROM 119 FOR 4)) IN ('. ', '') THEN NULL::NUMERIC
				ELSE LTRIM(SUBSTRING(record_value FROM 119 FOR 4))::NUMERIC
		   END AS pct_poverty_related_5_17, 	/* Estimated percent of related children age 5-17 in families in poverty */
		   CASE
				WHEN LTRIM(SUBSTRING(record_value FROM 134 FOR 6)) IN ('.', '') THEN NULL::INTEGER
				ELSE LTRIM(SUBSTRING(record_value FROM 134 FOR 6))::INTEGER
		   END AS median_household_income, 	/* Estimate of median household income */
		   SUBSTRING(record_value FROM 243 FOR 12) AS file_name,
		   CASE
				WHEN SUBSTRING(record_value FROM 246 FOR 2) = '' THEN NULL::INTEGER
				ELSE SUBSTRING(record_value FROM 246 FOR 2)::INTEGER
		   END AS yr,
		   year
	  FROM saipe_state_county_yr1989_2015_fixed_length;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Create covariate table from load table:
  - Postgres:
    ```sql
	DROP TABLE IF EXISTS :USER.saipe_state_poverty_1989_2015;
	CREATE TABLE saipe_state_poverty_1989_2015
	AS
	WITH a AS (
		SELECT CASE
					WHEN yr IS NULL THEN year
					WHEN yr < 50 THEN 2000+yr
					ELSE 1900+yr
			   END AS year,
			   'US'::Text AS cb_2014_us_nation_5m,
			   d.statens AS cb_2014_us_state_500k,
			   total_poverty_all_ages,
			   pct_poverty_all_ages,
			   pct_poverty_0_17,
			   pct_poverty_related_5_17,
			   median_household_income
		  FROM saipe_state_county_yr1989_2015 a
				LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
		 WHERE a.county_fips_code = '000'
		   AND a.state_fips_code  != '00'
	)
	SELECT a.year, a.cb_2014_us_nation_5m, a.cb_2014_us_state_500k,
		   a.total_poverty_all_ages,
		   a.pct_poverty_all_ages,
		   a.pct_poverty_0_17,
		   a.pct_poverty_related_5_17,
		   a.median_household_income,
		   CASE WHEN a.median_household_income IS NOT NULL THEN NTILE(5)
				OVER (PARTITION BY a.year ORDER BY a.median_household_income)
				ELSE NULL END AS median_hh_income_quin,
		   CASE WHEN a.pct_poverty_0_17 IS NOT NULL THEN NTILE(5)
				OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_0_17)
				ELSE NULL END AS med_pct_not_in_pov_0_17_quin,
		   CASE WHEN a.pct_poverty_related_5_17 IS NOT NULL THEN NTILE(5)
				OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_related_5_17)
				ELSE NULL END AS med_pct_not_in_pov_5_17r_quin,
		   CASE WHEN a.pct_poverty_all_ages IS NOT NULL THEN NTILE(5)
				OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_all_ages)
				ELSE NULL END AS med_pct_not_in_pov_quin
	  FROM a
	 ORDER BY 1,2,3;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Comment covariate load table:
  - Postgres:
    ```sql
	COMMENT ON TABLE saipe_state_poverty_1989_2015 IS 'US Census Small Area Income and Poverty Estimates 1989-2015 by county';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.year IS 'Year';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.cb_2014_us_nation_5m IS 'United States to county level including territories';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.median_household_income IS 'Estimate of median household income';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
	COMMENT ON COLUMN saipe_state_poverty_1989_2015.med_pct_not_in_pov_5_17r_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Check rowcount:
  - Postgres:
    ```sql
	SELECT COUNT(*) AS total FROM saipe_state_poverty_1989_2015;
	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM saipe_state_poverty_1989_2015;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 1122 THEN
			RAISE INFO 'Table: saipe_state_poverty_1989_2015 has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: saipe_state_poverty_1989_2015 has % rows; expecting 1122', c1_rec.total;
		END IF;
	END;
	$$;
    ```
* Cope with holes:
  - No county/state level 96 data; use 95 data
  - No county/state level 94 data; use 93 data
  - No county/state level 90-92 data; use 89 data
  E.g.:
  - Postgres:
    ```sql
	INSERT INTO saipe_state_poverty_1989_2015(year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin)
	SELECT 1996 AS year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin
	  FROM saipe_state_poverty_1989_2015 WHERE year = 1995;
	INSERT INTO saipe_state_poverty_1989_2015(year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin)
	SELECT 1994 AS year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin
	  FROM saipe_state_poverty_1989_2015 WHERE year = 1993;
	INSERT INTO saipe_state_poverty_1989_2015(year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin)
	SELECT 1992 AS year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin
	  FROM saipe_state_poverty_1989_2015 WHERE year = 1989;
	INSERT INTO saipe_state_poverty_1989_2015(year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin)
	SELECT 1991 AS year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin
	  FROM saipe_state_poverty_1989_2015 WHERE year = 1989;
	INSERT INTO saipe_state_poverty_1989_2015(year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin)
	SELECT 1990 AS year, cb_2014_us_nation_5m, cb_2014_us_state_500k, total_poverty_all_ages,
		pct_poverty_all_ages, pct_poverty_0_17, pct_poverty_related_5_17, median_household_income,
		median_hh_income_quin, med_pct_not_in_pov_0_17_quin, med_pct_not_in_pov_5_17r_quin, med_pct_not_in_pov_quin
	  FROM saipe_state_poverty_1989_2015 WHERE year = 1989;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Add constraints:
  - Postgres:
    ```sql
	ALTER TABLE saipe_state_poverty_1989_2015 ADD CONSTRAINT saipe_state_poverty_1989_2015_pk
		PRIMARY KEY (year, cb_2014_us_state_500k);
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Add indexes:
  - Postgres:
    ```sql
	CREATE INDEX saipe_state_poverty_1989_2015_year ON saipe_state_poverty_1989_2015 (year);
	CREATE INDEX saipe_state_poverty_1989_2015_cb_2014_us_nation_5m ON saipe_state_poverty_1989_2015(cb_2014_us_nation_5m);
	CREATE INDEX saipe_state_poverty_1989_2015_cb_2014_us_state_500k ON saipe_state_poverty_1989_2015(cb_2014_us_state_500k);

	CLUSTER saipe_state_poverty_1989_2015 USING saipe_state_poverty_1989_2015_pk;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Unload table:
  - Postgres:
    ```sql

	\copy saipe_state_poverty_1989_2015 TO 'saipe_state_poverty_1989_2015.csv' WITH CSV HEADER;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```

This process is then repeated for all other covariates:

* saipe_county_poverty_1989_2015
* seer_wbo_ethnicity

We can now create the production covariate tables for both county and state level from:

* saipe_state_poverty_1989_2015
* saipe_county_poverty_1989_2015
* seer_wbo_ethnicity

E.g:

* Create production covariate table from covariate tables:
  - Postgres:
    ```sql
	DROP TABLE IF EXISTS :USER.cov_cb_2014_us_county_500k;

	CREATE TABLE cov_cb_2014_us_county_500k
	(
	  year 												integer NOT NULL, -- Year
	  cb_2014_us_county_500k 							character varying(30) NOT NULL, -- Geolevel name
	  areaname					 						character varying(200),
	  total_poverty_all_ages							INTEGER,
	  pct_poverty_all_ages								NUMERIC,
	  pct_poverty_0_17									NUMERIC,
	  pct_poverty_related_5_17							NUMERIC,
	  median_household_income							NUMERIC,
	  median_hh_income_quin								INTEGER,
	  med_pct_not_in_pov_quin							INTEGER,
	  med_pct_not_in_pov_0_17_quin						INTEGER,
	  med_pct_not_in_pov_5_17r_quin						INTEGER,
	  pct_white_quintile								INTEGER,
	  pct_black_quintile								INTEGER,
	  CONSTRAINT cov_cb_2014_us_county_500k_pkey PRIMARY KEY (year, cb_2014_us_county_500k)
	);

	--
	-- Cope with holes in SAIPE data: before 89 use 1989 data
	-- Allow holes in SEER ethnicty data
	--
	INSERT INTO cov_cb_2014_us_county_500k(year, cb_2014_us_county_500k, areaname,
		   total_poverty_all_ages,
		   pct_poverty_all_ages,
		   pct_poverty_0_17,
		   pct_poverty_related_5_17,
		   median_household_income,
		   median_hh_income_quin,
		   med_pct_not_in_pov_quin,
		   med_pct_not_in_pov_0_17_quin,
		   med_pct_not_in_pov_5_17r_quin,
		   pct_white_quintile,
		   pct_black_quintile)
	WITH a AS (
		SELECT generate_series(MIN(year), MAX(year)) AS year
		  FROM seer_population
	), b AS (
		SELECT a.year, b.cb_2014_us_county_500k, b.areaname
		  FROM a CROSS JOIN lookup_cb_2014_us_county_500k b
	)
	SELECT b.year, b.cb_2014_us_county_500k, b.areaname,
		   COALESCE(c.total_poverty_all_ages, c89.total_poverty_all_ages) AS total_poverty_all_ages,
		   COALESCE(c.pct_poverty_all_ages, c89.pct_poverty_all_ages) AS pct_poverty_all_ages,
		   COALESCE(c.pct_poverty_0_17, c89.pct_poverty_0_17) AS pct_poverty_0_17,
		   COALESCE(c.pct_poverty_related_5_17, c89.pct_poverty_related_5_17) AS pct_poverty_related_5_17,
		   COALESCE(c.median_household_income, c89.median_household_income) AS median_household_income,
		   COALESCE(c.median_hh_income_quin, c89.median_hh_income_quin) AS median_hh_income_quin,
		   COALESCE(c.med_pct_not_in_pov_quin, c89.med_pct_not_in_pov_quin) AS med_pct_not_in_pov_quin,
		   COALESCE(c.med_pct_not_in_pov_0_17_quin, c89.med_pct_not_in_pov_0_17_quin)
				AS med_pct_not_in_pov_0_17_quin,
		   COALESCE(c.med_pct_not_in_pov_5_17r_quin, c89.med_pct_not_in_pov_5_17r_quin)
				AS med_pct_not_in_pov_5_17r_quin,
		   d.pct_white_quintile,
		   d.pct_black_quintile
	  FROM b
		LEFT OUTER JOIN saipe_county_poverty_1989_2015 c ON
			(b.year = c.year AND b.cb_2014_us_county_500k = c.cb_2014_us_county_500k)
		LEFT OUTER JOIN saipe_county_poverty_1989_2015 c89 ON
			(1989 = c89.year AND b.cb_2014_us_county_500k = c89.cb_2014_us_county_500k)
			LEFT OUTER JOIN seer_wbo_ethnicity_covariates d ON
			(b.year = d.year AND b.cb_2014_us_county_500k = d.cb_2014_us_county_500k)
	 ORDER BY 1, 2;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Check rowcount:
  - Postgres:
    ```sql
	SELECT year, COUNT(year) AS total, COUNT(median_hh_income_quin) AS t_median_hh_income_quin
	  FROM cov_cb_2014_us_county_500k
	 GROUP BY year
	 ORDER BY year;

	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM cov_cb_2014_us_county_500k;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 132553 THEN
			RAISE INFO 'Table: cov_cb_2014_us_county_500k has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: cov_cb_2014_us_county_500k has % rows; expecting 132553', c1_rec.total;
		END IF;
	END;
	$$;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Comment covariate table:
  - Postgres:
    ```sql
	COMMENT ON TABLE cov_cb_2014_us_county_500k
	  IS 'Example covariate table for: The County at a scale of 1:500,000';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.year IS 'Year';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.cb_2014_us_county_500k IS 'County FIPS code (geolevel id)';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.areaname IS 'Area (county) name';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.median_household_income IS 'Estimate of median household income';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.med_pct_not_in_pov_5_17r_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.pct_white_quintile IS '% White quintile (1=least white, 5=most)';
	COMMENT ON COLUMN cov_cb_2014_us_county_500k.pct_black_quintile IS '% Black quintile (1=least black, 5=most)';
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Add indexes:
  - Postgres:
    ```sql

	--
	-- Convert to index organised table
	--
	CLUSTER cov_cb_2014_us_county_500k USING cov_cb_2014_us_county_500k_pkey;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```
* Unload table:
  - Postgres:
    ```sql
	\copy cov_cb_2014_us_county_500k TO 'cov_cb_2014_us_county_500k.csv' WITH CSV HEADER;
    ```
  - SQL Server:
    ```sql
	TO BE ADDED
    ```

## Load Processing

* Remove covariate setup data and tables;
  - Postgres:
    ```sql
    DROP TABLE IF EXISTS rif_data.cov_cb_2014_us_county_500k;
    DROP TABLE IF EXISTS rif_data.cov_cb_2014_us_state_500k;

    DELETE FROM rif40_covariates
     WHERE geography = 'USA_2014';
    ```
  - SQL Server:
    ```sql
	IF OBJECT_ID('rif_data.cov_cb_2014_us_county_500k', 'U') IS NOT NULL BEGIN
		DROP TABLE rif_data.cov_cb_2014_us_county_500k;
		DELETE FROM rif40_covariates
		 WHERE geography = 'USA_2014';
	END;
	GO
	IF OBJECT_ID('rif_data.cov_cb_2014_us_state_500k', 'U') IS NOT NULL BEGIN
		DROP TABLE rif_data.cov_cb_2014_us_state_500k;
	END;
	GO
	```
* Create covariate table;
  - Postgres:
    ```sql
    CREATE TABLE rif_data.cov_cb_2014_us_county_500k (
		year								INTEGER NOT NULL, -- Year
		cb_2014_us_county_500k 							CHARACTER VARYING(30) NOT NULL, -- Geolevel name
		areaname								CHARACTER VARYING(200),
		total_poverty_all_ages							INTEGER,
		pct_poverty_all_ages							NUMERIC,
		pct_poverty_0_17								NUMERIC,
		pct_poverty_related_5_17						NUMERIC,
		median_household_income							NUMERIC,
		median_hh_income_quin							INTEGER,
		med_pct_not_in_pov_quin							INTEGER,
		med_pct_not_in_pov_0_17_quin								INTEGER,
		med_pct_not_in_pov_5_17r_quin								INTEGER,
		pct_white_quintile								INTEGER,
		pct_black_quintile								INTEGER,
		CONSTRAINT cov_cb_2014_us_county_500k_pkey PRIMARY KEY (year, cb_2014_us_county_500k)
    );
	CREATE TABLE rif_data.cov_cb_2014_us_state_500k (
	  year integer NOT NULL, -- Year
	  cb_2014_us_state_500k character varying(30) NOT NULL, -- Geolevel name
	  areaname					 						character varying(200),
	  total_poverty_all_ages							INTEGER,
	  pct_poverty_all_ages								NUMERIC,
	  pct_poverty_0_17									NUMERIC,
	  pct_poverty_related_5_17							NUMERIC,
	  median_household_income							NUMERIC,
	  median_hh_income_quin								INTEGER,
	  med_pct_not_in_pov_quin							INTEGER,
	  med_pct_not_in_pov_0_17_quin						INTEGER,
	  med_pct_not_in_pov_5_17r_quin						INTEGER,
	  CONSTRAINT cov_cb_2014_us_state_500k_pkey PRIMARY KEY (year, cb_2014_us_state_500k)
	);
    ```
  - SQL Server:
    ```sql
	CREATE TABLE rif_data.cov_cb_2014_us_county_500k (
	  year								INTEGER NOT NULL,
	  cb_2014_us_county_500k 							VARCHAR(30) NOT NULL,
	  areaname								VARCHAR(200),
	  total_poverty_all_ages							INTEGER,
	  pct_poverty_all_ages								NUMERIC,
	  pct_poverty_0_17								NUMERIC,
	  pct_poverty_related_5_17							NUMERIC,
	  median_household_income							NUMERIC,
	  median_hh_income_quin								INTEGER,
	  med_pct_not_in_pov_quin							INTEGER,
	  med_pct_not_in_pov_0_17_quin								INTEGER,
	  med_pct_not_in_pov_5_17r_quin								INTEGER,
	  pct_white_quintile								INTEGER,
	  pct_black_quintile								INTEGER,
	  CONSTRAINT cov_cb_2014_us_county_500k_pkey PRIMARY KEY (year, cb_2014_us_county_500k)
	);
	GO
	CREATE TABLE rif_data.cov_cb_2014_us_state_500k (
	  year integer NOT NULL,
	  cb_2014_us_state_500k VARCHAR(30) NOT NULL,
	  areaname					 						VARCHAR(200),
	  total_poverty_all_ages							NUMERIC,
	  pct_poverty_all_ages								NUMERIC,
	  pct_poverty_0_17									NUMERIC,
	  pct_poverty_related_5_17							NUMERIC,
	  median_household_income							NUMERIC,
	  median_hh_income_quin								INTEGER,
	  med_pct_not_in_pov_quin							INTEGER,
	  med_pct_not_in_pov_0_17_quin						INTEGER,
	  med_pct_not_in_pov_5_17r_quin						INTEGER,
	  CONSTRAINT cov_cb_2014_us_state_500k_pkey PRIMARY KEY (year, cb_2014_us_state_500k)
	);
	GO
	```

* Load CSV data into covariate table;
  - Postgres:
    ```sql
    \copy cov_cb_2014_us_county_500k FROM 'cov_cb_2014_us_county_500k.csv' WITH CSV HEADER;
	\copy cov_cb_2014_us_state_500k FROM 'cov_cb_2014_us_state_500k.csv' WITH CSV HEADER;
    ```
  - SQL Server:
    ```sql
	BULK INSERT rif_data.cov_cb_2014_us_county_500k
	FROM '$(pwd)/cov_cb_2014_us_county_500k.csv'	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line
	WITH
	(
		FIRSTROW = 2,
		FORMATFILE = '$(pwd)/cov_cb_2014_us_county_500k.fmt',		-- Use a format file
		TABLOCK					-- Table lock
	);
	GO
	BULK INSERT rif_data.cov_cb_2014_us_state_500k
	FROM '$(pwd)/cov_cb_2014_us_state_500k.csv'	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line
	WITH
	(
		FIRSTROW = 2,
		FORMATFILE = '$(pwd)/cov_cb_2014_us_state_500k.fmt',		-- Use a format file
		TABLOCK					-- Table lock
	);
	GO
	```
* Check all covariate table data has been loaded;
  - Postgres:
    ```sql
    --
    -- Check rowcount
    --
    SELECT COUNT(*) AS total FROM cov_cb_2014_us_county_500k;

    DO LANGUAGE plpgsql $$
    DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM cov_cb_2014_us_county_500k;
		c1_rec RECORD;
    BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
    --
		IF c1_rec.total = 132553 THEN
			RAISE INFO 'Table: cov_cb_2014_us_county_500k has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: cov_cb_2014_us_county_500k has % rows; expecting 132553', c1_rec.total;
		END IF;
    END;
    $$;

	SELECT COUNT(*) AS total FROM cov_cb_2014_us_state_500k;

	DO LANGUAGE plpgsql $$
	DECLARE
		c1 CURSOR FOR
			SELECT COUNT(*) AS total
			  FROM cov_cb_2014_us_state_500k;
		c1_rec RECORD;
	BEGIN
		OPEN c1;
		FETCH c1 INTO c1_rec;
		CLOSE c1;
	--
		IF c1_rec.total = 2296 THEN
			RAISE INFO 'Table: cov_cb_2014_us_state_500k has % rows', c1_rec.total;
		ELSE
			RAISE EXCEPTION 'Table: cov_cb_2014_us_state_500k has % rows; expecting 2296', c1_rec.total;
		END IF;
	END;
	$$;
    ```
  - SQL Server:
    ```sql
    --
    -- Check rowcount
    --
	SELECT COUNT(*) AS total FROM rif_data.cov_cb_2014_us_county_500k;

	DECLARE c1 CURSOR FOR
		SELECT COUNT(*) AS total FROM rif_data.cov_cb_2014_us_county_500k;
	DECLARE @c1_total AS INTEGER;
	OPEN c1;
	FETCH NEXT FROM c1 INTO @c1_total;
	IF @c1_total = 132553
		PRINT 'Table: cov_cb_2014_us_county_500k has 132553 rows';
	ELSE
		RAISERROR('Table: cov_cb_2014_us_county_500k has %i rows; expecting 132553', 16, 1, @c1_total);
	CLOSE c1;
	DEALLOCATE c1;
	GO

	SELECT COUNT(*) AS total FROM rif_data.cov_cb_2014_us_state_500k;

	DECLARE c1 CURSOR FOR
		SELECT COUNT(*) AS total FROM rif_data.cov_cb_2014_us_state_500k;
	DECLARE @c1_total AS INTEGER;
	OPEN c1;
	FETCH NEXT FROM c1 INTO @c1_total;
	IF @c1_total = 2296
		PRINT 'Table: cov_cb_2014_us_state_500k has 2296 rows';
	ELSE
		RAISERROR('Table: cov_cb_2014_us_state_500k has %i rows; expecting 2296', 16, 1, @c1_total);
	CLOSE c1;
	DEALLOCATE c1;
	GO
	```

* Convert covariate table to index organised table;
  - Postgres:
    ```sql
	--
	-- Convert to index organised table
	--
	CLUSTER rif_data.cov_cb_2014_us_county_500k USING cov_cb_2014_us_county_500k_pkey;
	CLUSTER rif_data.cov_cb_2014_us_state_500k USING cov_cb_2014_us_state_500k_pkey;
    ```
  - SQL Server: not needed; automatically created as an index organised table

* Comment covariate table and columns;
  - Postgres:
    ```sql
    COMMENT ON TABLE rif_data.cov_cb_2014_us_county_500k
		IS 'Example covariate table for: The County at a scale of 1:500,000';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.year IS 'Year';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.cb_2014_us_county_500k IS 'County FIPS code (geolevel id)';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.areaname IS 'Area (county) name';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_household_income IS 'Estimate of median household income';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.med_pct_not_in_pov_5_17r_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_white_quintile IS '% White quintile (1=least white, 5=most)';
    COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_black_quintile IS '% Black quintile (1=least black, 5=most)';

	COMMENT ON TABLE rif_data.cov_cb_2014_us_state_500k
	  IS 'Example covariate table for: The State at a scale of 1:500,000';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.year IS 'Year';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_household_income IS 'Estimate of median household income';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
	COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.med_pct_not_in_pov_5_17r_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
    ```
  - SQL Server:
    ```sql
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Example covariate table for: The County at a scale of 1:500,000',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k';
	GO

	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Year',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'year';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'County FIPS code (geolevel id)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'cb_2014_us_county_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Area (county) name',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'areaname';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimate of people of all ages in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'total_poverty_all_ages';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimate percent of people of all ages in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'pct_poverty_all_ages';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimated percent of people age 0-17 in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'pct_poverty_0_17';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimated percent of related children age 5-17 in families in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'pct_poverty_related_5_17';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimate of median household income',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'median_household_income';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimate of median household income (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'median_hh_income_quin';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'med_pct_not_in_pov_quin';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'med_pct_not_in_pov_0_17_quin';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'med_pct_not_in_pov_5_17r_quin';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'% White quintile (1=least white, 5=most)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'pct_white_quintile';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'% Black quintile (1=least black, 5=most)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_county_500k',
		@level2type = N'Column', @level2name = 'pct_black_quintile';
	GO

	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Example covariate table for: The State at a scale of 1:500,000',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k';
	GO

	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Year',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'year';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'State geographic Names Information System (GNIS) code',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'cb_2014_us_state_500k';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Area (county) name',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'areaname';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimate of people of all ages in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'total_poverty_all_ages';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimate percent of people of all ages in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'pct_poverty_all_ages';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimated percent of people age 0-17 in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'pct_poverty_0_17';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimated percent of related children age 5-17 in families in poverty',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'pct_poverty_related_5_17';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Estimate of median household income',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'median_household_income';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimate of median household income (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'median_hh_income_quin';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'med_pct_not_in_pov_quin';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'med_pct_not_in_pov_0_17_quin';
	GO
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',
		@value = N'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)',
		@level0type = N'Schema', @level0name = 'rif_data',
		@level1type = N'Table', @level1name = 'cov_cb_2014_us_state_500k',
		@level2type = N'Column', @level2name = 'med_pct_not_in_pov_5_17r_quin';
	GO
	```

* Grant grant access on covariate tables and views to an appropriate role:
  ```sql
  --
  -- Grant
  -- * The role SEER_USER needs to be created by an administrator (```CREATE ROLE seer_user;```):
  --
  GRANT SELECT ON rif_data.cov_cb_2014_us_county_500k TO seer_user;
  GRANT SELECT ON rif_data.cov_cb_2014_us_state_500k TO seer_user;
  ```

# Information Governance

See [Database management manual - Information Governance]({{ site.baseurl }}/rifDatabase/databaseManagementManual#4-information-governance)

# Flexible Configuration Support

RIF 4.0 has a number of options for more flexible configuration:

* Configurable [age groups](#age-groups)
* Configurable [ICD field Name](#icd-field-name)
* [Automatic Numerator Denominator Pairs](#automatic-numerator-denominator-pairs).
  If a single denominator is used for a geography then numerator-denominator pairs can be automatically created.

## Age Groups

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


## ICD field Name

Numerators are currently limited to ICD 9 and 10 coding only and a single ICD field. Support will be added for ICD 11 (subject to the 
release of the 11th Edition in June 2018).

In the longer term it is expected that support will be added for:

* Multiple ICD fields;
* ICD oncology O(ICD-O-1);
* UK HES oper and A+E codes;
* User specified conditions (pre defined groups), e.g. Low birthweight, complex groups of ICD codes, the all record condition (the
  *1=1* ad-hoc SQL filter in the previous RIF).

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

## Automatic Numerator Denominator Pairs

# Quality Control

## Extract Warnings

The table/view *t_rif40_warnings/rif40_warnings* will contain contain warning messages on a study basis. These can be created
by extract or R scripts. Traps will be added for:

* Out of range or null covariates by year;
* Missing years of numerator or denominator data;
* Males/females not present when requested in numerator or denominator;
* ICD codes not present when requested in numerator;
* Mal-join detection

## Numerator Denominator Pair Errors

The normal cases if for valid numerator and denominator pairs to appear in the view *rif40_num_denom*.
```sql
sahsuland=> select * from rif40_num_denom;
 geography |   numerator_table    | numerator_description |         theme_description         | denominator_table | denominator_description | automatic
-----------+----------------------+-----------------------+-----------------------------------+-------------------+-------------------------+-----------
 SAHSULAND | NUM_SAHSULAND_CANCER | cancer numerator      | covering various types of cancers | POP_SAHSULAND_POP | population health file  |         1
(1 row)
```

This view is special in that every user has one as it then uses your permissions to determine from the RIF configuration and your permissions whether you have access to a given
numerator and denominator pair. A further view *rif40_num_denom_errors* is provided to help resolve issues:

|          Column           |            Type             |                                                                                                                             Description                                                                                                                            |
|---------------------------|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| geography                 | character varying(50)       | Geography                                                                                                                                                                                                                                                          |
| numerator_owner           | character varying           | Numerator table owner                                                                                                                                                                                                                                              |
| numerator_table           | character varying(30)       | Numerator table                                                                                                                                                                                                                                                    |
| is_numerator_resolvable   | integer                     | Is the numerator table resolvable and accessible (0/1)                                                                                                                                                                                                             |
| n_num_denom_validated     | integer                     | Is the numerator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.                                                                                                       |
| numerator_description     | character varying(250)      | Numerator table description                                                                                                                                                                                                                                        |
| denominator_owner         | character varying           | Denominator table owner                                                                                                                                                                                                                                            |
| denominator_table         | character varying(30)       | Denominator table                                                                                                                                                                                                                                                  |
| is_denominator_resolvable | integer                     | Is the denominator table resolvable and accessible (0/1)                                                                                                                                                                                                           |
| d_num_denom_validated     | integer                     | Is the denominator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.                                                                                                     |
| denominator_description   | character varying(250)      | Denominator table description                                                                                                                                                                                                                                      |
| automatic                 | integer                     | Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.                                                                     |
| auto_indirect_error_flag  | integer                     | Error flag 0/1. Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator.                          |
| auto_indirect_error       | text                        | Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator. List of geographies and tables in error. |
| n_fdw_create_status       | character varying(1)        | RIF numerator foreign data wrappers table create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors).                                                                                                                        |
| n_fdw_error_message       | character varying(300)      | RIF numerator foreign data wrappers table error message when create status is: E(Created, errors in test SELECT, N(Not created, errors).                                                                                                                           |
| n_fdw_date_created        | timestamp without time zone | RIF numerator foreign data wrappers table date FDW table created (or attempted to be).                                                                                                                                                                             |
| n_fdw_rowtest_passed      | smallint                    | RIF numerator foreign data wrappers table SELECT rowtest passed (0/1).                                                                                                                                                                                             |

```sql
sahsuland=> select * from rif40_num_denom_errors;
- geography | numerator_owner |   numerator_table    | is_numerator_resolvable | n_num_denom_validated | numerator_description | denominator_owner | denominator_table | is_denominator_resolvable | d_num_denom_validated | denominator_description | automatic | auto_indirect_error_flag | auto_indirect_error | n_fdw_create_status | n_fdw_error_message | n_fdw_date_created | n_fdw_rowtest_passed
-----------+-----------------+----------------------+-------------------------+-----------------------+-----------------------+-------------------+-------------------+---------------------------+-----------------------+-------------------------+-----------+--------------------------+---------------------+---------------------+---------------------+--------------------+----------------------
 SAHSULAND | rif_data        | NUM_SAHSULAND_CANCER |                       1 |                     1 | cancer numerator      | rif_data          | POP_SAHSULAND_POP |                         1 |                     1 | population health file  |         1 |                        0 |                     |                     |                     |                    |
 USA_2014  | rif_data        | NUM_SAHSULAND_CANCER |                       1 |                     0 | cancer numerator      | rif_data          | POP_SAHSULAND_POP |                         1 |                     0 | population health file  |         1 |                        0 |                     |                     |                     |                    |
(2 rows)
```

In this case the error is not an error as *n_num_denom_validated* and *d_num_denom_validated* are both 0 so the denominator and numerator valid for this geography:

| geography | numerator_owner |   numerator_table    | is_numerator_resolvable | n_num_denom_validated | numerator_description | denominator_owner | denominator_table | is_denominator_resolvable | d_num_denom_validated | denominator_description | automatic | auto_indirect_error_flag | auto_indirect_error |
|-----------|-----------------|----------------------|-------------------------|-----------------------|-----------------------|-------------------|-------------------|---------------------------|-----------------------|-------------------------|-----------|--------------------------|---------------------|
| SAHSULAND | rif_data        | NUM_SAHSULAND_CANCER |                       1 |                     1 | cancer numerator      | rif_data          | POP_SAHSULAND_POP |                         1 |                     1 | population health file  |         1 |                        0 |                     |
| USA_2014  | rif_data        | NUM_SAHSULAND_CANCER |                       1 |                     0 | cancer numerator      | rif_data          | POP_SAHSULAND_POP |                         1 |                     0 | population health file  |         1 |                        0 |                     |

* If *d_num_denom_validated*=1 and *is_denominator_resolvable*=0, the denominator but the table itself (rif_data.pop_sahsuland_pop) is not resolvable and/or not accessible;
* If *n_num_denom_validated*=1 and *is_numerator_resolvable*=0, the numerator but the table itself (rif_data.num_sahsuland_cancer) is not resolvable and/or not accessible;

In both these case you need to check the grants on the tables: [Viewing your user setup]({{ site.baseurl }}/rifDatabase/databaseManagementManual#25-viewing-your-user-setup)

**Peter Hambly, May 2018**
