To install database, run in this order (I think):

1. Install SQL Server 2012 SP2 (Express for a test system/full version for production): https://www.microsoft.com/en-gb/download/details.aspx?id=43351# 

2. Create databases and users

Drop and create sahsuland, sahsuland_dev and test databasea and the rif40, rifuser and rifmanager users. Create rif_user, rif_manager etc roles.

Please edit to set rif40/rifuser/rifmanager passwords as they are set to their usernames, especially if your SQL Server database is networked!. 

Run the following command as Administrator in this directory (...rapidInquiryFacility\rifDatabase\SQLserver\installation):

```
sqlcmd -E -b -m-1 -e -i rif40_database_creation.sql
```

The test database is for building geosptial data. SQL Server express databases are limited to 10G in size; so to maximise the size of data that can be processed
a separate database is used.

2.1 Network connection errors	

This is when the above commd will not run.

```	
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>sqlcmd -E -b -m-1 -e -r1 -i rif40_database_creation.sql
Result 0x2, Level 16, State 1
Named Pipes Provider: Could not open a connection to SQL Server [2].
Mirosoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not found or not accessible. Check if instance name is correct and if SQL Server is configured to allow remote connections. For more information see SQL Server Books Online.
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.
```
* You may need to specify the instance name: e.g. -S Peter-PC\SQLEXPRESS. If you set this it will ned to be set in the environment as SQLCMDSERVER. This is usually caused by 
  multiple installations of SQL server on the machine in the past, i.e. the DefaultLocalInstance registry key is wrong.
* Check if remote access is enabled (it should be) using SQL Server Management Studio as adminstrator: https://msdn.microsoft.com/en-gb/library/ms191464(v=sql.120).aspx
* SQL Server Configuration Manager as adminstrator: https://msdn.microsoft.com/en-us/library/ms189083.aspx
* Check your firewall permits access to TCP port 1433. Be careful not to allow Internet access unless you intend it.
* The following is more helpful than the official Microsoft manuals: https://blogs.msdn.microsoft.com/walzenbach/2010/04/14/how-to-enable-remote-connections-in-sql-server-2008/

Now test your can connecti to the database.

2.2 Connection errors as rif40/rifuser/rifmanager etc

Command: sqlcmd -U rif40 -P rif40 -d sahsuland_dev

Test all combinations of rfi40/rifuser/rifmanager and sahsuland/sahsuland_dev

* Wrong server authentication mode

The server will need to be changed from Windows Authentication mode to SQL Server and Windows Authentication mode. Then restart SQL Server. 
See: https://msdn.microsoft.com/en-GB/library/ms188670.aspx

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U rif40 -P rif40 -d sahsuland_dev
Msg 18456, Level 14, State 1, Server PETER-PC\SQLEXPRESS, Line 1
Login failed for user 'rif40'.
```

```
Login failed for user 'rif40'. Reason: An attempt to login using SQL authentication failed. Server is configured for Windows authentication only. [CLIENT: <local machine>]
```

The node also show how to enable the sa (system adminstrator) account. As with all relational database adminstration accounts as strong (12+ chacracter) password is recommended to defeat 
attacks by dictionary or all possible passwords.

This is what a successful lopgin looks like: sqlcmd -U rif40 -P rif40

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U rif40 -P rif40
1> SELECT db_name();
2> GO

--------------------------------------------------------------------------------------------------------------------------------
sahsuland_dev

(1 rows affected)
1>
```

2.3 Adding you own user. Do not use windows native authentication or you will not be able to use the RIF!

```
USE [master];

CREATE LOGIN [Peter] WITH PASSWORD='Peter';
CREATE USER [Peter] FOR LOGIN [Peter] WITH DEFAULT_SCHEMA=[dbo];
CREATE SCHEMA [Peter] AUTHORIZATION [Peter];
ALTER USER [Peter] WITH DEFAULT_SCHEMA=[Peter];
GO

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


