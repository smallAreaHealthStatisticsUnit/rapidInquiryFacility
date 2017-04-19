-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study - create extract
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
-- Error codes:  ..\..\error_handling\rif40_custom_error_messages.sql
--
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_create_extract]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_create_extract]
GO 

CREATE PROCEDURE [rif40].[rif40_create_extract](@rval INT OUTPUT, @study_id INT, @debug INT)
WITH EXECUTE AS 'rif40' /* So as to be owned by RIF40 */
AS
BEGIN
/*
Function:	rif40_create_extract()
Parameter:	Success or failure [INTEGER], Study ID, enable debug (INTEGER: default 0)
Returns:	Success or failure [INTEGER], as  first parameter
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
 	year                SMALLINT 	NOT NULL,
	study_or_comparison	VARCHAR(1) 	NOT NULL,
	study_id			INTEGER 	NOT NULL,
 	area_id             VARCHAR 	NOT NULL,
	band_id				INTEGER,
 	sex                 SMALLINT,
    age_group			SMALLINT,
    ses					VARCHAR(30),
 	total_pop           DOUBLE PRECISION,

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

Procedure runs as definer (RIF40); but executes SELECTs from RIF views as caller so as to have access.
rif40_dll() is run as definer (RIF40) so extract tables are owner by the RIF and then GRANTed to the user.

 */
--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;
	
	DECLARE c1_creex CURSOR FOR
		SELECT study_id, study_state, extract_table, extract_permitted, username, description
		  FROM rif40_studies a
		 WHERE @study_id = a.study_id;
	DECLARE c2_creex CURSOR FOR
		SELECT DISTINCT(a.covariate_name) AS covariate_name
		  FROM rif40_inv_covariates a
		 WHERE @study_id = a.study_id
		 ORDER BY a.covariate_name;
	DECLARE c3_creex CURSOR FOR
		SELECT inv_name, inv_description
		  FROM rif40_investigations a
		 WHERE @study_id = a.study_id
  		 ORDER BY inv_id;
	DECLARE c4_creex CURSOR FOR
		SELECT grantee_username
		  FROM rif40_study_shares a
		 WHERE @study_id = a.study_id;
--
	DECLARE @c1_rec_study_id 			INTEGER;
	DECLARE @c1_rec_study_state 		VARCHAR(1);
	DECLARE @c1_rec_extract_table 		VARCHAR(30);
	DECLARE @c1_rec_extract_permitted	INTEGER;
	DECLARE @c1_rec_username			VARCHAR(90);
	DECLARE @c1_rec_description 		VARCHAR(250);
--
	DECLARE @c2_rec_covariate_name 		VARCHAR(30);
	DECLARE @c3_rec_inv_name 			VARCHAR(20);
	DECLARE @c3_rec_inv_description 	VARCHAR(250);
	DECLARE @c4_rec_grantee_username 	VARCHAR(90);
--
	DECLARE @schema_name 			VARCHAR(30);
--
	DECLARE @sql_stmt 	NVARCHAR(MAX);
--
	DECLARE @table_columns TABLE (column_name VARCHAR(30), column_comment VARCHAR(4000));
	DECLARE @index_columns TABLE (column_name VARCHAR(30));
	DECLARE @pk_index_columns TABLE (column_name VARCHAR(30));
--		
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('year', 'Year');
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('study_or_comparison', 'Study (S) or comparison (C) area');
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('study_id', 'Study ID');
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('area_id', 'Area ID');
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('band_id', 'Band ID');
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('sex', 'Sex');
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('age_group', 'Age group');
	INSERT INTO @table_columns(column_name, column_comment) VALUES ('total_pop', 'Total population');

	INSERT INTO @index_columns(column_name) VALUES ('area_id');
	INSERT INTO @index_columns(column_name) VALUES ('band_id');
	INSERT INTO @index_columns(column_name) VALUES ('sex');
	INSERT INTO @index_columns(column_name) VALUES ('age_group');
	
	INSERT INTO @pk_index_columns(column_name) VALUES ('year');
	INSERT INTO @pk_index_columns(column_name) VALUES ('study_or_comparison');
	INSERT INTO @pk_index_columns(column_name) VALUES ('study_id');
	INSERT INTO @pk_index_columns(column_name) VALUES ('area_id');
	INSERT INTO @pk_index_columns(column_name) VALUES ('sex');
	INSERT INTO @pk_index_columns(column_name) VALUES ('age_group');
