/*
Function returns the current value of a specified sequence (since SQL Server appears not to have a function that already does that)
*/

SELECT name, type, type_desc
  FROM sys.objects
 WHERE object_id = OBJECT_ID(N'[rif40].[rif40_sequence_current_value]')
   AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' );
GO

SELECT SCHEMA_NAME(o.SCHEMA_ID) AS referencing_schema_name,
	   o.name AS referencing_object_name,
	   o.type_desc AS referencing_object_type_desc
  FROM sys.sql_expression_dependencies sed
	INNER JOIN sys.objects o ON sed.referencing_id = o.[object_id]
	LEFT OUTER JOIN sys.objects o1 ON sed.referenced_id = o1.[object_id]
 WHERE sed.referenced_entity_name = 'rif40_sequence_current_value';
GO
 
--
-- SQL Generator
--
WITH a AS (
	SELECT SCHEMA_NAME(o.SCHEMA_ID) AS referencing_schema_name,
		   o.name AS referencing_object_name,
		   o.type_desc AS referencing_object_type_desc
	  FROM sys.sql_expression_dependencies sed
		INNER JOIN sys.objects o ON sed.referencing_id = o.[object_id]
		LEFT OUTER JOIN sys.objects o1 ON sed.referenced_id = o1.[object_id]
	 WHERE sed.referenced_entity_name = 'rif40_sequence_current_value'
)
SELECT 'IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N''[' + a.referencing_schema_name + '].[' + a.referencing_object_name + ']'')
                  AND type IN ( N''D'', N''TR'' ))' + 
		CASE 
			WHEN a.referencing_object_type_desc = 'SQL_TRIGGER' THEN
					'ALTER TABLE [' + a.referencing_schema_name + '].[' + OBJECT_NAME(o.parent_object_id) + '] DROP TRIGGER ' +
					a.referencing_object_name + ';'
			WHEN a.referencing_object_type_desc = 'DEFAULT_CONSTRAINT' THEN
					'ALTER TABLE [' + a.referencing_schema_name + '].[' + OBJECT_NAME(o.parent_object_id) + '] DROP CONSTRAINT ' +
					a.referencing_object_name + ';'
	   END AS sql
  FROM sys.objects o, a
 WHERE o.name = a.referencing_object_name
   AND o.parent_object_id <> 0;
GO

--
-- Generated SQL
--
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_result_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_results] DROP CONSTRAINT t_rif40_result_study_id_seq;
GO
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_result_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_results] DROP CONSTRAINT t_rif40_result_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_study_share_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[rif40_study_shares] DROP CONSTRAINT t_rif40_study_share_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_comparison_area_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_comparison_areas] DROP CONSTRAINT t_rif40_comparison_area_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_contextual_stat_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_contextual_stats] DROP CONSTRAINT t_rif40_contextual_stat_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_contextual_stat_inv_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_contextual_stats] DROP CONSTRAINT t_rif40_contextual_stat_inv_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql_log_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_study_sql_log] DROP CONSTRAINT t_rif40_study_sql_log_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_study_sql] DROP CONSTRAINT t_rif40_study_sql_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariate_inv_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_inv_covariates] DROP CONSTRAINT t_rif40_inv_covariate_inv_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariate_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_inv_covariates] DROP CONSTRAINT t_rif40_inv_covariate_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_condition_inv_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_inv_conditions] DROP CONSTRAINT t_rif40_inv_condition_inv_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_condition_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_inv_conditions] DROP CONSTRAINT t_rif40_inv_condition_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_study_area_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_study_areas] DROP CONSTRAINT t_rif40_study_area_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_investigation_study_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_investigations] DROP CONSTRAINT t_rif40_investigation_study_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[t_rif40_result_inv_id_seq]')
                  AND type IN ( N'D', N'TR' ))
	ALTER TABLE [rif40].[t_rif40_results] DROP CONSTRAINT t_rif40_result_inv_id_seq;
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_sequence_current_value]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
	DROP FUNCTION [rif40].[rif40_sequence_current_value];
GO
--
-- End of generated SQL
--

CREATE FUNCTION [rif40].[rif40_sequence_current_value](@l_sequence_name VARCHAR(max))
	RETURNS INT AS
BEGIN
	DECLARE @table_val INT;
	DECLARE @current_val INT;
	
/*
 * Cannot access temporary tables from within a function so cannot use ##t_rif40_studies_seq
 * or ##t_rif40_investigations_seq. These are now used as a check
 *
 * Therefore get the maximum sequence value from your own study/investigation. Normal 
 * transactional control assumes that your maximum study_id or inv_id is the current.
 * Any studies created after with bigger ids will be in a different transaction not 
 * visible to this transaction
 */	
	IF @l_sequence_name = 'rif40.rif40_study_id_seq' 
		BEGIN
			SELECT @table_val = MAX(study_id)
			  FROM rif40_studies
			 WHERE username = CURRENT_USER;
			RETURN @table_val;
		END
	ELSE IF @l_sequence_name = 'rif40.rif40_inv_id_seq' 
		BEGIN
			SELECT @table_val = MAX(inv_id)
			  FROM rif40_investigations
			 WHERE username = CURRENT_USER;
			RETURN @table_val;
		END;
	
	IF EXISTS (SELECT * FROM sys.objects 
			WHERE object_id = OBJECT_ID(@l_sequence_name) AND type in (N'SO'))
		BEGIN
			SELECT @current_val = CONVERT(INT, current_value)
			  FROM sys.sequences
			 WHERE object_id = OBJECT_ID(@l_sequence_name);
			 
			IF (@table_val IS NULL) 
				RETURN CAST('rif40_sequence_current_value error: No studies by user found in rif40_studies/rif40_investigations' AS INT) 
			ELSE IF (@current_val IS NULL OR @current_val = 0) 
				RETURN CAST('rif40_sequence_current_value error: Sequence never used' AS INT)
			ELSE IF (@table_val > 0 AND @table_val > @current_val) 
				RETURN CAST('rif40_sequence_current_value error: Impossible value for study_id or inv_id' AS INT)
			ELSE IF (@table_val > 0 AND @table_val <= @current_val) RETURN @table_val
				/* study_id or inv_id in rif40_studies/rif40_investigations is OK */
			ELSE RETURN CAST('rif40_sequence_current_value error: Logically impossible' AS INT);
				/* Logically impossible */
		END
	ELSE
		RETURN CAST('rif40_sequence_current_value error: Invalid sequence name: ' + @l_sequence_name AS INT); 
--		/* Cause deliberate error: cannot RAISE errors in SQL functions */	
		
	RETURN -1;
END;
GO

GRANT EXECUTE ON [rif40].[rif40_sequence_current_value] TO rif_user, rif_manager;
GO

--
-- EOF
