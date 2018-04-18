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
MAVEN_FLAGS=-Dmaven.test.skip=true
GRIP=python -m grip
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

all: RIF4
	$(MAVEN) --version
	$(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) taxonomyServices/target/taxonomyServices.war .
	$(COPY) rifServices/target/rifServices.war .
	$(COPY) rifDataLoaderTool\target\rifDataLoaderTool-jar-with-dependencies.jar rifDataLoaderTool-jar-with-dependencies.jar
#	$(COPY) rifITGovernanceTool\target\rifITGovernanceTool-jar-with-dependencies.jar rifITGovernanceTool-jar-with-dependencies.jar

#
# Will now work with Windows 7 (does not understand ";")
#
rifservice: 
	$(MAVEN) --version
	cd rifGenericLibrary && $(MAVEN) $(MAVEN_FLAGS) install
	cd rifServices && $(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) rifServices/target/rifServices.war .
	
dataloader: 
	$(MAVEN) --version
	cd rifDataLoaderTool && $(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) rifDataLoaderTool\target\rifDataLoaderTool-jar-with-dependencies.jar rifDataLoaderTool-jar-with-dependencies.jar

#
# Not built yet
#	
itgovernancetool: 
	$(MAVEN) --version
	cd rifITGovernanceTool && $(MAVEN) $(MAVEN_FLAGS) install
#	$(COPY) rifITGovernanceTool\target\rifITGovernanceTool-jar-with-dependencies.jar rifITGovernanceTool-jar-with-dependencies.jar
	
# docs: requires grip (https://github.com/joeyespo/grip) to be installed
doc: 
	$(GRIP) rifWebApplication\Readme.md --export docs\RIF_Web_Application_Installation.html
	$(GRIP) rifDatabase\Postgres\production\windows_install_from_pg_dump.md --export docs\RIF_Postgres_Install.html
	$(GRIP) rifDatabase\SQLserver\production\INSTALL.md --export docs\RIF_SQLserver_Install.html
	$(GRIP) rifDatabase\DataLoaderData\DataLoading.md --export docs\RIF_manual_data_loading.html
	$(COPY) "Documentation\RIF v4 0 Manual.pdf" "docs\RIF_v40_Manual.pdf"
	$(COPY) "Documentation\RIF Data Loader Manual.pdf" "docs\RIF_Data_Loader_Manual.pdf"
	$(GRIP) docs\README.md --export docs\index.html
	$(7ZIP) a -r docs.7z "docs\\*"
	$(7ZIP) l docs.7z
	
RIF4: 
	cd rifWebApplication/src/main/webapp/WEB-INF && $(7ZIP) a -r ../../../../../RIF4.7z *
	$(7ZIP) l RIF4.7z
	
taxonomyservice:	
	$(MAVEN) --version
	cd rifGenericLibrary && $(MAVEN) $(MAVEN_FLAGS) install
	cd taxonomyServices && $(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) taxonomyServices/target/taxonomyServices.war .
	
#
# Does NOT install RIF4.7z
#	
install: clean all
	$(COPY) rifServices.war "$(CATALINA_HOME)/webapps"
	$(COPY) taxonomyServices.war "$(CATALINA_HOME)/webapps"

clean: 
	$(MAVEN) --version
	$(MAVEN) clean
	$(DELETE) taxonomyServices.war rifServices.war RIF4.7z
#
# Eof