--
	DECLARE c5_creex CURSOR FOR
		SELECT column_name, column_comment
		  FROM @table_columns;
	DECLARE @c5_rec_column_name			VARCHAR(30);
	DECLARE @c5_rec_column_comment		VARCHAR(4000);
--		  
	DECLARE @ddl_stmts 	Sql_stmt_table;
	DECLARE @t_ddl		INTEGER=0;
	
/*	
	DECLARE @sql_frag 	VARCHAR(MAX);
--
	index_column	VARCHAR;
	table_column	VARCHAR;
	i		INTEGER:=0;
 */
--
	DECLARE @etime DATETIME, @stp DATETIME=GETDATE(), @etp DATETIME;
--
	DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @tab		VARCHAR(1)=CHAR(9);
	DECLARE @err_msg 	VARCHAR(MAX);
	DECLARE @msg	 	VARCHAR(MAX);

--
-- Use caller execution context to query RIF views
--
	EXECUTE AS CALLER /* RIF user */;
				
	OPEN c1_creex;
	FETCH NEXT FROM c1_creex INTO @c1_rec_study_id, @c1_rec_study_state, @c1_rec_extract_table, @c1_rec_extract_permitted, 
		@c1_rec_username, @c1_rec_description;
	IF @@CURSOR_ROWS = 0 BEGIN
		CLOSE c1_creex;
		DEALLOCATE c1_creex;
		SET @err_msg = formatmessage(55400, @study_id); -- Study ID %i not found
		THROW 55400, @err_msg, 1;
	END;
	
--
-- Check extract table does not exist
--
	IF @c1_rec_extract_table IS NULL BEGIN
		SET @err_msg = formatmessage(55401, @study_id); -- RIF40_STUDIES study %i extract table: not defined
		THROW 55401, @err_msg, 1;
	END;
	
	SET @schema_name=rif40.rif40_object_resolve(@c1_rec_extract_table);
	IF @schema_name IS NULL AND @c1_rec_study_state = 'V' 
		PRINT '[55402] RIF40_STUDIES study ' + CAST(@study_id AS VARCHAR) + ' extract table: ' + 
			@c1_rec_extract_table + ' defined, awaiting creation';
			
	ELSE IF @c1_rec_study_state != 'V' BEGIN
		SET @err_msg = formatmessage(55403, @study_id, @c1_rec_extract_table, @c1_rec_study_state); 
		-- RIF40_STUDIES study %i extract table: %s; in wrong state: %s
		THROW 55403, @err_msg, 1;
		END;
	ELSE BEGIN /* @schema_name IS NOT NULL */
		SET @err_msg = formatmessage(55404, @study_id, @c1_rec_extract_table, @schema_name); 
		-- RIF40_STUDIES study %i extract table: %s; exists in schema: %s
		THROW 55404, @err_msg, 1;
	END;    

--
-- Check extract is permitted
--
	IF @c1_rec_extract_permitted != 1 BEGIN
		SET @err_msg = formatmessage(55405, @study_id); 
		-- RIF40_STUDIES study %i extract not currently permitted [use RIF IG tool]
		THROW 55405, @err_msg, 1;
	END;

--
-- Check extract is being run by study owner
--
	IF @c1_rec_username != USER BEGIN
		SET @err_msg = formatmessage(55406, @study_id, @c1_rec_username /* Study owner */, USER); 
		-- RIF40_STUDIES study %i extract must be run by study owner %s not %s
		THROW 55406, @err_msg, 1;
	END;

