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
-- Rapid Enquiry Facility (RIF) - Web services integration functions for middleware:
--     				  rif40_GetMapAreas() - Get area IDs for <geolevel_view> for 
--											map area bounding box
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

DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_GetMapAreas(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL);
	
CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_GetMapAreas(
	l_geography 		VARCHAR, 
	l_geolevel_view 	VARCHAR,
	y_max 			REAL,
	x_max 			REAL, 
	y_min 			REAL, 
	x_min 			REAL)
RETURNS SETOF JSON
SECURITY INVOKER
AS $body$
/*
Function: 		rif40_GetMapAreas()
Parameters:		Geography, geolevel_view, 
				Y max, X max, Y min, X min as a record (bounding box)
Returns:		JSON: gid, area_id, name
Description:	Get area IDs for <geolevel_view> for map area bounding box. 


 */
 DECLARE
 	c1geojson2 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c2geojson2 CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;
	c3geojson2 REFCURSOR;
--
	c1_rec rif40_geographies%ROWTYPE;
	c2_rec rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
	c4_rec RECORD;
--
	sql_stmt		VARCHAR;
--
	drop_stmt 		VARCHAR;
	explain_text 	VARCHAR;
	temp_table 		VARCHAR;	
--
	stp 			TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp 			TIMESTAMP WITH TIME ZONE;
	took 			INTERVAL;
--
	error_message 	VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
 BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-52051, 'rif40_GetMapAreas', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Create unique results temporary table
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_GetMapAreas', 'DEBUG2') THEN
		temp_table:='l_'||REPLACE(rif40_sql_pkg.sys_context(NULL, 'AUDSID'), '.', '_');
--
-- Drop results temporary table
--
-- This could do with checking first to remove the notice:
-- psql:v4_0_rif40_sql_pkg.sql:3601: NOTICE:  table "l_7388_2456528_62637_130282_7388" does not exist, skipping
-- CONTEXT:  SQL statement "DROP TABLE IF EXISTS l_7388_2456528_62637_130282"
-- PL/pgSQL function "rif40_ddl" line 32 at EXECUTE statement
--
		drop_stmt:='DROP TABLE IF EXISTS '||temp_table;
		PERFORM rif40_sql_pkg.rif40_ddl(drop_stmt);
	END IF;
--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-52052, 'rif40_GetMapAreas', 'NULL geography parameter');
	END IF;	
--
	OPEN c1geojson2(l_geography);
	FETCH c1geojson2 INTO c1_rec;
	CLOSE c1geojson2;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-52053, 'rif40_GetMapAreas', 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;	
--
-- Test <geolevel view/area> exist
--
	OPEN c2geojson2(l_geography, l_geolevel_view);
	FETCH c2geojson2 INTO c2_rec;
	CLOSE c2geojson2;
--
	IF c2_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-52054, 'rif40_GetMapAreas', 'geography: %, <geoevel view> %: not found', 
			l_geography::VARCHAR	/* Geography */, 
			geolevel_view::VARCHAR	/* Geolevel view */);
	END IF;	
	sql_stmt:='WITH a AS ('||E'\n'||
		  '	SELECT area_id,'||E'\n'||
		  '            ST_MakeEnvelope($1 /* Xmin */, $2 /* Ymin */, $3 /* Xmax */, $4 /* YMax */) AS geom	/* Bound */'||E'\n'||
		  '	  FROM '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||E'\n'||
		  '	 WHERE ST_Intersects(optimised_geometry,'||E'\n'||
		  '        		ST_MakeEnvelope($1 /* Xmin */,'||E'\n'||
		  '        					 $2 	/* Ymin */,'||E'\n'||
		  '       			 		 $3 	/* Xmax */,'||E'\n'||
		  '        					 $4 	/* YMax */,'||E'\n'||
		  '        					 4326 	/* WGS 84 */))'||E'\n'||
		  '	   AND geolevel_name = $5		/* Partition eliminate */'||E'\n'||
		  '	   /* Intersect bound with geolevel geometry */'||E'\n'||
		  ')'||E'\n'||
		  'SELECT row_to_json(rec) AS map_area FROM ('||E'\n'||
		  '		SELECT b.gid, a.area_id, b.name'||E'\n'||
		  '		  FROM a, '||quote_ident(LOWER(c2_rec.lookup_table))||' b'||E'\n'||
		  '		 WHERE a.area_id = b.'||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n'||
		  '		 ORDER BY b.gid) rec';

--
-- Begin execution block to trap parse errors
--
-- EXPLAIN PLAN version
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_GetMapAreas', 'DEBUG2') THEN
		BEGIN
