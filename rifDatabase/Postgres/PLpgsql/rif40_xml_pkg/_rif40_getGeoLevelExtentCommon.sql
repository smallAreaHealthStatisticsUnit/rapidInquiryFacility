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

--
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_xml_pkg._rif40_getGeoLevelExtentCommon: 	50000 to 50099
--
CREATE OR REPLACE FUNCTION rif40_xml_pkg._rif40_getGeoLevelExtentCommon(
	l_geography 	VARCHAR,
	l_geolevel_view	VARCHAR,
	l_map_area	VARCHAR,
	l_study_id	INTEGER,
	OUT y_max 	REAL,
	OUT x_max 	REAL, 
	OUT y_min 	REAL, 
	OUT x_min 	REAL)
RETURNS RECORD
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_getGeoLevelExtentCommon()
Parameters:	Geography, geolevel_view, map area (single <geolevel view> area id) or, study id
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Must be rif40 or have rif_user or rif_manager role
Test geography exists
Test <geolevel view> exists
Check optional flags: map area (single <geolevel view> area id) or, study id or neither
- Neither: No further checks
  Called from: rif40_getGeoLevelFullExtent
- Map area ID only: Called from: rif40_getGeoLevelBoundsForArea
  Check map area ID exists
- Study ID only: Called from: rif40_getGeoLevelFullExtentForStudy
  Check study ID

  a) exists
  b) Geography matches
  c) Study geolevel matches <geolevel view>

Generates SQL> WITH a AS (
        SELECT ST_Extent(optimised_geometry) AS g
          FROM t_rif40_sahsu_geometry /- SAHSU -/
         WHERE geolevel_name = $1 /- <Geolevel view> -/
        )
        SELECT ST_Ymax(g)::REAL AS ymax, ST_Xmax(g)::REAL AS xmax, ST_Ymin(g)::REAL AS ymin, ST_Xmin(g)::REAL AS xmin
          FROM a;
	
or Map area id SQL variant>

WITH a AS (
        SELECT ST_Extent(optimised_geometry) AS g
          FROM t_rif40_sahsu_geometry /- SAHSU -/
         WHERE geolevel_name = $1 /- <Geolevel view> -/
           AND area_id       = $2 /- <Map area ID> -/
        )
        SELECT ST_Ymax(g)::REAL AS ymax, ST_Xmax(g)::REAL AS xmax, ST_Ymin(g)::REAL AS ymin, ST_Xmin(g)::REAL AS xmin
          FROM a;

e.g. SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelFullExtent('SAHSU', 'LEVEL2');

  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.0122 | -6.32507 | 54.6456 | -6.68853
(1 row)

Generated SQL can raise exceptions; these are trapped, printed to INFO and re-raised
 */
DECLARE
	c1geofullext CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography     = l_geography;
	c2geofullext CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;
	c3geofullext REFCURSOR;
	c4geofullext CURSOR(l_study_id INTEGER) FOR
		SELECT study_id, geography, study_geolevel_name
		  FROM rif40_studies
		 WHERE study_id     = l_study_id
		   AND USER         != 'rif40'
		UNION /* So can run as rif40 for testing */
		SELECT study_id, geography, study_geolevel_name
		  FROM t_rif40_studies
		 WHERE study_id     = l_study_id
		   AND USER         = 'rif40';
		  
	c5geofullext REFCURSOR;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
--
	sql_stmt VARCHAR;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-50000, '_rif40_getGeoLevelExtentCommon', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50001, '_rif40_getGeoLevelExtentCommon', 'NULL geography parameter');
	END IF;	
--
	OPEN c1geofullext(l_geography);
	FETCH c1geofullext INTO c1_rec;
	CLOSE c1geofullext;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50002, '_rif40_getGeoLevelExtentCommon', 'geography: % not found', 
			l_geography::VARCHAR		/* Geography */);
	END IF;	
--
-- Test <geolevel view> exists
--
	OPEN c2geofullext(l_geography, l_geolevel_view);
	FETCH c2geofullext INTO c2_rec;
	CLOSE c2geofullext;
