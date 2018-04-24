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
# Postgres ENV variables
#
PGDATABASE=sahsuland_dev
PGHOST=localhost
#
DEFAULT_VERBOSITY=terse
DEFAULT_DEBUG_LEVEL=0
DEFAULT_ECHO=none
DEFAULT_PSQL_USER=rif40
DEFAULT_USE_PLR=N
DEFAULT_CREATE_SAHSULAND_ONLY=N
#
# DEFAULT_ENCRYPTED_POSTGRES_PASSWORD hash comes from PGAdmin III or psql
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
DEFAULT_ENCRYPTED_POSTGRES_PASSWORD=md57d50cade66c8b224d1fbd91805cbf442
DEFAULT_ENCRYPTED_RIF40_PASSWORD=md5dba9980b96510989e720a66fc77d92c8
#
# Only set SAHSULAND_TABLESPACE_DIR if you have access to the directory!
#
DEFAULT_SAHSULAND_TABLESPACE_DIR=
#
# Default Windows Administrator
#
DEFAULT_WINDOWS_ADMIN_USER=Administrator
#
# Local overrides; copy Makefile.local.example to Makefile.local and edit Makefile.local
#
-include Makefile.local

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
DBNAME?=$(PGDATABASE)
VERBOSITY?=$(DEFAULT_VERBOSITY)
ifndef DEBUG_LEVEL
DEBUG_LEVEL?=$(DEFAULT_DEBUG_LEVEL)
$(info Debug level set to default: $(DEFAULT_DEBUG_LEVEL))
else
$(info Debug level set to: $(DEBUG_LEVEL))
endif
ECHO?=$(DEFAULT_ECHO)
PSQL_USER?=$(DEFAULT_PSQL_USER)
USE_PLR?=$(DEFAULT_USE_PLR)
CREATE_SAHSULAND_ONLY?=$(DEFAULT_CREATE_SAHSULAND_ONLY)
ENCRYPTED_POSTGRES_PASSWORD?=$(DEFAULT_ENCRYPTED_POSTGRES_PASSWORD)
ENCRYPTED_RIF40_PASSWORD?=$(DEFAULT_ENCRYPTED_RIF40_PASSWORD)
SAHSULAND_TABLESPACE_DIR?=$(DEFAULT_SAHSULAND_TABLESPACE_DIR)
WINDOWS_ADMIN_USER?=$(DEFAULT_WINDOWS_ADMIN_USER)

#
# Programs and parameters
#
PSQL=psql
PSQL_FLAGS=-U $(PSQL_USER) -d $(PGDATABASE) -h $(PGHOST) -w -e -P pager=off \
	-v testuser=$(TESTUSER) -v testpassword=$(TESTPASSWORD) -v verbosity=$(VERBOSITY) -v debug_level=$(DEBUG_LEVEL) -v echo=$(ECHO) \
	-v encrypted_postgres_password=$(ENCRYPTED_POSTGRES_PASSWORD) \
	-v encrypted_rif40_password=$(ENCRYPTED_RIF40_PASSWORD) \
	-v use_plr=$(USE_PLR) \
	-v sahsuland_tablespace_dir=$(SAHSULAND_TABLESPACE_DIR) \
	-v create_sahsuland_only=$(CREATE_SAHSULAND_ONLY) \
	-v pghost=$(PGHOST) \
	-v os=$(OS)

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
	RUN=powershell -ExecutionPolicy ByPass -file run.ps1
	DELETE=powershell -ExecutionPolicy ByPass -file delete.ps1
	HELP=findstr "\#-" Makefile
#	SUDO_MKDIR=runas /user:$(WINDOWS_ADMIN_USER) "powershell -ExecutionPolicy ByPass -file mkdir.ps1 $@" 
	SUDO_MKDIR=powershell -ExecutionPolicy ByPass -file mkdir.ps1 "$@"
	SUDO_CHOWN_TESUSER=powershell -ExecutionPolicy ByPass -file chown.ps1 $(TESTUSER) "$@"
	SUDO_CHOWN_POSTGRES=powershell -ExecutionPolicy ByPass -file chown.ps1 postgres "$@"
	COPY=powershell -ExecutionPolicy ByPass -file copy.ps1 
	PGPASS=powershell -ExecutionPolicy ByPass -file pgpass.ps1
	# Assumes tr came with make. Force to lowercase
	CLEAN_USERNAME=$(shell echo $(USERNAME) | tr ‘[:upper:]’ ‘[:lower:]’)
	TESTUSER?=$(CLEAN_USERNAME)
	ifeq ($(TESTUSER),)
		TESTUSER=$(USERNAME)
	endif
	TESTPASSWORD?=$(CLEAN_USERNAME)
	ifeq ($(TESTPASSWORD),)
		TESTPASSWORD=$(USERNAME)
	endif
