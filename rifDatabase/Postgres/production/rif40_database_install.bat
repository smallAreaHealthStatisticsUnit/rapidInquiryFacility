ECHO OFF
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - RIF40 create production Postgres database from backup
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
REM Usage: rif40_install_tables.bat
REM
REM recreate_all_sequences.bat MUST BE RUN FIRST
REM

REM
REM MUST BE RUN AS USER WHO CAN LOGON AS POSTGRES
REM
REM NET SESSION >nul 2>&1
REM if %errorlevel% equ 0 (
REM    ECHO Administrator PRIVILEGES Detected! 
REM ) else (
REM 	runas /noprofile /user:%COMPUTERNAME%\Administrator "NET SESSION" < one_line.txt
REM 	if %errorlevel% neq 0 {
REM 		ECHO NOT AN ADMIN!
REM 		exit /b 1
REM 	}
REM 	else {
REM 		ECHO Power user PRIVILEGES Detected! 
REM 	}
REM )

REM
REM Get DB settings
REM 
echo Creating production RIF Postgres database
REM

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
ECHO # WARNING! this script will the drop and create the RIF40 %NEWDB% Postgres database.
ECHO # Type control-C to abort.
ECHO #
ECHO # Test user: %NEWUSER%; password: %NEWPW%
ECHO #
ECHO ##########################################################################################
PAUSE

REM
REM Todo:
REM
REM 1. psqlrc
REM powershell -ExecutionPolicy ByPass -file copy.ps1  ../etc/psqlrc c:/PROGRA~1/POSTGR~1/9.5/etc
REM 2. Generate encrypted passwords
REM 3. Generate and test .pgpass
REM

REM
REM Create production database
REM
REM sqlcmd -E -b -m-1 -e -r1 -i rif40_production_creation.sql -v import_dir="%cd%\" -v newdb="%NEWDB%" -v newuser="%NEWUSER%"
CALL powershell -ExecutionPolicy ByPass -file run.ps1 db_create.rpt "%CD%" ^
	psql -U postgres -d postgres -h localhost -w -e -P pager=off ^
	-v testuser=%NEWUSER% -v newdb=%NEWDB% -v newpw=%NEWPW% ^
	-v verbosity=terse ^
	-v debug_level=1 ^
	-v echo=all ^
	-v encrypted_postgres_password=md51b0219e12aaf22cdaf7567e586887db4 ^
	-v encrypted_rif40_password=md5971757ca86c61e2d8f618fe7ab7a32a1 ^
	-v tablespace_dir= ^
	-v pghost=localhost ^
	-v os=Windows_NT ^
	-f db_create.sql
if %errorlevel% neq 0 (
	ECHO db_create.sql exiting with error code: %errorlevel%	
	exit /b 1
) else (
	ECHO db_create.sql built %NEWDB% OK
	CALL powershell -ExecutionPolicy ByPass -file run.ps1 pg_restore.rpt "%CD%" ^
		pg_restore -d %NEWDB% -U postgres sahsuland_dev.dump
	if %errorlevel% neq 0 (
		ECHO pg_restore exiting with error code: %errorlevel%	
		exit /b 1
	) else (		
		ECHO pg_restore restored %NEWDB% OK
REM
REM Clear settings
REM
		(SET NEWDB=)
		(SET NEWUSER=)
		(SET NEWPW=)
	)
)

REM
REM Run a test study
REM

REM
REM Eof