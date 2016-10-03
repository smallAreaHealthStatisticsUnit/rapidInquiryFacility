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
-- Rapid Enquiry Facility (RIF) - Create PG psql code zoomlevel display function
--								  Tile conversion functions from:
--								  http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Tools
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

/*
 --------------------------------------------
Web mercator (SRID:4326) degree to km table 

| places | degrees    | distance |
| ------ | ---------- | -------- |
| 0      | 1.0        | 111 km   |
| 1      | 0.1        | 11.1 km  |
| 2      | 0.01       | 1.11 km  |
| 3      | 0.001      | 111 m    |
| 4      | 0.0001     | 11.1 m   |
| 5      | 0.00001    | 1.11 m   |
| 6      | 0.000001   | 0.111 m  |
| 7      | 0.0000001  | 1.11 cm  |
| 8      | 0.00000001 | 1.11 mm  |


--------------------------------------------
OSM/Leaflet - Zoom Levels
http://wiki.openstreetmap.org/wiki/Zoom_levels 

| Level	 |	  Degree	 |	Area	  |  m / pixel |	~Scale
 -------     ---------     --------     --------       -----------
|0		 |	   360	     | whole world|	 156,412	|	1:500 Mio
|1		 |	   180	   	 |			  |   78,206	|	1:250 Mio
|2		 |		90		 |			  |   39,103	|	1:150 Mio
|3		 |		45		 |			  |   19,551	|	1:70 Mio
|4		 |		22.5	 |			  |    9,776	|	1:35 Mio
|5		 |		11.25	 |			  |    4,888	|   1:15 Mio
|6		 |		5.625	 |			  |    2,444	|	1:10 Mio
|7		 |		2.813	 |			  |    1,222	|   1:4 Mio
|8		 |		1.406	 |			  |  610.984	|	1:2 Mio
|9		 |		0.703	 | wide area  |	 305.492	|	1:1 Mio
|10		 |		0.352	 |			  |  152.746	|	1:500,000
|11		 |		0.176	 | area		  |   76.373	|   1:250,000
|12		 |		0.088	 |			  |   38.187	|   1:150,000
|13		 |		0.044	 | town		  |   19.093	|   1:70,000
|14		 |		0.022	 | village	  |    9.547	|   1:35,000
|15		 |		0.011	 |			  |    4.773	|   1:15,000
|16		 |		0.005	 | small road |	   2.387	|    1:8,000
|17		 |		0.003	 |			  |    1.193	|    1:4,000
|18		 |		0.001	 |			  |    0.596	|    1:2,000
|19		 |		0.0005	 |	          |    0.298	|    1:1,000

The 'degree' column gives the map width in degrees, for map at that zoom level which is 256 pixels wide. 
The values for "m / pixel" are calculated with an earth radius of 6372.7982 km. "Scale" (map scale) is 
only an approximate size comparison and refers to distances on the equator. In addition, the map scale 
will be dependent on the monitor. These values are for a monitor with a 0.3 mm / pixel (about 85.2 American DPI)

Metres per pixel math:

The distance represented by one pixel (S) is given by
S=C*cos(y)/2^(z+8)
where...
C is the (equatorial) circumference of the Earth
z is the zoom level
y is the latitude of where you're interested in the scale.
Make sure your calculator is in degrees mode, unless you want to express latitude in radians for some reason. C should be expressed in whatever scale unit you're interested in (miles, meters, feet, smoots, whatever). Since the earth is actually ellipsoidal, there will be a slight error in this calculation. But it's very slight. (0.3% maximum error)
See also

Assume 256 pixels = 67.33 mm
*/

DROP FUNCTION IF EXISTS rif40_geo_pkg.rif40_zoom_levels(NUMERIC);
		
--
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_zoom_levels:							60000 to 60099
--
CREATE OR REPLACE FUNCTION rif40_geo_pkg.rif40_zoom_levels(
		l_latitude 			NUMERIC	DEFAULT 0 /* Equator - degrees in projection 4326 */)
