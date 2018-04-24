ECHO OFF
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - RIF40 create production SQL Server database from backup
REM								   One directory (production) version
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
REM Usage: rif40_database_install.bat
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
REM Get DB settings
REM 
echo Creating production RIF SQL Server database
REM 
REM REBUILD_ALL is set up rebuild_all.bat - this prevents the questions being asked twice
REM
IF NOT DEFINED REBUILD_ALL (
	SET /P NEWUSER=New user [default peter]: %=% || SET NEWUSER=peter
	SET /P NEWDB=New RIF40 db [default sahsuland]: %=%|| SET NEWDB=sahsuland
	SET /P NEWPW=New user password [default %NEWUSER%]: %=% || SET NEWPW=%NEWUSER%
	SET REBUILD_ALL=N
)

IF NOT DEFINED NEWUSER (
	SET /P NEWUSER=New user [default peter]: %=% || SET NEWUSER=peter
)
IF NOT DEFINED NEWDB (
	SET /P NEWDB=New RIF40 db [default sahsuland]: %=%|| SET NEWDB=sahsuland
)
IF NOT DEFINED NEWPW (
	SET /P NEWPW=New user password [default %NEWUSER%]: %=% || SET NEWPW=%NEWUSER%
	)
ECHO ##########################################################################################
ECHO #
ECHO # WARNING! this script will the drop and create the RIF40 %NEWDB% SQL Server database.
ECHO # Type control-C to abort.
ECHO #
ECHO # Test user: %NEWUSER%; password: %NEWPW%
ECHO #
ECHO ##########################################################################################
PAUSE

REM
REM Create production database
REM
sqlcmd -E -b -m-1 -e -r1 -i rif40_production_creation.sql -v import_dir="%cd%\" -v newdb="%NEWDB%" -v newuser="%NEWUSER%"
if %errorlevel% neq 0 (
	ECHO rif40_production_creation.sql exiting with %errorlevel%	
	IF NOT DEFINED REBUILD_ALL (
REM
REM Clear seetings
REM
		(SET NEWDB=)
		(SET NEWUSER=)
		(SET NEWPW=)
	)
	exit /b 1
) else (
	ECHO rif40_production_creation.sql built OK %errorlevel%
)

REM
REM Create production user
REM
sqlcmd -E -b -m-1 -e -i rif40_production_user.sql -v newuser="%NEWUSER%" -v newdb="%NEWDB%" -v newpw="%NEWPW%"
if %errorlevel% neq 0  (
	ECHO rif40_production_user.sql exiting with %errorlevel%
	IF NOT DEFINED REBUILD_ALL (
REM
REM Clear seetings
REM
		(SET NEWDB=)
		(SET NEWUSER=)
		(SET NEWPW=)
	)	
	exit /b 1
) else (
REM
REM Run a test study (don't R/ODBC not setup yet!)
REM
REM	sqlcmd -U %NEWUSER% -P %NEWPW% -d %NEWDB% -b -m-1 -e -i rif40_run_study.sql
REM	if %errorlevel% neq 0  (
REM		ECHO Both %NEWDB% and sahsuland_dev built OK; test study failed
REM	)	
REM
REM Clear seetings
REM
	(SET NEWDB=)
	(SET NEWUSER=)
	(SET NEWPW=)
	ECHO rif40_production_user.sql built OK %errorlevel%; created RIF40 production database %NEWDB% with user: %NEWUSER%
)

REM
REM Eof