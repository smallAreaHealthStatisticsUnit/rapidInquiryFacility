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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	None
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze
 */
DECLARE
	c1_adpop2 CURSOR(l_geography VARCHAR) FOR
		SELECT denominator_table, COUNT(*) total_numerators 
		  FROM rif40_num_denom
		 WHERE geography = l_geography
		   AND automatic =1
		 GROUP BY denominator_table;
	c2_adpop2 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c2_rec rif40_geographies%ROWTYPE;
	c1_rec RECORD;
--
	i INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'add_population_to_rif40_geolevels_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Create lookup and Hierarchy tables
--
	FOR c2_rec IN c2_adpop2 LOOP
		i:=0;
		FOR c1_rec IN c1_adpop2(c2_rec.geography) LOOP
			i:=i+1;
			IF i = 1 THEN
				PERFORM rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(c2_rec.geography, c1_rec.denominator_table);
			END IF;
		END LOOP;
--
		IF i = 0 THEN
--
-- Demoted to a warning for EW01 geography testing
--
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'add_population_to_rif40_geolevels_geometry', 'No automatic denominators for geography: %', 
				c2_rec.geography::VARCHAR	/* Geography */);
--			PERFORM rif40_log_pkg.rif40_error(-10052, 'add_population_to_rif40_geolevels_geometry', 'No automatic denominators for geography: %', 
--				c2_rec.geography::VARCHAR	/* Geography */);
		ELSIF i > 1 THEN
			PERFORM rif40_log_pkg.rif40_error(-10051, 'add_population_to_rif40_geolevels_geometry', '>1 (%) automatic denominators for geography: %', 
				c2_rec.geography::VARCHAR	/* Geography */,
				i::VARCHAR			/* Number of automatic denominators */);
		END IF;
	END LOOP;
--
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10053, 'add_population_to_rif40_geolevels_geometry', 'No geographies found');
	END IF;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry() IS 'Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	None
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(l_geography VARCHAR, l_population_table VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	geography, denominator population table
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze

Example update statement:

WITH a AS (
	SELECT MAX(year) AS max_year 
	  FROM sahsuland_pop
), b AS (
	SELECT level2, TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total
	  FROM sahsuland_pop b, a
	 WHERE b.year = a.max_year
	 GROUP BY level2, TRUNC(age_sex_group/100), year
), c AS (
	SELECT b1.level2, b1.total AS total_males, b2.total AS total_females, b1.population_year
	  FROM b b1, b b2
	 WHERE b1.level2 = b2.level2
	   AND b1.sex    = 1
	   AND b2.sex    = 2
)
UPDATE t_rif40_sahsu_geometry d
   SET total_males = (
	SELECT total_males
	  FROM c
	 WHERE c.level2 = d.area_id),
       total_females = (
	SELECT total_females
	  FROM c
	 WHERE c.level2 = d.area_id),
       population_year = (
	SELECT population_year
	  FROM c
	 WHERE c.level2 = d.area_id)
 WHERE d.geolevel_name = 'LEVEL2';

To test:

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_sahsu_geometry
 WHERE geolevel_name = 'LEVEL2';

EW01 test SELECT

WITH a AS (
	SELECT MAX(year) AS max_year 
	  FROM v_ew01_rif_pop_asg_1_oa2001
), b AS (
	SELECT ladua2001, TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total
	  FROM v_ew01_rif_pop_asg_1_oa2001 b, a
	 WHERE b.year = a.max_year
	 GROUP BY ladua2001, TRUNC(age_sex_group/100), year
), c AS (
	SELECT b1.ladua2001, b1.total AS total_males, b2.total AS total_females, b1.population_year
	  FROM b b1, b b2
	 WHERE b1.ladua2001 = b2.ladua2001
	   AND b1.sex    = 1
	   AND b2.sex    = 2
)
SELECT * 
  FROM c;

 */
DECLARE
	c1_adpop CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2_adpop CURSOR(l_population_table VARCHAR) FOR
		SELECT *
		  FROM rif40_tables
		 WHERE table_name = l_population_table;
	c3_adpop CURSOR(l_population_table VARCHAR, l_schema VARCHAR, l_column VARCHAR) FOR
		SELECT *
		  FROM information_schema.columns
		 WHERE table_name   = LOWER(l_population_table)
		   AND table_schema = l_schema
		   AND column_name  = LOWER(l_column);
	c1_rec t_rif40_geolevels%ROWTYPE;
	c2_rec rif40_tables%ROWTYPE;
	c3_rec information_schema.columns%ROWTYPE;
--
	l_schema VARCHAR;
	sql_stmt VARCHAR;
	i INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'add_population_to_rif40_geolevels_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR			/* Username */);
	ELSIF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10041, 'add_population_to_rif40_geolevels_geometry', 'Null geography');
	ELSIF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10042, 'add_population_to_rif40_geolevels_geometry', 'Null population table for geography: %', 
			USER::VARCHAR			/* Geography */);
	END IF;
