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
--								  fix_null_geolevel_names() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.fix_null_geolevel_names()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	fix_null_geolevel_names()
Parameters:	None
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Add unique index
 */
DECLARE
	c2_fixnul2 CURSOR FOR
		SELECT *
		  FROM rif40_geographies;
--
	c2_rec rif40_geographies%ROWTYPE;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'fix_null_geolevel_names', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Create lookup and Hierarchy tables
--
	FOR c2_rec IN c2_fixnul2 LOOP
		PERFORM rif40_geo_pkg.fix_null_geolevel_names(c2_rec.geography);
	END LOOP;
--
	RETURN;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names() IS 'Function: 	fix_null_geolevel_names()
Parameters:	None
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Add unique index';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.fix_null_geolevel_names(l_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	fix_null_geolevel_names()
Parameters:	geography
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Fix non-unique names in lookup tables and geolevel geometry table
		Add unique index

Fix NULL geolevel names in lookup table data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
	SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.name
                  FROM ew2001_coa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.oa2001);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_coa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.oa2001)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix NULL geolevel names in geography geolevel geometry data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.area_id
                  FROM rif40_partitions.p_rif40_geolevels_geometry_ew01_oa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.area_id);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE rif40_partitions.p_rif40_geolevels_geometry_ew01_oa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.area_id)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix non-unique names in lookup tables and geolevel geometry table example SQL

DROP TABLE IF EXISTS temp_fix_null_geolevel_names;

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS
WITH a AS (
	SELECT name, COUNT(*) AS total
	  FROM ew2001_ward2001
	 GROUP BY name
	HAVING COUNT(*) > 1
)
SELECT DISTINCT b.ward2001, a.name, c.ladua2001, a.name||'('||c.ladua2001||')' AS new_name
  FROM a, ew2001_ward2001 b, ew2001_geography c
 WHERE a.name = b.name
   AND c.ward2001 = b.ward2001;

CREATE UNIQUE INDEX ward2001_pk ON temp_fix_null_geolevel_names(ward2001);
CREATE UNIQUE INDEX ward2001_uk ON temp_fix_null_geolevel_names(new_name);
ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.ward2001)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

UPDATE rif40_partitions.p_rif40_geolevels_geometry_ew01_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.area_id)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

 */

DECLARE
	c1_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT COUNT(geolevel_id) AS total_geolevel 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography;
	c2a_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id;
	c2_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography
		 ORDER BY geography, geolevel_id DESC;
	c3_fixnul 	REFCURSOR;
	c4_fixnul CURSOR(l_geography VARCHAR, l_geolevel_id INTEGER) FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography   = l_geography
		   AND geolevel_id = l_geolevel_id;
	c6_fixnul CURSOR(l_geography VARCHAR) FOR
		SELECT * 
		  FROM rif40_geographies
		 WHERE geography   = l_geography;
--
	total_geolevel	INTEGER;
	desc_geolevel	INTEGER=NULL;
	total_desc	INTEGER=NULL;
	l_rows		INTEGER;
	c2a_rec 	t_rif40_geolevels%ROWTYPE;
	c2_rec 		t_rif40_geolevels%ROWTYPE;
	c3_rec 		RECORD;
	higher_c4_rec 	t_rif40_geolevels%ROWTYPE;
	c5_rec 		RECORD;
	c6_rec 		rif40_geographies%ROWTYPE;
--
	sql_stmt 	VARCHAR;
	i 		INTEGER:=0;
	j 		INTEGER:=0;
	re_analyze 	BOOLEAN;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'fix_null_geolevel_names', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	OPEN c1_fixnul(l_geography);
	FETCH c1_fixnul INTO total_geolevel;
	CLOSE c1_fixnul;
--
	OPEN c6_fixnul(l_geography);
	FETCH c6_fixnul INTO c6_rec;
	CLOSE c6_fixnul;
	IF total_geolevel = 0 OR c6_rec.hierarchytable IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10031, 'fix_null_geolevel_names', 'Geography % not found', 
			l_geography::VARCHAR	/* Geography */);
	END IF;
