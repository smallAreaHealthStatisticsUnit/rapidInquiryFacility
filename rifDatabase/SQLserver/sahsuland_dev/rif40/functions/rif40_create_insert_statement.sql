
-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study - create insert statement
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
-- sqlcmd -U rif40 -P rif40 -d sahsuland -b -m-1 -e -r1 -i rif40_create_insert_statement.sql
--
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_create_insert_statement]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_create_insert_statement]
GO 

CREATE PROCEDURE [rif40].[rif40_create_insert_statement](
	@study_id 				INTEGER, 
	@study_or_comparison 	VARCHAR(1), 
	@year_start 			INTEGER=NULL, 
	@year_stop 				INTEGER=NULL, 
	@debug 					INTEGER=0)
AS
BEGIN
/*
Function:	rif40_create_insert_statement()
Parameter:	Study ID, study or comparison (S/C), year_start, year_stop, debug
Returns:	Success or failure [INTEGER]
Description:	Create INSERT SQL statement
 */

--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;
	
	DECLARE @rval 	INTEGER=1; 	-- Success
	
	DECLARE c1insext CURSOR FOR
		SELECT study_id, extract_table, comparison_geolevel_name, study_geolevel_name, min_age_group, max_age_group, denom_tab,
		       study_type
		  FROM rif40_studies a
		 WHERE a.study_id = @study_id;
	DECLARE @c1_rec_study_id 					INTEGER;
	DECLARE @c1_rec_extract_table 				VARCHAR(30);
	DECLARE @c1_rec_comparison_geolevel_name 	VARCHAR(30);
	DECLARE @c1_rec_study_geolevel_name 		VARCHAR(30);
	DECLARE @c1_rec_min_age_group 				INTEGER;
	DECLARE @c1_rec_max_age_group 				INTEGER;
	DECLARE @c1_rec_denom_tab					VARCHAR(30);
	DECLARE @c1_rec_study_type					INTEGER;
	
	DECLARE c3insext CURSOR FOR
		SELECT COUNT(DISTINCT(numer_tab)) AS distinct_numerators
		  FROM rif40_investigations a
		 WHERE a.study_id = @study_id;
	DECLARE @c3_rec_distinct_numerators 		INTEGER;
	DECLARE c4insext CURSOR FOR
		WITH b AS (
			SELECT DISTINCT(a.numer_tab) AS numer_tab
			  FROM rif40_investigations a
		 	 WHERE a.study_id   = @study_id
		)
		SELECT b.numer_tab,
 		       t.description, t.age_sex_group_field_name, t.year_start, t.year_stop, t.total_field /*, 
   		       t.sex_field_name, t.age_group_field_name, t.age_group_id */
		  FROM b, rif40_tables t 
		 WHERE t.table_name = b.numer_tab;
	DECLARE @c4_rec_numer_tab 					VARCHAR(30);
	DECLARE @c4_rec_description 				VARCHAR(2000);
	DECLARE @c4_rec_age_sex_group_field_name 	VARCHAR(30);
	DECLARE @c4_rec_year_start					INTEGER;
	DECLARE	@c4_rec_year_stop 					INTEGER;
	DECLARE @c4_rec_total_field				 	VARCHAR(30);
		
	DECLARE c7insext CURSOR FOR
		SELECT DISTINCT a.covariate_name AS covariate_name, b.covariate_table AS covariate_table_name
		  FROM rif40_inv_covariates a
				LEFT OUTER JOIN rif40_geolevels b ON (a.study_geolevel_name = b.geolevel_name)
		 WHERE a.study_id = @study_id
 		 ORDER BY a.covariate_name;
	DECLARE @c7_rec_covariate_name	 			VARCHAR(30);	
	DECLARE @c7_rec_covariate_table_name	 	VARCHAR(30);	 

	DECLARE c8insext CURSOR FOR
		SELECT b.denom_tab,
 		       COALESCE(t.description, 'N/A') AS description, t.total_field, t.year_start, t.year_stop,
   		       t.sex_field_name, t.age_group_field_name, t.age_sex_group_field_name, t.age_group_id, 
		       MIN(a.offset) AS min_age_group, MAX(a.offset) AS max_age_group
		  FROM rif40_studies b, rif40_tables t, rif40_age_groups a
		 WHERE t.table_name   = b.denom_tab
		   AND t.age_group_id = a.age_group_id
		   AND b.study_id     = @study_id
		 GROUP BY b.denom_tab,
 		       t.description, t.total_field, t.year_start, t.year_stop,
   		       t.sex_field_name, t.age_group_field_name, t.age_sex_group_field_name, t.age_group_id;
	DECLARE @c8_rec_denom_tab					VARCHAR(30);
	DECLARE @c8_rec_description					VARCHAR(2000);
	DECLARE @c8_rec_total_field					VARCHAR(30);
	DECLARE @c8_rec_year_start					INTEGER;
	DECLARE @c8_rec_year_stop					INTEGER;
	DECLARE @c8_rec_sex_field_name				VARCHAR(30);
	DECLARE @c8_rec_age_group_field_name		VARCHAR(30);
	DECLARE @c8_rec_age_sex_group_field_name	VARCHAR(30);
	DECLARE @c8_rec_age_group_id				INTEGER;		
	DECLARE @c8_rec_min_age_group				INTEGER;		
	DECLARE @c8_rec_max_age_group				INTEGER;		   
	
	DECLARE @covariate_table_name				VARCHAR(30);
	DECLARE @covariate_list						VARCHAR(2000);
	DECLARE @covariate_filter					VARCHAR(2000);

	DECLARE @inv_array 		TABLE (inv VARCHAR(MAX));
	DECLARE @inv_join_array TABLE (outer_join VARCHAR(MAX));
	DECLARE c9_inv_array CURSOR FOR
		SELECT inv
		  FROM @inv_array;
	DECLARE @c9_rec_inv			VARCHAR(MAX);
	DECLARE c10_inv_join_array CURSOR FOR
		SELECT outer_join
		  FROM @inv_join_array;
	DECLARE @c10_rec_outer_join		VARCHAR(MAX);
