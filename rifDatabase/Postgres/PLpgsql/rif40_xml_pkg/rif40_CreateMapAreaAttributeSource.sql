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
-- Tilemaker checked - potential for trouble with column names 
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

DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	VARCHAR, VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme, VARCHAR, VARCHAR[]);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]);

CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	c4getallatt4theme 	VARCHAR,
	l_geography 		VARCHAR,
	l_geolevel_select	VARCHAR,
	l_theme			VARCHAR,
	l_attribute_source	VARCHAR,
	l_attribute_name_array	VARCHAR[]	DEFAULT NULL)
RETURNS INTEGER
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_CreateMapAreaAttributeSource()
Parameters:	Temporary table name (same as REFCURSOR), Geography, <geolevel select>, 
		theme (enum: rif40_xml_pkg.rif40_geolevelAttributeTheme), attribute source, 
 		attribute name array [Default NULL - All attributes]
Returns:	Number of rows in temporary table name
Description:	Create temporary table with all values for attributes source, attribute names, geography and geolevel select
		Create artificial primary key gid_rowindex
		Index temporary table on gid_rowindex

Warnings: 

a) This function can be slow as if uses rif40_num_denom, takes 398mS on my laptop to fetch rows to RI40_NUM_DEMON based
   (health and population) themes
b) This function is VOLATILE. Calling it twice with the same parameters will not necessarily create a temporary table 
   with the same set of rows in the same order.
   This is because databases to not guarantee data block access order, even without parallelisation.
   To minimise the volatility:
	i) Define a sort order. This has a severe performance cost for large tables.

Checks:

- User must be rif40 or have rif_user or rif_manager role
- Test geography
- Test <geolevel select> exists
- Check attribute exists, build SQL injection proof select list
- Process themes
- If attribute name array is used, then all must be found
- Check if GID, gid_rowindex exist [they do not have to, but the query runs quicker if they do]
- Geometry tables must have gid and gid_rowindex
- Check temporary table does NOT exist

Examples:

Example 1) Full SELECT of sahsuland_pop
	   This performs well on a server, takes about 3 seconds on a laptop

--
-- Create temporary table
--
SELECT *
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
                'c4getallatt4theme_3' /- Temporary table -/,
                'SAHSU', 'LEVEL2', 'population', 'sahsuland_pop');
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50807] Geography: SAHSU, geolevel select: LEVEL2, theme: population; SQL fetch returned 6 attribute names, took: 00:00:00.213.
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51612] Neither gid or gid_rowindex columns are present in attrribute source table: sahsuland_pop
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51616] Temporary table: c4getallatt4theme_3 does not exist.
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51620] c4getallatt4theme SQL>
CREATE TEMPORARY TABLE c4getallatt4theme_3
AS
WITH a AS (
        SELECT a.level2 /- Map <geolevel select> to area_id -/ AS area_id,
       g.gid,
       LPAD(g.gid::Text, 10, '0'::Text)||'_'||LPAD(ROW_NUMBER() OVER(PARTITION BY a.level2 /- Use default table order -/)::Text, 10,
 '0'::Text) AS gid_rowindex,
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
 ORDER BY 3 /- gid_rowindex -/;
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51622] Cursor: c4getallatt4theme_3, geography: SAHSU, geolevel select: LEVEL2, theme: population, attribute names: [], source: sahsuland_pop, rows: 432960; SQL parse took: 00:00:04.7
59.
 rif40_createmapareaattributesource
------------------------------------
 432960
(1 row)

--
-- Create REFCURSOR
--
SELECT *
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /- Temporary table -/,
                NULL /- Cursor name - NULL = same as temporarty table - must be unique with a TX -/,
                0 /- No offset -/, NULL /- No row limit -/);
psql:alter_scripts/v4_0_alter_1.sql:645: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51205] c4getallatt4theme SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:645: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51207] Cursor: c4getallatt4theme_3, temporary table: c4getallatt4theme_3; offset: 0, row limit: , SQL parse took: 00:00:00.016.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

--
-- Fetch tests
--
FETCH FORWARD 5 IN c4getallatt4theme_3 /- 1.3 seconds with no row limit, no sort list, no gid/gid_rowindex columns built in -/;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000000001 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000002 | 1989 |           101 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000003 | 1989 |           102 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000004 | 1989 |           103 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000005 | 1989 |           104 | 01     | 01.001.000100 | 01.001.000100.1 |    34
(5 rows)