RETURNS TABLE(
		zoom_level			INTEGER, 
		latitude			NUMERIC,
		tiles				BIGINT,
		degrees_per_tile	NUMERIC,
		m_x_per_pixel_est	NUMERIC,
		m_x_per_pixel		NUMERIC,
		m_y_per_pixel		NUMERIC,
		m_x					BIGINT,
		m_y					BIGINT,
		simplify_tolerance	NUMERIC,
		scale				TEXT
		)
SECURITY INVOKER
AS $func$
/*
Function: 	 rif40_zoom_levels()
Parameters:	 Latitude (default 0 - equator)
Returns:	 Table of zoomlevel results
Description: Displays zoom level table, calculating m/pixel and scale accurately

OSM/Leaflet - Zoom Levels
http://wiki.openstreetmap.org/wiki/Zoom_levels 

SELECT * FROM rif40_geo_pkg.rif40_zoom_levels();
psql:alter_scripts/v4_0_alter_5.sql:134: INFO:  [DEBUG1] rif40_zoom_levels(): [60001] latitude: 0
 zoom_level | latitude |    tiles     | degrees_per_tile | m_x_per_pixel_est | m_x_per_pixel | m_y_per_pixel |   m_x    |   m_y    | simplify_tolerance |      scale
------------+----------+--------------+------------------+-------------------+---------------+---------------+----------+----------+--------------------+------------------
          0 |        0 |            1 |              360 |            156412 |        155497 |               | 39807187 |          |               1.40 | 1 in 591,225,112
          1 |        0 |            4 |              180 |             78206 |         77748 |               | 19903593 |          |               0.70 | 1 in 295,612,556
          2 |        0 |           16 |               90 |             39103 |         39136 |         39070 | 10018754 | 10001966 |               0.35 | 1 in 148,800,745
          3 |        0 |           64 |               45 |             19552 |         19568 |         19472 |  5009377 |  4984944 |               0.18 | 1 in 74,400,373
          4 |        0 |          256 |             22.5 |              9776 |          9784 |          9723 |  2504689 |  2489167 |               0.09 | 1 in 37,200,186
          5 |        0 |         1024 |            11.25 |              4888 |          4892 |          4860 |  1252344 |  1244120 |               0.04 | 1 in 18,600,093
          6 |        0 |         4096 |            5.625 |              2444 |          2446 |          2430 |   626172 |   622000 |              0.022 | 1 in 9,300,047
          7 |        0 |        16384 |            2.813 |              1222 |          1223 |          1215 |   313086 |   310993 |              0.011 | 1 in 4,650,023
          8 |        0 |        65536 |            1.406 |               611 |           611 |           607 |   156543 |   155495 |             0.0055 | 1 in 2,325,012
          9 |        0 |       262144 |            0.703 |               305 |           306 |           304 |    78272 |    77748 |             0.0027 | 1 in 1,162,506
         10 |        0 |      1048576 |            0.352 |               153 |           153 |           152 |    39136 |    38874 |             0.0014 | 1 in 581,253
         11 |        0 |      4194304 |            0.176 |                76 |            76 |            76 |    19568 |    19437 |            0.00069 | 1 in 290,626
         12 |        0 |     16777216 |            0.088 |                38 |            38 |            38 |     9784 |     9718 |            0.00034 | 1 in 145,313
         13 |        0 |     67108864 |            0.044 |                19 |            19 |            19 |     4892 |     4859 |            0.00017 | 1 in 72,657
         14 |        0 |    268435456 |            0.022 |               9.5 |           9.6 |           9.5 |     2446 |     2430 |          0.0000858 | 1 in 36,328
         15 |        0 |   1073741824 |            0.011 |               4.8 |           4.8 |           4.7 |     1223 |     1215 |          0.0000429 | 1 in 18,164
         16 |        0 |   4294967296 |            0.005 |               2.4 |           2.4 |           2.4 |      611 |      607 |          0.0000215 | 1 in 9,082
         17 |        0 |  17179869184 |            0.003 |              1.19 |          1.19 |          1.19 |      306 |      304 |          0.0000107 | 1 in 4,541
         18 |        0 |  68719476736 |           0.0014 |              0.60 |          0.60 |          0.59 |      153 |      152 |          0.0000054 | 1 in 2,271
         19 |        0 | 274877906944 |          0.00069 |              0.30 |          0.30 |          0.30 |       76 |       76 |          0.0000027 | 1 in 1,135
(20 rows)

Time: 2.648 ms
 */