--	
	DECLARE @sql_stmt	NVARCHAR(MAX);
	DECLARE @filter_sql	NVARCHAR(MAX);
--
	DECLARE @i			INTEGER=0;
	DECLARE @j			INTEGER=0;
	DECLARE @k			INTEGER=0;
--
	DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @tab		VARCHAR(1)=CHAR(9);
	DECLARE @err_msg 	VARCHAR(MAX);
	DECLARE @msg	 	VARCHAR(MAX);
	
	DECLARE @areas_table	VARCHAR(30)='##g_rif40_study_areas';
/*
SELECT study_id, area_id, band_id
INTO ##me
FROM rif40.rif40_study_areas;
SELECT TOP 10 * FROM ##me;
GO
IF OBJECT_ID('tempdb..##me') IS NOT NULL 
DROP TABLE ##me;
GO
SELECT study_id, area_id, band_id
INTO ##me
FROM rif40.rif40_study_areas;
SELECT TOP 10 * FROM ##me;
GO
 */
--
-- Use different areas_table for comparison (it has no band_id)
--	
	IF @study_or_comparison = 'C' 
		SET @areas_table='##g_rif40_comparison_areas';
--
	OPEN c1insext;	
	FETCH NEXT FROM c1insext INTO @c1_rec_study_id, @c1_rec_extract_table, 
		@c1_rec_comparison_geolevel_name, @c1_rec_study_geolevel_name, @c1_rec_min_age_group, @c1_rec_max_age_group, 
		@c1_rec_denom_tab, @c1_rec_study_type;
	IF @@CURSOR_ROWS = 0 BEGIN
		CLOSE c1insext;
		DEALLOCATE c1insext;
		SET @err_msg = formatmessage(56000, @study_id); -- Study ID %i not found
		THROW 56000, @err_msg, 1;
	END;
	CLOSE c1insext;
	DEALLOCATE c1insext;
	
	OPEN c7insext;
	FETCH NEXT FROM c7insext INTO @c7_rec_covariate_name, @c7_rec_covariate_table_name;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		IF @c7_rec_covariate_table_name IS NULL BEGIN
			CLOSE c7insext;
			DEALLOCATE c7insext;
			SET @err_msg = formatmessage(56006, @study_id); 	-- Study ID %i NULL covariate table
			THROW 56006, @err_msg, 1;
			END;
		ELSE IF @covariate_table_name IS NULL SET @covariate_table_name=@c7_rec_covariate_table_name;
			/* Only one covariate table is supported */ 
		ELSE IF @covariate_table_name != @c7_rec_covariate_table_name BEGIN 
			CLOSE c7insext;
			DEALLOCATE c7insext;
			SET @err_msg = formatmessage(56007, @study_id, @covariate_table_name, @c7_rec_covariate_table_name); 
				-- Study ID %i multiple covariate tables: %s, %s
			THROW 56007, @err_msg, 1;	
		END;
--
-- Covariates, if present are required at both study and comparison geolevels
-- So, do NOT remove the covariates or you will treated to an INLA R crash
 --		
		SET @k=@k+1;
		IF @covariate_list IS NULL SET @covariate_list=@tab + '       c1.' + LOWER(@c7_rec_covariate_name) + ',' + @crlf;
		ELSE SET @covariate_list=@covariate_list + @tab + '       c1.' + LOWER(@c7_rec_covariate_name) + ',' + @crlf;
--
		FETCH NEXT FROM c7insext INTO @c7_rec_covariate_name, @c7_rec_covariate_table_name;
	END; /* Loop k: c7insext */
	CLOSE c7insext;
	
	SET @sql_stmt='';
--
-- Start with CTE (WITH)
--
	
--
-- Get denominator setup
--
	OPEN c8insext;
	FETCH NEXT FROM c8insext INTO @c8_rec_denom_tab, @c8_rec_description, @c8_rec_total_field, @c8_rec_year_start, @c8_rec_year_stop,
   		       @c8_rec_sex_field_name, @c8_rec_age_group_field_name, @c8_rec_age_sex_group_field_name, @c8_rec_age_group_id,
			   @c8_rec_min_age_group, @c8_rec_max_age_group;
	IF @@CURSOR_ROWS = 0 BEGIN
		CLOSE c8insext;
		DEALLOCATE c8insext;
		SET @err_msg = formatmessage(56000, @study_id); -- Study ID %i not found
		THROW 56000, @err_msg, 1;
	END;
	CLOSE c8insext;
	DEALLOCATE c8insext;

--
-- Loop through distinct numerators
--
	SET @i=0;
	OPEN c4insext;
	FETCH NEXT FROM c4insext INTO @c4_rec_numer_tab, @c4_rec_description, @c4_rec_age_sex_group_field_name, 
		@c4_rec_year_start, @c4_rec_year_stop, @c4_rec_total_field;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @filter_sql='';
		SET @i=@i+1;
		