MOVE ABSOLUTE 1000 IN c4getallatt4theme_3 /- move to row 1000 -/;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000001001 | 1994 |           210 | 01     | 01.001.000200 | 01.001.000200.1 |   390
 01.001  |   1 | 0000000001_0000001002 | 1994 |           211 | 01     | 01.001.000200 | 01.001.000200.1 |   356
 01.001  |   1 | 0000000001_0000001003 | 1994 |           212 | 01     | 01.001.000200 | 01.001.000200.1 |   392
 01.001  |   1 | 0000000001_0000001004 | 1994 |           213 | 01     | 01.001.000200 | 01.001.000200.1 |   388
 01.001  |   1 | 0000000001_0000001005 | 1994 |           214 | 01     | 01.001.000200 | 01.001.000200.1 |   286
(5 rows)

MOVE ABSOLUTE 10000 IN c4getallatt4theme_3 /- move to row 10000 -/;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

MOVE ABSOLUTE 432958 IN c4getallatt4theme_3 /- move to row 432958 - two from the end -/;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.018  |  17 | 0000000017_0000005983 | 1996 |           220 | 01     | 01.018.019500 | 01.018.019500.3 |   388
 01.018  |  17 | 0000000017_0000005984 | 1996 |           221 | 01     | 01.018.019500 | 01.018.019500.3 |   402
(2 rows)

--
-- Test cursor close. Does release resources!!!!
--
SELECT rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor('c4getallatt4theme_3');
psql:alter_scripts/v4_0_alter_1.sql:659: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51401] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:21:59.422+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:659: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51404] Cursor: c1closecursor1 closed
 rif40_closegetmapareaattributecursor
--------------------------------------

(1 row)

SELECT *
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue('c4getallatt4theme_3' /- Temporary table -/,
                NULL /- Cursor name - NULL = same as temporarty table - must be unique with a TX -/,
                10000 /- Offset -/, NULL /- No row limit -/);
psql:alter_scripts/v4_0_alter_1.sql:665: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51205] c4getallatt4theme SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:665: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51207] Cursor: c4getallatt4theme_3, temporary table: c4getallatt4theme_3; offset: 10000, row limit: , SQL parse took: 00:00:00.01.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

SELECT rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource('c4getallatt4theme_3');
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51801] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:22:03.179+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51401] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:22:03.179+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51404] Cursor: c1closecursor1 closed
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51804] Found temporary table: c4getallatt4theme_3.
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_ddl(): SQL> DROP TABLE c4getallatt4theme_3;
 rif40_deletemapareaattributesource
------------------------------------

(1 row)

Debug:

DEBUG1 prints SQL and diagnostics
DEBUG2 does an EXPLAIN PLAN

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
	c3_getallatt4theme CURSOR(
			l_geography 		VARCHAR, 
			l_geolevel_select 	VARCHAR, 
			l_theme 		VARCHAR, 
			l_attribute_name_array 	VARCHAR[]) FOR
		SELECT * 
		  FROM rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(
				l_geography, l_geolevel_select, l_theme, l_attribute_name_array);
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
	c7_getallatt4theme CURSOR(l_table VARCHAR) FOR
		SELECT table_name 
		  FROM information_schema.tables 
		 WHERE table_name  = l_table 
	           AND table_type  = 'LOCAL TEMPORARY';
	c8getallatt4theme CURSOR(l_enumlabel VARCHAR) FOR
		SELECT enumlabel 
		  FROM pg_enum 
 		 WHERE enumtypid = 'rif40_xml_pkg.rif40_geolevelAttributeTheme'::regtype 
		   AND enumlabel = l_enumlabel
  		 ORDER BY enumsortorder;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
	c6a_rec RECORD;
	c6b_rec RECORD;
	c7_rec RECORD;
	c8_rec RECORD;
--
	explain_text 			VARCHAR;
	sql_stmt			VARCHAR;
	select_list			VARCHAR;
	invalid_attribute_source	INTEGER:=0;
	attribute_name_list		VARCHAR[];
	ordinal_position_list		INTEGER[];
	sorted_attribute_name_list	VARCHAR[];
	sorted_ordinal_position_list	INTEGER[];
	num_rows			INTEGER;
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
		PERFORM rif40_log_pkg.rif40_error(-51600, 'rif40_CreateMapAreaAttributeSource', 
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
		PERFORM rif40_log_pkg.rif40_error(-51601, 'rif40_CreateMapAreaAttributeSource', 'Cursor: % in use, created: %, SQL>'||E'\n'||'%;',
			c5_rec.name::VARCHAR		/* Cursor name */,
			c5_rec.creation_time::VARCHAR	/* Created */,
			c5_rec.statement::VARCHAR	/* SQL */);
	END IF;

--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51602, 'rif40_CreateMapAreaAttributeSource', 'NULL geography parameter');
	END IF;	
