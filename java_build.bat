ECHO OFF
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - Build RIF web serices and dataloader using Maven
REM
REM Copyright:
REM
REM The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
REM that rapidly addresses epidemiological and public health questions using 
REM routinely collected health and population data and generates standardised 
REM rates and relative risks for any given health outcome, for specified age 
REM and year ranges, for any given geographical area.
REM
REM Copyright 2014 Imperial College London, developed by the Small Area
REM Health Statistics Unit. The work of the Small Area Health Statistics Unit 
REM is funded by the Public Health England as part of the MRC-PHE Centre for 
REM Environment and Health. Funding for this project has also been received 
REM from the Centers for Disease Control and Prevention.  
REM
REM This file is part of the Rapid Inquiry Facility (RIF) project.
REM RIF is free software: you can redistribute it and/or modify
REM it under the terms of the GNU Lesser General Public License as published by
REM the Free Software Foundation, either version 3 of the License, or
REM (at your option) any later version.
REM
REM RIF is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
REM GNU Lesser General Public License for more details.
REM
REM You should have received a copy of the GNU Lesser General Public License
REM along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
REM to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
REM Boston, MA 02110-1301 USA
REM
REM Author:
REM
REM Margaret Douglass, Peter Hambly, SAHSU
REM
REM Usage: java_build.bat
REM

ECHO ON

SET PWD=%CD%
call mvn --version

call mvn clean
if %errorlevel% neq 0  (
	cd %PWD%	
	exit /b 1
)
call mvn install
if %errorlevel% neq 0  (
	cd %PWD%		
	exit /b 1
)
COPY rifServices\target\rifServices.war rifServices.war
if %errorlevel% neq 0  (
	cd %PWD%		
	exit /b 1
)
COPY rifWebApplication\target\RIF40.war RIF40.war
if %errorlevel% neq 0  (
	cd %PWD%		
	exit /b 1
)
COPY taxonomyServices\target\taxonomyServices.war taxonomyServices.war
if %errorlevel% neq 0  (	
	cd %PWD%	
	exit /b 1
)
COPY rifDataLoaderTool\target\rifDataLoaderTool-jar-with-dependencies.jar rifDataLoaderTool-jar-with-dependencies.jar
if %errorlevel% neq 0  (	
	cd %PWD%	
	exit /b 1
)
COPY statsService\target\statistics.war statistics.war
if %errorlevel% neq 0  (	
	cd %PWD%	
	exit /b 1
)

REM
REM Eof
