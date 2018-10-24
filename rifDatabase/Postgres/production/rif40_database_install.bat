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

(SET NEWDB=)
(SET NEWUSER=)
(SET NEWPW=)
(SET PGPASSWORD=)
(SET RIF40PW=)

REM
REM Get DB settings
REM 
echo Creating production RIF Postgres database
for /f "delims=" %%a in ('pg_config --sysconfdir') do @set PG_SYSCONFDIR=%%a
echo PG_SYSCONFDIR=%PG_SYSCONFDIR%

REM
REM Production install script:
REM * Need to create schema called %NEWUSER% not "peter";
REM * Create dummy pgpass.conf for admin and user;
REM * Check that %NEWSER% != "rif40
REM

REM
REM Install psqlrc if an administrator
REM
NET SESSION >nul 2>&1
if %errorlevel% equ 0 (
    ECHO Administrator PRIVILEGES Detected! 
	
	IF NOT EXIST "%PG_SYSCONFDIR%" (
		MKDIR "%PG_SYSCONFDIR%"
		if %errorlevel% neq 0 (
			ECHO Unable to create directory %PG_SYSCONFDIR%
		)		
	)
	IF NOT EXIST "%PG_SYSCONFDIR%\psqlrc" IF EXIST psqlrc (
		ECHO Copy psqlrc to %PG_SYSCONFDIR%
		COPY psqlrc "%PG_SYSCONFDIR%\psqlrc"
		if %errorlevel% neq 0 (
			ECHO Unable to copy psqlrc to %PG_SYSCONFDIR%
		)		
	)
) else (
 	runas /noprofile /user:%COMPUTERNAME%\Administrator "NET SESSION" < one_line.txt 2>&1 > nul
 	if %errorlevel% equ 0 (
 		ECHO Power user PRIVILEGES Detected! 
		
		IF NOT EXIST "%PG_SYSCONFDIR%" (
			MKDIR "%PG_SYSCONFDIR%"
			if %errorlevel% neq 0 (
				ECHO Unable to create PG system configuration directory %PG_SYSCONFDIR%
			)		
		)
			
		IF NOT EXIST "%PG_SYSCONFDIR%\psqlrc" IF EXIST psqlrc (
			ECHO Copy psqlrc to %PG_SYSCONFDIR%
			COPY psqlrc "%PG_SYSCONFDIR%\psqlrc"
			if %errorlevel% neq 0 (
				ECHO Unable to copy psqlrc to %PG_SYSCONFDIR%
			)		
		)
 	)
	else (
		ECHO This script must be run privileged
		PAUSE
		exit /b 1
	)
)

IF NOT DEFINED NEWUSER (
	SET /P NEWUSER=New user [default peter]: %=%	|| SET NEWUSER=peter
)
IF NOT DEFINED NEWDB (
	SET /P NEWDB=New RIF40 db [default sahsuland]: %=%	|| SET NEWDB=sahsuland
)

REM * Check that %NEWSER% != "rif40
IF %NEWUSER% EQU "rif40"  (
	ECHO RIF40 is not a valid RIF user
	PAUSE
	exit /b 1
)

REM
REM Get passwords from %USERPROFILE%\AppData\Roaming\postgresql\pgpass.conf if it exists
REM
SET PGPASSWORDDIR="%USERPROFILE%\AppData\Roaming\postgresql"
SET PGPASSWORDFILE="%USERPROFILE%\AppData\Roaming\postgresql\pgpass.conf"
	
IF NOT EXIST "%PGPASSWORDDIR%" (
	MKDIR "%PGPASSWORDDIR%"
	if %errorlevel% neq 0 (
		ECHO Unable to create PG password directory %PGPASSWORDDIR%
		PAUSE
		exit /b 1
	)		
)
		
REM
REM Password file does not exist, create it
REM
IF NOT EXIST %PGPASSWORDFILE% (
	ECHO Creating PGPASSWORDFILE %PGPASSWORDFILE%
	IF NOT DEFINED PGPASSWORD (
		SET /P PGPASSWORD=Postgres password [postgres]: %=%	|| SET PGPASSWORD=postgres
	)
	SET RIF40PW=rif40_%RANDOM%_%RANDOM%
	SET NEWPW=%NEWUSER%_%RANDOM%_%RANDOM%
)

REM
REM Set environment variables in three sections so expansions work correctly
REM
IF NOT EXIST %PGPASSWORDFILE% (
REM
REM hostname:port:database:username:password
REM
	SET LINE=localhost:5432:*:postgres:%PGPASSWORD%
	SET LINE2=localhost:5432:*:rif40:%RIF40PW%
	SET LINE3=localhost:5432:*:%NEWUSER%:%NEWPW%
)

