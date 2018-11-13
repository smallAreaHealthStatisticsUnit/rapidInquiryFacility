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
-- Rapid Enquiry Facility (RIF) - RIF state machine
--     				  			  rif40_create_extract
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
-- rif40_sm_pkg:
--
-- rif40_create_extract: 		55400 to 55599
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

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_create_extract(study_id INTEGER)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_create_extract()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Create extract table. Calls rif40_insert_extract() to populate extract table.

Notes:

1. SQL created by rif40_study_ddl_definer() runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Same on Postgres and Postgres/Oracle FDW variants. Oracle remote execution is handled by rif40_insert_extract()

Check extract table does not exist
Check extract is permitted

Create extract table

The basis for this is the performance tests created from the new EHA extract for Lea in September 2012
The table has the following standard columns

CREATE TABLE <extract_table> (
 	year                    	SMALLINT 	NOT NULL,
	study_or_comparison			VARCHAR(1) 	NOT NULL,
	study_id					INTEGER 	NOT NULL,
 	area_id                 	VARCHAR 	NOT NULL,	
	band_id						INTEGER,
    intersect_count         	INTEGER,         
    distance_from_nearest_source NUMERIC,  
    nearest_rifshapepolyid      VARCHAR,
    exposure_value              NUMERIC,	
 	sex                     	SMALLINT,
 	age_group               	VARCHAR,
 	total_pop               	DOUBLE PRECISION,

Disease mapping extract tables do not contain: intersect_count, distance_from_nearest_source, 
nearest_rifshapepolyid, exposure_value

One column per distinct covariate

 	<rif40_inv_covariates.covariate_name>    VARCHAR,

One column per investigation

 	<rif40_investigations>.<inv_name>        VARCHAR);

Index: year, study_or_comparison if no partitoning
       area_id, band_id, sex, age_group

Comment extract table and columns

Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process

Call rif40_insert_extract() to populate extract table.

Vacuum analyze

Partitioned by RANGE year

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process
 */
	c1_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1a_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c2_creex CURSOR(l_study_id INTEGER) FOR
		SELECT DISTINCT(a.covariate_name) AS covariate_name
		  FROM rif40_inv_covariates a
		 WHERE l_study_id = a.study_id
		 ORDER BY a.covariate_name;
	c3_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_investigations a
		 WHERE l_study_id = a.study_id
  		 ORDER BY inv_id;
	c4_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_study_shares a
		 WHERE l_study_id = a.study_id;
	c1_rec RECORD;
	c1a_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	schema 		VARCHAR;
--
	sql_stmt 	VARCHAR;
	ddl_stmts	VARCHAR[];
	t_ddl		INTEGER:=0;
	sql_frag 	VARCHAR;
--
	index_column	VARCHAR;
	index_columns 	VARCHAR[] := ARRAY['area_id', 'band_id', 'sex', 'age_group'];
	table_column	VARCHAR;
	table_columns	VARCHAR[];
	pk_index_columns VARCHAR[] := ARRAY['year', 'study_or_comparison', 'study_id', 'area_id',
		'sex',  'age_group'];
	column_comments	VARCHAR[];
	i		INTEGER:=0;
BEGIN
	OPEN c1_creex(study_id);
	FETCH c1_creex INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_creex;
		PERFORM rif40_log_pkg.rif40_error(-55400, 'rif40_create_extract', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_creex;
	OPEN c1a_creex(study_id);
	FETCH c1a_creex INTO c1a_rec;
	CLOSE c1a_creex;
--
-- Check extract table does not exist
--
	IF c1_rec.extract_table IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-55401, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract table: not defined',
			c1_rec.study_id::VARCHAR	/* Study id */);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NULL AND c1_rec.study_state = 'V'  THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_extract', 
			'[55402] RIF40_STUDIES study % extract table: % defined, awaiting creation',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.extract_table::VARCHAR 	/* extract_table */);
	ELSIF c1_rec.study_state != 'V' THEN
		PERFORM rif40_log_pkg.rif40_error(-55403, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract table: %; in wrong state: %',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.extract_table::VARCHAR 	/* extract_table */,
			c1_rec.study_state::VARCHAR	/* State */);
	ELSE /* schema IS NOT NULL */
		PERFORM rif40_log_pkg.rif40_error(-55404, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract table: %; exists in schema: %',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.extract_table::VARCHAR 	/* extract_table */,
			schema::VARCHAR			/* Schema */);
	END IF;         
