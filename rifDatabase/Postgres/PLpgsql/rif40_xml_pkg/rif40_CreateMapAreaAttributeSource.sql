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
--     				  rif40_CreateMapAreaAttributeSource
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
-- rif40_CreateMapAreaAttributeSource: 		51600 to 51799
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

CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	c4getallatt4theme 	VARCHAR,
	l_geography 		VARCHAR,
	l_geolevel_select	VARCHAR,
	l_theme			rif40_xml_pkg.rif40_geolevelAttributeTheme,
	l_attribute_source	VARCHAR,
	l_attribute_name_array	VARCHAR[]	DEFAULT NULL)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_CreateMapAreaAttributeSource()
Parameters:	Temporary table name (same as REFCURSOR), Geography, <geolevel select>, theme (enum: rif40_xml_pkg.rif40_geolevelAttributeTheme), attribute source, 
 		attribute name array [Default NULL - All attributes]
Returns:	Temporary table name
Description:	Create temporary table with all values for attributes source, attribute names, geography and geolevel select

Warnings: 

a) This function can be slow as if uses rif40_num_denom, takes 398mS on my laptop to fetch rows to RI40_NUM_DEMON based
   (health and population) themes
b) This function is VOLATILE. Calling it twice with the same parameters will not necessarily return the same set of rows in the same order.
   This is because databases to not guarantee data block access order, even without parallelisation.
   To minimise the volatility:
	i)  Set the row limit to NULL (all rows). This has a slight performance cost.
	ii) Define a sort order. This has a severe performance cost for large tables.

Checks:

- User must be rif40 or have rif_user or rif_manager role
- Test geography
- Test <geolevel select> exists
- Check attribute exists, build SQL injection proof select list
- Process themes
- If attribute name array is used, then all must be found
- Check if GID, gid_rowindex exist [they do not have to, but the query runs quicker if they do]
- Geometry tables must have gid and gid_rowindex

Examples:

Example 1) Full SELECT of sahsuland_pop
	   This performs well on a server, takes about 3 seconds on a laptop

SELECT *
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
                'c4getallatt4theme_3' /- Must be unique with a TX -/,
                'SAHSU', 'LEVEL2', 'population', 'sahsuland_pop', NULL /- All attributes -/);
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50807] Geography: SAHSU, geolevel select: LEVEL2, theme: population; SQ
L fetch returned 6 attribute names, took: 00:00:00.219.
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [50212] Neither gid or gid_rowindex columns are present in attrribute source table: sahsu
land_pop
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51219] c4getallatt4theme SQL>
WITH a AS (
        SELECT a.level2 /- Map <geolevel select> to area_id -/ AS area_id,
       g.gid,
       LPAD(g.gid::Text, 10, '0'::Text)||'_'||LPAD(ROW_NUMBER() OVER(PARTITION BY level2 /- Use default table order -/)::Text, 10, '0'::Text) AS gid_rowindex,
       a.year /- ordinal_position: 1 -/,
       a.age_sex_group /- ordinal_position: 2 -/,
       a.level1 /- ordinal_position: 3 -/,
       a.level3 /- ordinal_position: 5 -/,
       a.level4 /- ordinal_position: 6 -/,
       a.total /- ordinal_position: 7 -/
          FROM sahsuland_pop a,
               t_rif40_sahsu_geometry g
         WHERE g.geography     = $1
           AND g.geolevel_name = $2 /- Partition elimination -/
           AND g.area_id       = a.level2 /- Link gid -/
) /- Force CTE to be executed entirely -/
SELECT *
  FROM a
 ORDER BY 3 /- gid_rowindex -/ OFFSET $3;
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51221] Cursor: c4getallatt4theme_3, geography: SAHSU, geolevel select: LEVEL2, theme: po
pulation, attribute names: [], source: sahsuland_pop; offset: 0, row limit: , SQL parse took: 00:00:00.25.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

\timing
Timing is on.
FETCH FORWARD 5 IN c4getallatt4theme_3 /- 1.3 seconds with no row limit, no sort list, no gid/gid_rowindex columns built in -/;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000000001 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000002 | 1989 |           101 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000003 | 1989 |           102 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000004 | 1989 |           103 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000005 | 1989 |           104 | 01     | 01.001.000100 | 01.001.000100.1 |    34
(5 rows)

