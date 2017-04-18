
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - RIF40 create data load tables for SAHUSLAND data
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
REM Usage: rif_data_install_tables.bat
REM

ECHO OFF
REM
REM MUST BE RUN AS ADMINSTRATOR/POWERUSER
REM
NET SESSION >nul 2>&1
if %errorlevel% equ 0 (
    ECHO Administrator PRIVILEGES Detected! 
) else (
	runas /noprofile /user:%COMPUTERNAME%\Administrator "NET SESSION" < one_line.txt
	if %errorlevel% neq 0 {
		ECHO NOT AN ADMIN!
		exit /b 1
	}
	else {
		ECHO Power user PRIVILEGES Detected! 
	}
)

sqlcmd -d sahsuland_dev -b -m-1 -e -i rif40_import_sahsuland.sql -v path="%cd%\..\.." -I
REM E.g. path="C:\Users\mdouglas\rapidInquiryFacility\rifDatabase" 

REM
REM Eof