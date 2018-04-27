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
SET RND=%RANDOM%
SET RIF4DOCS=RIF4/docs/stage
SET DOCDIR=%CATALINA_HOME%\webapps\RIF4\docs\stage
SET DOCDIR2=%TEMP%\docs\%RND%
echo DOCDIR=%DOCDIR%
REM ECHO ON
IF NOT EXIST "%DOCDIR%" (
	echo mkdir "%DOCDIR%"
	mkdir "%DOCDIR%"
)

echo DOCDIR2="%DOCDIR2%"
IF NOT EXIST %DOCDIR2% (
	echo mkdir "%DOCDIR2%"
	mkdir "%DOCDIR2%"
)


echo DEL /S /Q "%DOCDIR%\*"
DEL /S /Q "%DOCDIR%\*"
REM
REM Set GITHUBAUTH if you exceed the anonymous downloads/hour limit
REM
REM SET GITHUBAUTH=--user=phambly@fastmail.co.uk --pass=XXXXXXXXXXXXXXXXX

echo python -m grip rifWebApplication\Readme.md %GITHUBAUTH% --export "%DOCDIR%\RIF_Web_Application_Installation.html"
python -m grip rifWebApplication\Readme.md --export "%DOCDIR%\RIF_Web_Application_Installation.html"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
echo python -m grip rifWebApplication\Readme.md %GITHUBAUTH% --export "%DOCDIR%\RIF_Web_Application_Installation.html"
python -m grip rifWebApplication\Readme.md --export "%DOCDIR%\RIF_Web_Application_Installation.html"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
echo python -m grip rifDatabase\Postgres\production\windows_install_from_pg_dump.md %GITHUBAUTH% --export "%DOCDIR%\RIF_Postgres_Install.html"
python -m grip rifDatabase\Postgres\production\windows_install_from_pg_dump.md --export "%DOCDIR%\RIF_Postgres_Install.html"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
echo python -m grip rifDatabase\SQLserver\production\INSTALL.md %GITHUBAUTH% --export "%DOCDIR%\RIF_SQLserver_Install.html"
python -m grip rifDatabase\SQLserver\production\INSTALL.md --export "%DOCDIR%\RIF_SQLserver_Install.html"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
echo python -m grip rifDatabase\DataLoaderData\DataLoading.md %GITHUBAUTH% --export "%DOCDIR%\RIF_manual_data_loading.html"
python -m grip rifDatabase\DataLoaderData\DataLoading.md --export "%DOCDIR%\RIF_manual_data_loading.html"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
echo cp "Documentation\RIF v4 0 Manual.pdf" "%DOCDIR%\RIF_v40_Manual.pdf"
cp "Documentation\RIF v4 0 Manual.pdf" "%DOCDIR%\RIF_v40_Manual.pdf"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
echo cp "Documentation\RIF Data Loader Manual.pdf" "%DOCDIR%\RIF_Data_Loader_Manual.pdf"
cp "Documentation\RIF Data Loader Manual.pdf" "%DOCDIR%\RIF_Data_Loader_Manual.pdf"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
echo python -m grip docs\README.md %GITHUBAUTH% --export "%DOCDIR%\index.html"
python -m grip docs\README.md --export "%DOCDIR%\index.html"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)
SET WGETARGS=--mirror --mirror --convert-links --page-requisites --no-parent
echo "C:\Program Files (x86)\GnuWin32\bin\wget" %WGETARGS% -P "%DOCDIR2%" http://localhost:8080/%RIF4DOCS%/index.html
"C:\Program Files (x86)\GnuWin32\bin\wget" %WGETARGS% -P "%DOCDIR2%" http://localhost:8080/%RIF4DOCS%/index.html
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)

echo DIR "%DOCDIR%" "%DOCDIR2%\localhost+8080\RIF4\docs\stage"
DIR "%DOCDIR%" "%DOCDIR2%\localhost+8080\RIF4\docs\stage"
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)

echo XCOPY "%DOCDIR2%\localhost+8080\RIF4\docs\stage" docs\ /S /Y
XCOPY "%DOCDIR2%\localhost+8080\RIF4\docs\stage" docs\ /S /Y
if %errorlevel% neq 0  (
	ECHO makeDoc.bat exiting with %errorlevel%
	exit /b 1
)

echo DEL /S /Q "%DOCDIR%\*"
DEL /S /Q "%DOCDIR%\*"

echo DEL /S /Q "%DOCDIR2%\*"
DEL /S /Q "%DOCDIR2%\*"

(SET RND=)
(SET DOCDIR=)
(SET DOCDIR2=)
(SET RIF4DOCS=)
exit /b 0