--
	IF c2_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50003, '_rif40_getGeoLevelExtentCommon', 
			'geography: %, <geoevel view> %: not found', 
			l_geography::VARCHAR		/* Geography */, 
			l_geolevel_view::VARCHAR	/* geolevel view */);
	END IF;	

--
-- Check optional flags: map area (single <geolevel view> area id) or, study id or neither
--
	IF l_map_area IS NULL AND l_study_id IS NULL THEN
-- Neither: No further checks
-- Called from: rif40_getGeoLevelFullExtent
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_getGeoLevelExtentCommon', 
			'[50004] geography: %, <geoevel view> %: validated (no map area (single <geolevel view> area id) or, study id)', 
			l_geography::VARCHAR		/* Geography */, 
			l_geolevel_view::VARCHAR	/* geolevel view */);
	ELSIF l_map_area IS NOT NULL AND l_study_id IS NULL THEN
-- Map area ID only: Called from: rif40_getGeoLevelBoundsForArea
-- Check map area ID exists
		IF c1_rec.geometrytable IS NULL THEN /* Pre tilemaker: no geometry table */
			sql_stmt:='SELECT area_id AS areaid /* pre tilemaker */'||E'\n'||
E'\t'||'  FROM '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||' /* '||l_geography||' */'||E'\n'||
E'\t'||' WHERE geolevel_name = $1 /* <Geolevel view> */'||E'\n'||
E'\t'||'   AND area_id       = $2 /* <map area> */';
		ELSE
			sql_stmt:='SELECT areaid /* Tilemaker */'||E'\n'||
