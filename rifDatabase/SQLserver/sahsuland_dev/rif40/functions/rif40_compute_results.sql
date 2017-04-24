-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study - compute results
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
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_compute_results]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_compute_results]
GO 

CREATE PROCEDURE [rif40].[rif40_compute_results](@study_id INT, @debug INT)
WITH EXECUTE AS 'rif40' /* So as to be owned by RIF40 */
AS
BEGIN
	PRINT 'In: rif40.rif40_compute_results()';

	DECLARE @rval INTEGER=0; 	-- Success		
/*
Function:	rif40_compute_results()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Compute results from extract table. Create map table

INSERT INTO rif40_results (study_id, inv_id, band_id, genders, direct_standardisation, adjusted, observed)
WITH a AS (
	SELECT study_id, band_id, sex,
	       SUM(COALESCE(inv_1, 0)) AS inv_1_observed
	  FROM s217_extract
	 WHERE study_or_comparison = 'S'
	 GROUP BY study_id, band_id, sex
)
SELECT study_id, 217 AS inv_id, band_id, 1 AS genders, 0 AS direct_standardisation, 0 AS adjusted, inv_1_observed AS observed
  FROM a
 WHERE sex = 1
UNION
SELECT study_id, 217 AS inv_id, band_id, 2 AS genders, 0 AS direct_standardisation, 0 AS adjusted, inv_1_observed AS observed
  FROM a
 WHERE sex = 2
UNION
SELECT study_id, 217 AS inv_id, band_id, 3 AS genders, 0 AS direct_standardisation, 0 AS adjusted, SUM(COALESCE(inv_1_observed, 0)) AS observed
  FROM a
 GROUP BY study_id, band_id
 ORDER BY 1, 2, 3, 4, 5, 6;

Map table for disease maps supports area_id, gid and gid_rowindex for faster middleware support
This may be added to extract creation later.

 */
 
 /*
	c1comp CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_studies a
		 WHERE a.study_id = l_study_id;
	c1acomp CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_studies a
		 WHERE a.study_id = l_study_id;
	c2comp CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_investigations a
		 WHERE a.study_id = l_study_id
		 ORDER BY inv_id;
	c3comp CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_study_shares a
		 WHERE l_study_id = a.study_id;
	c4comp CURSOR FOR
		SELECT col_description(a.oid, c.ordinal_position) AS description, c.column_name
		  FROM information_schema.columns c, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner IN (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
		   AND a.relname = 't_rif40_results'
		   AND a.relname = c.table_name
		   AND  c.column_name != 'hash_partition_number' /- Exclude inherited columns -/
		 ORDER BY 1;
--	c6comp CURSOR(l_geography INTEGER) FOR
--		SELECT *
--		  FROM rif40_geographies a
--		 WHERE l_geography = a.geography;
--
-- Inherited columns failure:
--
-- [11:27:30 AM 635] notice: rif40_ddl(): SQL in error (42703)> COMMENT ON COLUMN rif_studies.s11_map.hash_partition_number IS 'Hash partition number (for partition elimination)';
-- [11:27:30 AM 646] notice: rif40_study_ddl_definer(): [56408] Study 11: statement 51 error: 42703 "column "hash_partition_number" of relation "rif_studies.s11_map" does not exist" raised by
-- SQL> COMMENT ON COLUMN rif_studies.s11_map.hash_partition_number IS 'Hash partition number (for partition elimination)';
--  after: 00:00:03.877
-- [11:27:30 AM 658] notice: rif40_study_ddl_definer(): [56409] Study 11: error prevents further processing
--		 		 
	c5comp REFCURSOR;
--
	c1_rec RECORD;
	c1a_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
--	c6_rec RECORD;
--
	sql_stmt	VARCHAR;
	ddl_stmts	VARCHAR[];
	i		INTEGER:=0;
	t_ddl		INTEGER:=1;
	inv_array	INTEGER[];
	inv		INTEGER;
BEGIN
	OPEN c1comp(study_id);
	FETCH c1comp INTO c1_rec;
	OPEN c1acomp(study_id);
	FETCH c1acomp INTO c1a_rec;
	IF NOT FOUND THEN
		CLOSE c1comp;
		CLOSE c1acomp;
		PERFORM rif40_log_pkg.rif40_error(-55600, 'rif40_compute_results', 
			'Study ID % not found',
			study_id::VARCHAR		/- Study ID -/);
	END IF;
	CLOSE c1comp;
	CLOSE c1acomp;
--
-- Calculate observed
--
-- [No genders support]
--
	sql_stmt:='INSERT INTO rif40_results (study_id, inv_id, band_id, genders, direct_standardisation, adjusted, observed)'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'WITH a AS ('||E'\n';
--
	sql_stmt:=sql_stmt||E'\t'||'SELECT study_id, band_id, sex,';				/- Alredy banded -/
	FOR c2_rec IN c2comp(study_id) LOOP
		i:=i+1;
		inv_array[i]:=c2_rec.inv_id;
		IF i = 1 THEN
		       	sql_stmt:=sql_stmt||E'\n'||E'\t'||'       SUM(COALESCE('||c2_rec.inv_name||', 0)) AS inv_'||i::VARCHAR||'_observed'||
				E'\t'||'/- '||c2_rec.inv_id||' -  '||c2_rec.numer_tab||' - '||c2_rec.inv_description||' -/';
		ELSE
		       	sql_stmt:=sql_stmt||','||E'\n'||E'\t'||'       SUM(COALESCE('||c2_rec.inv_name||', 0)) AS inv_'||i::VARCHAR||'_observed'||
				E'\t'||'/- '||c2_rec.inv_id||' -  '||c2_rec.numer_tab||' - '||c2_rec.inv_description||' -/';
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||E'\t'||'	  FROM '||quote_ident(LOWER(c1_rec.extract_table))||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'	 WHERE study_or_comparison = ''S'''||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'	 GROUP BY study_id, band_id, sex'||E'\n';
	sql_stmt:=sql_stmt||')'||E'\n';
	FOREACH inv IN ARRAY inv_array LOOP
		IF i > 1 THEN
			sql_stmt:=sql_stmt||'UNION'||E'\n';
		END IF;
		sql_stmt:=sql_stmt||'SELECT study_id, '||inv::VARCHAR||' AS inv_id, band_id,'||
		' 1 AS genders, 0 /- Indirect -/ AS direct_standardisation, 0 /- Unadjusted -/ AS adjusted, inv_'||
			i::VARCHAR||'_observed AS observed'||E'\n';
		sql_stmt:=sql_stmt||'  FROM a'||E'\n';
		sql_stmt:=sql_stmt||' WHERE sex = 1'||E'\n';
		sql_stmt:=sql_stmt||'UNION'||E'\n';
		sql_stmt:=sql_stmt||'SELECT study_id, '||inv::VARCHAR||' AS inv_id, band_id,'||
		' 2 AS genders, 0 /- Indirect -/ AS direct_standardisation, 0 /- Unadjusted -/ AS adjusted, inv_'||
			i::VARCHAR||'_observed AS observed'||E'\n';
		sql_stmt:=sql_stmt||'  FROM a'||E'\n';
		sql_stmt:=sql_stmt||' WHERE sex = 2'||E'\n';
		sql_stmt:=sql_stmt||'UNION'||E'\n';
		sql_stmt:=sql_stmt||'SELECT study_id, '||inv::VARCHAR||' AS inv_id, band_id,'||
		' 3 /- both -/ AS genders, 0 /- Indirect -/ AS direct_standardisation, 0 /- Unadjusted -/ AS adjusted, SUM(COALESCE(inv_'||
			i::VARCHAR||'_observed, 0)) AS observed'||E'\n';
		sql_stmt:=sql_stmt||'  FROM a'||E'\n';
		sql_stmt:=sql_stmt||' GROUP BY study_id, band_id'||E'\n';
	END LOOP;
	sql_stmt:=sql_stmt||' ORDER BY 1, 2, 3, 4, 5, 6';
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'55601] SQL> %;',
		sql_stmt::VARCHAR);
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'rif40_results observed INSERT'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;
--	
-- Check if t_rif40_sahsu_geometry.gid_rowindex column exists (does not pre alter 2) 
--	
	OPEN c5comp FOR EXECUTE 
		'SELECT column_name'||E'\n'||
		'  FROM information_schema.columns'||E'\n'||
		' WHERE table_name   = '''||quote_ident('t_rif40_'||LOWER(c1_rec.geography)||'_geometry')||''''||E'\n'||
		'   AND column_name  = ''gid_rowindex'''||E'\n'||
		'   AND table_schema = ''rif40''';
	FETCH c5comp INTO c5_rec;
	CLOSE c5comp;
--
-- Create map table [DOES NOT CREATE ANY ROWS]
--
-- [CONTAINS NO GEOMETRY]
--
	IF c1_rec.study_type = 1 /- Disease mapping -/ THEN
--
-- GID_ROWINDEX support in maps (extracts subject to performance tests)
--
		ddl_stmts[t_ddl]:='CREATE TABLE rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||E'\n'||
			'AS'||E'\n'||
			'SELECT ''X''::Text AS gid, LPAD(ROW_NUMBER() OVER('||E'\n'||
			'       		ORDER BY a.study_id, a.band_id, a.inv_id, a.genders, a.adjusted, a.direct_standardisation)::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			'               a.band_id::Text AS area_id,'||E'\n'||
			'       a.*'||E'\n'||
			'  FROM rif40_results a'||E'\n'||
			' WHERE a.study_id      = '||study_id::VARCHAR||' /- Current study ID -/'||E'\n'||
			' LIMIT 1'::VARCHAR; 
	ELSE
		ddl_stmts[t_ddl]:='CREATE TABLE rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||E'\n'||
			'AS'||E'\n'||
			'SELECT a.*'||E'\n'||
			'  FROM rif40_results a'||E'\n'||
			' WHERE a.study_id = '||study_id::VARCHAR||' /- Current study ID -/ LIMIT 1'::VARCHAR; 
	END IF;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'[55602] SQL> %;',
		ddl_stmts[t_ddl]::VARCHAR);
--
-- Truncate it anyway to make sure
--
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:='TRUNCATE TABLE rif_studies.'||quote_ident(LOWER(c1_rec.map_table));
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'[55603] SQL> %;',
		ddl_stmts[t_ddl]::VARCHAR);
--
-- Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 
--
	IF c1_rec.extract_permitted = 1 THEN
		sql_stmt:='GRANT SELECT,INSERT,UPDATE,TRUNCATE ON rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||' TO '||USER;
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55604] SQL> %;',
			ddl_stmts[t_ddl]::VARCHAR);
		FOR c3_rec IN c3comp(study_id) LOOP
			sql_stmt:='GRANT SELECT,INSERT,UPDATE ON rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||' TO '||c3_rec.grantee_username;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
				'[55605] SQL> %;',
				ddl_stmts[t_ddl]::VARCHAR);
		END LOOP;
	END IF;
--
-- Comment
--
	sql_stmt:='COMMENT ON TABLE rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||' IS ''Study :'||study_id::VARCHAR||' map table''';
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'[55606] SQL> %;',
		ddl_stmts[t_ddl]::VARCHAR);
	FOR c4_rec IN c4comp LOOP
		sql_stmt:='COMMENT ON COLUMN rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||'.'||c4_rec.column_name||
			' IS '''||c4_rec.description||'''';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55607] SQL> %;',
			ddl_stmts[t_ddl]::VARCHAR);
	END LOOP;
	IF c1_rec.study_type = 1 /- Disease mapping -/ THEN
--
-- GID, GID_ROWINDEX support in maps (extracts subject to performance tests)
-- AREA_ID in disease mapping
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55608] SQL> %;',
			ddl_stmts[t_ddl]::VARCHAR);
--
		sql_stmt:='COMMENT ON COLUMN rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||'.gid_rowindex'||
			' IS ''GID rowindex record locator unique key''';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55607] SQL> %;',
			ddl_stmts[t_ddl]::VARCHAR);
--
		sql_stmt:='COMMENT ON COLUMN rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||'.gid'||
			' IS ''Geographic ID (artificial primary key originally created by shp2pgsql, equals RIF40_GEOLEVELS.GEOLEVEL_ID after ST_Union() conversion to single multipolygon per AREA_ID)''';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55607] SQL> %;',
			ddl_stmts[t_ddl]::VARCHAR);
	END IF;

--
-- Execute DDL code as rif40
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		RETURN FALSE;
	END IF;
--
-- Now do real insert as user
--
	IF c1_rec.study_type = 1 /- Disease mapping -/ THEN
--
-- GID, GID_ROWINDEX support in maps (extracts subject to performance tests)
-- AREA_ID in disease mapping
--
		sql_stmt:='INSERT INTO rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||E'\n'||
			'SELECT ''X''::Text AS gid, LPAD(ROW_NUMBER() OVER('||E'\n'||
			'       		ORDER BY a.study_id, a.band_id, a.inv_id, a.genders, a.adjusted, a.direct_standardisation)::Text, 10, ''0''::Text) AS gid_rowindex,'||E'\n'||
			'               a.band_id::Text AS area_id,'||E'\n'||
			'       a.*'||E'\n'||
			'  FROM rif40_results a'||E'\n'||
			' WHERE a.study_id      = '||study_id::VARCHAR||' /- Current study ID -/'||E'\n'||
			' ORDER BY 2			/- GID_ROWINDEX -/'::VARCHAR; 
	ELSE
		sql_stmt:='INSERT INTO rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||E'\n'||
			'SELECT a.*'||E'\n'||
			' FROM rif40_results a'||E'\n'||
			' WHERE a.study_id = '||study_id::VARCHAR||' /- Current study ID -/'||E'\n'||
			' ORDER BY 1, 2, 3, 4, 5, 6'::VARCHAR; 
	END IF;
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'Map table observed INSERT'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;

	ddl_stmts:=NULL;
	t_ddl:=1;
--
-- Add primary key
--
	sql_stmt:='ALTER TABLE rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||' ADD CONSTRAINT '||quote_ident(LOWER(c1_rec.map_table)||'_pk')||
		' PRIMARY KEY (study_id, band_id, inv_id, genders, adjusted, direct_standardisation)';
	ddl_stmts[t_ddl]:=sql_stmt;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'[55611] SQL> %;',
		sql_stmt::VARCHAR);
	t_ddl:=t_ddl+1;		
	
--
-- Execute DDL code as rif40
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		RETURN FALSE;
	END IF;
	
--
-- Update area_id, gid
--
	IF c1_rec.study_type = 1 /- Disease mapping -/ THEN

		ddl_stmts:=NULL;
		t_ddl:=1;	
		
		sql_stmt:='UPDATE rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||' a'||E'\n'||
			'   SET area_id = ('||E'\n'||
			'			SELECT b.area_id'||E'\n'||
			'			  FROM rif40_study_areas b'||E'\n'||
			'			 WHERE a.study_id = b.study_id'||E'\n'||
			'			   AND a.band_id  = b.band_id)';
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55612] SQL> %;',
			sql_stmt::VARCHAR);
		t_ddl:=t_ddl+1;		
--
-- Get geographies settings
--
--		OPEN c6comp(c1_rec.geography);
--		FETCH c6comp INTO c6_rec;
--		CLOSE c6comp;
--
		sql_stmt:='UPDATE rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||' a'||E'\n'||
			'   SET gid = ('||E'\n'||
			'			SELECT d.gid'||E'\n'||
			'			  FROM '||quote_ident('lookup_'||LOWER(c1_rec.study_geolevel_name))||' d'||E'\n'||
			'			 WHERE a.area_id       = d.'||quote_ident(LOWER(c1_rec.study_geolevel_name))||')';
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55613] SQL> %;',
			sql_stmt::VARCHAR);
		t_ddl:=t_ddl+1;			

--
-- Merge gid with gid_rowindex
--		
		sql_stmt:='UPDATE rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||' a'||E'\n'||
			'   SET gid_rowindex = a.gid||''_''||a.gid_rowindex'||E'\n'||
			' WHERE a.gid IS NOT NULL';
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55614] SQL> %;',
			sql_stmt::VARCHAR);
		t_ddl:=t_ddl+1;	
		
		PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmts); -- Needs to run in user execution context
	END IF;

	ddl_stmts:=NULL;
	t_ddl:=1;	
--
-- Index, analyze
--
	IF c1_rec.study_type = 1 /- Disease mapping -/ AND c5_rec.column_name /- gid_rowindex post alter 2 -/ IS NOT NULL THEN
--
-- Add gid_rowindex unique key for disease maps
--
		sql_stmt:='CREATE UNIQUE INDEX '||quote_ident(LOWER(c1_rec.map_table)||'_uk')||
			' ON rif_studies.'||quote_ident(LOWER(c1_rec.map_table))||'(gid_rowindex)';
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'[55615] SQL> %;',
			sql_stmt::VARCHAR);
		t_ddl:=t_ddl+1;	
	END IF;
	sql_stmt:='ANALYZE rif_studies.'||quote_ident(LOWER(c1_rec.map_table));
	ddl_stmts[t_ddl]:=sql_stmt;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'[55616] SQL> %;',
		sql_stmt::VARCHAR);
--
-- Execute DDL code as rif40
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		RETURN FALSE;
	END IF;

--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_compute_results', 
		'[55617] Study ID % map table % created',
		study_id::VARCHAR,
		c1_rec.map_table::VARCHAR);
--
-- Next expected...
--
	PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_compute_results', 
		'[55618] Study ID % rif40_compute_results() not fully implemented',
		study_id::VARCHAR);
	RETURN TRUE;
--
 */
	RETURN @rval;
END;
GO

--
-- Eof