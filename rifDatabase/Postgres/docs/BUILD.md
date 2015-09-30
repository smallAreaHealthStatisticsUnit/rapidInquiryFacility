# RIF40 Postgres database build from Github

## Database development environment

Current two databases are used: *sahsuland* and *sahsuland_dev*. The installer database *sahsuland* is kept stable for long periods, 
*sahsuland_dev* is the working database. The development database *sahusland_dev* is built from psql scripts which create and 
populate the database ojects. Then alter scripts not under development and then run and the database tested. The database is then 
exported and then imported into *sahsuland*. The *sahsuland_dev* database can then be enhanced with the alter scripts under development.

The middleware always runs against sahusland.

After the move from the SAHSU private network to the main 
Imperial network the database structure is only modified by alter scripts so that the SAHSU Private network 
and Microsoft SQL Server Ports can be kept up to date. Alter scripts are designed to be run more than once without 
side effects, although they must be run in numeric order.

Once the RIF is in production alter scripts will only be able to complete successfully once and cannot then 
be re-run.

The directory structure is rifDatabase\Postgres\:

* conf            - Exmaples of varaious configuration files
* etc             - Postgres psql logon script (.psqlrc)
* example_data    - Example extracts and results creating as part of SAHSUland build
* logs            - Logs from psql_scripts runs
* PLpgsql         - PL/pgsql scripts
* psql_scripts    - PSQL scripts; **Postgres database build directory**
* sahsuland       - SAHSUland creation psql scripts
* sahsuland\data  - SAHSUland data
* shapefiles      - Postgres psql SQL scripts derived from shapefiles, creation scripts
* shapefiles\data - Shapefiles

The databases *sahsuland* and *sahsuland_dev* are built using make and Node.js. 

## Tool chain 

On Windows:

* Install MinGW with the full development environment and add *c:\MinGW\msys\1.0\bin* to path
* Install Python 2.7 **not Python 3.n**
* Install a C/C++ compiler, suitable for use by Node.js. I used Microsoft Visual Studio 2012 
  under an Academic licence. The dependency on Node.js will be reduced by replacing the current 
  program with Node.js web services available for use by the wider RIF community.
* Create the Postgres etc directory (e.g. C:\Program Files\PostgreSQL\9.3\etc) and grant full control 
  to the username(s) running db_setup. This is so the build can install the psqlrc file.

For more information see: [Windows installer notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/windows.md)

On Windows, Linux and MacOS

* Install Postgres 9.3.5+ or 9.4.1+ and PostGIS 2.1.3+. Building will fail on Postgres 9.2 or 
  below as PL/pgsql GET STACKED DIAGNOSTICS is used; PostGIS before 2.1.3 has bugs that will cause 
  shapefile simplification to fail.
