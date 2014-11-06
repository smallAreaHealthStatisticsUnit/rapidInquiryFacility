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
--								  check_rif40_hierarchy_lookup_tables() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Diff geography hierarchy tables for 'missing' or 'spurious additional' or 'multiple hierarchy' geolevel codes
		Calls rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(geography)
 */
DECLARE
	c0 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c0_rec rif40_geographies%ROWTYPE;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'check_rif40_hierarchy_lookup_tables', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	FOR c0_rec IN c0 LOOP
		PERFORM rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(c0_rec.geography);
	END LOOP;
	PERFORM rif40_log_pkg.rif40_log('INFO', 'check_rif40_hierarchy_lookup_tables', 'All geography codes check OK');
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables() IS 'Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Diff geography hierarchy tables for ''missing'', ''spurious additional'' or ''multiple hierarchy'' geolevel codes
		Calls rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(geography)';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Geography 
Returns:	Nothing
Description:	Diff geography hierarchy table for 'missing', 'spurious additional' or 'multiple hierarchy' geolevel codes

		Calls lf_check_rif40_hierarchy_lookup_tables() to do diff
 */
DECLARE
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography = l_geography;
	c1_rec rif40_geographies%ROWTYPE;
--
	e INTEGER:=0;
	f INTEGER:=0;
	g INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'check_rif40_hierarchy_lookup_tables', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10003, 'check_rif40_hierarchy_lookup_tables', 'NULL geography parameter');
	END IF;	
--
	OPEN c1(l_geography);
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10004, 'check_rif40_hierarchy_lookup_tables', 'geography: % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;	
--
-- Call diff and multiple hierarchy tests
--
	e:=rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(c1_rec.geography, c1_rec.hierarchytable, 'missing');
	f:=rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(c1_rec.geography, c1_rec.hierarchytable, 'spurious additional');
	g:=rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(c1_rec.geography, c1_rec.hierarchytable, 'multiple hierarchy');
--
	IF e+f > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10005, 'check_rif40_hierarchy_lookup_tables', 'Geography: % codes check % missing, % spurious additional, % hierarchy fails', 
			c1_rec.geography	/* Geography */, 
			e::VARCHAR		/* Missing */, 
			f::VARCHAR		/* Spurious additional */, 
			g::VARCHAR		/* Multiple hierarchy */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'check_rif40_hierarchy_lookup_tables', 'Geography: % codes check OK', 
			c1_rec.geography::VARCHAR	/* Geography */);
	END IF;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(VARCHAR) IS 'Function: 	check_rif40_hierarchy_lookup_tables()
Parameters:	Geography 
Returns:	Nothing
Description:	Diff geography hierarchy table for ''missing'', ''spurious additional'' or ''multiple hierarchy'' geolevel codes

		Calls lf_check_rif40_hierarchy_lookup_tables() to do diff';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(l_geography VARCHAR, l_hierarchytable VARCHAR, l_type VARCHAR)
RETURNS integer 
SECURITY INVOKER
AS $body$
/*

Function: 	lf_check_rif40_hierarchy_lookup_tables()
Parameters:	Geography, hierarchy table, type: 'missing', 'spurious additional' or 'multiple hierarchy'
Returns:	Nothing
Description:	Diff geography hierarchy table using dynamic method 4
		Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on

Example of dynamic SQL. Note the use of an array return type to achieve method 4

WITH /- missing -/ a1 AS (
        SELECT COUNT(*) AS level1_total
          FROM (
                SELECT level1 FROM sahsuland_geography
                EXCEPT
                SELECT level1 FROM sahsuland_level1) as1)
, a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2 FROM sahsuland_geography
                EXCEPT
                SELECT level2 FROM sahsuland_level2) as2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3 FROM sahsuland_geography
                EXCEPT
                SELECT level3 FROM sahsuland_level3) as3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4 FROM sahsuland_geography
                EXCEPT
                SELECT level4 FROM sahsuland_level4) as4)
SELECT ARRAY[a1.level1_total, a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a1, a2, a3, a4;

Or: 

WITH -* hierarchy *- a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2, COUNT(DISTINCT(level1)) AS total
                  FROM sahsuland_geography
                 GROUP BY level2
                HAVING COUNT(DISTINCT(level1)) > 1) AS2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3, COUNT(DISTINCT(level2)) AS total
                  FROM sahsuland_geography
                 GROUP BY level3
                HAVING COUNT(DISTINCT(level2)) > 1) AS3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4, COUNT(DISTINCT(level3)) AS total
                  FROM sahsuland_geography
                 GROUP BY level4
                HAVING COUNT(DISTINCT(level3)) > 1) AS4)
SELECT ARRAY[a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a2, a3, a4;
 
 */
DECLARE
	c2 CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geolevel_id;
	c3 REFCURSOR;
	c4 CURSOR(l_geography VARCHAR, l_geolevel_id INTEGER) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography   = l_geography
		   AND geolevel_id = l_geolevel_id
		 ORDER BY geolevel_id;
--
	c2_rec t_rif40_geolevels%ROWTYPE;
	c3_rec RECORD;
	c4_rec t_rif40_geolevels%ROWTYPE;