Time: 1308.264 ms
MOVE ABSOLUTE 1000 IN c4getallatt4theme_3 /- move to row 1000 -/;
MOVE 1
Time: 1.314 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000001001 | 1994 |           210 | 01     | 01.001.000200 | 01.001.000200.1 |   390
 01.001  |   1 | 0000000001_0000001002 | 1994 |           211 | 01     | 01.001.000200 | 01.001.000200.1 |   356
 01.001  |   1 | 0000000001_0000001003 | 1994 |           212 | 01     | 01.001.000200 | 01.001.000200.1 |   392
 01.001  |   1 | 0000000001_0000001004 | 1994 |           213 | 01     | 01.001.000200 | 01.001.000200.1 |   388
 01.001  |   1 | 0000000001_0000001005 | 1994 |           214 | 01     | 01.001.000200 | 01.001.000200.1 |   286
(5 rows)

Time: 1.330 ms
MOVE ABSOLUTE 10000 IN c4getallatt4theme_3 /- move to row 10000 -/;
MOVE 1
Time: 1.518 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

Time: 1.314 ms
MOVE ABSOLUTE 432958 IN c4getallatt4theme_3 /- move to row 432958 - two from the end -/;
MOVE 1
Time: 14.297 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.018  |  17 | 0000000017_0000005983 | 1996 |           220 | 01     | 01.018.019500 | 01.018.019500.3 |   388
 01.018  |  17 | 0000000017_0000005984 | 1996 |           221 | 01     | 01.018.019500 | 01.018.019500.3 |   402
(2 rows)

SELECT *
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
                'c4getallatt4theme_1' /- Must be unique with a TX -/,
                'SAHSU', 'LEVEL4', 'covariate', 'sahsuland_covariates_level4', ARRAY['SES', 'year']);
psql:alter_scripts/v4_0_alter_1.sql:506: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50807] Geography: SAHSU, geolevel select: LEVEL4, theme: covariate; SQL
 fetch returned 2 attribute names, took: 00:00:00.016.
psql:alter_scripts/v4_0_alter_1.sql:506: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [50212] Neither gid or gid_rowindex columns are present in attrribute source table: sahsu
land_covariates_level4
psql:alter_scripts/v4_0_alter_1.sql:506: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51219] c4getallatt4theme SQL>
WITH a AS (
        SELECT a.level4 /- Map <geolevel select> to area_id -/ AS area_id,
       g.gid,
       LPAD(g.gid::Text, 10, '0'::Text)||'_'||LPAD(ROW_NUMBER() OVER(PARTITION BY level4 ORDER BY        a.ses /- ordinal_position: 3 -/,
       a.year /- ordinal_position: 1 -/)::Text, 10, '0'::Text) AS gid_rowindex,
       a.ses /- ordinal_position: 3 -/,
       a.year /- ordinal_position: 1 -/
          FROM sahsuland_covariates_level4 a,
               t_rif40_sahsu_geometry g
         WHERE g.geography     = $1
           AND g.geolevel_name = $2 /- Partition elimination -/
           AND g.area_id       = a.level4 /- Link gid -/
) /- Force CTE to be executed entirely -/
SELECT *
  FROM a
 ORDER BY 3 /- gid_rowindex -/ OFFSET $3 LIMIT $4;
psql:alter_scripts/v4_0_alter_1.sql:506: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51221] Cursor: c4getallatt4theme_1, geography: SAHSU, geolevel select: LEVEL4, theme: co
variate, attribute names: [SES,year], source: sahsuland_covariates_level4; offset: 0, row limit: 1000, SQL parse took: 00:00:00.047.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_1
(1 row)

FETCH FORWARD 5 IN c4getallatt4theme_1;
     area_id     | gid |     gid_rowindex      | ses | year
-----------------+-----+-----------------------+-----+------
 01.001.000100.1 |   1 | 0000000001_0000000001 |   4 | 1989
 01.001.000100.1 |   1 | 0000000001_0000000002 |   4 | 1990
 01.001.000100.1 |   1 | 0000000001_0000000003 |   4 | 1991
 01.001.000100.1 |   1 | 0000000001_0000000004 |   4 | 1992
 01.001.000100.1 |   1 | 0000000001_0000000005 |   4 | 1993
(5 rows)

Debug:

DEBUG1 prints SQL and diagnostics
DEBUG2 does an EXPLAIN PLAN

Timings: sahsuland_population (with EXPLAIN PLAN)

REF_CURSOR: NO limit, force sort of all attributes - 12 seconds, FETCH 5 rows - 4 seconds
REF_CURSOR: 1000 rows limit, force sort of all attributes - 12 seconds, FETCH 5 rows - 0.2 seconds
REF_CURSOR: 1000 rows limit, all attributes default (no sort in GID_ROWINDEX create) - 4 seconds, FETCH 5 rows - 0.2 seconds
REF_CURSOR: NO limit, all attributes default (no sort in GID_ROWINDEX create) - 4????? seconds, FETCH 5 rows - 0.2 seconds

