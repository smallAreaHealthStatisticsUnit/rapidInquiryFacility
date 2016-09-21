To install database, run in this order (I think):

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

4. Run the following scripts in order:

* rif40_install_sequences.bat
* rif40_install_tables.bat
* rif40_install_functions.bat
* rif40_install_views.bat
* rif40_install_log_error_handling.bat
* rif40_install_table_triggers.bat
* rif40_install_view_triggers.bat
* rif40_data_install_tables.bat

Or run:

* rif40_sahsuland_dev_install.bat

Notes:

* All scripts are now transactional, with a script of the same name usually in the source code directory;
* The function rif40_sequence_current_value() is created earlier by the sequences SQL script and 
  cannot be recreated once tables have been created;
* rif40_import_data.bat is now path independent. The SQL script (in this directory) will now delete all 
  setup data in the database!
	





