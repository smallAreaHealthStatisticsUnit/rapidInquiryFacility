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
-- Rapid Enquiry Facility (RIF) - Get study area adjacency matrix required by INLA
--
-- Tilemaker converted 
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
-- rif40_GetAdjacencyMatrix:				52000 to 52050
--
CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_GetAdjacencyMatrix(
	l_study_id			INTEGER)
RETURNS TABLE(
	 area_id 			VARCHAR,
	 num_adjacencies 	INTEGER, 
	 adjacency_list 	VARCHAR)
SECURITY INVOKER
AS $func$
/*
Function: 		rif40_GetAdjacencyMatrix()
Parameters:		study id
Returns:		area_id, num_adjacencies, adjacency_list as a table. 
Description: 	Get study area adjacency matrix required by INLA	

Needs to have adjacency_list limited to 8060 characters where converted to use adjacencies_<geography>

Generates and executes SQL>

WITH b AS (
        SELECT area_id, band_id
          FROM rif40_study_areas b1
         WHERE b1.study_id = $1
), c AS (
        SELECT b.area_id, b.band_id, c1.optimised_geometry
          FROM t_rif40_sahsu_geometry c1, b
    WHERE c1.geolevel_name = $2
      AND c1.area_id       = b.area_id
), d AS (
        SELECT d1.band_id,
                   d1.area_id,
                   d2.area_id AS adjacent_area_id,
                   COUNT(d2.area_id) OVER(PARTITION BY d1.area_id ORDER BY d2.area_id) AS num_adjacencies
          FROM c d1, c d2
         WHERE d1.area_id       != d2.area_id
           AND ST_Intersects(d1.optimised_geometry, d2.optimised_geometry)
), e AS (
        SELECT d.area_id::VARCHAR AS area_id, 
		       COUNT(d.area_id)::INTEGER AS num_adjacencies, 
		       string_agg(d.adjacent_area_id, ',' ORDER BY d.adjacent_area_id)::VARCHAR AS adjacency_list
          FROM d
         GROUP BY d.area_id
)
SELECT e.*
  FROM e
  ORDER BY 1, 2;
  
Returns a table:

SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(1) LIMIT 10;
     area_id     | num_adjacencies |     adjacency_list

-----------------+-----------------+------------------------------------------------------
 01.001.000100.1 |              17 | 01.001.000100.2,01.001.000200.1,01.002.000500.1,01.002.000500.4,01.002.000500.7,01.002.000600.1,01.002.000600.6,0
1.002.000600.8,01.002.000700.2,01.002.000800.7,01.002.000900.1,01.002.000900.2,01.002.000900.4,01.002.001300.1,01.002.001500.2,01.004.011100.1,01.005.
002400.1
 01.001.000100.2 |               2 | 01.001.000100.1,01.001.000200.1
 01.001.000200.1 |               4 | 01.001.000100.1,01.001.000100.2,01.001.000300.1,01.005.002400.1
 01.001.000300.1 |               1 | 01.001.000200.1
 01.002.000300.1 |               3 | 01.002.000300.2,01.002.000300.3,01.002.000300.4
 01.002.000300.2 |               4 | 01.002.000300.1,01.002.000300.4,01.002.000500.3,01.002.000600.2
 01.002.000300.3 |               3 | 01.002.000300.1,01.002.000300.4,01.002.000300.5
 01.002.000300.4 |               5 | 01.002.000300.1,01.002.000300.2,01.002.000300.3,01.002.000300.5,01.002.000600.2
 01.002.000300.5 |               2 | 01.002.000300.3,01.002.000300.4
 01.002.000400.1 |               6 | 01.002.000400.2,01.002.000400.5,01.002.000400.7,01.002.001700.1,01.002.001700.2,01.002.001700.3
(10 rows)
  
 */
 DECLARE
 	c1adjacency CURSOR(l_study_id INTEGER) FOR
		SELECT study_id, geography, study_geolevel_name
		  FROM rif40_studies
		 WHERE study_id     = l_study_id
		   AND USER         != 'rif40'
		UNION /* So can run as rif40 for testing */
		SELECT study_id, geography, study_geolevel_name
		  FROM t_rif40_studies
		 WHERE study_id     = l_study_id
		   AND USER         = 'rif40';
 	c2adjacency CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c3adjacency CURSOR(l_geography VARCHAR, l_geolevel VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel;	
--
	c1_rec 			RECORD;
	c2_rec 			RECORD;
	c3_rec 			RECORD;
--
	sql_stmt 		VARCHAR;
--
	error_message 	VARCHAR;
	geometry_table 	VARCHAR;  /* #ASSUME: Pre tilemaker: no geometry table */
--	
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-52000, 'rif40_GetAdjacencyMatrix', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	OPEN c1adjacency(l_study_id);
	FETCH c1adjacency INTO c1_rec;
	CLOSE c1adjacency;
	IF c1_rec.study_id IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-52001, 'rif40_GetAdjacencyMatrix', 
			'Study ID (%) not found.', 
			l_study_id::VARCHAR		/* Study ID */);
	END IF;
