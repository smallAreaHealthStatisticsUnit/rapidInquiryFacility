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
	 
# 1. Overview

This manual details how to manage RIF databases. See also the:

* [Tile-maker manual](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/tileMaker.md) for how to create
  RIF administrative geographies.
* [RIF Manual Data Loading manual](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase\DataLoaderData\DataLoading.md) 
  for details on the manual process for the loading of data into the RIF.

# 2. User Management
 
## 2.1 Creating new users

### 2.1.1 Postgres

### 2.1.2 SQL Server

## 2.2 Changing passwords

### 2.2.1 Postgres

### 2.2.2 SQL Server

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

### 3.4.1 Postgres

### 3.4.2 SQL Server

# 4. Information Governance

## 4.1 Auditing

### 4.1.1 Postgres

Basic statement logging can be provided by the standard logging facility with the configuration parameter ```log_statement = all```. 
Postgres has an extension [pgAudit](https://github.com/pgaudit/pgaudit) which provides much more auditing, however the Enterprise DB installer does not include Postgres 
extensions (apart from PostGIS). EnterpiseDB Postgres has its own auditing subsystem (*edb_audit), but this is is paid for item. To use pgAudit pgAudit must be compile from source

To configure Postgres server [error reporting and logging](https://www.postgresql.org/docs/9.6/static/runtime-config-logging.html) set:

* ```log_statement = all```. The default is 'none';
* ```Set log_min_error_statement = ERROR``` [default] or lower;
* ```log_error_verbosity = VERBOSE```;
* ```log_connections = TRUE```;
* ```log_disconnections = TRUE```;
* ```log_destinstion = stderr, eventlog, csvlog```. Other choices are *csvlog* or syslog*

Parameters can be set in the *postgresql.conf* file or on the server command line. This is stored in the database cluster's data directory, e.g. *C:\Program Files\PostgreSQL\9.6\data*. Beware, you can 
move the data directory to a solid state disk, mine is: * E:\Postgres\data*! Check the startup parameters in the Windows services app for the *"-D"* flag: 
```"C:\Program Files\PostgreSQL\9.6\bin\pg_ctl.exe" runservice -N "postgresql-x64-9.6" -D "E:\Postgres\data" -w```

If you are using CSV log files set:

* ```logging_collector = TRUE```;
* ```log_filename = postgresql-%Y-%m-%d.csv``` and ```log_rotation_age = 1440``` (in minutes) to provide a consistent, predictable naming scheme for your log files. This lets you predict what the file name will be and know 
  when an individual log file is complete and therefore ready to be imported. The log filename is in [strftime()](http://www.cplusplus.com/reference/ctime/strftime/) format; 
* ```log_rotation_size = 0``` to disable size-based log rotation, as it makes the log file name difficult to predict;
* ```log_truncate_on_rotation = TRUE``` to on so that old log data isn't mixed with the new in the same file.

 
### 4.1.2 SQL Server
  
# 5. Backup and recovery  

## 5.1 Postgres

## 5.2 SQL Server

# 6. Patching  

## 6.1 Postgres

## 6.2 SQL Server

Peter Hambly
Many 2018