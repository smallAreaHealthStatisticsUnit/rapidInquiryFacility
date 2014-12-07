-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
-- *
-- ************************************************************************
--
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- $Id$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Create PG psql code (Optional R support)
--
-- Copyright:
--
-- The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
-- that rapidly addresses epidemiological and public health questions using 
-- routinely collected health and population data and generates standardised 
-- rates and relative risks for any given health outcome, for specified age 
-- and year ranges, for any given geographical area.
--
-- Copyright 2014 Imperial College London, developed by the Small Area
-- Health Statistics Unit. The work of the Small Area Health Statistics Unit 
-- is funded by the Public Health England as part of the MRC-PHE Centre for 
-- Environment and Health. Funding for this project has also been received 
-- from the Centers for Disease Control and Prevention.  
--
-- This file is part of the Rapid Inquiry Facility (RIF) project.
-- RIF is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- RIF is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
-- to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
-- Boston, MA 02110-1301 USA
--
-- Author:
--
-- Peter Hambly, SAHSU
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing
\set VERBOSITY terse

BEGIN;
--
-- Check user is postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'postgres' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not postgres', user;	
	END IF;
END;
$$;

\echo Creating PG psql code (Optional R support)...

DO LANGUAGE plpgsql $$
DECLARE	
	c1_r1 CURSOR FOR 
		SELECT *
	      FROM pg_extension
		 WHERE extname = 'plr';
	c1_rec RECORD;
--
-- Functions to enable debug for
--
	rif40_r_pkg_functions 	VARCHAR[] := ARRAY[
		'r_init',
		'_r_init',	
		'r_cleanup',
		'installed_packages',
		'install_package_from_internet',
		'_install_all_packages_from_internet'];
	l_function 					VARCHAR;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
	PERFORM rif40_log_pkg.rif40_log_setup();
    PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);

--
	OPEN c1_r1;
	FETCH c1_r1 INTO c1_rec;
	CLOSE c1_r1;
	IF c1_rec.extname IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_rif40_r_pkg', 'PL/R extension version % loaded', 
			c1_rec.extversion::VARCHAR);

DROP FUNCTION IF EXISTS rif40_r_pkg.r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR);
DROP FUNCTION IF EXISTS rif40_r_pkg.r_cleanup();
DROP FUNCTION IF EXISTS rif40_r_pkg._r_cleanup();
DROP FUNCTION IF EXISTS rif40_r_pkg.installed_packages();
DROP FUNCTION IF EXISTS rif40_r_pkg.install_package_from_internet(VARCHAR);
DROP FUNCTION IF EXISTS rif40_r_pkg._install_all_packages_from_internet();
DROP FUNCTION IF EXISTS rif40_r_pkg._r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL);

CREATE OR REPLACE FUNCTION rif40_r_pkg.r_init(
	debug_level RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL DEFAULT 'DEBUG1', 
	cran_repository VARCHAR							DEFAULT 'http://cran.ma.imperial.ac.uk/')
RETURNS void
SECURITY DEFINER
AS $func$
DECLARE
	 c1_r2 CURSOR FOR
		SELECT *
		  FROM pg_settings 
		 WHERE name = 'data_directory' /* e.g. 'C:/Program Files/PostgreSQL/9.3/data' */;
	c1_rec RECORD;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
	OPEN c1_r2;
	FETCH c1_r2 INTO c1_rec;
	CLOSE c1_r2;
--
-- Standard R functions, escaped to prevent parse problems caused by Linux/Windows CR/LF
-- Kept short to ease parse problems (R''s parser is rubbish)...
--
	BEGIN