For timings without EXPLAIN PLAN, rif40_CreateMapAreaAttributeSource() executes very quickly, first fetch is delayed. This is 
because EXPLAIN PLAN creates a temporary table.
 */
DECLARE
	c1getallatt4theme CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography     = l_geography;
	c2getallatt4theme CURSOR(l_geography VARCHAR, l_geolevel_select VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel_select;
	c3getallatt4theme CURSOR(
			l_geography 		VARCHAR, 
			l_geolevel_select 	VARCHAR, 
			l_theme 		rif40_xml_pkg.rif40_geolevelAttributeTheme, 
			l_attribute_name_array 	VARCHAR[]) FOR
		SELECT * 
		  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(l_geography, l_geolevel_select, l_theme, l_attribute_name_array);
	c5getallatt4theme CURSOR(l_c4getallatt4theme VARCHAR) FOR
		SELECT *
		  FROM pg_cursors
		 WHERE name     = l_c4getallatt4theme;
	c6_6getallatt4theme CURSOR(l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) FOR
		SELECT column_name 
		  FROM information_schema.columns 
		 WHERE table_schema = l_schema
		   AND table_name   = l_table 
	           AND column_name  = l_column;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
	c6a_rec RECORD;
	c6b_rec RECORD;
--
	explain_text 			VARCHAR;
	sql_stmt			VARCHAR;
	select_list			VARCHAR;
	invalid_attribute_source	INTEGER:=0;
	attribute_name_list		VARCHAR[];
	ordinal_position_list		INTEGER[];
	sorted_attribute_name_list	VARCHAR[];
	sorted_ordinal_position_list	INTEGER[];
--
	stp 				TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp 				TIMESTAMP WITH TIME ZONE;
	took 				INTERVAL;
--
	error_message 			VARCHAR;
	v_detail 			VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- User must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-51200, 'rif40_CreateMapAreaAttributeSource', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;

--
-- Check if cursor exists
--
	OPEN c5getallatt4theme(LOWER(quote_ident(c4getallatt4theme::Text)));
	FETCH c5getallatt4theme INTO c5_rec;
	CLOSE c5getallatt4theme;
	IF c5_rec.name IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51201, 'rif40_CreateMapAreaAttributeSource', 'Cursor: % is use, created: %, SQL>'||E'\n'||'%;',
			c5_rec.name::VARCHAR		/* Cursor name */,
			c5_rec.creation_time::VARCHAR	/* Created */,
			c5_rec.statement::VARCHAR	/* SQL */);
	END IF;

--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51202, 'rif40_CreateMapAreaAttributeSource', 'NULL geography parameter');
	END IF;	
--
	OPEN c1getallatt4theme(l_geography);
	FETCH c1getallatt4theme INTO c1_rec;
	CLOSE c1getallatt4theme;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51203, 'rif40_CreateMapAreaAttributeSource', 'geography: % not found', 
			l_geography::VARCHAR		/* Geography */);
	END IF;	
--
-- Test <geolevel select> exists
--
	OPEN c2getallatt4theme(l_geography, l_geolevel_select);
	FETCH c2getallatt4theme INTO c2_rec;
	CLOSE c2getallatt4theme;
--
	IF c2_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51204, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %: not found', 
			l_geography::VARCHAR		/* Geography */, 
			l_geolevel_select::VARCHAR	/* geolevel select */);
	END IF;	
--
-- Check attribute exists
--
	FOR c3_rec IN c3getallatt4theme(l_geography, l_geolevel_select, l_theme, l_attribute_name_array) LOOP
		IF c3_rec.attribute_source = LOWER(l_attribute_source) THEN
			IF attribute_name_list IS NULL THEN
				attribute_name_list[1]:=c3_rec.attribute_name;
				ordinal_position_list[1]:=c3_rec.ordinal_position;
			ELSE
				attribute_name_list[array_length(attribute_name_list, 1) +1]:=c3_rec.attribute_name;
				ordinal_position_list[array_length(ordinal_position_list, 1) +1]:=c3_rec.ordinal_position;
			END IF;
		ELSE
			invalid_attribute_source:=invalid_attribute_source+1;
		END IF;
	END LOOP;

