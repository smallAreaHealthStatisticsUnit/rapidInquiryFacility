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
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifBuild
#-
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
TARGETS = ..\..\rifServices\target\rifServices-0.0.1-SNAPSHOT.war
#
# Run bash on Linux, Powershell on Windows_NT
#
OS?=Unknown
ifeq ($(OS),Windows_NT)
#
# Windows support
#
	RUN=powershell -ExecutionPolicy ByPass -file Scripts/run.ps1
	COPY=powershell -ExecutionPolicy ByPass -file Scripts/copy.ps1 
	DELETE=powershell -ExecutionPolicy ByPass -file Scripts/delete.ps1
	RENAME=powershell -ExecutionPolicy ByPass -file Scripts/rename.ps1	
	INSTALL=powershell -ExecutionPolicy ByPass -file Scripts/install.ps1	
	HELP=findstr "\#-" Makefile
else
#
# Linux and Macos support
#
	RUN=./run.sh
	HELP=grep "\#-" Makefile
	COPY=cp
	DELETE=rm -f
	RENAME=mv -f
	INSTALL=sudo Scripts/install.sh
endif
#
#- all: Build JavaMiddleware
all: 
	$(MAKE) -C JavaMiddleware
	$(MAKE) -C rifWebPlatform 
	
#- test: Regression test JavaMiddleware
test:
	$(MAKE) -C JavaMiddleware test
	
#- clean: Cleanup JavaMiddleware	
clean:
	$(MAKE) -C JavaMiddleware clean 
	$(MAKE) -C rifWebPlatform clean
	
#
#- help: Display this help
help:
	$(HELP)
	
#- install: Install JavaMiddleware, to be added: Java utilities
install: 
	$(MAKE) -C JavaMiddleware install
	$(MAKE) -C rifWebPlatform install	
	
#
# Eof
