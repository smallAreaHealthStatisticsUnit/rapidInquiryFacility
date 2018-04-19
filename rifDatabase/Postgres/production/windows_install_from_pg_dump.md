
Windows Postgres Install using pg_dump and scripts
==================================================

# Contents

- [1. Installation Prerequisites](#1-installation-prerequisites)
   - [1.1 Postgres](#11-postgres)	
      - [1.1.1 Postgres User Setup](#111-postgres-user-setup)
      - [1.1.2 Fixing Windows Code page errors](#112-fixing-windows-code-page-errors)
- [2. Installing a production database](#2-installing-a-production-database)
  - [2.1 Changing the postgres database administrator password](#21-Changing-the-postgres-database-administrator-password)

# 1 Installation Prerequisites
## 1.1 Postgres
 
* Uses *pg_dump* and Powershell
* Does **NOT** need *make* or *Node.js*

Postgres is best downloaded from Enterprise DB: http://www.enterprisedb.com/products-services-training/pgdownload. 
This is standard PostGres as packaged by Enterprise DB and not their own Postgres based product EDB Postgres
Enterprise/Standard/Developer. An installation guide is at: 
http://get.enterprisedb.com/docs/PostgreSQL_Installation_Guide_v9.6.pdf

The Postgres installer will ask for a password for Postgres; do **NOT** use internationalised characters, e.g. **£** as the 
database usually defaults to US ASCII!

The Postgres installer then runs stack builder to download and install the additional packages. 
Choose the local port. The following additional packages need to be installed:

* PostGres (database, PG Admin III administration tool, and common extensions)
* PostGIS (Geospatial integration)
* pgODBC (ODBC database connector for PostGres, 32 and 64 bit)

The PostGIS installer will ask to enable the builtin GDAL (Geospatial Data Abstraction Library). This 
**MUST** be *chosen*.

Once the install is complete:

* Add the Postgres bin directory (e.g. *C:\Program Files\PostgreSQL\9.5\bin*) to the path
* The following should normally be set in your environment:

  * PGDATABASE - &lt;your database name&gt;

* Check you can logon as postgres. If you need to reset the password see 
[Changing the postgres database administrator password](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/production/windows_install_from_pg_dump.md#21-Changing-the-postgres-database-administrator-password)

  ```
  C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>psql -U postgres -d postgres
  psql (9.5.0)
  Type "help" for help.

  postgres=#  
  ```
  * If you get a code page error on Windows see 1.1.2 below.

### 1.1.1 Postgres User Setup

The following users are created by the install script (*rif40_database_install.bat*):

* postgres     - The database administrator. You will have set this password when you created 
                 the Postgres cluster.
* rif40        - The schema owner. 
* &lt;user login&gt; - Your user login. This must be in lowercase and without spaces. 

The install script (*rif40_database_install.bat*) will create your local .pgpass/pgpass.conf files 
(see Postgres documentation for its location on various OS) and create random passwords for *rif40*
and *&lt;user login&gt;*.

E.g. C:\Users\pch\AppData\Roaming\postgresql\pgpass.conf:
```
localhost:5432:*:postgres:<password>
localhost:5432:*:pch:<password>
localhost:5432:*:rif40:<password>
```

### 1.1.2 Fixing Windows Code page errors

```
H:\>psql
psql (9.3.2)
WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
SSL connection (cipher: DHE-RSA-AES256-SHA, bits: 256)
Type "help" for help.
```
* Type:
```
cmd.exe /c chcp 1252
```
* Or: modify the cmd shortcut (find the cmd icon, right click, properties, shortcut panel) to run *cmd.exe /k chcp 1252*

# 2 Installing a production database

Run the database installer batch script as an administrator: rif40_database_install.bat.

The script will ask the user for:

* The production RIF user name (default: *peter*)
* The database name (default: *sahsuland*)

**ONLY USE LOWERCASE LETTERS, UNDERSCORE AND THE DIGITS 0-9; START WITH A LTTER**

The script will create the user pgpass.conf (in C:\Users\%USERNAME%\AppData\Roaming\postgresql) and the 
create the psqlrc psql logon script. If required it will:

* Ask the user for the *postgres* databaase administrator password (set as part of the database install)
* Set RIF40 user password (default: *rif40_&lt;randon digits&gt;_&lt;randon digits&gt;*)
* The production RIF user password (default: *&lt;username&gt;_&lt;randon digits&gt;_&lt;randon digits&gt;*)

The script runs *db_create.sql* to create the database and users and then restores the database from the
supplied sahsuland.dump using *pg_restore*.

```
C:\Users\support\Downloads\OneDrive-2018-04-18\Postgres>rif40_database_install.bat
Creating production RIF Postgres database
PG_SYSCONFDIR=C:/PROGRA~1/POSTGR~1/9.6/etc
Administrator PRIVILEGES Detected!
New user [default peter]:       pch
New RIF40 db [default sahsuland]:       trumpton
Using previously created "C:\Users\support\AppData\Roaming\postgresql\pgpass.conf"
Adding pch to PG password file: "C:\Users\support\AppData\Roaming\postgresql\pgpass.conf"
##########################################################################################
#
# WARNING! this script will the drop and create the RIF40 trumpton Postgres database.
# Type control-C to abort.
#
# Test user pch; password pch_13958_373
# Postgres password       Summ5r1609
# Schema (rif40) password rif4010669_20906
# PG password directory
# PG sysconfig directory  C:/PROGRA~1/POSTGR~1/9.6/etc
#
##########################################################################################
Press any key to continue . . .
Log: db_create.rpt
Working directory: C:\Users\support\Downloads\OneDrive-2018-04-18\Postgres
Command: psql
Arguments: -U postgres -d postgres -h localhost -w -e -P pager=off -v testuser=pch -v newdb=trumpton -v newpw=pch_13958_373 -v verbo
sity=terse -v debug_level=1 -v echo=all -v postgres_password=Summ5r1609 -v rif40_password=rif4010669_20906 -v tablespace_dir= -v pgh
ost=localhost -v os=Windows_NT -f db_create.sql
You are connected to database "postgres" as user "postgres" on host "localhost" at port "5432".
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT p.proname
                  FROM pg_proc p, pg_namespace n
                 WHERE p.proname  = 'rif40_startup'
                   AND n.nspname  = 'rif40_sql_pkg'
                   AND p.proowner = (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
                   AND n.oid      = p.pronamespace;
--
        c1_rec RECORD;
        sql_stmt VARCHAR;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.proname = 'rif40_startup' THEN
                PERFORM rif40_sql_pkg.rif40_startup();
        ELSE
                RAISE INFO 'RIF startup: not a RIF database';
        END IF;
--
-- Set a default path, schema to user
--
        IF current_user = 'rif40' THEN
                sql_stmt:='SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, ri
f40_partitions';
        ELSE
                sql_stmt:='SET search_path TO '||USER||',rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_
studies, rif40_partitions';
        END IF;
        RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
        EXECUTE sql_stmt;
END;
$$;
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  RIF startup: not a RIF database
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  SQL> SET search_path TO postgres,rif40, public, topology, gis, pop, rif_d
ata, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
BEGIN
DO
SET
SET
psql:db_create.sql:129: INFO:  db_create.sql() test new database name="trumpton" is OK
DO
Creating trumpton database if required
SET
SET
psql:db_create.sql:229: INFO:  db_create.sql() User check: postgres
psql:db_create.sql:229: INFO:  db_create.sql() rif40 password="<NULL>"
psql:db_create.sql:229: INFO:  db_create.sql() rif40 needs to be created encrypted password will be ="<NULL>"
psql:db_create.sql:229: INFO:  SQL> SET rif40.encrypted_rif40_password TO 'md515c55111bdb93bb7543f2480c7384531';
psql:db_create.sql:229: INFO:  SQL> SET rif40.rif40_password TO 'rif4010669_20906';
psql:db_create.sql:229: INFO:  db_create.sql() postgres password="Summ5r1609"
psql:db_create.sql:229: INFO:  db_create.sql() postgres encrypted password="md588c29e12c569ac444b7381647f09e2e1"
psql:db_create.sql:229: INFO:  SQL> SET rif40.encrypted_postgres_password TO 'md588c29e12c569ac444b7381647f09e2e1';
DO
psql:db_create.sql:283: INFO:  db_create.sql() RIF required Postgres version 9.3 or higher OK; current version: PostgreSQL 9.6.8, co
mpiled by Visual C++ build 1800, 64-bit
DO
psql:db_create.sql:315: INFO:  db_create.sql() RIF required extension: adminpack V1.0 is installable
psql:db_create.sql:315: INFO:  db_create.sql() RIF required extension: postgis V2.3.7 is installable
psql:db_create.sql:315: INFO:  db_create.sql() RIF required extension: sslinfo V1.2 is installable
DO
Create users and roles
psql:db_create.sql:526: NOTICE:  db_create.sql() postgres user password is unchanged
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema role rif_user exists
psql:db_create.sql:526: INFO:  db_create.sql() Role rif_user is not a superuser
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema role rif_manager exists
psql:db_create.sql:526: INFO:  db_create.sql() Role rif_manager is not a superuser
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema role rif_no_suppression exists
psql:db_create.sql:526: INFO:  db_create.sql() Role rif_no_suppression is not a superuser
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema role rifupg34 exists
psql:db_create.sql:526: INFO:  db_create.sql() Role rifupg34 is not a superuser
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema role rif_student exists
psql:db_create.sql:526: INFO:  db_create.sql() Role rif_student is not a superuser
psql:db_create.sql:526: INFO:  SQL> CREATE ROLE rif40 NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION ENCRYPTED PASS
WORD 'md515c55111bdb93bb7543f2480c7384531';
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: gis
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user gis exists; password is username
psql:db_create.sql:526: INFO:  SQL> ALTER USER gis PASSWORD  'gis';
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: pop
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user pop exists; password is username
psql:db_create.sql:526: INFO:  SQL> ALTER USER pop PASSWORD  'pop';
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: notarifuser
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user notarifuser exists; password is username
psql:db_create.sql:526: INFO:  SQL> ALTER USER notarifuser PASSWORD  'notarifuser';
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: test_rif_no_suppression
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user test_rif_no_suppression exists; password is username
psql:db_create.sql:526: INFO:  SQL> ALTER USER test_rif_no_suppression PASSWORD  'test_rif_no_suppression';
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: test_rif_user
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user test_rif_user exists; password is username
psql:db_create.sql:526: INFO:  SQL> ALTER USER test_rif_user PASSWORD  'test_rif_user';
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: test_rif_manager
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user test_rif_manager exists; password is username
psql:db_create.sql:526: INFO:  SQL> ALTER USER test_rif_manager PASSWORD  'test_rif_manager';
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: test_rif_student
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user test_rif_student exists; password is username
psql:db_create.sql:526: INFO:  SQL> ALTER USER test_rif_student PASSWORD  'test_rif_student';
psql:db_create.sql:526: INFO:  SQL> REVOKE CREATE ON SCHEMA public FROM PUBLIC;
DO
SET
SET
SET
SET
psql:db_create.sql:660: INFO:  db_create.sql() test user account parameter="pch/pch" OK
psql:db_create.sql:660: INFO:  db_create.sql() test user password parameter="pch_13958_373"
psql:db_create.sql:660: NOTICE:  db_create.sql() C209xx: User account does not exist: pch; creating
psql:db_create.sql:660: INFO:  SQL> CREATE ROLE pch NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN NOREPLICATION PASSWORD 'pch_13
958_373';
psql:db_create.sql:660: INFO:  SQL> GRANT CONNECT ON DATABASE postgres to pch;
psql:db_create.sql:660: INFO:  SQL> GRANT rif_manager TO pch;
psql:db_create.sql:660: INFO:  SQL> GRANT rif_user TO pch;
psql:db_create.sql:660: INFO:  SQL> GRANT CONNECT ON DATABASE postgres to rif40;
DO
COMMIT
SET
CREATE VIEW
Tuples only is on.
*****************************************************************************************************
* Try to connect as test user. This will fail if pgpass is not setup correctly
* Stored password file is in ~/.pgpass on Linux/Macos or: %APPDATA%/postgresql/pgpass.conf on Windows
* Format is: hostname:port:database:username:password
* An example is supplied in pgpass.conf in the current directory
*****************************************************************************************************
 localhost:5432:*:postgres:summ5r1609
 localhost:5432:*:pch:pch_13958_373
 localhost:5432:*:rif40:rif4010669_20906

Tuples only is off.
COPY 3
DROP VIEW
You are now connected to database "postgres" as user "pch".
Connection as pch to postgres OK!
           connected
-------------------------------
 Connected to: postgres AS pch
(1 row)

Try to connect as rif40. This will fail if if the password file is not setup correctly
You are now connected to database "postgres" as user "rif40".
Connection as rif40 to postgres OK!
            connected
---------------------------------
 Connected to: postgres AS rif40
(1 row)

You are now connected to database "postgres" as user "postgres".
************************************************************************************
*
* WARNING !!!
*
* This script will drop trumpton, re-create it in () or DEFAULT
*
************************************************************************************
SET
SET
SET
psql:db_create.sql:763: NOTICE:  db_create.sql() No -v tablespace_dir=<tablespace_dir> parameter
psql:db_create.sql:763: INFO:  SQL> SET rif40.tablespace_dir TO '';
DO
        param         |   value
----------------------+------------
 rif40.tablespace_dir |
 rif40.newdb          | trumpton
 rif40.os             | windows_nt
(3 rows)

CREATE VIEW
                                                     sql_stmt
------------------------------------------------------------------------------------------------------------------
 --
 -- ************************************************************************
 --
 -- Description:
 --
 -- Rapid Enquiry Facility (RIF) - Database creation autogenerated temp script to create tablespace and databases
 --
 DROP DATABASE IF EXISTS trumpton;
 CREATE DATABASE trumpton WITH OWNER rif40 /* No trumpton tablespace */;
 COMMENT ON DATABASE trumpton IS 'RIF V4.0 PostGres trumpton Example Database';
 --
 -- Eof
(12 rows)

COPY 12
DROP VIEW
\set ON_ERROR_STOP on
\i recreate_sahsu_dev_tmp.sql
--
-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Database creation autogenerated temp script to create tablespace and databases
--
DROP DATABASE IF EXISTS trumpton;
psql:recreate_sahsu_dev_tmp.sql:8: NOTICE:  database "trumpton" does not exist, skipping
DROP DATABASE
CREATE DATABASE trumpton WITH OWNER rif40 /* No trumpton tablespace */;
CREATE DATABASE
COMMENT ON DATABASE trumpton IS 'RIF V4.0 PostGres trumpton Example Database';
COMMENT
--
-- Eof

\c :newdb postgres :pghost
You are now connected to database "trumpton" as user "postgres".

--
-- Start transaction 2: :newdb build
--
BEGIN;
BEGIN
--
-- Check user is postgres on :newdb
--
\set ECHO none
SET
SET
psql:db_create.sql:915: INFO:  db_create.sql() User check: postgres
psql:db_create.sql:915: INFO:  SQL> CREATE EXTENSION IF NOT EXISTS postgis;
psql:db_create.sql:915: INFO:  SQL> CREATE EXTENSION IF NOT EXISTS adminpack;
psql:db_create.sql:915: INFO:  SQL> CREATE EXTENSION IF NOT EXISTS dblink;
psql:db_create.sql:915: INFO:  SQL> GRANT ALL ON DATABASE trumpton to rif40;
psql:db_create.sql:915: INFO:  SQL> REVOKE CREATE ON SCHEMA public FROM rif40;
psql:db_create.sql:915: INFO:  SQL> GRANT CONNECT ON DATABASE trumpton to pch;
psql:db_create.sql:915: INFO:  SQL> CREATE SCHEMA IF NOT EXISTS pch AUTHORIZATION pch;
psql:db_create.sql:915: INFO:  SQL> ALTER DATABASE trumpton SET search_path TO rif40, public, topology, gis, pop, rif_data, data_loa
d, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
COMMIT
ALTER ROLE
SET
                                             search_path
------------------------------------------------------------------------------------------------------
 rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
(1 row)

*****************************************************************************************************
* Try to connect as test user and rif40
*****************************************************************************************************
You are now connected to database "trumpton" as user "pch".
Connection as pch to trumpton OK!
           connected
-------------------------------
 Connected to: trumpton AS pch
(1 row)

Try to connect as rif40. This will fail if if the password file is not setup correctly
You are now connected to database "trumpton" as user "rif40".
Connection as rif40 to trumpton OK!
            connected
---------------------------------
 Connected to: trumpton AS rif40
(1 row)

Command psql ran OK.
db_create.sql built trumpton OK
Log: pg_restore.rpt
Working directory: C:\Users\support\Downloads\OneDrive-2018-04-18\Postgres
Command: pg_restore
Arguments: -d trumpton -U postgres sahsuland.dump
pg_restore: WARNING:  database "sahsuland" does not exist
Command pg_restore ran OK.
pg_restore/psql restored trumpton OK
```

## 2.1 Changing the postgres database administrator password

Check you can logon using psql as the database adminstrator account; *postgres*.
```
psql -d postgres -U postgres
You are connected to database "postgres" as user "postgres" on host "wpea-rif1" at port "5432".
psql (9.3.5)
Type "help" for help.

postgres=#
```

It is possible to login to postgres as postgres without a password using the administrator or 
root accounts. If you lock yourself out the *hba.conf* file will need the following line temporary 
added at the top of the file:

```
local  all   all   trust
```
or if local connections are not permitted by the Postgres build:
```
host  all   all  127.0.0.1/32  trust
```
**Remove these lines** after you have changed the password using:
```
psql -U postgres -d postgres
ALTER USER postgres PASSWORD <new password>;
```