--
-- Sort if the attribute name array is set, otherwise except the default (ordinal_position order)
--
	IF l_attribute_name_array IS NULL THEN
		sorted_attribute_name_list:=attribute_name_list;
		sorted_ordinal_position_list:=ordinal_position_list;
	ELSE
		FOR i IN 1 .. array_length(l_attribute_name_array, 1) LOOP /* Orignal sort out as specified to function */
			FOR j IN 1 .. array_length(attribute_name_list, 1) LOOP
				IF attribute_name_list[j] = LOWER(l_attribute_name_array[i]) THEN /* Found */
					sorted_attribute_name_list[i]:=attribute_name_list[j];
					sorted_ordinal_position_list[i]:=ordinal_position_list[j];
				END IF;
			END LOOP;
		END LOOP;
	END IF;
--
-- Check all attributes present
--
	IF sorted_attribute_name_list IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51205, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %, <attribute source>: %s; sorted attribute list is null', 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_select::VARCHAR		/* Geolevel select */,
			l_attribute_source::VARCHAR		/* Attribute source */);
	ELSIF array_length(attribute_name_list, 1) != array_length(sorted_attribute_name_list, 1) THEN
		PERFORM rif40_log_pkg.rif40_error(-51206, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %, <attribute source>: %s; sorted attribute list is not the same length as the original', 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_select::VARCHAR		/* Geolevel select */,
			l_attribute_source::VARCHAR		/* Attribute source */,
			array_length(sorted_attribute_name_list, 1)::VARCHAR	/* Sorted attribute list */,
			array_length(attribute_name_list, 1)::VARCHAR	/* Orignal attribute list  */);
	END IF;

--
-- Build SQL injection proof select list
--
	FOR i IN 1 .. array_length(sorted_attribute_name_list, 1) LOOP
		IF select_list IS NULL THEN
			select_list:='       a.'||quote_ident(sorted_attribute_name_list[i])||
				' /* ordinal_position: '||sorted_ordinal_position_list[i]||' */';
		ELSE
			select_list:=select_list||','||E'\n'||'       a.'||quote_ident(sorted_attribute_name_list[i])||
				' /* ordinal_position: '||sorted_ordinal_position_list[i]||' */';
		END IF;
	END LOOP;

--
-- No select list; either no attributes or an invalid attribute source
-- If attribute names in the list are invalid then an exception will have been raised by cursor c3getallatt4theme 
-- [the function rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme()]
--
	IF select_list IS NULL AND invalid_attribute_source > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-51207, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %, <attribute source>: %s; no attributes found, % of % <attribute source> were invalid', 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_select::VARCHAR		/* Geolevel select */,
			l_attribute_source::VARCHAR		/* Attribute source */,
			invalid_attribute_source::VARCHAR	/* Invalid attribute sources */,
			array_length(l_attribute_name_array, 1)::VARCHAR	/* Total attributes */);
	ELSIF select_list IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51208, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %, <attribute source>: %s; no attributes found', 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_select::VARCHAR		/* Geolevel select */,
			l_attribute_source::VARCHAR		/* Attribute source */);
	END IF;	

--
-- Check if GID, gid_rowindex exist [they do not have to, but the query runs quicker if they do]
--
	IF l_theme IN ('extract', 'results') THEN
		OPEN c6_6getallatt4theme('rif_studies', lower(l_attribute_source), 'gid');
	ELSE
		OPEN c6_6getallatt4theme('rif40', lower(l_attribute_source), 'gid');
	END IF;
	FETCH c6_6getallatt4theme INTO c6a_rec;
	CLOSE c6_6getallatt4theme;
	IF l_theme IN ('extract', 'results') THEN
		OPEN c6_6getallatt4theme('rif_studies', lower(l_attribute_source), 'gid_rowindex');
	ELSE
		OPEN c6_6getallatt4theme('rif40', lower(l_attribute_source), 'gid_rowindex');
	END IF;
	FETCH c6_6getallatt4theme INTO c6b_rec;
	CLOSE c6_6getallatt4theme;
	IF c6a_rec.column_name = 'gid' AND c6b_rec.column_name = 'gid_rowindex' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[50209] Both gid and gid_rowindex columns are present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	ELSIF c6a_rec.column_name = 'gid' AND c6b_rec.column_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[50210] Only the gid column is present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	ELSIF c6a_rec.column_name IS NULL AND c6b_rec.column_name = 'gid_rowindex' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[50211] Only the gid_rowindex column is present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[50212] Neither gid or gid_rowindex columns are present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	END IF;
--
-- Geometry tables must have gid and gid_rowindex
--
	IF l_theme = 'geometry' AND (c6a_rec.column_name IS NULL OR c6b_rec.column_name IS NULL) THEN
		PERFORM rif40_log_pkg.rif40_error(-51213, 'rif40_CreateMapAreaAttributeSource', 
			'Theme: % does not have gid or gid_rowindex column',
			l_theme::VARCHAR);
	END IF;
