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
			
CREATE OR REPLACE FUNCTION rif40_r_pkg.install_all_packages(use_internet BOOLEAN DEFAULT TRUE)
RETURNS void
AS $func$
DECLARE	
	 c1_r2 CURSOR FOR
		SELECT *
		  FROM pg_settings 
		 WHERE name = 'data_directory' /* 'C:/Program Files/PostgreSQL/9.3/data' */;
	c1_rec RECORD;
--
	error_message VARCHAR;
	v_detail VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
	OPEN c1_r2;
	FETCH c1_r2 INTO c1_rec;
	CLOSE c1_r2;
--
	IF use_internet THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'install_all_packages', 'Load packages from internet');
		BEGIN
			PERFORM rif40_r_pkg._install_all_packages_from_internet(c1_rec.setting); -- data_directory PG_DATA 
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

DROP FUNCTION IF EXISTS rif40_r_pkg._install_all_packages_from_internet(VARCHAR);
CREATE OR REPLACE FUNCTION rif40_r_pkg._install_all_packages_from_internet(pg_data VARCHAR)
RETURNS void
AS $func$
#
# Error handler
#
#options(error = traceback())

#
# R runs in: C:\Program Files\PostgreSQL\9.3\data
# (not R_HOME)
#
Rmessages <- file("R_io/Rmessages.txt", open = "wt")
Routput <- file("R_io/Routput.txt", open = "wt")
sink(Rmessages)
sink(Routput)
sink(Rmessages, type = "message")
sink(Routput, type = "output")
options(verbose=TRUE)

#
# Set R repository
#
r <- getOption("repos")
r["CRAN"] <- "http://cran.ma.imperial.ac.uk/"
options(repos = r)

#
# Postgres R library location
#
#pg_data="C:/Program Files/PostgreSQL/9.3/data"
my_lib=paste(pg_data, "/R_Library", sep='')
print(my_lib)

#
# Update
#
update.packages(checkBuilt=TRUE,lib=my_lib,ask=FALSE,quiet=TRUE)
#
# Install INLA
#
#source("http://www.math.ntnu.no/inla/givemeINLA.R") 
#inla.version()
#
rif40_log <- function(s) {
	sql_stmt=sprintf("SELECT rif40_log_pkg.rif40_log('INFO', '_install_all_packages_from_internet', '%s')", s)
	plan<-pg.spi.exec(sql_stmt)
}
rif40_error <- function(e, s) {
#	sql_stmt=sprintf("SELECT rif40_log_pkg.rif40_error(%d, '_install_all_packages_from_internet', '%s')", e, s)
#	plan<-pg.spi.exec(sql_stmt)
	pg.throwerror(sprintf("%d in _install_all_packages_from_internet(): %s", e, s))
}
#
# Installer function
#
sahsuInstall <- function(plist) {
	for(p in plist ) {
		if (!is.element(p, installed.packages()[,1])) {
			rif40_log(sprintf("Installing %s", p))
			install.packages(p,lib=my_lib,quiet=TRUE)
		}
		else {
			rif40_log(sprintf("%s is already installed", p))
		}
		if (require(p,lib=my_lib,character.only=TRUE)) {
			rif40_log(sprintf("Loaded %s", p))
		}
		else {
			rif40_error(-90125, sprintf("Could not load %s", p))
		}
	}
} 
#
# Install packages
#
sahsu_packages=c("sp", "rgdal", "plyr", "abind")
sahsuInstall(sahsu_packages)
#
# Cleanup
#
remove.packages(sahsu_packages,lib=my_lib)
rm(list=ls())
$func$ LANGUAGE plr;

DROP FUNCTION IF EXISTS rif40_r_pkg.installed_packages();

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
print(rpkgs)
#
return(rpkgs)

$func$ LANGUAGE plr;
--
-- Grants
--
GRANT EXECUTE ON FUNCTION rif40_r_pkg.install_all_packages(boolean) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_r_pkg.installed_packages() TO rif_manager, rif_user;

--
-- Install all required packages
-- 
		PERFORM rif40_r_pkg.install_all_packages();
		
--		PERFORM rif40_sql_pkg.rif40_method4('SELECT rif40_r_pkg.installed_packages()', 'Installed R packages');
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'v4_0_rif40_r_pkg', 'Optional PL/R extension not loaded');
	END IF;
END;
$$;

SELECT * FROM rif40_r_pkg.installed_packages();

\echo Created PG psql code (Optional R support).

--
-- Eof