--
-- Create results temporary table, extract explain plan  using _rif40_geojson_explain_ddl2() helper function.
-- This ensures the EXPLAIN PLAN output is a field called explain_line 
--
			sql_stmt:='SELECT explain_line FROM rif40_xml_pkg._rif40_geojson_explain_ddl2('||quote_literal(
					'EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE '||temp_table||' AS '||E'\n'||sql_stmt)||', $1, $2, $3, $4, $5)';
			FOR c4_rec IN EXECUTE sql_stmt USING x_min, y_min, x_max, y_max, l_geolevel_view LOOP
				IF explain_text IS NULL THEN
					explain_text:=c4_rec.explain_line;
				ELSE
					explain_text:=explain_text||E'\n'||c4_rec.explain_line;
				END IF;
			END LOOP;
--
-- Now extract actual results from temp table
--	
			FOR c3_rec IN EXECUTE 'SELECT * FROM '||temp_table LOOP
				RETURN NEXT c3_rec.map_area;
			END LOOP;
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_GetMapAreas() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||E'\n'||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '52055: %', error_message;
--
				RAISE;
		END;
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_GetMapAreas', '[52056] Bounds EXPLAIN PLAN.'||E'\n'||'%', explain_text::VARCHAR);
	ELSE
		BEGIN
			FOR c3_rec IN EXECUTE sql_stmt USING x_min, y_min, x_max, y_max, l_geolevel_view LOOP
				RETURN NEXT c3_rec.map_area;
			END LOOP;		
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_GetMapAreas() caught: '||E'\n'||
					SQLERRM::VARCHAR||' in SQL> '||E'\n'||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
				RAISE INFO '52057: %', error_message;
--
				RAISE;
		END;
	END IF;
--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	stp:=clock_timestamp();
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_GetMapAreas', 
		'[52058] <geolevel area id list> SQL> [using %, %, %, %, %], time taken so far: %'||E'\n'||'%;',
		x_min::VARCHAR			/* Xmin */,
		y_min::VARCHAR			/* Ymin */,
		x_max::VARCHAR			/* Xmax */,
		y_max::VARCHAR			/* Ymax */,
		l_geolevel_view::VARCHAR	/* Geolevel view */,
		took::VARCHAR			/* Time taken */,
		sql_stmt::VARCHAR		/* SQL statement */);
--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_GetMapAreas', 
		'[52059] Geography: %, <geoevel view> % bound [%, %, %, %] complete, took: %.', 		
		l_geography::VARCHAR			/* Geography */, 
		l_geolevel_view::VARCHAR		/* Geoelvel view */, 
		x_min::VARCHAR				/* Xmin */,
		y_min::VARCHAR				/* Ymin */,
		x_max::VARCHAR				/* Xmax */,
		y_max::VARCHAR				/* Ymax */,
		took::VARCHAR				/* Time taken */);
--
	RETURN;
 END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_GetMapAreas(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL) IS 'Function: 		rif40_GetMapAreas()
Parameters:		Geography, geolevel_view, 
				Y max, X max, Y min, X min as a record (bounding box)
Returns:		JSON: gid, area_id, name
Description:	Get area IDs for <geolevel_view> for map area bounding box.

Generates code:

WITH a AS (
        SELECT area_id,
            ST_MakeEnvelope($1 /* Xmin */, $2 /* Ymin */, $3 /* Xmax */, $4 /* YMax */) AS geom /* Bound */
          FROM t_rif40_sahsu_geometry
         WHERE ST_Intersects(optimised_geometry,
                        ST_MakeEnvelope($1 /* Xmin */,
                                        $2     /* Ymin */,
										$3     /* Xmax */,
                                        $4     /* YMax */,
                                        4326   /* WGS 84 */))
           AND geolevel_name = $5              /* Partition eliminate */
           /* Intersect bound with geolevel geometry */
)
SELECT row_to_json(rec) AS map_area FROM (
                SELECT b.gid, a.area_id, b.name
                  FROM a, sahsuland_level4 b
                 WHERE a.area_id = b.level4
                 ORDER BY b.gid) rec;
			
Test example:

WITH a AS (
        SELECT *
          FROM rif40_xml_pkg.rif40_getGeoLevelBoundsForArea(''SAHSU'', ''LEVEL2'', ''01.004'')
)
SELECT rif40_xml_pkg.rif40_GetMapAreas(
                        ''SAHSU''         /* Geography */,
                        ''LEVEL4''        /* geolevel view */,
                        a.y_max, a.x_max, a.y_min, a.x_min /* Bounding box - from cte */) AS json
  FROM a LIMIT 4;
  
Example JSON output:

                                        rif40_GetMapAreas interface
                                                   json
-----------------------------------------------------------------------------------------------------------
 {"gid":1,"area_id":"01.001.000100.1","name":"Abellan LEVEL4(01.001.000100.1)"}
 {"gid":85,"area_id":"01.002.001300.5","name":"Cobley LEVEL4(01.002.001300.5)"}
 {"gid":86,"area_id":"01.002.001300.6","name":"Cobley LEVEL4(01.002.001300.6)"}
 {"gid":87,"area_id":"01.002.001300.7","name":"Cobley LEVEL4(01.002.001300.7)"}
(4 rows)

Time: 448.753 ms';
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetMapAreas(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL) TO rif40, rif_user, rif_manager;
 
--
-- Eof