--
-- Check population table is a valid RIF denominator and exists
--
	OPEN c2_adpop(l_population_table);
	FETCH c2_adpop INTO c2_rec;
	CLOSE c2_adpop;
	IF c2_rec.table_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10018, 'add_population_to_rif40_geolevels_geometry', 'Geography: % Population table: % is not a RIF table', 
			l_geography::VARCHAR		/* Geography */,
			l_population_table::VARCHAR	/* Population table */);
	ELSIF c2_rec.isindirectdenominator != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-10019, 'add_population_to_rif40_geolevels_geometry', 'Geography: % Population table: % is a RIF table, but not an indirect denominator', 
			l_geography::VARCHAR		/* Geography */,
			l_population_table::VARCHAR	/* Population table */);
	ELSIF rif40_sql_pkg.rif40_is_object_resolvable(l_population_table) != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-10020, 'add_population_to_rif40_geolevels_geometry', 
			'Geography: % Population table: % is a RIF indirect denominator but is not resolvable', 
			l_geography::VARCHAR		/* Geography */,
			l_population_table::VARCHAR	/* Population table */);
	END IF;
	l_schema:=rif40_sql_pkg.rif40_object_resolve(l_population_table);
--
-- Do population update once per geolevel
--
	FOR c1_rec IN c1_adpop(l_geography) LOOP
--
-- Check geography geolevel is supported
--
		OPEN c3_adpop(l_population_table, l_schema, c1_rec.geolevel_name);
		FETCH c3_adpop INTO c3_rec;
		CLOSE c3_adpop;
		IF c3_rec.column_name IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-10021, 'add_population_to_rif40_geolevels_geometry', 
				'Population table: %.% does not have % geography geolevel column: %', 
				l_schema::VARCHAR			/* Schema */, 
				LOWER(l_population_table)::VARCHAR	/* Population table */, 
				l_geography::VARCHAR			/* Geography */, 
				LOWER(c1_rec.geolevel_name)::VARCHAR	/* Geolevel name */);
		END IF;