--
-- First past. Start with lowest resolution geolevel 
--
	i:=total_geolevel;
--
-- Check geolevels were processed
--
	IF total_geolevel = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10017, 'fix_null_geolevel_names', 'No rows found in: t_rif40_geolevels for geography: %',
			l_geography::VARCHAR 	/* Geography */);
	END IF;
--
	FOR c2a_rec IN c2a_fixnul(l_geography) LOOP
		j:=j+1;
--
-- Get the total description fields for the geolevel lookup table
--
		sql_stmt:='SELECT COUNT('||quote_ident(LOWER(c2a_rec.geolevel_name))||') AS total_area_ids,'||E'\n'||
			  '       COUNT('||quote_ident(LOWER(c2a_rec.lookup_desc_column))||') AS total_desc,'||E'\n'||
			  '       COUNT(DISTINCT('||quote_ident(LOWER(c2a_rec.lookup_desc_column))||')) AS total_uniq_desc'||E'\n'||
			  '  FROM '||quote_ident(LOWER(c2a_rec.lookup_table));
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'fix_null_geolevel_names', 'Pass: 1 Get the total description fields SQL> %', 
			sql_stmt::VARCHAR		/* SQL statement */);
		OPEN c3_fixnul FOR EXECUTE sql_stmt;
		FETCH c3_fixnul INTO c3_rec;
		CLOSE c3_fixnul;
--
-- OK, found a complete set of descriptions
--
		IF c3_rec.total_desc = c3_rec.total_area_ids AND c3_rec.total_desc = c3_rec.total_uniq_desc THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'fix_null_geolevel_names',
				'Pass: 1 Geography % geolevel % lookup table: % has % unique description rows (%)', 
				l_geography::VARCHAR 				/* Geography */, 
				c2a_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2a_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				LOWER(c2a_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- Oops, there is a gap
--
			IF desc_geolevel IS NOT NULL AND c2a_rec.geolevel_id - desc_geolevel > 1 THEN
				PERFORM rif40_log_pkg.rif40_error(-10026, 'fix_null_geolevel_names', 
					'Pass: 1 Geography % gap found between geolevel IDs: % and % (with complete descriptive names) for geography: % not found',
					l_geography::VARCHAR 		/* Geography */, 
					desc_geolevel::VARCHAR 		/* First geolevel ID with complete descriptive names */,
					c2a_rec.geolevel_id::VARCHAR 	/* Current geolevel ID with complete descriptive names */,
					l_geography::VARCHAR 		/* Geography */);
			ELSE
				desc_geolevel:=c2a_rec.geolevel_id;
				total_desc:=c3_rec.total_desc;
			END IF;
/*		ELSE
			desc_geolevel checked later */
		END IF;
	END LOOP;
--
-- Check geolevels were processed
--
	IF j = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10048, 'fix_null_geolevel_names', 
			'No geolevels were processed in: t_rif40_geolevels for geography: %, expected geolevels: %',
			l_geography::VARCHAR 	/* Geography */,
			total_geolevel::VARCHAR	/* Expected geolevels */);
	END IF;
--
-- Check there is a geolevel with complete descriptive names 
--
	IF desc_geolevel IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10027, 'fix_null_geolevel_names', 'No geolevel with complete descriptive names for geography: %',
			l_geography::VARCHAR 	/* Geography */);
	END IF;
