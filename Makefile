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
GRIP=python -m grip
WGET=wget
MKDIR=mkdir
ifeq ($(OS),Windows_NT)
	MAVEN=mvn
	COPY=cp
	DELETE=rm -f
	7ZIP="C:\Program Files\7-Zip\7z.exe"
else
#
# Linux macos support []
#
	MAVEN=mvn
	COPY=cp
	DELETE=rm -f
endif
# 
# Uncomment if you are having problems with the tests
#
#MAVEN_FLAGS=-Dmaven.test.skip=true
MAVEN_FLAGS=
# or use:
# make <target> MAVEN_FLAGS='-Dmaven.test.skip=true'
#

all:
	$(MAVEN) --version
	$(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) taxonomyServices/target/taxonomies.war .
	$(COPY) rifServices/target/rifServices.war .
	$(COPY) rifWebApplication/target/RIF40.war .
	$(COPY) rifDataLoaderTool\target\rifDataLoaderTool-jar-with-dependencies.jar rifDataLoaderTool-jar-with-dependencies.jar
	$(COPY) statsService/target/statistics.war .

#	$(COPY) rifITGovernanceTool\target\rifITGovernanceTool-jar-with-dependencies.jar rifITGovernanceTool-jar-with-dependencies.jar

#
# Will now work with Windows 7 (does not understand ";")
#
rifservice: 
	$(MAVEN) --version
	cd rifGenericLibrary && $(MAVEN) $(MAVEN_FLAGS) install
	cd rifServices && $(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) rifServices/target/rifServices.war .
	
RIF40: 
	$(MAVEN) --version
	cd rifWebApplication && $(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) rifWebApplication/target/RIF40.war .
	
statistics: 
	$(MAVEN) --version
	cd statsService && $(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) statsService/target/statistics.war .

RIF40install: RIF40
	$(COPY) RIF40.war "$(CATALINA_HOME)/webapps"
	
rifserviceinstall: rifservice
	$(COPY) rifServices.war "$(CATALINA_HOME)/webapps"
	
statsServiceInstall: statistics
	$(COPY) statistics.war "$(CATALINA_HOME)/webapps"
	
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

# docs: requires wget https://eternallybored.org/misc/wget/ and grep (Windows 10 has grep!)
broken_links: "docs\broken_links.txt"

"docs\broken_links.txt": 
	-$(WGET) --spider -r -nd -nv -H -l 2 -w 2 -o "docs\broken_links.log" https://smallareahealthstatisticsunit.github.io/rapidInquiryFacility/
	-grep -B1 'broken link!' "docs\broken_links.log" > "docs\broken_links.txt"
	
# docs: requires wget https://eternallybored.org/misc/wget/ and 7zip
"$(TMP)\rapidInquiryFacility\docs": 
	-$(MKDIR) "$(TMP)\rapidInquiryFacility"
	-$(MKDIR) "$(TMP)\rapidInquiryFacility\docs"
	
doc: "$(TMP)\rapidInquiryFacility\docs"
	$(COPY) "docs\source-documents\RIF_v40_Manual.pdf" "$(TMP)\rapidInquiryFacility\docs\RIF_v40_Manual.pdf"
	$(COPY) "docs\source-documents\RIF Data Loader Manual.pdf" "$(TMP)\rapidInquiryFacility\docs\RIF_Data_Loader_Manual.pdf"
	-$(WGET) --mirror --convert-links --page-requisites --no-parent -P "$(TMP)\rapidInquiryFacility\docs" https://smallareahealthstatisticsunit.github.io/rapidInquiryFacility/
	(cd "$(TMP)\\rapidInquiryFacility\\docs\\smallareahealthstatisticsunit.github.io" && $(7ZIP) a -r "$(TMP)\\rapidInquiryFacility\\docs.7z" "rapidInquiryFacility\\*")
	$(7ZIP) l "$(TMP)\\rapidInquiryFacility\\docs.7z"
	
taxonomyservice:	
	$(MAVEN) --version
	cd rifGenericLibrary && $(MAVEN) $(MAVEN_FLAGS) clean
	cd taxonomyServices && $(MAVEN) $(MAVEN_FLAGS) clean
	cd rifGenericLibrary && $(MAVEN) $(MAVEN_FLAGS) install
	cd taxonomyServices && $(MAVEN) $(MAVEN_FLAGS) install
	$(COPY) taxonomyServices/target/taxonomies.war .

taxonomyserviceinstall: taxonomyservice
	$(COPY) taxonomies.war "$(CATALINA_HOME)/webapps"
	
#
# Does NOW install RIF40.war
#	
install: clean all
	$(COPY) RIF40.war "$(CATALINA_HOME)/webapps"
	$(COPY) rifServices.war "$(CATALINA_HOME)/webapps"
	$(COPY) taxonomies.war "$(CATALINA_HOME)/webapps"
	$(COPY) statistics.war "$(CATALINA_HOME)/webapps"
	
clean: 
	$(MAVEN) --version
	cd rifGenericLibrary && $(MAVEN) $(MAVEN_FLAGS) clean
	cd taxonomyServices && $(MAVEN) $(MAVEN_FLAGS) clean
	$(MAVEN) clean
	$(DELETE) taxonomies.war rifServices.war RIF40.war RIF4.7z
	
#
# Eof