* Install 64 bit Node.js. The 32 bit node will run into memory limitation problems will vary large 
  shapefiles. node.js is required to build to geoJSON to topoJSON converter by Mike Bostock at: 
  (https://github.com/mbostock/topojson/wiki/Installation). Node.js is available from: (http://nodejs.org/)
* Install R; integrate R into Postgres [optional R integration](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/plr.md)

For more information see: 

* [Linux install from repository notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/linux_repo.md)
* [MACoS install from repository notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/macos_repo.md)
* [Linux install from source notes](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/docs/linux_source.md)

### Pre build tests

Run up a command tool, *not* in the Postgres build directory (psql_scripts):

* Type *make*. Check make works correctly:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres>make
make: *** No targets specified and no makefile found.  Stop.
```

#### Configuring make

**Do not edit the Makefile directly; subsequent git changes will cause conflicts and you may loose 
your changes.** Instead, in the GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts build directory, 
copy Makefile.local.example to Makefile.local and edit Makefile.local. Beware 
of your choice of editor on especially Windows. The RIF is developed on Windows and Linux at the same time so 
you will have files with both Linux <CR> and Windows <CRLF> semantics. Windows Notepad in particular 
does not understand Linux files.

If you enable PL/R then the directories ? and ? must exist

* Make sure psql and make run correctly on the command line (see the Windows notes). Check logon to psql with a password for:
  * postgres
* Check Node.js is correctly installed; *cd GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node*; *make install topojson*. 
  The [Node.js install dialog]
(https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/Node/Node%20Install.md) is provided as an example.
  **If the toposjon node.js does not run correctly then node is not properly installed**. 
* Check R is integrated into Postgres as PLR (See the PLR build instructions).

### Build control using make

The RIF database is built using GNU make for ease of portability. The RIF is built from the directory 
*GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts*.

#### Principal targets

* Create sahsuland_dev and sahusland databases. This is normally used to build a new database from scratch or to re-create a database:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make db_setup
```
   **Note that this does not apply the alter scripts under development**
   
* To build or rebuild the sahsuland_dev developement database. This is normally used for regression testing:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make clean sahsuland_dev
```
   **Note that this does not apply the alter scripts under development**
   
* To build or rebuild sahsuland_dev:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts> make clean sahsuland_dev
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

##### User setup

For make to work it needs to be able to logon as following users:

* postgres     - The database administrator. You will have set this password when you or the installer created 
                 the Postgres cluster.
* rif40        - The schema owner. You choose this password (see ENCRYPTED_RIF40_PASSWORD below)
* &lt;user login&gt; - Your user login. This must be in lowercase and without spaces. The build scripts will 
                 convert a mixed case username to lowercase if the Linux program tr is on the path. 
* notarifuser  - A security test user

The password for all users must be set in your local .pgpass/pgpass.conf files (see Postgres documentation for its location on various OS). 
The postgres user password must be correct in the .pgpass/pgpass.conf file and in Makefile.local or you will be locked out of postgres. 
The accounts apart from postgres are created by *db_create.sql*.

**IMPORTANT**

* By default *&lt;user login&gt;* and *notarifuser* passwords are the same as the username. It is advisable to set
  the *rif40* password to *rif40* for the moment as the middleware still uses hard coded passwords. This will be removed.  
* By default Postgres uses MD5 authentication; the user password is idependent of the Windows password unless you set up
  operating system, Kerberos or LDAP authentication 

E.g. C:\Users\pch\AppData\Roaming\postgresql\pgpass.conf:
```
wpea-rif1:5432:*:postgres:<password>
wpea-rif1:5432:*:pch:<password>
wpea-rif1:5432:*:rif40:<password>
wpea-rif1:5432:*:notarifuser:<password>
```

Once you have setup the *pgpass* file, check you can logon using psql as the database adminstrator account; *postgres*.
```
psql -d postgres -U postgres
You are connected to database "postgres" as user "postgres" on host "wpea-rif1" at port "5432".
psql (9.3.5)
Type "help" for help.

postgres=#
```
See the port specific instructions if you get a code page error or the shell cannot find psql.

The following should normally be set in your shell environment (see port specific instructions):

* PGDATABASE - sahusland_dev
* PGHOST - localhost

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
ALTER USER postgres ENCRYPTED PASSWORD 'md5a210d9711fa5ffb4f170c60676c8a63e';
```

Once *db_create.sql* has created the user accounts, each is tested in turn to check that the .pgpass/pgpass.conf 
file is setup correctly.

**See next section for Makefile.local example**

##### Makefile.local settings

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

#### Porting limitations

Makefiles have the following limitations:

* Full dependency tracking for SQL scripts has not yet been implemented; you are advised to do a *make clean* 
  or *make devclean* before building as in the below examples or nothing much may happen.
* A fully working version of Node.js that can compile is required or you will not be able to generate the 
  topoJSON tiles data.

#### Help

The Makefile has [help](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDatabase/Postgres/psql_scripts/Make%20Help.md):

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>make help
```
 
