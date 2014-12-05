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
		'install_package_from_internet',
		'_install_package_from_internet',
		'install_all_packages_from_internet',
		'_install_all_packages_from_internet'];
	l_function 					VARCHAR;
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
	sql_stmt=sprintf("SELECT rif40_log_pkg.rif40_log(''%s'', ''%s'', %s)", w, f, pg.quoteliteral(s))
	tryCatch(
        {
			plan<-pg.spi.exec(sql_stmt)
        },
        error=function(sql_error) {
#
# DO NOT throw an error - it will crash postgres!
#			pg.throwerror(sprintf("%d in %s(): caught error %s in SQL statement\nSQL>%s", -60606, f, sql_error, sql_stmt))
            stop(sprintf("rif40_log() caught error: %s in SQL statement\nSQL>%s", sql_error, sql_stmt))
        },
        warning=function(sql_warning) {
            stop(sprintf("rif40_log() caught warning: %s in SQL statement", sql_warning, sql_stmt))
        }
    )    
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
install_package_from_internet<-function(p) {
		if (!is.element(p, installed.packages(lib=my_lib)[,1])) {
			rif40_log("INFO", "install_package_from_internet", sprintf("Installing %s", p))
			rif40_log("DEBUG1", "install_package_from_internet", 
				sprintf("update.packages() >>>\n%s\n<<<", 
					toString(
						capture.output(
							install.packages(p,lib=my_lib,quiet=TRUE)))))
		}
		else {
			rif40_log("INFO", "install_package_from_internet", sprintf("%s is already installed", p))
		}
		global.loaded=NULL
		rif40_log("DEBUG1", "install_package_from_internet", 
				sprintf("require() >>>\n%s\n<<<", 
					toString(
						capture.output(
							global.loaded<--require(p,lib=my_lib,character.only=TRUE)))))
		if (global.loaded) {
			rif40_log("INFO", "install_package_from_internet", sprintf("Loaded %s", p))
		}
		else {
			rif40_error(-90125, "install_package_from_internet", sprintf("Could not load %s", p))
		}
}

install_all_packages_from_internet<-function(plist) {
	for(p in plist) {
		install_package_from_internet(p)
	}
}');
--
-- Set R repository
--
		PERFORM install_rcmd('r <- getOption("repos")
r["CRAN"] <- "'||cran_repository||'"
options(repos = r)');
--
-- Set my_lib (Postgres R library location)
--
		PERFORM install_rcmd('my_lib=paste("'||c1_rec.setting /* pg_data */||'", "/R_Library", sep='''')');
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
Rmessages <- file("R_io/Rmessages.txt", open = "wt")
Routput <- file("R_io/Routput.txt", open = "wt")
sink(Rmessages)
sink(Routput)
sink(Rmessages, type = "message")
sink(Routput, type = "output")

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
rif40_log("DEBUG1", "_r_init", sprintf("ls() >>>\n%s\n<<<", toString(ls())))

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

rif40_log("DEBUG1", "_install_all_packages_from_internet", sprintf("ls() >>>\n%s\n<<<", toString(ls())))

#
# Update
#
rif40_log("INFO", "_install_all_packages_from_internet", 
	sprintf("update.packages() >>>\n%s\n<<<", 
		toString(
			capture.output(
				update.packages(checkBuilt=TRUE,lib=my_lib,ask=FALSE,quiet=TRUE)))))
#
# Install INLA
#
#source("http://www.math.ntnu.no/inla/givemeINLA.R") 
#inla.version()

#
# Install packages
#
sahsu_packages=c("sp", "rgdal", "plyr", "abind")
install_all_packages_from_internet(sahsu_packages)
#
# Cleanup
#
remove.packages(sahsu_packages,lib=my_lib)
rif40_log("DEBUG1", "_install_all_packages_from_internet", sprintf("ls() >>>\n%s", toString(ls())))
#rm(list)
$func$ LANGUAGE plr;

CREATE OR REPLACE FUNCTION rif40_r_pkg.install_package_from_internet(package_name VARCHAR)
RETURNS void
AS $func$

rif40_log("DEBUG1", "install_package_from_internet", sprintf("ls() >>>\n%s\n<<<", toString(ls())))

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
# List libraries
#
pkgs<-installed.packages()
colnames(pkgs)
rpkgs<-pkgs[,c(1,2,3,10,13,16)]
#print(rpkgs)
#
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
Description: All all required packages';
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
		PERFORM rif40_r_pkg.install_all_packages();
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_rif40_r_pkg', 'Optional PL/R extension not loaded');
	END IF;
END;
$$;

END;

\echo Created PG psql code (Optional R support).

--
-- Eof