DECLARE
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- User must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-60000, 'rif40_zoom_levels', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Display l_latitude
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_zoom_levels', '[60001] latitude: %', 
			l_latitude::VARCHAR);
--
	BEGIN
		IF ABS(l_latitude) >= 90 THEN
			PERFORM rif40_log_pkg.rif40_error(-60002, 'rif40_zoom_levels', '[60002] Invalid latitude: %', 
				l_latitude::VARCHAR);
		END IF;
--
		RETURN QUERY	
			WITH a AS (
				SELECT generate_series(0, 19, 1)::INTEGER l_zoomlevel, 
					   substring(a1.spheroid, 1, position(',AUTHORITY[' in a1.spheroid)-1)||']' AS spheroid
				  FROM (
						SELECT substring(srtext, position('SPHEROID[' in srtext)) AS spheroid
						  FROM spatial_ref_sys	
						 WHERE srid = 4326) a1
			), b AS (
				SELECT a.l_zoomlevel, 
				       pow(2, a.l_zoomlevel+a.l_zoomlevel)::BIGINT AS tiles,
				       (360/pow(2, a.l_zoomlevel))::NUMERIC AS degrees_per_tile,
				       ((6372798.2 /* earth radius - m */ * cos(l_latitude)*2*pi())/pow(2, a.l_zoomlevel+8))::NUMERIC AS m_x_per_pixel_est,
					   (CASE 
							WHEN a.l_zoomlevel = 0 THEN 
								ST_Distance_Spheroid(
									ST_setsrid(ST_Makepoint(0, l_latitude), 4326),
									ST_setsrid(ST_Makepoint(180, l_latitude), 4326),
									'SPHEROID["WGS 84",6378137,298.257223563]')*2/256
							ELSE
								ST_Distance_Spheroid(
									ST_setsrid(ST_Makepoint(0, l_latitude) /* x is longitude and y is latitude */, 4326),
									ST_setsrid(ST_Makepoint(360/pow(2, a.l_zoomlevel), l_latitude), 4326),
									'SPHEROID["WGS 84",6378137,298.257223563]')/256 END)::NUMERIC AS m_x_per_pixel,
					   (CASE 
							WHEN l_latitude+360/pow(2, a.l_zoomlevel) > 90 THEN NULL
							ELSE ST_Distance_Spheroid(
									ST_setsrid(ST_Makepoint(0, l_latitude), 4326),
									ST_setsrid(ST_Makepoint(0, l_latitude+360/pow(2, a.l_zoomlevel)), 4326),
									'SPHEROID["WGS 84",6378137,298.257223563]')/256 END)::NUMERIC AS m_y_per_pixel
				  FROM a
			), c AS (
				SELECT ST_Distance_Spheroid( /* Calculate how long one WGS84 unit (i.e. degree is in metres) */
					   ST_GeomFromEWKT('SRID=4326;POINT(0 '||l_latitude||')'), /* POINT(long latitude) */
					   ST_GeomFromEWKT('SRID=4326;POINT(1 '||l_latitude||')'),
							'SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]]'::spheroid) AS one_unit_in_m
			)
			SELECT b.l_zoomlevel, 
			       l_latitude AS latitude,
				   b.tiles,
				   ROUND(b.degrees_per_tile, 
						(CASE
							WHEN b.l_zoomlevel < 4					THEN 0
							WHEN b.l_zoomlevel = 4					THEN 1
							WHEN b.l_zoomlevel = 5					THEN 2
							WHEN b.l_zoomlevel BETWEEN 5 AND 17		THEN 3
							WHEN b.l_zoomlevel = 18					THEN 4
							ELSE 5 END)::INT) AS degrees_per_tile,
				   ROUND(b.m_x_per_pixel_est::NUMERIC,  
						(CASE
							WHEN b.l_zoomlevel < 14					THEN 0
							WHEN b.l_zoomlevel BETWEEN 14 AND 16	THEN 1			
						ELSE 2 END)::INT) AS m_x_per_pixel_est,
				   ROUND(
						b.m_x_per_pixel, 
						(CASE
							WHEN b.l_zoomlevel < 14					THEN 0
							WHEN b.l_zoomlevel BETWEEN 14 AND 16	THEN 1			
						ELSE 2 END)::INT) AS m_x_per_pixel,	
				   ROUND(
						b.m_y_per_pixel, 
						(CASE
							WHEN b.l_zoomlevel < 14					THEN 0
							WHEN b.l_zoomlevel BETWEEN 14 AND 16	THEN 1			
						ELSE 2 END)::INT) AS m_y_per_pixel,	
					ROUND(b.m_x_per_pixel*256, 0)::BIGINT m_x,
					ROUND(b.m_y_per_pixel*256, 0)::BIGINT m_y,
					ROUND(
						(b.m_x_per_pixel/c.one_unit_in_m)::NUMERIC /* Size of a pixel in 4326 units (degrees) */, 
						(CASE
							WHEN b.l_zoomlevel < 6					THEN 2
							WHEN b.l_zoomlevel BETWEEN 6  AND 7	    THEN 3	
							WHEN b.l_zoomlevel BETWEEN 8  AND 10	THEN 4	
							WHEN b.l_zoomlevel BETWEEN 11 AND 13	THEN 5							
							ELSE 7 
						 END))::NUMERIC AS simplify_tolerance,
				   '1 in '||LTRIM(TO_CHAR(ROUND((256*b.m_x_per_pixel/0.06733)::NUMERIC, 0::INT), '999G999G999')) AS scale
			  FROM b, c;
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_zoom_levels() caught: '||E'\n'||
				SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
			RAISE INFO '60003: %', error_message;