--
-- Get the geolevel definition of the highest resolution geolevel with complete descriptive names. This is as the first name part of the defaulted
-- missing descriptions.
--
-- Abellan LEVEL3(01.001.000100)
--
	OPEN c4_fixnul(l_geography, desc_geolevel);
	FETCH c4_fixnul INTO higher_c4_rec;
	CLOSE c4_fixnul;
	IF higher_c4_rec IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-10028, 'fix_null_geolevel_names', 'Geolevel ID: % (with complete descriptive names) for geography: % not found',
			desc_geolevel::VARCHAR 	/* Geolevel ID with complete descriptive names */,
			l_geography::VARCHAR 	/* Geography */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names',
			'[10028] Geography % using geolevel % lookup table: % has % unique description rows (%) as the higher reference geolevel for descriptive names', 
			l_geography::VARCHAR					/* Geography */, 
			higher_c4_rec.geolevel_name::VARCHAR			/* Higher reference geolevel name */, 
			higher_c4_rec.lookup_table::VARCHAR			/* Higher reference geolevel lookup table */,
			total_desc::VARCHAR					/* Geolevel lookup table unique description rows */, 
			LOWER(higher_c4_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
	END IF;
--
-- Second pass. Start with highest resolution geolevel 
--
	j:=0;
	FOR c2_rec IN c2_fixnul(l_geography) LOOP
		j:=j+1;
		i:=i-1;
		re_analyze:=FALSE;
--
-- Get the total description fields for the geolevel lookup table
--
		sql_stmt:='SELECT COUNT('||quote_ident(LOWER(c2_rec.geolevel_name))||') AS total_area_ids,'||E'\n'||
			  '       COUNT('||quote_ident(LOWER(c2_rec.lookup_desc_column))||') AS total_desc,'||E'\n'||
			  '       SUM(CASE WHEN '||quote_ident(LOWER(c2_rec.lookup_desc_column))||' IS NULL THEN 1 ELSE 0 END) AS total_null_desc,'||E'\n'||
			  '       COUNT(DISTINCT('||quote_ident(LOWER(c2_rec.lookup_desc_column))||')) AS total_uniq_desc'||E'\n'||
			  '  FROM '||quote_ident(LOWER(c2_rec.lookup_table));
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'fix_null_geolevel_names', 'Pass: 2 Get the total description fields SQL> %', 
			sql_stmt::VARCHAR		/* SQL statement */);
		OPEN c3_fixnul FOR EXECUTE sql_stmt;
		FETCH c3_fixnul INTO c3_rec;
		CLOSE c3_fixnul;
--
-- Checks:
--
-- a) Lookup table empty
--
		l_rows:=NULL;
		IF c3_rec.total_area_ids = 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-10022, 'fix_null_geolevel_names', 
				'Pass: 2a Geography % geolevel % lookup table: % has no rows', 
				l_geography::VARCHAR		/* Geography */, 
				c2_rec.geolevel_name::VARCHAR	/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR	/* Geolevel lookup table */);
--
-- b) level 1 (lowest resolution geolevel) must have only 1 row
--
		ELSIF c2_rec.geolevel_id = 1 AND c3_rec.total_area_ids > 1 THEN 
			PERFORM rif40_log_pkg.rif40_error(-10023, 'fix_null_geolevel_names',
				'Pass: 2b Geography % lowest resolution geolevel % lookup table: % has >1: % rows (%) - OK', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- c) OK - all present and unique
--
		ELSIF c3_rec.total_desc = c3_rec.total_area_ids AND c3_rec.total_desc = c3_rec.total_uniq_desc THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names',
				'Pass: 2c Geography % geolevel % lookup table: % has % unique description rows (%) - OK', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- d) Not unique
--
		ELSIF c3_rec.total_desc = c3_rec.total_area_ids AND c3_rec.total_desc != c3_rec.total_uniq_desc THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'fix_null_geolevel_names',
				'Pass: 2d Geography % geolevel % lookup table: % has % description rows (%), only % are unique, % are NULL - FIX', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table description rows */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */,
				c3_rec.total_uniq_desc::VARCHAR			/* Geolevel lookup table unique description rows */,
				c3_rec.total_null_desc::VARCHAR			/* Geolevel lookup table NULL description rows */);