--
	OPEN c1getallatt4theme(l_geography);
	FETCH c1getallatt4theme INTO c1_rec;
	CLOSE c1getallatt4theme;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51603, 'rif40_CreateMapAreaAttributeSource', 'geography: % not found', 
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
		PERFORM rif40_log_pkg.rif40_error(-51604, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %: not found', 
			l_geography::VARCHAR		/* Geography */, 
			l_geolevel_select::VARCHAR	/* geolevel select */);
	END IF;	
--
-- Check enum value (theme)
--
	OPEN c8getallatt4theme(l_theme);
	FETCH c8getallatt4theme INTO c8_rec;
	CLOSE c8getallatt4theme;
	IF c8_rec.enumlabel IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50805, 'rif40_CreateMapAreaAttributeSource', 
			'Geography: %, geolevel select: %, invalid theme: %', 			
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_select::VARCHAR		/* Geolevel select */, 
			l_theme::VARCHAR			/* Theme */);
	END IF;

--
-- Check attribute exists
--
	FOR c3_rec IN c3_getallatt4theme(l_geography, l_geolevel_select, l_theme, l_attribute_name_array) LOOP
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
		PERFORM rif40_log_pkg.rif40_error(-51606, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %, <attribute source>: %s; sorted attribute list is null', 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_select::VARCHAR		/* Geolevel select */,
			l_attribute_source::VARCHAR		/* Attribute source */);
	ELSIF array_length(attribute_name_list, 1) != array_length(sorted_attribute_name_list, 1) THEN
		PERFORM rif40_log_pkg.rif40_error(-51607, 'rif40_CreateMapAreaAttributeSource', 
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
		PERFORM rif40_log_pkg.rif40_error(-51608, 'rif40_CreateMapAreaAttributeSource', 
			'geography: %, <geolevel select> %, <attribute source>: %s; no attributes found, % of % <attribute source> were invalid', 
			l_geography::VARCHAR			/* Geography */, 
			l_geolevel_select::VARCHAR		/* Geolevel select */,
			l_attribute_source::VARCHAR		/* Attribute source */,
			invalid_attribute_source::VARCHAR	/* Invalid attribute sources */,
			array_length(l_attribute_name_array, 1)::VARCHAR	/* Total attributes */);
	ELSIF select_list IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51609, 'rif40_CreateMapAreaAttributeSource', 
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
		OPEN c6_6getallatt4theme('rif_data', lower(l_attribute_source), 'gid');
	END IF;
	FETCH c6_6getallatt4theme INTO c6a_rec;
	CLOSE c6_6getallatt4theme;
	IF l_theme IN ('extract', 'results') THEN
		OPEN c6_6getallatt4theme('rif_studies', lower(l_attribute_source), 'gid_rowindex');
	ELSE
		OPEN c6_6getallatt4theme('rif_data', lower(l_attribute_source), 'gid_rowindex');
	END IF;
	FETCH c6_6getallatt4theme INTO c6b_rec;
	CLOSE c6_6getallatt4theme;
	IF c6a_rec.column_name = 'gid' AND c6b_rec.column_name = 'gid_rowindex' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[51610] Both gid and gid_rowindex columns are present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	ELSIF c6a_rec.column_name = 'gid' AND c6b_rec.column_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[51612] Only the gid column is present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	ELSIF c6a_rec.column_name IS NULL AND c6b_rec.column_name = 'gid_rowindex' THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[51612] Only the gid_rowindex column is present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource',
			'[51613] Neither gid or gid_rowindex columns are present in attrribute source table: %',
			l_attribute_source::VARCHAR);
	END IF;
