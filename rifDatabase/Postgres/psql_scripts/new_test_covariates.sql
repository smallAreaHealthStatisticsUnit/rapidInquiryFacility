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
-- Rapid Enquiry Facility (RIF) - Drop all (postgres) objects
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

--
-- Check user is not rif40/postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user NOT IN ('postgres', 'rif40') THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is rif40/postgres', user;	
	END IF;
END;
$$;

--
-- Check database is sahsuland_dev
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev', current_database();	
	END IF;
END;
$$;


BEGIN;

DROP TABLE IF EXISTS sahsuland_tri_level4;
CREATE TABLE sahsuland_tri_level4
AS
WITH a AS (
	SELECT DISTINCT level4, tri_1km, near_dist, areatri1km 
	  FROM sahsuland_covariates_level4
)
SELECT a.*, NTILE(5) OVER w1 AS near_dist_quintiles 
  FROM a 
	WINDOW w1 AS (ORDER BY near_dist)
 ORDER BY 1;  
CREATE UNIQUE INDEX sahsuland_tri_level4_pk ON sahsuland_tri_level4(level4); 
ALTER TABLE sahsuland_tri_level4 ADD CONSTRAINT sahsuland_tri_level4_pk PRIMARY KEY USING INDEX sahsuland_tri_level4_pk;

\COPY sahsuland_tri_level4 TO '../../dataload_example_data/sahsuland_tri_level4.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\', HEADER);
-- For editors' sake
 
SELECT near_dist_quintiles, COUNT(near_dist_quintiles) AS quintile, MIN(near_dist) AS min_val, MAX(near_dist) AS max_val 
  FROM sahsuland_tri_level4
 GROUP BY near_dist_quintiles
 ORDER BY 1;
   
DROP TABLE sahsuland_tri_level4;

DROP TABLE IF EXISTS sahsuland_ses_level4;
CREATE TABLE sahsuland_ses_level4
AS
SELECT DISTINCT level4, ses 
  FROM sahsuland_covariates_level4
 ORDER BY 1;  
CREATE UNIQUE INDEX sahsuland_ses_level4_pk ON sahsuland_ses_level4(level4); 
ALTER TABLE sahsuland_ses_level4 ADD CONSTRAINT sahsuland_ses_level4_pk PRIMARY KEY USING INDEX sahsuland_ses_level4_pk;

\COPY sahsuland_ses_level4 TO '../../dataload_example_data/sahsuland_ses_level4.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\', HEADER);
-- For editors' sake
 
DROP TABLE sahsuland_ses_level4;

DROP TABLE IF EXISTS sahsuland_ses_level3;
CREATE TABLE sahsuland_ses_level3
AS
SELECT DISTINCT level3, ses 
  FROM sahsuland_covariates_level3
 ORDER BY 1;  
CREATE UNIQUE INDEX sahsuland_ses_level3_pk ON sahsuland_ses_level3(level3); 
ALTER TABLE sahsuland_ses_level3 ADD CONSTRAINT sahsuland_ses_level3_pk PRIMARY KEY USING INDEX sahsuland_ses_level3_pk;

\COPY sahsuland_ses_level3 TO '../../dataload_example_data/sahsuland_ses_level3.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\', HEADER);
-- For editors' sake
 
DROP TABLE sahsuland_ses_level3;

DROP TABLE IF EXISTS sahsuland_ethnicity_level3;
CREATE TABLE sahsuland_ethnicity_level3
AS
SELECT DISTINCT level3, ethnicity 
  FROM sahsuland_covariates_level3
 ORDER BY 1;  
CREATE UNIQUE INDEX sahsuland_ethnicity_level3_pk ON sahsuland_ethnicity_level3(level3); 
ALTER TABLE sahsuland_ethnicity_level3 ADD CONSTRAINT sahsuland_ethnicity_level3_pk PRIMARY KEY USING INDEX sahsuland_ethnicity_level3_pk;

\COPY sahsuland_ethnicity_level3 TO '../../dataload_example_data/sahsuland_ethnicity_level3.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\', HEADER);
-- For editors' sake
 
DROP TABLE sahsuland_ethnicity_level3;

END;

--
-- Eof