--
-- Open WITH clause (common table expression)
-- 
		IF @i = 1 SET @sql_stmt=@sql_stmt + 'WITH n' + CAST(@i AS VARCHAR) + ' AS (' + @tab + 
			'/* ' + @c4_rec_numer_tab + ' - ' + @c4_rec_description + ' */' + @crlf
		ELSE SET @sql_stmt=@sql_stmt + @tab + 'n' + CAST(@i AS VARCHAR) + ' AS (' + @tab + 
			'/* ' + @c4_rec_numer_tab + ' - ' + @c4_rec_description + ' */' + @crlf;
			
--
-- Numerator JOINS
--
		INSERT INTO @inv_join_array(outer_join) VALUES (@tab + 'LEFT OUTER JOIN n' + CAST(@i AS VARCHAR) + ' ON ( ' + @crlf +
			@tab + '/* rif_data.' + LOWER(@c4_rec_numer_tab) + ' - ' + @c4_rec_description + ' */' + @crlf +
			@tab + @tab + '    d.area_id'+ @tab + @tab + ' = n' + CAST(@i AS VARCHAR) + '.area_id' + @crlf +
			@tab + @tab + 'AND d.year' + @tab + @tab + ' = n' + CAST(@i AS VARCHAR) + '.year' + @crlf +
--
-- [Add conversion support for differing age/sex/group names; convert to AGE_SEX_GROUP]
--
			@tab + @tab + 'AND d.' + LOWER(@c8_rec_age_sex_group_field_name) + @tab + ' = n' + CAST(@i AS VARCHAR) + '.n_age_sex_group)');
				/* List of numerator joins (for use in FROM clause) */;
			
		OPEN c7insext;
		FETCH NEXT FROM c7insext INTO @c7_rec_covariate_name, @c7_rec_covariate_table_name;
		WHILE @@FETCH_STATUS = 0
		BEGIN
			INSERT INTO @inv_join_array(outer_join) VALUES (
				@tab + @tab + 'AND d.' + LOWER(@c7_rec_covariate_name) +
					' = n' + CAST(@i AS VARCHAR) + '.' + LOWER(@c7_rec_covariate_name));
--
			FETCH NEXT FROM c7insext INTO @c7_rec_covariate_name, @c7_rec_covariate_table_name;
		END; /* Loop k: c7insext */
		CLOSE c7insext;
		DEALLOCATE c7insext;
	
		SET @sql_stmt=@sql_stmt + @tab + 'SELECT s.area_id' + @tab + @tab + '/* Study or comparision resolution */,' + @crlf;
		IF @covariate_list IS NOT NULL SET @sql_stmt=@sql_stmt + @tab + '       ' + @covariate_list;
		SET @sql_stmt=@sql_stmt + @tab + '       c.year,' + @crlf;
--
-- [Add support for differing age/sex/group names; convert to AGE_SEX_GROUP]
--
		SET @sql_stmt=@sql_stmt + @tab + '       c.' + LOWER(@c4_rec_age_sex_group_field_name) + ' AS n_age_sex_group,' + @crlf;				

--
-- Individual investigations [add age group/sex/year filters]
-- 
		DECLARE c5insext CURSOR FOR
			SELECT inv_id, inv_name, year_start, year_stop, genders, min_age_group, max_age_group, inv_description
			  FROM rif40_investigations a
			 WHERE a.study_id  = @study_id
			   AND a.numer_tab = @c4_rec_numer_tab
			 ORDER BY inv_id;
		DECLARE @c5_rec_inv_id			INTEGER;
		DECLARE @c5_rec_year_start		INTEGER;
		DECLARE	@c5_rec_year_stop 		INTEGER;
		DECLARE @c5_rec_inv_name		VARCHAR(20);
		DECLARE @c5_rec_genders			INTEGER;
		DECLARE @c5_rec_min_age_group	INTEGER;
		DECLARE @c5_rec_max_age_group	INTEGER;
		DECLARE @c5_rec_inv_description VARCHAR(2000);
		DECLARE @single_gender			INTEGER = NULL;
		DECLARE @single_gender_flag		INTEGER = 0;
--	
		OPEN c5insext;
		FETCH NEXT FROM c5insext INTO @c5_rec_inv_id, @c5_rec_inv_name, @c5_rec_year_start, @c5_rec_year_stop, @c5_rec_genders,
			@c5_rec_min_age_group, @c5_rec_max_age_group, @c5_rec_inv_description;
		WHILE @@FETCH_STATUS = 0
		BEGIN	
			IF @single_gender IS NULL BEGIN
					SET @single_gender=@c5_rec_genders;
					SET @single_gender_flag=1;
				END;
			ELSE IF @single_gender = @c5_rec_genders SET @single_gender_flag=1
			ELSE SET @single_gender_flag=0;
--		
			SET @j=@j+1;
			INSERT INTO @inv_array(inv) VALUES('       COALESCE(' + 
				'n' + CAST(@i AS VARCHAR) + '.inv_' + CAST(@c5_rec_inv_id AS VARCHAR) + '_' + LOWER(@c5_rec_inv_name) +
				', 0) AS inv_' + CAST(@c5_rec_inv_id AS VARCHAR) + '_' + LOWER(@c5_rec_inv_name)); 
				/* List of investigations (for use in final SELECT) */
			IF @j > 1 SET @sql_stmt=@sql_stmt + ',' + @crlf;
			SET @sql_stmt=@sql_stmt + @tab + '       SUM(CASE ' + @tab + @tab + '/* Numerators - can overlap */' + @crlf +
				@tab + @tab + @tab + 'WHEN ((' + @tab + @tab + '/* Investigation ' + CAST(@j AS VARCHAR) + ' ICD filters */' + @crlf;