E'\t'||'  FROM '||quote_ident(LOWER(c1_rec.geometrytable))||' a /* '||l_geography||' */, rif40_geolevels b'||E'\n'||
E'\t'||' WHERE a.geolevel_id   = b.geolevel_id'||E'\n'||
E'\t'||'   AND b.geolevel_name = $1 /* <Geolevel view> */'||E'\n'||
E'\t'||'   AND a.areaid        = $2 /* <map area> */'||E'\n'||
E'\t'||'   AND b.geography     = '''||l_geography||''' /* <geography> */'||E'\n'||
E'\t'||'   AND a.zoomlevel     = '||c1_rec.maxzoomlevel||' /* <max zoomlevel> */';
		END IF;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_getGeoLevelExtentCommon', '[50005] c3geofullext SQL>'||E'\n'||'%;', 
			sql_stmt::VARCHAR);
--
-- Execute
--
		BEGIN
			OPEN c3geofullext FOR EXECUTE sql_stmt USING l_geolevel_view, l_map_area;
			FETCH c3geofullext INTO c3_rec;
			CLOSE c3geofullext;
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='_rif40_getGeoLevelExtentCommon() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '50006: %', error_message;
--
				RAISE;
		END;
		IF c3_rec.areaid IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-50007, '_rif40_getGeoLevelExtentCommon', 
				'geography: %, <geoevel view> %: map area (%) not found.', 
				l_geography::VARCHAR		/* Geography */, 
				l_geolevel_view::VARCHAR	/* Geolevel view */,
				l_map_area::VARCHAR			/* Map area */);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_getGeoLevelExtentCommon', 
				'[50008] Geography: %, <geoevel view> %: map area (%) Validated.', 
				l_geography::VARCHAR		/* Geography */, 
				l_geolevel_view::VARCHAR	/* Geolevel view */,
				c3_rec.areaid::VARCHAR		/* Map area */);
		END IF;
	ELSIF l_map_area IS NULL AND l_study_id IS NOT NULL THEN
-- Study ID only: Called from: rif40_getGeoLevelFullExtentForStudy
-- Check study ID
--
-- a) exists
-- b) Geography matches
-- c) Study geolevel matches <geolevel view>
--
		OPEN c4geofullext(l_study_id);
		FETCH c4geofullext INTO c4_rec;
		CLOSE c4geofullext;
		IF c4_rec.study_id IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-50009, '_rif40_getGeoLevelExtentCommon', 
				'Geography: %, <geoevel view> %: study ID (%) not found.', 
				l_geography::VARCHAR		/* Geography */, 
				l_geolevel_view::VARCHAR	/* Geolevel view */,
				l_study_id::VARCHAR		/* Study ID */);
		ELSIF c4_rec.geography != l_geography THEN
			PERFORM rif40_log_pkg.rif40_error(-50010, '_rif40_getGeoLevelExtentCommon', 
				'Geography: %, <geoevel view> %: study ID (%) geography not valid (%) for study, expecting: %.', 
				l_geography::VARCHAR		/* Geography */, 
				l_geolevel_view::VARCHAR	/* Geolevel view */,
				l_study_id::VARCHAR		/* Study ID */,
				l_geography::VARCHAR		/* Specified geography */,
				c4_rec.geography::VARCHAR	/* Study geography */);
		ELSIF c4_rec.study_geolevel_name != l_geolevel_view THEN
			PERFORM rif40_log_pkg.rif40_error(-50012, '_rif40_getGeoLevelExtentCommon', 
				'Study ID (%) geography (%) study geolevel (%) not valid for study, expecting: %.', 
				l_study_id::VARCHAR		/* Study ID */,
				l_geography::VARCHAR		/* Specified geography */,
				l_geolevel_view::VARCHAR	/* Specified geolevel view */,
				c4_rec.study_geolevel_name::VARCHAR		
								/* Study geolevel */);
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_getGeoLevelExtentCommon', 
				'[50013] Study ID (%) geography (%) study geolevel (%) valid.', 
				c4_rec.study_id::VARCHAR	/* Study ID */,
				c4_rec.geography::VARCHAR	/* Study geography */,
				c4_rec.study_geolevel_name::VARCHAR		
								/* Study geolevel */);
		END IF;
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-50014, '_rif40_getGeoLevelExtentCommon', 
			'Geography: %, <geoevel view> %: both map area id (%) and study ID (%) are set.', 
			l_geography::VARCHAR		/* Geography */, 
			l_geolevel_view::VARCHAR	/* Geolevel view */,
			l_map_area::VARCHAR		/* Map area ID */,
			l_study_id::VARCHAR		/* Study ID */);
	END IF;

--
-- Create SQL statement
--

	IF c1_rec.geometrytable IS NULL THEN /* Pre tilemaker: no geometry table */
		sql_stmt:='WITH a AS ( /* pre tilemaker II */'||E'\n'||
E'\t'||'SELECT ST_Extent(optimised_geometry) AS g'||E'\n'||
E'\t'||'  FROM '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||' /* '||l_geography||' */'||E'\n'||
E'\t'||' WHERE geolevel_name = $1 /* <Geolevel view> */';	
		IF l_map_area IS NOT NULL AND l_study_id IS NULL THEN
			sql_stmt:=sql_stmt||E'\n'||
	E'\t'||'   AND area_id       = $2 /* <Map area ID> */';
		END IF;
	ELSE
		sql_stmt:='WITH a AS ( /* Tilemaker II */'||E'\n'||
E'\t'||'SELECT ST_Extent(geom) AS g'||E'\n'||
E'\t'||'  FROM '||quote_ident(LOWER(c1_rec.geometrytable))||' a /* '||l_geography||' */, rif40_geolevels b'||E'\n'||
E'\t'||' WHERE a.geolevel_id   = b.geolevel_id'||E'\n'||
E'\t'||'   AND b.geolevel_name = $1 /* <Geolevel view> */'||E'\n'||
E'\t'||'   AND b.geography     = '''||l_geography||''' /* <geography> */'||E'\n'||
E'\t'||'   AND a.zoomlevel     = '||c1_rec.maxzoomlevel||' /* max zoomlevel */';
		IF l_map_area IS NOT NULL AND l_study_id IS NULL THEN
			sql_stmt:=sql_stmt||E'\n'||
	E'\t'||'   AND areaid       = $2 /* <Map area ID> */';
		END IF;
	END IF;

	sql_stmt:=sql_stmt||E'\n'||
E'\t'||')'||E'\n'||
E'\t'||'SELECT ST_Ymax(g)::REAL AS ymax, ST_Xmax(g)::REAL AS xmax, ST_Ymin(g)::REAL AS ymin, ST_Xmin(g)::REAL AS xmin'||E'\n'||
E'\t'||'  FROM a';
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', '_rif40_getGeoLevelExtentCommon', '[50015] Results SQL>'||E'\n'||'%;', sql_stmt::VARCHAR);
--
-- Execute
--
	BEGIN
		IF l_map_area IS NULL AND l_study_id IS NULL THEN
			OPEN c5geofullext FOR EXECUTE sql_stmt USING l_geolevel_view;
		ELSIF l_map_area IS NOT NULL AND l_study_id IS NULL THEN
			OPEN c5geofullext FOR EXECUTE sql_stmt USING l_geolevel_view, l_map_area;
		ELSIF l_map_area IS NULL AND l_study_id IS NOT NULL THEN
			OPEN c5geofullext FOR EXECUTE sql_stmt USING l_geolevel_view;
		END IF;
		FETCH c5geofullext INTO c5_rec;
		CLOSE c5geofullext;
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='_rif40_getGeoLevelExtentCommon() caught: '||E'\n'||
				SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
			RAISE INFO '50016: %', error_message;
--
			RAISE;
	END;
--
	IF c5_rec.ymax IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50017, '_rif40_getGeoLevelExtentCommon', 
			'No bounding box found for geography: %, <geoevel view> %', 
			l_geography::VARCHAR		/* Geography */, 
			l_geolevel_view::VARCHAR	/* geolevel view */);
	END IF;
--
-- Set up return values
--
	y_max:=c5_rec.ymax;
	x_max:=c5_rec.xmax;
	y_min:=c5_rec.ymin;
	x_min:=c5_rec.xmin;
--
	RETURN; 
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON  FUNCTION rif40_xml_pkg._rif40_getGeoLevelExtentCommon(VARCHAR, VARCHAR, VARCHAR, INTEGER,
	OUT REAL, OUT REAL, OUT REAL, OUT REAL) IS 'Function: 	_rif40_getGeoLevelExtentCommon()
Parameters:	Geography, geolevel_view, map area (single <geolevel view> area id) or, study id
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Must be rif40 or have rif_user or rif_manager role
Test geography exists
Test <geolevel view> exists
Check optional flags: map area (single <geolevel view> area id) or, study id or neither
- Neither: No further checks
  Called from: rif40_getGeoLevelFullExtent
- Map area ID only: Called from: rif40_getGeoLevelBoundsForArea
  Check map area ID exists
- Study ID only: Called from: rif40_getGeoLevelFullExtentForStudy
  Check study ID

  a) exists
  b) Geography matches
  c) Study geolevel matches <geolevel view>

Generates SQL> WITH a AS (
        SELECT ST_Extent(optimised_geometry) AS g
          FROM t_rif40_sahsu_geometry /* SAHSU */
         WHERE geolevel_name = $1 /* <Geolevel view> */
        )
        SELECT ST_Ymax(g)::REAL AS ymax, ST_Xmax(g)::REAL AS xmax, ST_Ymin(g)::REAL AS ymin, ST_Xmin(g)::REAL AS xmin
          FROM a;
	
or Map area id SQL variant>

WITH a AS (
        SELECT ST_Extent(optimised_geometry) AS g
          FROM t_rif40_sahsu_geometry /* SAHSU */
         WHERE geolevel_name = $1 /* <Geolevel view> */
           AND area_id       = $2 /* <Map area ID> */
        )
        SELECT ST_Ymax(g)::REAL AS ymax, ST_Xmax(g)::REAL AS xmax, ST_Ymin(g)::REAL AS ymin, ST_Xmin(g)::REAL AS xmin
          FROM a;

	e.g. SELECT * FROM rif40_xml_pkg.rif40_getGeoLevelFullExtent(''SAHSU'', ''LEVEL2'');

  y_max  |  x_max   |  y_min  |  x_min
---------+----------+---------+----------
 55.0122 | -6.32507 | 54.6456 | -6.68853
(1 row)

Generated SQL can raise exceptions; these are trapped, printed to INFO and re-raised';

--
-- No GRANTS - private function
--

--
-- Eof