--
	sql_stmt 		VARCHAR;
	previous_geolevel_name 	VARCHAR:=NULL;
	i INTEGER;
	e INTEGER:=0;
	field INTEGER;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'lf_check_rif40_hierarchy_lookup_tables', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	sql_stmt:='WITH /* '||l_type||' */ ';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
				sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||' AS ('||E'\n';
			END IF;
			sql_stmt:=sql_stmt||E'\t'||'SELECT COUNT(*) AS '||quote_ident(LOWER(c2_rec.geolevel_name)||'_total')||E'\n';
			sql_stmt:=sql_stmt||E'\t'||'  FROM ('||E'\n';
		END IF;
		IF l_type = 'missing' THEN
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(l_hierarchytable))||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'EXCEPT'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(c2_rec.lookup_table))||
				') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
		ELSIF l_type = 'spurious additional' THEN
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(c2_rec.lookup_table))||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'EXCEPT'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||' FROM '||quote_ident(LOWER(l_hierarchytable))||
				') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
		ELSIF l_type = 'multiple hierarchy' THEN
			IF previous_geolevel_name IS NOT NULL THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||
					', COUNT(DISTINCT('||previous_geolevel_name||')) AS total'||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'  FROM '||quote_ident(LOWER(l_hierarchytable))||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||' GROUP BY '||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n';
				sql_stmt:=sql_stmt||E'\t'||E'\t'||'HAVING COUNT(DISTINCT('||previous_geolevel_name||')) > 1'||
					') '||quote_ident('as'||c2_rec.geolevel_id)||')'||E'\n';
			END IF;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-10017, 'lf_check_rif40_hierarchy_lookup_tables', 
				'Invalid check type: %, valid types are: ''missing'', ''spurious additional'', or ''multiple hierarchy''', 
				l_type::VARCHAR 	/* Check type */);
		END IF;
		previous_geolevel_name:=quote_ident(LOWER(c2_rec.geolevel_name));
	END LOOP;
	sql_stmt:=sql_stmt||'SELECT ARRAY[';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id)||'.'||quote_ident(LOWER(c2_rec.geolevel_name)||'_total');
			END IF;
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||'] AS res_array'||E'\n'||'FROM ';
	i:=0;
	FOR c2_rec IN c2(l_geography) LOOP
		i:=i+1;
		IF l_type = 'multiple hierarchy' THEN
			IF i = 1 THEN
				NULL;
			ELSIF i > 2 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id);
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id);
			END IF;
		ELSE
			IF i != 1 THEN
				sql_stmt:=sql_stmt||', '||quote_ident('a'||c2_rec.geolevel_id);
			ELSE
				sql_stmt:=sql_stmt||quote_ident('a'||c2_rec.geolevel_id);
			END IF;
		END IF;
	END LOOP;
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'lf_check_rif40_hierarchy_lookup_tables', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c3 FOR EXECUTE sql_stmt;
	FETCH c3 INTO c3_rec;
--
-- Process results array
--
	i:=0;
	FOREACH field IN ARRAY c3_rec.res_array LOOP
		i:=i+1;
		OPEN c4(l_geography, i);
		FETCH c4 INTO c4_rec;
		CLOSE c4;
		IF field != 0 THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'lf_check_rif40_hierarchy_lookup_tables', 'Geography: % geolevel %: [%] % codes: %', 
				l_geography::VARCHAR			/* Geography */, 
				i::VARCHAR				/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */, 
				field::VARCHAR				/* Area ID */);
			e:=e+1;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'lf_check_rif40_hierarchy_lookup_tables', 'Geography: % geolevel %: [%] no % codes', 
				l_geography::VARCHAR			/* Geography */, 
				i::VARCHAR				/* Geolevel ID */, 
				LOWER(c4_rec.geolevel_name)::VARCHAR	/* Geolevel name */, 
				l_type::VARCHAR				/* Check type */);
		END IF;
	END LOOP;
--
	RETURN e;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	lf_check_rif40_hierarchy_lookup_tables()
Parameters:	Geography, hierarchy table, type: ''missing'', ''spurious additional'' or ''multiple hierarchy''
Returns:	Nothing
Description:	Diff geography hierarchy table using dynamic method 4
		Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on

Example of dynamic SQL. Note the use of an array return type to achieve method 4

WITH /* missing */ a1 AS (
        SELECT COUNT(*) AS level1_total
          FROM (
                SELECT level1 FROM sahsuland_geography
                EXCEPT
                SELECT level1 FROM sahsuland_level1) as1)
, a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2 FROM sahsuland_geography
                EXCEPT
                SELECT level2 FROM sahsuland_level2) as2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3 FROM sahsuland_geography
                EXCEPT
                SELECT level3 FROM sahsuland_level3) as3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4 FROM sahsuland_geography
                EXCEPT
                SELECT level4 FROM sahsuland_level4) as4)
SELECT ARRAY[a1.level1_total, a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a1, a2, a3, a4;

Or:

WITH /* hierarchy */ a2 AS (
        SELECT COUNT(*) AS level2_total
          FROM (
                SELECT level2, COUNT(DISTINCT(level1)) AS total
                  FROM sahsuland_geography
                 GROUP BY level2
                HAVING COUNT(DISTINCT(level1)) > 1) AS2)
, a3 AS (
        SELECT COUNT(*) AS level3_total
          FROM (
                SELECT level3, COUNT(DISTINCT(level2)) AS total
                  FROM sahsuland_geography
                 GROUP BY level3
                HAVING COUNT(DISTINCT(level2)) > 1) AS3)
, a4 AS (
        SELECT COUNT(*) AS level4_total
          FROM (
                SELECT level4, COUNT(DISTINCT(level3)) AS total
                  FROM sahsuland_geography
                 GROUP BY level4
                HAVING COUNT(DISTINCT(level3)) > 1) AS4)
SELECT ARRAY[a2.level2_total, a3.level3_total, a4.level4_total] AS res_array
FROM a2, a3, a4;

';

--
-- Eof