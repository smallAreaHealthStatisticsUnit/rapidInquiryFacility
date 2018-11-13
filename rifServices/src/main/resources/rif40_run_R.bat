@ECHO OFF
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - SQL Server database - Run R script on a study extract.
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
REM Peter Hambly, SAHSU
REM
REM Usage: rif40_run_R.bat
REM
	

REM
REM Check R and Tomcat setup correctly
REM 
IF NOT DEFINED R_HOME (
	ECHO Please set R_HOME in the environment
	exit /b 1
)
IF NOT DEFINED CATALINA_HOME (
	ECHO Please set CATALINA_HOME in the environment
	exit /b 1
)

IF EXIST rif40_run_R_env.bat (
	CALL rif40_run_R_env
) else (
	ECHO File not found: rif40_run_R_env.bat
	exit /b 1
)

REM
REM The R script currently does NOT use ODBC; but it can. If you want to use ODBC and modify Adj_Cov_Smooth_csv, uncomment the below line
REM Beware: access to the database depends on network setup and a network database being used. i.e. localhost will ONLY work on the original
REM RIF platform
REM
IF NOT DEFINED PASSWORD (
	SET PASSWORD=UNKNOWN
REM	SET /P PASSWORD=Password [default %USERID%]: %=% || SET NEWUSER=%USERID%
)

ECHO ##########################################################################################
ECHO #
ECHO # Run R script on a study extract.
ECHO # 
ECHO # USERID=%USERID%
ECHO # DB_NAME=%DB_NAME%
ECHO # DB_HOST=%DB_HOST%
ECHO # DB_PORT=%DB_PORT%
ECHO # DB_DRIVER_PREFIX=%DB_DRIVER_PREFIX%
ECHO # DB_DRIVER_CLASS_NAME=%DB_DRIVER_CLASS_NAME%
ECHO # STUDYID=%STUDYID%
ECHO # INVESTIGATIONNAME=%INVESTIGATIONNAME%
ECHO # INVESTIGATIONID=%INVESTIGATIONID%
ECHO # ODBCDATASOURCE=%ODBCDATASOURCE%
ECHO # MODEL=%MODEL%
ECHO # COVARIATENAME=%COVARIATENAME%
ECHO # RISKANAL=%RISKANAL%
ECHO #
ECHO ##########################################################################################

REM
REM Edit to change if required. Disable dump to R data frames to scratch directory. Set scratch
REM directory to be current working directory
REM
SET SCRATCHSPACE=%CD%\\
SET DUMPFRAMESTOCSV=FALSE
	
REM
REM Run R
REM
ECHO "%R_HOME%\bin\x64\RScript" %SCRIPT_NAME% ^^
ECHO --db_driver_prefix=%DB_DRIVER_PREFIX% --db_driver_class_name=%DB_DRIVER_CLASS_NAME% --odbcDataSource=%ODBCDATASOURCE% ^^
ECHO --dbHost=%DB_HOST% --dbPort=%DB_PORT% --dbName=%DB_NAME% ^^
ECHO --studyID=%STUDYID% --investigationName=%INVESTIGATIONNAME% --investigationId=%INVESTIGATIONID% ^^
ECHO --model=%MODEL% --covariateName=%COVARIATENAME% ^^
ECHO --userID=%USERID% --password=XXXXXXXXXXXXXXXXXXXXXX ^^
ECHO --scratchspace="%SCRATCHSPACE%" --dumpframestocsv=%DUMPFRAMESTOCSV%
"%R_HOME%\bin\x64\RScript" Statistics_csv.R ^
	--db_driver_prefix=%DB_DRIVER_PREFIX% --db_driver_class_name=%DB_DRIVER_CLASS_NAME% --odbcDataSource=%ODBCDATASOURCE% ^
	--dbHost=%DB_HOST% --dbPort=%DB_PORT% --dbName=%DB_NAME% ^
	--studyID=%STUDYID% --investigationName=%INVESTIGATIONNAME% --investigationId=%INVESTIGATIONID% ^
	--model=%MODEL% --covariateName=%COVARIATENAME% ^
	--scratchspace="%SCRATCHSPACE%" --dumpframestocsv=%DUMPFRAMESTOCSV% --riskAnal=%RISKANAL% --
SET SERRORLEVEL=%errorlevel%
REM
REM Clear seetings
REM
(SET USERID=)
(SET PASSWORD=)
(SET DBNAME=)
(SET DBHOST=)
(SET DBPORT=)
(SET DB_DRIVER_PREFIX=)
(SET DB_DRIVER_CLASS_NAME=)
(SET INVESTIGATIONNAME=)
(SET ODBCDATASOURCE=)
(SET MODEL=)
(SET NAMES.ADJ.1=)
(SET ADJ.1=)
(SET NEWDB=)
(SET NEWUSER=)

if %SERRORLEVEL% neq 0 (
	ECHO Test study failed: Statistics_csv.R procedure had error for study: %STUDYID%; investigation: %INVESTIGATIONID%	
	(SET STUDYID=)
	(SET INVESTIGATIONID=)
	(SET SERRORLEVEL=)
	exit /b 1
) ELSE (
	ECHO Statistics_csv.R procedure OK for study: %STUDYID%; investigation: %INVESTIGATIONID%
	(SET STUDYID=)
	(SET INVESTIGATIONID=)
	(SET SERRORLEVEL=)
	exit /b 0
)

REM
REM Eof