IF NOT EXIST %PGPASSWORDFILE% (	
	ECHO %LINE%> %PGPASSWORDFILE%
	IF NOT EXIST %PGPASSWORDFILE% (
		ECHO Cannot create PGPASSWORDFILE %PGPASSWORDFILE%
		PAUSE
		exit /b 1
 	)
	ECHO %LINE2%>> %PGPASSWORDFILE%
	ECHO %LINE3%>> %PGPASSWORDFILE%
	(SET LINE=)
	(SET LINE2=)
	(SET LINE3=)
)

IF EXIST %PGPASSWORDFILE% (
	ECHO Using previously created %PGPASSWORDFILE%
	FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:5432:\*:postgres:" %PGPASSWORDFILE%') DO (
	  SET PGPASSWORD=%%F
	)
	IF NOT DEFINED PGPASSWORD (
		FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:\*:\*:postgres:" %PGPASSWORDFILE%') DO (
		  SET PGPASSWORD=%%F
		)	
	)
	FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:5432:\*:rif40:" %PGPASSWORDFILE%') DO (
	  SET RIF40PW=%%F
	)
	IF NOT DEFINED RIF40PW (
		FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:\*:\*:rif40:" %PGPASSWORDFILE%') DO (
		  SET RIF40PW=%%F
		)
	)
	FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:5432:\*:%NEWUSER%:" %PGPASSWORDFILE%') DO (
	  SET NEWPW=%%F
	)
	IF NOT DEFINED NEWPW (
		FOR /F "tokens=5 delims=:" %%F IN ('findstr "localhost:\*:\*:%NEWUSER%:" %PGPASSWORDFILE%') DO (
		  SET NEWPW=%%F
		)	
	)
) ELSE (
	ECHO PGPASSWORDFILE %PGPASSWORDFILE% does not exist
	PAUSE
	exit /b 1
)

REM
REM Otherwise ask the user
REM
IF NOT DEFINED PGPASSWORD (
	SET /P PGPASSWORD=Postgres password [default postgres]: %=%	|| SET PGPASSWORD=postgres
)
IF NOT DEFINED RIF40PW (
	SET ADD_RIF40PW=Y
	SET RIF40PW=rif40_%RANDOM%_%RANDOM%
)
IF NOT DEFINED NEWPW (
	SET ADD_NEWPW=Y
	SET NEWPW=%NEWUSER%_%RANDOM%_%RANDOM%
)

IF DEFINED ADD_RIF40PW (
	SET LINE2=localhost:5432:*:rif40:%RIF40PW%
)
IF DEFINED ADD_NEWPW (
	SET LINE3=localhost:5432:*:%NEWUSER%:%NEWPW%
)

IF DEFINED ADD_RIF40PW (
	ECHO Adding rif40 to PG password file: %PGPASSWORDFILE%
	ECHO %LINE2%>> %PGPASSWORDFILE%
	(SET ADD_RIF40PW=)
	(SET LINE2=)
)
IF DEFINED ADD_NEWPW (
	ECHO Adding %NEWUSER% to PG password file: %PGPASSWORDFILE%
	ECHO %LINE3%>> %PGPASSWORDFILE%
	(SET ADD_NEWPW=)
	(SET LINE3=)
)

ECHO ##########################################################################################
ECHO #
ECHO # WARNING! this script will the drop and create the RIF40 %NEWDB% Postgres database.
ECHO # Type control-C to abort.
ECHO #
ECHO # Test user %NEWUSER%; password %NEWPW%
ECHO # Postgres password       %PGPASSWORD%
ECHO # Schema (rif40) password %RIF40PW%
ECHO # PG password directory   %PGPASSDIR%
ECHO # PG sysconfig directory  %PG_SYSCONFDIR%
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
	IF NOT EXIST pgpass.conf (
		ECHO Copying generated pgpass.conf to %PGPASSFILE%
		COPY pgpass.conf %PGPASSFILE%
		if %errorlevel% neq 0 (
			ECHO Unable to copy pgpass.conf to %PGPASSFILE%
		)
	)
	
REM
REM Run sahsuland.sql (which can be edited) if present; otherwise use pg_restore on the binary dump file
REM	
	IF NOT EXIST sahsuland.dump (
		ECHO Cannot find database dump sahsuland.dump
		(SET NEWPW=)
		(SET PGPASSWORD=)
		(SET RIF40PW=)	
		exit /b 1
	) else (		
		CALL powershell -ExecutionPolicy ByPass -file run.ps1 pg_restore.rpt "%CD%" ^
			pg_restore -d %NEWDB% -U postgres sahsuland.dump
	)
	if %errorlevel% neq 0 (
		ECHO pg_restore/psql exiting with error code: %errorlevel%	
		(SET NEWPW=)
		(SET PGPASSWORD=)
		(SET RIF40PW=)
		exit /b 1
	) else (		
		ECHO pg_restore/psql restored %NEWDB% OK
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
