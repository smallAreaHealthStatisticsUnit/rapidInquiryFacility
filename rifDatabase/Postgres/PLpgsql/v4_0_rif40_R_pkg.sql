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
	BEGIN
--
-- Log functions
-- 
		PERFORM install_rcmd('#
# Common functions
#
# Function:		rif40_log()
# Parameters:	Log level (INFO, DEBUG1, DEBUG2, WARNING), function name, string
# Returns:		Nothing
# Description:  Calls PL/pgsql function rif40_log_pkg.rif40_log
#				Catches errors. These appear in the log file
#
rif40_log <- function(w, f, s) {
	if (nchar(s) > 0) {
		sql_stmt=sprintf("SELECT rif40_log_pkg.rif40_log(''%s'', ''%s'', %s)", w, f, pg.quoteliteral(s))
		tryCatch(
			{
				plan<-pg.spi.exec(sql_stmt)
			},
			error=function(sql_error) {
#
# DO NOT throw an error - it will crash postgres!
#				pg.throwerror(sprintf("%d in %s(): caught error %s in SQL statement\nSQL>%s", -60606, f, sql_error, sql_stmt))
				stop(sprintf("rif40_log() caught error: %s in SQL statement\nSQL>%s", sql_error, sql_stmt))
			},
			warning=function(sql_warning) {
				stop(sprintf("rif40_log() caught warning: %s in SQL statement", sql_warning, sql_stmt))
			}
		)    
	}
}

#
# Function:		rif40_debug()
# Parameters:	Log level (DEBUG1, DEBUG2), function name, format string, debug output
# Returns:		Nothing
# Description:  Calls PL/pgsql function rif40_log_pkg.rif40_log
#				Catches errors. These appear in the log file
#
rif40_debug <- function(w, f, s, o) {
	if (nchar(o) > 0) {
		rif40_log(w, f, sprintf(s, o))
	}
}

#
# Function:		rif40_error()
# Parameters:	negative error code, function name, string
# Returns:		Nothing; throws PL/pgsql exception
# Description:  Calls PL/pgsql function rif40_log_pkg.rif40_error
#				Must be caught with a standard exception handler in PL/pgsql or you will not get the message!
#				DO NOT USE tryCatch()
#
	rif40_error <- function(e, f, s) {
#
#	sql_stmt=sprintf("SELECT rif40_log_pkg.rif40_error(%d, ''%s'', ''%s'')", e, f, pg.quoteliteral(s))
#	plan<-pg.spi.exec(sql_stmt)
#
# Better message!
#
	pg.throwerror(sprintf("%d in %s(): %s", e, f, pg.quoteliteral(s)))
}');
		PERFORM install_rcmd('
#
# Installer functions
#
# Function:		install_package_from_internet()
# Parameters:	Package
# Returns:		Nothing; throws R parser exception
# Description:  Install package from Internet
#
install_package_from_internet<-function(p) {
		if (!is.element(p, installed.packages(lib=my_lib)[,1])) {
			rif40_log("INFO", "install_package_from_internet", sprintf("Installing %s", p))
			rif40_debug("DEBUG1", "install_package_from_internet", 
				"update.packages() >>>\n%s\n<<<", 
					toString(
						capture.output(
							install.packages(p,lib=my_lib))))
		}
		else {
			rif40_log("INFO", "install_package_from_internet", sprintf("%s is already installed", p))
		}
		global.loaded=NULL
		rif40_debug("DEBUG1", "install_package_from_internet", 
				"require() >>>\n%s\n<<<", 
					toString(
						capture.output(
							global.loaded<--require(p,lib.loc=my_lib,character.only=TRUE))))
		if (global.loaded) {
			rif40_log("INFO", "install_package_from_internet", sprintf("Loaded %s", p))
			rm(global.loaded)
		}
		else {
			rif40_error(-90125, "install_package_from_internet", sprintf("Could not load %s", p))
		}
}

#
# Function:		install_all_packages_from_internet()
# Parameters:	Package string vector
# Returns:		Nothing; throws R parser exception
# Description:  Install all packages in list from Internet
#
install_all_packages_from_internet<-function(plist) {
	for(p in plist) {
		install_package_from_internet(p)
	}
}');
--
-- Install inla (web) installer (http://www.math.ntnu.no/inla/givemeINLA.R with givemeINLA(testing=FALSE, lib = inla.lib) 
-- and library selection removed
--
		PERFORM install_rcmd('
`inla.update` = function(lib = NULL, testing = FALSE, force = FALSE)
{
    inla.installer(lib=lib, testing=testing, force=force)
}

`inla.installer` = function(lib = NULL, testing = FALSE, force = FALSE)
{
#
# Set web download directory
#
    if (testing)
        www = "http://www.math.ntnu.no/inla/binaries/testing"
    else 
        www = "http://www.math.ntnu.no/inla/binaries"

#
# Extract remote web build date; inla build version
#
    b.date = scan(paste(www,"/build.date", sep=""), quiet=TRUE, what = character(0))
    if (exists("inla.version")) {
        bb.date = inla.version("bdate")
    } 
	else {
        bb.date = "INLA.is.not.installed"
    }

#
# Install/update decision
#
    if (b.date == as.character(bb.date)) {
        if (!force) {
			rif40_log("DEBUG1", "install_package_from_internet", 
				sprintf("You have the newest version of INLA: %s (%s)", 
					toString(inla.version("version")), as.character(bb.date)))
            return (invisible())
		}
        else {
			rif40_log("DEBUG1", "install_package_from_internet", 
				sprintf("You have the newest version of INLA: %s (Install forced: %s)", 
					toString(inla.version("version")), as.character(bb.date)))
		}
    }
	else {
			rif40_log("INFO", "install_package_from_internet", 
				sprintf("Updating INLA: %s: (local: %s != www: %s)", 
					toString(inla.version("version")), as.character(bb.date), as.character(b.date)))
	}
    
    ## download and install INLA
    if (inla.installer.os("windows")) {
        suff = ".zip"
        tp = "win.binary"
    } else {    
        suff = ".tgz"
        tp = "source"
    }
    dfile = paste(tempdir(), .Platform$file.sep, "INLA", suff, sep="")
    sfile = paste(www, "/INLA", suff, sep="")
	rif40_log("DEBUG1", "install_package_from_internet", sprintf("Download %s to: %s", sfile, dfile))
    download.file(sfile, dfile)

    ## remove old library before installing the new one
    try(detach(package:INLA), silent = TRUE)
    try(unloadNamespace("INLA"), silent = TRUE)

 	rif40_log("DEBUG1", "install_package_from_internet", sprintf("Install package %s", dfile))
    install.packages(dfile, lib = lib, repos=NULL, type = tp)
    library(INLA, lib.loc = lib)

#   cat("\nType\n\tinla.version()\nto display the new version of R-INLA. Thanks for upgrading.\n\n")
    return (invisible())
}


`inla.installer.os` = function(type = c("linux", "mac", "windows", "else"))
{
    if (missing(type)) {
        stop("Type of OS is required.")
    }
    type = match.arg(type)
    
    if (type == "windows") {
        return (.Platform$OS.type == "windows")
    } else if (type == "mac") {
        result = (file.info("/Library")$isdir && file.info("/Applications")$isdir)
        if (is.na(result)) {
            result = FALSE
        }
        return (result)
    } else if (type == "linux") {
        return ((.Platform$OS.type == "unix") && !inla.installer.os("mac"))
    } else if (type == "else") {
        return (TRUE)
    } else {
        stop("This shouldn''t happen.")
    }
}
`inla.installer.os.type` = function()
{
    for (os in c("windows", "mac", "linux", "else")) {
        if (inla.installer.os(os)) {
            return (os)
        }
    }
    stop("This shouldn''t happen.")
}

`inla.installer.os.32or64bit` = function()
{
    return (ifelse(.Machine$sizeof.pointer == 4, "32", "64"))
}
`inla.installer.os.is.32bit` = function()
{
    return (inla.installer.os.32or64bit() == "32")
}
`inla.installer.os.is.64bit` = function()
{
    return (inla.installer.os.32or64bit() == "64")
}
 
`givemeINLA` = function(...) inla.installer(...)
if (!exists("inla.lib")) inla.lib = NULL
');
--
-- Set R repository
--
		PERFORM install_rcmd('
#
# Set CRAN repository
#
r <- getOption("repos")
r["CRAN"] <- "'||cran_repository||'"
options(repos = r)');
--
-- Set my_lib (Postgres R library location)
--
		PERFORM install_rcmd('
#
# Set local R_library (in $PGDATA/R_Library) for Postgres
#
my_lib=paste("'||c1_rec.setting /* pg_data */||'", "/R_Library", sep='''')
');
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
# Error handler
#
#options(error = traceback())

#
# Logging 
#
# R runs in: C:\Program Files\PostgreSQL\9.3\data
# (not R_HOME)
#
if (file.exists("R_io")) {
	rif40_log("INFO", "_r_init", "R_io message directory exists")
}
else {
	rif40_error(-90124, "_r_init", "R_io message directory needs to be created")
}
timestr=format(Sys.time(), "%H%M_%d%m%Y")
Rmessages_file=sprintf("R_io/Rmessages_%d_%s.txt", Sys.getpid(), timestr)
Routput_file=sprintf("R_io/Routput_%d_%s.txt", Sys.getpid(), timestr)
Rmessages <- file(Rmessages_file, open = "w")
Routput <- file(Routput_file, open = "w")
sink(Rmessages)
sink(Routput)
sink(Rmessages, type = "message")
sink(Routput, type = "output")
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
	pg.throwerror("r_init() not run in rif40_r_pkg.installed_packages()")
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
#rm(list)
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
	pg.throwerror("r_init() not run in rif40_r_pkg.installed_packages()")
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
	"colnames() >>>\n%s\n<<<", 
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

--
-- Comments
--
COMMENT ON FUNCTION rif40_r_pkg.install_all_packages(boolean) IS 'Function:	install_all_packages()
Parameters:	 Use internet [Default TRUE]
Returns:	 Nothing
Description: Install R packages from internet or files';
COMMENT ON FUNCTION rif40_r_pkg.installed_packages() IS 'Function:	installed_packages()
Parameters:	 None
Returns:	 Nothing
Description: All all required packages:

	sp: 	Classes and methods for spatial data
	rgdal: 	Bindings for the Geospatial Data Abstraction Library 
	plyr: 	Tools for splitting, applying and combining data 
	abind: 	Combine multi-dimensional arrays
	Matrix: Sparse and Dense Matrix Classes and Methods
			Classes and methods for dense and sparse matrices and operations on them using LAPACK and SuiteSparse.
	splines: Regression spline functions and classes [part of the standard install]
	inla:	Approximate Bayesian inference for latent Gaussian models by using integrated nested Laplace approximations';
COMMENT ON FUNCTION rif40_r_pkg.install_package_from_internet(VARCHAR) IS 'Function:	install_package_from_internet()
Parameters:	 Package name
Returns:	 Nothing
Description: Install additional package';
COMMENT ON FUNCTION rif40_r_pkg.r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR) IS 'Function:	r_init()
Parameters:	 Default debug level [DEBUG1], CRAN repository [Default:  http://cran.ma.imperial.ac.uk/]
Returns:	 Nothing
Description: R initialisation function. Call once per session';

--
-- Grants
--
GRANT EXECUTE ON FUNCTION rif40_r_pkg.install_all_packages(boolean) TO rif_manager, rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.installed_packages() TO rif_manager, rif_user, rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.install_package_from_internet(VARCHAR) TO rif_manager, rif_user, rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg._install_all_packages_from_internet(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR) TO rif_manager, rif_user, rif40;

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
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_rif40_r_pkg', 'Optional PL/R extension not loaded');
	END IF;

END;
$$;

END;

\echo Created PG psql code (Optional R support).

--
-- Eof