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
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\ERD
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
-include ../psql_scripts/Makefile.local

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
	TESTUSER=$(shell echo $(USERNAME) | tr �[:upper:]� �[:lower:]�)
	ifeq ($(TESTUSER),)
		TESTUSER=$(USERNAME)
	endif
#	TOPOJSON=C:\Users\$(USERNAME)\AppData\Roaming\npm\topojson.cmd
	TOPOJSON=node_modules\topojson\bin\topojson
	NODE=node
	NPM=npm
	PGPASSFILE=$(APPDATA)\postgresql\pgpass.conf
#
# This makefile will not work on Windows
# 
#	$(error SORRY - NEEDS LINUX	: Unsupported OS: $(OS); uname -s: $(UNAME_S))
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
	TESTUSER=$(shell id -un | tr �[:upper:]� �[:lower:]�)
	TOPOJSON=topojson
	NODE=node
	NPM=npm
	RIF40_PASSWORD=$(shell grep rif40 ~/.pgpass | awk -F":" '{print $$5}')
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
#
#
# Target extension depends on source, build rule
#
#.SUFFIXES: .sql .rpt
#.sql.rpt:
#	$(RUN) $@ "$(CURDIR)" $(PSQL) $(PSQL_FLAGS) -f $< 

#- all: build tool chain, ERD document database
all: dbms_tools rif40/index-sahsuland-postgres8.html sahsuland/index-sahsuland-postgres8.html rif_studies/index-sahsuland-postgres8.html

#- clean: remove ERD and documentation
clean: 
	rm -f ../Postgres/install/sahsuland.xml
	rm -f sahsuland.xml rif40.xml
	rm -rf docs/*
	rm -rf rif40/*
	rm -rf sahsuland/*

#- veryclean: remove dbmstools tool chain
veryclean:
	-rm -f dbmstools-0.4.5rc1.zip dbmstools-0.4.5rc1/jdbc-drivers/postgresql-9.4-1201.jdbc41.jar
	-rm -rf dbmstools-0.4.5rc1
	
#- dbmstools ERD documenter; rubbish, old, unsupported, unmaintainer but the best yet found
dbms_tools: dbmstools-0.4.5rc1.zip
dbmstools-0.4.5rc1.zip:
#
# This makefile will not work on Windows
# 
ifeq ($(OS),Windows_NT)
	$(error SORRY - NEEDS LINUX	: Unsupported OS: $(OS);)

	wget http://sourceforge.net/projects/dbmstools/files/dbmstools/0.4.5rc1/dbmstools-0.4.5rc1.zip
	unzip dbmstools-0.4.5rc1.zip
	(cd dbmstools-0.4.5rc1/jdbc-drivers; wget https://jdbc.postgresql.org/download/postgresql-9.4-1201.jdbc41.jar)
endif
	
#- sahusland.xml: ERD model of database
#- Note use of sed to fix the output into valid XML; there is a lot of specific cleaning, specifically:
#
# View definitions needs CDATA
# current_user() is not handled correctly
# The usual description substitutions (& => &amp; etc). These get very specific in the sahusland example data and you cannot use CDATA in a description field
# Functional indexes blow the program and are removed
#
sahsuland.xml:
	./dbmstools-0.4.5rc1/main/dbmsjy.py db2xml.py -j jdbc:postgresql://$(PG_HOPST):5432/sahsuland\?user=rif40\&password=$(RIF40_PASSWORD) -d postgres8 -s rif_data -g -v | \
		sed "s/\&/\&amp;/g" | \
		sed "s/\"\"current_user\"()\"/\"current_user()\"/g" | \
		sed "/column name\=/ s/>=/\&gt;\=/g" | \
		sed "/column name\=/ s/<=/\&lt;\=/g" | \
		sed "/column name\=/ s/: </: \&lt;/g" | \
		sed "/column name\=/ s/>_</\&gt;_\&lt;/g" | \
		sed "/column name\=/ s/<zoomlevel>/\&lt;zoomlevel\&gt;/g" | \
		sed "/column name\=/ s/<zoom level>/\&lt;zoomlevel\&gt;/g" | \
		sed "/column name\=/ s/tile number>/tile number\&gt;/g" | \
		sed "/column name\=/ s/format <geography/format \&lt;geography/g" | \
		sed "/<index name=\"sahsuland_cancer_age_group\"/,+2 d" | \
		sed "/<index name=\"sahsuland_cancer_sex\"/,+2 d" | \
		sed "s/<view-definition>/<view-definition><\![CDATA["/g | \
		sed "s/<\/view-definition>/]]><\/view-definition>/g" > sahsuland.xml
rif40.xml:
	./dbmstools-0.4.5rc1/main/dbmsjy.py db2xml.py -j jdbc:postgresql://$(PG_HOPST):5432/sahsuland\?user=rif40\&password=$(RIF40_PASSWORD) -d postgres8 -s rif40 -g -v | \
		sed "s/\&/\&amp;/g" | \
		sed "s/\"\"current_user\"()\"/\"current_user()\"/g" | \
		sed "s/<view-definition>/<view-definition><\![CDATA["/g | \
		sed "s/<\/view-definition>/]]><\/view-definition>/g" > rif40.xml
rif_studies.xml:
	./dbmstools-0.4.5rc1/main/dbmsjy.py db2xml.py -j jdbc:postgresql://$(PG_HOPST):5432/sahsuland\?user=rif40\&password=$(RIF40_PASSWORD) -d postgres8 -s rif_studies -g -v | \
		sed "s/\&/\&amp;/g" | \
		sed "s/\"\"current_user\"()\"/\"current_user()\"/g" | \
		sed "s/<view-definition>/<view-definition><\![CDATA["/g | \
		sed "s/<\/view-definition>/]]><\/view-definition>/g" > rif_studies.xml

#- docs/index-sahsuland-postgres8.html: Documentation target WARNING: Needs graphviz
sahsuland/index-sahsuland-postgres8.html: sahsuland.xml
	./dbmstools-0.4.5rc1/main/xml2doc.py -d postgres8 -f -v -g -w -o sahsuland sahsuland.xml
rif40/index-sahsuland-postgres8.html: rif40.xml
	./dbmstools-0.4.5rc1/main/xml2doc.py -d postgres8 -f -v -g -w -o rif40 rif40.xml
rif_studies/index-sahsuland-postgres8.html: rif_studies.xml
	./dbmstools-0.4.5rc1/main/xml2doc.py -d postgres8 -f -v -g -w -o rif_studies rif_studies.xml

#
# Eof