--
-- Check extract is permitted
--
	IF c1_rec.extract_permitted != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-55405, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract not currently permitted [use RIF IG tool]',
			c1_rec.study_id::VARCHAR	/* Study id */);
	END IF;

--
-- Check extract is being run by study owner
--
	IF c1_rec.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-55406, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract must be run by study owner % not %',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.username::VARCHAR	/* Study owner */,
			USER::VARCHAR			/* User of this function */);
	END IF;
	
--
-- Disease mapping extract tables do not contain: intersect_count, distance_from_nearest_source, 
-- nearest_rifshapepolyid, exposure_value  
--	
	IF c1_rec.study_type != 1 THEN /* Risk analysis */
		table_columns := ARRAY['year', 'study_or_comparison', 'study_id', 'area_id',
			'band_id', 'sex',  'age_group', 'total_pop', 
			'intersect_count', 'distance_from_nearest_source', 'nearest_rifshapepolyid', 'exposure_value'];
		column_comments := ARRAY['Year', 'Study (S) or comparison (C) area', 'Study ID', 'Area ID',
			'Band ID', 'Sex',  'Age group', 'Total population', 'Number of intersects with shapes', 
			'Distance from nearest source (Km)', 'Nearest rifshapepolyid (shape reference)', 
			'Exposure value (when bands selected by exposure values)'];
	ELSE
		table_columns := ARRAY['year', 'study_or_comparison', 'study_id', 'area_id',
			'band_id', 'sex',  'age_group', 'total_pop'];
		column_comments := ARRAY['Year', 'Study (S) or comparison (C) area', 'Study ID', 'Area ID',
			'Band ID', 'Sex',  'Age group', 'Total population'];
	END IF;
	
--
-- Create extract table
--
-- The basis for this is the performance tests created from the new EHA extract for Lea in September 2012
-- The table has the following standard columns
--
--CREATE TABLE <extract_table> (
-- 	year                    	SMALLINT 	NOT NULL,
--	study_or_comparison			VARCHAR(1) 	NOT NULL,
--	study_id					INTEGER 	NOT NULL,
-- 	area_id                 	VARCHAR 	NOT NULL,
--	band_id						INTEGER,
--  intersect_count         	INTEGER,         
--  distance_from_nearest_source NUMERIC, 
--  nearest_rifshapepolyid      VARCHAR,
--  exposure_value              NUMERIC,
-- 	sex                     	SMALLINT,
-- 	age_group               	VARCHAR,
-- 	total_pop               	DOUBLE PRECISION,
--
-- Disease mapping extract tables do not contain: intersect_count, distance_from_nearest_source, 
-- nearest_rifshapepolyid, exposure_value  
--
	sql_stmt:='CREATE TABLE rif_studies.'||LOWER(c1_rec.extract_table)||' ('||E'\n'||
		 	E'\t'||'year                    	SMALLINT 	NOT NULL,'||E'\n'||
			E'\t'||'study_or_comparison			VARCHAR(1) 	NOT NULL,'||E'\n'||
			E'\t'||'study_id					INTEGER 	NOT NULL,'||E'\n'||
 			E'\t'||'area_id                 	VARCHAR 	NOT NULL,'||E'\n'||
			E'\t'||'band_id						INTEGER,'||E'\n';
	IF c1_rec.study_type != 1 THEN /* Risk analysis */
		sql_stmt:=sql_stmt||E'\t'||'intersect_count         	INTEGER,'||E'\n'||
							E'\t'||'distance_from_nearest_source NUMERIC,'||E'\n'||
							E'\t'||'nearest_rifshapepolyid      VARCHAR,'||E'\n'||
							E'\t'||'exposure_value    	     	NUMERIC,'||E'\n';
	END IF;
	sql_stmt:=sql_stmt||E'\t'||'sex                     	SMALLINT,'||E'\n'||
					    E'\t'||'age_group               	SMALLINT,'||E'\n';
--
-- One column per distinct covariate
--
-- 	<rif40_inv_covariates>.<covariate_name>    VARCHAR,
--
	FOR c2_rec IN c2_creex(study_id) LOOP
		sql_stmt:=sql_stmt||E'\t'||LOWER(c2_rec.covariate_name)||'               VARCHAR,'||E'\n'; /* Covariate value always coerced to a VARCHAR */
		table_columns:=array_append(table_columns, c2_rec.covariate_name::VARCHAR);
		pk_index_columns:=array_append(pk_index_columns, c2_rec.covariate_name::VARCHAR);
		column_comments:=array_append(column_comments, c2_rec.covariate_name::VARCHAR);
	END LOOP;
--
-- One column per investigation
--
-- 	<rif40_investigations>.<inv_name>          VARCHAR);
--
-- [Make INV_1 INV_<inv_id> extracts] - this appears to be approximately the case; as it is INV_NAME, but the default needs to
-- be looked at
--
	FOR c3_rec IN c3_creex(study_id) LOOP
		sql_stmt:=sql_stmt||E'\t'||LOWER(c3_rec.inv_name)||'               BIGINT,'||E'\n';
		table_columns:=array_append(table_columns, LOWER(c3_rec.inv_name)::VARCHAR);
		column_comments:=array_append(column_comments, c3_rec.inv_description::VARCHAR);
	END LOOP;
	sql_stmt:=sql_stmt||E'\t'||'total_pop               DOUBLE PRECISION)';