--
	OPEN c2adjacency(c1_rec.geography);
	FETCH c2adjacency INTO c2_rec;
	CLOSE c2adjacency;
--
	IF c2_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-52002, 'rif40_GetAdjacencyMatrix', 'geography: % not found', 
			c1_rec.geography::VARCHAR	/* Geography */);
	END IF;		
--
-- Test <geolevel view/area> exist
--
	OPEN c3adjacency(c1_rec.geography, c1_rec.study_geolevel_name);
	FETCH c3adjacency INTO c3_rec;
	CLOSE c3adjacency;
--
	IF c3_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-52004, 'rif40_GetAdjacencyMatrix', 'geography: %, <geoevel view> %: not found', 
			c1_rec.geography::VARCHAR	/* Geography */, 
			c1_rec.study_geolevel_name::VARCHAR	/* Geolevel view */);
	END IF;	
--
	BEGIN
		IF c2_rec.geometrytable IS NOT NULL THEN
			geometry_table:=c2_rec.geometrytable;
		END IF;
	EXCEPTION	
/*
psql:alter_scripts/v4_0_alter_9.sql:148: ERROR:  record "c1_rec" has no field "geometrytable"
CONTEXT:  SQL statement "SELECT c1_rec.geometrytable IS NULL"
PL/pgSQL function rif40_xml_pkg.rif40_getadjacencymatrix(integer) line 202 at IF
*/
		WHEN others THEN NULL;
	END;
--		
	IF geometry_table IS NULL THEN  /* Pre tilemaker: no geometry table */
		sql_stmt:='WITH b AS ( /* Pre tilemaker: no geometry table */ '||E'\n'||
'	SELECT area_id, band_id'||E'\n'||
'	  FROM rif40_study_areas b1'||E'\n'||
'	 WHERE b1.study_id = $1'||E'\n'||
'), c AS ('||E'\n'||
'	SELECT b.area_id, b.band_id, c1.optimised_geometry'||E'\n'||
'	  FROM '||quote_ident('geometry_'||LOWER(c1_rec.geography))||' c1, b'||E'\n'||
'    WHERE c1.geolevel_name = $2'||E'\n'||
'      AND c1.area_id       = b.area_id'||E'\n'||	  
'), d AS ('||E'\n'||
'	SELECT d1.band_id,'||E'\n'||
'		   d1.area_id,'||E'\n'|| 
'		   d2.area_id AS adjacent_area_id,'||E'\n'||
'		   COUNT(d2.area_id) OVER(PARTITION BY d1.area_id ORDER BY d2.area_id) AS num_adjacencies'||E'\n'||
'	  FROM c d1, c d2'||E'\n'||
'	 WHERE d1.area_id       != d2.area_id'||E'\n'||
'	   AND ST_Intersects(d1.optimised_geometry, d2.optimised_geometry)'||E'\n'||
'), e AS ('||E'\n'||
'	SELECT d.area_id::VARCHAR AS area_id,'||E'\n'||
'		   COUNT(d.area_id)::INTEGER AS num_adjacencies,'||E'\n'|| 
'		   string_agg(d.adjacent_area_id, '','' ORDER BY d.adjacent_area_id)::VARCHAR AS adjacency_list'||E'\n'||
'	  FROM d'||E'\n'||
'	 GROUP BY d.area_id'||E'\n'||
')'||E'\n'||
'SELECT e.*'||E'\n'||
'  FROM e'||E'\n'||
'  ORDER BY 1, 2';
	ELSE
		sql_stmt:='WITH b AS ( /* Tilemaker: has geometry table */ '||E'\n'||
'	SELECT area_id, band_id'||E'\n'||
'	  FROM rif40_study_areas b1'||E'\n'||
'	 WHERE b1.study_id = $1'||E'\n'||
'), c AS ('||E'\n'||
'	SELECT b.area_id, b.band_id, c1.geom'||E'\n'||
'	  FROM '||quote_ident(LOWER(geometry_table))||' c1, b'||E'\n'||
'    WHERE c1.geolevel_id   = $2'||E'\n'||
'      AND c1.areaid        = b.area_id'||E'\n'||	  
'	   AND c1.zoomlevel     = '||c2_rec.maxzoomlevel||'   /* max zoomlevel: Partition eliminate */'||E'\n'||	  
'), d AS ('||E'\n'||
'	SELECT d1.band_id,'||E'\n'||
'		   d1.area_id,'||E'\n'|| 
'		   d2.area_id AS adjacent_area_id,'||E'\n'||
'		   COUNT(d2.area_id) OVER(PARTITION BY d1.area_id ORDER BY d2.area_id) AS num_adjacencies'||E'\n'||
'	  FROM c d1, c d2'||E'\n'||
'	 WHERE d1.area_id       != d2.area_id'||E'\n'||
'	   AND ST_Intersects(d1.geom, d2.geom)'||E'\n'||
'), e AS ('||E'\n'||
'	SELECT d.area_id::VARCHAR AS area_id,'||E'\n'||
'		   COUNT(d.area_id)::INTEGER AS num_adjacencies,'||E'\n'|| 
'		   string_agg(d.adjacent_area_id, '','' ORDER BY d.adjacent_area_id)::VARCHAR AS adjacency_list'||E'\n'||
'	  FROM d'||E'\n'||
'	 GROUP BY d.area_id'||E'\n'||
')'||E'\n'||
'SELECT e.*'||E'\n'||
'  FROM e'||E'\n'||
'  ORDER BY 1, 2';
	END IF;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_GetAdjacencyMatrix', '[52005] SQL>'||E'\n'||'%;', sql_stmt::VARCHAR);