--
			RAISE;
	END;
--
	RETURN;
END;
$func$
LANGUAGE PLPGSQL;
				
COMMENT ON FUNCTION rif40_geo_pkg.rif40_zoom_levels(NUMERIC) IS 'Function: 	 rif40_zoom_levels()
Parameters:	 Latitude (default 0 - equator)
Returns:	 Table of zoomlevel results
Description: Displays zoom level table, calculating m/pixel and scale accurately

OSM/Leaflet - Zoom Levels
http://wiki.openstreetmap.org/wiki/Zoom_levels 

SELECT * FROM rif40_geo_pkg.rif40_zoom_levels();
psql:alter_scripts/v4_0_alter_5.sql:134: INFO:  [DEBUG1] rif40_zoom_levels(): [60001] latitude: 0
 zoom_level | latitude |    tiles     | degrees_per_tile | m_x_per_pixel_est | m_x_per_pixel | m_y_per_pixel |   m_x    |   m_y    | simplify_tolerance |      scale
------------+----------+--------------+------------------+-------------------+---------------+---------------+----------+----------+--------------------+------------------
          0 |        0 |            1 |              360 |            156412 |        155497 |               | 39807187 |          |               1.40 | 1 in 591,225,112
          1 |        0 |            4 |              180 |             78206 |         77748 |               | 19903593 |          |               0.70 | 1 in 295,612,556
          2 |        0 |           16 |               90 |             39103 |         39136 |         39070 | 10018754 | 10001966 |               0.35 | 1 in 148,800,745
          3 |        0 |           64 |               45 |             19552 |         19568 |         19472 |  5009377 |  4984944 |               0.18 | 1 in 74,400,373
          4 |        0 |          256 |             22.5 |              9776 |          9784 |          9723 |  2504689 |  2489167 |               0.09 | 1 in 37,200,186
          5 |        0 |         1024 |            11.25 |              4888 |          4892 |          4860 |  1252344 |  1244120 |               0.04 | 1 in 18,600,093
          6 |        0 |         4096 |            5.625 |              2444 |          2446 |          2430 |   626172 |   622000 |              0.022 | 1 in 9,300,047
          7 |        0 |        16384 |            2.813 |              1222 |          1223 |          1215 |   313086 |   310993 |              0.011 | 1 in 4,650,023
          8 |        0 |        65536 |            1.406 |               611 |           611 |           607 |   156543 |   155495 |             0.0055 | 1 in 2,325,012
          9 |        0 |       262144 |            0.703 |               305 |           306 |           304 |    78272 |    77748 |             0.0027 | 1 in 1,162,506
         10 |        0 |      1048576 |            0.352 |               153 |           153 |           152 |    39136 |    38874 |             0.0014 | 1 in 581,253
         11 |        0 |      4194304 |            0.176 |                76 |            76 |            76 |    19568 |    19437 |            0.00069 | 1 in 290,626
         12 |        0 |     16777216 |            0.088 |                38 |            38 |            38 |     9784 |     9718 |            0.00034 | 1 in 145,313
         13 |        0 |     67108864 |            0.044 |                19 |            19 |            19 |     4892 |     4859 |            0.00017 | 1 in 72,657
         14 |        0 |    268435456 |            0.022 |               9.5 |           9.6 |           9.5 |     2446 |     2430 |          0.0000858 | 1 in 36,328
         15 |        0 |   1073741824 |            0.011 |               4.8 |           4.8 |           4.7 |     1223 |     1215 |          0.0000429 | 1 in 18,164
         16 |        0 |   4294967296 |            0.005 |               2.4 |           2.4 |           2.4 |      611 |      607 |          0.0000215 | 1 in 9,082
         17 |        0 |  17179869184 |            0.003 |              1.19 |          1.19 |          1.19 |      306 |      304 |          0.0000107 | 1 in 4,541
         18 |        0 |  68719476736 |           0.0014 |              0.60 |          0.60 |          0.59 |      153 |      152 |          0.0000054 | 1 in 2,271
         19 |        0 | 274877906944 |          0.00069 |              0.30 |          0.30 |          0.30 |       76 |       76 |          0.0000027 | 1 in 1,135
