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
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness
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
-include ../../Postgres/psql_scripts/Makefile.local

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
DEFAULT_DEBUG_LEVEL=1
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
	-v testuser=$(TESTUSER) -v verbosity=$(VERBOSITY) -v debug_level=$(DEBUG_LEVEL) -v echo=$(ECHO) \
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
.PHONY: all
 
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
	TESTUSER=$(shell echo $(USERNAME) | tr ‘[:upper:]’ ‘[:lower:]’)
	ifeq ($(TESTUSER),)
		TESTUSER=$(USERNAME)
	endif
#	TOPOJSON=C:\Users\$(USERNAME)\AppData\Roaming\npm\topojson.cmd
	TOPOJSON=Node node_modules\topojson\bin\topojson
	NODE=node
	NPM=npm
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
	TESTUSER=$(shell id -un | tr ‘[:upper:]’ ‘[:lower:]’)
	TOPOJSON=node_modules/topojson/bin/topojson
	NODE=node
	NPM=npm
    ifeq ($(UNAME_S),Linux)
		OS=$(UNAME_S)
#		POSTGRES_ETC_DIR=/var/opt/PostgreSQL/9.3/etc
		PGPASS=./pgpass.sh
	else
		ifeq ($(UNAME_S),Darwin)	
			OS=$(UNAME_S)
#			POSTGRES_ETC_DIR=/Library/PostgreSQL/9.3/etc/posgresql
			PGPASS=./pgpass.sh
			TOPOJSON=/usr/local/bin/topojson
		else
#
# Unsupported OS
#
			$(error Unsupported OS: $(OS); uname -s: $(UNAME_S))
		endif
	endif
	PGPASSFILE=~/.pgpass
endif
#
# Module list, module package.json for dependency control
#
MODULES=node_modules/pg node_modules/pg-native node_modules/optimist node_modules/mutexjs
MODULE_JSON=node_modules/pg-native/package.json node_modules/pg/package.json node_modules/optimist/package.json node_modules/mutexjs/package.json

#- all: Build modules, run the complete database test harness	
all: modules update db_test_harness

#- modules: Build required Node.js modules using npm install --save to update dependencies in package.json
modules: $(MODULE_JSON) $(MODULES)

# Module dependency control	
$(MODULE_JSON):
	-$(NPM) update --save pg pg-native optimist mutexjs	
$(MODULES):
	-mkdir $@
	
#clean:
#	 $(DELETE) node_modules
	 
# No install target (dummy)	 
install:
#- update: Update required Node.js modules using npm install --save to update dependencies in package.json 
update:
	-$(NPM) update --save pg pg-native optimist mutexjs	
	
#- db_test_harness: Run the complete database test harness	(same as target test)	
db_test_harness:
	$(NODE) db_test_harness.js -H $(PGHOST) -D $(PGDATABASE) -U $(TESTUSER) -d $(DEBUG_LEVEL)
	
#- rif40_create_disease_mapping_example: Run rif40_create_disease_mapping_example test (standard test 1)
rif40_create_disease_mapping_example:
	$(NODE) db_test_harness.js -H $(PGHOST) -D $(PGDATABASE) -U $(TESTUSER) -d $(DEBUG_LEVEL) -C rif40_create_disease_mapping_example	

#- test: Run the complete database test harness	
test: db_test_harness
#- retest: Re-run the failed database test harness tests (-F flag)
retest:
	$(NODE) db_test_harness.js -H $(PGHOST) -D $(PGDATABASE) -U $(TESTUSER) -d $(DEBUG_LEVEL) -F	
	

#- help: Display makefile help, db_test_harness.js help
help:
	$(HELP)
	$(NODE) db_test_harness.js --help
	
#
# Eof
