USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_num_denom')
BEGIN
	DROP TRIGGER [rif40].[tr_num_denom]
END
GO

CREATE trigger [tr_num_denom]
  on [rif40].[t_rif40_num_denom]
  for insert , update
  as
  begin 

  -- check numerator table 
  DECLARE @num nvarchar(MAX) =
    (
    SELECT 
		[NUMERATOR_TABLE] + ', '
        FROM inserted ic 
        WHERE [NUMERATOR_TABLE] NOT in  (select TABLE_NAME from [rif40].[rif40_tables])
        FOR XML PATH('')
    );
	IF @num IS NOT NULL
		BEGIN
			RAISERROR(50075, 16, 1, @num) with log;
		END;

 --- check isnumerator status for numerator tables  
   DECLARE @num_status  nvarchar(MAX) =
    (
    SELECT 
		[NUMERATOR_TABLE] + ', '
        FROM inserted ic 
        WHERE [NUMERATOR_TABLE] IN (select TABLE_NAME from [rif40].[rif40_tables] where ISNUMERATOR <>1) 
		FOR XML PATH('')
    );
	IF @num_status IS NOT NULL
		BEGIN
			RAISERROR(50076, 16, 1, @num_status) with log;
		END;
 -- check denominator table  
   DECLARE @dnom nvarchar(MAX) =
    (
    SELECT 
		DENOMINATOR_TABLE + ', '
        FROM inserted ic 
        WHERE DENOMINATOR_TABLE not in  (select TABLE_NAME from [rif40].[rif40_tables])
        FOR XML PATH('')
    );
	IF @dnom IS NOT NULL
		BEGIN
			RAISERROR(50077, 16, 1, @dnom) with log;
		END;
	-- check denominator status table 
	 DECLARE @denom_status  nvarchar(MAX) =
    (
    SELECT 
		DENOMINATOR_TABLE + ', '
        FROM inserted ic 
        WHERE DENOMINATOR_TABLE IN (select TABLE_NAME from [rif40].[rif40_tables] where ISNUMERATOR =1) 
		FOR XML PATH('')
    );
	IF @denom_status IS NOT NULL
		BEGIN
			RAISERROR(50078, 16, 1, @denom_status) with log;
		END;

  end  

