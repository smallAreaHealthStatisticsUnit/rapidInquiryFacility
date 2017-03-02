/*
Check - USER exists.
Check - USER is Kerberos USER on INSERT.
Check - UPDATE not allowed.
Check - DELETE only allowed on own records.
Check - relative_risk, smoothed_relative_risk, posterior_probability, posterior_probability_lower95, posterior_probability_upper95,
  	smoothed_smr, smoothed_smr_lower95, smoothed_smr_upper95, residual_relative_risk, residual_rr_lower95, residual_rr_upper95
	are NULL for directly standardised results
*/


IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_result_checks')
BEGIN
	DROP TRIGGER [rif40].[tr_result_checks]
END
GO


CREATE TRIGGER [tr_result_checks] 
on [rif40].[t_rif40_results]
for insert , update , delete
as
BEGIN
DECLARE  @xtype varchar(5);
IF EXISTS (SELECT * FROM DELETED)
	SET @XTYPE = 'D';
IF EXISTS (SELECT * FROM INSERTED)
BEGIN
	IF (@XTYPE = 'D')
	BEGIN
		SET @XTYPE = 'U';
	END
END
ELSE
	SET @XTYPE = 'I';

DECLARE @has_studies_check VARCHAR(MAX) = 
(
	SELECT count(study_id) as total
	FROM [rif40].[t_rif40_results]
);
				
IF @XTYPE = 'D'
BEGIN
	DECLARE @delete_user_check VARCHAR(MAX) = 
	(
		SELECT inv_id, study_id, band_id, genders, adjusted, direct_standardisation, username
		FROM deleted
		WHERE username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @delete_user_check IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51083, @delete_user_check);
		THROW 51083, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_results]';
		THROW 51083, @err_msg1, 1;
	END CATCH;	
	RETURN;
END;

IF @XTYPE = 'U' OR @XTYPE = 'I'
BEGIN
	DECLARE @insert_user_check VARCHAR(MAX) = 
	(
		SELECT inv_id, study_id, band_id, genders, adjusted, direct_standardisation, username
		FROM inserted
		WHERE username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @insert_user_check IS NOT NULL
	BEGIN
		IF @has_studies_check = 0 AND SUSER_SNAME()='RIF40'
		BEGIN
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_results]', 't_rif40_results insert/update allowed during build';
		END
		ELSE 
		BEGIN TRY
			rollback;
			DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51084, @insert_user_check);
			THROW 51084, @err_msg2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_results]';
			THROW 51084, @err_msg2, 1;
		END CATCH;	
	END
	ELSE
	BEGIN
		IF @XTYPE = 'U'
		BEGIN TRY
			rollback;
			DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51085);
			THROW 51085, @err_msg3, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_results]';
			THROW 51085, @err_msg3, 1;
		END CATCH;	
	END;
	
	IF @has_studies_check > 0
	BEGIN
		DECLARE @standardized_results_prob VARCHAR(MAX) = 
		(
			SELECT inv_id, study_id, band_id, genders, adjusted, direct_standardisation
			FROM inserted ic
			WHERE direct_standardisation=1
			AND (
			(ic.relative_risk IS NOT NULL AND ic.relative_risk <> '') OR
			(ic.smoothed_relative_risk IS NOT NULL AND ic.smoothed_relative_risk <> '') OR
			(ic.posterior_probability IS NOT NULL AND ic.posterior_probability <> '') OR
			(ic.posterior_probability_lower95 IS NOT NULL AND ic.posterior_probability_lower95 <> '') OR
			(ic.posterior_probability_upper95 IS NOT NULL AND ic.posterior_probability_upper95 <> '') OR
			(ic.smoothed_smr IS NOT NULL AND ic.smoothed_smr <> '') OR
			(ic.smoothed_smr_lower95 IS NOT NULL AND ic.smoothed_smr_lower95 <> '') OR
			(ic.smoothed_smr_upper95 IS NOT NULL AND ic.smoothed_smr_upper95 <> '') OR
			(ic.residual_relative_risk IS NOT NULL AND ic.residual_relative_risk <> '') OR
			(ic.residual_rr_lower95 IS NOT NULL AND ic.residual_rr_lower95 <> '') OR
			(ic.residual_rr_upper95 IS NOT NULL AND ic.residual_rr_upper95 <> ''))
			FOR XML PATH('')
		);
		IF @standardized_results_prob IS NOT NULL
		BEGIN TRY
			rollback;
			DECLARE @err_msg4 VARCHAR(MAX) = formatmessage(51086, @standardized_results_prob);
			THROW 51086, @err_msg4, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_results]';
			THROW 51086, @err_msg4, 1;
		END CATCH;	
	END;
END;
END;
GO
