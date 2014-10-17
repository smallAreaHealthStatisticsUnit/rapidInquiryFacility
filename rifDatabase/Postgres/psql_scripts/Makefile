# ************************************************************************
#
# GIT Header
#
# $Format:Git ID: (%h) %ci$
# $Id: e96a6b0aa1ba85325e1b7b0e57163d2b7707440b $
# Version hash: $Format:%H$
#
# Description:
#-
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
#-
#- DO NOT RUN THE SUBDIRECTORY MAKEFILES DIRECTLY; THEY REQUIRE ENVIRONMENT SETUP TO WORK CORRECTLY
#-
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
SUBDIRS = alter_scripts test_scripts
#
# Conditional environment variables - can be changed to point to sahsuland by higher makefiles
#
# PG_DATABASE - set to sahsuland_dev
#
PGDATABASE=sahsuland_dev
DEFAULT_VERBOSITY=terse
DEFAULT_DEBUG_LEVEL=0
DEFAULT_ECHO=none
DEFAULT_PSQL_USER=rif40
DEFAULT_ENCRYPTED_POSTGRES_PASSWORD=md57d50cade66c8b224d1fbd91805cbf442
DEFAULT_USE_PLR=N

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
DBNAME?=$(PGDATABASE)
TESTUSER?=$(USERNAME)
VERBOSITY?=$(DEFAULT_VERBOSITY)
DEBUG_LEVEL?=$(DEFAULT_DEBUG_LEVEL)
ECHO?=$(DEFAULT_ECHO)
PSQL_USER?=$(DEFAULT_PSQL_USER)
ENCRYPTED_POSTGRES_PASSWORD?=$(DEFAULT_ENCRYPTED_POSTGRES_PASSWORD)
USE_PLR?=$(DEFAULT_USE_PLR)
#
# Programs and parameters
#
WINDOWS_RUN_SCRIPT=run.ps1
LINUX_RUN_SCRIPT=run.sh
PSQL=psql
PSQL_FLAGS=-U $(PSQL_USER) -d $(PGDATABASE) -w -e -P pager=off \
	-v testuser=$(TESTUSER) -v verbosity=$(VERBOSITY) -v debug_level=$(DEBUG_LEVEL) -v echo=$(ECHO) \
	-v encrypted_postgres_password=$(ENCRYPTED_POSTGRES_PASSWORD) -v use_plr=$(USE_PLR) \
	-v sahsuland_tablespace_dir=$(SAHSULAND_TABLESPACE_DIR)

#
# Phony (i.e. not a file) targets
#
.PHONY: recurse $(SUBDIRS) all dev devclean clean sahsuland_dev patch test
 
#
# Run bash on Linux, Powershell on Windows_NT
#
OS?=Unknown
ifeq ($(OS),Windows_NT)
#
# Windows support
#
RUN_SCRIPT=$(WINDOWS_RUN_SCRIPT)
RUN=powershell -ExecutionPolicy ByPass -file $(RUN_SCRIPT)
DELETE=powershell -ExecutionPolicy ByPass -file delete.ps1
HELP=findstr "\#-" Makefile
SAHSULAND_TABLESPACE_DIR="C:\PostgresDB\sahsuland"
COPY=powershell -ExecutionPolicy ByPass -file copy.ps1 
POSTGRES_ETC_DIR="C:\Program Files\PostgreSQL\9.3\etc"
PGPASS=powershell -ExecutionPolicy ByPass -file pgpass.ps1
#
# ADD Linux support
#
else
#
# Unsupported OS
#
$(error Unsupported OS: $(OS))
endif

#
# Target extension depends on source, build rule
#
.SUFFIXES: .sql .rpt
.sql.rpt:
	$(RUN) $@ $(CURDIR) $(PSQL) $(PSQL_FLAGS) -f $< 
	
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
#- verbose	- Messages/errors with full context
#- terse 	- Just the error or message
#-
#- PSQL echo (ECHO)
#-
#- all: 		- All SQL
#- none:		- No SQL
#-
#- Targets
#- 
#- 1. patching
#-
#- all: Run all completed alter scripts [DEFAULT]
all:
	$(MAKE) -C alter_scripts all

#- patch: Run all completed alter scripts on both sahsuland_dev and sahusland	
patch:
	$(MAKE) -C alter_scripts clean
	$(MAKE) -C alter_scripts all
	$(MAKE) -C alter_scripts clean
	$(MAKE) -C alter_scripts all PGDATABASE=sahsuland