--
-- Geometry tables must have gid and gid_rowindex
--
	IF l_theme = 'geometry' AND (c6a_rec.column_name IS NULL OR c6b_rec.column_name IS NULL) THEN
		PERFORM rif40_log_pkg.rif40_error(-51614, 'rif40_CreateMapAreaAttributeSource', 
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
		PERFORM rif40_log_pkg.rif40_error(-51615, 'rif40_CreateMapAreaAttributeSource', 
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
			  E'\t'||'       '||quote_ident('geometry_'||LOWER(l_geography))||' g'||E'\n'||
			  E'\t'||' WHERE g.geography     = $1'||E'\n'||
			  E'\t'||'   AND g.geolevel_name = $2 /* Partition elimination */'||E'\n'||
			  E'\t'||'   AND g.area_id       = a.'||quote_ident(LOWER(l_geolevel_select))||' /* Link gid */'||E'\n';
	ELSIF l_theme= 'extract' THEN
		sql_stmt:='WITH a AS ('||E'\n'||
			  E'\t'||'SELECT '||select_list||E'\n'||
			  E'\t'||'  FROM rif_studies.'||quote_ident(lower(l_attribute_source))||' /* Needs path adding */'||' a,'||E'\n'||
			  E'\t'||'       '||quote_ident('geometry_'||LOWER(l_geography))||' g'||E'\n'||
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
			  ' ORDER BY gid_rowindex';
--
-- Check temporary table does NOT exist
--
	OPEN c7_getallatt4theme(LOWER(c4getallatt4theme));
	FETCH c7_getallatt4theme INTO c7_rec;
	CLOSE c7_getallatt4theme;
	IF c7_rec.table_name IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_error(51616, 'rif40_DeleteMapAreaAttributeSource', 'Temporary table: % already exists.',
			c7_rec.table_name::VARCHAR		/*  Temporary table name */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_DeleteMapAreaAttributeSource', '[51617] Temporary table: % does not exist.',
			LOWER(c4getallatt4theme)::VARCHAR		/* Temporary table name */);
	END IF;	

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
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource', '[51618] c4getallatt4theme EXPLAIN SQL> '||E'\n'||'%;', sql_stmt::VARCHAR); 
			sql_stmt:='EXPLAIN ANALYZE VERBOSE CREATE TEMPORARY TABLE '||quote_ident(LOWER(c4getallatt4theme::Text))||E'\n'||'AS'||E'\n'||sql_stmt;
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
				RAISE INFO '51619: %', error_message;
--
				RAISE;
		END;
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_CreateMapAreaAttributeSource', 
			'[51620] c4getallatt4theme EXPLAIN PLAN.'||E'\n'||'%', explain_text::VARCHAR);	
	ELSE
--
-- Non EXPLAIN PLAN version
--
		BEGIN
--
-- Create temporary table
--
			sql_stmt:='CREATE TEMPORARY TABLE '||quote_ident(LOWER(c4getallatt4theme::Text))||E'\n'||'AS'||E'\n'||sql_stmt;
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource', 
				'[51621] c4getallatt4theme SQL> '||E'\n'||'%;', sql_stmt::VARCHAR); 
			EXECUTE sql_stmt USING l_geography, l_geolevel_select;
			GET DIAGNOSTICS num_rows = ROW_COUNT;
		EXCEPTION
			WHEN others THEN
--
-- Print exception to INFO, re-raise
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='rif40_CreateMapAreaAttributeSource() caught: '||E'\n'||
					SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
				RAISE INFO '51622: %', error_message;
--
				RAISE;
		END;
	END IF;
--
-- Create unique index on gid_rowindex, ANALYZE
--
	sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c4getallatt4theme))||'_uk ON '||quote_ident(LOWER(c4getallatt4theme))||'(gid_rowindex)';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	sql_stmt:='ANALYZE VERBOSE '||quote_ident(LOWER(c4getallatt4theme));
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- EXPLAIN PLAN masks the row count, so COUNT UK
--
	IF rif40_log_pkg.rif40_is_debug_enabled('rif40_CreateMapAreaAttributeSource', 'DEBUG2') THEN
		sql_stmt:='SELECT COUNT(gid_rowindex) AS num_rows FROM '||quote_ident(LOWER(c4getallatt4theme));
		EXECUTE sql_stmt INTO num_rows;
	END IF;

--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_CreateMapAreaAttributeSource', 
		'[51623] Cursor: %, geography: %, geolevel select: %, theme: %, attribute names: [%], source: %, rows: %; SQL parse took: %.', 			
		LOWER(quote_ident(c4getallatt4theme::Text))::VARCHAR	/* Cursor name */, 
		l_geography::VARCHAR					/* Geography */, 
		l_geolevel_select::VARCHAR				/* Geolevel select */, 
		l_theme::VARCHAR					/* Theme */, 
		array_to_string(l_attribute_name_array, ',')::VARCHAR	/* attribute names */, 
		l_attribute_source::VARCHAR				/* attribute source table */, 
		num_rows::VARCHAR					/* Number of rows processed */,
		took::VARCHAR						/* Time taken */);