--
-- Add conditions
--
			DECLARE c6insext CURSOR FOR
				SELECT condition
				  FROM rif40_inv_conditions a
				 WHERE a.study_id = @study_id
				   AND a.inv_id   = @c5_rec_inv_id
				 ORDER BY inv_id, line_number;	
			DECLARE @c6_rec_condition	VARCHAR(2000);
--	
			SET @k=0;
			OPEN c6insext;
			FETCH NEXT FROM c6insext INTO @c6_rec_condition;
			WHILE @@FETCH_STATUS = 0
			BEGIN	
				SET @k=@k+1;
				IF @k = 1 BEGIN
						SET @filter_sql=@filter_sql + @tab + @tab + @tab + '    ' + @c6_rec_condition +
							' /* Filter ' + CAST(@k AS VARCHAR) + ' */' + @crlf;
						SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + '    ' + @c6_rec_condition +
							' /* Filter ' + CAST(@k AS VARCHAR) + ' */' + @crlf;
					END;
				ELSE BEGIN	
						SET @filter_sql=@filter_sql + @tab + @tab + @tab + ' OR ' + @c6_rec_condition +
							' /* Filter ' + CAST(@k AS VARCHAR) + ' */'  + @crlf;
						SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + ' OR ' + @c6_rec_condition +
							' /* Filter ' + CAST(@k AS VARCHAR) + ' */'  + @crlf;
					END;
--
				FETCH NEXT FROM c6insext INTO @c6_rec_condition;
			END; /* Loop k: c6insext */
			CLOSE c6insext;
			DEALLOCATE c6insext;	
			SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + ') /* ' + CAST(@k AS VARCHAR) + ' lines of conditions: study: ' +
				CAST(@study_id AS VARCHAR) + ', inv: ' + CAST(@c5_rec_inv_id AS VARCHAR) + ' */' + @crlf +
				@tab + @tab + @tab + 'AND (1=1' + @crlf;	
				
--
-- Processing years filter [commented out in Postgres]
--
/*			IF year_start = year_stop THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND (c.year = @yearstart'||E'\t'||'-* Denominator (INSERT) year filter *-'||E'\n';
			ELSE
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND (c.year BETWEEN @yearstart AND @yearstop'||E'\t'||'-* Denominator (INSERT) year filter *-'||E'\n';
			END IF; */		

--
-- Investigation filters: year, age group, genders
--
			IF @c5_rec_year_start = @c5_rec_year_stop SET @sql_stmt=@sql_stmt + @tab + @tab + @tab +
				'   AND  c.year = ' + CAST(@c5_rec_year_start AS VARCHAR) + @crlf
			ELSE IF @c4_rec_year_start = @c5_rec_year_start AND @c4_rec_year_stop = @c5_rec_year_stop SET @sql_stmt=@sql_stmt +
				@tab + @tab + @tab + @tab + '        /* No year filter required for investigation ' + CAST(@j AS VARCHAR) + ' */' + @crlf
			ELSE SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + '   AND  c.year BETWEEN ' + CAST(@c5_rec_year_start AS VARCHAR) +
					' AND ' + CAST(@c5_rec_year_stop AS VARCHAR) + 
					' /* Investigation ' + CAST(@j AS VARCHAR) + ' year filter */' + @crlf;
		
			IF @c5_rec_genders = 3 SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab + 
				'        /* No genders filter required for investigation  ' + CAST(@j AS VARCHAR) + ' */' + @crlf
			ELSE SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + 
				'   AND  FLOOR(c.' + LOWER(@c4_rec_age_sex_group_field_name) + '/100) = ' + 
					CAST(@c5_rec_genders AS VARCHAR) + '/* Investigation ' + CAST(@j AS VARCHAR) + ' gender filter */' + @crlf;
		
			IF @c8_rec_min_age_group = @c5_rec_min_age_group AND @c8_rec_max_age_group = @c5_rec_max_age_group SET @sql_stmt=@sql_stmt +
				@tab + @tab + @tab + @tab + 
				'        /* No age group filter required for investigation ' + CAST(@j AS VARCHAR) + ' */)' + @crlf
			ELSE SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab + 
				'   AND (c.' + LOWER(@c4_rec_age_sex_group_field_name) + ' % 100) BETWEEN ' + 
				CAST(@c5_rec_min_age_group AS VARCHAR) + ' AND ' + CAST(@c5_rec_max_age_group AS VARCHAR) +
				' /* Investigation ' + CAST(@j AS VARCHAR) + ' age group filter */)' + @crlf;
--
			IF @c4_rec_total_field IS NULL /* Handle total fields */ SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + ') THEN 1' + @crlf
			ELSE SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + ') THEN ' + LOWER(@c4_rec_total_field) + @crlf;
--
			SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + 'ELSE 0' + @crlf +
				@tab + '       END) inv_' + CAST(@c5_rec_inv_id AS VARCHAR) + '_' + LOWER(@c5_rec_inv_name) +
				@tab + '/* Investigation ' + CAST(@j AS VARCHAR) + ' - ' + @c5_rec_inv_description + ' */ ';			
