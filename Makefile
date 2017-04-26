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
#- Rapid Enquiry Facility (RIF) - Makefile for Middleware
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

OS?=Unknown
ifeq ($(OS),Windows_NT)
	MAVEN=mvn
	7ZIP="C:\Program Files\7-Zip\7z.exe"
	COPY=cp
	DELETE=rm -f
else
#
# Linux macos support []
#
	MAVEN=mvn
	7ZIP="C:\Program Files\7-Zip\7z.exe"
	COPY=cp
	DELETE=rm -f
$(error 7ZIP unsupported: $(7ZIP))
endif

all: rifservice RIF4 taxonomyservice

rifservice: rifServices.war
rifServices.war:
	$(MAVEN) --version
	cd rifGenericLibrary; $(MAVEN) -Dmaven.test.skip=true install
	cd rapidInquiryFacility ; $(MAVEN) -Dmaven.test.skip=true install
	cd rifServices ; $(MAVEN) -Dmaven.test.skip=true install
	$(COPY) rifServices/target/rifServices.war .
	
RIF4: RIF4.7z
RIF4.7z:
	cd rifWebApplication/src/main/webapp/WEB-INF; $(7ZIP) a ../../../../../RIF4.7z *
	$(7ZIP) l RIF4.7z
	
taxonomyservice: taxonomyServices.war
taxonomyServices.war:	
	cd taxonomyServices ; $(MAVEN) -Dmaven.test.skip=true install
	$(COPY) taxonomyServices/target/taxonomyServices.war .
	
install: clean all

clean: 
	cd rapidInquiryFacility ; $(MAVEN) clean
	cd rifGenericLibrary; $(MAVEN) clean
	cd rifServices; $(MAVEN) clean
	cd taxonomyServices; $(MAVEN) clean
	$(DELETE) taxonomyServices.war rifServices.war RIF4.7z
#
# Eof