--
-- Execute
--
	BEGIN
		IF geometry_table IS NULL THEN /* Pre tilemaker: no geometry table */
			RETURN QUERY EXECUTE sql_stmt USING c1_rec.study_id, c1_rec.study_geolevel_name;
		ELSE
			RETURN QUERY EXECUTE sql_stmt USING c1_rec.study_id, c3_rec.geolevel_id;
		END IF;
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_GetAdjacencyMatrix() caught: '||E'\n'||
				SQLERRM::VARCHAR||' in SQL> '||sql_stmt||E'\n'||'Detail: '||v_detail::VARCHAR;
			RAISE INFO '52006: %', error_message;
--
			RAISE;
	END;
	
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_GetAdjacencyMatrix(INTEGER) IS 'Function: 	rif40_GetAdjacencyMatrix()
Parameters:	study id
Returns:	area_id, num_adjacencies, adjacency_list as a table. 
Description: Get study area adjacency matrix required by INLA	

Needs to have adjacency_list limited to 8060 characters where converted to use adjacencies_<geography>

Generates and executes SQL>

WITH b AS (
        SELECT area_id, band_id
          FROM rif40_study_areas b1
         WHERE b1.study_id = $1
), c AS (
        SELECT b.area_id, b.band_id, c1.optimised_geometry
          FROM t_rif40_sahsu_geometry c1, b
    WHERE c1.geolevel_name = $2
      AND c1.area_id       = b.area_id
), d AS (
        SELECT d1.band_id,
                   d1.area_id,
                   d2.area_id AS adjacent_area_id,
                   COUNT(d2.area_id) OVER(PARTITION BY d1.area_id ORDER BY d2.area_id) AS num_adjacencies
          FROM c d1, c d2
         WHERE d1.area_id       != d2.area_id
           AND ST_Intersects(d1.optimised_geometry, d2.optimised_geometry)
), e AS (
        SELECT d.area_id::VARCHAR AS area_id, 
		       COUNT(d.area_id)::INTEGER AS num_adjacencies, 
		       string_agg(d.adjacent_area_id, '','' ORDER BY d.adjacent_area_id)::VARCHAR AS adjacency_list          FROM d
         GROUP BY d.area_id
)
SELECT e.*
  FROM e
  ORDER BY 1, 2;
  
Returns a table:

SELECT * FROM rif40_xml_pkg.rif40_GetAdjacencyMatrix(1) LIMIT 10;
     area_id     | num_adjacencies |     adjacency_list

-----------------+-----------------+------------------------------------------------------
 01.001.000100.1 |              17 | 01.001.000100.2,01.001.000200.1,01.002.000500.1,01.002.000500.4,01.002.000500.7,01.002.000600.1,01.002.000600.6,0
1.002.000600.8,01.002.000700.2,01.002.000800.7,01.002.000900.1,01.002.000900.2,01.002.000900.4,01.002.001300.1,01.002.001500.2,01.004.011100.1,01.005.
002400.1
 01.001.000100.2 |               2 | 01.001.000100.1,01.001.000200.1
 01.001.000200.1 |               4 | 01.001.000100.1,01.001.000100.2,01.001.000300.1,01.005.002400.1
 01.001.000300.1 |               1 | 01.001.000200.1
 01.002.000300.1 |               3 | 01.002.000300.2,01.002.000300.3,01.002.000300.4
 01.002.000300.2 |               4 | 01.002.000300.1,01.002.000300.4,01.002.000500.3,01.002.000600.2
 01.002.000300.3 |               3 | 01.002.000300.1,01.002.000300.4,01.002.000300.5
 01.002.000300.4 |               5 | 01.002.000300.1,01.002.000300.2,01.002.000300.3,01.002.000300.5,01.002.000600.2
 01.002.000300.5 |               2 | 01.002.000300.3,01.002.000300.4
 01.002.000400.1 |               6 | 01.002.000400.2,01.002.000400.5,01.002.000400.7,01.002.001700.1,01.002.001700.2,01.002.001700.3
(10 rows)
';
	
--
-- Grants
--
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetAdjacencyMatrix(INTEGER) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetAdjacencyMatrix(INTEGER) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetAdjacencyMatrix(INTEGER) TO rif_manager;

--
-- Eof