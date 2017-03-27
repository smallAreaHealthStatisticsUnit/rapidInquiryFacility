SQL Server Database Installation
================================

# Contents
- [1. Install SQL Server 2012 SP2](#1-install-sql-server-2012-sp2)
- [2. Create Databases and Users](#2-create-databases-and-users)
   - [2.1 Network connection errors](#21-network-connection-errors)
   - [2.2 Logon errors](#22-logon-errors)
     - [2.2.1 Wrong server authentication mode](#221-wrong-server-authentication-mode)
- [3. Create Additional Users](#3-create-additional-users)
- [4. Installing the RIF Schema](#4-installing-the-rif-schema)
  - [4.1 BULK INSERT Permission](#41-bulk-insert-permission)
  - [4.2 Re-running scripts](#42-re-running-scripts)
    - [4.2.1 Geospatial script: rif40_sahsuland_tiles.bat](#421-geospatial-script-rif40_sahsuland_tilesbat)
    - [4.2.2 Re-load sahsuland example data](#422-re-load-sahsuland-example-data)
  - [4.3 SQL Server BULK INSERT Issues](#43-sql-server-bulk-insert-issues)
    - [4.3.1 Line Termination](#431-line-termination)
- [5. Script Notes](#5-script-notes)
  - [5.1 Script and documentation TODO](#51-script-and-documentation-todo)	
	
# 1. Install SQL Server 2012 SP2

Install SQL Server 2012 SP2  (Express for a test system/full version for production): https://www.microsoft.com/en-gb/download/details.aspx?id=43351# 

**DO NOT INSTALL SQL Server 2008 or before**

Check the version of your database:
```
sqlcmd -E
1> SELECT @@version AS version, compatibility_level FROM sys.databases Where DB_NAME() = name ;
2> go
version                                                                                               compatibility_level
----------------------------------------------------------------------------------------------------- -------------------
Microsoft SQL Server 2012 (SP2-GDR) (KB3194719) - 11.0.5388.0 (X64)
        Sep 23 2016 16:56:29
        Copyright (c) Microsoft Corporation
        Express Edition (64-bit) on Windows NT 6.1 <X64> (Build 7601: Service Pack 1)                 110
```
* The compatibility level should be *110* and the version *Microsoft SQL Server 2012 (SP2...*. If it is not then you have more 
  than one SQL Server database on your machine, see setting *SQLCMDSERVER* in the next section.
* If you install SQL Server 2014+; make sure SQL Server Management Studio has been installed; [see this blog for further instructions](https://www.hanselman.com/blog/DownloadSQLServerExpress.aspx)

# 2. Create Databases and Users

Run the following command as Administrator in this directory (...rapidInquiryFacility\rifDatabase\SQLserver\installation):

```
sqlcmd -E -b -m-1 -e -i rif40_database_creation.sql
```
Note:
- **_This script will destroy all existing users and data_**;
- This script will:
  * Drops and creates the *sahsuland*, *sahsuland_dev* and *test* databasea and the *rif40*, *rifuser* and *rifmanager* user (logon) roles;   
  * Creates *rif_user*, *rif_manager*, *rif_no_suppression*, *rif_student* and *notarifuser* non logon (i.e. group) roles;
  * The role *rif_user* allows users to create tables and views;
  * The role *rif_manager* allows users to additionally create procedures and functions;
  * The rif40 user can do BULK INSERT;
  * The default database is *sahsuland_dev*;* The *rif40* users password is `rif40`. Chnage it after install.
- The application is installed in the *rif40* schema and data is installed in the *rif_data* schema; both owned by the *rif40* role;
- Please edit the script to set *rif40*/*rifuser*/*rifmanager* passwords as they are set to their **_usernames_**, especially if 
  your SQL Server database is networked! 
- The test database is for building geosptial data. SQL Server express databases are limited to 10G in size; so to maximise the size of data that can be processed
  a separate database is used.

## 2.1 Network connection errors

This is when the above command will not run.

```	
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>sqlcmd -E -b -m-1 -e -r1 -i rif40_database_creation.sql
Result 0x2, Level 16, State 1
Named Pipes Provider: Could not open a connection to SQL Server [2].
Mirosoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not found or not accessible. Check if instance name is correct and if SQL Server is configured to allow remote connections. For more information see SQL Server Books Online.
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.
```
  * You may need to specify the instance name: e.g. `-S PETER-PC\SAHSU`, e.g.
	```
	sqlcmd -E -S PETER-PC\SAHSU -b -m-1 -e -r1 -i rif40_database_creation.sql
	```  
    If you set this it will ned to be set in the environment as *SQLCMDSERVER*. This is usually caused by 
    multiple installations of SQL server on the machine in the past, i.e. the *DefaultLocalInstance* registry key is wrong.
  * Check if remote access is enabled (it should be) using SQL Server Management Studio as adminstrator: https://msdn.microsoft.com/en-gb/library/ms191464(v=sql.120).aspx
  * Check TCP access is enabled using SQL Server Configuration Manager as adminstrator: https://msdn.microsoft.com/en-us/library/ms189083.aspx
  * Check your firewall permits access to TCP port 1433. **Be careful _not_ to allow Internet access unless you intend it.**
  * The following is more helpful than the official Microsoft manuals: https://blogs.msdn.microsoft.com/walzenbach/2010/04/14/how-to-enable-remote-connections-in-sql-server-2008/

Now test your can connect to the database.

## 2.2 Logon errors

Test for logon errors as using the command: `sqlcmd -U rif40 -P rif40 -d sahsuland_dev`

Test all combinations of *rif40*/*rifuser*/*rifmanager* logon roles and *sahsuland*/*sahsuland_dev* databases.

### 2.2.1 Wrong server authentication mode

The server will need to be changed from Windows Authentication mode to SQL Server and Windows Authentication mode. Then restart SQL Server. 
See: https://msdn.microsoft.com/en-GB/library/ms188670.aspx

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U rif40 -P rif40 -d sahsuland_dev
Msg 18456, Level 14, State 1, Server PETER-PC\SQLEXPRESS, Line 1
Login failed for user 'rif40'.
```

  In the Window "applications" event log:
```
Login failed for user 'rif40'. Reason: An attempt to login using SQL authentication failed. Server is configured for Windows authentication only. [CLIENT: <local machine>]
```

  The node also show how to enable the sa (system adminstrator) account. As with all relational database adminstration accounts as strong (12+ chacracter) password is recommended to defeat 
  attacks by dictionary or all possible passwords.

  This is what a successful login looks like: `sqlcmd -U rif40 -P rif40`

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U rif40 -P rif40
1> SELECT db_name();
2> GO

--------------------------------------------------------------------------------------------------------------------------------
sahsuland_dev

(1 rows affected)
1>
``` 
# 3. Create Additional Users

Run the optional script *rif40_test_user.sql*. This creates a default user *%newuser%* from the command environment. This is set from the command line using 
the -v newuser=<my new user> parameter. Run as Administrator:

```
sqlcmd -E -b -m-1 -e -i rif40_test_user.sql -v newuser=peter
```

* User is created with the *rif_user* (can create tables and views) and *rif_manager* roles (can also create procedures and functions), can do `BULK INSERT`;
* User can use sahsuland, sahsuland_dev and test databases;
* The test database is for geospatial processing and does not have the *rif_user* and *rif_manager* roles, the user can create tables, views,
* procedures and function and do `BULK INSERT`s
* Will fail to re-create a user if the user already has objects (tables, views etc)
* This user's password will be the username, so change it on a networked system.

Test connection and object privilges:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U peter -P peter -d test
1> SELECT db_name() AS db_name INTO test_table;
2> SELECT * FROM test_table;
3> go

(1 rows affected)
db_name
--------------------------------------------------------------------------------------------------------------------------------
test

(1 rows affected)
1> quit

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>
```
 
# 4. Installing the RIF Schema

Run the following scripts ad Administrator:

* rif40_sahsuland_dev_install.bat (see note 4.1 below before you run this script)
* rif40_sahsuland_install.bat (see note 4.1 below before you run this script)

An additional script is proved to build exverything and create an example study. This should be edited to set the test user variable, NEWUSER. Note that this user's password will be the 
username, so change it on a networked system:

* rebuild_all.bat (see note 4.1 below before you run this script)
  rebuild_all.bat can be abort using *control-C*; no other key will abort the script. You will be asked if you want to abort; 'Y' or 'y' will abort; any other keys will continue
	```
	C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>rebuild_all.bat

	C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>ECHO OFF
	Administrator PRIVILEGES Detected!
	##########################################################################################
	#
	# WARNING! this script will the drop and create the sahsuland and sahusland_dev databases.
	# Type control-C to abort.
	#
	# Test user: "peter"
	#
	##########################################################################################
	Press any key to continue . . .
	Terminate batch job (Y/N)? y
```

**These scripts do NOT drop existing tables, the database must be rebuilt from scratch**.
**You _must_ build sahusland_dev before sahusland**; as it loads the error messages.

The indivuidual scripts can be run by batch files for sahsuland_dev only, but they must be run in this order:

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

An additional script in the installation directory can be used to create an example study, for example the *<my new user>* user created by the script *rif40_test_user.sql*:
```
sqlcmd -U <my new user> -P <my new user> -d sahsuland_dev -b -m-1 -e -r1 -i rif40_run_study.sql
```

## 4.1 BULK INSERT Permission

SQL Server needs access granted to `BULK INSERT` files, they are not coipied from the client to the server.

SQL Server needs access to the relative directories: *..\..\GeospatialData\tileMaker* and *..\..\\DataLoaderData\SAHSULAND*. The simplest
way is to allow read/execute access to the local users group (e.g. PH-LAPTOP\Users).

*DO NOT TRY TO RUN BULK INSERT FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).* Use a local directory which SQL Server has
access to. Note that SQL Server *BULK LOAD* behaves dirrently if you logon using Windows authentication (where it will use your credentials 
to access the files) to using a username and password (where it will use the Server's credentials to acces the file).

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

## 4.2 Re-running scripts

### 4.2.1 Geospatial script: rif40_sahsuland_tiles.bat

Re-runniong the geospatial script: rif40_sahsuland_tiles.bat will produce the following error:

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
### 4.2.2 Re-load sahsuland example data

Re-load sahsuland example data with: rif40_sahsuland_data.bat

## 4.3 SQL Server BULK INSERT Issues

### 4.3.1 Line Termination

```
BULK INSERT rif_data.pop_sahsuland_pop FROM 'C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\DataLoaderData\SAHSULAND/pop_sahsuland_pop.csv'
WITH
(
   FORMATFILE = 'C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\DataLoaderData\SAHSULAND/pop_sahsuland_pop.fmt',
   TABLOCK,
   FIRSTROW=2
);

Msg 245, Level 16, State 1, Server PETER-PC\SAHSU, Line 3
' to data type int.hen converting the varchar value '0
```
* This is caused by line termination. SQL Server is expecting a Unix format file (i.e. with "\n" as a line terminator). The file is almost certainly in DOS 
  format (with \r\n as a line terminator). Convert the file to Unix format using Notetab++, Cygwin/MingW dos2unix or perl:
```
perl -i -p -e "s/\r//" <oldfilename >newfilename
```
* The Githib repository has been fixed using a .gitattributes file:
```
# Declare files that will always have Unix LF line endings on checkout.
*.csv text eol=lf
*.fmt text eol=lf
```

# 5. Script Notes

* All scripts except database creation are now transactional, with a script of the same name usually in the source code directory; 
  The T-SQL functions sp_addsrvrolemember() and sp_addrolemember() are not tranactional;
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

## 5.1 Script and documentation TODO
	
Still to do:

* Search path notes
* On logon trigger - Postgres *rif40_startup()* function
* Run study procedure *rif40_run_study()*
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

Peter Hambly
2nd March 2017
