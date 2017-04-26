SQL Server Production Database Installation
===========================================

# Contents
- [1. Install SQL Server 2012 SP2](#1-install-sql-server-2012-sp2)
- [2. Installing the RIF](#2-installing-the-rif)
   - [2.1 Network connection errors](#21-network-connection-errors)
   - [2.2 Logon errors](#22-logon-errors)
     - [2.2.1 Wrong server authentication mode](#221-wrong-server-authentication-mode)
   - [2.3 SQL Server Restore](#23-sql-server-restore)
   - [2.4 Power User Issues](#24-power-user-issues)
- [3. Create Additional Users](#3-create-additional-users)

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

# 2. Installing the RIF

A standalone script *rif40_sahsuland_install.bat* is provided to install the RIF. It is designed to run in a single directory, and is in
*...rapidInquiryFacility\rifDatabase\SQLserver\production*. A backup of the *sahsuland_dev* database is required, as created by 
*rebuild_all.bat* (see: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/SQLserver/installation/README.md) or supplied by SAHSU. 

Either:

1. Create *...rapidInquiryFacility\rifDatabase\SQLserver\production\sahsuland_dev.bak* using rebuild_all.bat

or:

2. Use a pre-built database dump provided by SAHSU. If you use the prebuilt version check that the
   dump *sahsuland_dev.bak* is unZipped.
   
You will need to enter:

* Database name
* User name
* User password

The database name and user name should only contain lowercase letters, underscore (\_) and numbers and must start with a letter.
The default password is the same as the username; chnage it on a production system conntected to the internet!

This script runs:

* rif40_production_creation.sql - this creates the new database $(NEWDB) (by default *sahsuland*) and then runs:
  * rif40_roles.sql - to re-create cluster logins and roles (i.e. the *rif40* login);
  * rif40_database_roles.sql - creates database users, roles and schemas;
  * rif40_custom_error_messages.sql - add custom error messages to *master* database;
  * Import database from ./sahsuland_dev.bak into $(NEWDB); fixing the log file names so to be as just created for $(NEWDB);
  * rif40_production_user.sql - creates the production user specified. This in turn runs:
    * rif40_user_objects.sql;
	
  Notes: 
	* *Does not grant ```BLUK INSERT```*;
	* User is a RIF manager.

* Script output. Use control-C to abort the script before database (re-)creation.	
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\production>rif40_sahsuland_install.bat

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\production>ECHO OFF
Administrator PRIVILEGES Detected!
Creating production RIF database
New user [default peter]:  kevin
New RIF40 db [default sahsuland]: rif40
New user password [default kevin]:  garwood1901
##########################################################################################
#
# WARNING! this script will the drop and create the RIF40 rif40 database.
# Type control-C to abort.
#
# Test user: kevin; password: garwood1901
#
##########################################################################################
Press any key to continue . . .
```

* Typing ```<enter>``` produces a lot of output. Successful completion looks like:

```
--
-- Eof (rif40_user_objects.sql)

--
-- Eof (rif40_production_user.sql)

rif40_production_user.sql built OK 0; created RIF40 production database sahsuland with user: peter

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\production>
```
	
## 2.1 Network connection errors

This is when the above command will not run.

```	
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>sqlcmd -E -d rif40 -b -m-1 -e -r1 -i rif40_production_creation.sql
Result 0x2, Level 16, State 1
Named Pipes Provider: Could not open a connection to SQL Server [2].
Mirosoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not found or not accessible. Check if instance name is correct and if SQL Server is configured to allow remote connections. For more information see SQL Server Books Online.
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.
```
  * You may need to specify the instance name: e.g. `-S PETER-PC\SAHSU`, e.g.
	```
	sqlcmd -E -S PETER-PC\SAHSU -d rif40 -b -m-1 -e -r1 -i rif40_production_creation.sql -v import_dir="%cd%\" -v newdb="%NEWDB%"
	```  
    If you set this it will ned to be set in the environment as *SQLCMDSERVER*. This is usually caused by 
    multiple installations of SQL server on the machine in the past, i.e. the *DefaultLocalInstance* registry key is wrong.
  * Check if remote access is enabled (it should be) using SQL Server Management Studio as adminstrator: https://msdn.microsoft.com/en-gb/library/ms191464(v=sql.120).aspx
  * Check TCP access is enabled using SQL Server Configuration Manager as adminstrator: https://msdn.microsoft.com/en-us/library/ms189083.aspx
  * Check your firewall permits access to TCP port 1433. **Be careful _not_ to allow Internet access unless you intend it.**
  * The following is more helpful than the official Microsoft manuals: https://blogs.msdn.microsoft.com/walzenbach/2010/04/14/how-to-enable-remote-connections-in-sql-server-2008/

Now test your can connect to the database.

## 2.2 Logon errors

Test for logon errors as using the command: `sqlcmd -U peter -P XXXXXXXXXXXX

### 2.2.1 Wrong server authentication mode

The server will need to be changed from Windows Authentication mode to SQL Server and Windows Authentication mode. Then restart SQL Server. 
See: https://msdn.microsoft.com/en-GB/library/ms188670.aspx

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U peter -P XXXXXXXXXXXX
Msg 18456, Level 14, State 1, Server PETER-PC\SQLEXPRESS, Line 1
Login failed for user 'peter'.
```

  In the Window "applications" event log:
```
Login failed for user 'peter'. Reason: An attempt to login using SQL authentication failed. Server is configured for Windows authentication only. [CLIENT: <local machine>]
```

  The node also show how to enable the sa (system adminstrator) account. As with all relational database adminstration accounts as strong (12+ chacracter) password is recommended to defeat 
  attacks by dictionary or all possible passwords.

  This is what a successful login looks like: `sqlcmd -U kevin -P XXXXXXXXXXXX`

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>sqlcmd -U kevin -P XXXXXXXXXXXX
1> SELECT db_name();
2> GO

--------------------------------------------------------------------------------------------------------------------------------
rif40

(1 rows affected)
1>
``` 

The database specified by *db_name()* will be the one specified by $(NEWDB). Running this script against a pre-existing development user 
will change the default database from *sahsuland_dev* to *$(NEWDB)*.

## 2.3 SQL Server Restore

SQL Server needs access granted to the drectories used to the `RESTORE` file used to import the database.

SQL Server needs access to the current directory. The simplest
way is to allow read/executre permission to the local users group (e.g. PH-LAPTOP\Users).

*DO NOT TRY `RESTORE` FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).* Use a local directory which SQL Server has
access to; e.g. somewhere on the C: drive. Note that SQL Server *RESTORE* behaves dirrently if you logon using Windows authentication (where it will use your credentials 
to access the files) to using a username and password (where it will use the Server's credentials to acces the file).

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

## 2.4 Power User Issues

This is caused by *rif40_sahsuland_install.bat* failing complaining the user is not an Administrator when run as a power user.

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
* Grant full database adminstration privileges (all of them!) to this user;
* Check the user logon is now an Adminstrator (i.e. is dbo):
	```
	sqlcmd -E
	1> SELECT user_name();
	2> GO

	--------------------------------------------------------------------------------------------------------------------------------
	dbo

	(1 rows affected)
	1> quit
	``` 
* Re-run *rif40_sahsuland_install.bat*
	
# 3. Create Additional Users

Run the optional script *rif40_production_user.sql*. This creates a default user *%newuser%* from the command environment. This is set from the command line using 
the -v newuser=<my new user>  and -v newpw=<my new password> parameters. Run as Administrator:

```
sqlcmd -E -b -m-1 -e -i rif40_production_user.sql -v newuser=kevin -v newpw=XXXXXXXXXXXX
```

* User is created with the *rif_user* (can create tables and views) and *rif_manager* roles (can also create procedures and functions);
* User can use the sahsuland database;
* Will fail to re-create a user if the user already has objects (tables, views etc);

Test connection and object privilges:
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
1> quit

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>
```