--
-- Fix non unique names
--
/*
DROP TABLE IF EXISTS temp_fix_null_geolevel_names;

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS
WITH a AS (
	SELECT name, COUNT(*) AS total
	  FROM ew2001_ward2001
	 GROUP BY name
	HAVING COUNT(*) > 1
)
SELECT DISTINCT b.ward2001, a.name, c.ladua2001, a.name||'('||c.ladua2001||')' AS new_name
  FROM a, ew2001_ward2001 b, ew2001_geography c
 WHERE a.name = b.name
   AND c.ward2001 = b.ward2001;

CREATE UNIQUE INDEX ward2001_pk ON temp_fix_null_geolevel_names(ward2001);
CREATE UNIQUE INDEX ward2001_uk ON temp_fix_null_geolevel_names(new_name);
ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.ward2001)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

UPDATE rif40_partitions.p_rif40_geolevels_geometry_ew01_ward2001 c
           SET name = (SELECT new_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.area_id)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

 */
			sql_stmt:='DROP TABLE IF EXISTS temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS /* Lookup table de-duplicate update */'||E'\n'||
				'WITH a AS ('||E'\n'||
				E'\t'||'SELECT name, COUNT(*) AS total'||E'\n'||
				E'\t'||'  FROM '||quote_ident(LOWER(c2_rec.lookup_table))||E'\n'||
				E'\t'||' GROUP BY name'||E'\n'||
				E'\t'||'HAVING COUNT(*) > 1'||E'\n'||
				')'||E'\n'||
				'SELECT DISTINCT b.'||quote_ident(LOWER(c2_rec.geolevel_name))||
					', a.name'||
					', c.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||
					', a.name'||'||''(''||c.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||'||'')'' AS new_name'||E'\n'||
				'  FROM a, '||quote_ident(LOWER(c2_rec.lookup_table))||' b, ew2001_geography c'||E'\n'||
				' WHERE a.name'||' = b.'||quote_ident(LOWER(c2_rec.geolevel_name))||''||E'\n'||
				'   AND c.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = b.'||quote_ident(LOWER(c2_rec.geolevel_name));

			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_pk ON'||
				' temp_fix_null_geolevel_names('||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_uk ON temp_fix_null_geolevel_names(new_name)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='ANALYZE VERBOSE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			sql_stmt:='UPDATE /* d.1 */ '||quote_ident(LOWER(c2_rec.lookup_table))||' c'||E'\n'||
				'   SET name = (SELECT new_name /* Replacement 3 */ '||E'\n'||
				'		  FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		 WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.'||quote_ident(LOWER(c2_rec.geolevel_name))||')'||E'\n'||
				' WHERE c.'||quote_ident(LOWER(c2_rec.geolevel_name))||' IN ('||E'\n'||
				E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n'||
				E'\t'||'  FROM temp_fix_null_geolevel_names)';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2d Geography % fixed geolevel %: % resolution duplicate lookup table % % descriptions', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				c2_rec.lookup_table::VARCHAR		/* Lookup table */,
				l_rows::VARCHAR				/* Rows updated */);
--
			sql_stmt:='UPDATE /* d.2 */ rif40_partitions.'||quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))||' /* Geometry table */ c'||E'\n'||
				'   SET name = (SELECT new_name /* Replacement 4 */ '||E'\n'||
				'		  FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		 WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.area_id)'||E'\n'||
				' WHERE c.area_id IN ('||E'\n'||
				E'\t'||'SELECT '||quote_ident(LOWER(c2_rec.geolevel_name))||E'\n'||
				E'\t'||'  FROM temp_fix_null_geolevel_names)';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2d Geography % fixed geolevel %: % resolution duplicate geolevel geometry % % descriptions', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))::VARCHAR /* Geometry table */,
				l_rows::VARCHAR				/* Rows updated */);
--
			sql_stmt:='DROP TABLE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			re_analyze:=TRUE;

--
-- e) Missing single item top level - fix using geography description
--
		ELSIF c2_rec.geolevel_id = 1 AND c3_rec.total_area_ids = 1 THEN /* Fix top level lookup */
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'fix_null_geolevel_names',
				'Pass: 2f Geography % geolevel % lookup table: % missing single item top level %/% incomplete description rows (%) - FIX ', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				c3_rec.total_area_ids::VARCHAR			/* Geolevel lookup table unique name rows (AREA_IDs) */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);

			sql_stmt:='UPDATE /* e.1 */ '||quote_ident(LOWER(c2_rec.lookup_table))||E'\n'||
				  '   SET '||quote_ident(LOWER(c2_rec.lookup_desc_column))||' = '||quote_literal(l_geography);
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2e Geography % fixed geolevel %: % lowest resolution lookup table % % description', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				c2_rec.lookup_desc_column::VARCHAR	/* Lookup table */,
				l_rows::VARCHAR				/* Rows updated */);
