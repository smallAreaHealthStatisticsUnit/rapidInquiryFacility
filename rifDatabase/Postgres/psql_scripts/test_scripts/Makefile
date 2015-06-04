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
# Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts\test_scripts
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
-include Makefile.local
#
# Conditional environment variables - can be changed to point to sahsuland by higher maskefiles
#
PGDATABASE?=sahsuland_dev
PGHOST?=localhost
DEFAULT_VERBOSITY=terse
DEFAULT_DEBUG_LEVEL=0
DEFAULT_ECHO=none
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

#
# Run bash on Linux, Powershell on Windows_NT
#
OS?=Unknown
ifeq ($(OS),Windows_NT)
#
# Windows support
#
	RUN=powershell -ExecutionPolicy ByPass -file ../run.ps1
	DELETE=powershell -ExecutionPolicy ByPass -file ../delete.ps1
	# Assumes tr came with make. Force to lowercase
	TESTUSER=$(shell echo $(USERNAME) | tr ‘[:upper:]’ ‘[:lower:]’)
	ifeq ($(TESTUSER),)
		TESTUSER=$(USERNAME)
	endif
	TOPOJSON="C:\node_modules\.bin\topojson.cmd"
else
#
# Linux macos support
#
	UNAME_S := $(shell uname -s)
	RUN=../run.sh
	DELETE=rm -f
	TESTUSER=$(shell id -un | tr ‘[:upper:]’ ‘[:lower:]’)
        ifeq ($(UNAME_S),Linux)
	else
		ifeq ($(UNAME_S),Darwin)
		else
#
# Unsupported OS
#
			$(error Unsupported OS: $(OS); uname -s: $(UNAME_S))
		endif
	endif
endif

#
# Programs and parameters
#
PSQL=psql
PSQL_FLAGS=-U rif40 -d $(DBNAME) -h $(PGHOST) -w -P pager=off \
	-v testuser=$(TESTUSER) -v verbosity=$(VERBOSITY) -v debug_level=$(DEBUG_LEVEL) -v echo=$(ECHO) \
	-v pghost=$(PGHOST)
	

#
# List of source code
#
NO_ALTER_SOURCES=test_1_sahsuland_geography.sql test_2_ddl_checks.sql test_3_user_setup.sql
SOURCES=$(NO_ALTER_SOURCES) test_6_middleware_1.sql test_4_study_id_1.sql test_5_clone_delete_test.sql test_7_middleware_2.sql

#
# Derive targets from sources
# Phony (i.e. not a file) targets
#
.PHONY: all clean recurse
ifeq ($(PGDATABASE),sahsuland)
#
TARGETS=$(SOURCES:%.sql=%.sahsuland_rpt)
NO_ALTER_TARGETS=$(NO_ALTER_SOURCES:.sql=.sahsuland_rpt)
.SUFFIXES: .sql .sahsuland_rpt
.sql.sahsuland_rpt:
		$(RUN) "$(CURDIR)/$@" "$(CURDIR)/.." $(PSQL) $(PSQL_FLAGS) -f test_scripts/$< 
#
$(info Database is: sahsuland; targets: $(TARGETS); no_alter targets: $(NO_ALTER_TARGETS)) 	
#	
else
ifeq ($(PGDATABASE),sahsuland_dev)
#
TARGETS=$(SOURCES:%.sql=%.sahsuland_dev_rpt)
NO_ALTER_TARGETS=$(NO_ALTER_SOURCES:.sql=.sahsuland_dev_rpt)
.SUFFIXES: .sql .sahsuland_dev_rpt
.sql.sahsuland_dev_rpt:
	$(RUN) "$(CURDIR)/$@" "$(CURDIR)/.." $(PSQL) $(PSQL_FLAGS) -f test_scripts/$< 
#
$(info Database is: sahsuland_dev;  targets: $(TARGETS); no_alter targets: $(NO_ALTER_TARGETS)) 		
#	
else
$(error Unknown database: $(PGDATABASE))
#
#TARGETS=$(SOURCES:%.sql=%.rpt)
#NO_ALTER_TARGETS=$(NO_ALTER_SOURCES:.sql=.rpt)
#.SUFFIXES: .sql .rpt
#.sql.rpt:
#		$(RUN) "$(CURDIR)/$@" "$(CURDIR)/.." $(PSQL) $(PSQL_FLAGS) -f test_scripts/$< 	
#	
endif
endif	
	
#
# Targets
#
#- all: Run all test scripts [DEFAULT]
all: $(TARGETS)
#- no_alter:
no_alter: $(NO_ALTER_TARGETS)
#- clean: Remove run logs
clean: 
	$(DELETE) $(TARGETS)

# Do nothing
recurse:

#
# Eof