--
			FETCH NEXT FROM c5insext INTO @c5_rec_inv_id, @c5_rec_inv_name, @c5_rec_year_start, @c5_rec_year_stop, @c5_rec_genders,
				@c5_rec_min_age_group, @c5_rec_max_age_group, @c5_rec_inv_description;
		END; /* Loop j: c5insext */
		CLOSE c5insext;
		DEALLOCATE c5insext;
		
--
-- Check at least one investigation
--
		IF @j = 0 BEGIN
			CLOSE c4insext;
			DEALLOCATE c4insext;
			SET @err_msg = formatmessage(56004, @study_id, @c4_rec_numer_tab); 
				-- Study ID %i no investigations created: distinct numerator: %s
			THROW 56004, @err_msg, 1;
		END;
		SET @sql_stmt=@sql_stmt + @crlf;

--
-- From clause
--
		SET @sql_stmt=@sql_stmt + @tab + 
				'  FROM ' + @areas_table + ' s, ' + @tab + 
					'/* Numerator study or comparison area to be extracted */' + @crlf +
				@tab + 
				'       rif_data.' + LOWER(@c4_rec_numer_tab) + ' c' + @tab + 
					'/* ' + @c4_rec_description + ' */' + @crlf
		IF @covariate_table_name IS NOT NULL BEGIN
			SET @sql_stmt=@sql_stmt + @tab + @tab + @tab +
				'LEFT OUTER JOIN rif_data.' + LOWER(@covariate_table_name) + ' c1 ON (' + @tab + '/* Covariates */' + @crlf;
--
-- This is joining at the study geolevel. This needs to be aggregated to the comparison area
--
			SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab +
				'    c.' + LOWER(@c1_rec_study_geolevel_name) +
				' = c1.' + LOWER(@c1_rec_study_geolevel_name) + @tab + @tab + '/* Join at study geolevel */' + @crlf;
			SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab +
				'AND c.year = c1.year)' + @crlf; /* Was @yearstart - may cause a performance problem */
		END;

		IF @study_or_comparison = 'C' SET @sql_stmt=@sql_stmt + @tab + 
			' WHERE c.' + LOWER(@c1_rec_comparison_geolevel_name) + ' = s.area_id ' + @tab + '/* Comparison selection */' + @crlf
		ELSE SET @sql_stmt=@sql_stmt + @tab + ' WHERE c.' + LOWER(@c1_rec_study_geolevel_name) + ' = s.area_id ' + 
			@tab + '/* Study selection */' + @crlf;
			
		IF @filter_sql IS NOT NULL SET @sql_stmt=@sql_stmt + @tab + '   AND (' + @crlf + @filter_sql + ')' + @crlf
		ELSE SET @sql_stmt=@sql_stmt + @tab + '/* No filter */' + @crlf;
		
--
-- Add correct age_sex_group limits
--
		IF @single_gender_flag = 0 SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab + 
			'        /* No genders filter required for numerator (multiple genders used) */' + @crlf
		ELSE IF @single_gender = 3 SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab + 
			'        /* No genders filter required for numerator (only one gender used) */' + @crlf
		ELSE SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + 
			'   AND  FLOOR(c.' + LOWER(@c8_rec_age_sex_group_field_name) + '/100) = ' + 
				CAST(@single_gender AS VARCHAR) + '       /* Numerator gender filter */' + @crlf;
					
		IF @c8_rec_min_age_group = @c1_rec_min_age_group AND @c8_rec_max_age_group = @c1_rec_max_age_group SET @sql_stmt=@sql_stmt + 
			@tab + '       /* No age group filter required for numerator */' + @crlf
		ELSE SET @sql_stmt=@sql_stmt + @tab + '   AND (c.' + LOWER(@c8_rec_age_sex_group_field_name) + ' % 100) BETWEEN ' +
				CAST(@c1_rec_min_age_group AS VARCHAR) + ' AND ' + CAST(@c1_rec_max_age_group AS VARCHAR) +
				' /* Numerator age group filter */' + @crlf;
		SET @sql_stmt=@sql_stmt + @tab + '   AND s.study_id = @studyid' + @tab + @tab + '/* Current study ID */' + @crlf;

--
-- Processing years filter
--
		IF @year_start = @year_stop SET @sql_stmt=@sql_stmt + @tab + '   AND c.year = @yearstart' + @tab + @tab + 
			' /* Numerator (INSERT) year filter */' + @crlf;
		ELSE SET @sql_stmt=@sql_stmt + @tab + '   AND c.year BETWEEN @yearstart AND @yearstop' + @tab + 
			' /* Numerator (INSERT) year filter */' + @crlf;

--
-- Group by clause
-- [Add support for differing age/sex/group names]
--		
		IF @study_or_comparison = 'C' SET @sql_stmt=@sql_stmt + @tab + ' GROUP BY c.year, s.area_id,' + @crlf
 		ELSE SET @sql_stmt=@sql_stmt + @tab + ' GROUP BY c.year, s.area_id, s.band_id,' + @crlf;
		IF @covariate_list IS NOT NULL SET @sql_stmt=@sql_stmt + @tab + '          ' + @covariate_list;
		SET @sql_stmt=@sql_stmt + @tab + '          c.' + LOWER(@c4_rec_age_sex_group_field_name) + @crlf;