(20 rows)

Time: 2.648 ms';
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_zoom_levels(NUMERIC) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_zoom_levels(NUMERIC) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_zoom_levels(NUMERIC) TO rif_manager;
 
 --
 -- Tile conversion functions from http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Tools
 --
DROP FUNCTION IF EXISTS rif40_geo_pkg.longitude2tile(DOUBLE PRECISION, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.latitude2tile(DOUBLE PRECISION, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.tile2latitude(INTEGER, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.tile2longitude(INTEGER, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.y_osm_tile2_tms_tile(INTEGER, INTEGER);

CREATE OR REPLACE FUNCTION rif40_geo_pkg.longitude2tile(longitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (longitude + 180) / 360 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
  
COMMENT ON FUNCTION rif40_geo_pkg.longitude2tile(DOUBLE PRECISION, INTEGER) IS 'Function: 	 longitude2tile()
Parameters:	 Latitude, zoom level
Returns:	 OSM Tile x
Description: Convert longitude (WGS84 - 4326) to OSM tile x

Derivation of the tile X/Y 

* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

x = lon
y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
(lat and lon are in radians)

* Transform range of x and y to 0 – 1 and shift origin to top left corner:

x = [1 + (x / π)] / 2
y = [1 − (y / π)] / 2

* Calculate the number of tiles across the map, n, using 2**zoom
* Multiply x and y by n. Round results down to give tilex and tiley.';
 
CREATE OR REPLACE FUNCTION rif40_geo_pkg.latitude2tile(latitude DOUBLE PRECISION, zoom_level INTEGER)
RETURNS INTEGER AS
$$
    SELECT FLOOR( (1.0 - LN(TAN(RADIANS(latitude)) + 1.0 / COS(RADIANS(latitude))) / PI()) / 2.0 * (1 << zoom_level) )::INTEGER
$$
LANGUAGE sql IMMUTABLE;
 
COMMENT ON FUNCTION rif40_geo_pkg.latitude2tile(DOUBLE PRECISION, INTEGER) IS 'Function: 	 latitude2tile()
Parameters:	 Latitude, zoom level
Returns:	 OSM Tile y
Description: Convert latitude (WGS84 - 4326) to OSM tile y

Derivation of the tile X/Y 

* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

x = lon
y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
(lat and lon are in radians)

* Transform range of x and y to 0 – 1 and shift origin to top left corner:

x = [1 + (x / π)] / 2
y = [1 − (y / π)] / 2

* Calculate the number of tiles across the map, n, using 2**zoom
* Multiply x and y by n. Round results down to give tilex and tiley.';
 
CREATE OR REPLACE FUNCTION rif40_geo_pkg.tile2latitude(y INTEGER, zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
$BODY$
DECLARE
	n FLOAT;
	sinh FLOAT;
	E FLOAT = 2.7182818284;
BEGIN
    n = PI() - (2.0 * PI() * y) / POWER(2.0, zoom_level);
    sinh = (1 - POWER(E, -2*n)) / (2 * POWER(E, -n));
    RETURN DEGREES(ATAN(sinh));
END;
$BODY$
LANGUAGE plpgsql IMMUTABLE;
 
COMMENT ON FUNCTION rif40_geo_pkg.tile2latitude(INTEGER, INTEGER) IS 'Function: 	 tile2latitude()
Parameters:	 OSM Tile y, zoom level
Returns:	 Latitude
Description: Convert OSM tile y to latitude (WGS84 - 4326)';
 
CREATE OR REPLACE FUNCTION rif40_geo_pkg.tile2longitude(x INTEGER, zoom_level INTEGER)
RETURNS DOUBLE PRECISION AS
$$
	SELECT ( ( (x * 1.0) / (1 << zoom_level) * 360.0) - 180.0)::DOUBLE PRECISION
$$
LANGUAGE sql IMMUTABLE;
 
COMMENT ON FUNCTION rif40_geo_pkg.tile2longitude(INTEGER, INTEGER) IS 'Function: 	 tile2latitude()
Parameters:	 OSM Tile x, zoom level
Returns:	 Longitude
Description: Convert OSM tile y to longitude (WGS84 - 4326)';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.y_osm_tile2_tms_tile(y INTEGER, zoom_level INTEGER)
RETURNS INTEGER AS
$$
SELECT (POWER(2, zoom_level) -y -1)::INTEGER
$$
LANGUAGE sql IMMUTABLE;

COMMENT ON FUNCTION rif40_geo_pkg.y_osm_tile2_tms_tile(INTEGER, INTEGER) IS 'Function: 	 tile2latitude()
Parameters:	 OSM Tile y, zoom level
Returns:	 TMS tile y
Description: Convert OSM tile y to TMS tile y';

GRANT EXECUTE ON FUNCTION rif40_geo_pkg.longitude2tile(DOUBLE PRECISION, INTEGER) TO rif40, rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.latitude2tile(DOUBLE PRECISION, INTEGER) TO rif40, rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.tile2latitude(INTEGER, INTEGER) TO rif40, rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.tile2longitude(INTEGER, INTEGER) TO rif40, rif_user, rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.y_osm_tile2_tms_tile(INTEGER, INTEGER) TO rif40, rif_user, rif_manager;

 --
 -- Eof