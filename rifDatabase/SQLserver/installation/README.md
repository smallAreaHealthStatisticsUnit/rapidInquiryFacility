To install database, run in this order (I think):

0. Install SQL Server 2012 SP2 (Express for a test system/full version for production): https://www.microsoft.com/en-gb/download/details.aspx?id=43351# 

1. Create database and rif40 user (pleased set rif40 password); run as Administrator:

	```
	sqlcmd -E -b -m-1 -e -i rif40_database_creation.sql
	/*
	Creation of sahsuland_dev database
	*/

	IF EXISTS(SELECT * FROM sys.sysdatabases where name='sahsuland_dev')
			DROP DATABASE sahsuland_dev;


	CREATE DATABASE sahsuland_dev;


	USE [sahsuland_dev];

	CREATE LOGIN rif40 WITH PASSWORD='rif40';
	CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo];

	Msg 5701, Level 0, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 2
	Changed database context to 'sahsuland_dev'.
	CREATE SCHEMA [rif40] AUTHORIZATION [rif40];

	CREATE SCHEMA [rif_data] AUTHORIZATION [rif40];

	ALTER USER [rif40] WITH DEFAULT_SCHEMA=[rif40];
	```

2. Create database roles; run as Administrator:

	```
	sqlcmd -E -b -m-1 -e -i rif40_users_roles.sql 
	/*
	Required roles:
	rif_manager
	rif_user

	Optional:
	rif_student
	rif_no_suppression (?)

	Testing:
	notarifuser
	*/
	USE [sahsuland_dev];

	IF DATABASE_PRINCIPAL_ID('rif_manager') IS NULL
			CREATE ROLE [rif_manager];

	IF DATABASE_PRINCIPAL_ID('rif_user') IS NULL
			CREATE ROLE [rif_user];

	IF DATABASE_PRINCIPAL_ID('rif_student') IS NULL
			CREATE ROLE [rif_student];

	IF DATABASE_PRINCIPAL_ID('rif_no_suppression') IS NULL
			CREATE ROLE [rif_no_suppression];

	IF DATABASE_PRINCIPAL_ID('notarifuser') IS NULL
			CREATE ROLE [notarifuser];


	Msg 5701, Level 0, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 13
	Changed database context to 'sahsuland_dev'.

	```

3. Run optional rif40_test_user.sql. This creates a default user %USERNAME% from the environment; run as Administrator:

	```
	sqlcmd -E -b -m-1 -e -i rif40_test_user.sql
	/*
	Need some test people - $(USERNAME) is the user environment username (admin in my case)

	THIS SCRIPT MUST BE RUN AS ADMINSITRATOR
	*/

	BEGIN
	IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'admin')
		BEGIN
					CREATE LOGIN admin WITH PASSWORD='admin';
					CREATE USER [admin] FOR LOGIN [admin] WITH DEFAULT_SCHEMA=[dbo];
					ALTER SERVER ROLE [rif_manager] ADD MEMBER [admin];
					ALTER SERVER ROLE [rif_user] ADD MEMBER [admin];
			END;

	IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'admin')
			BEGIN
					EXEC('CREATE SCHEMA [admin] AUTHORIZATION [admin]');
					ALTER USER [admin] WITH DEFAULT_SCHEMA=[admin];
			END;

	SELECT name, type_desc FROM sys.database_principals WHERE name = N'admin';
	SELECT * FROM sys.schemas WHERE name = N'admin';

	END;

	name                                                                                                                             type_desc

	-------------------------------------------------------------------------------------------------------------------------------- ---------------------------------------------------
	---------
	admin                                                                                                                            SQL_USER


	(1 rows affected)
	name                                                                                                                             schema_id   principal_id
	-------------------------------------------------------------------------------------------------------------------------------- ----------- ------------
	admin                                                                                                                                      7            7

	(1 rows affected)
	```
   In the case where the user already exists they need to be granted access to the sahsuland_dev database or the ```ALTER 
   SERVER ROLE [rif_manager] ADD MEMBER [Peter];``` command will fail. Use SQL Server Management Studio to do this until 
   I work out the obscure SQL command required (see: create_peter.sql)
   
4. Run the following scripts in order:

