
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_t_rif40_comparison_areas')
BEGIN
	DROP TRIGGER [rif40].[tr_t_rif40_comparison_areas]
END
GO

------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_t_rif40_comparison_areas]
on [rif40].[t_rif40_comparison_areas]
FOR insert
as
BEGIN 
--
-- Effectively disable check during initial system load
--
DECLARE @has_studies_check VARCHAR(MAX) = 
(
	SELECT count(study_id) as total
	FROM [rif40].[t_rif40_results]
);
IF @has_studies_check=0 AND SUSER_SNAME() = 'RIF40'
BEGIN
	EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[tr_t_rif40_comparison_areas]', 'T_RIF40_COMPARISON_AREAS allowed during build before first result is added to system [CHECK DISABLED]';
	RETURN;
END;


--first get current study_id
DECLARE @current_study_id int;
SET @current_study_id= [rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq');
IF @current_study_id IS NULL 
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51043);
		THROW 51043, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_comparison_areas]';
		THROW 51043, @err_msg1, 1;
	END CATCH;	

	
--see whether there are comparison areas defined	
DECLARE @current_comp_geolevel VARCHAR(30), @current_hierarchytable VARCHAR(30);
SELECT @current_comp_geolevel=a.comparison_geolevel_name, @current_hierarchytable=b.hierarchytable
FROM t_rif40_studies a, rif40_geographies b
WHERE a.geography = b.geography
AND a.study_id  = @current_study_id;

IF @current_comp_geolevel IS NULL or @current_hierarchytable IS NULL
BEGIN TRY
	rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51044, @current_study_id);
		THROW 51044, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_comparison_areas]';
		THROW 51044, @err_msg2, 1;
	END CATCH;		
--are there area_ids in comparison areas that are not defined in the geographies hierarchy table?
DECLARE @total_leftover int;
DECLARE @area_id_check_sql NVARCHAR(MAX) = 'SELECT @total_leftoverOUT=COUNT(area_id) FROM ( SELECT area_id FROM rif40.t_rif40_comparison_areas WHERE study_id = '
	+CAST(@current_study_id AS NVARCHAR) +' EXCEPT SELECT '+@current_comp_geolevel+' FROM rif_data.'+@current_hierarchytable+') a';
--EXEC [rif40].[rif40_log] 'DEBUG2', '[rif40].[tr_t_rif40_comparison_areas]', 'SQL> '+@area_id_check_sql;
DECLARE @ParmDefinition nvarchar(500) = N'@total_leftoverOUT int OUTPUT';
EXEC sp_executesql @area_id_check_sql, @ParmDefinition, @total_leftoverOUT=@total_leftover OUTPUT;

IF @total_leftover > 0
BEGIN TRY
	rollback;
		DECLARE @err_msg3_txt VARCHAR(max)= 'study_id='+CAST(@current_study_id AS VARCHAR)+', hierarchytable='+@current_hierarchytable+', comp_geolevel='+@current_comp_geolevel+', total remaining='+@total_leftover;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51045, @err_msg3_txt);
		THROW 51045, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_comparison_areas]';
		THROW 51045, @err_msg3, 1;
	END CATCH;		
ELSE
BEGIN
	DECLARE @log_msg VARCHAR(max) = 'T_RIF40_COMPARISON_AREAS study_id OK: '+CAST(@current_study_id AS VARCHAR)+' found all areas on hierarcy table';
	EXEC [rif40].[rif40_log] 'DEBUG2', '[rif40].[tr_t_rif40_comparison_areas]', @log_msg;
END;
END;
GO
