@ECHO OFF
REM ************************************************************************
REM
REM Description:
REM
REM Rapid Enquiry Facility (RIF) - Postgres database - create test study
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
REM Usage: rif40_run_study.bat
REM

REM 
REM REBUILD_ALL is set up rebuild_all.bat - this prevents the questions being asked twice
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

IF DEFINED DB_DRIVER SET DB_DRIVER=
IF DEFINED DEFAULT_DB_DRIVER SET DEFAULT_DB_DRIVER=
IF DEFINED ODBC_DATA_SOURCE SET ODBC_DATA_SOURCE=

SET DEFAULT_DB_DRIVER=--db_driver_prefix^=jdbc:postgresql --db_driver_class_name^=org.postgresql.Driver
IF NOT DEFINED DB_DRIVER (
	SET /P DB_DRIVER=Java database driver [default "%DEFAULT_DB_DRIVER%"]: %=% || SET DB_DRIVER=%DEFAULT_DB_DRIVER%
)
IF NOT DEFINED ODBC_DATA_SOURCE (
	SET /P ODBC_DATA_SOURCE=ODBC Data Source [default PostgreSQL35W]: %=% || SET ODBC_DATA_SOURCE=PostgreSQL35W
)


IF EXIST study_id.txt DEL /F study_id.txt
IF EXIST inv_id.txt DEL /F inv_id.txt

IF DEFINED STUDY_ID SET STUDY_ID=
IF DEFINED INV_ID SET INV_ID=

REM
REM Get next study_id and inv_id from database
REM
psql -U %NEWUSER% -d %NEWDB% -A -b -t -c "SELECT LTRIM(RTRIM((MAX(study_id)+1)::Text)) AS study_id FROM rif40_studies;" > study_id.txt
if %errorlevel% neq 0  (
	ECHO rif40_run_study procedure OK; unable to get study_id from database
)
psql -U %NEWUSER% -d %NEWDB% -A -b -t -c "SELECT LTRIM(RTRIM((MAX(inv_id)+1)::Text)) AS inv_id FROM rif40_investigations;" > inv_id.txt
if %errorlevel% neq 0  (
	ECHO rif40_run_study procedure OK; unable to get inv_id from database
)
REM

IF EXIST study_id.txt (
REM	type study_id.txt
	SET /p STUDY_ID=< study_id.txt
) else (
	ECHO Test study failed: no study_id.txt
	exit /b 1
)
IF EXIST inv_id.txt (
REM	type inv_id.txt
	SET /p INV_ID=< inv_id.txt
) else (
	ECHO Test study failed: no inv_id.txt
	exit /b 1
)

REM
REM Edit to chnage if required. Enables dump to R data frames to scratch directory
REM
SET SCRATCHSPACE=c:\rifDemo\scratchSpace\
SET DUMPFRAMESTOCSV=TRUE

REM ECHO STUDY_ID=%STUDY_ID%
REM ECHO INV_ID=%INV_ID%
REM (SET STUDY_ID=)
REM (SET INV_ID=)
REM exit /b 1
	
REM
REM Run a test study
REM
ECHO Run study: %STUDY_ID%; investigation: %INV_ID%	
psql -U %NEWUSER% -d %NEWDB% -w -e -f rif40_run_study.sql
if %errorlevel% neq 0  (
	ECHO Test study failed: rif40_run_study procedure had error
	exit /b 1
) else (
	ECHO rif40_run_study procedure OK for study: %STUDY_ID%; investigation: %INV_ID%; R command ^>^>^>
	ECHO "%R_HOME%\bin\x64\RScript" "%CATALINA_HOME%\\webapps\\rifServices\\WEB-INF\\classes\\Adj_Cov_Smooth.R" ^^
	ECHO %DB_DRIVER% ^^
	ECHO --db_host=localhost --db_port=5432 --db_name=%NEWDB% ^^
	ECHO --study_id=%STUDY_ID% --investigation_name=T_INV_1 --covariate_name=SES --investigation_id=%INV_ID% --r_model=het_r_procedure ^^
	ECHO --odbc_data_source=%ODBC_DATA_SOURCE% --user_id=%NEWUSER% --password=XXXXXXXXXXXXXXXXXXXXXX
	ECHO --scratchspace=%SCRATCHSPACE% --dumpframestocsv=%DUMPFRAMESTOCSV%
	call RScript.bat "%R_HOME%\bin\x64\RScript" "%CATALINA_HOME%\\webapps\\rifServices\\WEB-INF\\classes\\Adj_Cov_Smooth.R" ^
		%DB_DRIVER% ^
		--db_host=localhost --db_port=5432 --db_name=%NEWDB% ^
		--study_id=%STUDY_ID% --investigation_name=T_INV_1 --covariate_name=SES --investigation_id=%INV_ID% --r_model=het_r_procedure ^
		--odbc_data_source=%ODBC_DATA_SOURCE% --user_id=%NEWUSER% --password=%NEWPW% ^
		--scratchspace=%SCRATCHSPACE% --dumpframestocsv=%DUMPFRAMESTOCSV%
)

REM
REM Clear seetings
REM
(SET NEWDB=)
(SET NEWUSER=)
(SET NEWPW=)
(SET STUDY_ID=)
(SET INV_ID=)
(SET DB_DRIVER=)
(SET DEFAULT_DB_DRIVER=)
(SET ODBC_DATA_SOURCE=)

REM
REM Eof