--
-- Close WITH clause (common table expression)
-- 
		SET @sql_stmt=@sql_stmt + @tab + ') /* ' + @c4_rec_numer_tab + ' - ' + @c4_rec_description + ' */,' + @crlf;
		
--
		FETCH NEXT FROM c4insext INTO @c4_rec_numer_tab, @c4_rec_description, @c4_rec_age_sex_group_field_name, 
			@c4_rec_year_start, @c4_rec_year_stop, @c4_rec_total_field;
	END; /* Loop i: c4insext */
	CLOSE c4insext;
	DEALLOCATE c4insext;

	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: A';
--
-- Denominator CTE with covariates joined at study geolevel
--
/* /e.g. 
, d AS (
        SELECT d1.year, s.area_id, NULL::INTEGER AS band_id, d1.age_sex_group, 
	       c.ses, 
	       SUM(COALESCE(d1.total, 0)) AS total_pop
          FROM g_rif40_comparison_areas s, sahsuland_pop d1     /- Study or comparison area to be extracted -/
  	      LEFT OUTER JOIN sahsuland_covariates_level4 c ON (        /- Covariates -/
     	               d1.level2 = c.level2				/- Join at study geolevel -/
     	           AND c.year    = @yearstart)
         WHERE d1.year    = @yearstart          /- Denominator (INSERT) year filter -/
           AND s.area_id  = d1.level2   /- Comparison geolevel join -/
           AND s.area_id  IS NOT NULL   /- Exclude NULL geolevel -/
           AND s.study_id = @studyid   /- Current study ID -/
               /- No age group filter required for denominator -/
         GROUP BY d1.year, s.area_id, d1.age_sex_group, c.ses
)
 */
	SET @sql_stmt=@sql_stmt + @tab + 'd AS (' + @crlf;
	IF @study_or_comparison = 'C' SET @sql_stmt=@sql_stmt + @tab + 'SELECT d1.year, s.area_id, CAST(NULL AS INTEGER) AS band_id, d1.'+
			LOWER(@c8_rec_age_sex_group_field_name) + ',' + @crlf
	ELSE IF @c1_rec_study_type != 1 /* Risk analysis */ SET @sql_stmt=@sql_stmt + 
		@tab + 'SELECT d1.year, s.area_id, s.band_id,' + @crlf + 
		@tab + @tab + 's.intersect_count, s.distance_from_nearest_source, s.nearest_rifshapepolyid, s.exposure_value, d1.' +
			LOWER(@c8_rec_age_sex_group_field_name) + ',' + @crlf;
	ELSE SET @sql_stmt=@sql_stmt + @tab + 'SELECT d1.year, s.area_id, s.band_id, d1.' +
			LOWER(@c8_rec_age_sex_group_field_name) + ',' + @crlf;

	IF @covariate_list IS NOT NULL SET @sql_stmt=@sql_stmt + @tab + '          ' + @covariate_list + @crlf;
	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: B';
	
	SET @sql_stmt=@sql_stmt + @tab + '       SUM(COALESCE(d1.'+ coalesce(LOWER(@c8_rec_total_field), 'total') + 
		', 0)) AS total_pop' + @crlf + @tab + '  FROM ' + @areas_table + ' s, rif_data.' +
		LOWER(@c1_rec_denom_tab) + ' d1 ' + @tab + '/* Denominator study or comparison area to be extracted */' + @crlf;
--
-- This is joining at the study geolevel. This needs to be aggregated to the comparison area
--			
	IF @covariate_table_name IS NOT NULL BEGIN
		SET @sql_stmt=@sql_stmt + @tab + @tab + @tab +
			'LEFT OUTER JOIN rif_data.' + LOWER(@covariate_table_name) + ' c1 ON (' + @tab + '/* Covariates */' + @crlf;
--
-- This is joining at the study geolevel. This needs to be aggregated to the comparison area
--
		SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab +
			'    d1.' + LOWER(@c1_rec_study_geolevel_name) +
			' = c1.' + LOWER(@c1_rec_study_geolevel_name) + @tab + @tab + '/* Join at study geolevel */' + @crlf;
		SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab +
			'AND d1.year = c1.year)' + @crlf; /* Was @yearstart - may cause a performance problem */
	END;

	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: C';	
	
	IF @year_start = @year_stop SET @sql_stmt=@sql_stmt + @tab + ' WHERE d1.year = @yearstart' + @tab + @tab + 
		' /* Denominator (INSERT) year filter */' + @crlf
	ELSE SET @sql_stmt=@sql_stmt + @tab + ' WHERE d1.year BETWEEN @yearstart AND @yearstop' + @tab + 
		' /* Denominator (INSERT) year filter */' + @crlf;
			
	IF @study_or_comparison = 'C' SET @sql_stmt=@sql_stmt + @tab + 
		'   AND s.area_id  = d1.'+ LOWER(@c1_rec_comparison_geolevel_name) + @tab + '/* Comparison geolevel join */' + @crlf
	ELSE SET @sql_stmt=@sql_stmt + @tab + 
		'   AND s.area_id  = d1.' + LOWER(@c1_rec_study_geolevel_name) + @tab + '/* Study geolevel join */' + @crlf;
	SET @sql_stmt=@sql_stmt + @tab + '   AND s.area_id  IS NOT NULL' + @tab + '/* Exclude NULL geolevel */' + @crlf +
		@tab + '   AND s.study_id = @studyid' + @tab + @tab + '/* Current study ID */' + @crlf;