--
-- Process themes
--
-- Gid is derived from the GID in the geometry table, gid_rowindex is calculated on the fly from the selection order
--
-- gid and gid_rowindex must be joined for health and population as there are multiple geolevels in the table, so gid would have multiple meanings
--
	IF l_theme IN ('covariate', 'health', 'population') THEN
		IF l_attribute_name_array IS NULL THEN
			select_list:='a.'||quote_ident(LOWER(l_geolevel_select))||' /* Map <geolevel select> to area_id */ AS area_id, '||E'\n'||
				  '       g.gid, '||E'\n'||
			          '       LPAD(g.gid::Text, 10, ''0''::Text)||''_''||'||
				  		'LPAD(ROW_NUMBER() OVER(PARTITION BY a.'||quote_ident(LOWER(l_geolevel_select))||
						' /* Use default table order */)::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			          select_list;
		ELSE /* Sort order specified */
			select_list:='a.'||quote_ident(LOWER(l_geolevel_select))||' /* Map <geolevel select> to area_id */ AS area_id, '||E'\n'||
				  '       g.gid, '||E'\n'||
			          '       LPAD(g.gid::Text, 10, ''0''::Text)||''_''||'||
				  		'LPAD(ROW_NUMBER() OVER(PARTITION BY a.'||quote_ident(LOWER(l_geolevel_select))||
						' ORDER BY '||select_list||')::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			          select_list;
		END IF;
--
-- gid and gid_rowindex will be added in later patch
--
	ELSIF l_theme = 'extract' THEN
		IF l_attribute_name_array IS NULL THEN
			select_list:='a.area_id, '||E'\n'||
				  '       g.gid, '||E'\n'||
			          '       LPAD(g.gid::Text, 10, ''0''::Text)||''_''||'||
						'LPAD(ROW_NUMBER() OVER(PARTITION BY a.area_id'||
						' /* Use default table order */)::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			          select_list;
		ELSE /* Sort order specified */
			select_list:='a.area_id, '||E'\n'||
				  '       g.gid, '||E'\n'||
			          '       LPAD(g.gid::Text, 10, ''0''::Text)||''_''||'||
						'LPAD(ROW_NUMBER() OVER(PARTITION BY a.area_id'||
						' ORDER BY '||select_list||')::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			          select_list;
		END IF;
--
-- gid and gid_rowindex; area_id for disease maps will be added in later patch
-- Not available for risk analysis
--
	ELSIF l_theme = 'results' THEN
--
-- This is now done at map (results) creation
--
		IF c6a_rec.column_name = 'gid' AND c6b_rec.column_name = 'gid_rowindex' THEN
			select_list:='a.area_id,'||E'\n'||
			          select_list;
--
-- Other is risk analysis; use band_id as a substitute for gid
--
		ELSIF l_attribute_name_array IS NULL THEN
			select_list:='       LPAD(g.band_id::Text, 10, ''0''::Text)||''_''||'||
						'LPAD(ROW_NUMBER() OVER(PARTITION BY a.band_id'||
						' /- Use default table order -/)::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			          select_list;
		ELSE /* Sort order specified */
			select_list:='       LPAD(g.band_id::Text, 10, ''0''::Text)||''_''||'||
						'LPAD(ROW_NUMBER() OVER(PARTITION BY a.band_id'||
						' ORDER BY '||select_list||')::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			          select_list;
		END IF;
--
-- Geometry tables must have gid and gid_rowindex
--
	ELSIF l_theme = 'geometry' THEN
		select_list:='a.area_id, '||E'\n'||
			  '       a.gid, '||E'\n'||
			  '       a.gid_rowindex, '||E'\n'||
		          select_list;
	ELSE	
--	
-- This may mean the theme is not supported yet...
--
		PERFORM rif40_log_pkg.rif40_error(-51214, 'rif40_CreateMapAreaAttributeSource', 
			'Invalid theme: %',
			l_theme::VARCHAR);
	END IF;