--
-- Create UPDATE statement
--
		sql_stmt:='WITH a AS ('||E'\n'||
			  E'\t'||'SELECT MAX(year) AS max_year'||E'\n'|| 
			  E'\t'||'  FROM '||quote_ident(LOWER(l_population_table))||E'\n'||
			  '), b AS ('||E'\n'||
			  E'\t'||'SELECT '||quote_ident(LOWER(c1_rec.geolevel_name))||', TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total'||E'\n'||
			  E'\t'||'  FROM '||quote_ident(LOWER(l_population_table))||' b, a'||E'\n'||
			  E'\t'||' WHERE b.year = a.max_year'||E'\n'||
			  E'\t'||' GROUP BY '||quote_ident(LOWER(c1_rec.geolevel_name))||', TRUNC(age_sex_group/100), year'||E'\n'||
			  '), c AS ('||E'\n'||
			  E'\t'||'SELECT b1.'||quote_ident(LOWER(c1_rec.geolevel_name))||', b1.total AS total_males, b2.total AS total_females, b1.population_year'||E'\n'||
			  E'\t'||'  FROM b b1, b b2'||E'\n'||
			  E'\t'||' WHERE b1.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = b2.'||quote_ident(LOWER(c1_rec.geolevel_name))||''||E'\n'||
			  E'\t'||'   AND b1.sex    = 1'||E'\n'||
	 		  E'\t'||'   AND b2.sex    = 2'||E'\n'||
			  ')'||E'\n'||
			  'UPDATE '||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||' d'||E'\n'||
			  '   SET total_males = ('||E'\n'||
			  E'\t'||'SELECT total_males'||E'\n'||
			  E'\t'||'  FROM c'||E'\n'||
			  E'\t'||' WHERE c.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = d.area_id),'||E'\n'||
			  '       total_females = ('||E'\n'||
			  E'\t'||'SELECT total_females'||E'\n'||
			  E'\t'||'  FROM c'||E'\n'||
			  E'\t'||' WHERE c.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = d.area_id),'||E'\n'||
			  '       population_year = ('||E'\n'||
			  E'\t'||'SELECT population_year'||E'\n'||
			  E'\t'||'  FROM c'||E'\n'||
			  E'\t'||' WHERE c.'||quote_ident(LOWER(c1_rec.geolevel_name))||' = d.area_id)'||E'\n'||
 			  ' WHERE d.geolevel_name = '''||c1_rec.geolevel_name||'''';
		IF sql_stmt IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-10037, 'add_population_to_rif40_geolevels_geometry', 'NULL SQL statement for %.%',
				c1_rec.geography::VARCHAR		/* Geography */,
				c1_rec.geolevel_name::VARCHAR		/* Geolevel name */);
		END IF;
		i:=i+1;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'add_population_to_rif40_geolevels_geometry', 'SQL> %', sql_stmt::VARCHAR);
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
--
-- Check geolevels were processed
--
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10017, 'add_population_to_rif40_geolevels_geometry', 'Geography: % No rows found in: t_rif40_geolevels',
			l_geography::VARCHAR		/* Geography */);
	END IF;
--
-- Re-analyze
--
	sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry');
	IF sql_stmt IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10038, 'add_population_to_rif40_geolevels_geometry', 'NULL SQL statement for Geography: %',
			l_geography::VARCHAR		/* Geography */);
	END IF;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(VARCHAR, VARCHAR) IS 'Function: 	add_population_to_rif40_geolevels_geometry()
Parameters:	geography, denominator population table
Returns:	Nothing
Description:	Add denominator population table to geography geolevel geometry data, re-analyze

Example update statement:

WITH a AS (
	SELECT MAX(year) AS max_year 
	  FROM sahsuland_pop
), b AS (
	SELECT level2, TRUNC(age_sex_group/100) AS sex, year AS population_year, SUM(total) AS total
	  FROM sahsuland_pop b, a
	 WHERE b.year = a.max_year
	 GROUP BY level2, TRUNC(age_sex_group/100), year
), c AS (
	SELECT b1.level2, b1.total AS total_males, b2.total AS total_females, b1.population_year
	  FROM b b1, b b2
	 WHERE b1.level2 = b2.level2
	   AND b1.sex    = 1
	   AND b2.sex    = 2
)
UPDATE t_rif40_sahsu_geometry d
   SET total_males = (
	SELECT total_males
	  FROM c
	 WHERE c.level2 = d.area_id),
       total_females = (
	SELECT total_females
	  FROM c
	 WHERE c.level2 = d.area_id),
       population_year = (
	SELECT population_year
	  FROM c
	 WHERE c.level2 = d.area_id)
 WHERE d.geolevel_name = ''LEVEL2'';';

--
-- Eof