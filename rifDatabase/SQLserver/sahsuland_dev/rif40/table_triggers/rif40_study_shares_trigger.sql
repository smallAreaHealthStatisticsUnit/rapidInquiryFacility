USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_study_shares')
BEGIN
	DROP TRIGGER [rif40].[tr_study_shares]
END
GO


CREATE trigger [tr_study_shares]
on [rif40].[rif40_study_shares]
for insert , update 
as

begin
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
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
		 	Raiserror (50081, 16,1 );
			rollback tran
		  end 
END
-- STUDY NOT FOUND 
	DECLARE @study nvarchar(MAX) =
		(
		SELECT 
			STUDY_ID + ', '
			FROM inserted
			WHERE study_id NOT in (select STUDY_ID from  t_rif40_studies)
			FOR XML PATH('')
		);
	BEGIN TRY 
		IF @study IS NOT NULL
		BEGIN
			RAISERROR(50082, 16, 1, @study) with log;
		END;
	END TRY 
	BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc]
	END CATCH 

-- Grantor is NOT owner of the study
DECLARE @GRANTOR nvarchar(MAX) =
		(
		SELECT 
			USERNAME + ', '
			FROM t_rif40_studies
			WHERE study_id in (select STUDY_ID from  INSERTED )
			AND USERNAME not IN (SELECT GRANTOR FROM inserted)
			FOR XML PATH('')
		);
	BEGIN TRY 
		IF @GRANTOR IS NOT NULL
		BEGIN
			RAISERROR(50083, 16, 1, @GRANTOR) with log;
		END;
	END TRY 
	BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc]
	END CATCH 
END  
GO