--
-- Build SELECT statement
--
	IF l_theme IN ('covariate', 'health', 'population') THEN
		sql_stmt:='WITH a AS ('||E'\n'||
			  E'\t'||'SELECT '||select_list||E'\n'||
			  E'\t'||'  FROM '||quote_ident(lower(l_attribute_source))||' a,'||E'\n'||
			  E'\t'||'       '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||' g'||E'\n'||
			  E'\t'||' WHERE g.geography     = $1'||E'\n'||
			  E'\t'||'   AND g.geolevel_name = $2 /* Partition elimination */'||E'\n'||
			  E'\t'||'   AND g.area_id       = a.'||quote_ident(LOWER(l_geolevel_select))||' /* Link gid */'||E'\n';
	ELSIF l_theme= 'extract' THEN
		sql_stmt:='WITH a AS ('||E'\n'||
			  E'\t'||'SELECT '||select_list||E'\n'||
			  E'\t'||'  FROM rif_studies.'||quote_ident(lower(l_attribute_source))||' /* Needs path adding */'||' a,'||E'\n'||
			  E'\t'||'       '||quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')||' g'||E'\n'||
			  E'\t'||' WHERE g.geography     = $1'||E'\n'||
			  E'\t'||'   AND g.geolevel_name = $2 /* Partition elimination */'||E'\n'||
			  E'\t'||'   AND g.area_id       = a.area_id /* Link gid */'||E'\n';
	ELSIF l_theme = 'results' THEN
		sql_stmt:='WITH a AS ('||E'\n'||
			  E'\t'||'SELECT '||select_list||E'\n'||
			  E'\t'||'  FROM rif_studies.'||quote_ident(lower(l_attribute_source))||' /* Needs path adding */'||
				' a /* No gid link yet, only possible for disease maps */'||E'\n';
	ELSIF l_theme = 'geometry' THEN
		sql_stmt:='WITH a AS ('||E'\n'||
			  E'\t'||'SELECT '||select_list||E'\n'||
			  E'\t'||'  FROM '||quote_ident(lower(l_attribute_source))||' a'||E'\n'||
			  E'\t'||' WHERE a.geography     = $1'||E'\n'||
			  E'\t'||'   AND a.geolevel_name = $2 /* Partition elimination */'||E'\n';
	END IF;	
--
	sql_stmt:=sql_stmt||') /* Force CTE to be executed entirely */'||E'\n'||
			  'SELECT *'||E'\n'||
			  '  FROM a'||E'\n'||
			  ' ORDER BY 3 /* gid_rowindex */';
				
--
-- As function returns a REFCURSOR it only parses and does NOT execute
--
--
-- If DEBUG2 is enabled, the do an EXPLAIN PLAN
-- As EXPLAIN PLAN returns the plan as the output rows; the actual is placed in a temporary and extracted after the EXPLAIN PLAN
--
-- Begin execution block to trap parse errors
--
-- EXPLAIN PLAN version
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_CreateMapAreaAttributeSource', 'DEBUG2') THEN
		BEGIN
--
-- Create temporary table
--
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource', '[51215] c4getallatt4theme EXPLAIN SQL> '||E'\n'||'%;', sql_stmt::VARCHAR); 
			sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE '||LOWER(quote_ident(c4getallatt4theme::Text))||E'\n'||'AS'||E'\n'||sql_stmt;
--
-- Create results temporary table, extract explain plan  using _rif40_CreateMapAreaAttributeSource_explain_ddl() helper function.
-- This ensures the EXPLAIN PLAN output is a field called explain_line 
--
			sql_stmt:='SELECT explain_line FROM rif40_xml_pkg._rif40_CreateMapAreaAttributeSource_explain_ddl('||quote_literal(sql_stmt)||', $1, $2)';
			FOR c4_rec IN EXECUTE sql_stmt USING l_geography, l_geolevel_select LOOP
				IF explain_text IS NULL THEN
					explain_text:=c4_rec.explain_line;
				ELSE
					explain_text:=explain_text||E'\n'||c4_rec.explain_line;
				END IF;
			END LOOP;
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_CreateMapAreaAttributeSource() caught: '||E'\n'||
					SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
				RAISE INFO '51216: %', error_message;
--
				RAISE;
		END;
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_CreateMapAreaAttributeSource', 
			'[51217] c4getallatt4theme EXPLAIN PLAN.'||E'\n'||'%', explain_text::VARCHAR);	
	ELSE
--
-- Non EXPLAIN PLAN version
--
		BEGIN
--
-- Create temporary table
--
			sql_stmt:='CREATE TEMPORARY TABLE '||LOWER(quote_ident(c4getallatt4theme::Text))||E'\n'||'AS'||E'\n'||sql_stmt;
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource', 
				'[51219] c4getallatt4theme SQL> '||E'\n'||'%;', sql_stmt::VARCHAR); 
			EXECUTE sql_stmt USING l_geography, l_geolevel_select;
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_CreateMapAreaAttributeSource() caught: '||E'\n'||
					SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
				RAISE INFO '51220: %', error_message;
