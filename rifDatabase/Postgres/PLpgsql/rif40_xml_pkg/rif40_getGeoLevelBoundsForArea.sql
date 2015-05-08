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
-- Rapid Enquiry Facility (RIF) - Web services integration functions for middleware
--     				  Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
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
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_getGeoLevelBoundsForArea(
	l_geography 	VARCHAR,
	l_geolevel_view	VARCHAR,
	l_map_area	VARCHAR,
	OUT y_max 	REAL,
	OUT x_max 	REAL, 
	OUT y_min 	REAL, 
	OUT x_min 	REAL)
RETURNS RECORD
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_getGeoLevelBoundsForArea()
Parameters:	Geography, geolevel_view, map area id
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision). In WGS 84 (4326)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view> <map area ID>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Must be rif40 or have rif_user or rif_manager role

Calls: rif40_xml_pkg._rif40_getGeoLevelExtentCommon() to:

Test geography exists
Test <geolevel view> exists
Check map area ID exists

Generates map area id SQL variant>

WITH a AS (
        SELECT ST_Extent(optimised_geometry) AS g
          FROM t_rif40_sahsu_geometry /- SAHSU -/
         WHERE geolevel_name = $1 /- <Geolevel view> -/
           AND area_id       = $2 /- <Map area ID> -/
        )
        SELECT ST_Ymax(g)::REAL AS ymax, ST_Xmax(g)::REAL AS xmax, ST_Ymin(g)::REAL AS ymin, ST_Xmin(g)::REAL AS xmin
          FROM a;

e.g.
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004');
  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.0122 | -6.32507 | 54.6456 | -6.68853

Generated SQL can raise exceptions; these are trapped, printed to INFO and re-raised
 */
DECLARE
	c1_rec RECORD;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-50000, 'rif40_getGeoLevelBoundsForArea', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
	c1_rec:=rif40_xml_pkg._rif40_getGeoLevelExtentCommon(l_geography, l_geolevel_view, 
		l_map_area /* map area (single <geolevel view> area id) */, NULL /* or, study id */);
--
-- Set up return values
--
	y_max:=c1_rec.y_max;
	x_max:=c1_rec.x_max;
	y_min:=c1_rec.y_min;
	x_min:=c1_rec.x_min;
--
	RETURN; 
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_getGeoLevelBoundsForArea(VARCHAR, VARCHAR, VARCHAR, 
	OUT REAL, OUT REAL, OUT REAL, OUT REAL) IS 'Function: 	rif40_getGeoLevelBoundsForArea()
Parameters:	Geography, geolevel_view, map area id
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision). In WGS 84 (4326)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view> <map area ID>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Must be rif40 or have rif_user or rif_manager role

Calls: rif40_xml_pkg._rif40_getGeoLevelExtentCommon() to:

Test geography exists
Test <geolevel view> exists
Check map area ID exists

Generates map area id SQL variant>

WITH a AS (
        SELECT ST_Extent(optimised_geometry) AS g
          FROM t_rif40_sahsu_geometry /* SAHSU */
         WHERE geolevel_name = $1 /* <Geolevel view> */
           AND area_id       = $2 /* <Map area ID> */
        )
        SELECT ST_Ymax(g)::REAL AS ymax, ST_Xmax(g)::REAL AS xmax, ST_Ymin(g)::REAL AS ymin, ST_Xmin(g)::REAL AS xmin
          FROM a;

e.g.
SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea(''SAHSU'', ''LEVEL2'', ''01.004'');
  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.0122 | -6.32507 | 54.6456 | -6.68853

Generated SQL can raise exceptions; these are trapped, printed to INFO and re-raised';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_getGeoLevelBoundsForArea(VARCHAR, VARCHAR, VARCHAR,
	OUT REAL, OUT REAL, OUT REAL, OUT REAL) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_getGeoLevelBoundsForArea(VARCHAR, VARCHAR, VARCHAR,
	OUT REAL, OUT REAL, OUT REAL, OUT REAL) TO rif_user;

--
-- Eof
