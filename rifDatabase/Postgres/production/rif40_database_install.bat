@ECHO OFF
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
REM Usage: rif40_database_install.bat
REM

REM
REM Get DB settings
REM 
echo Creating production RIF Postgres database
for /f "delims=" %%a in ('pg_config --sysconfdir') do @set PG_SYSCONFDIR=%%a
echo PG_SYSCONFDIR=%PG_SYSCONFDIR%

REM
REM Install psqlrc if an administrator
REM
NET SESSION >nul 2>&1
if %errorlevel% equ 0 (
    ECHO Administrator PRIVILEGES Detected! 
	
	IF NOT EXIST %PG_SYSCONFDIR%\psqlrc IF EXIST psqlrc (
		ECHO Copy psqlrc to %PG_SYSCONFDIR%
		COPY psqlrc %PG_SYSCONFDIR%\psqlrc
		if %errorlevel% neq 0 (
			ECHO Unable to copy psqlrc to %PG_SYSCONFDIR%
		)		
	)
) else (
 	runas /noprofile /user:%COMPUTERNAME%\Administrator "NET SESSION" < one_line.txt 2>&1 > nul
 	if %errorlevel% equ 0 (
 		ECHO Power user PRIVILEGES Detected! 
			
		IF NOT EXIST %PG_SYSCONFDIR%\psqlrc IF EXIST psqlrc (
			ECHO Copy psqlrc to %PG_SYSCONFDIR%
			COPY psqlrc %PG_SYSCONFDIR%\psqlrc
			if %errorlevel% neq 0 (
				ECHO Unable to copy psqlrc to %PG_SYSCONFDIR%
			)		
		)
 	)
)

IF NOT DEFINED NEWUSER (
	SET /P NEWUSER=New user [default peter]: %=%	|| SET NEWUSER=peter
)
IF NOT DEFINED NEWDB (
	SET /P NEWDB=New RIF40 db [default sahsuland]: %=%	|| SET NEWDB=sahsuland
)

REM
REM Get passwords from C:\Users\%USERNAME%\AppData\Roaming\postgresql\pgpass.conf if it exists
REM
SET PGPASSFILE="C:\Users\%USERNAME%\AppData\Roaming\postgresql\pgpass.conf"
IF EXIST %PGPASSFILE% (
	FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:5432:\*:postgres:" %PGPASSFILE%') DO (
	  SET PGPASSWORD=%%F
	)
	FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:5432:\*:rif40:" %PGPASSFILE%') DO (
	  SET RIF40PW=%%F
	)
	FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:5432:\*:%NEWUSER%:" %PGPASSFILE%') DO (
	  SET NEWPW=%%F
	)
)

REM
REM Otherwise ask the user
REM
IF NOT DEFINED PGPASSWORD (
	SET /P PGPASSWORD=Postgres password [default postgres]: %=%	|| SET PGPASSWORD=postgres
)
IF NOT DEFINED RIF40PW (
	SET /P RIF40PW=Schema [rif40] password [default rif40]: %=%	|| SET RIF40PW=rif40
)
IF NOT DEFINED NEWPW (
	SET /P NEWPW=New user password [default %NEWUSER%]: %=%	|| SET NEWPW=%NEWUSER%
)

ECHO ##########################################################################################
ECHO #
ECHO # WARNING! this script will the drop and create the RIF40 %NEWDB% Postgres database.
ECHO # Type control-C to abort.
ECHO #
ECHO # Test user: %NEWUSER%; password: %NEWPW%
ECHO # Postgres password:       %PGPASSWORD%
ECHO # Schema (rif40) password: %RIF40PW%
ECHO #
ECHO ##########################################################################################
PAUSE

REM
REM Create production database
REM
CALL powershell -ExecutionPolicy ByPass -file run.ps1 db_create.rpt "%CD%" ^
	psql -U postgres -d postgres -h localhost -w -e -P pager=off ^
	-v testuser=%NEWUSER% -v newdb=%NEWDB% -v newpw=%NEWPW% ^
	-v verbosity=terse ^
	-v debug_level=1 ^
	-v echo=all ^
	-v postgres_password=%PGPASSWORD% ^
	-v rif40_password=%RIF40PW% ^
	-v tablespace_dir= ^
	-v pghost=localhost ^
	-v os=Windows_NT ^
	-f db_create.sql
if %errorlevel% neq 0 (
	ECHO db_create.sql exiting with error code: %errorlevel%
	(SET NEWPW=)
	(SET PGPASSWORD=)
	(SET RIF40PW=)	
	exit /b 1
) else (
	ECHO db_create.sql built %NEWDB% OK
	
REM
REM This needs to be tested on a clean install
REM
	IF NOT EXIST %PGPASSFILE% IF EXIST pgpass.conf (
		ECHO Copying generated pgpass.conf to %PGPASSFILE%
		COPY pgpass.conf %PGPASSFILE%
		if %errorlevel% neq 0 (
			ECHO Unable to copy pgpass.conf to %PGPASSFILE%
		)
	)
	CALL powershell -ExecutionPolicy ByPass -file run.ps1 pg_restore.rpt "%CD%" ^
		pg_restore -d %NEWDB% -U postgres sahsuland_dev.dump
	if %errorlevel% neq 0 (
		ECHO pg_restore exiting with error code: %errorlevel%	
		(SET NEWPW=)
		(SET PGPASSWORD=)
		(SET RIF40PW=)
		exit /b 1
	) else (		
		ECHO pg_restore restored %NEWDB% OK
REM
REM Clear settings
REM
		(SET NEWDB=)
		(SET NEWUSER=)
		(SET NEWPW=)
		(SET PGPASSWORD=)
		(SET RIF40PW=)
	)
)

REM
REM Eof