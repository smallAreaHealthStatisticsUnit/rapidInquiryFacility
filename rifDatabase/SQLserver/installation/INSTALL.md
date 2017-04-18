SQL Server Production Database Installation
===========================================

# Contents
- [1. Install SQL Server 2012 SP2](#1-install-sql-server-2012-sp2)
- [2. Create Databases and Users](#2-create-databases-and-users)
   - [2.1 Network connection errors](#21-network-connection-errors)
   - [2.2 Logon errors](#22-logon-errors)
     - [2.2.1 Wrong server authentication mode](#221-wrong-server-authentication-mode)
- [3. Create Additional Users](#3-create-additional-users)
- [4. Installing the RIF Schema](#4-installing-the-rif-schema)
  - [4.1 BULK INSERT Permission](#41-bulk-insert-permission)

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

## 4.1 BULK INSERT Permission

SQL Server needs access granted to `BULK INSERT` files, they are not copied from the client to the server.

SQL Server needs access to the relative directories: *..\..\GeospatialData\tileMaker* and *..\..\\DataLoaderData\SAHSULAND*. The simplest
way is to allow read/execute access to the local users group (e.g. PH-LAPTOP\Users).

*DO NOT TRY TO RUN BULK INSERT FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).* Use a local directory which SQL Server has
access to; e.g. somewhere on the C: drive. Note that SQL Server *BULK LOAD* behaves dirrently if you logon using Windows authentication (where it will use your credentials 
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