--
	RETURN num_rows;	/* Number of rows in temporary table name */
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	rif40_CreateMapAreaAttributeSource()
Parameters:	Temporary table name (same as REFCURSOR), Geography, <geolevel select>, 
		theme (enum: rif40_xml_pkg.rif40_geolevelAttributeTheme), attribute source, 
 		attribute name array [Default NULL - All attributes]
Returns:	Number of rows in temporary table name
Description:	Create temporary table with all values for attributes source, attribute names, geography and geolevel select
		Create artificial primary key gid_rowindex
		Index temporary table on gid_rowindex

Warnings: 

a) This function can be slow as if uses rif40_num_denom, takes 398mS on my laptop to fetch rows to RI40_NUM_DEMON based
   (health and population) themes
b) This function is VOLATILE. Calling it twice with the same parameters will not necessarily create a temporary table 
   with the same set of rows in the same order.
   This is because databases to not guarantee data block access order, even without parallelisation.
   To minimise the volatility:
	i) Define a sort order. This has a severe performance cost for large tables.

Checks:

- User must be rif40 or have rif_user or rif_manager role
- Test geography
- Test <geolevel select> exists
- Check attribute exists, build SQL injection proof select list
- Process themes
- If attribute name array is used, then all must be found
- Check if GID, gid_rowindex exist [they do not have to, but the query runs quicker if they do]
- Geometry tables must have gid and gid_rowindex
- Check temporary table does NOT exist

Examples:

Example 1) Full SELECT of sahsuland_pop
	   This performs well on a server, takes about 3 seconds on a laptop

--
-- Create temporary table
--
SELECT *
  FROM rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
                ''c4getallatt4theme_3'' /* Temporary table */,
                ''SAHSU'', ''LEVEL2'', ''population'', ''sahsuland_pop'');
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_getAllAttributesForGeoLevelAttributeTheme(): [50807] Geography: SAHSU, geolevel select: LEVEL2, theme: population; SQL fetch returned 6 attribute names, took: 00:00:00.213.
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51612] Neither gid or gid_rowindex columns are present in attrribute source table: sahsuland_pop
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51616] Temporary table: c4getallatt4theme_3 does not exist.
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51620] c4getallatt4theme SQL>
CREATE TEMPORARY TABLE c4getallatt4theme_3
AS
WITH a AS (
        SELECT a.level2 /* Map <geolevel select> to area_id */ AS area_id,
       g.gid,
       LPAD(g.gid::Text, 10, ''0''::Text)||''_''||LPAD(ROW_NUMBER() OVER(PARTITION BY a.level2 /* Use default table order */)::Text, 10,
 ''0''::Text) AS gid_rowindex,
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
 ORDER BY 3 /* gid_rowindex */;
psql:alter_scripts/v4_0_alter_1.sql:638: INFO:  [DEBUG1] rif40_CreateMapAreaAttributeSource(): [51622] Cursor: c4getallatt4theme_3, geography: SAHSU, geolevel select: LEVEL2, theme: population, attribute names: [], source: sahsuland_pop, rows: 432960; SQL parse took: 00:00:04.7
59.
 rif40_createmapareaattributesource
------------------------------------
 432960
(1 row)

--
-- Create REFCURSOR
--
SELECT *
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(''c4getallatt4theme_3'' /* Temporary table */,
                NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */,
                0 /* No offset */, NULL /* No row limit */);
psql:alter_scripts/v4_0_alter_1.sql:645: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51205] c4getallatt4theme SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:645: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51207] Cursor: c4getallatt4theme_3, temporary table: c4getallatt4theme_3; offset: 0, row limit: , SQL parse took: 00:00:00.016.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

--
-- Fetch tests
--
FETCH FORWARD 5 IN c4getallatt4theme_3 /* 1.3 seconds with no row limit, no sort list, no gid/gid_rowindex columns built in */;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000000001 | 1989 |           100 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000002 | 1989 |           101 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000003 | 1989 |           102 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000004 | 1989 |           103 | 01     | 01.001.000100 | 01.001.000100.1 |    34
 01.001  |   1 | 0000000001_0000000005 | 1989 |           104 | 01     | 01.001.000100 | 01.001.000100.1 |    34
(5 rows)

