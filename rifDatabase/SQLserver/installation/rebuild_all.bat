ECHO OFF
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - RIF40 create sahsuland database objects and install data
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
REM Clear seetings
REM
(SET NEWDB=)
(SET NEWUSER=)
(SET NEWPW=)
(SET SNEWDB=)
(SET SNEWUSER=)
(SET SNEWPW=)
(SET SQLCMDPASSWORD=)

REM
REM Get DB settings
REM
echo Creating development RIF databases
SET /P NEWUSER=New user [default peter]: %=% || SET NEWUSER=peter
SET NEWDB=sahsuland
REM
REM Passwords tests: see test_pw.txt
REM
REM The ^s are escape characters
REM
SET "XNEWPW=Peter!^@#$%^^^&*:.\`^|/?=+-_[]{}()^<^>"
SET "NEWPW=Peter!@$%^~"
REM
SET /P NEWPW=New user password [default %NEWPW%]: %=% || SET "NEWPW=%NEWPW%"
SET REBUILD_ALL=Y
SET SNEWUSER=%NEWUSER%
SET "SNEWPW=%NEWPW%"
SET SNEWDB=%NEWDB%
ECHO ##########################################################################################
ECHO #
ECHO # WARNING! this script will the drop and create the RIF40 sahsuland and sahusland_dev databases.
ECHO # Type control-C to abort.
ECHO #
ECHO # Test user: %NEWUSER%; password: %NEWPW%
ECHO #
ECHO ##########################################################################################
PAUSE

REM
REM Create development database
REM
ECHO Create development database...
sqlcmd -E -b -m-1 -e -r1 -i rif40_development_creation.sql -v newuser="%NEWUSER%"
if %errorlevel% neq 0 (
	ECHO rif40_development_creation.sql exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_development_creation.sql built OK %errorlevel%
)

CALL rif40_sahsuland_dev_install.bat 
if %errorlevel% neq 0  (
	ECHO rif40_sahsuland_dev_install.bat exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_sahsuland_dev_install.bat built OK %errorlevel%
)

REM
REM Create production database
REM
CALL rif40_sahsuland_install.bat 
if %errorlevel% neq 0  (
	ECHO rif40_sahsuland_install.bat exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_sahsuland_install.bat built OK %errorlevel%
)

REM
REM Create development user
REM
ECHO Create development user...
sqlcmd -E -b -m-1 -e -i rif40_development_user.sql -v newuser="%SNEWUSER%" -v newpw="%SNEWPW%"
if %errorlevel% neq 0  (
	ECHO rif40_development_user.sql exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_development_user.sql built OK %errorlevel%
)

REM
REM Create production user
REM
ECHO Create production user...
sqlcmd -E -b -m-1 -e -i rif40_production_user.sql -v newuser="%SNEWUSER%" -v newdb="%SNEWDB%" -v newpw="%SNEWPW%"
if %errorlevel% neq 0  (
	ECHO rif40_production_user.sql exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_production_user.sql built OK %errorlevel%
)
	
REM
REM Run a test study
REM
ECHO Run a test study...
SET "SQLCMDPASSWORD=%SNEWPW%"
sqlcmd -U %SNEWUSER% -d %SNEWDB% -b -m-1 -e -i rif40_run_study.sql
if %errorlevel% neq 0  (
	ECHO Both %SNEWDB% and sahsuland_dev built OK
	
REM
REM Clear seetings
REM
REM	(SET SQLCMDPASSWORD=)
	(SET NEWDB=)
	(SET NEWUSER=)
	(SET NEWPW=)
	(SET SNEWUSER=)
	(SET SNEWPW=)
	(SET SNEWDB=)
	(SET REBUILD_ALL=)

	ECHO rif40_run_study.sql exiting with %errorlevel%
	exit /b 1
) else (
	ECHO rif40_run_study.sql ran OK %errorlevel%
	ECHO Both %SNEWDB% and sahsuland_dev built OK
)

REM
REM Clear seetings
REM
REM (SET SQLCMDPASSWORD=)
(SET NEWDB=)
(SET NEWUSER=)
(SET NEWPW=)
(SET SNEWUSER=)
(SET SNEWPW=)
(SET REBUILD_ALL=)

REM
REM Eof