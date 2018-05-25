Database Management Manual
==========================

# Contents

- [1. Overview](#1-overview)
- [2. User Management](#2-user-management)
  - [2.1 Creating new users](#21-creating-new-users)
     - [2.1.1 Postgres](#211-postgres)
     - [2.1.2 SQL Server](#212-sql-server)
	   -[2.1.2.1 Manually creating a new user](#2121-manually-creating-a-new-user)
  - [2.2 Changing passwords](#22-changing-passwords)
     - [2.2.1 Postgres](#221-postgres)
     - [2.2.2 SQL Server](#222-sql-server)
  - [2.3 Proxy accounts](#23-proxy-accounts)
     - [2.3.1 Postgres](#231-postgres)
     - [2.3.2 SQL Server](#232-sql-server)
  - [2.4 Granting permission](#24-granting-permission)
     - [2.4.1 Postgres](#241-postgres)
     - [2.4.2 SQL Server](#242-sql-server)
  - [2.5 Viewing your user setup](#25-viewing-your-user-setup)
     - [2.5.1 Postgres](#251-postgres)
     - [2.5.2 SQL Server](#252-sql-server)
- [3. Data Management](#3-data-management)
  - [3.1 Creating new schemas](#31-creating-new-schemas)
     - [3.1.1 Postgres](#311-postgres)
     - [3.1.2 SQL Server](#312-sql-server)
  - [3.2 Tablespaces](#32-tablespaces)
     - [3.2.1 Postgres](#321-postgres)
     - [3.2.2 SQL Server](#322-sql-server)
  - [3.3 Partitioning](#33-partitioning)
     - [3.3.1 Postgres](#331-postgres)
     - [3.3.2 SQL Server](#332-sql-server)
  - [3.4 Granting permission](#34-granting-permission)
     - [3.4.1 Postgres](#341-postgres)
     - [3.4.2 SQL Server](#342-sql-server)
- [4. Information Governance](#4-information-governance)
  - [4.1 Auditing](#41-auditing)
     - [4.1.1 Postgres](#411-postgres)
     - [4.1.2 SQL Server](#412-sql-server)
- [5. Backup and recovery](#5-backup-and-recovery)
   - [5.1 Postgres](#51-postgres)
   - [5.2 SQL Server](#52-sql-server)
- [6. Patching](#6-patching)
   - [6.1 Postgres](#61-postgres)
   - [6.2 SQL Server](#62-sql-server)
- [7. Tuning](#7-tuning)
   - [7.1 Postgres](#71-postgres)
   - [7.2 SQL Server](#72-sql-server)
	 
# 1. Overview

This manual details how to manage RIF databases. See also the:

* [Tile-maker manual](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/tileMaker.md) for how to create
  RIF administrative geographies.
* [RIF Manual Data Loading manual](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase\DataLoaderData\DataLoading.md) 
  for details on the manual process for the loading of data into the RIF.

# 2. User Management
 
## 2.1 Creating new users

New users must be created in lower case, start with a letter, and only contain the characters: ```[a-z][0-9]_```. **Do not use mixed case, upper case, dashes, space or non 
ASCII e.g. UTF8) characters**. Beware; the database stores user names internally in upper case. This is because of the RIF's Oracle heritage. 

### 2.1.1 Postgres

Run the optional script *rif40_production_user.sql*. This creates a default user *%newuser%* with password *%newpw%* in database *%newdb%* from the command environment. 
This is set from the command line using the -v newuser=<my new user> -v newpw=<my new password> and -v newdb=<my database> parameters. Run as a normal user or an 
Administrator, using the *postgres* account:

```
psql  -U postgres -d postgres -w -e -f rif40_production_user.sql -v newuser=kevin -v newpw=nivek -v newdb=sahsuland
```

* User is created with the *rif_user* (can create tables and views) and *rif_manager* roles (can also create procedures and functions);
* User can use the sahsuland database;
* Will fail to re-create a user if the user already has objects (tables, views etc);

Test connection and object privileges, access to RIF numerators and denominators:
```
C:\Users\phamb\Documents\GitHub\rapidInquiryFacility>psql -U kevin
Password for user kevin:
You are connected to database "sahsuland" as user "kevin" on host "localhost" at port "5432".
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  rif40_log_setup() DEFAULTED send DEBUG to INFO: off; debug function list: []
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.03s  rif40_startup(): SQL> SET search_path TO kevin,rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions,rif_studies;
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.06s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS kevin.rif40_run_study(INTEGER, INTEGER);
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: NOTICE:  function kevin.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.14s  rif40_startup(): Created temporary table: g_rif40_study_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.16s  rif40_startup(): Created temporary table: g_rif40_comparison_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.00s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.02s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.03s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: kevin
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.03s  rif40_startup(): VIEW rif40_user_version not found; rebuild forced
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.03s  rif40_startup(): search_path: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, rif_studies, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  analyzing "kevin.t_rif40_num_denom"
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  "t_rif40_num_denom": scanned 0 of 0 pages, containing 0 live rows and 0 dead rows; 0 rows in sample, 0 estimated total rows
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.15s  rif40_startup(): Created table: t_rif40_num_denom
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.29s  rif40_startup(): Created view: rif40_num_denom_errors
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.34s  rif40_startup(): Created view: rif40_num_denom
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.34s  rif40_startup(): V$Revision: 1.11 $ Creating view: rif40_user_version
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00001.36s  rif40_startup(): Deleted 0, created 6 tables/views/foreign data wrapper tables
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  SQL> SET search_path TO kevin,rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
psql (9.6.8)
Type "help" for help.

sahsuland=> SELECT current_database() AS db_name INTO test_table;
SELECT 1
sahsuland=> SELECT * FROM test_table;
  db_name
-----------
 sahsuland
(1 row)


sahsuland=> SELECT * FROM rif40_num_denom;
 geography |   numerator_table    |             numerator_description             |         theme_description         | denominator_table |                                 denominator_description                                  | automatic
-----------+----------------------+-----------------------------------------------+-----------------------------------+-------------------+------------------------------------------------------------------------------------------+-----------
 SAHSULAND | NUM_SAHSULAND_CANCER | cancer numerator                              | covering various types of cancers | POP_SAHSULAND_POP | population health file                                                                   |         1
(1 row)

sahsuland=> \q

C:\Users\phamb\Documents\GitHub\rapidInquiryFacility>
```

### 2.1.2 SQL Server

Run the optional script *rif40_production_user.sql*. This creates a default user *%newuser%* with password *%newpw%* in database *%newdb%* from the command environment. 
This is set from the command line using the -v newuser=<my new user> -v newpw=<my new password> and -v newdb=<my database> parameters. Run as *Administrator*:

```
sqlcmd -E -b -m-1 -e -i rif40_production_user.sql -v newuser=kevin -v newpw=XXXXXXXXXXXX -v newdb=sahsuland
```

* User is created with the *rif_user* (can create tables and views) and *rif_manager* roles (can also create procedures and functions);
* User can use the sahsuland database;
* Will fail to re-create a user if the user already has objects (tables, views etc);

Test connection and object privileges, access to RIF numerators and denominators:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U kevin -P XXXXXXXXXXXX
1> SELECT db_name() AS db_name INTO test_table;
2> SELECT * FROM test_table;
3> go

(1 rows affected)
db_name
--------------------------------------------------------------------------------------------------------------------------------
rif40

(1 rows affected)
1> SELECT * FROM rif40_num_denom;
2> go
geography                                          numerator_table                numerator_description                                                                                                                                                                                                                                      theme_description                                                                                                                                                                                        denominator_table              denominator_description                                                                                                                                                                                                                                    automatic
-------------------------------------------------- ------------------------------ ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ------------------------------ ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ---------
SAHSULAND                                          NUM_SAHSULAND_CANCER           cancer numerator                                                                                                                                                                                                                                           covering various types of cancers                                                                                                                                                                        POP_SAHSULAND_POP              population health file                                                                                                                                                                                                                                             1

(1 rows affected)
1> quit

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>
```

#### 2.1.2.1 Manually creating a new user

These instructions are based on *rif40_production_user.sql*. This uses *NEWUSER* and *NEWDB* from the CMD environment.

* Change "mydatabasename" to the name of your database, e.g. *sahsuland*;
* Change "mydatabasenuser" to the name of your user, e.g. *peter*;
* Change "mydatabasepassword" to the name of your users password;

1. Validate the RIF user
```SQL
USE [master];
GO
DECLARE @newuser VARCHAR(MAX)='mydatabaseuser';
DECLARE @invalid_chars INTEGER;
DECLARE @first_char VARCHAR(1);
SET @invalid_chars=PATINDEX('%[^0-9a-z_]%', @newuser);
SET @first_char=SUBSTRING(@newuser, 1, 1);
IF @invalid_chars IS NULL
	RAISERROR('New username is null', 16, 1, @newuser);
ELSE IF @invalid_chars > 0
	RAISERROR('New username: %s contains invalid character(s) starting at position: %i.', 16, 1, 
		@newuser, @invalid_chars);
ELSE IF (LEN(@newuser) > 30) 
	RAISERROR('New username: %s is too long (30 characters max).', 16, 1, @newuser);
ELSE IF ISNUMERIC(@first_char) = 1
	RAISERROR('First character in username: %s is numeric: %s.', 16, 1, @newuser, @first_char);
ELSE 
	PRINT 'New username: ' + @newuser + ' OK';	
GO
```

2. Create Login
```SQL
USE [master];
GO
IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name = N'mydatabaseuser')
CREATE LOGIN [mydatabaseuser] WITH PASSWORD='mydatabasepassword', CHECK_POLICY = OFF;
GO

ALTER LOGIN [mydatabaseuser] WITH DEFAULT_DATABASE = [mydatabasename];
GO	
```

3. Creare user and grant roles
```SQL
USE mydatabasename;
GO
BEGIN
	IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = N'mydatabaseuser')
	CREATE USER [mydatabaseuser] FOR LOGIN [mydatabaseuser] WITH DEFAULT_SCHEMA=[dbo]
	ELSE ALTER USER [mydatabaseuser] WITH LOGIN=[mydatabasepassword];
	
--
-- Object privilege grants
--
	GRANT CREATE TABLE TO [mydatabaseuser];
	GRANT CREATE VIEW TO [mydatabaseuser];
--
	IF NOT EXISTS (SELECT name FROM sys.schemas WHERE name = N'mydatabaseuser')
		EXEC('CREATE SCHEMA [mydatabaseuser] AUTHORIZATION [mydatabasepassword]');
	ALTER USER [mydatabaseuser] WITH DEFAULT_SCHEMA=[mydatabaseuser];
	ALTER ROLE rif_user ADD MEMBER [mydatabaseuser];
	ALTER ROLE rif_manager ADD MEMBER [mydatabaseuser];	
END;
GO
```
* **Change the password**. The password is set to *mydatabasepassword*.

4. Create user pecific object views: *rif40_num_denom*, *rif40_num_denom_errors*. These must be created as the user so they run with the users privileges and therefore only return
   RIF data tables to which the user has been granted access permission.

```SQL
USE mydatabasename;
GO
--
-- RIF40 num_denom, rif40_num_denom_errors
--
-- needs functions:
--	rif40_is_object_resolvable
--	rif40_num_denom_validate
--	rif40_auto_indirect_checks
--

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[mydatabaseuser].[rif40_num_denom]') AND type in (N'V'))
BEGIN
	DROP VIEW [mydatabaseuser].[rif40_num_denom]
END
GO

CREATE VIEW [mydatabaseuser].[rif40_num_denom] AS 
 WITH n AS (
         SELECT n1.geography,
            n1.numerator_table,
            n1.numerator_description,
            n1.automatic,
            n1.theme_description
           FROM ( SELECT g.geography,
                    n_1.table_name AS numerator_table,
                    n_1.description AS numerator_description,
                    n_1.automatic,
                    t.description AS theme_description
                   FROM [rif40].[rif40_geographies] g,
                        [rif40].[rif40_tables] n_1,
                        [rif40].[rif40_health_study_themes] t
                  WHERE n_1.isnumerator = 1 AND n_1.automatic = 1
  				    AND [rif40].[rif40_is_object_resolvable](n_1.table_name) = 1
					AND n_1.theme = t.theme) n1
          WHERE [rif40].[rif40_num_denom_validate](n1.geography, n1.numerator_table) = 1
        ), d AS (
         SELECT d1.geography,
            d1.denominator_table,
            d1.denominator_description
           FROM ( SELECT g.geography,
                    d_1.table_name AS denominator_table,
                    d_1.description AS denominator_description
                   FROM [rif40].[rif40_geographies] g,
                        [rif40].[rif40_tables] d_1
                  WHERE d_1.isindirectdenominator = 1
  				    AND d_1.automatic = 1
					AND [rif40].[rif40_is_object_resolvable](d_1.table_name) = 1) d1
          WHERE [rif40].[rif40_num_denom_validate](d1.geography, d1.denominator_table) = 1 
		    AND [rif40].[rif40_auto_indirect_checks](d1.denominator_table) IS NULL
        )
 SELECT n.geography,
    n.numerator_table,
    n.numerator_description,
    n.theme_description,
    d.denominator_table,
    d.denominator_description,
    n.automatic
   FROM n,
    d
  WHERE n.geography = d.geography
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator and indirect standardisation denominator pairs. Use RIF40_NUM_DENOM_ERROR if your numerator and denominator table pair is missing. You must have your own copy of RIF40_NUM_DENOM or you will only see the tables RIF40 has access to. Tables not rejected if the user does not have access or the table does not contain the correct geography geolevel fields.' , 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Geography', 
	@level0type=N'SCHEMA', @level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table', 
	@level0type=N'SCHEMA', @level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'numerator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table description', 
	@level0type=N'SCHEMA', @level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'numerator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table health study theme description', 
	@level0type=N'SCHEMA', @level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'theme_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table', 
	@level0type=N'SCHEMA', @level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'denominator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table description', 
	@level0type=N'SCHEMA', @level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'denominator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.' , 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'automatic'
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[mydatabaseuser].[rif40_num_denom_errors]') AND type in (N'V'))
BEGIN
	DROP VIEW [mydatabaseuser].[rif40_num_denom_errors]
END
GO

CREATE VIEW [mydatabaseuser].[rif40_num_denom_errors] AS 
 WITH n AS (
         SELECT n1.geography,
            n1.numerator_table,
            n1.numerator_description,
            n1.automatic,
            n1.is_object_resolvable,
            n1.n_num_denom_validated,
            n1.numerator_owner
           FROM ( SELECT g.geography,
                    n_1.table_name AS numerator_table,
                    n_1.description AS numerator_description,
                    n_1.automatic,
                    [rif40].[rif40_is_object_resolvable](n_1.table_name) AS is_object_resolvable,
                    [rif40].[rif40_num_denom_validate](g.geography, n_1.table_name) AS n_num_denom_validated,
                    [rif40].[rif40_object_resolve](n_1.table_name) AS numerator_owner
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] n_1
                  WHERE n_1.isnumerator = 1 AND n_1.automatic = 1) n1
        ), d AS (
         SELECT d1.geography,
            d1.denominator_table,
            d1.denominator_description,
            d1.is_object_resolvable,
            d1.d_num_denom_validated,
            d1.denominator_owner,
            [rif40].[rif40_auto_indirect_checks](d1.denominator_table) AS auto_indirect_error
           FROM ( SELECT g.geography,
                    d_1.table_name AS denominator_table,
                    d_1.description AS denominator_description,
                    [rif40].[rif40_is_object_resolvable](d_1.table_name) AS is_object_resolvable,
                    [rif40].[rif40_num_denom_validate](g.geography, d_1.table_name) AS d_num_denom_validated,
                    [rif40].[rif40_object_resolve](d_1.table_name) AS denominator_owner
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] d_1
                  WHERE d_1.isindirectdenominator = 1 AND d_1.automatic = 1) d1
        )
 SELECT n.geography,
    n.numerator_owner,
    n.numerator_table,
    n.is_object_resolvable AS is_numerator_resolvable,
    n.n_num_denom_validated,
    n.numerator_description,
    d.denominator_owner,
    d.denominator_table,
    d.is_object_resolvable AS is_denominator_resolvable,
    d.d_num_denom_validated,
    d.denominator_description,
    n.automatic,
        CASE
            WHEN d.auto_indirect_error IS NULL THEN 0
            ELSE 1
        END AS auto_indirect_error_flag,
    d.auto_indirect_error /*,
    f.create_status AS n_fdw_create_status,
    f.error_message AS n_fdw_error_message,
    f.date_created AS n_fdw_date_created,
    f.rowtest_passed AS n_fdw_rowtest_passed */
   FROM d,
    n
  WHERE n.geography = d.geography;
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'All possible numerator and indirect standardisation denominator pairs with error diagnostic fields. As this is a CROSS JOIN the will be a lot of output as tables are not rejected on the basis of user access or containing the correct geography geolevel fields.' , 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Geography', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table owner' , 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'numerator_owner'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table' , 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'numerator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the numerator table resolvable and accessible (0/1)' , 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'is_numerator_resolvable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the numerator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'n_num_denom_validated'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table description', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'numerator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table owner', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'denominator_owner'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'denominator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the denominator table resolvable and accessible (0/1)', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'is_denominator_resolvable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the denominator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'd_num_denom_validated'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table description', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'denominator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'automatic'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Error flag 0/1. Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator.', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'auto_indirect_error_flag'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator. List of geographies and tables in error.', 
	@level0type=N'SCHEMA',@level0name=N'mydatabaseuser', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'auto_indirect_error'
GO
```

## 2.2 Changing passwords

Valid characters for passwords have been tested as: ```[A-Z][a-z][0-9]!@$^~_-```. Passwords must be up to 30 characters long; longer passwords may be supported. The following are definitely **NOT** 
valid: [SQL Server/ODBC special characters](http://msdn.microsoft.com/en-us/library/windows/desktop/ms715433%28v=vs.85%29.aspx): ```[]{}(),;?*=!@```.
Use of special characters]: ```\/&%``` is not advised as command line users will need to use an escaping URI to connect.

### 2.2.1 Postgres

The file .pgpass in a user's home directory or the file referenced by PGPASSFILE can contain passwords to be used if the connection requires a password 
(and no password has been specified otherwise). On Microsoft Windows the file is named %APPDATA%\postgresql\pgpass.conf (where %APPDATA% refers to the 
Application Data subdirectory in the user's profile).

This file should contain lines of the following format:

```
hostname:port:database:username:password
```

You can add a comment to the file by preceding the line with #. Each of the first four fields can be a literal value, or *, which matches anything. The password field from the first line 
that matches the current connection parameters will be used. (Therefore, put more-specific entries first when you are using wildcards.) 
If an entry needs to contain : or \, escape this character with \. A host name of localhost matches both TCP (host name localhost) and Unix domain socket 
(pghost empty or the default socket directory) connections coming from the local machine. 

On Unix systems, the permissions on .pgpass must disallow any access to world or group; achieve this by the command chmod 0600 ~/.pgpass. If the permissions are less strict 
than this, the file will be ignored. On Microsoft Windows, it is assumed that the file is stored in a directory that is secure, so no special permissions check is made.

If you change a users password and you use the user from the command line (typically *postgres*, *rif40* and the test username) change it in the *&lt;pgpass&gt;* file.

Notes: 

* Your normal user and the administrator account will have separate *&lt;pgpass&gt;* files;
* The *postgres* account password is set by the Postgres (EnterpriseDB) installler. This password is not set in the Administrator *&lt;pgpass&gt;* file.;
* The *rif40* and the test username are both set by the database install script to &lt;username&gt;_&lt;5 digit random number&gt;_&lt;5 digit random number&gt;. 
  These passwords are set in the Administrator *&lt;pgpass&gt;* file.;
* No passwords are set in the normal user *&lt;pgpass&gt;* file;

To change a Postgres password:
```SQL
ALTER ROLE rif40 WITH PASSWORD 'XXXXXXXX';
```

### 2.2.2 SQL Server

To change a SQL server password:
```SQL
ALTER LOGIN rif40 WITH PASSWORD = 'XXXXXXXX';
GO
```
  
## 2.3 Proxy accounts

Proxy accounts are of use to the RIF as it can allow a normal user to login as a schema owner. Good practice is not the set the schema owner passwords (e.g. *rif40*) as these tend to be known by 
several people and tend to get written down as these accounts are infrequently used. Proxy accounts allow for privilege minimisation. Importantly the use proxy accounts is fully audited and privilege 
escalation (i.e. use of the proxy)  
 
The RIF front end application and middleware use user name and passwords to authenticate. Therefore federated mechanism such as [Kerberos](https://en.wikipedia.org/wiki/Kerberos_(protocol)) and 
[SSPI](https://en.wikipedia.org/wiki/Security_Support_Provider_Interface) (Windows authentication) will not work; and would require 
a [GSSAPI](https://en.wikipedia.org/wiki/Generic_Security_Services_Application_Program_Interface) implementation in the middleware. Invariably substantial browser and server key 
set-up is required and this is very difficult to set up (some years ago the SAHSU private network used this for five years).
  
### 2.3.1 Postgres

If you need to integrate into you Active Directory or authentication services you are advised to use [LDAP](https://en.wikipedia.org/wiki/Lightweight_Directory_Access_Protocol). 
This permits user name and password authentication; *ldap* does not support proxying. Login to the database using the command lines can then use *SSPI* and this can then be proxied 
to allow schema access. See Postgres [LDAP Authentication](https://www.postgresql.org/docs/9.6/static/auth-methods.html#AUTH-LDAP)

Postgres proxy accounts are controlled by *pg_ident.conf* in the Postgres data directory. See 
[Postgres Client Authentication](https://www.postgresql.org/docs/9.6/static/client-authentication.html)

The *map name* must be one of following mappable methods from *hba.conf* (i.e. that support proxying):

* ident: Identification Protocol as described in RFC 1413 (INSECURE: DO NOT USE)
* peer: Peer authentication is only available on operating systems providing the getpeereid() function, the SO_PEERCRED socket parameter, or similar mechanisms. Currently that includes Linux, most flavors of BSD including OS X, and Solaris.
* gss: GSSAPI/Kerberos
* pam: Linux PAM (Pluggable authentication modules)
* sspi: Windows native autentiation (NTLM V2)
* cert: Uses SSL client certificates to perform authentication. It is therefore only available for SSL connections.

The Windows installer guide for Postgres has examples:

* [Authentication Setup - hba.conf](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/production/windows_install_from_pg_dump.md#32-authentication-setup-hbaconf)
* [Proxy user setup - ident.conf](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/production/windows_install_from_pg_dump.md#33-proxy-user-setup-identconf)

So, if I setup SSPI as per the examples to use *SSPI* in *hba.conf*:
```
#
# Active directory GSSAPI connections with pg_ident.conf maps for schema accounts
#
hostssl	sahsuland	all	 	127.0.0.1/32 		sspi 	map=sahsuland
hostssl	sahsuland	all	 	::1/128 		sspi 	map=sahsuland
hostssl	sahsuland_dev	all	 	127.0.0.1/32 		sspi 	map=sahsuland_dev
hostssl	sahsuland_dev	all	 	::1/128 		sspi 	map=sahsuland_dev
```

With the maps *sahsuland* and *sahsuland_dev* defined in ident.conf:
```
# MAPNAME       SYSTEM-USERNAME         PG-USERNAME
#
sahsuland	pch			pop
sahsuland	pch			gis
sahsuland	pch			rif40
sahsuland	pch			pch
#
sahsuland_dev	pch			pop
sahsuland_dev	pch			gis
sahsuland_dev	pch			rif40
sahsuland_dev	pch			pch
sahsuland_dev	pch			postgres
```

Set the RIF40 password to an impossible value:

```ALTER ROLE rif40 WITH PASSWORD 'md5ac4bbe016b8XXXXXXXXXX6981f240dcae';```

Finally, optioanlly add the passwords to the 
[Pgpass](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/production/windows_install_from_pg_dump.md#31-postgres-user-password-file)

I can then logon as rif40 using SSPI:

```
C:\Users\phamb\OneDrive\SEER Data>psql -U rif40
You are connected to database "sahsuland" as user "rif40" on host "localhost" at port "5432".
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  rif40_log_setup() DEFAULTED send DEBUG to INFO: off; debug function list: []
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.01s  rif40_startup(): search_path not set for: rif40
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.01s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS rif40.rif40_run_study(INTEGER, INTEGER);
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: NOTICE:  function rif40.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.06s  rif40_startup(): Created temporary table: g_rif40_study_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.08s  rif40_startup(): Created temporary table: g_rif40_comparison_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.15s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.15s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.15s  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.15s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: rif40
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.15s  rif40_startup(): search_path: public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.16s  rif40_startup(): Deleted 0, created 2 tables/views/foreign data wrapper tables
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  SQL> SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
psql (9.6.8)
Type "help" for help.

sahsuland=>
```

### 2.3.2 SQL Server

This needs to be investigated as it is not cetain SQL SErver has the correct functionality.

TO BE ADDED. See: [Create a SQL Server Agent Proxy](https://docs.microsoft.com/en-us/sql/ssms/agent/create-a-sql-server-agent-proxy?view=sql-server-2017) as an example.

## 2.4 Granting permission

The RIF is setup so that three roles control access to the application:

* *rif_user*: User level access to the application with full access to data at the highest resolution. 
  No ability to change the RIF configuration or to add more data;
* * rif_manager*: Manager level access to the application with full access to data at the highest resolution. 
  Ability to change the RIF configuration. No ability by default to add data to the RIF. Data is normally added 
  using the schema owner account (rif40); see the above section on proxying to access the schema account user 
  a manager accounts credentials.
* *rif_student*. Restricted access to the application with controlled access to data at the higher resolutions. 
  No ability to change the RIF configuration or to add more data; 

Access to data is controlled by the permissions granted to that data and not by the RIF.

* In the *SAHSULAND* example database data access is granted to *rif_user*, *rif_manager* and *rif_student*.
* In the *SEER* dataset data access is granted to *seer_user*.
 
### 2.4.1 Postgres

To create the SEER_USER role and grant it to a user (peter) logon as the administrator (postgres):
```
psql -U postgres -d postgres
CREATE ROLE seer_user;
GRANT seer_user TO peter;
```

### 2.4.2 SQL Server

To create the SEER_USER role and grant it to a user (peter) logon as the administrator in an Administrator 
*cmd* window:
```
sqlcmd -E
USE sahsuland;
IF DATABASE_PRINCIPAL_ID('seer_user') IS NULL
	CREATE ROLE [seer_user];
SELECT name, type_desc FROM sys.database_principals WHERE name LIKE '%seer_user%';
ALTER ROLE [seer_user] ADD MEMBER [peter];
GO
```

## 2.5 Viewing your user setup

TO BE ADDED

### 2.5.1 Postgres

### 2.5.2 SQL Server

# 3. Data Management
 
## 3.1 Creating new schemas

TO BE ADDED

### 3.1.1 Postgres

### 3.1.2 SQL Server

## 3.2 Tablespaces

TO BE ADDED

### 3.2.1 Postgres

### 3.2.2 SQL Server

## 3.3 Partitioning

### 3.3.1 Postgres

Currently only the geometry tables, e.g. *rif_data.geometry_sahsuland* are partitioned.

TO BE ADDED: Postgres 10 partitioning.

See [Postgres Patching](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/rifDatabase/databaseManagementManual#61-postgres)
for a description of historic Postgres partitioning. The partitioning on the geometry tables uses the range 
partitioning schema but generates the code directly. This functionality is part of the tile maker.

### 3.3.2 SQL Server

TO BE ADDED: SQL Server partitioning and licensing conditions.

## 3.4 Granting permission

Tables or views may be granted directly to the user or indirectly via a role. Good administration proactive is to grant via a role.

To grant via a role, you must first create a role. for example create and GRANT seer_user role to a user *peter*:

### 3.4.1 Postgres

Logon as the Postgres saperuser *postgres" or other role with the *superuser* privilege.

```SQL
psql -U postgres -d postgres
CREATE ROLE seer_user;
GRANT seer_user TO peter;
```

There is no *CREATE ROLE IF NOT EXIST*.
```SQL
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'seer_user') THEN
        CREATE ROLE seer_user;
    END IF;
END
$$;
```

To view all roles: ```SELECT * FROM pg_roles;```

### 3.4.2 SQL Server

```SQL
sqlcmd -E
USE sahsuland;
IF DATABASE_PRINCIPAL_ID('seer_user') IS NULL
	CREATE ROLE [seer_user];
ALTER ROLE [seer_user] ADD MEMBER [peter];
GO
```

To view all roles: ```SELECT name, type_desc FROM sys.database_principals WHERE name LIKE '%seer_user%';```

# 4. Information Governance

## 4.1 Auditing

### 4.1.1 Postgres

Basic statement logging can be provided by the standard logging facility with the configuration parameter ```log_statement = all```. 
Postgres has an extension [pgAudit](https://github.com/pgaudit/pgaudit) which provides much more auditing, however the Enterprise DB installer does not include Postgres 
extensions (apart from PostGIS). EnterpiseDB Postgres has its own auditing subsystem (*edb_audit), but this is is paid for item. To use pgAudit pgAudit must be compile from source

To configure Postgres server [error reporting and logging](https://www.postgresql.org/docs/9.6/static/runtime-config-logging.html) set the following Postgres system parameters see
[Postgres tuning](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/databaseManagementManual.md#71-postgres) for details on how to set parameters:

* ```log_statement = all```. The default is 'none';
* ```Set log_min_error_statement = error``` [default] or lower;
* ```log_error_verbosity = verbose```;
* ```log_connections = on```;
* ```log_disconnections = on```;
* ```log_destinstion = stderr, eventlog, csvlog```. Other choices are *csvlog* or syslog*

Parameters can be set in the *postgresql.conf* file or on the server command line. This is stored in the database cluster's data directory, e.g. *C:\Program Files\PostgreSQL\9.6\data*. Beware, you can 
move the data directory to a solid state disk, mine is: * E:\Postgres\data*! Check the startup parameters in the Windows services app for the *"-D"* flag: 
```"C:\Program Files\PostgreSQL\9.6\bin\pg_ctl.exe" runservice -N "postgresql-x64-9.6" -D "E:\Postgres\data" -w```

If you are using CSV log files set:

* ```logging_collector = on```;
* ```log_filename = postgresql-%Y-%m-%d.log``` and ```log_rotation_age = 1440``` (in minutes) to provide a consistent, predictable naming scheme for your log files. This lets you predict what the file name will be and know 
  when an individual log file is complete and therefore ready to be imported. The log filename is in [strftime()](http://www.cplusplus.com/reference/ctime/strftime/) format; 
* ```log_rotation_size = 0``` to disable size-based log rotation, as it makes the log file name difficult to predict;
* ```log_truncate_on_rotation = on``` to on so that old log data isn't mixed with the new in the same file.

Create a Postgres event log custom view, create a [custom event view](https://technet.microsoft.com/en-us/library/gg131917.aspx) in the Event Viewer. 

![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/conf/postgres_event_filter_setup.png?raw=true "Postgres event log custom view")

An XML setup file [postgres_event_log_custom_view.xml](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/rifDatabase/conf/postgres_event_log_custom_view.xml) is also provided:
```xml
<ViewerConfig>
	<QueryConfig>
		<QueryParams>
			<Simple>
				<Channel>Application</Channel>
				<RelativeTimeInfo>0</RelativeTimeInfo>
				<Source>PostgreSQL</Source>
				<BySource>True</BySource>
			</Simple>
		</QueryParams>
		<QueryNode>
			<Name>PostgreSQL</Name>
			<Description>Postgres DB</Description>
			<QueryList>
				<Query Id="0" Path="Application">
					<Select Path="Application">*[System[Provider[@Name='PostgreSQL']]]</Select>
				</Query>
			</QueryList>
		</QueryNode>
	</QueryConfig>
</ViewerConfig>
```

An example log file extract (postgresql-2018-05-21.log):
```log
2018-05-21 16:20:39 BST LOG:  00000: statement: select * from rif40_num_denom;
2018-05-21 16:20:39 BST LOCATION:  exec_simple_query, postgres.c:927
```

The equivalent CSV file entry (postgresql-2018-05-21.csv) is far more detailed:
```csv
2018-05-21 16:20:39.261 BST,"peter","sahsuland",9900,"::1:62022",5b02e3be.26ac,4,"",2018-05-21 16:20:30 BST,2/237,0,LOG,00000,"statement: select * from rif40_num_denom;",,,,,,,,"exec_simple_query, postgres.c:927","RIF (psql)"
2
```

By loading the CSV log in to a table it can then be queried:

```sql
CREATE TABLE postgres_log
(
  log_time timestamp(3) with time zone,
  user_name text,
  database_name text,
  process_id integer,
  connection_from text,
  session_id text,
  session_line_num bigint,
  command_tag text,
  session_start_time timestamp with time zone,
  virtual_transaction_id text,
  transaction_id bigint,
  error_severity text,
  sql_state_code text,
  message text,
  detail text,
  hint text,
  internal_query text,
  internal_query_pos integer,
  context text,
  query text,
  query_pos integer,
  location text,
  application_name text,
  PRIMARY KEY (session_id, session_line_num)
);
CREATE TABLE
\copy postgres_log FROM 'E:\Postgres\data\pg_log\postgresql-2018-05-21.csv' WITH csv;
COPY 25
sahsuland=> SELECT * FROM postgres_log WHERE user_name = USER AND message LIKE '%select * from rif40_num_denom;';
          log_time          | user_name | database_name | process_id | connection_from |  session_id   | session_line_num | command_tag |   session_start_time   | virtual_transaction_id | transaction_id | error_severity | sql_state_code |                  message                  | detail | hint | internal_query | internal_query_pos | context | query | query_pos |             location              | application_name
----------------------------+-----------+---------------+------------+-----------------+---------------+------------------+-------------+------------------------+------------------------+----------------+----------------+----------------+-------------------------------------------+--------+------+----------------+--------------------+---------+-------+-----------+-----------------------------------+------------------
 2018-05-21 16:20:39.261+01 | peter     | sahsuland     |       9900 | ::1:62022       | 5b02e3be.26ac |                4 |             | 2018-05-21 16:20:30+01 | 2/237                  |              0 | LOG            | 00000          | statement: select * from rif40_num_denom; |        |      |                |                    |         |       |           | exec_simple_query, postgres.c:927 | RIF (psql)
(1 row)
```

Formatted the CSV log entry is:

| Log field name         | Value                                     |
|------------------------|-------------------------------------------|
| log_time               | 2018-05-21 16:20:39.261+01                | 
| user_name              | peter                                     |
| database_name          | sahsuland                                 |
| process_id             | 9900                                      |
| connection_from        | ::1:62022                                 |
| session_id             | 5b02e3be.26ac                             |
| session_line_num       | 4                                         |
| command_tag            |                                           |
| session_start_time     | 2018-05-21 16:20:30+01                    |
| virtual_transaction_id | 2/237                                     |
| transaction_id         | 0                                         |
| error_severity         | LOG                                       |
| sql_state_code         | 00000                                     |
| message                | statement: select * from rif40_num_denom; |
| detail                 |                                           |
| hint                   |                                           |
| internal_query         |                                           |
| internal_query_pos     |                                           |
| context                |                                           |
| query                  |                                           |
| query_pos              |                                           |
| location               | exec_simple_query, postgres.c:927         | 
| application_name       | RIF (psql)                                |
 
The equivalent PostgreSQL Windows log entry entry is:
 
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/conf/postgres_event_viewer_log.png?raw=true "Equivalent PostgreSQL Windows log entry entry")
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/conf/postgres_event_viewer_log2.png?raw=true "Equivalent PostgreSQL Windows log entry entry")

### 4.1.2 SQL Server

TO BE ADDED
  
# 5. Backup and recovery  

## 5.1 Postgres

TO BE ADDED: Using pg_dump/restore

## 5.2 SQL Server

TO BE ADDED

# 6. Patching  

Alter scripts are numbered sequentially and have the same functionality in both ports, e.g. ```v4_0_alter_10.sql```. Scripts are safe to run more than once.

Pre-built databases are supplied patched up to date.

Scripts must be applied as follows:

| Date            | Script            | Description                         |
|-----------------|-------------------|-------------------------------------|
| 30th June  2018 | v4_0_alter_10.sql | To be added (risk analysis changes) |

## 6.1 Postgres

Alter scripts *v4_0_alter_1.sql* to *v4_0_alter_9.sql* related to be original database development on Postgres
and were not created on SQL Server. The scripts *v4_0_alter_3.sql* and *v4_0_alter_4.sql* enable partitioning on```
ranges (data with year fields) and hashes (study_id) respectively. Alter script 4 must go after 7. Alter 
script 7 provides:
- Support for  ontologies (e.g. ICD9, 10); removed previous table based support.
- Modify t_rif40_inv_conditions to remove SQL injection risk

This is because alter script 7 was written before the partitioning was enabled and does not support it.

Partitioning was removed as it is supported natively in Postgres 10. Support for hash partitioning is
in alter 4 is incomplete:

- Partition movement is not supported; 
- The hashing function *rif40_sql_pkg._rif40_hash* has not been added to the code so hash partition elimination 
  will not occur. Range partition elimination does work.
  ```
  CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_hash(l_value VARCHAR, l_bucket INTEGER)
  RETURNS INTEGER
  AS 'SELECT (ABS(hashtext(l_value))%l_bucket)+1;' LANGUAGE sql IMMUTABLE STRICT;
  ```

The actual partitioning is carried out by: *v4_0_study_id_partitions.sql* and *v4_0_year_partitions.sql* and
the *rif40_sql_pkg.rif40_hash_partition* and *rif40_sql_pkg.rif40_range_partition* packages.

**This code has not been used since early 2018 and is supplied as is with testing since that date. RIF users
are strongly advised to use the native Postgres 10 functionality.**

Scripts are in the standard bundle in the directory *Database alter scripts\Postgres* or in github in *rapidInquiryFacility\rifDatabase\Postgres\psql_scripts\alter_scripts*

```
psql -U rif40 -d <your database name> -w -e -P pager=off -v verbosity=terse -v debug_level=0 -v use_plr=N -v pghost=localhost -v echo=none -f alter_scripts/<alter script name>
```

## 6.2 SQL Server

Scripts are in the standard bundle in the directory *Database alter scripts\SQL Server* or in github in *rapidInquiryFacility\rifDatabase\SQLserver\sahsuland_dev\alter_scripts*

```
sqlcmd -U rif40 -P <rif40 password> -d <your database name> -b -m-1 -e -r1 -i <alter script name>
```

# 7. Tuning  

## 7.1 Postgres

The best source for Postgres tuning information is at the [Postgres Performance Optimization Wiki](https://wiki.postgresql.org/wiki/Performance_Optimization). This references
[Tuning Your PostgreSQL Server](https://wiki.postgresql.org/wiki/Tuning_Your_PostgreSQL_Server)

Parameters can be set in the *postgresql.conf* file or on the server command line. This is stored in the database cluster's data directory, e.g. *C:\Program Files\PostgreSQL\9.6\data*. Beware, you can 
move the data directory to a solid state disk, mine is: * E:\Postgres\data*! Check the startup parameters in the Windows services app for the *"-D"* flag: 
```"C:\Program Files\PostgreSQL\9.6\bin\pg_ctl.exe" runservice -N "postgresql-x64-9.6" -D "E:\Postgres\data" -w```

An example [postgresql.conf](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/conf/postgresql.conf) is supplied. 
The principal tuning changes are:

* Shared buffers: 1GB
* Temporary buffers: 256MB
* Work memory: 256MB
* Try to use huge pages - this is called large page support in Windows. This is to reduce the process memory footprint 
  [translation lookaside buffer](https://answers.microsoft.com/en-us/windows/forum/windows_10-performance/physical-and-virtual-memory-in-windows-10/e36fb5bc-9ac8-49af-951c-e7d39b979938) size.

  Example parameter entries from *postgresql.conf*:
```conf
shared_buffers = 1024MB     # min 128kB; default 128 MB (9.6)
                            # (change requires restart)
temp_buffers = 256MB        # min 800kB; default 8M

huge_pages = try            # on, off, or try
                            # (change requires restart
work_mem = 256MB            # min 64kB; default 4MB
```

On Windows, I modified the *postgresql.conf* rather than use SQL at the server command line. This is because the server command line needs to be in the units of the parameters, 
so shared buffers of 1G is 131072 8KB pages. This is not very intuitive compared to using MB/GB etc.
 
The amount of memory given to Postgres should allow room for *tomcat* if installed together with the application server; shared memory should generally not exceed a quarter of the available RAM.

## 7.2 SQL Server

TO BE ADDED

Peter Hambly
Many 2018