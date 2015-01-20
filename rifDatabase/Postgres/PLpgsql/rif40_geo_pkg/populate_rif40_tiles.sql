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
-- Rapid Enquiry Facility (RIF) - Populate tile lookup table 
--							      T_RIF40_<GEOGRAPHY>_MAPTILES from simplified geometry
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
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_xml_pkg:
--
-- rif40_GetMapAreas: 		52051 to 52099
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

DROP FUNCTION IF EXISTS rif40_geo_pkg.populate_rif40_tiles(VARCHAR);
	
CREATE OR REPLACE FUNCTION rif40_geo_pkg.populate_rif40_tiles(
	l_geography 		VARCHAR)
RETURNS VOID
SECURITY INVOKER
AS $body$
/*
Function: 		populate_rif40_tiles()
Parameters:		Geography
Returns:		Nothing
Description:	Populate tile lookup table T_RIF40_<GEOGRAPHY>_MAPTILES from simplified geometry

 */
DECLARE
BEGIN

END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.populate_rif40_tiles(VARCHAR) IS 'Function: 		populate_rif40_tiles()
Parameters:		Geography
Returns:		Nothing
Description:	Populate tile lookup table T_RIF40_<GEOGRAPHY>_MAPTILES from simplified geometry';

WITH a AS ( /* level geolevel */
	SELECT a1.geography, a1.geolevel_name,
	       MIN(geolevel_id) AS min_geolevel_id, 
		   a2.max_geolevel_id
	  FROM rif40_geolevels a1, (
			SELECT geography, MAX(geolevel_id) AS max_geolevel_id FROM rif40_geolevels GROUP BY geography) a2
	 WHERE a1.geography = 'SAHSU'
	   AND a1.geography = a2.geography
	 GROUP BY a1.geography, a1.geolevel_name, a2.max_geolevel_id
	HAVING MIN(geolevel_id) = 1
), b AS ( /* Get bounds of geography */
	SELECT b.geography,
	       a.min_geolevel_id,
	       a.max_geolevel_id,
		   8::INTEGER zoomlevel,
		   ST_XMax(optimised_geometry_2) Xmax,
		   ST_XMin(optimised_geometry_2) Xmin,
		   ST_YMax(optimised_geometry_2) Ymax,
		   ST_YMin(optimised_geometry_2) Ymin
      FROM t_rif40_sahsu_geometry b, a
     WHERE a.geography = b.geography
	   AND a.geolevel_name = b.geolevel_name
), d AS ( /* Convert to tile numbers */
	SELECT geography, min_geolevel_id, max_geolevel_id, zoomlevel, 
		   Xmin AS area_Xmin, Xmax AS area_Xmax, Ymin AS area_Ymin, Ymax AS area_Ymax,
           rif40_geo_pkg.latitude2tile(Xmin, zoomlevel) AS X_mintile,
           rif40_geo_pkg.latitude2tile(Xmax, zoomlevel) AS X_maxtile,
           rif40_geo_pkg.longitude2tile(Ymin, zoomlevel) AS Y_mintile,
           rif40_geo_pkg.longitude2tile(Ymax, zoomlevel) AS Y_maxtile
      FROM b
), e AS (
	SELECT min_geolevel_id, 
		   CASE WHEN X_mintile > X_maxtile THEN generate_series(X_mintile, X_maxtile, -1)
				ELSE generate_series(X_mintile, X_maxtile) END AS x_series
	  FROM d
), f AS (
	SELECT min_geolevel_id, 
		   CASE WHEN Y_mintile > Y_maxtile THEN generate_series(Y_mintile, Y_maxtile, -1)
				ELSE generate_series(Y_mintile, Y_maxtile) END AS y_series 
	  FROM d
), g AS (
	SELECT geography, min_geolevel_id, zoomlevel, 
		   area_Xmin, area_Xmax, area_Ymin, area_Ymax, 
		   generate_series(min_geolevel_id::int, max_geolevel_id::int) AS geolevel_series
	  FROM d
), h AS (
	SELECT g.geography, g.geolevel_series AS geolevel_id, zoomlevel, 
		   area_Xmin, area_Xmax, area_Ymin, area_Ymax, 
		   x_series, y_series, h.geolevel_name,
		   g.geography||'_'||g.geolevel_series::Text||'_'||h.geolevel_name||'_'||
				zoomlevel::Text||'_'||x_series::Text||'_'||y_series::Text AS tilename,
	       rif40_geo_pkg.tile2latitude(x_series::INTEGER, zoomlevel) AS tile_Xmin,
	       rif40_geo_pkg.tile2latitude((x_series+1)::INTEGER, zoomlevel) AS tile_Xmax,
	       rif40_geo_pkg.tile2longitude(y_series::INTEGER, zoomlevel) AS tile_Ymin,
	       rif40_geo_pkg.tile2longitude((y_series+1)::INTEGER, zoomlevel) AS tile_Ymax
      FROM rif40_geolevels h, g, e FULL JOIN f ON (e.min_geolevel_id = f.min_geolevel_id)
     WHERE g.min_geolevel_id = e.min_geolevel_id
       AND h.geography       = g.geography
       AND g.geolevel_series = h.geolevel_id
), i AS (
	SELECT geography, geolevel_id, zoomlevel, 
		   area_Xmin, area_Xmax, area_Ymin, area_Ymax, 
		   tile_Xmin, tile_Ymax, tile_Xmax, tile_Ymin, 
		   x_series, y_series, geolevel_name, tilename, 
		   CASE 
					WHEN tile_Xmin > tile_Xmax AND tile_Ymin > tile_Ymax THEN /* Both reversed */
						'X,Y'
					WHEN tile_Xmin > tile_Xmax AND tile_Ymin < tile_Ymax THEN /* X reversed */
						'X'
					WHEN tile_Xmin < tile_Xmax AND tile_Ymin > tile_Ymax THEN /* Y reversed */
						'Y'
					WHEN tile_Xmin < tile_Xmax AND tile_Ymin < tile_Ymax THEN /* Neither reversed */
						'No'
					ELSE 'Err' END AS reversed,
	       ST_Centroid(
				CASE 
					WHEN tile_Xmin > tile_Xmax AND tile_Ymin > tile_Ymax THEN /* Both reversed */
						ST_MakeEnvelope(tile_Xmax, tile_Ymax, tile_Xmin, tile_Ymin)
					WHEN tile_Xmin > tile_Xmax AND tile_Ymin < tile_Ymax THEN /* X reversed */
						ST_MakeEnvelope(tile_Xmax, tile_Ymin, tile_Xmin, tile_Ymax)
					WHEN tile_Xmin < tile_Xmax AND tile_Ymin > tile_Ymax THEN /* Y reversed */
						ST_MakeEnvelope(tile_Xmin, tile_Ymax, tile_Xmax, tile_Ymin)
					WHEN tile_Xmin < tile_Xmax AND tile_Ymin < tile_Ymax THEN /* Neither reversed */
						ST_MakeEnvelope(tile_Xmin, tile_Ymin, tile_Xmax, tile_Ymax)
					ELSE /* Error ! */ NULL END) AS centroid
	  FROM h
), j AS (
	SELECT geography, geolevel_id, zoomlevel, 
		   area_Xmin, area_Xmax, area_Ymin, area_Ymax, 
		   tile_Xmin, tile_Ymax, tile_Xmax, tile_Ymin,
		   x_series, y_series, geolevel_name, tilename, reversed, 
	       ST_X(centroid) AS X_min, ST_Y(centroid) AS Y_min	  
	  FROM i
), k AS (
	SELECT geography, geolevel_id, zoomlevel, 
		   area_Xmin, area_Xmax, area_Ymin, area_Ymax, 
		   tile_Xmin, tile_Ymax, tile_Xmax, tile_Ymin,
		   x_series, y_series, geolevel_name, tilename, reversed,
	       X_min, Y_min, 
		   rif40_geo_pkg.latitude2tile(X_min, zoomlevel) AS X_tile,
		   rif40_geo_pkg.longitude2tile(Y_min, zoomlevel) AS Y_tile
	  FROM j
)
/* SELECT *  
     FROM k ORDER BY 1; */
SELECT tilename, 
       SUBSTRING(rif40_xml_pkg.rif40_get_geojson_tiles(
					geography::VARCHAR, 
					geolevel_name::VARCHAR, 
					tile_Ymax::REAL, 
					tile_Xmax::REAL, 
					tile_Ymin::REAL, 
					tile_Xmin::REAL, 
					zoomlevel::INTEGER)
			FROM 1 FOR 50) AS json
  FROM k
 ORDER BY 1;
 
 --
 -- Eof