@ECHO OFF
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - Start RIF tomcat server
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
REM Usage: start_rif.bat
REM

REM
REM Install psqlrc if an administrator
REM
NET SESSION >nul 2>&1
if %errorlevel% equ 0 (
    ECHO Administrator PRIVILEGES Detected! 
) else (
 	runas /noprofile /user:%COMPUTERNAME%\Administrator "NET SESSION" < one_line.txt 2>&1 > nul
 	if %errorlevel% equ 0 (
 		ECHO Power user PRIVILEGES Detected! 
	)
	else (
		ECHO This script must be run privileged
		PAUSE
		exit /b 1
	)
)

IF NOT DEFINED CATALINA_HOME (
		ECHO CATALINA_HOME must be set in the environment
		PAUSE
		exit /b 1
)
IF NOT DEFINED JAVA_HOME (
		IF NOT DEFINED JRE_HOME (
			ECHO JAVA_HOME or JRE_HOME must be set in the environment
			PAUSE
			exit /b 1
		)
)
IF NOT DEFINED R_HOME (
		ECHO R_HOME must be set in the environment
		PAUSE
		exit /b 1
)
IF NOT EXIST "%CATALINA_HOME%/bin" (
		ECHO No tomcat bin directory: %CATALINA_HOME%/bin
		PAUSE
		exit /b 1
)
IF NOT EXIST "%CATALINA_HOME%/bin/catalina.bat" (
		ECHO No tomcat bin catalina.bat: %CATALINA_HOME%/bin/catalina.bat
		PAUSE
		exit /b 1
)

ECHO ##########################################################################################
ECHO #
ECHO # Tomcat CATALINA_HOME: %CATALINA_HOME%
ECHO # JAVA_HOME:            %JAVA_HOME%
ECHO # JRE_HOME:             %JRE_HOME%
ECHO # R_HOME:               %R_HOME%
ECHO #
ECHO ##########################################################################################
PAUSE

cd "%CATALINA_HOME%/bin"
CALL catalina.bat start
IF %errorlevel% neq 0 (
	ECHO start_rif.bat exiting with error code: %errorlevel%
	PAUSE
	exit /b 1
) ELSE (
	echo RIF Tomcat started OK
	PAUSE
	exit /b 0
)

REM
REM Eof