--
-- Create extract table
--
-- The basis for this is the performance tests created from the new EHA extract for Lea in September 2012
-- The table has the following standard columns
--
--CREATE TABLE <extract_table> (
-- 	year                    SMALLINT 	NOT NULL,
--	study_or_comparison	VARCHAR(1) 	NOT NULL,
--	study_id		INTEGER 	NOT NULL,
-- 	area_id                 VARCHAR 	NOT NULL,
--	band_id			INTEGER,
-- 	sex                     SMALLINT,
-- 	age_group               VARCHAR,
-- 	total_pop               DOUBLE PRECISION,
--
	SET @sql_stmt='CREATE TABLE rif_studies.' + LOWER(@c1_rec_extract_table) + ' (' + @crlf +
		 	@tab + 'year                           SMALLINT    NOT NULL,' + @crlf +
			@tab + 'study_or_comparison            VARCHAR(1)  NOT NULL,' + @crlf +
			@tab + 'study_id                       INTEGER     NOT NULL,' + @crlf +
 			@tab + 'area_id                        VARCHAR(30) NOT NULL,' + @crlf +
			@tab + 'band_id                        INTEGER,' + @crlf +
 			@tab + 'sex                            SMALLINT,' + @crlf +
 			@tab + 'age_group                      SMALLINT,' + @crlf;
--
-- One column per distinct covariate
--
-- 	<rif40_inv_covariates>.<covariate_name>    VARCHAR,
--
	OPEN c2_creex;
	FETCH NEXT FROM c2_creex INTO @c2_rec_covariate_name;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @sql_stmt=@sql_stmt + @tab + LEFT(LOWER(@c2_rec_covariate_name) + REPLICATE(' ',30), 30) + ' VARCHAR(30),' + @crlf; 
		INSERT INTO @table_columns(column_name, column_comment) VALUES (@c2_rec_covariate_name, @c2_rec_covariate_name);
		INSERT INTO @pk_index_columns(column_name) VALUES (@c2_rec_covariate_name);
--
		FETCH NEXT FROM c2_creex INTO @c2_rec_covariate_name;
	END;
	CLOSE c2_creex;
	DEALLOCATE c2_creex;
	
--
-- One column per investigation
--
-- 	<rif40_investigations>.<inv_name>          VARCHAR);
--
-- [Make INV_1 INV_<inv_id> extracts] - this appears to be approximately the case; as it is INV_NAME, but the default needs to
-- be looked at
--
	OPEN c3_creex;
	FETCH NEXT FROM c3_creex INTO @c3_rec_inv_name, @c3_rec_inv_description;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @sql_stmt=@sql_stmt + @tab + LEFT(LOWER(@c3_rec_inv_name) + REPLICATE(' ',30), 30) + 
			' BIGINT,' + @crlf;	
		INSERT INTO @table_columns(column_name, column_comment) VALUES (@c3_rec_inv_name, @c3_rec_inv_description);
--
		FETCH NEXT FROM c3_creex INTO @c3_rec_inv_name, @c3_rec_inv_description;
	END;
	CLOSE c3_creex;
	DEALLOCATE c3_creex;
	SET @sql_stmt=@sql_stmt + @tab + 'total_pop                      DOUBLE PRECISION)';
--
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
--
-- Comment extract table and columns
--
	DECLARE @comment_text NVARCHAR(MAX)='Study ' + 
			CAST(@study_id AS VARCHAR) + ' extract: ' + COALESCE(@c1_rec_description, 'NO DESCRIPTION');
	SET @sql_stmt='sp_addextendedproperty' + @crlf +
