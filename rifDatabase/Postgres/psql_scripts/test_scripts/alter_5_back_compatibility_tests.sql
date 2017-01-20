--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- $Id$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF alter script 5 - Zoomlevel support
--								  Back compatibility tests
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

SELECT b.geolevel_id, b.geolevel_name, a.zoomlevel,
       COUNT(a.areaid) AS t_areas, 
       SUM(ST_NPoints(a.geom)) AS t_points, 
	   ROUND((SUM(ST_Area(ST_Transform(a.geom, 27700))))::NUMERIC, 1) AS t_area, /* Transform 4326 => 27700 so in metres */
	   ROUND((SUM(ST_perimeter(ST_Transform(a.geom, 27700))))::NUMERIC, 1) AS t_perimeter,
	   ROUND((SUM(ST_Area(ST_Transform(a.geom, 27700)))/10000001)::NUMERIC, 1) AS t_area_km2, 
	   ROUND((SUM(ST_perimeter(ST_Transform(a.geom, 27700)))/1000)::NUMERIC, 1) AS t_perimeter_km
  FROM geometry_sahsuland a, rif40_geolevels b
 WHERE a.geolevel_id = b.geolevel_id
   AND b.geography = 'SAHSULAND'
 GROUP BY b.geolevel_id, b.geolevel_name, a.zoomlevel
 ORDER BY b.geolevel_id, a.zoomlevel;
 
SELECT a.geolevel_name, 
/*	   ST_Distance_Spheroid(
			ST_GeomFromEWKT('SRID=27700;POINT(0 0)'), 
			ST_GeomFromEWKT('SRID=27700;POINT('||b.st_simplify_tolerance||' 0)'),
			'SPHEROID["Airy 1830",6377563.396,299.3249646]') st_simplify_tolerance_in_m, WRONG - IN DEGREES */
       COUNT(a.area_id) AS t_areas, 
       SUM(ST_NPoints(a.shapefile_geometry)) AS t_points, 
	   ROUND((SUM(ST_Area(a.shapefile_geometry)))::NUMERIC, 1) AS t_area, 
	   ROUND((SUM(ST_perimeter(a.shapefile_geometry)))::NUMERIC, 1) AS t_perimeter,
	   ROUND((SUM(ST_Area(a.shapefile_geometry))/10000001)::NUMERIC, 1) AS t_area_km2, 
	   ROUND((SUM(ST_perimeter(a.shapefile_geometry))/1000)::NUMERIC, 1) AS t_perimeter_km
  FROM t_rif40_sahsu_geometry a, rif40_geolevels b
 WHERE a.geolevel_name = b.geolevel_name
 GROUP BY b.geolevel_id, a.geolevel_name
 ORDER BY b.geolevel_id;

WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSULAND', 'SAHSU_GRD_LEVEL2', '01.004')
), b AS (
	SELECT ST_Centroid(ST_MakeEnvelope(a.x_min, a.y_min, a.x_max, a.y_max)) AS centroid
	  FROM a
), c AS (
	SELECT ST_X(b.centroid) AS X_centroid, ST_Y(b.centroid) AS Y_centroid, 11 AS zoom_level	  
	  FROM b
), d AS (
	SELECT zoom_level, X_centroid, Y_centroid, 
		   rif40_geo_pkg.latitude2tile(Y_centroid, zoom_level) AS Y_tile,
		   rif40_geo_pkg.longitude2tile(X_centroid, zoom_level) AS X_tile
	  FROM c
), e AS (
	SELECT zoom_level, X_centroid, Y_centroid,
		   rif40_geo_pkg.tile2latitude(Y_tile, zoom_level) AS Y_min,
		   rif40_geo_pkg.tile2longitude(X_tile, zoom_level) AS X_min,	
		   rif40_geo_pkg.tile2latitude(Y_tile+1, zoom_level) AS Y_max,
		   rif40_geo_pkg.tile2longitude(X_tile+1, zoom_level) AS X_max,
		   X_tile, Y_tile
	  FROM d
) 
SELECT * FROM e;
 
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
), b AS (
	SELECT ST_Centroid(ST_MakeEnvelope(a.x_min, a.y_min, a.x_max, a.y_max)) AS centroid
	  FROM a
), c AS (
	SELECT ST_X(b.centroid) AS X_centroid, ST_Y(b.centroid) AS Y_centroid, 11 AS zoom_level	  
	  FROM b
), d AS (
	SELECT zoom_level, X_centroid, Y_centroid, 
		   rif40_geo_pkg.latitude2tile(Y_centroid, zoom_level) AS Y_tile,
		   rif40_geo_pkg.longitude2tile(X_centroid, zoom_level) AS X_tile
	  FROM c
), e AS (
	SELECT zoom_level, X_centroid, Y_centroid,
		   rif40_geo_pkg.tile2latitude(Y_tile, zoom_level) AS Y_min,
		   rif40_geo_pkg.tile2longitude(X_tile, zoom_level) AS X_min,	
		   rif40_geo_pkg.tile2latitude(Y_tile+1, zoom_level) AS Y_max,
		   rif40_geo_pkg.tile2longitude(X_tile+1, zoom_level) AS X_max,
		   X_tile, Y_tile
	  FROM d
) 
SELECT * FROM e;

WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSULAND', 'SAHSU_GRD_LEVEL2', '01.004')
) 
SELECT rif40_xml_pkg.rif40_GetMapAreas(
			'SAHSULAND' /* Geography */, 
			'SAHSU_GRD_LEVEL4' 	/* geolevel view */, 
			a.y_max, a.x_max, a.y_min, a.x_min /* Bounding box - from cte */) AS json 
  FROM a LIMIT 4;
  
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
) 
SELECT rif40_xml_pkg.rif40_GetMapAreas(
			'SAHSU' /* Geography */, 
			'LEVEL4' 	/* geolevel view */, 
			a.y_max, a.x_max, a.y_min, a.x_min /* Bounding box - from cte */) AS json 
  FROM a LIMIT 4;

WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSULAND', 'SAHSU_GRD_LEVEL2', '01.004')
), b AS (
	SELECT ST_Centroid(ST_MakeEnvelope(a.x_min, a.y_min, a.x_max, a.y_max)) AS centroid
	  FROM a
), c AS (
	SELECT ST_X(b.centroid) AS X_centroid, ST_Y(b.centroid) AS Y_centroid, 11 AS zoom_level	  
	  FROM b
), d AS (
	SELECT zoom_level, X_centroid, Y_centroid, 
		   rif40_geo_pkg.latitude2tile(Y_centroid, zoom_level) AS Y_tile,
		   rif40_geo_pkg.longitude2tile(X_centroid, zoom_level) AS X_tile
	  FROM c
), e AS (
	SELECT zoom_level, X_centroid, Y_centroid,
		   rif40_geo_pkg.tile2latitude(Y_tile, zoom_level) AS Y_min,
		   rif40_geo_pkg.tile2longitude(X_tile, zoom_level) AS X_min,	
		   rif40_geo_pkg.tile2latitude(Y_tile+1, zoom_level) AS Y_max,
		   rif40_geo_pkg.tile2longitude(X_tile+1, zoom_level) AS X_max,
		   X_tile, Y_tile
	  FROM d
) 
SELECT SUBSTRING(
		rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			e.y_max::REAL, e.x_max::REAL, e.y_min::REAL, e.x_min::REAL, /* Bounding box - from cte */
			e.zoom_level::INTEGER /* Zoom level */,
			FALSE /* Check tile co-ordinates [Default: FALSE] */,
			TRUE /* Lack of topoJSON is an error [DEFAULT] */)::Text 
			FROM 1 FOR 160 /* Truncate to 160 chars */) AS json 
  FROM e LIMIT 4;
  
