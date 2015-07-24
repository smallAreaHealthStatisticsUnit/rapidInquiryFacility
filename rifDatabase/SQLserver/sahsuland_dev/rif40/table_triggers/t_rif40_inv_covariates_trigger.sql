/*
<trigger_t_rif40_inv_covariates_checks_description>
<para>
Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
Check - study_geolevel_name.
Check - Covariates a) MIN and MAX.  b) Limits c) Check access to covariate table, <covariate name> column exists d) Check score.
</para>
</trigger_t_rif40_inv_covariates_checks_description>

Currently requires study geolevel in t_rif40_inv_covariates to be the same as study geolevel in t_rif40_studies (because that's what is in Postgres,
although comments suggest that it should be <= then t_rif40_studies version.  Currently does not appear to be easy way to compare
names of geolevels

*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_inv_covariate')
BEGIN
	DROP TRIGGER [rif40].[tr_inv_covariate]
END
GO


-------------------------
 -- create trigger code 
 --------------------------
 CREATE trigger [tr_inv_covariate]
 on [rif40].[t_rif40_inv_covariates]
 for insert, update 
 as
 begin 

 Declare  @xtype varchar(5)
 	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END
	IF EXISTS (SELECT * FROM INSERTED)
		BEGIN
			IF (@XTYPE = 'D')
		BEGIN
			SET @XTYPE = 'U'
		END
	ELSE
		BEGIN
			SET @XTYPE = 'I'
		END
		
		
--check if username is correct
IF @XTYPE = 'I' OR @XTYPE='U'
BEGIN
	DECLARE @different_user	VARCHAR(MAX) = 
	(
		SELECT username, study_id, inv_id, covariates_name
		FROM inserted
		WHERE username != SUSER_SNAME()
	 FOR XML PATH('') 
	); 
	IF @different_user IS NOT NULL
	BEGIN
	DECLARE @has_studies_check VARCHAR(MAX) = 
	(
		SELECT count(study_id) as total
		FROM [rif40].[t_rif40_results]
	);
	IF SUSER_SNAME() = 'RIF40' and @has_studies_check=0
		BEGIN
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_inv_covariates]', 't_rif40_inv_covariates insert allowed during build';
		END;
		ELSE
			BEGIN TRY
				rollback;
				DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51068, @different_user);
				THROW 51068, @err_msg1, 1;
			END TRY
			BEGIN CATCH
				EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
				THROW 51068, @err_msg1, 1;
			END CATCH;	
	END;
END;

--updates not allowed		
IF @XTYPE = 'U'
BEGIN TRY
	rollback;
	DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(50091);
	THROW 50091, @err_msg2, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50091, @err_msg2, 1;
END CATCH;	

--can only delete your own records
IF @XTYPE = 'D'
BEGIN
	DECLARE @different_user_del	VARCHAR(MAX) = 
	(
		SELECT username, study_id, inv_id, line_number
		FROM deleted
		WHERE username != SUSER_SNAME()
	 FOR XML PATH('') 
	); 
	IF @different_user_del IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51069, @different_user_del);
		THROW 51069, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
		THROW 51069, @err_msg3, 1;
	END CATCH;	
	
	RETURN;
END;

--
-- Check study geolevel name is the same (or lower than) than study geolevel name in t_rif40_studies
-- Postgres code only checks whether they are identical
-- 
DECLARE @missing_study_geolevel VARCHAR(MAX) = 
(
	SELECT geography, study_geolevel_name
	from inserted b
	where not exists (select 1
		  FROM t_rif40_geolevels a
		 WHERE a.geography     = b.geography
		   AND a.geolevel_name = b.study_geolevel_name)
	FOR XML PATH ('');
);
IF @missing_study_geolevel IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg4 VARCHAR(MAX) = formatmessage(50098, @missing_study_geolevel);
	THROW 50098, @err_msg4, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50098, @err_msg4, 1;
END CATCH;

DECLARE @current_study INT = [rif40].[rif40_sequence_current_value] 'rif40_study_id_seq';
DECLARE @found_study_info VARCHAR(MAX) = 
(
	SELECT 1
	FROM t_rif40_studies a, t_rif40_geolevels b
	WHERE a.study_id=[rif40].[rif40_sequence_current_value] 'rif40_study_id_seq'
	AND b.geolevel_name = a.study_geolevel_name
);
IF @found_study_info IS NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg5 VARCHAR(MAX) = formatmessage(51070, @current_study);
	THROW 51070, @err_msg5, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 51070, @err_msg5, 1;
END CATCH;

DECLARE @mismatched_geolevels VARCHAR(MAX) =
(
	SELECT a.study_geolevel_name as new_geolevel, b.study_geolevel_name as t_rif40_studies_geolevel, b.study_id
	from inserted a, t_rif40_studies b
	where b.study_id=[rif40].[rif40_sequence_current_value] 'rif40_study_id_seq'
	and a.study_geolevel_name != b.study_geolevel_name
);
IF @mismatched_geolevels IS NOT NULL
BEGIN
	DECLARE @log_msg = 'T_RIF40_INV_COVARIATES study geolevel name != T_RIF40_STUDIES geolevel name: '+@mismatched_gelevels;
	EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_inv_covariate]', @log_msg;
END;

 ----------------------------------------
 -- check covariates : if min< expected 
 ----------------------------------------
DECLARE @min_problem nvarchar(MAX) =
(
	SELECT  a.geography, a.study_geolevel_name, a.covariate_name, a.min as new_min, b.min as rif40_covariates_min
	from inserted a,  [rif40].[rif40_covariates] b
	WHERE  b.geography      = a.geography
		AND b.geolevel_name  = a.study_geolevel_name
		AND b.covariate_name = a.covariate_name
		AND a.min < b.min
	FOR XML PATH('')
);
IF @min_problem IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg6 VARCHAR(MAX) = formatmessage(50092, @min_problem);
	THROW 50092, @err_msg6, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50092, @err_msg6, 1;
END CATCH;

-----------------------------------------
-- check covariates --if max> expected 
-----------------------------------------
 DECLARE @max_problem nvarchar(MAX) =
(
	SELECT  a.geography, a.study_geolevel_name, a.covariate_name, a.max as new_max, b.max as rif40_covariates_max
	from inserted a,  [rif40].[rif40_covariates] b
	WHERE  b.geography      = a.geography
		AND b.geolevel_name  = a.study_geolevel_name
		AND b.covariate_name = a.covariate_name
		AND a.max > b.max
	FOR XML PATH('')
);
IF @max_problem IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg7 VARCHAR(MAX) = formatmessage(50093, @max_problem);
	THROW 50093, @err_msg7, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50093, @err_msg7, 1;
END CATCH;

-------------------------------
----Remove when more features supported
--------------------------------
 DECLARE @unsupported_cov_type2 nvarchar(MAX) =
(
	SELECT a.study_id, a.geography, a.study_geolevel_name, a.covariate_name
	FROM inserted a, [rif40].[rif40_covariates] b
	WHERE a.geography=b.geography
	AND a.study_geolevel_name=b.geolevel_name
	and a.covariate_name=b.covariate_name
	and b.type=2
	FOR XML PATH('')
 );
IF @unsupported_cov_type2 IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg8 VARCHAR(MAX) = formatmessage(50094, @unsupported_cov_type2);
	THROW 50094, @err_msg8, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50094, @err_msg8, 1;
END CATCH;


 DECLARE @non_int_max_with_type1 nvarchar(MAX) =
(
	SELECT a.study_id, a.geography, a.study_geolevel_name, a.covariate_name, a.max
	FROM inserted a, [rif40].[rif40_covariates] b
	WHERE a.geography=b.geography
	AND a.study_geolevel_name=b.geolevel_name
	and a.covariate_name=b.covariate_name
	and b.type=1
	AND a.max <> round(a.max,0)
	FOR XML PATH('')
 );
IF @non_int_max_with_type1 IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg9 VARCHAR(MAX) = formatmessage(50095, @non_int_max_with_type1);
	THROW 50095, @err_msg9, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50095, @err_msg9, 1;
END CATCH;

DECLARE @non_int_min_with_type1 nvarchar(MAX) =
(
	SELECT a.study_id, a.geography, a.study_geolevel_name, a.covariate_name, a.min
	FROM inserted a, [rif40].[rif40_covariates] b
	WHERE a.geography=b.geography
	AND a.study_geolevel_name=b.geolevel_name
	and a.covariate_name=b.covariate_name
	and b.type=1
	AND a.min <> round(a.min,0)
	FOR XML PATH('')
 );
IF @non_int_min_with_type1 IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg10 VARCHAR(MAX) = formatmessage(50096, @non_int_min_with_type1);
	THROW 50096, @err_msg10, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50096, @err_msg10, 1;
END CATCH;
 
DECLARE @invalid_min_type1 nvarchar(MAX) =
(
	SELECT a.study_id, a.geography, a.study_geolevel_name, a.covariate_name, a.min
	FROM inserted a, [rif40].[rif40_covariates] b
	WHERE a.geography=b.geography
	AND a.study_geolevel_name=b.geolevel_name
	and a.covariate_name=b.covariate_name
	and b.type=1
	AND a.min<0
	FOR XML PATH('')
);
IF @invalid_min_type1 IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg11 VARCHAR(MAX) = formatmessage(50097, @non_int_min_with_type1);
	THROW 50097, @err_msg11, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 50097, @err_msg11, 1;
END CATCH;

--
-- c) Check access to covariate table, <covariate name> column exists
--
DECLARE @covariate_col_missing VARCHAR(max) =
(	
	SELECT a.inv_id, a.study_id, a.study_geolevel_name, a.covariate_name, b.covariate_table
	from inserted a, [rif40].[t_rif40_geolevels] b
	where a.geography=b.geography
	and a.study_geolevel_name=b.geolevel_name
	and not exists (
		select 1 from INFORMATION_SCHEMA.COLUMNS c
			where c.table_name=b.covariate_name
			and c.column_name=a.covariate_name)
	FOR XML PATH('')
);
IF @covariate_col_missing IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg12 VARCHAR(MAX) = formatmessage(51071, @covariate_col_missing);
	THROW 51071, @err_msg12, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 51071, @err_msg12, 1;
END CATCH;

--covariate table YEAR column exists
DECLARE @covariate_year_missing VARCHAR(max) =
(	
	SELECT a.inv_id, a.study_id, a.study_geolevel_name, b.covariate_table
	from inserted a, [rif40].[t_rif40_geolevels] b
	where a.geography=b.geography
	and a.study_geolevel_name=b.geolevel_name
	and not exists (
		select 1 from INFORMATION_SCHEMA.COLUMNS c
			where c.table_name=b.covariate_name
			and c.column_name='YEAR')
	FOR XML PATH('')
);
IF @covariate_year_missing IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg13 VARCHAR(MAX) = formatmessage(51072, @covariate_col_missing);
	THROW 51072, @err_msg13, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 51072, @err_msg13, 1;
END CATCH;

--covariate table study_geolevel_name column exists
DECLARE @covariate_geolevel_missing VARCHAR(max) =
(	
	SELECT a.inv_id, a.study_id, a.study_geolevel_name, b.covariate_table
	from inserted a, [rif40].[t_rif40_geolevels] b
	where a.geography=b.geography
	and a.study_geolevel_name=b.geolevel_name
	and not exists (
		select 1 from INFORMATION_SCHEMA.COLUMNS c
			where c.table_name=b.covariate_name
			and c.column_name=a.study_geolevel_name)
	FOR XML PATH('')
);
IF @covariate_geolevel_missing IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg14 VARCHAR(MAX) = formatmessage(51073, @covariate_geolevel_missing);
	THROW 51073, @err_msg14, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_inv_covariates]';
	THROW 51073, @err_msg14, 1;
END CATCH;

--
-- d) Check score
--
DECLARE @cov_is_type1 VARCHAR(MAX) = (
	SELECT a.study_id, a.geography
	FROM inserted a, [rif40].[rif40_covariates] b
	WHERE a.geography=b.geography
	AND a.study_geolevel_name=b.geolevel_name
	and a.covariate_name=b.covariate_name
	and b.type=1
	FOR XML PATH('')
);
IF @cov_is_type1 IS NOT NULL
BEGIN
--need a cursor, alas



DECLARE @total_leftover int;
DECLARE @area_id_check_sql VARCHAR(MAX) = 'SELECT @total_leftover=COUNT(area_id) AS total FROM ( SELECT area_id FROM t_rif40_comparison_areas WHERE study_id = '
	+@current_study_id +' EXCEPT SELECT lower('+@current_comp_geolevel+') FROM '+@current_hierarchytable+') a';
DECLARE @ParmDefinition nvarchar(500) = N'@total_leftoverOUT int OUTPUT';
EXEC sp_executesql @area_id_check_sql, @ParmDefinition, @total_leftoverOUT=@total_leftover OUTPUT;


sql_stmt:='SELECT COUNT(*) AS total FROM "'||owner||'".'||c2b_rec.covariate_table||' WHERE '||NEW.covariate_name||' = $1';
END


end;
