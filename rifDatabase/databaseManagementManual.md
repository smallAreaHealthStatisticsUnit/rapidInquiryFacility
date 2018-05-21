Database Management Manual
==========================

# Contents

- [1. Overview](#1-overview)
- [2. User Management](#2-user-management)
  - [2.1 Creating new users](#21-creating-new-users)
     - [2.1.1 Postgres](#211-postgres)
     - [2.1.2 SQL Server](#212-sql-server)
  - [2.2 Changing passwords](#22-changing-passwords)
     - [2.2.1 Postgres](#221-postgres)
     - [2.2.2 SQL Server](#222-sql-server)
  - [2.3 Proxy accounts](#23-proxy-accounts)
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

New users must be created in lower case, start with a letter, and only contain the characters: ```[A-Z][a-z][0-9]_```

### 2.1.1 Postgres

### 2.1.2 SQL Server

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

## 2.4 Granting permission

### 2.4.1 Postgres

### 2.4.2 SQL Server

## 2.5 Viewing your user setup

### 2.5.1 Postgres

### 2.5.2 SQL Server

# 3. Data Management
 
## 3.1 Creating new schemas

### 3.1.1 Postgres

### 3.1.2 SQL Server

## 3.2 Tablespaces

### 3.2.1 Postgres

### 3.2.2 SQL Server

## 3.3 Partitioning

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
  
# 5. Backup and recovery  

## 5.1 Postgres

## 5.2 SQL Server

# 6. Patching  

Alter scripts are numbered sequentially and have the same functionality in both ports, e.g. ```v4_0_alter_10.sql```. Scripts are safe to run more than once.

Pre-built databases are supplied patched up to date.

Scripts must be applied as follows:

| Date            | Script            | Description                         |
|-----------------|-------------------|-------------------------------------|
| 30th June  2018 | v4_0_alter_10.sql | To be added (risk analysis changes) |

## 6.1 Postgres

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

## 7.2 SQL Server

Peter Hambly
Many 2018