# Causes hangs; does strange things to console!!!
#	AM_I_ADMIN=$(shell cmd /C .\am_i_admin.bat)
#	ifeq ($(AM_I_ADMIN),Y)
#			$(error DO not tun this makefile as an administrator)
#	TOPOJSON="C:\node_modules\.bin\topojson.cmd"
	PGPASSFILE=$(APPDATA)\postgresql\pgpass.conf
else
#
# Linux macos support
#
	UNAME_S := $(shell uname -s)
	RUN=./run.sh
	DELETE=rm -f
	HELP=grep "\#-" Makefile
	SUDO_MKDIR=sudo mkdir -p "$@"
	SUDO_CHOWN_TESUSER=chown -R $(TESTUSER) "$@"
	SUDO_CHOWN_POSTGRES=chown -R postgres "$@"
	COPY=cp
#   Assumes tr came with make. Force to lowercase
#	CLEAN_USERNAME=$(shell echo $(USERNAME) | tr ‘[:upper:]’ ‘[:lower:]’)
#	TESTUSER?=$(CLEAN_USERNAME)
#	ifeq ($(TESTUSER),)
#		TESTUSER?=$(USERNAME)
#	endif
	TESTUSER?=$(USERNAME)
	TOPOJSON=topojson
    ifeq ($(UNAME_S),Linux)
		OS=$(UNAME_S)
#		POSTGRES_ETC_DIR=/var/opt/PostgreSQL/9.3/etc
		PGPASS=./pgpass.sh
	else
		ifeq ($(UNAME_S),Darwin)	
			OS=$(UNAME_S)
#			POSTGRES_ETC_DIR=/Library/PostgreSQL/9.3/etc/posgresql
			PGPASS=./pgpass.sh
		else
#
# Unsupported OS
#
			$(error Unsupported OS: $(OS); uname -s: $(UNAME_S))
		endif
	endif
	PGPASSFILE=~/.pgpass
endif
PG_VERSION=$(shell pg_config --version)
#
# Escape directory names. This assumes that Postgres is installed on the C drive
# Make assumes the usual Windows names, not the Postgres funny onews, so the slashes need to be the windows way around and C: in capitals
#
PG_SYSCONFDIR=$(shell pg_config --sysconfdir)
POSTGRES_ETC_DIR=$(PG_SYSCONFDIR)
empty=
ESC_POSTGRES_ETC_DIR:=$(subst $(empty)/$(empty),$(empty)\$(empty),$(subst c:,C:,$(subst $(empty) $(empty),$(empty)\ $(empty),$(POSTGRES_ETC_DIR))))
#
# R output directories (under PG_DATA)
#
R_LIBRARY=$(ESC_POSTGRES_ETC_DIR)\..\data\R_library
R_SOURCE=$(ESC_POSTGRES_ETC_DIR)\..\data\R_source
ifeq ($(USE_PLR),Y)
	PLR_DIRS=$(R_LIBRARY) $(R_SOURCE)
	R_SOURCE_FILES=R_source/rif40_capture_output.R
else
	PLR_DIRS=
	R_SOURCE_FILES=
endif
#
# Target extension depends on source, build rule
#
.SUFFIXES: .sql .rpt
.sql.rpt:
	$(RUN) $@ "$(CURDIR)" $(PSQL) $(PSQL_FLAGS) -f $< 
	
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
#- all: Run all completed alter scripts and test [DEFAULT]
all: $(PLR_DIRS)
	$(MAKE) -C alter_scripts all
	$(MAKE) -C test_scripts clean all

#- patch: Run all completed alter scripts on both sahsuland_dev and sahusland	
patch:
	$(MAKE) -C alter_scripts clean all
	$(MAKE) -C test_scripts clean all
	$(MAKE) -C alter_scripts clean all PGDATABASE=sahsuland_empty
	$(MAKE) -C alter_scripts clean all PGDATABASE=sahsuland
	$(MAKE) -C test_scripts clean all PGDATABASE=sahsuland
	$(MAKE) v4_0_vacuum_analyse_dev
	
#- repatch: Re-run all in-complete alter scripts on both sahsuland_dev and sahusland	
repatch:
	$(MAKE) -C alter_scripts all
	$(MAKE) -C test_scripts all
	$(MAKE) -C alter_scripts all PGDATABASE=sahsuland_empty
	$(MAKE) -C alter_scripts all PGDATABASE=sahsuland
	$(MAKE) -C test_scripts all PGDATABASE=sahsuland
	$(MAKE) v4_0_vacuum_analyse_dev