MOVE ABSOLUTE 1000 IN c4getallatt4theme_3 /* move to row 1000 */;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.001  |   1 | 0000000001_0000001001 | 1994 |           210 | 01     | 01.001.000200 | 01.001.000200.1 |   390
 01.001  |   1 | 0000000001_0000001002 | 1994 |           211 | 01     | 01.001.000200 | 01.001.000200.1 |   356
 01.001  |   1 | 0000000001_0000001003 | 1994 |           212 | 01     | 01.001.000200 | 01.001.000200.1 |   392
 01.001  |   1 | 0000000001_0000001004 | 1994 |           213 | 01     | 01.001.000200 | 01.001.000200.1 |   388
 01.001  |   1 | 0000000001_0000001005 | 1994 |           214 | 01     | 01.001.000200 | 01.001.000200.1 |   286
(5 rows)

MOVE ABSOLUTE 10000 IN c4getallatt4theme_3 /* move to row 10000 */;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

MOVE ABSOLUTE 432958 IN c4getallatt4theme_3 /* move to row 432958 - two from the end */;
MOVE 1
FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.018  |  17 | 0000000017_0000005983 | 1996 |           220 | 01     | 01.018.019500 | 01.018.019500.3 |   388
 01.018  |  17 | 0000000017_0000005984 | 1996 |           221 | 01     | 01.018.019500 | 01.018.019500.3 |   402
(2 rows)

--
-- Test cursor close. Does release resources!!!!
--
SELECT rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor(''c4getallatt4theme_3'');
psql:alter_scripts/v4_0_alter_1.sql:659: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51401] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:21:59.422+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:659: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51404] Cursor: c1closecursor1 closed
 rif40_closegetmapareaattributecursor
--------------------------------------

(1 row)

SELECT *
  FROM rif40_xml_pkg.rif40_GetMapAreaAttributeValue(''c4getallatt4theme_3'' /* Temporary table */,
                NULL /* Cursor name - NULL = same as temporarty table - must be unique with a TX */,
                10000 /* Offset */, NULL /* No row limit */);
psql:alter_scripts/v4_0_alter_1.sql:665: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51205] c4getallatt4theme SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:665: INFO:  [DEBUG1] rif40_GetMapAreaAttributeValue(): [51207] Cursor: c4getallatt4theme_3, temporary table: c4getallatt4theme_3; offset: 10000, row limit: , SQL parse took: 00:00:00.01.
 rif40_getmapareaattributevalue
--------------------------------
 c4getallatt4theme_3
(1 row)

FETCH FORWARD 5 IN c4getallatt4theme_3;
 area_id | gid |     gid_rowindex      | year | age_sex_group | level1 |    level3     |     level4      | total
---------+-----+-----------------------+------+---------------+--------+---------------+-----------------+-------
 01.002  |   2 | 0000000002_0000008593 | 1990 |           112 | 01     | 01.002.000700 | 01.002.000700.2 |   728
 01.002  |   2 | 0000000002_0000008594 | 1990 |           113 | 01     | 01.002.000700 | 01.002.000700.2 |   542
 01.002  |   2 | 0000000002_0000008595 | 1990 |           114 | 01     | 01.002.000700 | 01.002.000700.2 |   514
 01.002  |   2 | 0000000002_0000008596 | 1990 |           115 | 01     | 01.002.000700 | 01.002.000700.2 |   494
 01.002  |   2 | 0000000002_0000008597 | 1990 |           116 | 01     | 01.002.000700 | 01.002.000700.2 |   528
(5 rows)

SELECT rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource(''c4getallatt4theme_3'');
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51801] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:22:03.179+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51401] Cursor: c4getallatt4theme_3 in use, created: 2014-07-09 09:22:03.179+01, SQL>
SELECT * FROM c4getallatt4theme_3 ORDER BY gid_rowindex OFFSET $1;
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_closeGetMapAreaAttributeCursor(): [51404] Cursor: c1closecursor1 closed
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_DeleteMapAreaAttributeSource(): [51804] Found temporary table: c4getallatt4theme_3.
psql:alter_scripts/v4_0_alter_1.sql:669: INFO:  [DEBUG1] rif40_ddl(): SQL> DROP TABLE c4getallatt4theme_3;
 rif40_deletemapareaattributesource
------------------------------------

(1 row)

Debug:

DEBUG1 prints SQL and diagnostics
DEBUG2 does an EXPLAIN PLAN

';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_CreateMapAreaAttributeSource(
	VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) TO rif_user;

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
		PERFORM rif40_log_pkg.rif40_error(-51699, 'rif40_CreateMapAreaAttributeSource_explain_ddl', 'User % must be rif40 or have rif_user or rif_manager role', 
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
