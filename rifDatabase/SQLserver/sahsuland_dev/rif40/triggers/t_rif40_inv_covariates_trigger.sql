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
------------------------------------------------
--When Transaction is an Update  then rollback 
-------------------------------------------------
	IF (@XTYPE = 'U')
		BEGIN
		 	Raiserror (50091, 16,1 );
			rollback tran
		  end 
END


 ----------------------------------------
 -- check covariates : if min< expected 
 ----------------------------------------
	 DECLARE @min nvarchar(MAX) =
		(
		SELECT 
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