#- world: fully rebuild databases from source
world:
	$(MAKE) db_setup
	
#- dev: Run all alter scripts in development
dev: $(PLR_DIRS)
	$(MAKE) -C alter_scripts dev DEBUG_LEVEL=1

# Dummy for test	
noop: $(PLR_DIRS)

#-
#- 2. build
#-	
#- sahsuland_dev_no_alter: Rebuild sahsuland_dev, test [State of SAHSULAND at port to SQL server], finally VACUUM ANALYSE
sahsuland_dev_no_alter: clean v4_0_create_sahsuland.rpt 
	-$(MAKE) -C test_scripts all DEBUG_LEVEL=1 ECHO=all
	$(MAKE) v4_0_vacuum_analyse_dev
	
#- sahsuland_dev: Rebuild sahsuland_dev, test, then patch dev only, retest, finally VACUUM ANALYZE
#- Does not run all alter scripts in development
#- Test can fail
sahsuland_dev: clean $(PLR_DIRS) v4_0_create_sahsuland.rpt 
	-$(MAKE) -C test_scripts no_alter DEBUG_LEVEL=1 ECHO=all
	$(MAKE) -C alter_scripts clean all DEBUG_LEVEL=1 ECHO=all
	-$(MAKE) -C test_scripts clean all DEBUG_LEVEL=1 ECHO=all
	$(MAKE) v4_0_vacuum_analyse_dev
	
#- sahsuland_empty: Rebuild sahsuland_empty, test, then patch dev only
#- Does not run all alter scripts in development
#- Test can fail
sahsuland_empty: $(PLR_DIRS) 
	$(MAKE) clean PGDATABASE=sahsuland_empty
	$(MAKE) recreate_sahsuland_empty_db.rpt PSQL_USER=postgres PGDATABASE=postgres DEBUG_LEVEL=1 ECHO=all
	$(MAKE) v4_0_create_sahsuland_empty.rpt PGDATABASE=sahsuland_empty
	$(MAKE) -C alter_scripts clean all DEBUG_LEVEL=1 ECHO=all PGDATABASE=sahsuland_empty
	-$(MAKE) -C test_scripts clean all DEBUG_LEVEL=1 ECHO=all PGDATABASE=sahsuland_empty
	
#- topojson_convert: GeoJSON to topoJSON converter
topojson_convert:
#	$(MAKE) -C ../Node topojson_convert
# create a completed target
	
# Add UK gis

#-
#- 3. installers
#-	
#- sahsuland.sql: Dump sahsuland database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
sahsuland.sql: ../production/sahsuland.sql
sahsuland.dump: ../production/sahsuland.dump	
# make dependent on completed target
../production/sahsuland.sql: 
	pg_dump -U postgres -w -F plain -T '*x_uk*' -T '*.x_ew01*' -N $(TESTUSER) -v -C sahsuland > ../production/sahsuland.sql
../production/sahsuland.dump:	
	pg_dump -U postgres -w -F custom -T '*x_uk*' -T '*.x_ew01*' -N $(TESTUSER) -v sahsuland > ../production/sahsuland.dump	
#- sahsuland_dev_dump: Dump sahsuland_dev database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#-                     Used to create sahsuland
sahsuland_dev_dump: ../production/sahsuland_dev.dump 
../production/sahsuland_dev.dump:
	pg_dump -U postgres -w -F custom -Z 9 -T '*x_uk*' -T '*.x_ew01*' -v sahsuland_dev > ../production/sahsuland_dev.dump	
#
../production/sahsuland.dump:
	pg_dump -U postgres -w -F custom -T '*x_uk*' -T '*.x_ew01*' -N $(TESTUSER) -v sahsuland > ../production/sahsuland.dump		
	
#-
#- 4. test
#-	
#- test: Run all test scripts [Non verbose, no debug]
test:
	$(MAKE) -C test_scripts clean all
#- retest: Re-run incomplete test scripts [Non verbose, no debug]
retest:
	$(MAKE) -C test_scripts all	
#- test_no_alter: Run test scripts able to be run before the alter scripts [Non verbose, no debug]
test_no_alter:
	$(MAKE) -C test_scripts clean no_alter
#- test: Run all test scripts [debug_level=1]
debug_level_1_test:
	$(MAKE) -C test_scripts clean all DEBUG_LEVEL=1 ECHO=all
