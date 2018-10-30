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
# Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts\alter_scripts
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
-include ../Makefile.local
#
# Conditional environment variables - can be changed to point to sahsuland by higher maskefiles
#
PGDATABASE?=sahsuland_dev
PGHOST?=localhost
DBNAME?=$(PGDATABASE)
VERBOSITY?=$(DEFAULT_VERBOSITY)
#
DEFAULT_DEBUG_LEVEL=0
ifndef DEBUG_LEVEL
DEBUG_LEVEL?=$(DEFAULT_DEBUG_LEVEL)
$(info Debug level set to default: $(DEFAULT_DEBUG_LEVEL))
else
$(info Debug level set to: $(DEBUG_LEVEL))
endif
DEFAULT_ECHO=none
ECHO?=$(DEFAULT_ECHO)
PSQL_USER?=$(DEFAULT_PSQL_USER)
USE_PLR?=$(DEFAULT_USE_PLR)

#
# Programs and parameters
#
PSQL=psql
PSQL_FLAGS=-U rif40 -d $(DBNAME) -w -e -P pager=off \
	-v testuser=$(TESTUSER) \
	-v verbosity=$(VERBOSITY) \
	-v debug_level=$(DEBUG_LEVEL) \
	-v use_plr=$(USE_PLR) \
	-v pghost=$(PGHOST) \
	-v echo=$(ECHO)

#
# PL/R dev/prod sources, if enabled
#
ifeq ($(USE_PLR),Y)
	PLR_DEV_SOURCES=v4_0_alter_6.sql
	PLR_SOURCES=
else
	PLR_DEV_SOURCES=
	PLR_SOURCES=
endif

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
	CLEAN_USERNAME=$(shell echo $(USERNAME) | tr ‘[:upper:]’ ‘[:lower:]’)
	TESTUSER?=$(CLEAN_USERNAME)
	ifeq ($(TESTUSER),)
		TESTUSER=$(USERNAME)
	endif
else
#
# Linux macos support
#
	UNAME_S := $(shell uname -s)
	RUN=../run.sh
	DELETE=rm -f
	CLEAN_USERNAME=$(shell id -un | tr ‘[:upper:]’ ‘[:lower:]’)
	TESTUSER?=$(CLEAN_USERNAME)
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
# Alter script 4 must go after 7 - Support for ontologies (e.g. ICD9, 10); removed previous table based support.
#								    Modify t_rif40_inv_conditions to remove SQL injection risk
#
# This is because 7 was written before the partitioning was enabled and does not support it
#

#
# Partitioning is not required on sahsuland_empty
#
ifeq ($(PGDATABASE),sahsuland_empty)
PARTITIONING_SOURCES=
else
PARTITIONING_SOURCES=
# Issue with new dataLoader - will become script
# PARTITIONING_SOURCES=v4_0_alter_3.sql v4_0_alter_4.sql
endif
SOURCES=v4_0_alter_1.sql v4_0_alter_2.sql v4_0_alter_5.sql $(PLR_SOURCES) v4_0_alter_7.sql v4_0_alter_8.sql v4_0_alter_9.sql \
		v4_0_alter_10.sql v4_0_alter_11.sql \
		 $(PARTITIONING_SOURCES)
 
# DEV_SOURCES=v4_0_alter_4.sql  

# DEV_SOURCES=$(PLR_DEV_SOURCES) 
DEV_SOURCES=$(PLR_DEV_SOURCES)
#
# Derive targets from sources
# Target extension depends on source, build rule
#
DEV_TARGETS2=$(DEV_SOURCES:%.sql=)
#
# Phony (i.e. not a file) targets
#
.PHONY: all dev clean devclean recurse $(DEV_TARGETS2)

ifeq ($(PGDATABASE),sahsuland)
#
TARGETS=$(SOURCES:%.sql=%.sahsuland_rpt)
DEV_TARGETS=$(DEV_SOURCES:%.sql=%.sahsuland_rpt)
.SUFFIXES: .sql .sahsuland_rpt
.sql.sahsuland_rpt:
	$(RUN) "$(CURDIR)/$@" "$(CURDIR)/.." $(PSQL) $(PSQL_FLAGS) -f alter_scripts/$< 
#
$(info Database is: $(PGDATABASE); testuser: $(TESTUSER);  targets: $(TARGETS)) 	
#	
else
ifeq ($(PGDATABASE),sahsuland_empty)
#
TARGETS=$(SOURCES:%.sql=%.sahsuland_empty_rpt)
DEV_TARGETS=$(DEV_SOURCES:%.sql=%.sahsuland_empty_rpt)
.SUFFIXES: .sql .sahsuland_empty_rpt
.sql.sahsuland_empty_rpt:
	$(RUN) "$(CURDIR)/$@" "$(CURDIR)/.." $(PSQL) $(PSQL_FLAGS) -f alter_scripts/$< 	
#
$(info Database is: $(PGDATABASE); testuser: $(TESTUSER);  targets: $(TARGETS))  	
#
else
ifeq ($(PGDATABASE),sahsuland_dev)
#
TARGETS=$(SOURCES:%.sql=%.sahsuland_dev_rpt)
DEV_TARGETS=$(DEV_SOURCES:%.sql=%.sahsuland_dev_rpt)
.SUFFIXES: .sql .sahsuland_dev_rpt
.sql.sahsuland_dev_rpt:
	$(RUN) "$(CURDIR)/$@" "$(CURDIR)/.." $(PSQL) $(PSQL_FLAGS) -f alter_scripts/$< 	
#
$(info Database is: $(PGDATABASE); testuser: $(TESTUSER);  targets: $(TARGETS))  	
#
else
$(error Unknown database: $(PGDATABASE))
#
#TARGETS=$(SOURCES:%.sql=%.rpt)
#DEV_TARGETS=$(DEV_SOURCES:%.sql=%.rpt)	
#.SUFFIXES: .sql .rpt
#.sql.rpt:
#	$(RUN) "$(CURDIR)/$@" "$(CURDIR)/.." $(PSQL) $(PSQL_FLAGS) -f alter_scripts/$< 		
#	
endif
endif
endif
		
#
# Targets
#
#- all: Run all completed alter scripts [DEFAULT]
all: node_check $(TARGETS)
#- dev: Run all alter scripts in development
dev: node_check $(DEV_TARGETS)
#- clean: Remove logs so completed scripts can be re-run
clean: 
	$(DELETE) $(TARGETS)
	$(MAKE) -C ../../Node clean
#- devclean: Remove logs so alter scripts in development can be re-run 
#-           Not normally needed as they abort.
devclean: 
	$(DELETE) $(DEV_TARGETS)

#- topojson_convert: GeoJSON to topoJSON converter
topojson_convert:
	$(MAKE) -C ../../Node topojson_convert
	
# Do nothing
recurse:

node_check: 
		$(MAKE) -C ../../Node node_check
#
# So you can type make v4_0_alter_5
#
$(DEV_TARGETS2): $(DEV_TARGETS)

#
# Eof