* rif40_install_sequences.bat
* rif40_install_tables.bat
* rif40_install_log_error_handling.bat
* rif40_install_functions.bat
* rif40_install_views.bat
* rif40_install_table_triggers.bat
* rif40_install_view_triggers.bat
* rif40_data_install_tables.bat
* rif40_sahsuland_tiles.bat (see note 4.1 below)
* rif40_sahsuland_data.bat (see note 4.1 below)

Or run:

* rif40_sahsuland_dev_install.bat (see note 4.1 below)

Notes
#####

4.1 SQL Server access to BULK INSERT files

SQL Server needs access to the relative directories: ..\..\GeospatialData\tileMaker and ..\..\\DataLoaderData\SAHSULAND. The simplest
way is to allow read/execute access to the local users group (e.g. PH-LAPTOP\Users).

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

4.2 Re-running rif40_sahsuland_tiles.bat

This will produce the following error:

```
-- SQL statement 75: Remove old geolevels meta data table >>>
DELETE FROM t_rif40_geolevels WHERE geography = 'SAHSULAND';

Msg 547, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
The DELETE statement conflicted with the REFERENCE constraint "rif40_covariates_geolevel_fk". The conflict occurred in database "sah
suland_dev", table "rif40.rif40_covariates".
Msg 3621, Level 0, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
The statement has been terminated.
```

To resolve: delete the coariates. You must re-run rif40_sahsuland_data.bat afterwards.

```
DELETE FROM rif40.rif40_covariates WHERE geography = 'SAHSULAND';
```

5. Load geography with:

6. Load sahsuland example data with:

Notes:

* All scripts are now transactional, with a script of the same name usually in the source code directory;
* The function rif40_sequence_current_value() is created earlier by the sequences SQL script and 
  cannot be recreated once tables have been created;
* rif40_import_data.bat is now path independent. The SQL script (in this directory) will now delete all 
  setup data in the database!
* All the default column constraints have now been named so they can be dropped and recreated. On an earlier 
  database you will get errors like:

```
Msg 3729, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 6
Cannot DROP FUNCTION 'rif40.rif40_sequence_current_value' because it is being referenced by object 'DF__t_rif40_r__inv_i__12A9974E'.
```
  Fix by running manually:

  * rif40_drop_all_data.sql
  * ..\sahsuland_dev\rif40\tables\recreate_all_tables.sql

* The following tables are not present in SQL Server and not needed:
                                
	RIF40_DUAL                                           
	RIF40_ICD10                                          
	RIF40_ICD9                                           
	RIF40_POPULATION_EUROPE                              
	RIF40_POPULATION_US                                 
	RIF40_POPULATION_WORLD                               
	RIF40_TEST_HARNESS                                   
	RIF40_TEST_RUNS                                     
	T_RIF40_FDW_TABLES
  
* To be SQL Server temporary tables (i.e. ##g_rif40_comparison_areas) created by on-logon trigger:

	G_RIF40_COMPARISON_AREAS                            
	G_RIF40_STUDY_AREAS 
	
Still to do:

* Search path
* On logon trigger - Postgres rif40_startup() function
* Fix selected columns:

| type    | column_name                                                 | nullable | data_type        | Notes                           |       
|---------|-------------------------------------------------------------|----------|------------------|---------------------------------| 
| Extra   | RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISON_POPULATION          | NULL     | numeric          |                                 |   
| Extra   | RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMN_EXISTS               | NOT NULL | varchar          |                                 |   
| Extra   | RIF40_PARAMETERS.DESCRIPTION                                | NOT NULL | varchar          |                                 |   
| Extra   | T_RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISON_POPULATION        | NULL     | numeric          |                                 |  
| Extra   | T_RIF40_INVESTIGATIONS.ROWID                                | NOT NULL | uniqueidentifier |                                 |                                          
| Missing | RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMNN_EXISTS              | NULL     | bool             | Fix Postgres spelling           |                               | 
| Missing | RIF40_PARAMETERS.PARAM_DESCRIPTION                          | NULL     | VARCHAR2(250)    | Resolve with Postgres           |                               |    
| Missing | T_RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION       | NULL     | NUMBER(38,6)     | Fix Postgres spelling           |                                          | 

* Fix NULL/NOT NULL issues in postgres_diff_report.txt
* Check Postgres alter 1-8 for minor changes


