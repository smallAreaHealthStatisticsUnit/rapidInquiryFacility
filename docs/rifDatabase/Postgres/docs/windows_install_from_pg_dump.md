---
layout: default
title: Windows Postgres Install using pg_dump and scripts
---

* Contents
{:toc}

# Installation Prerequisites
## Postgres

* Uses *pg_dump* and Powershell
* Does **NOT** need *make* or *Node.js*

Postgres is best downloaded from Enterprise DB: (http://www.enterprisedb.com/products-services-training/pgdownload).
This is standard PostGres as packaged by Enterprise DB and not their own Postgres based product EDB Postgres
Enterprise/Standard/Developer. An installation guide is at:
(http://get.enterprisedb.com/docs/PostgreSQL_Installation_Guide_v9.6.pdf)

WARNING: The RIF requires Postgres 9.3 or above to work. 9.1 and 9.2 will not work. It has *NOT* yet been tested on Postgres 10.

Postgres is usually setup in one of four ways:

1. Standalone mode on a Windows firewalled laptop. This uses local database MD5 passwords and no SSL and is not considered secure for network use.
2. Secure mode on a Windows server. This uses remote database connections using SSL; with MD5 passwords for psql and Java connectivity.
3. Secure mode on a Windows server and Active directory network. This uses remote database connections using SSL; with SSPI (Windows GSS
   connectivity) for psql and secure LDAP for Java connectivity.
4. Secure mode on a Linux server and Active directory network. This uses remote database connections using SSL; with GSSAPI/Kerberos for
   psql and secure LDAP for Java connectivity.

The front and and middleware require username and password authentications; so method 4 must not be used.

Postgres also can proxy users (see *ident.conf* examples are in [Configuration File Examples](#configuration-file-examples)

Typically this is used to allow remote postgres administrator user authentication and to logon as the schema owner (rif40).

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
[Changing the postgres database administrator password](#changing-the-postgres-database-administrator-password)

  ```
  C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>psql -U postgres -d postgres
  psql (9.5.0)
  Type "help" for help.

  postgres=#
  ```
  * If you get a code page error on Windows see 1.1.2 below.

### Memory Requirements

Approximately:

* Windows 10 seems to need 6-8GB to function in normal use;
* The database needs 1-2 GB (SQL server will automatically; Postgres will run in less);
* R needs 1-2 GB (depends on study size; may need more);
* The Middleware 2-3GB;
* The front end 1-2GB.

16GB recommended. If you process large geographies using the Node.js tilemaker 48-64GB is recommended.

### Postgres User Setup

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

### Fixing Windows Code page errors

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

# Installing a production database

Run the database installer batch script as an administrator: rif40_database_install.bat.

The script will ask the user for:

* The production RIF user name (default: *peter*)
* The database name (default: *sahsuland*)

**ONLY USE LOWERCASE LETTERS, UNDERSCORE AND THE DIGITS 0-9; START WITH A LTTER**

The script will create the user *pgpass.conf* (in *C:\Users\%USERNAME%\AppData\Roaming\postgresql*) and the
create the *psqlrc* psql logon script. If required it will:

* Ask the user for the *postgres* databaase administrator password (set as part of the database install)
* Set RIF40 user password (default: *rif40_&lt;randon digits&gt;_&lt;randon digits&gt;*)
* The production RIF user password (default: *&lt;username&gt;_&lt;randon digits&gt;_&lt;randon digits&gt;*)

The script runs *db_create.sql* to create the database and users and then restores the database from the
supplied *sahsuland.dump* using *pg_restore*.

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
# Postgres password       XXXXXXXXXX
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
sity=terse -v debug_level=1 -v echo=all -v postgres_password=XXXXXXXXXX -v rif40_password=rif4010669_20906 -v tablespace_dir= -v pgh
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
psql:db_create.sql:229: INFO:  db_create.sql() postgres password="XXXXXXXXXX"
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
 localhost:5432:*:postgres:XXXXXXXXXX
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

## Changing the postgres database administrator password

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
# Configuration File Examples

If you are running locally only (e.g. on a laptop) you do *NOT* need to edit the configuration files.

## Postgres user password file

Postgres user password files are located in:

* Windows *%APPDATA%\Roaming\postgresql\pgpass.conf*, e.g. *C:\Users\pch\AppData\Roaming\postgresql\pgpass.conf*
* Linux/MacOS: *~/.pgpass*

One line per host, database and account. Fields separated by ":". Order is:

* Host
* Port
* Database (usually *)
* User
* Password
```
localhost:5432:*:postgres:XXXXXXX
localhost:5432:*:peterh: XXXXXXX
wpea-pch:5432:*:peterh: XXXXXXX
wpea-rif1:5432:*:postgres: XXXXXXX
wpea-rif1:5432:*:pch: XXXXXXX
```

# Authentication Setup (hba.conf)

You **MUST** read the Postgres manuals before editing this file.

Fields separated by TAB.

* TYPE: Connection type:
  * local: UDP
  * host: TCP/IP
  * hostssl: TCP/IP with TLS
* DATABASE: Database name
* USER: Username
* ADDRESS: Host/address mask
* METHOD:
  * Non mappable:
	  * trust: Allow with authentication (useful if you have locked yourself out by chnaginbg hte password!
	  * reject: disallow
	  * md5: MD5 password authentication
	  * password: plain text password (**INSECURE: DO NOT USE**)
	  * krb5: Kerberos 5 (**OBSOLETED: use GSSAPI**)
	  * ldap: directory services
	  * radius: RADIUS authentication

  * Mappable using ident.conf (i.e. support proxying):
	  * ident: Identification Protocol as described in RFC 1413 (**INSECURE: DO NOT USE**)
	  * peer: Peer authentication is only available on operating systems providing the getpeereid() function,
			  the SO_PEERCRED socket parameter, or similar mechanisms. Currently that includes Linux, most flavors
			  of BSD including OS X, and Solaris.
	  * gss: GSSAPI/Kerberos
	  * pam: Linux PAM (Pluggable authentication modules)
	  * sspi: Windows native autentiation (NTLM V2)
	  * cert: Uses SSL client certificates to perform authentication. It is therefore only available for SSL connections.

```
# PostgreSQL Client Authentication Configuration File
# ===================================================
#
# Refer to the "Client Authentication" section in the PostgreSQL
# documentation for a complete description of this file.  A short
# synopsis follows.
#
# This file controls: which hosts are allowed to connect, how clients
# are authenticated, which PostgreSQL user names they can use, which
# databases they can access.  Records take one of these forms:
#
# local      DATABASE  USER  METHOD  [OPTIONS]
# host       DATABASE  USER  ADDRESS  METHOD  [OPTIONS]
# hostssl    DATABASE  USER  ADDRESS  METHOD  [OPTIONS]
# hostnossl  DATABASE  USER  ADDRESS  METHOD  [OPTIONS]
#
# (The uppercase items must be replaced by actual values.)
#
# The first field is the connection type: "local" is a Unix-domain
# socket, "host" is either a plain or SSL-encrypted TCP/IP socket,
# "hostssl" is an SSL-encrypted TCP/IP socket, and "hostnossl" is a
# plain TCP/IP socket.
#
# DATABASE can be "all", "sameuser", "samerole", "replication", a
# database name, or a comma-separated list thereof. The "all"
# keyword does not match "replication". Access to replication
# must be enabled in a separate record (see example below).
#
# USER can be "all", a user name, a group name prefixed with "+", or a
# comma-separated list thereof.  In both the DATABASE and USER fields
# you can also write a file name prefixed with "@" to include names
# from a separate file.
#
# ADDRESS specifies the set of hosts the record matches.  It can be a
# host name, or it is made up of an IP address and a CIDR mask that is
# an integer (between 0 and 32 (IPv4) or 128 (IPv6) inclusive) that
# specifies the number of significant bits in the mask.  A host name
# that starts with a dot (.) matches a suffix of the actual host name.
# Alternatively, you can write an IP address and netmask in separate
# columns to specify the set of hosts.  Instead of a CIDR-address, you
# can write "samehost" to match any of the server's own IP addresses,
# or "samenet" to match any address in any subnet that the server is
# directly connected to.
#
# METHOD can be "trust", "reject", "md5", "password", "gss", "sspi",
# "krb5", "ident", "peer", "pam", "ldap", "radius" or "cert".  Note that
# "password" sends passwords in clear text; "md5" is preferred since
# it sends encrypted passwords.
#
# OPTIONS are a set of options for the authentication in the format
# NAME=VALUE.  The available options depend on the different
# authentication methods -- refer to the "Client Authentication"
# section in the documentation for a list of which options are
# available for which authentication methods.
#
# Database and user names containing spaces, commas, quotes and other
# special characters must be quoted.  Quoting one of the keywords
# "all", "sameuser", "samerole" or "replication" makes the name lose
# its special character, and just match a database or username with
# that name.
#
# This file is read on server startup and when the postmaster receives
# a SIGHUP signal.  If you edit the file on a running system, you have
# to SIGHUP the postmaster for the changes to take effect.  You can
# use "pg_ctl reload" to do that.

# Put your actual configuration here
# ----------------------------------
#
# If you want to allow non-local connections, you need to add more
# "host" records.  In that case you will also need to make PostgreSQL
# listen on a non-local interface via the listen_addresses
# configuration parameter, or via the -i or -h command line switches.

# TYPE  DATABASE        USER            ADDRESS                 METHOD

# IPv4, IPv6 local connections:
#
host    all             postgres	127.0.0.1/32            md5
host    all             postgres      ::1/128                 md5
hostssl all		 postgres     146.179.138.xxx 	  255.255.255.255    md5
#
# Allow local connections as schema owner (usually use a proxy)
#
#hostssl    sahsuland	   pop             127.0.0.1/32            md5
#hostssl    sahsuland	   pop             ::1/128                 md5
#hostssl    sahsuland	   gis             127.0.0.1/32            md5
#hostssl    sahsuland	   gis             ::1/128                 md5
#hostssl    sahsuland	   rif40           127.0.0.1/32            md5
#hostssl    sahsuland	   rif40           ::1/128                 md5
#
# Active directory GSSAPI connections with pg_ident.conf maps for schema accounts
#
hostssl	sahsuland	all	 	127.0.0.1/32 		sspi 	map=sahsuland
hostssl	sahsuland	all	 	::1/128 		sspi 	map=sahsuland
hostssl	sahsuland_dev	all	 	127.0.0.1/32 		sspi 	map=sahsuland_dev
hostssl	sahsuland_dev	all	 	::1/128 		sspi 	map=sahsuland_dev
#
# Allow remote access from specified IP addresses by:
#
# a) SSPI (Windows native GSS [Kerberos] machanism
#
hostssl	sahsuland	all	 	146.179.138. xxx	255.255.255.255	sspi 	map=sahsuland
hostssl	sahsuland_dev	all	 	146.179.138. xxx	255.255.255.255	sspi 	map=sahsuland_dev
#
# b) LDAP (to be fixed – need to use different server
#
# hostssl	sahsuland_dev	all	 	146.179.138.157 	255.255.255.255 ldap ldapurl="ldaps:// xxx.ic.ac.uk/basedn;cn=;,o=Imperial College,c=GB"
#
# No LDAP URLs or username map on Windows
#
# 2014-03-12 13:44:24 GMT LOG: 00000: LDAP login failed for user "cn=pch,o=Imperial College,c=GB" on server " xxx.ic.ac.uk": Invalid Credentials 2014-03-12 13:44:24 GMT LOCATION: CheckLDAPAuth, src\backend\libpq\auth.c:2321
#
#host	sahsuland_dev	all	 	146.179.138.157 	255.255.255.255 ldap ldapserver= xxx.ic.ac.uk ldapprefix="uid=" ldapsuffix=",ou=phs,o=Imperial College,c=GB"
#
# 2014-03-12 13:50:33 GMT LOG: 00000: LDAP login failed for user "pch@IC.AC.UK" on server " xxx.ic.ac.uk": Invalid DN Syntax 2014-03-12 13:50:33 GMT LOCATION: CheckLDAPAuth, src\backend\libpq\auth.c:2321
#
#host	sahsuland_dev	all	 	146.179.138.157 	255.255.255.255 ldap ldapserver= xxx.ic.ac.uk ldapprefix= ldapsuffix="@IC.AC.UK"
#host	sahsuland_dev	all	 	146.179.138.157 	255.255.255.255 ldap ldapserver= xxx.ic.ac.uk ldapprefix= ldapsuffix=",o=Imperial College,c=GB"
#
# Other databases
#
hostssl	traffic		all	 	127.0.0.1/32 		sspi
hostssl	traffic		all	 	::1/128 		sspi
hostssl	traffic		all	 	146.179.138. xxx	255.255.255.255	sspi
#
#host    all             all             127.0.0.1/32            md5
#host    all             all             ::1/128                 md5
# Allow replication connections from localhost, by a user with the
# replication privilege.
#host    replication     postgres        127.0.0.1/32            md5
#host    replication     postgres        ::1/128                 md5
```

# Proxy User Setup (ident.conf)

You **MUST** read the Postgres manuals before editing this file.

One line per per system user and map, fields separated by TAB in the order:

* MAPNAME: map name in hba.conf (not all authentication types can proxy - e.g. md5 cannot!)
* SYSTEM-USERNAME: account name to proxy for
* PG-USERNAME: account name to authenticate as

```
# PostgreSQL User Name Maps
# =========================
#
# Refer to the PostgreSQL documentation, chapter "Client
# Authentication" for a complete description.  A short synopsis
# follows.
#
# This file controls PostgreSQL user name mapping.  It maps external
# user names to their corresponding PostgreSQL user names.  Records
# are of the form:
#
# MAPNAME  SYSTEM-USERNAME  PG-USERNAME
#
# (The uppercase quantities must be replaced by actual values.)
#
# MAPNAME is the (otherwise freely chosen) map name that was used in
# pg_hba.conf.  SYSTEM-USERNAME is the detected user name of the
# client.  PG-USERNAME is the requested PostgreSQL user name.  The
# existence of a record specifies that SYSTEM-USERNAME may connect as
# PG-USERNAME.
#
# If SYSTEM-USERNAME starts with a slash (/), it will be treated as a
# regular expression.  Optionally this can contain a capture (a
# parenthesized subexpression).  The substring matching the capture
# will be substituted for \1 (backslash-one) if present in
# PG-USERNAME.
#
# Multiple maps may be specified in this file and used by pg_hba.conf.
#
# No map names are defined in the default configuration.  If all
# system user names and PostgreSQL user names are the same, you don't
# need anything in this file.
#
# This file is read on server startup and when the postmaster receives
# a SIGHUP signal.  If you edit the file on a running system, you have
# to SIGHUP the postmaster for the changes to take effect.  You can
# use "pg_ctl reload" to do that.

# Put your actual configuration here
# ----------------------------------

# MAPNAME       SYSTEM-USERNAME         PG-USERNAME
#
sahsuland	pch			pop
sahsuland	pch			gis
sahsuland	pch			rif40
sahsuland	pch			pch
#
sahsuland_dev	pch			pop
sahsuland_dev	pch			gis
sahsuland_dev	pch			rif40
sahsuland_dev	pch			pch
sahsuland_dev	pch			postgres
#
# Eof
```