'		@name = N''' + @comment_text + ''',' + @crlf +   
'		@value = N''Area Name field'',' + @crlf + 
'		@level0type = N''Schema'', @level0name = ''rif_studies'',' + @crlf +  
'		@level1type = N''Table'', @level1name = ''' + LOWER(@c1_rec_extract_table) + '''';
	SET @t_ddl=@t_ddl+1;	
	INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);
	
	OPEN c5_creex;
	FETCH NEXT FROM c5_creex INTO @c5_rec_column_name, @c5_rec_column_comment;
	WHILE @@FETCH_STATUS = 0
	BEGIN
--		i:=i+1;
		SET @sql_stmt='sp_addextendedproperty' + @crlf +
'		@name = N''' + @c5_rec_column_comment + ''',' + @crlf +   
'		@value = N''Area Name field'',' + @crlf + 
'		@level0type = N''Schema'', @level0name = ''rif_studies'',' + @crlf +  
'		@level1type = N''Table'', @level1name = ''' + LOWER(@c1_rec_extract_table) + ''',' + @crlf +
'    	@level2type = N''Column'', @level2name = ''' + LOWER(@c5_rec_column_name) + '''';			
		SET @t_ddl=@t_ddl+1;	
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);	
--
		FETCH NEXT FROM c5_creex INTO @c5_rec_column_name, @c5_rec_column_comment;
	END;
	CLOSE c5_creex;
	DEALLOCATE c5_creex;

--
-- Grant to study owner (USER), all GRANTEE_USERNAME in  rif40_study_shares if extract_permitted=1 
--
	IF @c1_rec_extract_permitted = 1 BEGIN
		SET @sql_stmt='GRANT SELECT,INSERT,DELETE ON rif_studies.' + LOWER(@c1_rec_extract_table) + ' TO ' + USER;	
		SET @t_ddl=@t_ddl+1;	
		INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);	
		OPEN c4_creex;
		FETCH NEXT FROM c4_creex INTO @c4_rec_grantee_username;
		WHILE @@FETCH_STATUS = 0
		BEGIN
			SET @sql_stmt='GRANT SELECT,INSERT ON rif_studies.' + LOWER(@c1_rec_extract_table) + 
				' TO ' + @c4_rec_grantee_username;	
			SET @t_ddl=@t_ddl+1;	
			INSERT INTO @ddl_stmts(sql_stmt) VALUES (@sql_stmt);	
--
			FETCH NEXT FROM c4_creex INTO @c4_rec_grantee_username;
		END;
		CLOSE c4_creex;
		DEALLOCATE c4_creex;
	END;
 
	REVERT;	/* Revert to procedure owner context (RIF40) to create tables */
--
-- Create extract table
--
	EXECUTE rif40.rif40_ddl
			@rval		/* Result: 0/1 */,
			@ddl_stmts	/* SQL table */,
			@debug		/* enable debug: 0/1) */;

--
-- Use caller execution context to INSERT extract data
--
	EXECUTE AS CALLER /* RIF user */;
 
--
-- Call rif40_insert_extract() to populate extract table.
-- 
	EXECUTE rif40.rif40_insert_extract 
		@rval, 
		@c1_rec_study_id,
		@debug;
	IF @rval = 0 BEGIN
			SET @msg='[55408] RIF40_STUDIES study ' + CAST(@c1_rec_study_id AS VARCHAR) +
				' populated extract failed, see previous warnings'	/* Study id */;
			PRINT @msg;
			RETURN @rval;
		END; 
	ELSE BEGIN
		SET @msg = 'Study ' + CAST(@c1_rec_study_id AS VARCHAR) + ' OK';
		PRINT @msg;
	END;
	
	REVERT;	/* Revert to procedure owner context (RIF40) to create indexes */
	/*
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
	IF c1_rec.study_type != '1' THEN /- study type: 1 - disease mapping -/
		sql_frag:=sql_frag||',band_id'; /- Risk analysis only -/
	END IF;
	sql_stmt:='ALTER TABLE rif_studies.'||LOWER(c1_rec.extract_table)||
		' ADD CONSTRAINT '||LOWER(c1_rec.extract_table)||'_pk PRIMARY KEY ('||sql_frag||')';
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;
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
			c1_rec.study_id::VARCHAR);
		RETURN FALSE;
	END IF;
--
	RETURN TRUE;
 */	
	SET @rval=1; 	-- Success
	RETURN @rval;
END;
GO

--
-- Eof