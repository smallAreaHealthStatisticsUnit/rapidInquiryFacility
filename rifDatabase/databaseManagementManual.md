Database Management Manual
==========================

# Contents

- [1. Overview](#1-overview)
- [2. User Management](#2-user-management)
  - [2.1 Creating new users](#21-creating-new-users)
     - [2.1.1 Postgres](#211-postgres)
	   - [2.1.1.1 Manually creating a new user](#2111-manually-creating-a-new-user)
     - [2.1.2 SQL Server](#212-sql-server)
	   - [2.1.2.1 Manually creating a new user](#2121-manually-creating-a-new-user)
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
     - [5.1.1 Logical Backups](#511-logical-backups)
     - [5.1.2 Continuous Archiving and Point-in-Time Recovery](#512-continuous-archiving-and-point-in-time-recovery)
   - [5.2 SQL Server](#52-sql-server)
     - [5.2.1 Logical Backups](#521-logical-backups)
     - [5.2.2 Continuous Archiving and Point-in-Time Recovery](#522-continuous-archiving-and-point-in-time-recovery)
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

#### 2.1.1.1 Manually creating a new user

These instructions are based on *rif40_production_user.sql*. This uses *NEWUSER* and *NEWDB* from the CMD environment.

* Change "mydatabasename" to the name of your database, e.g. *sahsuland*;
* Change "mydatabasenuser" to the name of your user, e.g. *peter*;
* Change "mydatabasepassword" to the name of your users password;

1. Validate the RIF user; connect as user *postgres* on the database *postgres*:
```SQL
DO LANGUAGE plpgsql $$
BEGIN
	IF current_user != 'postgres' OR current_database() != 'postgres' THEN
		RAISE EXCEPTION 'rif40_production_user.sql() current_user: % and current database: % must both be postgres', current_user, current_database();
	END IF;
END;
$$;

```

2. Create Login; connect as user *postgres* on the database *mydatabasename* (.e.g. sahsuland):
```SQL
DO LANGUAGE plpgsql $$
DECLARE
	c2 CURSOR(l_usename VARCHAR) FOR 
		SELECT * FROM pg_user WHERE usename = l_usename;
	c3 CURSOR FOR 
		SELECT CURRENT_SETTING('rif40.nnewpw') AS nnewpw,
		       CURRENT_SETTING('rif40.newpw') AS newpw;
	c4 CURSOR(l_name VARCHAR, l_pass VARCHAR) FOR
		SELECT rolpassword::Text AS rolpassword, 
		       'md5'||md5(l_pass||l_name)::Text AS password
	  	  FROM pg_authid
	     WHERE rolname = l_name;
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	sql_stmt VARCHAR;
	u_name	VARCHAR;
	u_pass	VARCHAR;
	u_database VARCHAR;
BEGIN
	u_name:='mydatabasenuser';
	u_pass:='mydatabasepassword';
	u_database:='mydatabasename';
--
-- Test account exists
--
	OPEN c2(u_name);
	FETCH c2 INTO c2_rec;
	CLOSE c2;
	IF c2_rec.usename IS NULL THEN
		RAISE NOTICE 'C209xx: User account does not exist: %; creating', u_name;	
		sql_stmt:='CREATE ROLE '||u_name||
			' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD '''||
				CURRENT_SETTING('rif40.newpw')||'''';
		RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
		EXECUTE sql_stmt;
	ELSE
--	
		OPEN c4(u_name, u_pass);
		FETCH c4 INTO c4_rec;
		CLOSE c4;
		IF c4_rec.rolpassword IS NULL THEN
			RAISE EXCEPTION 'C209xx: User account: % has a NULL password', 
				c2_rec.usename;	
		ELSIF c4_rec.rolpassword != c4_rec.password THEN
			RAISE INFO 'rolpassword: "%"', c4_rec.rolpassword;
			RAISE INFO 'password(%):    "%"', u_pass, c4_rec.password;
			RAISE EXCEPTION 'C209xx: User account: % password (%) would change; set password correctly', c2_rec.usename, u_pass;		
		ELSE
			RAISE NOTICE 'C209xx: User account: % password is unchanged', 
				c2_rec.usename;
		END IF;
--
		IF pg_has_role(c2_rec.usename, 'rif_user', 'MEMBER') THEN
			RAISE INFO 'rif40_production_user.sql() user account="%" is a rif_user', c2_rec.usename;
		ELSIF pg_has_role(c2_rec.usename, 'rif_manager', 'MEMBER') THEN
			RAISE INFO 'rif40_production_user.sql() user account="%" is a rif manager', c2_rec.usename;
		ELSE
			RAISE EXCEPTION 'C209xx: User account: % is not a rif_user or rif_manager', c2_rec.usename;	
		END IF;
	END IF;	
--
	sql_stmt:='GRANT CONNECT ON DATABASE '||u_database||' to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='GRANT rif_manager TO '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
	sql_stmt:='GRANT rif_user TO '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
```

3. Create user and grant roles
```SQL
DO LANGUAGE plpgsql $$
DECLARE	
	sql_stmt VARCHAR;
	u_name	VARCHAR;
	u_database	VARCHAR;
BEGIN
	u_name:='mydatabasenuser';
	u_database:='mydatabasename';
	IF user = 'postgres' AND current_database() = u_database THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not postgres on % database (%)', 
			user, u_database, current_database();	
	END IF;	

--
	sql_stmt:='GRANT CONNECT ON DATABASE '||u_database||' to '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;

	sql_stmt:='CREATE SCHEMA IF NOT EXISTS '||u_name||' AUTHORIZATION '||u_name;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;	
END;
$$;
```
* **Change the password**. The password is set to *mydatabasepassword*.

The user specific object views: *rif40_num_denom*, *rif40_num_denom_errors* are automatically created. These must be created as the user so they run with the users 
privileges and therefore only return RIF data tables to which the user has been granted access permission.

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

4. Create user specific object views: *rif40_num_denom*, *rif40_num_denom_errors*. These must be created as the user so they run with the users privileges and therefore only return
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
several people and tend to get written down as these accounts are infrequently used. Proxy accounts allow for privilege minimisation. Importantly the use proxy accounts is fully audited, in 
particular the privilege escalation (i.e. use of the proxy).

The SAHSU Private network uses proxying for these reasons.  
 
The RIF front end application and middleware use user name and passwords to authenticate. Therefore federated mechanism such as [Kerberos](https://en.wikipedia.org/wiki/Kerberos_(protocol)) and 
[SSPI](https://en.wikipedia.org/wiki/Security_Support_Provider_Interface) (Windows authentication) will not work; and would require 
a [GSSAPI](https://en.wikipedia.org/wiki/Generic_Security_Services_Application_Program_Interface) implementation in the middleware. Invariably substantial browser and server key 
set-up is required and this is very difficult to set up (some years ago the SAHSU private network used this for five years; the experiment was not repeated).
  
### 2.3.1 Postgres

If you need to integrate into you Active Directory or authentication services you are advised to use [LDAP](https://en.wikipedia.org/wiki/Lightweight_Directory_Access_Protocol). 
This permits user name and password authentication; *ldap* does not support proxying. Login to the database using the command lines can then use *SSPI* and this can then be proxied 
to allow schema access. See Postgres [LDAP Authentication](https://www.postgresql.org/docs/9.6/static/auth-methods.html#AUTH-LDAP) and 
[LDAP Authentication against AD](https://wiki.postgresql.org/wiki/LDAP_Authentication_against_AD)

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

This needs to be investigated as it is not certain SQL Server has the correct functionality and the setup would need to be trialled. See:

* [Create a SQL Server Agent Proxy](https://docs.microsoft.com/en-us/sql/ssms/agent/create-a-sql-server-agent-proxy?view=sql-server-2017);
* [Creating an LDAP user authentication environment (MSSQL)](http://dcx.sap.com/sa160/en/dbusage/ug-ldap-setup-sql.html)

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

Currently only the geometry tables, e.g. *rif_data.geometry_sahsuland* are partitioned using inheritance and custom triggers. Postgres 10 has native support for partitioning, see: 
[Postgres 10 partitioning](https://www.postgresql.org/docs/10/static/ddl-partitioning. html). The implementation is still incomplete and the 
following limitations apply to partitioned tables:

* There is no facility available to create the matching indexes on all partitions automatically. Indexes must be added to each partition with separate commands. This also means that 
  there is no way to create a primary key, unique constraint, or exclusion constraint spanning all partitions; it is only possible to constrain each leaf partition individually.
* Since primary keys are not supported on partitioned tables, foreign keys referencing partitioned tables are not supported, nor are foreign key references from a partitioned table 
  to some other table.
* Using the ON CONFLICT clause with partitioned tables will cause an error, because unique or exclusion constraints can only be created on individual partitions. There is no support 
  for enforcing uniqueness (or an exclusion constraint) across an entire partitioning hierarchy.
* An UPDATE that causes a row to move from one partition to another fails, because the new value of the row fails to satisfy the implicit partition constraint of the original partition.
* Row triggers, if necessary, must be defined on individual partitions, not the partitioned table.

See [Postgres Patching](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/rifDatabase/databaseManagementManual#61-postgres)
for a description of historic Postgres partitioning. The partitioning on the geometry tables uses the range 
partitioning schema but generates the code directly. This functionality is part of the tile maker.

### 3.3.2 SQL Server

SQL Server supports table and index partitioning, see [Partitioned Tables and Indexes](https://docs.microsoft.com/en-us/sql/relational-databases/partitions/partitioned-tables-and-indexes?view=sql-server-2017)
Beware of the [SQL Server partitioning and licensing conditions](https://download.microsoft.com/download/9/C/6/9C6EB70A-8D52-48F4-9F04-08970411B7A3/SQL_Server_2016_Licensing_Guide_EN_US.pdf); 
you may need a full enterprise license.

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

See: 

[Creating a successful auditing strategy for your SQL Server databases](https://www.sqlshack.com/creating-successful-auditing-strategy-sql-server-databases/)
[SQL Server Audit](https://docs.microsoft.com/en-us/sql/relational-databases/security/auditing/sql-server-audit-database-engine?view=sql-server-2017)
[Create a Server Audit and Database Audit Specification](https://docs.microsoft.com/en-us/sql/relational-databases/security/auditing/create-a-server-audit-and-database-audit-specification?view=sql-server-2017) 
 
To setup *(Common criteria compliance*:

* Use the SQL Server management studio server properties pane: 
  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/auditing.PNG?raw=true "SQL Server auditing setup");
* Also check audit failed and successful logins;  
* Restart SQL Server;

TO BE ADDED: auditing DDL and DML (without using triggers!)

# 5. Backup and recovery  

As with all relational databases; cold backups are recommended as a baselines and should be carried out using your enterprise backup tools with the database down. Two further backup solutions are
suggested:

* Logical backups (recommended given the likely size of most RIF databses);
* Continuous archiving and point-in-time recovery (PITR) for more advanced sites;

Because of the fully transactional nature of both Postgres and SQL Server consistent logical backups can be run with users logged on, the database does not need to be put into a quiescent state.

Please bear in mind that continuous archiving and point-in-time recovery greatly expands the recovery options allowing for corruption repair and recovery from
object deletion incidents.

Replication is invariably very complex and is beyond the scope of this manual. It is recommended for very large sites with Postgres because of Postgres' poor support for corruption 
detection and repair. 

## 5.1 Postgres

### 5.1.1 Logical Backups

Postgres logical backup and recovery uses *pg_dump* and *pg_restore*. pg_dump only dumps a single database. To backup global objects that are common to all databases in a cluster, such as roles and tablespaces, 
use *pg_dumpall*.

Two basic formats: 1) a SQL script to recreate the database using *psql* and 2) a binary dump file to restore using *pg_restore*.

1. SQL Script: ```pg_dump -U postgres -w -F plain -v -C sahsuland > sahsuland.sql```	
2. Binary dump file: ```pg_dump -U postgres -w -F custom -v sahsuland > sahsuland.dump```

Where the database name is *sahsuland*
	
Flags:

* *-U postgres*:
* "-F &lt;format%gt;*: Format: plain (SQL), custom or directory (pg_restore);
* *-w*: Do not prompt for a password;
* *-v*: Be verbose;
 
To restore a custom or directory pg_dump* file: ```pg_restore -d sahsuland -U postgres -v sahsuland.dump```. This is the method uses to create the example database *sahsuland* 
from the development database *sahsuland_dev*.

See: 

* [pd_dump](https://www.postgresql.org/docs/9.6/static/app-pgdump.html)
* [pg_restore](https://www.postgresql.org/docs/9.6/static/app-pgrestore.html)
 
### 5.1.2 Continuous Archiving and Point-in-Time Recovery 

Postgres supports continuous archiving and point-in-time recovery (PITR).

See: 

* [Continuous Archiving and Point-in-Time Recovery](https://www.postgresql.org/docs/9.6/static/continuous-archiving.html)
* [Postgres Corruption WIKI](https://wiki.postgresql.org/wiki/Corruption)
* [Postgres replication](https://www.postgresql.org/docs/9.6/static/different-replication-solutions.html)
 
## 5.2 SQL Server

### 5.2.1 Logical Backups

SQL Server logical backup and restore are SQL commands entered using ```sqlcmd```. See:

* [Back Up and Restore of SQL Server Databases](https://docs.microsoft.com/en-us/sql/relational-databases/backup-restore/back-up-and-restore-of-sql-server-databases?view=sql-server-2017)
* [Create a full database backup](https://docs.microsoft.com/en-us/sql/relational-databases/backup-restore/create-a-full-database-backup-sql-server?view=sql-server-2017)
* [BACKUP command](https://docs.microsoft.com/en-us/sql/t-sql/statements/backup-transact-sql?view=sql-server-2017)
* [RESTORE command](https://docs.microsoft.com/en-us/sql/t-sql/statements/restore-statements-transact-sql?view=sql-server-2017)

```
BACKUP DATABASE [sahsuland] TO DISK='C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\production\sahsuland.bak' 
  WITH COPY_ONLY, INIT;
GO
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 6
Processed 42040 pages for database 'sahsuland_dev', file 'sahsuland_dev' on file 54.
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 6
Processed 2 pages for database 'sahsuland_dev', file 'sahsuland_dev_log' on file 54.
Msg 3014, Level 0, State 1, Server PETER-PC\SAHSU, Line 6
BACKUP DATABASE successfully processed 42042 pages in 3.940 seconds (83.363 MB/sec).
```

To restore a backup:

```
RESTORE DATABASE [sahsuland]
        FROM DISK='C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\production\sahsuland.bak'
        WITH REPLACE;
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
Processed 45648 pages for database 'sahsuland', file 'sahsuland' on file 1.
Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
Processed 14 pages for database 'sahsuland', file 'sahsuland_log' on file 1.
Msg 3014, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
RESTORE DATABASE successfully processed 45662 pages in 5.130 seconds (69.538 MB/sec).
```

See the script *rif40_production_creation.sql* if you want to rename the database, its files or to move the files.
 
### 5.2.2 Continuous Archiving and Point-in-Time Recovery 

This requires a transaction log backup, i.e. not the copy only version created in the previous section. You will need to do a full backup, followed by differential backups:

```
-- Create a full database backup first.  
BACKUP DATABASE sahsuland   
   TO sahsuland   
   WITH INIT;  
GO  
-- Time elapses.  
-- Create a differential database backup, appending the backup  
-- to the backup device containing the full database backup.  
BACKUP DATABASE sahsuland  
   TO sahsuland  
   WITH DIFFERENTIAL;  
GO 
```

Log backups require the database to be in the full recovery model, not the default simple recovery model. See:
[Back Up a Transaction Log](https://docs.microsoft.com/en-us/sql/relational-databases/backup-restore/back-up-a-transaction-log-sql-server?view=sql-server-2017)

For restoration see:  
[Restore a SQL Server Database to a Point in Time (Full Recovery Model](https://docs.microsoft.com/en-us/sql/relational-databases/backup-restore/restore-a-sql-server-database-to-a-point-in-time-full-recovery-model?view=sql-server-2017)

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

The following aspects of tuning are covered:

* Server memory allocation
* Huge/large page support
* RIF application tuning

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

On Postgres the extract queries all do an ```EXPLAIN PLAN VERBOSE``` to the log:
```
+00037.83s  [DEBUG1] rif40_execute_insert_statement(): [56005] SQL> EXPLAIN (VERBOSE, FORMAT text)
INSERT INTO s416_extract (
	year,study_or_comparison,study_id,area_id,band_id,sex,age_group,test_1002,total_pop) /* 1 numerator(s) */
WITH n1 AS (	/* NUM_SAHSULAND_CANCER - cancer numerator */
	SELECT s.area_id		/* Study or comparision resolution */,
	       c.year,
	       c.age_sex_group AS n_age_sex_group,
	       SUM(CASE 		/* Numerators - can overlap */
			WHEN ((	/* Investigation 1 ICD filters */
				    icd LIKE 'C33%' /* Value filter */ /* Filter 1 */
				 OR icd LIKE 'C340%' /* Value filter */ /* Filter 2 */
				 OR icd LIKE 'C341%' /* Value filter */ /* Filter 3 */
				 OR icd LIKE 'C342%' /* Value filter */ /* Filter 4 */
				 OR icd LIKE 'C343%' /* Value filter */ /* Filter 5 */
				 OR icd LIKE 'C348%' /* Value filter */ /* Filter 6 */
				 OR icd LIKE 'C349%' /* Value filter */ /* Filter 7 */) /* 7 lines of conditions: study: 416, inv: 414 */
			AND (1=1
			   AND  c.year BETWEEN 1995 AND 1996/* Investigation 1 year filter */
				        /* No genders filter required for investigation 1 */
				        /* No age group filter required for investigation 1 */)
			) THEN total
			ELSE 0
	       END) inv_414_test_1002	/* Investigation 1 -  */ 
	  FROM rif40_study_areas s,	/* Numerator study or comparison area to be extracted */
	       num_sahsuland_cancer c	/* cancer numerator */
	 WHERE c.sahsu_grd_level4 = s.area_id 	/* Study selection */
	   AND (
				    icd LIKE 'C33%' /* Value filter */ /* Filter 1 */
				 OR icd LIKE 'C340%' /* Value filter */ /* Filter 2 */
				 OR icd LIKE 'C341%' /* Value filter */ /* Filter 3 */
				 OR icd LIKE 'C342%' /* Value filter */ /* Filter 4 */
				 OR icd LIKE 'C343%' /* Value filter */ /* Filter 5 */
				 OR icd LIKE 'C348%' /* Value filter */ /* Filter 6 */
				 OR icd LIKE 'C349%' /* Value filter */ /* Filter 7 */)
				        /* No genders filter required for numerator (only one gender used) */
	       /* No age group filter required for numerator */
	   AND s.study_id = 416		/* Current study ID */
	   AND c.year = 1995		/* Numerator (INSERT) year filter */
	 GROUP BY c.year, s.area_id, s.band_id,
	          c.age_sex_group
) /* NUM_SAHSULAND_CANCER - cancer numerator */
, d AS (
	SELECT d1.year, s.area_id, s.band_id, d1.age_sex_group,
	       SUM(COALESCE(d1.total, 0)) AS total_pop
	  FROM rif40_study_areas s, pop_sahsuland_pop d1 	/* Denominator study or comparison area to be extracted */
	 WHERE d1.year = 1995		/* Denominator (INSERT) year filter */
	   AND s.area_id  = d1.sahsu_grd_level4	/* Study geolevel join */
	   AND s.area_id  IS NOT NULL	/* Exclude NULL geolevel */
	   AND s.study_id = 416		/* Current study ID */
	       /* No age group filter required for denominator */
	 GROUP BY d1.year, s.area_id, s.band_id,
	          d1.age_sex_group
) /* End of denominator */
SELECT d.year,
       'S' AS study_or_comparison,
       416 AS study_id,
       d.area_id,
       d.band_id,
       TRUNC(d.age_sex_group/100) AS sex,
       MOD(d.age_sex_group, 100) AS age_group,
       COALESCE(n1.inv_414_test_1002, 0) AS inv_414_test_1002, 
       d.total_pop
  FROM d			/* Denominator - population health file */
	LEFT OUTER JOIN n1 ON ( 	/* NUM_SAHSULAND_CANCER - cancer numerator */
		    d.area_id		 = n1.area_id
		AND d.year		 = n1.year
		AND d.age_sex_group	 = n1.n_age_sex_group
		)
 ORDER BY 1, 2, 3, 4, 5, 6, 7;
+00038.04s  [DEBUG1] rif40_execute_insert_statement(): [56602] Study ID 416, statement: 15
Description: Study extract insert 1995 (EXPLAIN)
 query plan:
Insert on rif_studies.s416_extract  (cost=19943.90..21263.53 rows=52785 width=588)
  ->  Subquery Scan on "*SELECT*"  (cost=19943.90..21263.53 rows=52785 width=588)
        Output: "*SELECT*".year, "*SELECT*".study_or_comparison, "*SELECT*".study_id, "*SELECT*".area_id, "*SELECT*".band_id, "*SELECT*".sex, "*SELECT*".age_group, "*SELECT*".inv_414_test_1002, "*SELECT*".total_pop
        ->  Sort  (cost=19943.90..20075.86 rows=52785 width=544)
              Output: d.year, ('S'::text), (416), d.area_id, d.band_id, (trunc(((d.age_sex_group / 100))::double precision)), (mod(d.age_sex_group, 100)), (COALESCE(n1.inv_414_test_1002, '0'::bigint)), d.total_pop
              Sort Key: d.year, d.area_id, d.band_id, (trunc(((d.age_sex_group / 100))::double precision)), (mod(d.age_sex_group, 100))
              CTE n1
                ->  HashAggregate  (cost=2789.39..2818.90 rows=2951 width=36)
                      Output: s.area_id, c.year, c.age_sex_group, sum(CASE WHEN ((((c.icd)::text ~~ 'C33%'::text) OR ((c.icd)::text ~~ 'C340%'::text) OR ((c.icd)::text ~~ 'C341%'::text) OR ((c.icd)::text ~~ 'C342%'::text) OR ((c.icd)::text ~~ 'C343%'::text) OR ((c.icd)::text ~~ 'C348%'::text) OR ((c.icd)::text ~~ 'C349%'::text)) AND (c.year >= 1995) AND (c.year <= 1996)) THEN c.total ELSE 0 END), s.band_id
                      Group Key: c.year, s.area_id, s.band_id, c.age_sex_group
                      ->  Hash Join  (cost=2094.27..2686.10 rows=2951 width=36)
                            Output: s.area_id, s.band_id, c.year, c.age_sex_group, c.icd, c.total
                            Hash Cond: ((c.sahsu_grd_level4)::text = (s.area_id)::text)
                            ->  Index Scan using num_sahsuland_cancer_year on rif_data.num_sahsuland_cancer c  (cost=0.42..548.20 rows=2909 width=32)
                                  Output: c.year, c.age_sex_group, c.sahsu_grd_level1, c.sahsu_grd_level2, c.sahsu_grd_level3, c.sahsu_grd_level4, c.icd, c.total
                                  Index Cond: (c.year = 1995)
                                  Filter: (((c.icd)::text ~~ 'C33%'::text) OR ((c.icd)::text ~~ 'C340%'::text) OR ((c.icd)::text ~~ 'C341%'::text) OR ((c.icd)::text ~~ 'C342%'::text) OR ((c.icd)::text ~~ 'C343%'::text) OR ((c.icd)::text ~~ 'C348%'::text) OR ((c.icd)::text ~~ 'C349%'::text))
                            ->  Hash  (cost=2078.15..2078.15 rows=1256 width=20)
                                  Output: s.area_id, s.band_id
                                  ->  Subquery Scan on s  (cost=2062.45..2078.15 rows=1256 width=20)
                                        Output: s.area_id, s.band_id
                                        ->  Sort  (cost=2062.45..2065.59 rows=1256 width=26)
                                              Output: c_1.username, (NULL::integer), c_1.area_id, c_1.band_id
                                              Sort Key: c_1.username
                                              InitPlan 1 (returns $0)
                                                ->  Seq Scan on pg_catalog.pg_authid a  (cost=0.00..6.23 rows=1 width=64)
                                                      Output: upper(((a.rolname)::information_schema.sql_identifier)::text)
                                                      Filter: (pg_has_role(a.oid, 'USAGE'::text) AND (upper(((a.rolname)::information_schema.sql_identifier)::text) = 'RIF_MANAGER'::text))
                                              ->  Nested Loop Left Join  (cost=0.70..1991.57 rows=1256 width=26)
                                                    Output: c_1.username, NULL::integer, c_1.area_id, c_1.band_id
                                                    Join Filter: (c_1.study_id = s_1.study_id)
                                                    Filter: (((c_1.username)::name = "current_user"()) OR ('RIF_MANAGER'::text = $0) OR ((s_1.grantee_username IS NOT NULL) AND ((s_1.grantee_username)::text <> ''::text)))
                                                    ->  Index Scan using t_rif40_study_areas_pk on rif40.t_rif40_study_areas c_1  (cost=0.42..1948.73 rows=1256 width=30)
                                                          Output: c_1.username, c_1.study_id, c_1.area_id, c_1.band_id
                                                          Index Cond: (c_1.study_id = 416)
                                                    ->  Materialize  (cost=0.27..8.30 rows=1 width=10)
                                                          Output: s_1.study_id, s_1.grantee_username
                                                          ->  Index Only Scan using rif40_study_shares_pk on rif40.rif40_study_shares s_1  (cost=0.27..8.30 rows=1 width=10)
                                                                Output: s_1.study_id, s_1.grantee_username
                                                                Index Cond: (s_1.study_id = 416)
                                                                Filter: ((s_1.grantee_username)::name = "current_user"())
              CTE d
                ->  HashAggregate  (cost=5946.13..6473.98 rows=52785 width=32)
                      Output: d1.year, s_2.area_id, s_2.band_id, d1.age_sex_group, sum(COALESCE(d1.total, 0))
                      Group Key: d1.year, s_2.area_id, s_2.band_id, d1.age_sex_group
                      ->  Hash Join  (cost=2097.41..5273.88 rows=53780 width=32)
                            Output: s_2.area_id, s_2.band_id, d1.year, d1.age_sex_group, d1.total
                            Hash Cond: ((d1.sahsu_grd_level4)::text = (s_2.area_id)::text)
                            ->  Index Scan using pop_sahsuland_pop_year on rif_data.pop_sahsuland_pop d1  (cost=0.43..2375.17 rows=52785 width=28)
                                  Output: d1.year, d1.age_sex_group, d1.sahsu_grd_level1, d1.sahsu_grd_level2, d1.sahsu_grd_level3, d1.sahsu_grd_level4, d1.total
                                  Index Cond: (d1.year = 1995)
                            ->  Hash  (cost=2081.29..2081.29 rows=1256 width=20)
                                  Output: s_2.area_id, s_2.band_id
                                  ->  Subquery Scan on s_2  (cost=2065.59..2081.29 rows=1256 width=20)
                                        Output: s_2.area_id, s_2.band_id
                                        ->  Sort  (cost=2065.59..2068.73 rows=1256 width=26)
                                              Output: c_2.username, (NULL::integer), c_2.area_id, c_2.band_id
                                              Sort Key: c_2.username
                                              InitPlan 3 (returns $2)
                                                ->  Seq Scan on pg_catalog.pg_authid a_1  (cost=0.00..6.23 rows=1 width=64)
                                                      Output: upper(((a_1.rolname)::information_schema.sql_identifier)::text)
                                                      Filter: (pg_has_role(a_1.oid, 'USAGE'::text) AND (upper(((a_1.rolname)::information_schema.sql_identifier)::text) = 'RIF_MANAGER'::text))
                                              ->  Nested Loop Left Join  (cost=0.70..1994.71 rows=1256 width=26)
                                                    Output: c_2.username, NULL::integer, c_2.area_id, c_2.band_id
                                                    Join Filter: (c_2.study_id = s_3.study_id)
                                                    Filter: (((c_2.username)::name = "current_user"()) OR ('RIF_MANAGER'::text = $2) OR ((s_3.grantee_username IS NOT NULL) AND ((s_3.grantee_username)::text <> ''::text)))
                                                    ->  Index Scan using t_rif40_study_areas_pk on rif40.t_rif40_study_areas c_2  (cost=0.42..1951.87 rows=1256 width=30)
                                                          Output: c_2.username, c_2.study_id, c_2.area_id, c_2.band_id
                                                          Index Cond: ((c_2.study_id = 416) AND (c_2.area_id IS NOT NULL))
                                                    ->  Materialize  (cost=0.27..8.30 rows=1 width=10)
                                                          Output: s_3.study_id, s_3.grantee_username
                                                          ->  Index Only Scan using rif40_study_shares_pk on rif40.rif40_study_shares s_3  (cost=0.27..8.30 rows=1 width=10)
                                                                Output: s_3.study_id, s_3.grantee_username
                                                                Index Cond: (s_3.study_id = 416)
                                                                Filter: ((s_3.grantee_username)::name = "current_user"())
              ->  Merge Left Join  (cost=5425.21..6510.61 rows=52785 width=544)
                    Output: d.year, 'S'::text, 416, d.area_id, d.band_id, trunc(((d.age_sex_group / 100))::double precision), mod(d.age_sex_group, 100), COALESCE(n1.inv_414_test_1002, '0'::bigint), d.total_pop
                    Merge Cond: (((d.area_id)::text = (n1.area_id)::text) AND (d.year = n1.year) AND (d.age_sex_group = n1.n_age_sex_group))
                    ->  Sort  (cost=5196.11..5328.08 rows=52785 width=536)
                          Output: d.year, d.area_id, d.band_id, d.age_sex_group, d.total_pop
                          Sort Key: d.area_id, d.year, d.age_sex_group
                          ->  CTE Scan on d  (cost=0.00..1055.70 rows=52785 width=536)
                                Output: d.year, d.area_id, d.band_id, d.age_sex_group, d.total_pop
                    ->  Sort  (cost=229.10..236.48 rows=2951 width=532)
                          Output: n1.inv_414_test_1002, n1.area_id, n1.year, n1.n_age_sex_group
                          Sort Key: n1.area_id, n1.year, n1.n_age_sex_group
                          ->  CTE Scan on n1  (cost=0.00..59.02 rows=2951 width=532)
                                Output: n1.inv_414_test_1002, n1.area_id, n1.year, n1.n_age_sex_group
+00038.28s  rif40_execute_insert_statement(): [56605] Study 416: Study extract insert 1995 (EXPLAIN) OK, took: 00:00:00.061771
+00039.82s  rif40_execute_insert_statement(): [56605] Study 416: Study extract insert 1996 OK, inserted 54120 rows, took: 00:00:01.067706
+00039.82s  rif40_execute_insert_statement(): [56605] Study 416: Comparison area insert OK, inserted 1 rows, took: 00:00:00.001336
+00040.18s  [DEBUG1] rif40_execute_insert_statement(): [56005] SQL> EXPLAIN (VERBOSE, FORMAT text)
INSERT INTO s416_extract (
	year,study_or_comparison,study_id,area_id,band_id,sex,age_group,test_1002,total_pop) /* 1 numerator(s) */
WITH n1 AS (	/* NUM_SAHSULAND_CANCER - cancer numerator */
	SELECT s.area_id		/* Study or comparision resolution */,
	       c.year,
	       c.age_sex_group AS n_age_sex_group,
	       SUM(CASE 		/* Numerators - can overlap */
			WHEN ((	/* Investigation 1 ICD filters */
				    icd LIKE 'C33%' /* Value filter */ /* Filter 1 */
				 OR icd LIKE 'C340%' /* Value filter */ /* Filter 2 */
				 OR icd LIKE 'C341%' /* Value filter */ /* Filter 3 */
				 OR icd LIKE 'C342%' /* Value filter */ /* Filter 4 */
				 OR icd LIKE 'C343%' /* Value filter */ /* Filter 5 */
				 OR icd LIKE 'C348%' /* Value filter */ /* Filter 6 */
				 OR icd LIKE 'C349%' /* Value filter */ /* Filter 7 */) /* 7 lines of conditions: study: 416, inv: 414 */
			AND (1=1
			   AND  c.year BETWEEN 1995 AND 1996/* Investigation 1 year filter */
				        /* No genders filter required for investigation 1 */
				        /* No age group filter required for investigation 1 */)
			) THEN total
			ELSE 0
	       END) inv_414_test_1002	/* Investigation 1 -  */ 
	  FROM rif40_comparison_areas s,	/* Numerator study or comparison area to be extracted */
	       num_sahsuland_cancer c	/* cancer numerator */
	 WHERE c.sahsu_grd_level1 = s.area_id 	/* Comparison selection */
	   AND (
				    icd LIKE 'C33%' /* Value filter */ /* Filter 1 */
				 OR icd LIKE 'C340%' /* Value filter */ /* Filter 2 */
				 OR icd LIKE 'C341%' /* Value filter */ /* Filter 3 */
				 OR icd LIKE 'C342%' /* Value filter */ /* Filter 4 */
				 OR icd LIKE 'C343%' /* Value filter */ /* Filter 5 */
				 OR icd LIKE 'C348%' /* Value filter */ /* Filter 6 */
				 OR icd LIKE 'C349%' /* Value filter */ /* Filter 7 */)
				        /* No genders filter required for numerator (only one gender used) */
	       /* No age group filter required for numerator */
	   AND s.study_id = 416		/* Current study ID */
	   AND c.year = 1995		/* Numerator (INSERT) year filter */
	 GROUP BY c.year, s.area_id,
	          c.age_sex_group
) /* NUM_SAHSULAND_CANCER - cancer numerator */
, d AS (
	SELECT d1.year, s.area_id, NULL::INTEGER AS band_id, d1.age_sex_group,
	       SUM(COALESCE(d1.total, 0)) AS total_pop
	  FROM rif40_comparison_areas s, pop_sahsuland_pop d1 	/* Denominator study or comparison area to be extracted */
	 WHERE d1.year = 1995		/* Denominator (INSERT) year filter */
	   AND s.area_id  = d1.sahsu_grd_level1	/* Comparison geolevel join */
	   AND s.area_id  IS NOT NULL	/* Exclude NULL geolevel */
	   AND s.study_id = 416		/* Current study ID */
	       /* No age group filter required for denominator */
	 GROUP BY d1.year, s.area_id,
	          d1.age_sex_group
) /* End of denominator */
SELECT d.year,
       'C' AS study_or_comparison,
       416 AS study_id,
       d.area_id,
       d.band_id,
       TRUNC(d.age_sex_group/100) AS sex,
       MOD(d.age_sex_group, 100) AS age_group,
       COALESCE(n1.inv_414_test_1002, 0) AS inv_414_test_1002, 
       d.total_pop
  FROM d			/* Denominator - population health file */
	LEFT OUTER JOIN n1 ON ( 	/* NUM_SAHSULAND_CANCER - cancer numerator */
		    d.area_id		 = n1.area_id
		AND d.year		 = n1.year
		AND d.age_sex_group	 = n1.n_age_sex_group
		)
 ORDER BY 1, 2, 3, 4, 5, 6, 7;
+00040.40s  [DEBUG1] rif40_execute_insert_statement(): [56602] Study ID 416, statement: 18
Description: Comparison extract insert 1995 (EXPLAIN)
 query plan:
Insert on rif_studies.s416_extract  (cost=4290.84..4291.92 rows=43 width=588)
  ->  Subquery Scan on "*SELECT*"  (cost=4290.84..4291.92 rows=43 width=588)
        Output: "*SELECT*".year, "*SELECT*".study_or_comparison, "*SELECT*".study_id, "*SELECT*".area_id, "*SELECT*".band_id, "*SELECT*".sex, "*SELECT*".age_group, "*SELECT*".inv_414_test_1002, "*SELECT*".total_pop
        ->  Sort  (cost=4290.84..4290.95 rows=43 width=544)
              Output: d.year, ('C'::text), (416), d.area_id, d.band_id, (trunc(((d.age_sex_group / 100))::double precision)), (mod(d.age_sex_group, 100)), (COALESCE(n1.inv_414_test_1002, '0'::bigint)), d.total_pop
              Sort Key: d.year, d.area_id, d.band_id, (trunc(((d.age_sex_group / 100))::double precision)), (mod(d.age_sex_group, 100))
              CTE n1
                ->  HashAggregate  (cost=701.68..701.76 rows=8 width=19)
                      Output: c_1.area_id, c.year, c.age_sex_group, sum(CASE WHEN ((((c.icd)::text ~~ 'C33%'::text) OR ((c.icd)::text ~~ 'C340%'::text) OR ((c.icd)::text ~~ 'C341%'::text) OR ((c.icd)::text ~~ 'C342%'::text) OR ((c.icd)::text ~~ 'C343%'::text) OR ((c.icd)::text ~~ 'C348%'::text) OR ((c.icd)::text ~~ 'C349%'::text)) AND (c.year >= 1995) AND (c.year <= 1996)) THEN c.total ELSE 0 END)
                      Group Key: c.year, c_1.area_id, c.age_sex_group
                      ->  Nested Loop  (cost=22.98..607.14 rows=2909 width=19)
                            Output: c_1.area_id, c.year, c.age_sex_group, c.icd, c.total
                            Join Filter: ((c_1.area_id)::text = (c.sahsu_grd_level1)::text)
                            ->  Sort  (cost=22.56..22.56 rows=1 width=9)
                                  Output: c_1.username, (NULL::integer), c_1.area_id
                                  Sort Key: c_1.username
                                  InitPlan 1 (returns $0)
                                    ->  Seq Scan on pg_catalog.pg_authid a  (cost=0.00..6.23 rows=1 width=64)
                                          Output: upper(((a.rolname)::information_schema.sql_identifier)::text)
                                          Filter: (pg_has_role(a.oid, 'USAGE'::text) AND (upper(((a.rolname)::information_schema.sql_identifier)::text) = 'RIF_MANAGER'::text))
                                  ->  Nested Loop Left Join  (cost=0.27..16.32 rows=1 width=9)
                                        Output: c_1.username, NULL::integer, c_1.area_id
                                        Join Filter: (c_1.study_id = s.study_id)
                                        Filter: (((c_1.username)::name = "current_user"()) OR ('RIF_MANAGER'::text = $0) OR ((s.grantee_username IS NOT NULL) AND ((s.grantee_username)::text <> ''::text)))
                                        ->  Seq Scan on rif40.t_rif40_comparison_areas c_1  (cost=0.00..8.00 rows=1 width=13)
                                              Output: c_1.username, c_1.study_id, c_1.area_id
                                              Filter: (c_1.study_id = 416)
                                        ->  Index Only Scan using rif40_study_shares_pk on rif40.rif40_study_shares s  (cost=0.27..8.30 rows=1 width=10)
                                              Output: s.study_id, s.grantee_username
                                              Index Cond: (s.study_id = 416)
                                              Filter: ((s.grantee_username)::name = "current_user"())
                            ->  Index Scan using num_sahsuland_cancer_year on rif_data.num_sahsuland_cancer c  (cost=0.42..548.20 rows=2909 width=19)
                                  Output: c.year, c.age_sex_group, c.sahsu_grd_level1, c.sahsu_grd_level2, c.sahsu_grd_level3, c.sahsu_grd_level4, c.icd, c.total
                                  Index Cond: (c.year = 1995)
                                  Filter: (((c.icd)::text ~~ 'C33%'::text) OR ((c.icd)::text ~~ 'C340%'::text) OR ((c.icd)::text ~~ 'C341%'::text) OR ((c.icd)::text ~~ 'C342%'::text) OR ((c.icd)::text ~~ 'C343%'::text) OR ((c.icd)::text ~~ 'C348%'::text) OR ((c.icd)::text ~~ 'C349%'::text))
              CTE d
                ->  HashAggregate  (cost=3585.40..3585.83 rows=43 width=15)
                      Output: d1.year, c_2.area_id, NULL::integer, d1.age_sex_group, sum(COALESCE(d1.total, 0))
                      Group Key: d1.year, c_2.area_id, d1.age_sex_group
                      ->  Nested Loop  (cost=22.98..3057.55 rows=52785 width=15)
                            Output: c_2.area_id, d1.year, d1.age_sex_group, d1.total
                            Join Filter: ((c_2.area_id)::text = (d1.sahsu_grd_level1)::text)
                            ->  Sort  (cost=22.56..22.56 rows=1 width=9)
                                  Output: c_2.username, (NULL::integer), c_2.area_id
                                  Sort Key: c_2.username
                                  InitPlan 3 (returns $2)
                                    ->  Seq Scan on pg_catalog.pg_authid a_1  (cost=0.00..6.23 rows=1 width=64)
                                          Output: upper(((a_1.rolname)::information_schema.sql_identifier)::text)
                                          Filter: (pg_has_role(a_1.oid, 'USAGE'::text) AND (upper(((a_1.rolname)::information_schema.sql_identifier)::text) = 'RIF_MANAGER'::text))
                                  ->  Nested Loop Left Join  (cost=0.27..16.32 rows=1 width=9)
                                        Output: c_2.username, NULL::integer, c_2.area_id
                                        Join Filter: (c_2.study_id = s_1.study_id)
                                        Filter: (((c_2.username)::name = "current_user"()) OR ('RIF_MANAGER'::text = $2) OR ((s_1.grantee_username IS NOT NULL) AND ((s_1.grantee_username)::text <> ''::text)))
                                        ->  Seq Scan on rif40.t_rif40_comparison_areas c_2  (cost=0.00..8.00 rows=1 width=13)
                                              Output: c_2.username, c_2.study_id, c_2.area_id
                                              Filter: ((c_2.area_id IS NOT NULL) AND (c_2.study_id = 416))
                                        ->  Index Only Scan using rif40_study_shares_pk on rif40.rif40_study_shares s_1  (cost=0.27..8.30 rows=1 width=10)
                                              Output: s_1.study_id, s_1.grantee_username
                                              Index Cond: (s_1.study_id = 416)
                                              Filter: ((s_1.grantee_username)::name = "current_user"())
                            ->  Index Scan using pop_sahsuland_pop_year on rif_data.pop_sahsuland_pop d1  (cost=0.43..2375.17 rows=52785 width=15)
                                  Output: d1.year, d1.age_sex_group, d1.sahsu_grd_level1, d1.sahsu_grd_level2, d1.sahsu_grd_level3, d1.sahsu_grd_level4, d1.total
                                  Index Cond: (d1.year = 1995)
              ->  Hash Left Join  (cost=0.30..2.08 rows=43 width=544)
                    Output: d.year, 'C'::text, 416, d.area_id, d.band_id, trunc(((d.age_sex_group / 100))::double precision), mod(d.age_sex_group, 100), COALESCE(n1.inv_414_test_1002, '0'::bigint), d.total_pop
                    Hash Cond: (((d.area_id)::text = (n1.area_id)::text) AND (d.year = n1.year) AND (d.age_sex_group = n1.n_age_sex_group))
                    ->  CTE Scan on d  (cost=0.00..0.86 rows=43 width=536)
                          Output: d.year, d.area_id, d.band_id, d.age_sex_group, d.total_pop
                    ->  Hash  (cost=0.16..0.16 rows=8 width=532)
                          Output: n1.inv_414_test_1002, n1.area_id, n1.year, n1.n_age_sex_group
                          ->  CTE Scan on n1  (cost=0.00..0.16 rows=8 width=532)
                                Output: n1.inv_414_test_1002, n1.area_id, n1.year, n1.n_age_sex_group
+00040.83s  rif40_execute_insert_statement(): [56605] Study 416: Comparison extract insert 1995 (EXPLAIN) OK, took: 00:00:00.004237
```

## 7.2 SQL Server

SQL Server automatically allocates memory as needed by the server up to the limit of 2,147,483,647MB! In practice you may wish to reduce this figure to 40% of the available RAM.

By default SQL Server is not using *largepages* (the names for *huge_pages* in SQL Server):

```SQL
1> SELECT large_page_allocations_kb FROM sys.dm_os_process_memory;
2> go
large_page_allocations_kb
-------------------------
                        0
```

To enable *largepages* you need to [Enable the Lock Pages in Memory Option](https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/enable-the-lock-pages-in-memory-option-windows?view=sql-server-2017).
This will have consequences for the automated tuning which unless limited in size will remove the ability of Windows to free up SQL Server memory for other applications (and Windows itself). You need to be on a 
big server and make sure your memory set-up is stable before enabling it.  
				
The [SQL Server profiler](https://docs.microsoft.com/en-us/sql/tools/sql-server-profiler/sql-server-profiler?view=sql-server-2017) needs to be used to trace RIF application tuning.
				
Peter Hambly
May 2018