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
--
--stopped here, the Postgres code doesn't make sense to me...
DECLARE @different_study_geolevels VARCHAR(MAX) = 
(

	SELECT a.study_geolevel_name, b.geolevel_id, a.study_id
		  FROM t_rif40_studies a, t_rif40_geolevels b
		 WHERE currval('rif40_study_id_seq'::regclass) = study_id
		   AND b.geolevel_name = a.study_geolevel_name

	c2_ckicov CURSOR(l_geography  varchar, l_geolevel_name  varchar) FOR
		SELECT *
		  FROM t_rif40_geolevels
		 WHERE l_geography     = geography
		   AND l_geolevel_name = geolevel_name;
		   
SELECT a.study_geolevel_name, b.geolevel_id, a.study_id
		  FROM t_rif40_studies a, t_rif40_geolevels b
		 WHERE currval('rif40_study_id_seq'::regclass) = study_id
		   AND b.geolevel_name = a.study_geolevel_name;
		   
		   
 ----------------------------------------
 -- check covariates : if min< expected 
 ----------------------------------------
DECLARE @min_problem nvarchar(MAX) =
		(
		SELECT  ic.min
        cast(ic.[min] as varchar(20))+ ' '
		FROM inserted ic 
		where EXISTS (SELECT 1 FROM [rif40].[rif40_covariates] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			  c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  ic.[min]<c.[min])
        FOR XML PATH('')
		 );

	IF @min IS NOT NULL
		BEGIN
			RAISERROR(50092, 16, 1, @min) with log;
		END;
-----------------------------------------
-- check covariates --if max> expected 
-----------------------------------------
 DECLARE @max nvarchar(MAX) =
		(
		SELECT 
        cast(ic.[max] as varchar(20))+ ' '
		FROM inserted ic 
		where EXISTS (SELECT 1 FROM [rif40].[rif40_covariates] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			  c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  ic.[max]>c.[max])
        FOR XML PATH('')
		 );

	IF @max IS NOT NULL
		BEGIN
			RAISERROR(50093, 16, 1, @max) with log;
		END;


-------------------------------
----Remove when supported
--------------------------------
 DECLARE @type2 nvarchar(MAX) =
		(
		SELECT 
        [STUDY_ID]+ ' '
		FROM inserted ic 
		where EXISTS (SELECT 1 FROM [rif40].[rif40_covariates] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			  c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  c.type =2)
        FOR XML PATH('')
		 );

	IF @type2 IS NOT NULL
		BEGIN
			RAISERROR(50094, 16, 1,@type2 ) with log;
		END;


 DECLARE @type1 nvarchar(MAX) =
		(
		SELECT 
        [STUDY_ID]+ ' '
		FROM inserted ic 
		where EXISTS (SELECT 1 FROM [rif40].[rif40_covariates] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			  c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  c.type =1 and 
			  ic.MAX <> round(ic.MAX,0))
        FOR XML PATH('')
		 );

	IF @type1 IS NOT NULL
		BEGIN
			RAISERROR(50095, 16, 1,@type1 ) with log;
		END;

 DECLARE @type1b nvarchar(MAX) =
		(
		SELECT 
        [STUDY_ID]+ ' '
		FROM inserted ic 
		where EXISTS (SELECT 1 FROM [rif40].[rif40_covariates] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			  c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  c.type =1 and 
			  ic.MIN<> round(ic.MIN,0))
        FOR XML PATH('')
		 );

	IF @type1b IS NOT NULL
		BEGIN
			RAISERROR(50096, 16, 1,@type1b ) with log;
		END;

 DECLARE @type1_min nvarchar(MAX) =
		(
		SELECT 
        [STUDY_ID]+ ' '
		FROM inserted ic 
		where EXISTS (SELECT 1 FROM [rif40].[rif40_covariates] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			  c.covariate_name=ic.covariate_name and -- can comment it out to test 
			  c.type =1 and 
			  ic.MIN<0)
        FOR XML PATH('')
		 );

	IF @type1_min IS NOT NULL
		BEGIN
			RAISERROR(50097, 16, 1,@type1_min ) with log;
		END;
-------------------------------
--Check - study_geolevel_name.
-------------------------------

DECLARE @study_geolevel_nm nvarchar(MAX) =
		(
		SELECT 
        [STUDY_GEOLEVEL_NAME]+ ' '
		FROM inserted ic 
		where EXISTS (SELECT 1 FROM [rif40].[t_rif40_geolevels] c 
              WHERE ic.[geography]=c.[GEOGRAPHY] and 
			  c.geolevel_name=ic.study_geolevel_name and
			  ic.STUDY_GEOLEVEL_NAME is not Null 
					)
        FOR XML PATH('')
		 );

	IF @study_geolevel_nm IS NOT NULL
		BEGIN
			RAISERROR(50098, 16, 1, @study_geolevel_nm) with log;
		END;

end