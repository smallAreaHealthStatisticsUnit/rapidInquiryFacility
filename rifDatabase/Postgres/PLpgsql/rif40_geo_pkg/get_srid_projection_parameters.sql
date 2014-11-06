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
-- Rapid Enquiry Facility (RIF) - Create PG psql code (Geographic processing)
--								  add_population_to_rif40_geolevels_geometry() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.get_srid_projection_parameters(l_geography VARCHAR, l_param VARCHAR)
RETURNS VARCHAR 
SECURITY INVOKER
AS $body$
/*
Function: 	get_srid_projection_parameters()
Parameters:	Geography, projection parameter
Returns:	Projection parameter value
Description:	Get projection parameter value from SPATIAL_REF_SYS for geography

Typically used to get the units (+units). 

WITH a AS (
	SELECT string_to_array(unnest(string_to_array(proj4text, ' ')), '=') AS projparams 
	  FROM spatial_ref_sys
	 WHERE srid = 27700
)
SELECT projparams[1] AS param, projparams[2] AS value
  FROM a
 WHERE projparams[1] IS NOT NULL;

  param   |                      value                       
----------+--------------------------------------------------
 +proj    | tmerc
 +lat_0   | 49
 +lon_0   | -2
 +k       | 0.9996012717
 +x_0     | 400000
 +y_0     | -100000
 +ellps   | airy
 +towgs84 | 446.448,-125.157,542.06,0.15,0.247,0.842,-20.489
 +units   | m
 +no_defs | 
(10 rows)
 */
DECLARE
	c1_srid CURSOR(l_geography VARCHAR, l_param VARCHAR) FOR
		WITH a AS (
			SELECT string_to_array(unnest(string_to_array(proj4text, ' ')), '=') AS projparams 
			  FROM spatial_ref_sys
			 WHERE srid = (SELECT srid FROM rif40_geographies WHERE geography = l_geography)
		)
		SELECT projparams[1] AS param, projparams[2] AS value
		  FROM a
		 WHERE projparams[1] = l_param;
	c1_rec RECORD;
BEGIN
	OPEN c1_srid(l_geography, l_param);
	FETCH c1_srid INTO c1_rec;
	CLOSE c1_srid;
--
	IF c1_rec.param IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-19001, 'get_srid_projection_parameters', 'Geography (%) or projection parameter (%) is invalid', 
			l_geography::VARCHAR 	/* Geography */,
			l_param::VARCHAR	/* Projection parameter */);
	END IF;
--
	RETURN c1_rec.value;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) IS 'Function: 	get_srid_projection_parameters()
Parameters:	Geography, projection parameter
Returns:	Projection parameter value
Description:	Get projection parameter value from SPATIAL_REF_SYS for geography

Typically used to get the units (+units). 

WITH a AS (
	SELECT string_to_array(unnest(string_to_array(proj4text, '' '')), ''='') AS projparams 
	  FROM spatial_ref_sys
	 WHERE srid = 27700
)
SELECT projparams[1] AS param, projparams[2] AS value
  FROM a
 WHERE projparams[1] IS NOT NULL;

  param   |                      value                       
----------+--------------------------------------------------
 +proj    | tmerc
 +lat_0   | 49
 +lon_0   | -2
 +k       | 0.9996012717
 +x_0     | 400000
 +y_0     | -100000
 +ellps   | airy
 +towgs84 | 446.448,-125.157,542.06,0.15,0.247,0.842,-20.489
 +units   | m
 +no_defs | 
(10 rows)';

--
-- Eof