---
layout: default
title: SQL Server Production Database Installation
---

1. Contents
{:toc}

# Install SQL Server

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

# Installing the RIF

A standalone script *rif40_database_install.bat* is provided to install the RIF. It is designed to run in a single directory, and is in
*...rapidInquiryFacility\rifDatabase\SQLserver\production*. A backup of the *sahsuland_dev* database is required, as created by
*rebuild_all.bat* see: [SQL Server Development Database Installation]({{ site.baseurl }}/rifDatabase/SQLserver/installation/) or supplied by SAHSU.
If you use the pre-built version check that the dump *sahsuland_dev.bak* is unZipped.

You will need to enter:

* Database name
* User name
* User password

The database name and user name should only contain lowercase letters, underscore (\_) and numbers and must start with a letter.
The default password is the same as the username; change it on a production system connected to the internet!

Before you run *rebuild_all.bat* check the [restore permissions]({{ site.baseurl }}/rifDatabase/SQLserver/production/INSTALL#sql-server-restore)

**THESE SCRIPTS DO DROP ALL EXISTING TABLES AND DATA**.

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
  C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\production>rif40_database_install.bat

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

## Network connection errors

This is when the above command will not run.

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>sqlcmd -E -d rif40 -b -m-1 -e -r1 -i rif40_production_creation.sql
Result 0x2, Level 16, State 1
Named Pipes Provider: Could not open a connection to SQL Server [2].
Microsoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not found or not accessible. Check if instance name is correct and if SQL Server is configured to allow remote connections. For more information see SQL Server Books Online.
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.
```

  * You may need to specify the instance name: e.g. `-S PETER-PC\SAHSU`, e.g.
	```
	sqlcmd -E -S PETER-PC\SAHSU -d rif40 -b -m-1 -e -r1 -i rif40_production_creation.sql -v import_dir="%cd%\" -v newdb="%NEWDB%"
	```
	
    If you set this it will need to be set in the environment as *SQLCMDSERVER*. This is usually caused by
    multiple installations of SQL server on the machine in the past, i.e. the *DefaultLocalInstance* registry
	key is wrong. In Windows 10/SQL Server 2012 setting the instance name caused an error (i.e. PETER-PC worked but
	PETER-PC\SAHSU did not).
  * Check if remote access is enabled (it should be) using SQL Server Management Studio as administrator: (https://msdn.microsoft.com/en-gb/library/ms191464(v=sql.120).aspx)
  * Check TCP access is enabled using SQL Server Configuration Manager as administrator: https://msdn.microsoft.com/en-us/library/ms189083.aspx
  * Check your firewall permits access to TCP port 1433. **Be careful _not_ to allow Internet access unless you intend it.**
  * The following is more helpful than the official Microsoft manuals: (https://blogs.msdn.microsoft.com/walzenbach/2010/04/14/how-to-enable-remote-connections-in-sql-server-2008/)

Now test your can connect to the database.

## Logon errors

Test for logon errors as using the command: ```sqlcmd -U peter -P XXXXXXXXXXXX```

### Wrong server authentication mode

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

The node also show how to enable the sa (system administrator) account. As with all relational database administration accounts as strong (12+ character) password is recommended to defeat
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

## SQL Server Restore

SQL Server needs access granted to the directories used to the `RESTORE` file used to import the database.

SQL Server needs access to the current directory. The simplest
way is to allow read and execute permission to the local users group (e.g. PH-LAPTOP\Users).

*DO NOT TRY `RESTORE` FROM NETWORK DRIVES or CLOUD DRIVES (e.g. Google Drive).* Use a local directory which SQL Server has
access to; e.g. somewhere on the C: drive. Note that SQL Server *RESTORE* behaves differently if you logon using Windows authentication (where it will use your credentials
to access the files) to using a username and password (where it will use the Server's credentials to access the file).

```
--
-- Export database to ../production/sahsuland_dev.bak
-- Grant local users read and execute to this directory
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

## Power User Issues

This is caused by *rif40_database_install.bat* failing complaining the user is not an Administrator when run as a power user.

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
* Re-run *rif40_database_install.bat*

# Create Additional Users

Run the optional script *rif40_production_user.sql*. This creates a default user *%newuser%* from the command environment. This is set from the command line using
the ```-v newuser=<my new user>``` and ```-v newpw=<my new password>``` parameters. Run as Administrator:

```
sqlcmd -E -b -m-1 -e -i rif40_production_user.sql -v newuser=kevin -v newpw=XXXXXXXXXXXX
```

* User is created with the *rif_user* (can create tables and views) and *rif_manager* roles (can also create procedures and functions);
* User can use the sahsuland database;
* Will fail to re-create a user if the user already has objects (tables, views etc);

Test connection and object privileges:
```sql
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

# Installation By Hand

This assumes that your database team will not run the *rif40_database_install.bat* script as an Administrator, so they will have to run:
*rif40_production_creation.sql* as a database administrator or run the commands manually in it, and then do the same for
*rif40_production_user.sql* to create a user, also as an administrator. The RIF must have a *rif40* schema account; although the password can be set to gibberish.
There are no restrictions as to the database name (other than it being valid).

All commands are assumed to be run by an administrator.

## Creating The Database

These instructions are based on *rif40_production_creation.sql*. This uses *NEWUSER*, *NEWDB* and *import_dir* from the CMD enviornment.

* Change "mydatabasename" to the name of your database, e.g. *sahsuland*;
* Change "mybackuppath" to the path to the supplied backup file, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\production*

1. Validate the database name:

	```sql
	USE master;
	GO
	DECLARE @newdb VARCHAR(MAX)='mydatabasename';
	DECLARE @invalid_chars INTEGER;
	DECLARE @first_char VARCHAR(1);
	SET @invalid_chars=PATINDEX('%[^0-9a-z_]%', @newdb);
	SET @first_char=SUBSTRING(@newdb, 1, 1);
	IF @invalid_chars IS NULL
		RAISERROR('New database name is null', 16, 1, @newdb);
	ELSE IF @invalid_chars > 0
		RAISERROR('New database name: %s contains invalid character(s) starting at position: %i.', 16, 1,
			@newdb, @invalid_chars);
	ELSE IF (LEN(@newdb) > 30)
		RAISERROR('New database name: %s is too long (30 characters max).', 16, 1, @newdb);
	ELSE IF ISNUMERIC(@first_char) = 1
		RAISERROR('First character in database name: %s is numeric: %s.', 16, 1, @newdb, @first_char);
	ELSE
		PRINT 'New database name: ' + @newdb + ' OK';
	GO
	```

2. Create the database

	```sql
	USE master;
	GO
	IF EXISTS(SELECT * FROM sys.sysdatabases where name='mydatabasename')
		DROP DATABASE mydatabasename;
	GO
	CREATE DATABASE mydatabasename;
	GO
	```

3. Import Database from supplied backup file

	```sql
	USE master;
	GO
	--
	-- Find the actual database file names for the new mydatabasename DB
	--
	SELECT DB_NAME(mf1.database_id) AS database_name,
		   mf1.physical_name AS physical_db_filename,
		   mf2.physical_name AS physical_log_filename
	  FROM sys.master_files mf1, sys.master_files mf2
	 WHERE mf1.database_id = mf2.database_id
	   AND mf1.name        = 'mydatabasename'
	   AND mf2.name        = 'mydatabasename_log';
	GO

	--
	-- Import database from sahsuland_dev.bak
	-- Grant local users read access to this directory
	--
	DECLARE c1_db CURSOR FOR
		SELECT DB_NAME(mf1.database_id) AS database_name,
			   mf1.physical_name AS physical_db_filename,
			   mf2.physical_name AS physical_log_filename
		  FROM sys.master_files mf1, sys.master_files mf2
		 WHERE mf1.database_id = mf2.database_id
		   AND mf1.name        = 'mydatabasename'
		   AND mf2.name        = 'mydatabasename_log';
	DECLARE @database_name 			VARCHAR(30);
	DECLARE @physical_db_filename 	VARCHAR(MAX);
	DECLARE @physical_log_filename 	VARCHAR(MAX);
	DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);

	OPEN c1_db;
	FETCH NEXT FROM c1_db INTO @database_name, @physical_db_filename, @physical_log_filename;
	CLOSE c1_db;
	DEALLOCATE c1_db;

	/*

	E.g.:

	SQL[dbo]> RESTORE DATABASE [sahsuland]
			FROM DISK='C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\production\sahsuland_dev.bak'
			WITH REPLACE,
			MOVE 'sahsuland_dev' TO 'F:\SqlServer\sahsuland.mdf',
			MOVE 'sahsuland_dev_log' TO 'F:\SqlServer\sahsuland_log.ldf';
	Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
	Processed 45648 pages for database 'sahsuland', file 'sahsuland_dev' on file 1.
	Msg 4035, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
	Processed 14 pages for database 'sahsuland', file 'sahsuland_dev_log' on file 1.
	Msg 3014, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
	RESTORE DATABASE successfully processed 45662 pages in 5.130 seconds (69.538 MB/sec).
	SQL[dbo]> ALTER DATABASE [sahsuland] MODIFY FILE ( NAME = sahsuland_dev, NEWNAME = sahsuland);
	Msg 5021, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
	The file name 'sahsuland' has been set.
	SQL[dbo]> ALTER DATABASE [sahsuland] MODIFY FILE ( NAME = sahsuland_dev_log, NEWNAME = sahsuland_log);
	Msg 5021, Level 0, State 1, Server PETER-PC\SAHSU, Line 1
	The file name 'sahsuland_log' has been set.

	 */

	DECLARE @sql_stmt NVARCHAR(MAX);
	SET @sql_stmt =	'RESTORE DATABASE [mydatabasename]' + @crlf +
	'        FROM DISK=''mybackuppath\sahsuland_dev.bak''' + @crlf +
	'        WITH REPLACE,' + @crlf +
	'        MOVE ''sahsuland_dev'' TO ''' + @physical_db_filename + ''',' + @crlf +
	'        MOVE ''sahsuland_dev_log'' TO ''' + @physical_log_filename + '''';
	PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
	EXECUTE sp_executesql @sql_stmt;
	SET @sql_stmt='ALTER DATABASE [mydatabasename] MODIFY FILE ( NAME = sahsuland_dev, NEWNAME = mydatabasename)';
	PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
	EXECUTE sp_executesql @sql_stmt;
	SET @sql_stmt='ALTER DATABASE [mydatabasename] MODIFY FILE ( NAME = sahsuland_dev_log, NEWNAME = mydatabasename_log)';
	PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
	EXECUTE sp_executesql @sql_stmt;
	GO
	```

4. Create the schema owner (rif40)

   The RIF schema owner **MUST** be called *rif40* and will require BULK INSERT privilege to load data.

   Most users will need to set the password to be able to load data.
  
	```sql
	USE master;
	GO
	--
	-- RIF40: Schema owner
	--
	IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name = N'rif40') BEGIN
		DECLARE @sql_stmt NVARCHAR(MAX);
		DECLARE @stp VARCHAR(60)=CAST(GETDATE() AS VARCHAR);
		SET @sql_stmt =	'CREATE LOGIN [rif40] WITH PASSWORD=''' + CONVERT(VARCHAR(32), HASHBYTES('MD5', @stp), 2) + /* PW changes every minute */
			''', CHECK_POLICY = OFF';
		PRINT 'SQL[' + USER + ']> ' + @sql_stmt + ';';
		EXECUTE sp_executesql @sql_stmt;	;	-- Change this password if you want to logon!
	END;
	GO
	```

5. Create database specific roles
  
	```sql
	USE mydatabasename;
	GO

	--
	-- Check database is NOT master
	--
	DECLARE @database_name 	VARCHAR(30)=DB_NAME();
	IF (@database_name = 'master')
		RAISERROR('rif40_database_roles.sql: Database is master: %s', 16, 1, @database_name);
	GO

	--
	-- Create database users, roles and schemas for sahsuland/sahsuland_dev
	--

	IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = N'rif40')
		CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo]
		ELSE ALTER USER [rif40] WITH LOGIN=[rif40];
	GO

	IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif40')
	EXEC('CREATE SCHEMA [rif40] AUTHORIZATION [rif40]');
	GO
	IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_data')
	EXEC('CREATE SCHEMA [rif_data] AUTHORIZATION [rif40]');
	GO
	IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = N'rif_studies')
	EXEC('CREATE SCHEMA [rif_studies] AUTHORIZATION [rif40]');
	GO

	ALTER USER [rif40] WITH DEFAULT_SCHEMA=[rif40];
	GO

	ALTER LOGIN [rif40] WITH DEFAULT_DATABASE = [mydatabasename];
	GO

	--
	-- Default per databasxe (not server!) roles
	--
	IF DATABASE_PRINCIPAL_ID('rif_manager') IS NULL
		CREATE ROLE [rif_manager];
	GO
	IF DATABASE_PRINCIPAL_ID('rif_user') IS NULL
		CREATE ROLE [rif_user];
	GO
	IF DATABASE_PRINCIPAL_ID('rif_student') IS NULL
		CREATE ROLE [rif_student];
	GO
	IF DATABASE_PRINCIPAL_ID('rif_no_suppression') IS NULL
		CREATE ROLE [rif_no_suppression];
	GO
	IF DATABASE_PRINCIPAL_ID('notarifuser') IS NULL
		CREATE ROLE [notarifuser];
	GO

	--
	-- Object privilege grants
	--
	GRANT CREATE FUNCTION TO [rif_manager];
	GO
	GRANT CREATE PROCEDURE TO [rif_manager];
	GO
	GRANT CREATE TABLE TO [rif_manager];
	GO
	GRANT CREATE VIEW TO [rif_manager];
	GO
	GRANT CREATE TABLE TO [rif_user];
	GO
	GRANT CREATE VIEW TO [rif_user];
	GO

	GRANT CREATE FUNCTION TO [rif40];
	GO
	GRANT CREATE PROCEDURE TO [rif40];
	GO
	GRANT CREATE TABLE TO [rif40];
	GO
	GRANT CREATE VIEW TO [rif40];
	GO
	GRANT CREATE TYPE TO [rif40];
	GO

	--
	-- Grant USAGE on the rif_studies schema to RIF40. This implies control
	--
	GRANT ALTER ON SCHEMA :: rif_studies TO [rif40];
	GO

	--
	-- Allow rif40 BULK INSERT
	--
	EXEC sp_addsrvrolemember @loginame = N'rif40', @rolename = N'bulkadmin';
	GO
	```

## Creating a New User

These instructions are based on *rif40_production_user.sql*. This uses *NEWUSER* and *NEWDB* from the CMD enviornment.

* Change "mydatabasename" to the name of your database, e.g. *sahsuland*;
* Change "mydatabasenuser" to the name of your user, e.g. *peter*;
* Change "mydatabasepassword" to the name of your users password;

1. Validate the RIF user

	```sql
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

	```sql
	USE [master];
	GO
	IF NOT EXISTS (SELECT * FROM sys.sql_logins WHERE name = N'mydatabaseuser')
	CREATE LOGIN [mydatabaseuser] WITH PASSWORD='mydatabasepassword', CHECK_POLICY = OFF;
	GO

	ALTER LOGIN [mydatabaseuser] WITH DEFAULT_DATABASE = [mydatabasename];
	GO
	```

3. Create user and grant roles

	```sql
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

    ```sql
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

## Testing The Database

The best test of a correctly installed database is to logon as a test user and select from *rif40_num_denom* (numerator/denominator) pair view. The standard row
will only appear if you can find and select from both tables:

```sql
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility>sqlcmd -U peter -P XXXXXXXXXXXXXXXXXXXXXXX -d sahsuland
1> select * from rif40_num_denom;
2> go
geography                                          numerator_table                numerator_description                                                                                                                                                                                                                                      theme_description
                                                                                                                                      denominator_table              denominator_description
                automatic
-------------------------------------------------- ------------------------------ ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------- ------------------------------ -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------- ---------
SAHSULAND                                          NUM_SAHSULAND_CANCER           cancer numerator                                                                                                                                                                                                                                           covering various types of cancers
                                                                                                                                      POP_SAHSULAND_POP              population health file
                        1

(1 rows affected)
1>
```