--
-- Add correct age_sex_group limits
--

--
-- Note that the gender filter causes R to blob. This section is also commented out in the Postgres port
--
--	IF @single_gender_flag = 0 SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab + 
--		'        /* No genders filter required for denominator (multiple genders used) */' + @crlf
--	ELSE IF @single_gender = 3 SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + @tab + 
--		'        /* No genders filter required for denominator (only one gender used) */' + @crlf
--	ELSE SET @sql_stmt=@sql_stmt + @tab + @tab + @tab + 
--		'   AND  FLOOR(d1.' + LOWER(@c8_rec_age_sex_group_field_name) + '/100) = ' + 
--			CAST(@single_gender AS VARCHAR) + '       /* Denominator gender filter */' + @crlf;
				
	IF @c8_rec_min_age_group = @c1_rec_min_age_group AND @c8_rec_max_age_group = @c1_rec_max_age_group SET @sql_stmt=@sql_stmt + @tab + 
		'       /* No age group filter required for denominator */' + @crlf
	ELSE SET @sql_stmt=@sql_stmt + @tab + 
		'   AND (d1.' + LOWER(@c8_rec_age_sex_group_field_name) + ' % 100) BETWEEN ' + 
			CAST(@c1_rec_min_age_group AS VARCHAR) + ' AND ' + CAST(@c1_rec_max_age_group AS VARCHAR) +
			' /* Denominator age group filter */' + @crlf;
--
-- [Add gender filter]
--

	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: D';
--
-- Add GROUP BY clause 
-- [Add support for differing age/sex/group names]
--
	IF @study_or_comparison = 'C' SET @sql_stmt=@sql_stmt + 
		@tab + ' GROUP BY d1.year, s.area_id,' + @crlf;
	ELSE IF @c1_rec_study_type != 1 /* Risk analysis */ SET @sql_stmt=@sql_stmt + 
		@tab + ' GROUP BY d1.year, s.area_id, s.band_id,' + @crlf + 
		@tab + @tab + 's.intersect_count, s.distance_from_nearest_source, s.nearest_rifshapepolyid, s.exposure_value,' + @crlf;
	ELSE SET @sql_stmt=@sql_stmt + 
		@tab + ' GROUP BY d1.year, s.area_id, s.band_id,' + @crlf;
	IF @covariate_list IS NOT NULL SET @sql_stmt=@sql_stmt + @tab + '          ' + @covariate_list;
	SET @sql_stmt=@sql_stmt + @tab + '          d1.' + LOWER(@c4_rec_age_sex_group_field_name) + @crlf;
		
	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: D1';

	SET @sql_stmt=@sql_stmt + @crlf + ') /* End of denominator */' + @crlf;

	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: E';		
--	
-- Add INSERT
--
	SET @sql_stmt=@sql_stmt + 'INSERT INTO rif_studies.' + LOWER(@c1_rec_extract_table) + ' (' + @crlf;	

--
-- Add INSERT columns
-- 
	DECLARE c2insext CURSOR FOR
		SELECT column_name
		  FROM information_schema.columns a
		 WHERE a.table_schema = 'rif_studies'
		   AND a.table_name   = LOWER(@c1_rec_extract_table)
		 ORDER BY a.ordinal_position;			    
	DECLARE @c2_rec_column_name 	VARCHAR(30);
	SET @i=0;
	OPEN c2insext;
	FETCH NEXT FROM c2insext INTO @c2_rec_column_name;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @i=@i+1;
		IF @i = 1 SET @sql_stmt=@sql_stmt + @tab + @c2_rec_column_name
		ELSE SET @sql_stmt=@sql_stmt + ',' + @c2_rec_column_name;
--
		FETCH NEXT FROM c2insext INTO @c2_rec_column_name;
	END;
	CLOSE c2insext;
	DEALLOCATE c2insext;
--
	IF @i = 0 BEGIN
		SET @err_msg = formatmessage(56001, @study_id, @c1_rec_extract_table); -- Study ID %i no columns found for extract table: %s
		THROW 56001, @err_msg, 1;
	END;

--
-- Get number of distinct numerators
--
	OPEN c3insext;
	FETCH NEXT FROM c3insext INTO @c3_rec_distinct_numerators;
	IF @@CURSOR_ROWS = 0 BEGIN
		CLOSE c3insext;
		DEALLOCATE c3insext;
		SET @err_msg = formatmessage(56000, @study_id); -- Study ID %i not found
		THROW 56000, @err_msg, 1;
	END;
	CLOSE c3insext;
	DEALLOCATE c3insext;
	SET @sql_stmt=@sql_stmt + ') /* '+ CAST(@c3_rec_distinct_numerators AS VARCHAR) + ' numerator(s) */' + @crlf;

	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: F';	
