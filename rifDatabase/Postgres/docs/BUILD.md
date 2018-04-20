RIF40 Postgres database build from Github
=========================================

# Contents
- [1. Postgres Requirements](#1-postgres-requirements)
  - [1.1 Memory Requirements](#11-memory-requirements)
- [2. Postgres Setup](#2-postgres-setup)
  - [2.1 Database Development Environment](#21-database-development-environment)
  - [2.2 Tool Chain](#22-tool-chain)
    - [2.2.1 Windows](#221-windows)
    - [2.2.1.1 Fixing Windows Code page errors](#2211-fixing-windows-code-page-errors)
    - [2.2.2 Unixen](#222-unixen)
  - [2.3 Building the Database](#23-building-the-database)
    - [2.3.1 Pre build Tests](#231-pre-build-tests)
      - [2.3.1.1 Configuring make](#2311-configuring-make)
    - [2.3.2 Build control using make](#232-build-control-using-make)
      - [2.3.2.1 Setup](#2321-setup)
        - [2.3.2.1.1 User setup](#23211-user-setup)
        - [2.2.2.1.2 Makefile.local settings](#23212-makefilelocal-settings)
        - [2.3.2.1.3 Principal build target](#23213-principal-build-target)
        - [2.3.2.1.4 Issues with the build](#23214-issues-with-the-build)
        - [2.3.2.1.5 Other Build targets](#23214-other-build-targets)
      - [2.3.2.2 Porting limitations](#2322-porting-limitations)
      - [2.3.2.3 Help](#2323-help)
      - [2.3.2.4 Configuration File Examples](#2324-configuration-file-examples)
        - [2.3.2.4.1 Postgres user password file](#23241-postgres-user-password-file)
        - [2.3.2.4.2 Authentication Setup (hba.conf)](#23242-authentication-setup-hbaconf)
        - [2.3.2.4.3 Proxy User Setup (ident.conf)](#23243-proxy-user-setup-identconf)

# 1. Postgres Requirements 

The new V4.0 RIF uses either Postgres or Microsoft SQL server as a database backend.

WARNING: The RIF requires Postgres 9.3 or above to work. 9.1 and 9.2 will not work. In particular PL/pgsql GET STACKED DIAGNOSTICS is used which 
is a post 9.2 option. It has *NOT* yet been tested on Postgres 10.

It is possible to install Windows Postgres RIF using pg_dump and scripts. This could also be used for MacOS and Linux with shell scripts instead.

* Uses *pg_dump* and Powershell
* Does **NOT** need *make* or *Node.js* 

See: [Windows Postgres Install using pg_dump and scripts](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/windows_install_from_pg_dump.md)

The database build creates the production database dump file ```sahsuland.dump``` is present in *rapidInquiryFacility\rifDatabase\Postgres\production* used to create production databases.

## 1.1 Memory Requirements
 
Approximately: 

* Windows 10 seems to need 6-8GB to function in normal use; 
* The database needs 1-2 GB (SQL server will automatically; Postgres will run in less); 
* R needs 1-2 GB (depends on study size; may need more); 
* The Middleware 2-3GB;
* The front end 1-2GB.
 
16GB recommended. If you process large geographies using the Node.js tilemaker 48-64GB is recommended.

# 2. Postgres Setup

Postgres is usually setup in one of four ways:
 
* 1 Standalone mode on a Windows firewalled laptop. This uses local database MD5 passwords and no SSL and is not considered secure for network use.
* 2 Secure mode on a Windows server. This uses remote database connections using SSL; with MD5 passwords for psql and Java connectivity.
* 3 Secure mode on a Windows server and Active directory network. This uses remote database connections using SSL; with SSPI (Windows GSS 
    connectivity) for psql and secure LDAP for Java connectivity.
* 4 Secure mode on a Linux server and Active directory network. This uses remote database connections using SSL; with GSSAPI/Kerberos for 
    psql and secure LDAP for Java connectivity.

The front and and middleware require username and password authentications; so method 4 must not be used.
  
Postgres also can proxy users (see ident.conf examples in the bottom of the build notes. 
Typically this is used to allow remote postgres administrator user authentication and to logon as the schema owner (rif40).

## 2.1 Database Development Environment

Current three databases are used: *sahsuland*, *sahsuland_dev* and *sahusland_empty*. The installer database *sahsuland* is kept stable for long periods, 
*sahsuland_dev* is the working database. The development database *sahusland_dev* is built from psql scripts which create and 
populate the database ojects. Then alter scripts not under development and then run and the database tested. The database is then 
exported and then imported into *sahsuland*. The *sahsuland_dev* database can then be enhanced with the alter scripts under development.
*sahusland_empty* is used to test data loading and contains no health data, only the *sahsuland* geography.

The middleware always runs against *sahsuland*.

After the move from the SAHSU private network to the main 
Imperial network the database structure is only modified by alter scripts so that the SAHSU Private network 
and Microsoft SQL Server Ports can be kept up to date. Alter scripts are designed to be run more than once without 
side effects, although they must be run in numeric order.

Once the RIF is in production alter scripts will be setup to only be able to complete successfully once and cannot then 
be re-run.

The directory structure is *rifDatabase\Postgres\*:

* conf            - Exmaples of varaious configuration files
* etc             - Postgres psql logon script (.psqlrc)
* example_data    - Example extracts and results creating as part of SAHSUland build
* logs            - Logs from psql_scripts runs
* PLpgsql         - PL/pgsql scripts
* psql_scripts    - PSQL scripts; **Postgres database build directory**. The 
                    [README](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/README.md) 
					describes the build process in detail.
* sahsuland       - SAHSUland creation psql scripts
* sahsuland\data  - SAHSUland data
* shapefiles      - Postgres psql SQL scripts derived from shapefiles, creation scripts
* shapefiles\data - Shapefiles

The *sahsuland* example data is contatined in *rifDatabase\*:

* DataLoaderData\SAHSULAND - SAHSUland numerator, denominator and covariate data after processing by the data loader loader
* GeospatialData\tileMaker - SAHSUland geospatial data

These duretories are shared with the SQL Serer port.

The databases *sahsuland*, *sahsuland_dev* and *sahusland_empty* are built using make and Node.js. 

## 2.2 Tool Chain 

### 2.2.1 Windows

* Install [MinGW](https://sourceforge.net/projects/mingw/files/latest/download?source=files) with the full development environment and add *c:\MinGW\msys\1.0\bin* to path
* Install [Python 2.7](https://www.python.org/downloads/) **not Python 3.n**
* Install a C/C++ compiler, suitable for use by Node.js. I used Microsoft Visual Studio 2012 
  under an Academic licence. The dependency on Node.js will be reduced by replacing the current 
  program with Node.js web services available for use by the wider RIF community.
* Install 64 bit [Node.js](https://nodejs.org/en/download/). The 32 bit node will run into memory limitation problems will vary large 
  shapefiles. node.js is required to build to geoJSON to topoJSON converter by Mike Bostock at: 
  (https://github.com/mbostock/topojson/wiki/Installation). Node.js is available from: (http://nodejs.org/)
* The RIF calls R directly from Java using JRI and therefore does not need PLR. If Postgres/R integration is required: 
  install R; integrate R into Postgres [optional R integration](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/plr.md).
   * Create the PL/R local R_library (in $PGDATA/R_Library), e.g: *C:/Program Files/PostgreSQL/9.3/data/R_library*
   * Create the Postgres etc directory (e.g. *C:\Program Files\PostgreSQL\9.3\etc*) and grant the ability to write 
     files (full control on Windows) to the username(s) running db_setup. This is so the build can install the psqlrc file.
* Install Postgres 9.3.5+ or 9.4.1+ and PostGIS 2.1.3+ (See port specific install notes). Building will fail on Postgres 9.2 or 
  below as PL/pgsql GET STACKED DIAGNOSTICS is used; PostGIS before 2.1.3 has bugs that will cause 
  shapefile simplification to fail (both the original PostGIS version and the newer TileMaker Node.js version).
  
  * Postgres is best downloaded from Enterprise DB: http://www.enterprisedb.com/products-services-training/pgdownload. 
  * Install Postgres
  * The Postgres installer then runs stack builder to download and install the additional packages. The following additional packages need to be installed:

    * PostGIS (Geospatial integration)
    * pgJDBC (Java database connector for PostGres)
    * pgODBC (ODBC database connector for PostGres)

    The following are optional:

    * pgAgent (batch engine)
    * pgBouncer (load balancer; for use if you have a synchronous or near synchronous replica database)

#### 2.2.1.1 Fixing Windows Code page errors

See [notes for windows users](https://www.postgresql.org/docs/9.6/static/app-psql.html):

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
 
### 2.2.2 Unixen

Unix builds are not currently supported. For more information see: 

* [Linux install from repository notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/linux_repo.md)
* [MACoS install from repository notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/macos_repo.md)
* [Linux install from source notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/linux_source.md)


## 2.3 Building the Database

### 2.3.1 Pre Build Tests

Run up a shell/command tool, *not* in the Postgres build directory (psql_scripts). The Window sizing needs to be at least 
132 columns wide and 50 rows high; preferably with a multi thousand line buffer. Otherwiser psql scripts may require <ENTER> 
on scrolling:

* Type *make* in the directory *rapidInquiryFacility\rifDatabase\Postgres*. Check make works correctly:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres>make
make: *** No targets specified and no makefile found.  Stop.
```

If you type make in the *rapidInquiryFacility* directory make will build the middleware. This may not be what you want!

#### 2.3.1.1 Configuring make

**Do not edit the Makefile directly; subsequent git changes will cause conflicts and you may loose 
your changes.** Instead, in the GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts build directory, 
copy *Makefile.local.example* to *Makefile.local* and edit *Makefile.local*. Beware 
of your choice of editor on especially Windows. The RIF is developed on Windows and Linux at the same time so 
you will have files with both Linux <CR> and Windows <CRLF> semantics. Windows Notepad in particular 
does not understand Linux files.

If you enable PL/R then the directories R_library (in $PGDATA/R_Library) and $R_HOME/share/extension must exist

* Make sure psql and make run correctly on the command line (see the Windows notes). Check logon to psql with a password for:
  * postgres
* Check Node.js is correctly installed; *cd GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node*; *npm install pg pg-native topojson*; *make install topojson*. 
  The [Node.js install dialog]
(https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/Node/Node%20Install.md) is provided as an example.
  **If the toposjon node.js does not run correctly then node is not properly installed**. 
  The requirement for Node will be removed.
* Check R is integrated into Postgres as PLR (See the PLR build instructions).

### 2.3.2 Build control using make

The RIF database is built using GNU make for ease of portability. The RIF is built from the directory 
*GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts*.

#### 2.3.2.1 Setup

##### 2.3.2.1.1 User setup

Make **MUST NOT** be run as an administrator but a normal user (completely the oppposite of the SQL Server port!). Beware: you must *NOT* have power user privilege. 

For make to work it needs to be able to logon as following users:

* postgres     - The database administrator. You will have set this password when you or the installer created 
                 the Postgres cluster.
* rif40        - The schema owner. You choose this password (see ENCRYPTED_RIF40_PASSWORD below)
* &lt;user login&gt; - Your user login. This must be in lowercase and without spaces. The username (TESTUSER) may be set in Makefile.local
* notarifuser  - A security test user

The password for all users must be set in your local .pgpass/pgpass.conf files. See [Postgres documentation](https://www.postgresql.org/docs/9.6/static/libpq-pgpass.html) for its location on various ports. 
The postgres user password must be correct in the .pgpass/pgpass.conf file and in Makefile.local or you may be locked out of postgres. 
The accounts apart from postgres are created by *db_create.sql*.

**IMPORTANT**

* By default *&lt;user login&gt;* and *notarifuser* passwords are the same as the username. It is advisable to set
  the *rif40* password to *rif40* for the moment as the middleware still uses hard coded passwords. This will be removed. 
* The *&lt;user login&gt;* password can be set using TESTPASSWORD in Makefile.local
* By default Postgres uses MD5 authentication; the user password is idependent of the Windows password unless you set up
  operating system, Kerberos or LDAP authentication 
* Before you build the database only the administrator account *postgres* is setup!.

E.g. C:\Users\pch\AppData\Roaming\postgresql\pgpass.conf:
```
localhost:5432:*:postgres:<password>
localhost:5432:*:pch:<password>
localhost:5432:*:rif40:<password>
localhost:5432:*:notarifuser:<password>
```

See *Configuration File Examples* below.

Set the following Postgres environment variables using the system control panel: *Control Panel\All Control Panel Items\System:*. This is 
well hidden on Windows 10, but you can type the path into Windows explorer! Choose *Advanced System Settings*, *Environment variables* and modify the *System Variables*.

* Make sure Postgres, MSys and the Java development kit are on the path
* Add: 
  ```PGUSER=&lt;user login&gt;```
  ```PGDATABASE=sahsuland```
  and if required PGHOST and PGPORT to the user environment
  

Create as Administrator a directory for a system wide *psql* logon script (plsqrc) in Postgres 
  
```C:\Program Files\PostgreSQL\9.6\etc```

This needs to be set so your database creation user can wruite file to this directory.
   
Once you have setup the *pgpass* file and the Postgres environment, check you can logon using psql as the database adminstrator account; *postgres*.
```
psql -d postgres -U postgres
You are connected to database "postgres" as user "postgres" on host "wpea-rif1" at port "5432".
psql (9.3.5)
Type "help" for help.

postgres=#
```

**IT IS STRONGLY ADVISED TO LEAVE THIS WINDOW OPEN SO YOU CANNOT LOCK YOURSELF OUT OF THE DATABASE IF YOU SET IT UP WRONG**. 

This scripts do check the setup is correct; but this could fail. It is possible to login to postgres as postgres without a password using the administrator or 
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
See [fixing windows code page errors](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/BUILD.md#2211-fixing-windows-code-page-errors) 
if you get a code page error.

Makefile.local is used to set:

* ENCRYPTED_POSTGRES_PASSWORD
* ENCRYPTED_RIF40_PASSWORD

The is in the form of an Md5 hash of the password with the username appended. It can be generated as follows:
```
SELECT 'md5'||md5('Imperial1234'||'rif40') AS password;
              password
-------------------------------------
 md5a210d9711fa5ffb4f170c60676c8a63e
(1 row)
```

The database creation script *db_create.sql* check tyo see of the current Postgres adminstrator (*postgres*) password is the 
same as set in the Makefile, and will abort database creation if it is not.
```
psql:db_create.sql:147: INFO:  db_create.sql() User check: postgres
psql:db_create.sql:147: INFO:  db_create.sql() rif40 needs to be created encrypted password will be ="md5971757ca86c61e2d8f618fe7ab7a32a1"
psql:db_create.sql:147: ERROR:  db_create.sql() postgres encrypted password set in makefile="md5b631d55b5718b4d083a4b6e73e5fd0c5" differs from database: "md5ef9bbf3d76edb4da049ed82636ca74f1
CONTEXT:  PL/pgSQL function inline_code_block line 53 at RAISE
```

You can change the password direct using the encrypted hash; ***beware this can get you locked out if you get it wrong**!
```
ALTER USER postgres ENCRYPTED PASSWORD 'md5a210d9711fa5ffb4f170c60676c8a63e';
```

Once *db_create.sql* has created the user accounts, each is tested in turn to check that the .pgpass/pgpass.conf 
file is setup correctly.

**See next section for Makefile.local example**

##### 2.3.2.1.2 Makefile.local settings

Typical example (Makefile.local.example):
```
# ************************************************************************
#
# GIT Header
#
# $Format:Git ID: (%h) %ci$
# $Id: e96a6b0aa1ba85325e1b7b0e57163d2b7707440b $
# Version hash: $Format:%H$
#
# Description:
#
# Rapid Enquiry Facility (RIF) - Makefile.local environment overrides (example)
#
# Copyright:
#
# The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
# that rapidly addresses epidemiological and public health questions using 
# routinely collected health and population data and generates standardised 
# rates and relative risks for any given health outcome, for specified age 
# and year ranges, for any given geographical area.
#
# Copyright 2014 Imperial College London, developed by the Small Area
# Health Statistics Unit. The work of the Small Area Health Statistics Unit 
# is funded by the Public Health England as part of the MRC-PHE Centre for 
# Environment and Health. Funding for this project has also been received 
# from the Centers for Disease Control and Prevention.  
#
# This file is part of the Rapid Inquiry Facility (RIF) project.
# RIF is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# RIF is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
# to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
# Boston, MA 02110-1301 USA
#
# Author:
#
# Peter Hambly, SAHSU
#
# Local overrides; copy Makefile.local.example to Makefile.local and edit Makefile.local
#
# Local Makefile overrides - example
#
# Postgres ENV variables
#
#PGHOST=localhost
#
# ENCRYPTED_POSTGRES/RIF40_PASSWORD hash comes from PGAdmin III or psql
#
# Postgres MD5 password format is: 
#
# 'md5'||md5('password'||'username'); e.g. to set the rif40 password to: Imperial1234, use psql:
#
# SELECT 'md5'||md5('Imperial1234'||'rif40') AS password;
# SELECT 'md5'||md5('Imperial1234'||'postgres') AS password;
#
# The rif user password is always set to the username
#
#ENCRYPTED_POSTGRES_PASSWORD=md5913e79312c717f83cfc1626754233824
#ENCRYPTED_RIF40_PASSWORD=md5a210d9711fa5ffb4f170c60676c8a63e
#
# Only set SAHSULAND_TABLESPACE_DIR if postgres has access to the directory!
#
# Make you escape spaces in the correct manner for the OS:
#
# e.g.
#
# Windows: SAHSULAND_TABLESPACE_DIR=\"C:rif40 database\"
#
#SAHSULAND_TABLESPACE_DIR=\"C:/rif40 database\"

#
# Default Windows Administrator
#
DEFAULT_WINDOWS_ADMIN_USER=Administrator

#
# PL/pgsql debug levels (DEBUG_LEVEL);
#
# 0 - Suppressed, INFO only
# 1 - Major function calls
# 2 - Major function calls, data
# 3 - Reserved for future used
# 4 - Reserved for future used
#
# PSQL verbosity (VERBOSITY):
#
# verbose	- Messages/errors with full context
# terse 	- Just the error or message
#
# PSQL echo (ECHO)
#
# all: 		- All SQL
# none:		- No SQL
#
# PSQL script user (PSQL_USER)
#
# - Usually rif40 (schema owner)
#
# Use PL/R (USE_PLR)
#
# - Database has PL/R extension loaded (not needed by default)
#
# Create SAHSULAND database only (CREATE_SAHSULAND_ONLY)
#
# - Do not create SAHSULAND_DEV
#
#VERBOSITY=terse
#DEBUG_LEVEL=0
#ECHO=none
#PSQL_USER=rif40
#USE_PLR=N
#CREATE_SAHSULAND_ONLY=N

#
# Testuser: defaults to USERNAME; set if USERNAME is in mixed case or contains spaces
# Testpassword: password for testuser; defaults to USERNAME; set if USERNAME is in mixed case or contains spaces
#
# TESTUSER=myusername
# TESTPASSWORD=myusername

#
# Eof
```

Parameters:

* PGDATABASE
* PGHOST
* DEFAULT_VERBOSITY
* DEFAULT_DEBUG_LEVEL
* DEFAULT_ECHO
* DEFAULT_PSQL_USER
* DEFAULT_ENCRYPTED_POSTGRES_PASSWORD
* DEFAULT_ENCRYPTED_RIF40_PASSWORD
* DEFAULT_USE_PLR
* DEFAULT_CREATE_SAHSULAND_ONLY

##### 2.3.2.1.3 Principal build target

**DO NOT RUN AS AN ADMINISTRATOR**

* The `db_setup` target is normally used to build a new database from scratch or to re-create a database. Creates the following databases:
  * sahsuland_empty: A empty database with the *SAHSULAND* geography and no data. For testing the data loader.
  * sahsuland_dev: A complete database created from scripts complete with the *SAHSULAND* exmaple data.
  * sahusland: A production database creared from a script and an export. Eventually a scriot will be create install sahsulabd from the 
    export.

* Use clean aswell to force rebuild
* If the build works it ends with:
  ```
	pg_dump: creating ACL "rif_data.TABLE tiles_sahsuland"
	pg_dump: creating ACL "rif_studies.TABLE s1_extract"
	pg_dump: creating ACL "rif_studies.TABLE s1_map"
	pg_dump: creating ACL "rif_studies.TABLE s2_extract"
	pg_dump: creating ACL "rif_studies.TABLE s2_map"
	pg_dump: creating ACL "rif_studies.TABLE s5_extract"
	pg_dump: creating ACL "rif_studies.TABLE s5_map"
	make[1]: Leaving directory `/c/Users/phamb/Documents/GitHub/rapidInquiryFacility/rifDatabase/Postgres/psql_scripts'
	SAHSULAND and SAHSULAND_DEV setup completed OK  
  ```
  and the production database dumnp file ```sahsuland.sql``` is present in *rapidInquiryFacility\rifDatabase\Postgres\production*.
  
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make clean db_setup
```
   **Note that this does not apply the alter scripts under development**

The principal build target (*make db_setup*) runs the following Makefiles/targets/scripts:

* Clean build targets (log files); local make target: clean
* Run local make target: *db_create*; parameters: *PSQL_USER=postgres PGDATABASE=postgres DEBUG_LEVEL=1 ECHO=all*
  * Run psql script *db_create.sql*; log: *db_create.rpt*
* Install required Node.js locally (in directory *../Node*) make target: *install*	
* Run local make target: *sahsuland_dev*; parameters: *PGDATABASE=sahsuland_dev*
  * Run local make target to create **sahsuland_dev**:
    * Run local make target: *clean*
	* Run local make target: *$(PLR_DIRS)*
	* Run main **sahsuland_dev** creation psql script: *v4_0_create_sahsuland.sql*; log: *v4_0_create_sahsuland.rpt* 
	* Run test scripts not dependent on alter scripts (in directory *test_scripts*) make target: *no_alter*; *parameters: DEBUG_LEVEL=1 ECHO=all*
	  **This is the build state at the point the SQL Server port was started**.
	* Run alter scripts (in directory *alter_scripts*) make targets: *clean all*; parameters: *DEBUG_LEVEL=1 ECHO=all*
	  * *v4_0_alter_1.sql*: Misc schema design changes
 	  * *v4_0_alter_2.sql*: Misc data viewer changes.
 	  * *v4_0_alter_5.sql*: Zoomlevel support. Rebuilds all geolevel tables with full partitioning (alter #3 support).
 	  * *v4_0_alter_6.sql*: R support [optional script if PL/R integration is enabled with *USE_PLR=Y*].
 	  * *v4_0_alter_7.sql*: Support for taxonomies/ontologies (e.g. ICD9, 10); removed previous table based support.
                            Modify t_rif40_inv_conditions to remove SQL injection risk.
 	  * *v4_0_alter_8.sql*: Database test harness; see [test harness README](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/TestHarness/db_test_harness/README.md)
 	  * *v4_0_alter_9.sql*: Misc integration fixes

		  1. Replace old geosptial build code with new data loader. Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; 
			 use rif40_geolevels lookup_table/tile_table
		  2. Make RIF40_TABLES.THEME nullable for denominators
		  3. INSERT INTO rif40_table_outcomes wrong OUTCOME_GROUP_NAME used in v4_0_postgres_sahsuland_imports.sql, suspect ICD hard coded. [Not a bug]
		  4. Fix:
			 * RIF40_NUMERATOR_OUTCOME_COLUMNS.COLUMNN_EXISTS to COLUMN_EXISTS
			 * T_RIF40_CONTEXTUAL_STATS/RIF40_CONTEXTUAL_STATS.TOTAL_COMPARISION_POPULATION to TOTAL_COMPARISON_POPULATION
		  5. Resolve: RIF40_PARAMETERS.DESCRIPTION (SQL Server) or PARAM_DESCRIPTION (Postgres)
		  6. rif40_GetAdjacencyMatrix.sql: change ST_Touches() to ST_Intersects() to fix missing adjacencies caused by small slivers
		  7. Add t_rif40_study_status/rif40_study_status
		  8. Add stats_method to rif40_studies
	  
	  Under development [not run by *db_setup*; use *make dev* to build]:
	  
	  * *v4_0_alter_3.sql*: Range partitioning (e.g. by year).
 	  * *v4_0_alter_4.sql*: Hash partitioning (e.g. by study_id) [will require PostGres 11]
	  
      Paritioning is a native feature in Postgres 10; so once this is stable (e.g. 10.2) we can convert to partitioning using SQL.
 	  
	  Completed alter script logs are named *v4_0_alter_N.&lt;database&gt;_rpt*; and renamed to *v4_0_alter_N.&lt;database&gt;_rpt.err* on error.
	  This gives make a dependency so it can re-run.
	  
	* Run test scripts (in directory *test_scripts*) make targets: *clean all*; parameters: *DEBUG_LEVEL=1 ECHO=all*
	  Test scripts that **do not** require alter scripts to be run (*no_alter* target above): 
	  * *test_1_sahsuland_geography.sql*: Test sahsuland geography is setup correctly.
	  * *test_2_ddl_checks.sql*: Check all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted
	  * *test_3_user_setup.sql*:Check &lt;test user&gt; is setup correctly.
	  
	  Test scripts that **do** require alter scripts to be run:
	  * *test_8_triggers.sql*: Test trest harness, load and run trigger tests.
	  * *test_6_middleware_1.sql*: Middleware tests 1 for *v4_0_alter_1.sql*.
	  * *test_4_study_id_1.sql*: This creates the standard study_id 1.
	  * *test_5_clone_delete_test.sql*: Clone delete test for standard study_id 1.
	  * *test_7_middleware_2.sql*: Test 7: Middleware tests 2 for *v4_0_alter_2.sql*
	  
	  Completed alter script logs are named *&lt;script&gt;.&lt;database&gt;_rpt*; and renamed to *&lt;script&gt;.&lt;database&gt;_rpt.err* on error.
	  This gives make a dependency so it can re-run.
	  
	  There is no concept of test scripts for alter scripts under development.
	  
	  Test scripts are run in sequence apart from test 8 which is run first to test the test harness.
	  
  * Run local make target: *v4_0_vacuum_analyse_dev*
    * Run psql script to *VACUUM ANALYZE* the *sahsuland_dev* database *v4_0_vacuum_analyse.sql*; log: *v4_0_vacuum_analyse.rpt*  
* Run local make target: *sahsuland_dev_dump*; parameters: *PGDATABASE=sahsuland_dev*
  * Dump *sahsuland_dev* database using *pg_dump* in custom format, excluding UK geography tables, 
    parameters: -U postgres -w -F custom -T '\*x_uk\*' -T '\*.x_ew01\*' -v sahsuland_dev > ../install/sahsuland_dev.dump
    **This is used to create the *sahsuland* database**.	
* Run in *../install* *pg_restore*; log; *pg_restore.rpt*; parameters: *-d sahsuland -U postgres -v ../install/sahsuland_dev.dump* 
* Run alter scripts (in directory alter_scripts) make targets: *clean all*; parameters: *PGDATABASE=sahsuland*		
* Run Node.js program (in directory *../Node*) make target: *topojson_convert*; parameters: *PGDATABASE=sahsuland*
  This runs the Node.js program *topojson_convert.js* to convert GeoJSON to TopoJSON. This will be replaced by a web service.
* Run local make targets: *clean v4_0_vacuum_analyse_dev*; parameters: *PGDATABASE=sahsuland*
    * Run psql script to *VACUUM ANALYZE* the *sahsuland* database *v4_0_vacuum_analyse.sql*; log: *v4_0_vacuum_analyse.rpt*
* Run test scripts (in directory *test_scripts*) make targets: *test_scripts clean all*; parameters: *PGDATABASE=sahsuland*	
* Run local make target: *sahsuland_dump*
  * Dump *sahsuland* database using *pg_dump* in custom format, excluding UK geography tables, 
    parameters: *-U postgres -w -F custom -T '\*x_uk\*' -T '\*.x_ew01\*' -v sahsuland > ../install/sahsuland.dump*
    **This is used as the *sahsuland* database* installer.**	
* Run Entity Relationship Diagrams build (in directory *../../ERD*) make target: *dbms_tools*
  <ADD>
	
End of a successful db_setup make run:
```
pg_dump: setting owner and privileges for FK CONSTRAINT table_or_view_name_hide_fk
pg_dump: setting owner and privileges for FK CONSTRAINT table_or_view_name_hide_fk
make[1]: Leaving directory `/c/Users/pch/Documents/GitHub/rapidInquiryFacility/rifDatabase/Postgres/psql_scripts'
make -C ../../ERD dbms_tools
Debug level set to default: 0
make[1]: Entering directory `/c/Users/pch/Documents/GitHub/rapidInquiryFacility/rifDatabase/ERD'
Makefile:184: *** commands commence before first target.  Stop.
make[1]: Leaving directory `/c/Users/pch/Documents/GitHub/rapidInquiryFacility/rifDatabase/ERD'
make: [db_setup] Error 2 (ignored)
SAHSULAND and SAHSULAND_DEV setup completed OK
```

##### 2.3.2.1.4 Issues with the build

* 1. Cannot ccpy psqlrc to Postgres etc directory

```
Makefile:358: warning: overriding commands for target `../production/sahsuland.dump'
Makefile:350: warning: ignoring old commands for target `../production/sahsuland.dump'
Please create c:/Program Files/PostgreSQL/9.6/etc as root/Administrator
Please create c:/Program Files/PostgreSQL/9.6/etc as root/Administrator
powershell -ExecutionPolicy ByPass -file copy.ps1  ../etc/psqlrc c:/Program Files/PostgreSQL/9.6/etc
Please Create: c:/Program Files/PostgreSQL/9.6/etc as root/Administrator
```

* Fix: permissions on directory, create if needed. Needs to be writeable by the user not an adminsitrator only

* 2. ENCRYPTED_POSTGRES_PASSWORD is incorrect in Makefile.local

```
psql:db_create.sql:147: INFO:  db_create.sql() User check: postgres
psql:db_create.sql:147: INFO:  db_create.sql() rif40 needs to be created encrypted password will be ="md5971757ca86c61e2d8f618fe7ab7a32a1"
psql:db_create.sql:147: ERROR:  db_create.sql() postgres encrypted password set in makefile="md5b631d55b5718b4d083a4b6e73e5fd0c5" differs from database: "md5ef9bbf3d76edb4da049ed82636ca74f1
CONTEXT:  PL/pgSQL function inline_code_block line 53 at RAISE
```

##### 2.3.2.1.5 Other build targets
   
* To build or rebuild the sahsuland_dev development database. This is normally used for regression testing:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make clean sahsuland_dev
```
   **Note that this does not apply the alter scripts under development**
   
* To build or rebuild sahsuland_empty
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make clean sahsuland_empty
```

* To patch sahsuland and sahsuland_dev:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make patch
```

* To re-patch sahsuland and sahsuland_dev:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make repatch
```

* To patch sahsuland_dev using the alter scripts under development:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make dev
```
   **Patching will be improved so that scripts are only run once; at present all patch scripts are run and 
     designed to be run multiple times**

#### 2.3.2.2 Porting limitations

Makefiles have the following limitations:

* Full dependency tracking for SQL scripts has not yet been implemented; you are advised to do a *make clean* 
  or *make devclean* before building as in the below examples or nothing much may happen.
  
#### 2.3.2.3 Help

The Makefile has [help](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/psql_scripts/Make%20Help.md):

```C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>make help
Debug level set to default: 0
Makefile:366: warning: overriding commands for target `../production/sahsuland.dump'
Makefile:358: warning: ignoring old commands for target `../production/sahsuland.dump'
findstr "#-" Makefile
#-
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
#-
#- DO NOT RUN THE SUBDIRECTORY MAKEFILES DIRECTLY; THEY REQUIRE ENVIRONMENT SETUP TO WORK CORRECTLY
#-
        HELP=findstr "\#-" Makefile
        HELP=grep "\#-" Makefile
#-
#- PL/pgsql debug levels (DEBUG_LEVEL);
#-
#- 0 - Suppressed, INFO only
#- 1 - Major function calls
#- 2 - Major function calls, data
#- 3 - Reserved for future used
#- 4 - Reserved for future used
#-
#- PSQL verbosity (VERBOSITY):
#-
#- verbose      - Messages/errors with full context
#- terse        - Just the error or message
#-
#- PSQL echo (ECHO)
#-
#- all:                 - All SQL
#- none:                - No SQL
#-
#- Targets
#-
#- 1. patching
#-
#- all: Run all completed alter scripts and test [DEFAULT]
#- patch: Run all completed alter scripts on both sahsuland_dev and sahusland
#- repatch: Re-run all in-complete alter scripts on both sahsuland_dev and sahusland
#- world: fully rebuild databases from source
#- dev: Run all alter scripts in development
#-
#- 2. build
#-
#- sahsuland_dev_no_alter: Rebuild sahsuland_dev, test [State of SAHSULAND at port to SQL server], finally VACUUM ANALYSE
#- sahsuland_dev: Rebuild sahsuland_dev, test, then patch dev only, retest, finally VACUUM ANALYZE
#- Does not run all alter scripts in development
#- Test can fail
#- sahsuland_empty: Rebuild sahsuland_empty, test, then patch dev only
#- Does not run all alter scripts in development
#- Test can fail
#- topojson_convert: GeoJSON to topoJSON converter
#-
#- 3. installers
#-
#- sahsuland.sql: Dump sahsuland database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#- sahsuland_dev_dump: Dump sahsuland_dev database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#-                     Used to create sahsuland
#-
#- 4. test
#-
#- test: Run all test scripts [Non verbose, no debug]
#- retest: Re-run incomplete test scripts [Non verbose, no debug]
#- test_no_alter: Run test scripts able to be run before the alter scripts [Non verbose, no debug]
#- test: Run all test scripts [debug_level=1]
#- test: Run all test scripts [debug_level=1]
#- test: Run all test scripts [Verbose, debug_level=2, echo=all]
#-
#- 5. cleanup
#-
#- clean: Remove logs so completed scripts can be re-run
#- devclean: Remove logs so alter scripts in development can be r-run
#-           Not normally needed as they abort.
#-
#- 7. Database setup. Needs to be able to connect to postgresDB as postgres
#-
#- db_setup: Re-create empty sahsuland, sahsuland_dev; build sahusland_dev from scripts;
#-           build dev dump files; restore sahsuland from dev dump; patch sahsuland to dev standard;
#-           build production dump file; rebuild ERD model
#- ERD: remake ERD
#- db_create: Create empty database
#-
#- 7. miscellaneous
#-
#- v4_0_vacuum_analyse_dev: VACUUM ANALYZE sahsuland dev database
#- help: Display this help
#- recurse: Recursive make target: make recurse <recursive target>
#-          e.g. make recurse alter_1.rpt
#-
```
 
#### 2.3.2.4 Configuration File Examples

If you are running locally only (e.g. on a laptop) you do *NOT* need to edit the configuration files.

##### 2.3.2.4.1 Postgres user password file

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

##### 2.3.2.4.2 Authentication Setup (hba.conf)

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

##### 2.3.2.4.3 Proxy User Setup (ident.conf)

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