--
-- cannot ALTER TABLE "sahsuland_level1" because it is being used by active queries in this session
--
--			sql_stmt:='ALTER TABLE '||quote_ident(LOWER(c2_rec.lookup_table))||' ALTER COLUMN '||quote_ident(LOWER(c2_rec.lookup_desc_column))||' SET NOT NULL';
--			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Re-create unique index
--
			sql_stmt:='DROP INDEX IF EXISTS '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2');
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2')||
				' ON '||quote_ident(LOWER(c2_rec.lookup_table))||'('||quote_ident(LOWER(c2_rec.lookup_desc_column))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Fix top level shapefile if possible
--
			IF c2_rec.shapefile_desc_column IS NOT NULL THEN 
				sql_stmt:='UPDATE /* e.2 */ '||quote_ident(LOWER(c2_rec.shapefile_table))||E'\n'||
				  '   SET '||quote_ident(LOWER(c2_rec.shapefile_desc_column))||
					' = (SELECT description FROM rif40_geographies WHERE geography = '''||l_geography||''')';
				l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
					'Pass: 2e Geography % fixed geolevel %: % lowest resolution shapefile table % description', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
					l_rows::VARCHAR				/* Rows updated */);
			END IF;
--
-- Fix geometry table (it must exist)
--
			sql_stmt:='UPDATE /* e.3 */ '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||E'\n'||
				  '   SET name = (SELECT description FROM rif40_geographies WHERE geography = '''||l_geography||''')'||E'\n'||
				  ' WHERE geolevel_name = '''||c2_rec.geolevel_name||'''';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
				'Pass: 2e Geography % fixed geolevel %: % lowest resolution % % descriptions', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_id::VARCHAR			/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR			/* Curent geolevel name */, 
				't_rif40_'||LOWER(l_geography)||'_geometry'	/* Geometry table */,
				l_rows::VARCHAR					/* Rows updated */);
			re_analyze:=TRUE;
--
-- f) Level 2 (second lowest) resolution geolevel must be complete
--
		ELSIF c2_rec.geolevel_id = 2 AND c3_rec.total_desc != c3_rec.total_area_ids THEN
			PERFORM rif40_log_pkg.rif40_error(-10025, 'fix_null_geolevel_names',
				'Pass: 2f Geography % geolevel % lookup table: % has %/% incomplete description rows (%) - FIX ', 
				l_geography::VARCHAR				/* Geography */, 
				c2_rec.geolevel_name::VARCHAR			/* Geolevel name */, 
				c2_rec.lookup_table::VARCHAR			/* Geolevel lookup table */,
				c3_rec.total_desc::VARCHAR			/* Geolevel lookup table unique description rows */, 
				c3_rec.total_area_ids::VARCHAR			/* Geolevel lookup table unique name rows (AREA_IDs) */, 
				LOWER(c2_rec.lookup_desc_column)::VARCHAR 	/* Geolevel lookup table description column */);
--
-- g) Fix Level 3 (third lowest) resolution geolevel and so on if required
--
/*
CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
	SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.name
                  FROM ew2001_coa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.oa2001);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_coa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.oa2001)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

 */
		ELSIF c3_rec.total_desc != c3_rec.total_area_ids THEN
			sql_stmt:='DROP TABLE IF EXISTS temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS /* Lookup table update */'||E'\n'||
				E'\t'||'SELECT DISTINCT a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' /* source */, '||
					'a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */, b2.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||
					'||'' '||UPPER(c2_rec.geolevel_name)||'(''||COALESCE(a.'||quote_ident(LOWER(c2_rec.geolevel_name))||', ''UNK'')||'')'' AS '||
					quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||E'\n'||
				E'\t'||'  FROM '||quote_ident(LOWER(c6_rec.hierarchytable))||' /* Hierarchy table */ a'||E'\n'||
				E'\t'||'	LEFT OUTER JOIN '||quote_ident(LOWER(higher_c4_rec.lookup_table))||' b2 ON '||
					'(a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' = b2.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||')'||E'\n'||
				E'\t'||E'\t'||'/* highest resolution geolevel with complete descriptive names */'||E'\n'||
				E'\t'||' WHERE NOT EXISTS ('||E'\n'||
				E'\t'||E'\t'||'SELECT b1.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||E'\n'||
				E'\t'||E'\t'||'  FROM '||quote_ident(LOWER(c2_rec.lookup_table))||' b1'||E'\n'||
				E'\t'||E'\t'||' WHERE b1.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||' IS NOT NULL'||E'\n'||
				E'\t'||E'\t'||'   AND a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */ = '||
					'b1.'||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_pk ON'||
				' temp_fix_null_geolevel_names('||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='ANALYZE VERBOSE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='UPDATE /* f.1 */ '||quote_ident(LOWER(c2_rec.lookup_table))||' c'||E'\n'||
				'   SET name = (SELECT '||quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||' /* Replacement 1 */ '||E'\n'||
				'		 FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.'||quote_ident(LOWER(c2_rec.geolevel_name))||')'||E'\n'||
				' WHERE c.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||' IS NULL';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Check l_rows is NOT 0 i.e. no rows were updated
--
			IF l_rows = 0 THEN
				PERFORM rif40_sql_pkg.rif40_method4('SELECT * FROM temp_fix_null_geolevel_names LIMIT 100', 
					'Pass: 2f Update 1 Dump of temp_fix_null_geolevel_names');
				PERFORM rif40_log_pkg.rif40_error(-10028, 'fix_null_geolevel_names', 
					'Pass: 2f Update 1 Geography % fixed geolevel %: % resolution lookup table % no rows updated', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */,
					c2_rec.lookup_table::VARCHAR		/* Lookup table */);
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
					'Pass: 2f Geography % fixed geolevel %: % resolution lookup table % % descriptions', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
					c2_rec.lookup_table::VARCHAR		/* Lookup table */,
					l_rows::VARCHAR				/* Rows updated */);
			END IF;
			sql_stmt:='DROP TABLE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);

/*
CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
SELECT DISTINCT a.ladua2001 -* source *-, a.oa2001 -* target *-, b2.name||' OA2001('||COALESCE(a.oa2001, 'UNK')||')' AS oa2001_name
          FROM ew2001_geography -* Hierarchy table *- a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                -* highest resolution geolevel with complete descriptive names *-
         WHERE NOT EXISTS (
                SELECT b1.area_id
                  FROM rif40_partitions.p_rif40_geolevels_geometry_ew01_oa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.area_id);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE rif40_partitions.p_rif40_geolevels_geometry_ew01_oa2001 c
           SET name = (SELECT oa2001_name -* Replacement *- 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.area_id)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

 */
			sql_stmt:='CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS /* Geolevel geometry table update */'||E'\n'||
				E'\t'||'SELECT DISTINCT a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' /* source */, '||
					'a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */, b2.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||
					'||'' '||UPPER(c2_rec.geolevel_name)||'(''||COALESCE(a.'||quote_ident(LOWER(c2_rec.geolevel_name))||', ''UNK'')||'')'' AS '||
					quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||E'\n'||
				E'\t'||'  FROM '||quote_ident(LOWER(c6_rec.hierarchytable))||' /* Hierarchy table */ a'||E'\n'||
				E'\t'||'	LEFT OUTER JOIN '||quote_ident(LOWER(higher_c4_rec.lookup_table))||' b2 ON '||
					'(a.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||' = b2.'||quote_ident(LOWER(higher_c4_rec.geolevel_name))||')'||E'\n'||
				E'\t'||E'\t'||'/* highest resolution geolevel with complete descriptive names */'||E'\n'||
				E'\t'||' WHERE NOT EXISTS ('||E'\n'||
				E'\t'||E'\t'||'SELECT b1.area_id'||E'\n'||
				E'\t'||E'\t'||'  FROM rif40_partitions.'||quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))||' b1 /* Geometry table */'||E'\n'||
				E'\t'||E'\t'||' WHERE b1.'||COALESCE(quote_ident(LOWER(c2_rec.shapefile_desc_column)), 'name')||' IS NOT NULL'||E'\n'||
				E'\t'||E'\t'||'   AND a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' /* target */ = b1.area_id)';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.geolevel_name))||'_pk ON'||
				' temp_fix_null_geolevel_names('||quote_ident(LOWER(c2_rec.geolevel_name))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='ANALYZE VERBOSE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='UPDATE /* f.2 */ rif40_partitions.'||quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))||' c /* Geometry table */'||E'\n'||
				'   SET name = (SELECT '||quote_ident(LOWER(c2_rec.geolevel_name)||'_name')||' /* Replacement 2 */ '||E'\n'||
				'		 FROM temp_fix_null_geolevel_names a'||E'\n'||
				'		WHERE a.'||quote_ident(LOWER(c2_rec.geolevel_name))||' = c.area_id)'||E'\n'||
				' WHERE c.name IS NULL';
			l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Check l_rows is NOT 0 i.e. no rows were updated
--
			IF l_rows = 0 THEN
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'fix_null_geolevel_names', 
					'Pass: 2f Update 2 Geography % fixed geolevel %: % resolution geometry table % no rows updated', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */,
					quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))::VARCHAR /* Geometry table */);
				PERFORM rif40_sql_pkg.rif40_method4('SELECT * FROM temp_fix_null_geolevel_names LIMIT 100', 
					'Pass: 2f Update 2 Dump of temp_fix_null_geolevel_names');
			ELSE
				PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'fix_null_geolevel_names', 
					'Pass: 2f Geography % fixed geolevel %: % resolution geometry table % % descriptions', 
					l_geography::VARCHAR			/* Geography */, 
					c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
					c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
					quote_ident('p_rif40_geolevels_geometry_'||LOWER(l_geography)||'_'||LOWER(c2_rec.geolevel_name))::VARCHAR /* Geometry table */,
					l_rows::VARCHAR				/* Rows updated */);
			END IF;
--
			sql_stmt:='DROP TABLE temp_fix_null_geolevel_names';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Re-create unique index
--
			sql_stmt:='DROP INDEX IF EXISTS '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2');
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
			sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c2_rec.lookup_table)||'_uk2')||
				' ON '||quote_ident(LOWER(c2_rec.lookup_table))||'('||quote_ident(LOWER(c2_rec.lookup_desc_column))||')';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Unexpected logical condition
--
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-10029, 'fix_null_geolevel_names', 
				'Pass: 2 Geography % fixed geolevel %: % Unexpected logical condition in first loop; total_area_ids: %, total_desc: %, total_null_desc: %, total_uniq_desc: %', 
				l_geography::VARCHAR			/* Geography */, 
				c2_rec.geolevel_id::VARCHAR		/* Current geolevel ID (descending) */,
				c2_rec.geolevel_name::VARCHAR		/* Curent geolevel name */, 
				c3_rec.total_area_ids::VARCHAR,
				c3_rec.total_desc::VARCHAR,
				c3_rec.total_null_desc::VARCHAR,
				c3_rec.total_uniq_desc::VARCHAR);
		END IF;

--
--
-- Re-analyze lookup and shapefile tables
--
		IF re_analyze THEN
			sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c2_rec.lookup_table));
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
			sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c2_rec.shapefile_table));
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
		END IF;
	END LOOP;
--
-- Check geolevels were processed
--
	IF j = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-10049, 'fix_null_geolevel_names', 
			'No geolevels were processed in: t_rif40_geolevels for geography: %, expected geolevels: %',
			l_geography::VARCHAR 	/* Geography */,
			total_geolevel::VARCHAR	/* Expected geolevels */);
	END IF;
--
-- Re-create unique index
--
	sql_stmt:='DROP INDEX IF EXISTS '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry_uk2');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	sql_stmt:='CREATE UNIQUE INDEX '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry_uk2')||
		' ON '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||'(name)';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Re-analyze geometry tables
--
	sql_stmt:='ANALYZE VERBOSE '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry');
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);	
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names(VARCHAR) IS 'Function: 	fix_null_geolevel_names()
Parameters:	geography
Returns:	Nothing
Description:	Fix NULL geolevel names in geography geolevel geometry and lookup table data, re-analyze
		Fix non-unique names in lookup tables and geolevel geometry table
		Add unique index

Fix NULL geolevel names in lookup table data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
	SELECT DISTINCT a.ladua2001 /* source */, a.oa2001 /* target */, b2.name||'' OA2001(''||COALESCE(a.oa2001, ''UNK'')||'')'' AS oa2001_name
          FROM ew2001_geography /* Hierarchy table */ a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                /* highest resolution geolevel with complete descriptive names */
         WHERE NOT EXISTS (
                SELECT b1.name
                  FROM ew2001_coa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.oa2001);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_coa2001 c
           SET name = (SELECT oa2001_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.oa2001)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix NULL geolevel names in geography geolevel geometry data example SQL>

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS 
SELECT DISTINCT a.ladua2001 /* source */, a.oa2001 /* target */, b2.name||'' OA2001(''||COALESCE(a.oa2001, ''UNK'')||'')'' AS oa2001_name
          FROM ew2001_geography /* Hierarchy table */ a
                LEFT OUTER JOIN ew2001_ladua2001 b2 ON (a.ladua2001 = b2.ladua2001)
                /* highest resolution geolevel with complete descriptive names */
         WHERE NOT EXISTS (
                SELECT b1.area_id
                  FROM rif40_partitions.p_rif40_geolevels_geometry_ew01_oa2001 b1
                 WHERE b1.name IS NOT NULL
                   AND a.oa2001 /- target -/ = b1.area_id);

CREATE UNIQUE INDEX oa2001_pk ON temp_fix_null_geolevel_names(oa2001);

ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE rif40_partitions.p_rif40_geolevels_geometry_ew01_oa2001 c
           SET name = (SELECT oa2001_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.oa2001 = c.area_id)
         WHERE c.name IS NULL;

DROP TABLE temp_fix_null_geolevel_names;

ANALYZE VERBOSE ew2001_coa2001;

Fix non-unique names in lookup tables and geolevel geometry table example SQL

DROP TABLE IF EXISTS temp_fix_null_geolevel_names;

CREATE TEMPORARY TABLE temp_fix_null_geolevel_names AS
WITH a AS (
	SELECT name, COUNT(*) AS total
	  FROM ew2001_ward2001
	 GROUP BY name
	HAVING COUNT(*) > 1
)
SELECT DISTINCT b.ward2001, a.name, c.ladua2001, a.name||''(''||c.ladua2001||'')'' AS new_name
  FROM a, ew2001_ward2001 b, ew2001_geography c
 WHERE a.name = b.name
   AND c.ward2001 = b.ward2001;

CREATE UNIQUE INDEX ward2001_pk ON temp_fix_null_geolevel_names(ward2001);
CREATE UNIQUE INDEX ward2001_uk ON temp_fix_null_geolevel_names(new_name);
ANALYZE VERBOSE temp_fix_null_geolevel_names;

UPDATE ew2001_ward2001 c
           SET name = (SELECT new_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.ward2001)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);

UPDATE rif40_partitions.p_rif40_geolevels_geometry_ew01_ward2001 c
           SET name = (SELECT new_name /* Replacement */ 
                         FROM temp_fix_null_geolevel_names a
                        WHERE a.ward2001 = c.area_id)
         WHERE c.ward2001 IN (SELECT ward2001
			  FROM temp_fix_null_geolevel_names);
';

--
-- Eof