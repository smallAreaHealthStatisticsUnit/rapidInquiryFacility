SQL Server Development Database Installation
============================================

# Contents
- [1. Install SQL Server](#1-install-sql-server)
- [2. Create Databases and Users](#2-create-databases-and-users)
   - [2.1 Network connection errors](#21-network-connection-errors)
   - [2.2 Power User Issues](#22-power-user-issues)
- [3. Create Additional Users](#3-create-additional-users)
   - [3.1 Logon errors](#31-logon-errors)
     - [3.1.1 Wrong server authentication mode](#311-wrong-server-authentication-mode)
- [4. Installing the RIF Schema](#4-installing-the-rif-schema)
  - [4.1 BULK INSERT Permission](#41-bulk-insert-permission)
  - [4.2 Re-running scripts](#42-re-running-scripts)
    - [4.2.1 Geospatial script: rif40_sahsuland_tiles.bat](#421-geospatial-script-rif40_sahsuland_tilesbat)
    - [4.2.2 Re-load sahsuland example data](#422-re-load-sahsuland-example-data)
  - [4.3 SQL Server Backup and Restore](#43-sql-server-backup-and-restore)
  - [4.4 SQL Server BULK INSERT Issues](#44-sql-server-bulk-insert-issues)
    - [4.4.1 Line Termination](#441-line-termination)
- [5. Script Notes](#5-script-notes)
  - [5.1 Script and documentation TODO](#51-script-and-documentation-todo)	
	
# 1. Install SQL Server

Install SQL Server 2012 SP2 or 2016(Express for a test system/full version for production). **DO NOT INSTALL SQL Server 2008 or before**: 

* [SQL Server 2012](https://www.microsoft.com/en-gb/download/details.aspx?id=43351#)
* [SQL Server 2016](https://www.microsoft.com/en-GB/evalcenter/evaluate-sql-server-2016)
* [SQL Server 2016 developer edition](https://my.visualstudio.com/Downloads?q=SQL%20Server%202016%20Developer). Note this requires a My visual studio login. An 
  installer may be downloaded from: http://go.microsoft.com/fwlink/?LinkID=799009

SQL Server 2012 developer/evaluation edition databases are limited to 5G in size. This is a series limitation for the RIF. The SQL Server 2016 developer edition
does not haves this limitation and is therefore recommended for RIF development. Note:

* Use of development/evaluation edition is Usage is restricted – design, development, testing and 
  demonstration of programs using the SQL database engine are all permitted, as long as the user has permanent access to the license owner’s internal network. 
  Therefore, while you could demonstrate the RIF to a client, you could not let that client play around with it themselves afterwards. Using the license in 
  any other way, such as to support a commercial software installation, would constitute a breach of the license terms

* Microsoft gets access to your data – it is mandatory with any non-commercial installation of SQL Server that all your usage data covering performance, errors, 
  feature use, IP addresses, device identifiers and more, is sent to Microsoft. There are no exceptions. This will cause issues with particularly sensitive data
  and the developer edition may not work on the private network.

See: https://www.matrix42.com/blog/2016/09/23/how-free-is-microsoft-sql-server-developer-edition-really/ 

If you install SQL Server 2014+; make sure SQL Server Management Studio has been installed; 
[SQL Server Management Studio](https://docs.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms?view=sql-server-2017)

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

or for SQL Server 2016

```
1> SELECT @@version AS version, compatibility_level FROM sys.databases Where DB_NAME() = name ;
2> go
version                                                                                              compatibility_level
---------------------------------------------------------------------------------------------------- -------------------
Microsoft SQL Server 2016 (SP1-GDR) (KB4019089) - 13.0.4206.0 (X64)
        Jul  6 2017 07:55:03
        Copyright (c) Microsoft Corporation
        Developer Edition (64-bit) on Windows 10 Pro 6.3 <X64> (Build 16299: )
```

* The compatibility level should be *110* and the version *Microsoft SQL Server 2012 (SP2...*. If it is not then you have more 
  than one SQL Server database on your machine, see setting *SQLCMDSERVER* in the next section.

# 2. Create Databases and Users

**This section details the original method using the script *rif40_development_creation.sql*. This section is useful to sort out connection 
problems. The easier way to rebuild the RIF is to skip to section 4, and run  *rebuild_all.bat* which runs all the required scriipts 
in sequence and prompts for the RIF user and user password.**

Run the following command as Administrator in this directory (...rapidInquiryFacility\rifDatabase\SQLserver\installation):

```
sqlcmd -E -b -m-1 -e -i rif40_development_creation.sql
```
Note:
- **_This script will destroy all existing users and data_**;
- This script will:
  * Drops and creates the *sahsuland*, *sahsuland_dev* and *test* database and the *rif40*, *rifuser* and *rifmanager* user (logon) roles;   
  * Creates *rif_user*, *rif_manager*, *rif_no_suppression*, *rif_student* and *notarifuser* non logon (i.e. group) roles;
  * The role *rif_user* allows users to create tables and views;
  * The role *rif_manager* allows users to additionally create procedures and functions;
  * The rif40 user can do BULK INSERT;
  * The password for the rif40 login is random; this user is purely present to own objects;
  * The default database is *sahsuland_dev*;
- The application is installed in the *rif40* schema and data is installed in the *rif_data* schema; both owned by the *rif40* role;
- The test database is for building geospatial data. SQL Server express databases are limited to 10G in size; so to maximise the size of data that can be processed
  a separate database is used.

## 2.1 Network connection errors

This is when the above command will not run.

```	
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>sqlcmd -E -b -m-1 -e -r1 -i rif40_database_creation.sql
Result 0x2, Level 16, State 1
Named Pipes Provider: Could not open a connection to SQL Server [2].
Microsoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not found or not accessible. Check if instance name is correct and if SQL Server is configured to allow remote connections. For more information see SQL Server Books Online.
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.
```
  * You may need to specify the instance name: e.g. `-S PETER-PC\SAHSU`, e.g.
	```
	sqlcmd -E -S PETER-PC\SAHSU -b -m-1 -e -r1 -i rif40_database_creation.sql
	```  
    If you set this it will need to be set in the environment as *SQLCMDSERVER*. This is usually caused by 
    multiple installations of SQL server on the machine in the past, i.e. the *DefaultLocalInstance* registry key is wrong.
  * Check if remote access is enabled (it should be) using SQL Server Management Studio as administrator: https://msdn.microsoft.com/en-gb/library/ms191464(v=sql.120).aspx
  * Check TCP access is enabled using SQL Server Configuration Manager as administrator: https://msdn.microsoft.com/en-us/library/ms189083.aspx
    If when you open SQL Server Configuration Manager in SQL Server you get the following error: "Cannot connect to WMI provider. You do not have permission or the server is unreachable"; see:
    (https://support.microsoft.com/en-us/help/956013/error-message-when-you-open-sql-server-configuration-manager-in-sql-se). Make sure you set number to the highest version present in the directory: 

    ```mofcomp "%programfiles(x86)%\Microsoft SQL Server\**&lt;number&gt;**\Shared\sqlmgmproviderxpsp2up.mof"```
    e.g.   
   ```
	C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>mofcomp "%programfiles(x86)%\Microsoft SQL Server\140\Shared\sqlmgmprovid
	erxpsp2up.mof"
	Microsoft (R) MOF Compiler Version 6.3.9600.16384
	Copyright (c) Microsoft Corp. 1997-2006. All rights reserved.
	Parsing MOF file: C:\Program Files (x86)\Microsoft SQL Server\140\Shared\sqlmgmproviderxpsp2up.mof
	MOF file has been successfully parsed
	Storing data in the repository...
	Done!
    ```
  * Check the SQL server port (1433) is listening on TCP/IP for both localhost internal machine connections [::1 and 127.0.0.1:] and if required for network connections 
    ```C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>netstat -an | findstr "143[34]"
	  TCP    0.0.0.0:1433           0.0.0.0:0              LISTENING
	  TCP    127.0.0.1:1434         0.0.0.0:0              LISTENING
	  TCP    129.31.247.202:60396   129.31.247.202:1433    TIME_WAIT
	  TCP    192.168.1.101:60392    192.168.1.101:1433     TIME_WAIT
	  TCP    192.168.1.101:60397    192.168.1.101:1433     TIME_WAIT
	  TCP    [::]:1433              [::]:0                 LISTENING
	  TCP    [::1]:1434             [::]:0                 LISTENING
	  TCP    [2001:0:4137:9e76:2464:202e:7ee0:835]:60398  [2001:0:4137:9e76:2464:202e:7ee0:835]:1433  TIME_WAIT
	  TCP    [fe80::2464:202e:7ee0:835%3]:1433  [fe80::2464:202e:7ee0:835%3]:60395  ESTABLISHED
	  TCP    [fe80::2464:202e:7ee0:835%3]:60395  [fe80::2464:202e:7ee0:835%3]:1433  ESTABLISHED
    ```
    If it is then the first two points have worked and you have a firewall issue!
  * Check your firewall permits access to TCP port 1433. **Be careful _not_ to allow Internet access unless you intend it.**
  * The following is more helpful than the official Microsoft manuals: https://blogs.msdn.microsoft.com/walzenbach/2010/04/14/how-to-enable-remote-connections-in-sql-server-2008/
  * Check you can connect using *sqlcmd -E -S tcp:**&lt;your hostname&gt;**```:
    ```
	C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>sqlcmd -E -S tcp:DESKTOP-4P2SA80
    1>
	```

## 2.2 Power User Issues

This is caused by *rebuild_all.bat* failing complaining the user is not an Administrator when run as a power user.

```
sqlcmd -E
1> SELECT user_name();
2> GO

--------------------------------------------------------------------------------------------------------------------------------
guest

(1 rows affected)
1> quit
``` 

The solution to this is to:

* logon as *sa* using the password for the *sa* provided during the install;
* Create a Windows authenticated user login as the domain user name (e.g. *IC\pch*);
* Grant full database administration privileges (all of them!) to this user;
* Check the user logon is now an Administrator (i.e. is dbo):
	```
	sqlcmd -E
	1> SELECT user_name();
	2> GO

	--------------------------------------------------------------------------------------------------------------------------------
	dbo

	(1 rows affected)
	1> quit
	``` 
* Re-run *rebuild_all.bat*
	
# 3. Create Additional Users

Run the optional script *rif40_development_user.sql*. This creates a default user *%newuser%* from the command environment. This is set from the command line using 
the -v newuser=<my new user> and -v newpw=<my new password> parameters. Run as Administrator:

```
sqlcmd -E -b -m-1 -e -i rif40_development_user.sql -v newuser=peter -v newpw=XXXXXXXXXXXXXXXX
```

* User is created with the *rif_user* (can create tables and views) and *rif_manager* roles (can also create procedures and functions), 
  can do `BULK INSERT`;
* User can use sahsuland_dev and test databases;
* The test database is for geospatial processing and does not have the *rif_user* and *rif_manager* roles, the user can create tables, 
  views;
* procedures and function and do `BULK INSERT`s;
* Will fail to re-create a user if the user already has objects (tables, views etc);

Now test your can connect to the database and check your object creation privileges:
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

## 3.1 Logon errors

Test for logon errors as using the command: `sqlcmd -U peter -P XXXXXXXXXXXXXXXX -d sahsuland_dev`

Test all combinations of *sahsuland*/*sahsuland_dev*/*test* databases.

### 3.1.1 Wrong server authentication mode

The server will need to be changed from Windows Authentication mode to SQL Server and Windows Authentication mode. Then restart SQL Server. 
See: https://msdn.microsoft.com/en-GB/library/ms188670.aspx

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U peter -P XXXXXXXXXXXXXXXX -d sahsuland_dev
Msg 18456, Level 14, State 1, Server PETER-PC\SQLEXPRESS, Line 1
Login failed for user 'peter'.
```

  In the Window "applications" event log:
```
Login failed for user 'peter'. Reason: An attempt to login using SQL authentication failed. Server is configured for Windows authentication only. [CLIENT: <local machine>]
```

  The node also show how to enable the sa (system administrator) account. As with all relational database adminstration accounts as strong (12+ chacracter) password is recommended to defeat 
  attacks by dictionary or all possible passwords.

  This is what a successful login looks like: `sqlcmd -U peter -P XXXXXXXXXXXXXXXX`

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U peter -P XXXXXXXXXXXXXXXX
1> SELECT db_name();
2> GO

--------------------------------------------------------------------------------------------------------------------------------
sahsuland_dev

(1 rows affected)
1>
``` 
 
# 4. Installing the RIF Schema

A script (*rapidInquiryFacility\rifDatabase\SQLserver\installation\rebuild_all.bat*) is provided to build everything and create an example study. Note that this user's password will be the 
username, so change it on a networked system:

* rebuild_all.bat (see note 4.1 below before you run this script)
  rebuild_all.bat can be abort using *control-C*; no other key will abort the script. 
  * This script prompts for the username and password. The default username is *peter*.
  * This user's password by default will be the username, so change it on a networked system!
  * You will be asked if you want to abort; 'Y' or 'y' will abort; any other keys will continue
	```
	C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>rebuild_all.bat

	C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>ECHO OFF
	Administrator PRIVILEGES Detected!
	Creating development RIF databases
	New user [default peter]:
	New user password [default peter]:
	##########################################################################################
	#
	# WARNING! this script will the drop and create the RIF40 sahsuland and sahusland_dev databases.
	# Type control-C to abort.
	#
	# Test user: peter; password: XXXXXXXXXXXXXXXX
	#
	##########################################################################################
	Press any key to continue . . .
	Terminate batch job (Y/N)? y
    ```

Before you run *rebuild_all.bat* check the [BLUK INSERRT permissions](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/installation/README.md#41-bulk-insert-permission)

**These scripts do NOT drop existing tables, the database must be rebuilt from scratch**.
**You _must_ build sahusland_dev before sahsuland**; as *sahsuland_dev* is exported as the basis for *sahsuland*.

The batch tests for Administrator or power user privilege; gets the settings as detailed above and then runs the following scripts 
as Administrator:

* rif40_development_creation.sql - creates the development databases *sahsuland_dev* and *test*.
* rif40_sahsuland_dev_install.bat - to create and export *sahsuland_dev* (see notes 4.1 and 4.3 below before you run this script)
* rif40_sahsuland_install.bat - to create the *sahsuland* database and restore the *sahsuland_dev* backup into it 
  (see note 4.3 below before you run this script). This script also causes a pause before rebuilding *sahsuland*.
  This is described in detail in: 
  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/production/INSTALL.md
* rif40_development_user.sql - to create the development user (default database *sahsuland_dev*)
* rif40_run_study.sql - test the development user by running a study

The individual scripts can be run by batch files for sahsuland_dev only, but they must be run in this order:

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

SQL Server needs access permission granted to the directories used to `BULK INSERT` files, the files are not copied from the client to the 
server as in the *Postgres* *psql* ```\copy` command and the *Oracle* *sqlldr* command.

SQL Server needs access to the relative directories: *..\..\GeospatialData\tileMaker* and *..\..\\DataLoaderData\SAHSULAND*. The simplest
way is to allow read/execute access to the local users group (e.g. PH-LAPTOP\Users or USERS depending on your Windows version).

The following directories need to have read and execute permission granted to local users (and subdirectories):

* *rapidInquiryFacility\rifDatabase\GeospatialData*
* *rapidInquiryFacility\rifDatabase\DataLoaderData*
* *rapidInquiryFacility\rifDatabase\SQLserver*

The following directories need to have read, write, modify and execute permission granted to local users (and subdirectories):

* *rapidInquiryFacility\rifDatabase\SQLserver\production*

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

## 4.2 Re-running scripts

### 4.2.1 Geospatial script: rif40_sahsuland_tiles.bat

Re-running the geospatial script: rif40_sahsuland_tiles.bat will produce the following error:

```
-- SQL statement 75: Remove old geolevels meta data table >>>
DELETE FROM t_rif40_geolevels WHERE geography = 'SAHSULAND';

Msg 547, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
The DELETE statement conflicted with the REFERENCE constraint "rif40_covariates_geolevel_fk". The conflict occurred in database "sah
suland_dev", table "rif40.rif40_covariates".
Msg 3621, Level 0, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 5
The statement has been terminated.
```

To resolve: delete the covariates. You must re-run rif40_sahsuland_data.bat afterwards.

```
DELETE FROM rif40.rif40_covariates WHERE geography = 'SAHSULAND';
```
### 4.2.2 Re-load sahsuland example data

Re-load sahsuland example data with: rif40_sahsuland_data.bat

## 4.3 SQL Server Backup and Restore

SQL Server needs access granted to the directories used to `BACKUP` and to `RESTORE` files.

SQL Server needs access to the relative directory: *..\production*. The simplest
way is to allow full control to the local users group (e.g. PH-LAPTOP\Users).

*DO NOT TRY TO `BACKUP` or `RESTORE` FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).* Use a local directory which SQL Server has
access to; e.g. somewhere on the C: drive. Note that SQL Server *BACKUP* and *RESTORE* behaves differently if you logon using Windows authentication (where it will use your credentials 
to access the files) to using a username and password (where it will use the Server's credentials to access the file).

```
--
-- Export database to ../production/sahsuland_dev.bak
-- Grant local users full control to this directory
--
BACKUP DATABASE [sahsuland_dev] TO DISK='C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\
production\sahsuland_dev.bak';

Msg 3201, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 6
Cannot open backup device 'C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\production\sah
suland_dev.bak'. Operating system error 5(Access is denied.).
Msg 3013, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 6
BACKUP DATABASE is terminating abnormally.
rif40_sahsuland_dev_install.sql exiting with 1
rif40_sahsuland_dev_install.bat exiting with 1
```

## 4.4 SQL Server BULK INSERT Issues

### 4.4.1 Line Termination

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
  The T-SQL functions sp_addsrvrolemember() and sp_addrolemember() are not transactional;
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
  
* To be SQL Server temporary tables (i.e. #g_rif40_comparison_areas) created by the *rif40_run_study* procedure:

	G_RIF40_COMPARISON_AREAS                            
	G_RIF40_STUDY_AREAS 

## 5.1 Script and documentation TODO
	
Still to do (low priority):

* Search path notes - there isn't one!
* On logon trigger - Postgres *rif40_startup()* function and the *rif40_user_objects.sql* script

Peter Hambly
2nd March 2017
