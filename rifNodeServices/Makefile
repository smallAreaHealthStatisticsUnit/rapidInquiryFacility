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
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifNodeServices
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
-include ../rifDatabase/Postgres/psql_scripts/Makefile.local

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
ifneq (,)
This makefile requires GNU Make.
endif
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
	FOREVER=$(APPDATA)\npm\forever
	NPM=npm
	PGPASSFILE=$(APPDATA)\postgresql\pgpass.conf
	MKDIR=mkdir
	RM=rm -f
else
#
# Linux macos support
#
	UNAME_S := $(shell uname -s)
	RUN=./run.sh
	HELP=grep "\#-" Makefile
	SUDO_MKDIR=sudo mkdir -p "$@"
	SUDO_CHOWN_TESUSER=chown -R $(TESTUSER) "$@"
	SUDO_CHOWN_POSTGRES=chown -R postgres "$@"
	COPY=cp
	TESTUSER=$(shell id -un | tr ‘[:upper:]’ ‘[:lower:]’)
	TOPOJSON=node_modules/topojson/bin/topojson
	NODE=node
	NPM=npm
	FOREVER=~/npm/forever
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
	MKDIR=mkdir
	RM=rm -f
endif
#
# Module list, module package.json for dependency control
#
# Now needs C++ V1.1 compliant compiler - MS VS 2010 is not...	
#	node_modules/pg-native/package.json \ 
#
MODULE_JSON=node_modules/request/package.json \
	node_modules/JSZip/package.json \
	node_modules/form-data/package.json \
	node_modules/magic-globals/package.json \
	node_modules/helmet/package.json \
	node_modules/pg/package.json \
	node_modules/pg-native/package.json \
	node_modules/msnodesqlv8/package.json \
	node_modules/srs/package.json \
	node_modules/xml2js/package.json \
	node_modules/async/package.json \
	node_modules/reproject/package.json \
	node_modules/mapshaper/package.json \
	node_modules/forever/package.json \
	node_modules/shapefile/package.json \
	node_modules/node-uuid/package.json \
	node_modules/chroma-js/package.json \
	node_modules/jszip/package.json \
	node_modules/express/package.json \
	node_modules/morgan/package.json \
	node_modules/topojson/package.json \
	node_modules/request-debug/package.json \
	node_modules/cjson/package.json \
	node_modules/geojson2wkt/package.json \
	node_modules/connect-busboy/package.json
#
MODULES=$(patsubst %/package.json,%,$(MODULE_JSON))
MODULE_LIST=$(patsubst node_modules/%/package.json,%,$(MODULE_JSON))
#
# Forever (NPM daemon control)
#
FOREVER=node_modules\\forever\\bin\\forever
NODE_MAX_MEMORY?=4096
FOREVER_OPTIONS=--max-old-space-size=$(NODE_MAX_MEMORY) --expose-gc
FOREVER_VERBOSE=-verbose
FOREVER_LOGGING=-l forever.log -e forever.err -o forever.log --append

NODE_DEBUG=request,form-data,busboy,connect-busboy
NODE_DEBUG_DEBUG=express:*

#combined-stream,mime-types,fs
#- all: Build modules, run the complete database test harness	
all: modules update
# rifNode

#forever: $(FOREVER)
forever:

#$(FOREVER):
#	$(NPM) install -g forever
	
#- modules: Build required Node.js modules using npm install --save to update dependencies in package.json
modules: $(MODULES) $(MODULE_JSON) 

# Module dependency control	
$(MODULE_JSON):
	$(NPM) update --save $(MODULE_LIST)
	
$(MODULES):
	-$(MKDIR) $@
	-$(NPM) install --save $(patsubst node_modules/%,%,$@)
	
#- clean: clean Node modules; avoid Windows path length stupidities with rimraf	
clean:
	 $(NPM) install rimraf 
	 $(NODE) node_modules\rimraf\bin.js $(MODULES)
	 
#- install:  No install target (dummy)	 
install:

#- server-start: start server
server-start: forever expressServer.js
	$(FOREVER) start -c "node $(FOREVER_OPTIONS)" $(FOREVER_VERBOSE) $(FOREVER_LOGGING) ./expressServer.js
#- server-restart: restart server
server-restart: forever expressServer.js
	-$(RM) forever.err forever.log
	-$(FOREVER) stop $(FOREVER_VERBOSE) $(FOREVER_LOGGING) ./expressServer.js
	$(FOREVER) start -c "node $(FOREVER_OPTIONS)" $(FOREVER_VERBOSE) $(FOREVER_LOGGING) ./expressServer.js
#- server-restart-debug: restart server with debugging
server-restart-debug: 
	$(MAKE) -e NODE_DEBUG=$(NODE_DEBUG) -e DEBUG=$(NODE_DEBUG_DEBUG) server-restart
#- server-status: status of running server
server-status: forever expressServer.js
	$(FOREVER) list $(FOREVER_VERBOSE) ./expressServer.js
#- server-stop: stop server
server-stop: forever expressServer.js
	$(FOREVER) stopall $(FOREVER_VERBOSE) ./expressServer.js
#- server-log: display server logs
server-logs: forever expressServer.js
	$(FOREVER) logs $(FOREVER_VERBOSE) ./expressServer.js 
	$(FOREVER) logs 0 $(FOREVER_VERBOSE) ./expressServer.js 

#- test\make_bighelloworld.js: create >2G data\bighelloworld.js	
data\bighelloworld.js:
	$(NODE) test\make_bighelloworld.js
		
#- update: Update required Node.js modules using npm install --save to update dependencies in package.json 
update:
	-$(NPM) update --save $(MODULE_LIST)	
		
#- test: Run the test harness	
test: forever.err modules update server-restart
	$(NPM) test
	
#- help: Display makefile help, rifNode.js help
help:
	$(HELP)
	
#
# Eof