WITH a AS (
	SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea('SAHSU', 'LEVEL2', '01.004')
), b AS (
	SELECT ST_Centroid(ST_MakeEnvelope(a.x_min, a.y_min, a.x_max, a.y_max)) AS centroid
	  FROM a
), c AS (
	SELECT ST_X(b.centroid) AS X_centroid, ST_Y(b.centroid) AS Y_centroid, 11 AS zoom_level	  
	  FROM b
), d AS (
	SELECT zoom_level, X_centroid, Y_centroid, 
		   rif40_geo_pkg.latitude2tile(Y_centroid, zoom_level) AS Y_tile,
		   rif40_geo_pkg.longitude2tile(X_centroid, zoom_level) AS X_tile
	  FROM c
), e AS (
	SELECT zoom_level, X_centroid, Y_centroid,
		   rif40_geo_pkg.tile2latitude(Y_tile, zoom_level) AS Y_min,
		   rif40_geo_pkg.tile2longitude(X_tile, zoom_level) AS X_min,	
		   rif40_geo_pkg.tile2latitude(Y_tile+1, zoom_level) AS Y_max,
		   rif40_geo_pkg.tile2longitude(X_tile+1, zoom_level) AS X_max,
		   X_tile, Y_tile
	  FROM d
) 
SELECT SUBSTRING(
		rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSU'::VARCHAR 	/* Geography */, 
			'LEVEL4'::VARCHAR 	/* geolevel view */, 
			e.y_max::REAL, e.x_max::REAL, e.y_min::REAL, e.x_min::REAL, /* Bounding box - from cte */
			e.zoom_level::INTEGER /* Zoom level */,
			FALSE /* Check tile co-ordinates [Default: FALSE] */,
			TRUE /* Lack of topoJSON is an error [DEFAULT] */)::Text 
			FROM 1 FOR 160 /* Truncate to 160 chars */) AS json 
  FROM e LIMIT 4;

/*

NEW:

* Has BBOX!
* Changed: 
	id to gid
	areaID to area_id
	areaName to name
	
                 json

------------------------------------------------------------------------------------------------------------------------------------
 {"type":"Topology","objects":{"collection":{"type":"GeometryCollection","bbox":[-6.82275391169586,53.207109234967,-5.5204382207014,
54.0446078939211],"geometries":[{"type":"Polygon","properties":{"id":1130,"areaID":"01.013.016800.3","areaName":"01.013.016800.3","x
":989,"y":660,"block":"19","sahsu_grd
(1 row)

i.e.

 {"type":"Topology","objects":{"collection":{"type":"GeometryCollection","bbox":[-6.82275391169586,53.207109234967,-5.5204382207014,
54.0446078939211],"geometries":[{"type":"Polygon","properties":{"gid":1130,"area_id":"01.013.016800.3","name":"01.013.016800.3","x":
989,"y":660,"block":"19","sahsu_grd_l
(1 row)

OLD:

                 json

------------------------------------------------------------------------------------------------------------------------------------
 {"type":"Topology","objects":{"11_989_660":{"type":"GeometryCollection","geometries":[{"type":"Polygon","properties":{"name":"Kozni
ewska LEVEL4(01.013.016800.3)","area_id":"01.013.016800.3","gid":1116},"id":1116,"arcs":[[0,1,2]]},{"type":"Polygon","properties":{"
name":"Maitland LEVEL4(01.015.016200.
(1 row)

 */  
SELECT substring(rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSULAND'::VARCHAR 	/* Geography */, 
			'SAHSU_GRD_LEVEL4'::VARCHAR 	/* geolevel view */, 
			11::INTEGER 		/* Zoom level */,
			989::INTEGER 		/* X tile number */,
			660::INTEGER		/* Y tile number */)::Text from 1 for 300) AS json; 

SELECT substring(rif40_xml_pkg.rif40_get_geojson_tiles(
			'SAHSU'::VARCHAR 	/* Geography */, 
			'LEVEL4'::VARCHAR 	/* geolevel view */, 
			11::INTEGER 		/* Zoom level */,
			989::INTEGER 		/* X tile number */,
			660::INTEGER		/* Y tile number */)::Text from 1 for 300) AS json; 
  
--
-- Eof