--
				RAISE;
		END;
	END IF;

--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource', 
		'[51221] Cursor: %, geography: %, geolevel select: %, theme: %, attribute names: [%], source: %, SQL parse took: %.', 			
		LOWER(quote_ident(c4getallatt4theme::Text))::VARCHAR	/* Cursor name */, 
		l_geography::VARCHAR					/* Geography */, 
		l_geolevel_select::VARCHAR				/* Geolevel select */, 
		l_theme::VARCHAR					/* Theme */, 
		array_to_string(l_attribute_name_array, ',')::VARCHAR	/* attribute names */, 
		l_attribute_source::VARCHAR				/* attribute source table */, 
		took::VARCHAR						/* Time taken */);
--
	RETURN c4getallatt4theme;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(VARCHAR, VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme, VARCHAR, VARCHAR[]) IS 'Function: 	rif40_CreateMapAreaAttributeSource()
Parameters:	Temporary table name (same as REFCURSOR), Geography, <geolevel select>, theme (enum: rif40_xml_pkg.rif40_geolevelAttributeTheme), attribute source, 
 		attribute name array [Default NULL - All attributes]
Returns:	Temporary table name
Description:	Create temporary table with all values for attributes source, attribute names, geography and geolevel select

Warnings: 

a) This function can be slow as if uses rif40_num_denom, takes 398mS on my laptop to fetch rows to RI40_NUM_DEMON based
   (health and population) themes
b) This function is VOLATILE. Calling it twice with the same parameters will not necessarily return the same set of rows in the same order.
   This is because databases to not guarantee data block access order, even without parallelisation.
   To minimise the volatility:
	i)  Set the row limit to NULL (all rows). This has a slight performance cost.
	ii) Define a sort order. This has a severe performance cost for large tables.

Examples:

Example 1) Full SELECT of sahsuland_pop
	   This performs well on a server, takes about 3 seconds on a laptop

SELECT *
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
                ''c4getallatt4theme_3'' /* Must be unique with a TX */,
                ''SAHSU'', ''LEVEL2'', ''population'', ''sahsuland_pop'', NULL /* All attributes */, 0 /* No offset */, NULL /* No row limit */);
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50807] Geography: SAHSU, geolevel select: LEVEL2, theme: population; S
L fetch returned 6 attribute names, took: 00:00:00.219.
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [50212] Neither gid or gid_rowindex columns are present in attrribute source table: sahs
land_pop
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51219] c4getallatt4theme SQL>
WITH a AS (
        SELECT a.level2 /* Map <geolevel select> to area_id */ AS area_id,
       g.gid,
       LPAD(g.gid::Text, 10, ''0''::Text)||''_''||LPAD(ROW_NUMBER() OVER(PARTITION BY level2 /* Use default table order */)::Text, 10, ''0''::Text) AS gid_rowindex,
       a.year /* ordinal_position: 1 */,
       a.age_sex_group /* ordinal_position: 2 */,
       a.level1 /* ordinal_position: 3 */,
       a.level3 /* ordinal_position: 5 */,
       a.level4 /* ordinal_position: 6 */,
       a.total /* ordinal_position: 7 */
          FROM sahsuland_pop a,
               t_rif40_sahsu_geometry g
         WHERE g.geography     = $1
           AND g.geolevel_name = $2 /* Partition elimination */
           AND g.area_id       = a.level2 /* Link gid */
) /* Force CTE to be executed entirely */
SELECT *
  FROM a
 ORDER BY 3 /* gid_rowindex */ OFFSET $3;
psql:alter_scripts/v4_0_alter_1.sql:542: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51221] Cursor: c4getallatt4theme_3, geography: SAHSU, geolevel select: LEVEL2, theme: p
pulation, attribute names: [], source: sahsuland_pop; offset: 0, row limit: , SQL parse took: 00:00:00.25.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

\timing
Timing is on.
FETCH FORWARD 5 IN c4getallatt4theme_3 /* 1.3 seconds with no row limit, no sort list, no gid/gid_rowindex columns built in */;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000000001 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000002 | 1989 |           101 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000003 | 1989 |           102 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000004 | 1989 |           103 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000005 | 1989 |           104 | 01     | 01.001.000100 | 01.001.000100.1 |    34
(5 rows)

