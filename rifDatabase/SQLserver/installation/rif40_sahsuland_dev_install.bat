REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - RIF40 create sahsuland_dev database objects and install data
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
REM Usage: rif40_install_tables.bat
REM
REM recreate_all_sequences.bat MUST BE RUN FIRST
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

REM
REM Build sahsuland_dev
REM
sqlcmd -d sahsuland_dev -b -m-1 -e -i rif40_sahsuland_dev_install.sql -v path="%cd%\..\.." -I
if %errorlevel% neq 0  (
	ECHO rif40_sahsuland_dev_install.sql exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_sahsuland_dev_install.sql built OK %errorlevel%
)

REM
REM Reset rif40 password to rif40
REM 
sqlcmd -d sahsuland_dev -b -m-1 -e -Q "ALTER LOGIN [rif40] WITH PASSWORD = 'rif40'"
if %errorlevel% neq 0  (
	ECHO Unable to reset rif40 password; exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40 password reset OK %errorlevel%
)

REM Does not work in github tree - SQL server needs access permissions!
REM
REM BULK INSERT rif_data.lookup_sahsu_grd_level1
REM FROM '%USERPROFILE%\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\GeospatialData\tileMaker/mssql_lookup_sahsu_grd_level1.csv'     -- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line
REM WITH
REM (
REM        FORMATFILE = '%USERPROFILE%\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\GeospatialData\tileMaker/mssql_lookup_sahsu_grd_level1.fmt',            -- Use a format file
REM         TABLOCK                                 -- Table lock
REM );
REM
REM Msg 4861, Level 16, State 1, Server PH-LAPTOP\SQLEXPRESS, Line 7
REM Cannot bulk load because the file "%USERPROFILE%\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation\..\..\GeospatialData\tileMaker/mssql_lookup_sahsu_grd_level1.csv" could not be opened. Operating system error code 5(Access is denied.).
REM
REM CREATE LOGIN [rif40] WITH PASSWORD='rif40', CHECK_POLICY = OFF;

sqlcmd -U rif40 -P rif40 -d sahsuland_dev -b -m-1 -e -r1 -i ..\..\GeospatialData\tileMaker\rif_mssql_SAHSULAND.sql -v pwd="%cd%\..\..\GeospatialData\tileMaker"
if %errorlevel% neq 0  (
	ECHO rif_mssql_SAHSULAND.sql exiting with %errorlevel%
	sqlcmd -d sahsuland_dev -b -m-1 -e -i rif40_password_reset.sql
	ECHO rif_mssql_SAHSULAND.sql error; see above...
	exit /b 1
) else (
	ECHO rif_mssql_SAHSULAND.sql built OK %errorlevel%
)

sqlcmd -U rif40 -P rif40 -d sahsuland_dev -b -m-1 -e -r1 -i ..\..\DataLoaderData\SAHSULAND\ms_run_data_loader.sql -v pwd="%cd%\..\..\DataLoaderData\SAHSULAND"
if %errorlevel% neq 0  (
	ECHO ms_run_data_loader.sql exiting with %errorlevel%
	sqlcmd -d sahsuland_dev -b -m-1 -e -i rif40_password_reset.sql
	ECHO ms_run_data_loader.sql error; see above...
	exit /b 1
) else (
	ECHO ms_run_data_loader.sql built OK %errorlevel%
	ECHO sahsuland_dev built OK.
)

REM
REM Reset RIF40 password to random characters
REM
sqlcmd -d sahsuland_dev -b -m-1 -e -i rif40_password_reset.sql
if %errorlevel% neq 0  (
	ECHO rif40_password_reset.sql exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_password_reset.sql built OK %errorlevel%
)

REM
REM Export sahusland_dev
REM
sqlcmd -d sahsuland_dev -b -m-1 -e -i rif40_export_sahsuland_dev.sql -v export_dir="%cd%\..\production\" -I
if %errorlevel% neq 0  (
	ECHO rif40_export_sahsuland_dev.sql exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_export_sahsuland_dev.sql built OK %errorlevel%
)

REM
REM Eof