--
-- SELECT statement
--
	SET @sql_stmt=@sql_stmt + 'SELECT d.year,' + @crlf +
		'       ''' + @study_or_comparison + ''' AS study_or_comparison,' + @crlf +
		'       @studyid AS study_id,' + @crlf +
		'       d.area_id,' + @crlf +
		'       d.band_id,' + @crlf;
		
	IF @c1_rec_study_type != 1 /* Risk analysis */ BEGIN
		IF @study_or_comparison = 'C' 
			SET @sql_stmt=@sql_stmt + @crlf +
				@tab + @tab + 
				'NULL AS intersect_count, NULL AS distance_from_nearest_source, NULL AS nearest_rifshapepolyid, NULL AS exposure_value,' + @crlf;
		ELSE
			SET @sql_stmt=@sql_stmt + @crlf +
				@tab + @tab + 
				'd.intersect_count, d.distance_from_nearest_source, d.nearest_rifshapepolyid, d.exposure_value,' + @crlf;
	END;
	
--
-- [Add support for differing age/sex/group names]
--
	SET @sql_stmt=@sql_stmt + '       FLOOR(d.' + LOWER(@c8_rec_age_sex_group_field_name) + '/100) AS sex,' + @crlf +
		'       (d.' + LOWER(@c8_rec_age_sex_group_field_name) + ' % 100) AS age_group,' + @crlf;

--
-- Add covariate names (Assumes 1 covariate table.1 covariate)
--
--		IF study_or_comparison = 'C' THEN
--			sql_stmt:=sql_stmt||'       NULL::INTEGER AS '||LOWER(c7_rec.covariate_name)||','||E'\n';
--		ELSE
	IF @c7_rec_covariate_name IS NOT NULL SET @sql_stmt=@sql_stmt + 
		'       d.' + LOWER(@c7_rec_covariate_name) + ',' + @crlf;
					/* Multiple covariate support will be needed here */
	IF @sql_stmt IS NOT NULL PRINT 'SQL Statement OK: G';
--
-- Add investigations 
--
	OPEN c9_inv_array;
	FETCH NEXT FROM c9_inv_array INTO @c9_rec_inv;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @sql_stmt=@sql_stmt + @c9_rec_inv + ',' + @crlf;
--
		FETCH NEXT FROM c9_inv_array INTO @c9_rec_inv;
	END;
	CLOSE c9_inv_array;
	DEALLOCATE c9_inv_array;

--
-- Add denominator
--
	SET @sql_stmt=@sql_stmt + '       d.total_pop' + @crlf;

--
-- FROM clause
--
	SET @sql_stmt=@sql_stmt + '  FROM d' + @tab + @tab + @tab + '/* Denominator - ' + @c8_rec_description + ' */' + @crlf;
	OPEN c10_inv_join_array;
	FETCH NEXT FROM c10_inv_join_array INTO @c10_rec_outer_join;
	WHILE @@FETCH_STATUS = 0
	BEGIN
		SET @sql_stmt=@sql_stmt + @c10_rec_outer_join + @crlf;
--
		FETCH NEXT FROM c10_inv_join_array INTO @c10_rec_outer_join;
	END;
	CLOSE c10_inv_join_array;
	DEALLOCATE c10_inv_join_array;

--
-- ORDER BY clause
--
	SET @sql_stmt=@sql_stmt + ' ORDER BY 1, 2, 3, 4, 5, 6, 7';
--
	IF @sql_stmt IS NOT NULL BEGIN

		SET @msg='56005: Create INSERT statement (' + COALESCE(CAST(LEN(@sql_stmt) AS VARCHAR), 'no') + 
		' chars)' + @crlf + 'SQL> ';
		PRINT @msg; -- Split into 2 so missing output is obvious; splitting SQL statement on CRLFs
--		PRINT @sql_stmt;
-- 		EXPERIMENTAL CODE TO SPLIT SQL INTO LINES WITH SEPARATE PRINT FOR TOMCAT 
		DECLARE @psql_stmt NVARCHAR(MAX) = REPLACE(@sql_stmt, @crlf, '|');
		DECLARE @sql_frag varchar(4000) = null
		WHILE LEN(@psql_stmt) > 0
		BEGIN
			IF PATINDEX('%|%', @psql_stmt) > 0
			BEGIN
				SET @sql_frag = SUBSTRING(@psql_stmt,
											0,
											PATINDEX('%|%', @psql_stmt))
				PRINT @sql_frag

				SET @psql_stmt = SUBSTRING(@psql_stmt,
										  LEN(@sql_frag + '|') + 1,
										  LEN(@psql_stmt))
			END
			ELSE
			BEGIN
				SET @sql_frag = @psql_stmt
				SET @psql_stmt = NULL
				PRINT @sql_frag
			END
		END; 
--
		INSERT INTO ##g_insert_dml(sql_stmt, name, study_id) VALUES (@sql_stmt, @study_or_comparison + ': 1', @study_id);
--		
		END;
	ELSE
		BEGIN
			SET @err_msg = formatmessage(56008, @study_id); 
				-- Study ID %i no SQL generated for INSERT into extract table.
			THROW 56008, @err_msg, 1;		
	END;
--
	RETURN @rval;
END;
GO

GRANT EXECUTE ON OBJECT::[rif40].[rif40_create_insert_statement] TO rif_user;
GRANT EXECUTE ON OBJECT::[rif40].[rif40_create_insert_statement] TO rif_manager;
GO

--
-- sqlcmd -U peter -P peter -d sahsuland

/*
IF OBJECT_ID('tempdb..##g_insert_dml') IS NULL CREATE TABLE ##g_insert_dml(study_id INTEGER, name VARCHAR(20), sql_stmt NVARCHAR(MAX));
GO
EXECUTE rif40.rif40_create_insert_statement 2, 'C', 2000, 2000;
GO
DECLARE @studyid INTEGER=2;
DECLARE @yearstart INTEGER=2000;

 */
--
-- Eof