#- dev: Run all alter scripts in development
dev:
	$(MAKE) -C alter_scripts dev DEBUG_LEVEL=1
	
#-
#- 2. build
#-	
#- sahsuland_dev_no_alter: Rebuild sahsuland_dev, test [State of SAHSULAND at port to SQL server], finally VACUUM ANALYZE
sahsuland_dev_no_alter: clean v4_0_create_sahsuland.rpt 
	$(MAKE) -C test_scripts all DEBUG_LEVEL=1 ECHO=all
	$(MAKE) v4_0_vacuum_analyse_dev
	
#- sahsuland_dev: Rebuild sahsuland_dev, test, then patch dev only, retest, finally VACUUM ANALYZE
sahsuland_dev: clean v4_0_create_sahsuland.rpt 
	$(MAKE) -C test_scripts no_alter DEBUG_LEVEL=1 ECHO=all
	$(MAKE) -C alter_scripts all DEBUG_LEVEL=1 ECHO=all
	$(MAKE) -C test_scripts clean all DEBUG_LEVEL=1 ECHO=all
	$(MAKE) v4_0_vacuum_analyse_dev
	
# Add UK gis

#-
#- 3. installers
#-	
#- sahsuland_dump: Dump sahsuland database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
sahsuland_dump:
	pg_dump -U postgres -w -F plain -T '*x_uk*' -T '*.x_ew01*' -v -C sahsuland > ../install/sahsuland.sql	
#- sahsuland_dev_dump: Dump sahsuland_dev database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#-                     Used to create sahsuland
sahsuland_dev_dump:
	pg_dump -U postgres -w -F plain -T '*x_uk*' -T '*.x_ew01*' -v sahsuland_dev > ../install/sahsuland_dev.sql	
	
#-
#- 4. test
#-	
#- test: Run all test scripts [Non verbose, no debug]
test:
	$(MAKE) -C test_scripts clean all
#- test_no_alter: Run test scripts able to be run before the alter scripts [Non verbose, no debug]
test_no_alter:
	$(MAKE) -C test_scripts clean no_alter
#- test: Run all test scripts [debug_level=1]
debug_level_1_test:
	$(MAKE) -C test_scripts clean all DEBUG_LEVEL=1 ECHO=all
#- test: Run all test scripts [Verbose, debug_level=2, echo=all]
verbose_test:
	$(MAKE) -C test_scripts clean all VERBOSITY=verbose DEBUG_LEVEL=2 ECHO=all
	
#-
#- 5. cleanup
#-	
#- clean: Remove logs so completed scripts can be re-run
clean:
	$(MAKE) -C alter_scripts clean
	$(MAKE) -C test_scripts clean
	$(DELETE) db_create.rpt v4_0_create_sahsuland.rpt v4_0_vacuum_analyse.rpt
#- devclean: Remove logs so alter scripts in development can be r-run 
#-           Not normally needed as they abort.
devclean:
	$(MAKE) -C alter_scripts devclean

#-
#- 7. Database setup. Needs to be able to connect to postgresDB as postgres
#-
# Stored password file is in ~/.pgpass on Linux or: %APPDATA%\postgresql\pgpass.conf on Windows
# Format is: hostname:port:database:username:password
#
#- db_setup: Creste sahusland, sahsuland_dev
db_setup: pgpass.conf psqlrc
		$(MAKE) db_create PSQL_USER=postgres PGDATABASE=postgres DEBUG_LEVEL=1 ECHO=all
		$(MAKE) sahsuland_dev 
		$(MAKE) sahsuland_dev_dump
#		$(RUN) pg_restore.rpt ../install pg_restore -d sahsuland -U postgres -v sahsuland.dump
		
db_create: db_create.rpt

#
# Files to create/install
#
# pgpass: password file
pgpass.conf:
#		$(PGPASS)
# psqlrc: psql logon script
psqlrc:	../etc/psqlrc
		$(COPY) ../etc/psqlrc $(POSTGRES_ETC_DIR)
		
#-
#- 7. miscellaneous
#-	

#- v4_0_vacuum_analyse_dev: VACUUM ANALYZE sahsuland dev database
v4_0_vacuum_analyse_dev: v4_0_vacuum_analyse.rpt

#- help: Display this help
help:
	$(HELP)

#- recurse: Recursive make target: make recurse <recursive target>
#-          e.g. make recurse alter_1.rpt
recurse: $(SUBDIRS)	
$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

#-
#
# Eof