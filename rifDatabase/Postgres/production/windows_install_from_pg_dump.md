
Windows Postgres Install using pg_dump and scripts
==================================================

# Contents

- [1. Installation Prerequistes](#1-installation-prerequistes)
   - [1.1 Postgres](#11-postgres)	
      - [1.1.1 Postgres User Setup](#111-postgres-user-setup)
      - [1.1.2 Fixing Windows Code page errors](#112-fixing-windows-code-page-errors)
- [2. Installing a production database](#2-installing=a-production-database)

# 1. Installation Prerequistes
## 1.1 Postgres
 
* Uses *pg_dump* and Powershell
* Does **NOT** need *make* or *Node.js*

Postgres is best downloaded from Enterprise DB: http://www.enterprisedb.com/products-services-training/pgdownload. 
This is standard PostGres as packaged by Enterprise DB and not their own Postgres based product EDB Postgres
Enterprise/Standard/Developer. An installation guide is at: 
http://get.enterprisedb.com/docs/PostgreSQL_Installation_Guide_v9.6.pdf

The Postgres installer then runs stack builder to download and install the additional packages. The following additional packages need to be installed:

* PostGres (database, PG Admin III administration tool, and common extensions)
* PostGIS (Geospatial integration)
* pgODBC (ODBC database connector for PostGres)

Once the install is complete:

* Add the Postgres bin directory (e.g. **) to the path
* The following should normally be set in your environment:

  * PGDATABASE - sahusland
  * PGHOST - localhost

* If you get a code page error om Windows see 1.1.2 below.

### 1.1.1 Postgres User Setup

The following users are created by the install script (*rif40_database_install.bat*):

* postgres     - The database administrator. You will have set this password when you or the installer created 
                 the Postgres cluster.
* rif40        - The schema owner. You choose this password (see ENCRYPTED_RIF40_PASSWORD below)
* &lt;user login&gt; - Your user login. This must be in lowercase and without spaces. 

The install script (*rif40_database_install.bat*) will create your local .pgpass/pgpass.conf files 
(see Postgres documentation for its location on various OS). 

E.g. C:\Users\pch\AppData\Roaming\postgresql\pgpass.conf:
```
wpea-rif1:5432:*:postgres:<password>
wpea-rif1:5432:*:pch:<password>
wpea-rif1:5432:*:rif40:<password>
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
* Or: modify the cmd shortcut to run *cmd.exe /k chcp 1252*

2. Installing a production database

Run the datbase installer batch script: rif40_database_install.bat
```
C:\rifDemo\Postgres\production>rif40_database_install.bat
Creating production RIF Postgres database
PG_SYSCONFDIR=C:/PROGRA~1/POSTGR~1/9.6/etc
Administrator PRIVILEGES Detected!
'ELSE' is not recognized as an internal or external command,
operable program or batch file.
##########################################################################################
#
# WARNING! this script will the drop and create the RIF40 sahsuland Postgres database.
# Type control-C to abort.
#
# Test user: peter; password: <peter pw>
# Postgres password:       <PGPASSWORD>
# Schema (rif40) password: <rif40 pw>
# PG password directory:
# PG sysconfig directory:  C:/PROGRA~1/POSTGR~1/9.6/etc
#
##########################################################################################
Press any key to continue . . .
Log: db_create.rpt
Working directory: C:\rifDemo\Postgres\production
Command: psql
Arguments: -U postgres -d postgres -h localhost -w -e -P pager=off -v testuser=peter -v newdb=sahsuland -v newpw=<peter pw> -v verbosity=terse -v debug_level=1 -v echo=all -v postgres_password=<PGPASSWORD> -v rif40_password=<rif40 pw> -v tablespace_dir= -v pghost=localhost -v os=Windows_NT -f db_create.sql
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
                sql_stmt:='SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
        ELSE
                sql_stmt:='SET search_path TO '||USER||',rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
        END IF;
        RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
        EXECUTE sql_stmt;
END;
$$;
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  RIF startup: not a RIF database
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  SQL> SET search_path TO postgres,rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
BEGIN
DO
SET
SET
psql:db_create.sql:129: INFO:  db_create.sql() test new database name="sahsuland" is OK
DO
Creating sahsuland database if required
SET
SET
psql:db_create.sql:229: INFO:  db_create.sql() User check: postgres
psql:db_create.sql:229: INFO:  db_create.sql() rif40 password="<rif40 pw>"
psql:db_create.sql:229: INFO:  db_create.sql() rif40 encrypted password="md5971757ca86c61e2d8f618fe7ab7a32a1"
psql:db_create.sql:229: INFO:  SQL> SET rif40.encrypted_rif40_password TO 'md5971757ca86c61e2d8f618fe7ab7a32a1';
psql:db_create.sql:229: INFO:  SQL> SET rif40.rif40_password TO '<rif40 pw>';
psql:db_create.sql:229: INFO:  db_create.sql() postgres password="<PGPASSWORD>"
psql:db_create.sql:229: INFO:  db_create.sql() postgres encrypted password="md51d8b49337a87cdc91f4946d42c5c72e8"
psql:db_create.sql:229: INFO:  SQL> SET rif40.encrypted_postgres_password TO 'md51d8b49337a87cdc91f4946d42c5c72e8';
DO
psql:db_create.sql:283: INFO:  db_create.sql() RIF required Postgres version 9.3 or higher OK; current version: PostgreSQL 9.6.3, compiled by Visual C++ build 1800, 64-bit
DO
psql:db_create.sql:315: INFO:  db_create.sql() RIF required extension: adminpack V1.0 is installable
psql:db_create.sql:315: INFO:  db_create.sql() RIF required extension: postgis V2.3.3 is installable
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
psql:db_create.sql:526: INFO:  db_create.sql() privilege check OK for: rif40
psql:db_create.sql:526: INFO:  db_create.sql() RIF schema user rif40 exists; changing password to encrypted
psql:db_create.sql:526: INFO:  SQL> ALTER USER rif40 ENCRYPTED PASSWORD  'md5971757ca86c61e2d8f618fe7ab7a32a1';
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
psql:db_create.sql:660: INFO:  db_create.sql() test user account parameter="peter/<peter pw>" OK
psql:db_create.sql:660: INFO:  db_create.sql() test user password parameter="peter"
psql:db_create.sql:660: INFO:  db_create.sql() user account="peter" is a rif_user
psql:db_create.sql:660: INFO:  SQL> GRANT CONNECT ON DATABASE postgres to peter;
psql:db_create.sql:660: INFO:  SQL> GRANT rif_manager TO peter;
psql:db_create.sql:660: INFO:  SQL> GRANT rif_user TO peter;
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
 localhost:5432:*:peter:<peter pw>
 localhost:5432:*:postgres:<PGPASSWORD>
 localhost:5432:*:rif40:<rif40 pw>

Tuples only is off.
COPY 3
DROP VIEW
WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
You are now connected to database "postgres" as user "peter".
Connection as peter to postgres OK!
            connected
---------------------------------
 Connected to: postgres AS peter
(1 row)

Try to connect as rif40. This will fail if if the password file is not setup correctly
WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
You are now connected to database "postgres" as user "rif40".
Connection as rif40 to postgres OK!
            connected
---------------------------------
 Connected to: postgres AS rif40
(1 row)

WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
You are now connected to database "postgres" as user "postgres".
************************************************************************************
*
* WARNING !!!
*
* This script will drop sahsuland, re-create it in () or DEFAULT
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
 rif40.newdb          | sahsuland
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
 DROP DATABASE IF EXISTS sahsuland;
 CREATE DATABASE sahsuland WITH OWNER rif40 /* No sahsuland tablespace */;
 COMMENT ON DATABASE sahsuland IS 'RIF V4.0 PostGres sahsuland Example Database';
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
DROP DATABASE IF EXISTS sahsuland;
DROP DATABASE
CREATE DATABASE sahsuland WITH OWNER rif40 /* No sahsuland tablespace */;
CREATE DATABASE
COMMENT ON DATABASE sahsuland IS 'RIF V4.0 PostGres sahsuland Example Database';
COMMENT
--
-- Eof

\c :newdb postgres :pghost
WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
You are now connected to database "sahsuland" as user "postgres".

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
psql:db_create.sql:911: INFO:  db_create.sql() User check: postgres
psql:db_create.sql:911: INFO:  SQL> CREATE EXTENSION IF NOT EXISTS postgis;
psql:db_create.sql:911: INFO:  SQL> CREATE EXTENSION IF NOT EXISTS adminpack;
psql:db_create.sql:911: INFO:  SQL> CREATE EXTENSION IF NOT EXISTS dblink;
psql:db_create.sql:911: INFO:  SQL> GRANT ALL ON DATABASE sahsuland to rif40;
psql:db_create.sql:911: INFO:  SQL> REVOKE CREATE ON SCHEMA public FROM rif40;
psql:db_create.sql:911: INFO:  SQL> GRANT CONNECT ON DATABASE sahsuland to peter;
psql:db_create.sql:911: INFO:  SQL> ALTER DATABASE sahsuland SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
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
WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
You are now connected to database "sahsuland" as user "peter".
Connection as peter to sahsuland OK!
            connected
----------------------------------
 Connected to: sahsuland AS peter
(1 row)

Try to connect as rif40. This will fail if if the password file is not setup correctly
WARNING: Console code page (850) differs from Windows code page (1252)
         8-bit characters might not work correctly. See psql reference
         page "Notes for Windows users" for details.
You are now connected to database "sahsuland" as user "rif40".
Connection as rif40 to sahsuland OK!
            connected
----------------------------------
 Connected to: sahsuland AS rif40
(1 row)

Command psql ran OK.
db_create.sql built sahsuland OK
'EXIST' is not recognized as an internal or external command,
operable program or batch file.
Copying generated pgpass.conf to
The file cannot be copied onto itself.
        0 file(s) copied.
Log: pg_restore.rpt
Working directory: C:\rifDemo\Postgres\production
Command: pg_restore
Arguments: -d sahsuland -U postgres sahsuland_dev.dump
pg_restore: WARNING:  database "sahsuland_dev" does not exist
pg_restore: [archiver (db)] Error while PROCESSING TOC:
pg_restore: [archiver (db)] Error from TOC entry 9; 2615 15640269 SCHEMA hugh hugh
pg_restore: [archiver (db)] could not execute query: ERROR:  role "hugh" does not exist
    Command was: ALTER SCHEMA hugh OWNER TO hugh;


pg_restore: [archiver (db)] Error from TOC entry 4846; 0 0 ACL hugh hugh
pg_restore: [archiver (db)] could not execute query: ERROR:  role "hugh" does not exist
    Command was: REVOKE ALL ON SCHEMA hugh FROM PUBLIC;
REVOKE ALL ON SCHEMA hugh FROM hugh;
GRANT ALL ON SCHEMA hugh TO hugh;



WARNING: errors ignored on restore: 2
Error in command execution
pg_restore exiting with error code: 1

C:\rifDemo\Postgres\production>
```



Once you have setup the *pgpass* file, check you can logon using psql as the database adminstrator account; *postgres*.
```
psql -d postgres -U postgres
You are connected to database "postgres" as user "postgres" on host "wpea-rif1" at port "5432".
psql (9.3.5)
Type "help" for help.

postgres=#
```

**IT IS STRONGLY ADVISED TO LEAVE THIS WINDOW OPEN SO YOU CANNOT LOCK YOURSELF OUT OF THE DATABASE IF YOU SET IT UP WRONG**. This scripts 
do check the setup is correct; but this could fail. It is possible to login to postgres as postgres without a password using the administrator or 
root accounts. If you lock yourself out the hba.conf file will need the following line temporary added at the top of the file:

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