Time: 1308.264 ms
MOVE ABSOLUTE 1000 IN c4getallatt4theme_3 /* move to row 1000 */;
MOVE 1
Time: 1.314 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000001001 | 1994 |           210 | 01     | 01.001.000200 | 01.001.000200.1 |   390
 01.001  |   1 | 0000000001_0000001002 | 1994 |           211 | 01     | 01.001.000200 | 01.001.000200.1 |   356
 01.001  |   1 | 0000000001_0000001003 | 1994 |           212 | 01     | 01.001.000200 | 01.001.000200.1 |   392
 01.001  |   1 | 0000000001_0000001004 | 1994 |           213 | 01     | 01.001.000200 | 01.001.000200.1 |   388
 01.001  |   1 | 0000000001_0000001005 | 1994 |           214 | 01     | 01.001.000200 | 01.001.000200.1 |   286
(5 rows)

Time: 1.330 ms
MOVE ABSOLUTE 10000 IN c4getallatt4theme_3 /* move to row 10000 */;
MOVE 1
Time: 1.518 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

Time: 1.314 ms
MOVE ABSOLUTE 432958 IN c4getallatt4theme_3 /* move to row 432958 - two from the end */;
MOVE 1
Time: 14.297 ms
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.018  |  17 | 0000000017_0000005983 | 1996 |           220 | 01     | 01.018.019500 | 01.018.019500.3 |   388
 01.018  |  17 | 0000000017_0000005984 | 1996 |           221 | 01     | 01.018.019500 | 01.018.019500.3 |   402
(2 rows)

Time: 1.301 ms

Example 2) Covariate theme; specified columns (forcing re-sort); otherwise defaults

FETCH FORWARD 5 IN c4getallatt4theme_1;
     area_id     | gid |     gid_rowindex      | ses | year
-----------------+-----+-----------------------+-----+------
 01.001.000100.1 |   1 | 0000000001_0000000001 |   4 | 1989
 01.001.000100.1 |   1 | 0000000001_0000000002 |   4 | 1990
 01.001.000100.1 |   1 | 0000000001_0000000003 |   4 | 1991
 01.001.000100.1 |   1 | 0000000001_0000000004 |   4 | 1992
 01.001.000100.1 |   1 | 0000000001_0000000005 |   4 | 1993
(5 rows)

Debug:

DEBUG1 prints SQL and diagnostics
DEBUG2 does an EXPLAIN PLAN

Timings: sahsuland_population (with EXPLAIN PLAN)

REF_CURSOR: NO limit, force sort of all attributes - 12 seconds, FETCH 5 rows - 4 seconds
REF_CURSOR: 1000 rows limit, force sort of all attributes - 12 seconds, FETCH 5 rows - 0.2 seconds
REF_CURSOR: 1000 rows limit, all attributes default (no sort in GID_ROWINDEX create) - 4 seconds, FETCH 5 rows - 0.2 seconds
REF_CURSOR: NO limit, all attributes default (no sort in GID_ROWINDEX create) - 4????? seconds, FETCH 5 rows - 0.2 seconds

For timings without EXPLAIN PLAN, rif40_CreateMapAreaAttributeSource() executes very quickly, first fetch is delayed. This is 
because EXPLAIN PLAN creates a temporary table.';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	VARCHAR, VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme, VARCHAR, VARCHAR[]) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	VARCHAR, VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme, VARCHAR, VARCHAR[]) TO rif_user;

CREATE OR REPLACE FUNCTION rif40_xml_pkg._rif40_CreateMapAreaAttributeSource_explain_ddl(
	sql_stmt VARCHAR, l_geography VARCHAR, l_geolevel_select VARCHAR)
RETURNS TABLE(explain_line	TEXT)
SECURITY INVOKER
AS $func$
/*
Function: 	_rif40_CreateMapAreaAttributeSource_explain_ddl()
Parameters:	SQL statement, geography, geolevel select
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column. 
		Supports EXPLAIN and EXPLAIN ANALYZE for rif40_geolevelAttributeTheme() ONLY.
 */
BEGIN
--
-- Must be rifupg34, rif40 or have rif_user or rif_manager role
--
	IF USER != 'rifupg34' AND NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-51299, 'rif40_CreateMapAreaAttributeSource_explain_ddl', 'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR);
	END IF;
--
	RETURN QUERY EXECUTE sql_stmt USING l_geography, l_geolevel_select;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg._rif40_CreateMapAreaAttributeSource_explain_ddl(VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	rif40_CreateMapAreaAttributeSource_explain_ddl()
Parameters:	SQL statement, geography, geolevel select
Returns: 	TABLE of explain_line
Description:	Coerce EXPLAIN output into a table with a known column. 
		Supports EXPLAIN and EXPLAIN ANALYZE for rif40_geolevelAttributeTheme() ONLY.';

--
-- Eof