#- test: Run all test scripts [debug_level=1]
debug_level_1_retest:
	$(MAKE) -C test_scripts all DEBUG_LEVEL=1 ECHO=all	
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
	$(DELETE) db_setup.rpt db_create.rpt recreate_sahsuland_empty_db.rpt recreate_sahsuland_empty_db.rpt \
			v4_0_create_sahsuland.rpt v4_0_create_sahsuland_empty.rpt v4_0_vacuum_analyse.rpt \
			../production/sahsuland_dev.dump ../production/sahsuland.dump ../production/sahsuland.sql \
			../install/sahsuland_dev.dump ../install/sahsuland.dump ../install/sahsuland.sql
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
#- db_setup: Re-create empty sahsuland, sahsuland_dev; build sahusland_dev from scripts; 
#-           build dev dump files; restore sahsuland from dev dump; patch sahsuland to dev standard; 
#-           build production dump file; rebuild ERD model
db_setup: psqlrc $(PGPASSFILE) $(PLR_DIRS)
ifneq ($(OS),Windows_NT)
ifneq ($(UNAME_S),Darwin)	
	$(MAKE) -C ../../ERD veryclean dbms_tools
endif
endif
	$(MAKE) clean
	$(MAKE) db_create PSQL_USER=postgres PGDATABASE=postgres DEBUG_LEVEL=1 ECHO=all
#	$(MAKE) -C ../Node install	
	$(MAKE) sahsuland_empty PGDATABASE=sahsuland_empty
	$(MAKE) sahsuland_dev PGDATABASE=sahsuland_dev
	$(MAKE) sahsuland_dev_dump PGDATABASE=sahsuland_dev
ifeq ($(PG_VERSION),PostgreSQL 9.4.1)
	-$(RUN) pg_restore.rpt ../production pg_restore -d sahsuland -U postgres -v ../production/sahsuland_dev.dump
else
ifeq ($(PG_VERSION),PostgreSQL 9.4.2)
	-$(RUN) pg_restore.rpt ../production pg_restore -d sahsuland -U postgres -v ../production/sahsuland_dev.dump
else
ifeq ($(PG_VERSION),PostgreSQL 9.4.3)
	-$(RUN) pg_restore.rpt ../production pg_restore -d sahsuland -U postgres -v ../production/sahsuland_dev.dump
else
ifeq ($(PG_VERSION),PostgreSQL 9.4.4)
	-$(RUN) pg_restore.rpt ../production pg_restore -d sahsuland -U postgres -v ../production/sahsuland_dev.dump
else
ifeq ($(PG_VERSION),PostgreSQL 9.4.5)
	-$(RUN) pg_restore.rpt ../production pg_restore -d sahsuland -U postgres -v ../production/sahsuland_dev.dump
else
	$(RUN) pg_restore.rpt ../production pg_restore -d sahsuland -U postgres -v ../production/sahsuland_dev.dump
endif
endif
endif
endif
endif
	$(MAKE) -C alter_scripts clean all PGDATABASE=sahsuland		
#	$(MAKE) -C ../Node topojson_convert PGDATABASE=sahsuland
	$(MAKE) clean v4_0_vacuum_analyse_dev PGDATABASE=sahsuland
	$(MAKE) -C test_scripts clean all PGDATABASE=sahsuland	
	$(MAKE) sahsuland.dump
ifneq ($(OS),Windows_NT)
ifneq ($(UNAME_S),Darwin)	
	-$(MAKE) -C ../../ERD dbms_tools
endif
endif	
	@echo "SAHSULAND and SAHSULAND_DEV setup completed OK"

#- ERD: remake ERD	
erd:
	$(MAKE) -C ../../ERD clean all
#
# Now has Node, ERD integration, pg_restore allowed to fail on Postgres 9.4.1
#
#- db_create: Create empty database
db_create: 
	-$(MAKE) clean
	$(MAKE) db_create.rpt PSQL_USER=postgres PGDATABASE=postgres DEBUG_LEVEL=1 ECHO=all


#
# Files to create/install
#
# pgpass: password file [not supported yet]
$(PGPASSFILE):
		$(error Please create $(PGPASSFILE))
		
# psqlrc: psql logon script
$(POSTGRES_ETC_DIR):
	@echo "Please create $(POSTGRES_ETC_DIR) as root/Administrator"
psqlrc:	../etc/psqlrc $(POSTGRES_ETC_DIR)
		$(COPY) ../etc/psqlrc $(POSTGRES_ETC_DIR)
#
# R library directory - need to be created aka POSTGRES_ETC_DIR
#
$(R_LIBRARY):
		$(error Please create $(R_LIBRARY))
$(R_SOURCE):
		$(error Please create $(R_SOURCE))
#
# $(ESC_POSTGRES_ETC_DIR): Create directory for psqlrc (requires privilege) - NEEDS TO BE FIXED
#		
$(ESC_POSTGRES_ETC_DIR):
		-$(SUDO_MKDIR)
		-$(SUDO_CHOWN_TESUSER)
		
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
