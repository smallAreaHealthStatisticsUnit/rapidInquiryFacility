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
# Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
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
SUBDIRS = alter_scripts test_scripts
#
# Environment variables - set to sahsuland_dev
#
PGDATABASE=sahsuland_dev
TESTUSER?=$(USERNAME)

#
# Programs and parameters
#
WINDOWS_RUN_SCRIPT=run.ps1
LINUX_RUN_SCRIPT=run.sh
PSQL=psql
PSQL_FLAGS=-U rif40 -d $(PGDATABASE) -w -e -P pager=off -v testuser=$(TESTUSER)

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
	
#
# Targets
# 
#- all: Run all completed alter scripts [DEFAULT]
all:
	$(MAKE) -C alter_scripts all

#- patch: Run all completed alter scripts on both sahsuland_dev and sahusland	
patch:
	$(MAKE) -C alter_scripts clean
	$(MAKE) -C alter_scripts all
	$(MAKE) -C alter_scripts clean
	$(MAKE) -C alter_scripts all PGDATABASE=sahsuland
	
#- sahsuland_dev: Rebuild sahsuland_dev, then patch dev only
sahsuland_dev: clean v4_0_create_sahsuland.rpt test
	$(MAKE) -C alter_scripts clean
#	$(MAKE) -C alter_scripts all
# Add UK gis
	
#- test: Run test scripts
test:
	$(MAKE) -C test_scripts
	
#- dev: Run all alter scripts in development
dev:
	$(MAKE) -C alter_scripts dev
#- clean: Remove logs so completed scripts can be re-run
clean:
	$(MAKE) -C alter_scripts clean
	$(MAKE) -C test_scripts clean
	$(DELETE) v4_0_create_sahsuland.rpt
#- devclean: Remove logs so alter scripts in development can be r-run 
#-           Not normally needed as they abort.
devclean:
	$(MAKE) -C alter_scripts devclean

#
# Recursive make target: make recurse <recursive tsrget>
#
recurse: $(SUBDIRS)	
$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

#
# Eof