--
-- Log functions
-- 
		PERFORM install_rcmd('#'||E'\n'||
'# Common functions'||E'\n'||
'#'||E'\n'||
'# Function:		rif40_log()'||E'\n'||
'# Parameters:	Log level (INFO, DEBUG1, DEBUG2, WARNING), function name, string'||E'\n'||
'# Returns:		Nothing'||E'\n'||
'# Description:  Calls PL/pgsql function rif40_log_pkg.rif40_log'||E'\n'||
'#				Catches errors. These appear in the log file'||E'\n'||
'#'||E'\n'||
'rif40_log <- function(w, f, s) {'||E'\n'||
'	if (nchar(s) > 0) {'||E'\n'||
'		sql_stmt=sprintf("SELECT rif40_log_pkg.rif40_log(''%s'', ''%s'', %s)", w, f, pg.quoteliteral(s))'||E'\n'||
'		tryCatch('||E'\n'||
'			{'||E'\n'||
'				plan<-pg.spi.exec(sql_stmt)'||E'\n'||
'			},'||E'\n'||
'			error=function(sql_error) {'||E'\n'||
'#'||E'\n'||
'# DO NOT throw an error - it will crash postgres!'||E'\n'||
'#				pg.throwerror(sprintf("%d in %s(): caught error %s in SQL statement\nSQL>%s", -60606, f, sql_error, sql_stmt))'||E'\n'||
'				stop(sprintf("rif40_log() caught error: %s in SQL statement\nSQL>%s", sql_error, sql_stmt))'||E'\n'||
'			},'||E'\n'||
'			warning=function(sql_warning) {'||E'\n'||
'				stop(sprintf("rif40_log() caught warning: %s in SQL statement", sql_warning, sql_stmt))'||E'\n'||
'			}'||E'\n'||
'		)'||E'\n'||    
'	}'||E'\n'||
'}');
		PERFORM install_rcmd('#'||E'\n'||
'#}'||E'\n'||
'# Function:		rif40_debug()'||E'\n'||
'# Parameters:	Log level (DEBUG1, DEBUG2), function name, format string, debug output'||E'\n'||
'# Returns:		Nothing'||E'\n'||
'# Description:  Calls PL/pgsql function rif40_log_pkg.rif40_log'||E'\n'||
'#				Catches errors. These appear in the log file'||E'\n'||
'#}'||E'\n'||
'rif40_debug <- function(w, f, s, o) {'||E'\n'||
'	if (nchar(o) > 0) {'||E'\n'||
' 		rif40_log(w, f, sprintf(s, o))'||E'\n'||
'	}'||E'\n'||
'}');
		PERFORM install_rcmd('#'||E'\n'||
'# Function:		rif40_error()'||E'\n'||
'# Parameters:	negative error code, function name, string'||E'\n'||
'# Returns:		Nothing; throws PL/pgsql exception'||E'\n'||
'# Description:  Calls PL/pgsql function rif40_log_pkg.rif40_error'||E'\n'||
'#				Must be caught with a standard exception handler in PL/pgsql or you will not get the message!'||E'\n'||
'#				DO NOT USE tryCatch()'||E'\n'||
'#'||E'\n'||
'rif40_error <- function(e, f, s) {'||E'\n'||
'#'||E'\n'||
'#	sql_stmt=sprintf("SELECT rif40_log_pkg.rif40_error(%d, ''%s'', ''%s'')", e, f, pg.quoteliteral(s))'||E'\n'||
'#	plan<-pg.spi.exec(sql_stmt)'||E'\n'||
'#'||E'\n'||
'# Better message!'||E'\n'||
'#'||E'\n'||
'	pg.throwerror(sprintf("%d in %s(): %s", e, f, pg.quoteliteral(s)))'||E'\n'||
'}');
		PERFORM install_rcmd('#'||E'\n'||
'# Installer functions'||E'\n'||
'#'||E'\n'||
'# Function:		install_package_from_internet()'||E'\n'||
'# Parameters:	Package'||E'\n'||
'# Returns:		Nothing; throws R parser exception'||E'\n'||
'# Description:  Install package from Internet'||E'\n'||
'#'||E'\n'||
'install_package_from_internet<-function(p) {'||E'\n'||
'		if (!is.element(p, installed.packages(lib=my_lib)[,1])) {'||E'\n'||
'			rif40_log("INFO", "install_package_from_internet", sprintf("Installing %s", p))'||E'\n'||
'			rif40_debug("DEBUG1", "install_package_from_internet", '||E'\n'||
'				"update.packages() >>>\n%s\n<<<",'||E'\n'||
'					toString('||E'\n'||
'						capture.output('||E'\n'||
'							install.packages(p,lib=my_lib))))'||E'\n'||
'		}'||E'\n'||
'		else {'||E'\n'||
'			rif40_log("INFO", "install_package_from_internet", sprintf("%s is already installed", p))'||E'\n'||
'		}'||E'\n'||
'		global.loaded=NULL'||E'\n'||
'		rif40_debug("DEBUG1", "install_package_from_internet",'||E'\n'||
'				"require() >>>\n%s\n<<<",'||E'\n'||
'					toString('||E'\n'||
'						capture.output('||E'\n'||
'							global.loaded<--require(p,lib.loc=my_lib,character.only=TRUE))))'||E'\n'||
'		if (global.loaded) {'||E'\n'||
'			rif40_log("INFO", "install_package_from_internet", sprintf("Loaded %s", p))'||E'\n'||
'			rm(global.loaded)'||E'\n'||
'		}'||E'\n'||
'		else {'||E'\n'||
'			rif40_error(-90125, "install_package_from_internet", sprintf("Could not load %s", p))'||E'\n'||
'		}'||E'\n'||
'}'||E'\n'||
'#'||E'\n'||
'# Function:		install_all_packages_from_internet()'||E'\n'||
'# Parameters:	Package string vector'||E'\n'||
'# Returns:		Nothing; throws R parser exception'||E'\n'||
'# Description:  Install all packages in list from Internet'||E'\n'||
'#'||E'\n'||
'install_all_packages_from_internet<-function(plist) {'||E'\n'||
'	for(p in plist) {'||E'\n'||
'		install_package_from_internet(p)'||E'\n'||
'	}'||E'\n'||
'}');
--
-- Install inla (web) installer (http://www.math.ntnu.no/inla/givemeINLA.R with givemeINLA(testing=FALSE, lib = inla.lib) 
-- and library selection removed
--
		PERFORM install_rcmd(''||E'\n'||
'`inla.update` = function(lib = NULL, testing = FALSE, force = FALSE) {'||E'\n'||
'    inla.installer(lib=lib, testing=testing, force=force)'||E'\n'||
'}'||E'\n'||
'#'||E'\n'||
'`inla.installer` = function(lib = NULL, testing = FALSE, force = FALSE) {'||E'\n'||
'#'||E'\n'||
'# Set web download directory'||E'\n'||
'#'||E'\n'||
'    if (testing)'||E'\n'||
'        www = "http://www.math.ntnu.no/inla/binaries/testing"'||E'\n'||
'    else '||E'\n'||
'        www = "http://www.math.ntnu.no/inla/binaries"'||E'\n'||
'#'||E'\n'||
'# Extract remote web build date; inla build version'||E'\n'||
'#'||E'\n'||
'    b.date = scan(paste(www,"/build.date", sep=""), quiet=TRUE, what = character(0))'||E'\n'||
'    if (exists("inla.version")) {'||E'\n'||
'        bb.date = inla.version("bdate")'||E'\n'||
'    } '||E'\n'||
'	else {'||E'\n'||
'        bb.date = "INLA.is.not.installed"'||E'\n'||
'    }'||E'\n'||
'#'||E'\n'||
'# Install/update decision'||E'\n'||
'#'||E'\n'||
'    if (b.date == as.character(bb.date)) {'||E'\n'||
'        if (!force) {'||E'\n'||
'			rif40_log("DEBUG1", "install_package_from_internet", '||E'\n'||
'				sprintf("You have the newest version of INLA: %s (%s)", '||E'\n'||
'					toString(inla.version("version")), as.character(bb.date)))'||E'\n'||
'           return (invisible())'||E'\n'||
'		}'||E'\n'||
'        else {'||E'\n'||
'			rif40_log("DEBUG1", "install_package_from_internet", '||E'\n'||
'				sprintf("You have the newest version of INLA: %s (Install forced: %s)", '||E'\n'||
'					toString(inla.version("version")), as.character(bb.date)))'||E'\n'||
'		}'||E'\n'||
'   }'||E'\n'||
'	else {'||E'\n'||
'			rif40_log("INFO", "install_package_from_internet",'||E'\n'||
'				sprintf("Updating INLA: %s: (local: %s != www: %s)", '||E'\n'||
'					toString(inla.version("version")), as.character(bb.date), as.character(b.date)))'||E'\n'||
'	}'||E'\n'||
'#'||E'\n'||
'# download and install INLA'||E'\n'||
'#'||E'\n'||
'    if (inla.installer.os("windows")) {'||E'\n'||
'        suff = ".zip"'||E'\n'||
'        tp = "win.binary"'||E'\n'||
'    } else {'||E'\n'||
'        suff = ".tgz"'||E'\n'||
'        tp = "source"'||E'\n'||
'    }'||E'\n'||
'    dfile = paste(tempdir(), .Platform$file.sep, "INLA", suff, sep="")'||E'\n'||
'    sfile = paste(www, "/INLA", suff, sep="")'||E'\n'||
'	rif40_log("DEBUG1", "install_package_from_internet", sprintf("Download %s to: %s", sfile, dfile))'||E'\n'||
'    download.file(sfile, dfile)'||E'\n'||
'#'||E'\n'||
'# remove old library before installing the new one'||E'\n'||
'#'||E'\n'||
'    try(detach(package:INLA), silent = TRUE)'||E'\n'||
'    try(unloadNamespace("INLA"), silent = TRUE)'||E'\n'||
'#'||E'\n'||
' 	rif40_log("DEBUG1", "install_package_from_internet", sprintf("Install package %s", dfile))'||E'\n'||
'    install.packages(dfile, lib = lib, repos=NULL, type = tp)'||E'\n'||
'    library(INLA, lib.loc = lib)'||E'\n'||
'#'||E'\n'||
'#   cat("\nType\n\tinla.version()\nto display the new version of R-INLA. Thanks for upgrading.\n\n")'||E'\n'||
'    return (invisible())'||E'\n'||
'}'||E'\n'||
'#'||E'\n'||
'`inla.installer.os` = function(type = c("linux", "mac", "windows", "else")) {'||E'\n'||
'    if (missing(type)) {'||E'\n'||
'        stop("Type of OS is required.")'||E'\n'||
'    }'||E'\n'||
'    type = match.arg(type)'||E'\n'||
'# '||E'\n'||
'    if (type == "windows") {'||E'\n'||
'        return (.Platform$OS.type == "windows")'||E'\n'||
'    } else if (type == "mac") {'||E'\n'||
'        result = (file.info("/Library")$isdir && file.info("/Applications")$isdir)'||E'\n'||
'        if (is.na(result)) {'||E'\n'||
'            result = FALSE'||E'\n'||
'        }'||E'\n'||
'        return (result)'||E'\n'||
'    } else if (type == "linux") {'||E'\n'||
'        return ((.Platform$OS.type == "unix") && !inla.installer.os("mac"))'||E'\n'||
'    } else if (type == "else") {'||E'\n'||
'        return (TRUE)'||E'\n'||
'    } else {'||E'\n'||
'        stop("This shouldn''t happen.")'||E'\n'||
'    }'||E'\n'||
'}'||E'\n'||
'`inla.installer.os.type` = function() {'||E'\n'||
'    for (os in c("windows", "mac", "linux", "else")) {'||E'\n'||
'        if (inla.installer.os(os)) {'||E'\n'||
'            return (os)'||E'\n'||
'        }'||E'\n'||
'    }'||E'\n'||
'    stop("This shouldn''t happen.")'||E'\n'||
'}'||E'\n'||
'#'||E'\n'||
'`inla.installer.os.32or64bit` = function() {'||E'\n'||
'    return (ifelse(.Machine$sizeof.pointer == 4, "32", "64"))'||E'\n'||
'}'||E'\n'||
'`inla.installer.os.is.32bit` = function() {'||E'\n'||
'    return (inla.installer.os.32or64bit() == "32")'||E'\n'||
'}'||E'\n'||
'`inla.installer.os.is.64bit` = function() {'||E'\n'||
'    return (inla.installer.os.32or64bit() == "64")'||E'\n'||
'}'||E'\n'||
'#'||E'\n'|| 
'`givemeINLA` = function(...) inla.installer(...)'||E'\n'||
'if (!exists("inla.lib")) inla.lib = NULL');
--
-- Set R repository
--
		PERFORM install_rcmd('#'||E'\n'||
'# Set CRAN repository'||E'\n'||
'#'||E'\n'||
'r <- getOption("repos")'||E'\n'||
'r["CRAN"] <- "'||cran_repository||'"'||E'\n'||
'options(repos = r)');
--
-- Set my_lib (Postgres R library location)
--
		PERFORM install_rcmd('#'||E'\n'||
'# Set local R_library (in $PGDATA/R_Library) for Postgres'||E'\n'||
'#'||E'\n'||
'global.Rmessages_file<<-NULL'||E'\n'||
'global.Routput_file<<-NULL'||E'\n'||
'global.Rmessages<<-NULL'||E'\n'||
'global.Routput<<-NULL'||E'\n'||
'my_lib=paste("'||c1_rec.setting /* pg_data */||'", "/R_Library", sep='''')');
--
		PERFORM rif40_r_pkg._r_init(debug_level); -- data_directory PG_DATA 
	EXCEPTION
		WHEN others THEN
-- 
-- Not supported until 9.2
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='r_init() caught: '||E'\n'||SQLERRM::VARCHAR||' in R (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
			RAISE INFO '1: %', error_message;
			RAISE;
	END;
END;
$func$ LANGUAGE plpgsql;
			
CREATE OR REPLACE FUNCTION rif40_r_pkg._r_init(
	debug_level 	RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL 	DEFAULT 'DEBUG1')
RETURNS void
AS $func$
#
# Check r_init() has NOT been run
#
if (exists("Rmessages_file")) {
	pg.throwerror("r_init() already run [in rif40_r_pkg._r_init()]")
}
#
# Error handler
#
#options(error = traceback())

#
# Logging 
#
# R runs in: C:\Program Files\PostgreSQL\9.3\data
# (not R_HOME)
#
if (file.exists(tempdir())) {
	rif40_log("DEBUG1", "_r_init", sprintf("Temp dir: %s directory exists", tempdir()))
}
else {
	rif40_error(-90124, "_r_init", sprintf("Temp dir: %s directory needs to be created",  tempdir()))
}
timestr=format(Sys.time(), "%H%M_%d%m%Y")
global.Rmessages_file<<-sprintf("%s/Rmessages_%d_%s.txt", tempdir(), Sys.getpid(), timestr)
global.Routput_file<<-sprintf("%s/Routput_%d_%s.txt", tempdir(), Sys.getpid(), timestr)
global.Rmessages <<- file(global.Rmessages_file, open = "w")
global.Routput <<- file(global.Routput_file, open = "w")
sink(global.Rmessages, type = "message")
sink(global.Routput, type = "output")
rm(timestr)
#
# Enable verbosity
#
options(verbose=TRUE)

rif40_log("INFO", "_r_init", sprintf("Set CRAN repository to: %s", r["CRAN"]))

#
# Check my_lib - Postgres R library location
#

if (file.exists(my_lib)) {
	rif40_log("INFO", "_r_init", sprintf("R library directory exists: %s", my_lib))
}
else {
	rif40_error(-90126, "_r_init", sprintf("R library directory needs to be created: %s", my_lib))
}
.libPaths(my_lib)
rif40_log("DEBUG1", "_r_init", sprintf("Set library path to: %s", toString(.libPaths())))
rif40_debug("DEBUG1", "_r_init", "ls() >>>\n%s\n<<<", toString(ls()))

$func$ LANGUAGE plr;

CREATE OR REPLACE FUNCTION rif40_r_pkg.install_all_packages(use_internet BOOLEAN DEFAULT TRUE)
RETURNS void
AS $func$
DECLARE	
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
	IF use_internet THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'install_all_packages', 'Load packages from internet');
		BEGIN
			PERFORM rif40_r_pkg.r_init('DEBUG1');
			PERFORM rif40_r_pkg._install_all_packages_from_internet();
		EXCEPTION
			WHEN others THEN
-- 
-- Not supported until 9.2
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='install_all_packages() caught: '||E'\n'||SQLERRM::VARCHAR||' in R (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '1: %', error_message;
				RAISE;
		END;
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-30999, 'install_all_packages', 'Load packages from files - NOT YET IMPLEMENTED');
	END IF;
END;
$func$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION rif40_r_pkg._install_all_packages_from_internet()
RETURNS void
AS $func$
#
# Check r_init() has been run
#
if (!exists("my_lib")) {
	pg.throwerror("r_init() not run [in rif40_r_pkg._install_all_packages_from_internet()]")
}

rif40_debug("DEBUG1", "_install_all_packages_from_internet", "ls() >>>\n%s\n<<<", toString(ls()))

#
# Update
#
rif40_debug("INFO", "_install_all_packages_from_internet", 
	"update.packages() >>>\n%s\n<<<", 
		toString(
			capture.output(
				update.packages(checkBuilt=TRUE,lib=my_lib,ask=FALSE,quiet=TRUE))))

#
# Install packages
#
sahsu_packages=c("sp", "rgdal", "plyr", "abind", "Matrix")
install_all_packages_from_internet(sahsu_packages)
rm(sahsu_packages)

#
# Install INLA
#
if (!is.element("INLA", installed.packages(lib=my_lib)[,1])) {
	rif40_log("INFO", "_install_all_packages_from_internet", "Installing inla")
	rif40_debug("DEBUG1", "_install_all_packages_from_internet", 
		"givemeINLA() >>>\n%s\n<<<", 
			toString(
				capture.output(
					givemeINLA(testing=FALSE, lib = my_lib))))
}
else {
	library(INLA, lib.loc = my_lib)
	rif40_log("INFO", "_install_all_packages_from_internet", 
		sprintf("INLA v%s is already installed; checking for updates", toString(inla.version("version"))))
	rif40_debug("DEBUG1", "_install_all_packages_from_internet", 
		"inla.update() >>>\n%s\n<<<", 
			toString(
				capture.output(
					inla.update(testing=FALSE, lib = my_lib))))
}

#
# Cleanup
#
#remove.packages(sahsu_packages,lib=my_lib)
rif40_debug("DEBUG1", "_install_all_packages_from_internet", "ls() >>>\n%s", toString(ls()))
$func$ LANGUAGE plr;

CREATE OR REPLACE FUNCTION rif40_r_pkg.install_package_from_internet(package_name VARCHAR)
RETURNS void
AS $func$

rif40_debug("DEBUG1", "install_package_from_internet", "ls() >>>\n%s\n<<<", toString(ls()))

install_package_from_internet(package_name);

$func$ LANGUAGE plr;

CREATE OR REPLACE FUNCTION rif40_r_pkg.installed_packages()
RETURNS /* Available columns:
 [1] "Package"               "LibPath"               "Version"              
 [4] "Priority"              "Depends"               "Imports"              
 [7] "LinkingTo"             "Suggests"              "Enhances"             
[10] "License"               "License_is_FOSS"       "License_restricts_use"
[13] "OS_type"               "MD5sum"                "NeedsCompilation"     
[16] "Built"                
 */
 TABLE(package VARCHAR, libpath VARCHAR, version VARCHAR, 
	license VARCHAR, os_type VARCHAR, built VARCHAR)
AS $func$
#
# Check r_init() has been run
#
if (!exists("my_lib")) {
	pg.throwerror("r_init() not run [in rif40_r_pkg.installed_packages()]")
}
#
# List libraries
#
global.pkgs=NULL
rif40_debug("INFO", "_install_all_packages_from_internet", 
	"update.packages() >>>\n%s\n<<<", 
		toString(
			capture.output(
				global.pkgs<-installed.packages())))
#
# Print column names
#
rif40_debug("INFO", "installed_packages", 
	"Available colnames() >>>\n%s\n<<<", 
		toString(
			colnames(global.pkgs)))
#
# Extract as required
#
rpkgs<-global.pkgs[,c(1,2,3,10,13,16)]
#
rm(global.pkgs)
rif40_debug("DEBUG2", "installed_packages", "rpkgs >>>\n%s\n<<<", toString(rpkgs))
#
rif40_debug("DEBUG1", "installed_packages", "ls() >>>\n%s\n<<<", toString(ls()))

return(rpkgs)

$func$ LANGUAGE plr;

CREATE OR REPLACE FUNCTION rif40_r_pkg.r_cleanup()
RETURNS void
AS $func$
DECLARE
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
	PERFORM rif40_r_pkg._r_cleanup();
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
		GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
		error_message:='r_cleanup() caught: '||E'\n'||SQLERRM::VARCHAR||' in R (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
		RAISE;
END;
$func$ LANGUAGE plpgsql;
	
CREATE OR REPLACE FUNCTION rif40_r_pkg._r_cleanup()
RETURNS void
AS $func$
#
# Function:	r_cleanup()
# Parameters:	 None
# Returns:	 Nothing
# Description: R Cleanup function
#
# Check r_init() has been run
#
if (!exists("my_lib")) {
	pg.throwerror("r_init() not run [in rif40_r_pkg.r_cleanup()]")
}
#
# Close messages and output logs: R_io/Rmessages_<pid>_<date time string>.txt,  R_io/Routput_<pid>_<date time string>.txt
#

flush(global.Routput)
sink(file=NULL, type = "output")
close(global.Routput)
Routput_size=file.info(global.Routput_file)$size
#
if (Routput_size > 0) {
	con=file(global.Rmessages_file, "r")
	text=readLines(con)
	rtext=NULL;
	for (i in 1:length(text)){
		rtext=paste(rtext, text[i], sep="\n")
	}
	rif40_log("WARNING", "r_cleanup", 
		sprintf("Output file: %s is not zero sized (%d bytes i.e. NOT all output trapped)\n%d lines >>>\n%s\n<<<", 
			global.Routput_file, Routput_size, length(text), rtext))
	close(con)
}
else {
	rif40_debug("DEBUG1", "r_cleanup", "Output file: %s is zero sized (i.e. all output trapped); removing", 
		global.Routput_file)
	file.remove(global.Routput_file)
	if (file.exists(global.Routput_file)) {
		stop(sprintf("Unable to delete output file %s", 
			global.Routput_file))
	}
}
#
flush(global.Rmessages)
sink(file=NULL, type = "message")
close(global.Rmessages)
Rmessages_size=file.info(global.Rmessages_file)$size
if (Rmessages_size > 0) {
	con=file(global.Rmessages_file, "r")
	text=readLines(con)
	rtext=NULL;
	for (i in 1:length(text)){
		rtext=paste(rtext, text[i], sep="\n")
	}
	rif40_log("WARNING", "r_cleanup", 
		sprintf("Messages file: %s is not zero sized (%d bytes i.e. NOT all messages trapped)\n%d lines >>>\n%s\n<<<", 
			global.Rmessages_file, Rmessages_size, length(text), rtext))
	close(con)
}
else {
	rif40_debug("DEBUG1", "r_cleanup", "Messages file: %s is zero sized (i.e. all messages trapped); removing", 
		global.Rmessages_file)
	sink(file=NULL, type = "message")
	file.remove(global.Rmessages_file)
	if (file.exists(global.Rmessages_file)) {
		stop(sprintf("Unable to delete messages file %s: %s", global.Rmessages_file))
	}
}
#
rif40_debug("DEBUG1", "r_cleanup", "ls() >>>\n%s", toString(ls()))
rm(list=ls())
$func$ LANGUAGE plr;
--
-- Comments
--
COMMENT ON FUNCTION rif40_r_pkg.install_all_packages(boolean) IS 'Function:	install_all_packages()
Parameters:	 Use internet [Default TRUE]
Returns:	 Nothing
Description: Install R packages from internet or files into local library:

	sp: 	Classes and methods for spatial data
	rgdal: 	Bindings for the Geospatial Data Abstraction Library 
	plyr: 	Tools for splitting, applying and combining data 
	abind: 	Combine multi-dimensional arrays
	Matrix: Sparse and Dense Matrix Classes and Methods
			Classes and methods for dense and sparse matrices and operations on them using LAPACK and SuiteSparse.
	splines: Regression spline functions and classes [part of the standard install]
	inla:	Approximate Bayesian inference for latent Gaussian models by using integrated nested Laplace approximations';
COMMENT ON FUNCTION rif40_r_pkg.installed_packages() IS 'Function:	installed_packages()
Parameters:	 None
Returns:	 Nothing
Description: List all required packages';
COMMENT ON FUNCTION rif40_r_pkg.install_package_from_internet(VARCHAR) IS 'Function:	install_package_from_internet()
Parameters:	 Package name
Returns:	 Nothing
Description: Install additional package';
COMMENT ON FUNCTION rif40_r_pkg.r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR) IS 'Function:	r_init()
Parameters:	 Default debug level [DEBUG1], CRAN repository [Default:  http://cran.ma.imperial.ac.uk/]
Returns:	 Nothing
Description: R initialisation function. Call once per session:

Set up messages and output logs: R_io/Rmessages_<pid>_<date time string>.txt,  R_io/Routput_<pid>_<date time string>.txt
Set CRAN repository
Set local library: $PG_DATA/R_Library';
COMMENT ON FUNCTION rif40_r_pkg.r_cleanup() IS 'Function:	r_cleanup()
Parameters:	 None
Returns:	 Nothing
Description: R Cleanup function

Close messages and output logs: tempdir()/Rmessages_<pid>_<date time string>.txt,  R_io/Routput_<pid>_<date time string>.txt
';
COMMENT ON FUNCTION rif40_r_pkg._r_cleanup() IS 'Function:	r_cleanup()
Parameters:	 None
Returns:	 Nothing
Description: R Cleanup function

Close messages and output logs: R_io/Rmessages_<pid>_<date time string>.txt,  R_io/Routput_<pid>_<date time string>.txt
';

--
-- Grants
--
GRANT EXECUTE ON FUNCTION rif40_r_pkg.install_all_packages(boolean) TO rif_manager, rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.installed_packages() TO rif_manager, rif_user, rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.install_package_from_internet(VARCHAR) TO rif_manager, rif_user, rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg._install_all_packages_from_internet(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR) TO rif_manager, rif_user, rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.r_cleanup() TO rif_manager, rif_user, rif40;

--
-- Enabled debug on select rif40_r_pkg_functions functions
--
	FOREACH l_function IN ARRAY rif40_r_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
	
--
-- Install all required packages
-- 
		BEGIN
		PERFORM rif40_r_pkg.install_all_packages();
		EXCEPTION
			WHEN others THEN
-- 
-- Not supported until 9.2
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='v4_0_rif40_r_pkg() caught: '||E'\n'||SQLERRM::VARCHAR||' in R (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '1: %', error_message;
				RAISE;
		END;
		PERFORM rif40_r_pkg.r_cleanup();
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_rif40_r_pkg', 'Optional PL/R extension not loaded');
	END IF;

END;
$$;

END;

\echo Created PG psql code (Optional R support).

--
-- Eof