--
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;

--
-- Partitioned by RANGE year, study_or_comparison
--

--
-- Comment extract table and columns
--
	IF c1_rec.description IS NOT NULL THEN
		sql_stmt:='COMMENT ON TABLE rif_studies.'||LOWER(c1_rec.extract_table)||' IS ''Study '||study_id::Text||' extract: '||c1_rec.description::Text||'''';
	ELSE
		sql_stmt:='COMMENT ON TABLE rif_studies.'||LOWER(c1_rec.extract_table)||' IS ''Study '||study_id::Text||' extract: NO DESCRIPTION''';
	END IF;
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;
	FOREACH table_column IN ARRAY table_columns LOOP
		i:=i+1;
		sql_stmt:='COMMENT ON COLUMN rif_studies.'||LOWER(c1_rec.extract_table)||'.'||table_column||' IS '''||column_comments[i]||'''';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
	END LOOP;

--
-- Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 
--
	IF c1_rec.extract_permitted = 1 THEN
		sql_stmt:='GRANT SELECT,INSERT,TRUNCATE ON rif_studies.'||LOWER(c1_rec.extract_table)||' TO '||USER;
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		FOR c4_rec IN c4_creex(study_id) LOOP
			sql_stmt:='GRANT SELECT,INSERT ON rif_studies.'||LOWER(c1_rec.extract_table)||' TO '||c4_rec.grantee_username;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		END LOOP;
	END IF;

--
-- Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40)
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		PERFORM rif40_log_pkg.rif40_log ('WARNING', 'rif40_create_extract', 
			'[55407] RIF40_STUDIES study % extract creation failed, see previous warnings',
			c1_rec.study_id::VARCHAR	/* Study id */);
		RETURN FALSE;
	END IF;
--
-- Call rif40_insert_extract() to populate extract table.
-- 
	IF rif40_sm_pkg.rif40_insert_extract(c1_rec.study_id) = FALSE THEN
		PERFORM rif40_log_pkg.rif40_log ('WARNING', 'rif40_create_extract', 
			'[55408] RIF40_STUDIES study % populated extract failed, see previous warnings',
			c1_rec.study_id::VARCHAR	/* Study id */);
		RETURN FALSE;
	END IF;
--
-- Reset DDL statement array
--
	ddl_stmts:=NULL;
	t_ddl:=0;
--
-- Index: year, study_or_comparison if no partitioning
--	  area_id, band_id, sex, age_group
--
-- NEEDS TO BE MOVED TO AFTER INSERT, ADD PK
--
	IF c1_rec.partition_parallelisation = 0 THEN
		index_columns:=array_cat(index_columns, ARRAY['year', 'study_or_comparison']::VARCHAR[]);
	END IF;
	FOREACH index_column IN ARRAY index_columns LOOP
		sql_stmt:='CREATE INDEX '||LOWER(c1_rec.extract_table)||'_'||index_column||
			' ON rif_studies.'||LOWER(c1_rec.extract_table)||'('||index_column||')';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
	END LOOP;

--
-- Primary key index on: year, study_or_comparison, study_id, area_id, band_id, sex, age_group,
-- ses column(s)
--	
	sql_frag:=NULL;
	FOREACH index_column IN ARRAY pk_index_columns LOOP
		IF sql_frag IS NULL THEN
			sql_frag:=LOWER(index_column);
		ELSE
			sql_frag:=sql_frag||','||LOWER(index_column);
		END IF;
	END LOOP;
--	IF c1_rec.study_type != '1' THEN /* study type: 1 - disease mapping */
--		sql_frag:=sql_frag||',band_id'; /* Risk analysis only */
--	END IF;
	sql_stmt:='ALTER TABLE rif_studies.'||LOWER(c1_rec.extract_table)||
		' ADD CONSTRAINT '||LOWER(c1_rec.extract_table)||'_pk PRIMARY KEY ('||sql_frag||')';
--  Peter H: 19/1/2018 - disable PK as covariate values may be NULL and NULL is not an allowed PK value!	
--	t_ddl:=t_ddl+1;	
--	ddl_stmts[t_ddl]:=sql_stmt;
--
-- Vacuum analyze - raises 25001 "VACUUM cannot run inside a transaction block"
--
-- DEFER TO LATER
--
	sql_stmt:='ANALYZE rif_studies.'||LOWER(c1_rec.extract_table);
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;
--
-- Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40)
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		PERFORM rif40_log_pkg.rif40_log ('WARNING', 'rif40_create_extract', 
			'[55409] RIF40_STUDIES study % extract index/analyze failed, see previous warnings',
			c1_rec.study_id::VARCHAR	/* Study id */);
		RETURN FALSE;
	END IF;
--
	RETURN TRUE;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_create_extract(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_create_extract(INTEGER) IS 'Function:	rif40_create_extract()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Create extract table. Calls rif40_insert_extract() to populate extract table.

Notes:

1. SQL created by rif40_study_ddl_definer() runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Same on Postgres and Postgres/Oracle FDW variants. Oracle remote execution is handled by rif40_insert_extract()

Check extract table does not exist
Check extract is permitted

Create extract table

The basis for this is the performance tests created from the new EHA extract for Lea in September 2012
The table has the following standard columns

CREATE TABLE <extract_table> (
 	year                    	SMALLINT 	NOT NULL,
	study_or_comparison			VARCHAR(1) 	NOT NULL,
	study_id					INTEGER 	NOT NULL,
 	area_id                 	VARCHAR 	NOT NULL,	
	band_id						INTEGER,
    intersect_count         	INTEGER,         
    distance_from_nearest_source NUMERIC,  
    nearest_rifshapepolyid      VARCHAR,
    exposure_value              NUMERIC,	
 	sex                     	SMALLINT,
 	age_group               	VARCHAR,
 	total_pop               	DOUBLE PRECISION,

One column per distinct covariate

 	<rif40_inv_covariates.covariate_name>    VARCHAR,

One column per investigation

 	<rif40_investigations>.<inv_name>        VARCHAR);

Index: year, study_or_comparison if no partitoning
       area_id, band_id, sex, age_group

Disease mapping extract tables do not contain: intersect_count, distance_from_nearest_source, 
nearest_rifshapepolyid, exposure_value

Comment extract table and columns

Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process

Call rif40_insert_extract() to populate extract table.

Vacuum analyze

Partitioned by RANGE year

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process';

--
-- Eof
