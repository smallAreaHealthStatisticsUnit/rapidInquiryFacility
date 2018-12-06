
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_results')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_results];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_results]
on [rif40].[rif40_results]
instead of insert , update , delete
as
BEGIN 
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
DECLARE  @XTYPE varchar(1);
IF EXISTS (SELECT * FROM DELETED)
	SET @XTYPE = 'D';
	
IF EXISTS (SELECT * FROM INSERTED)
BEGIN
	IF (@XTYPE = 'D')
		SET @XTYPE = 'U'
	ELSE 
		SET @XTYPE = 'I'
END;

IF @XTYPE='I'
BEGIN
--
-- Check (USER = username OR NULL) and USER is a RIF user; if OK INSERT
--
	DECLARE @insert_invalid_user VARCHAR(MAX) = 
	(
		SELECT SUSER_SNAME() AS username, study_id, inv_id, band_id, genders, direct_standardisation, adjusted
		  FROM inserted
		 WHERE NOT (username = SUSER_SNAME() OR username is null)		  /* Not the study owner */
		   AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_user')    != 1 /* Not a rif_user or a rif_manager */
	       AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') != 1
		FOR XML PATH('')
	);
	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51128, @insert_invalid_user);
		THROW 51128, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_results]';
		THROW 51128, @err_msg1, 1;
	END CATCH;	

	INSERT INTO t_rif40_results (
				username,
				study_id,
				inv_id,
				band_id,
				genders,
				direct_standardisation,
				adjusted,
				observed,
				expected,
				lower95,
				upper95,
				relative_risk,
				smoothed_relative_risk,
				posterior_probability,
				posterior_probability_upper95,
				posterior_probability_lower95,
				residual_relative_risk,
				residual_rr_lower95,
				residual_rr_upper95,
				smoothed_smr,
				smoothed_smr_lower95,
				smoothed_smr_upper95)
	SELECT
				isnull(username,SUSER_SNAME()),
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40_study_id_seq')),
				isnull(inv_id,[rif40].[rif40_sequence_current_value]('rif40_inv_id_seq')),
				band_id /* no default value */,
				genders /* no default value */,
				direct_standardisation /* no default value */,
				adjusted /* no default value */,
				observed /* no default value */,
				expected /* no default value */,
				lower95 /* no default value */,
				upper95 /* no default value */,
				relative_risk /* no default value */,
				smoothed_relative_risk /* no default value */,
				posterior_probability /* no default value */,
				posterior_probability_upper95 /* no default value */,
				posterior_probability_lower95 /* no default value */,
				residual_relative_risk /* no default value */,
				residual_rr_lower95 /* no default value */,
				residual_rr_upper95 /* no default value */,
				smoothed_smr /* no default value */,
				smoothed_smr_lower95 /* no default value */,
				smoothed_smr_upper95 /* no default value */
	FROM inserted;
END;

IF @XTYPE='U'
BEGIN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
	DECLARE @update_invalid_user VARCHAR(MAX) =
	(
		select a.username as 'old_username', b.username as 'new_username', SUSER_SNAME() as 'current_user'
		from deleted a
		left outer join inserted b on a.study_id=b.study_id AND a.inv_id=b.inv_id AND a.band_id=b.band_id
			AND a.genders=b.genders and a.direct_standardisation=b.direct_standardisation AND a.adjusted=b.adjusted
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51129, @update_invalid_user);
		THROW 51129, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_results]';
		THROW 51129, @err_msg2, 1;
	END CATCH;	
	
	DELETE FROM [rif40].[t_rif40_results]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_results].study_id
		AND b.inv_id=[rif40].[t_rif40_results].inv_id
		AND b.band_id=[rif40].[t_rif40_results].band_id
		AND b.genders=[rif40].[t_rif40_results].genders
		AND b.direct_standardisation=[rif40].[t_rif40_results].direct_standardisation
		AND b.adjusted=[rif40].[t_rif40_results].adjusted);
		
	INSERT INTO t_rif40_results (
				username,
				study_id,
				inv_id,
				band_id,
				genders,
				direct_standardisation,
				adjusted,
				observed,
				expected,
				lower95,
				upper95,
				relative_risk,
				smoothed_relative_risk,
				posterior_probability,
				posterior_probability_upper95,
				posterior_probability_lower95,
				residual_relative_risk,
				residual_rr_lower95,
				residual_rr_upper95,
				smoothed_smr,
				smoothed_smr_lower95,
				smoothed_smr_upper95)
	SELECT	username,
				study_id,
				inv_id,
				band_id,
				genders,
				direct_standardisation,
				adjusted,
				observed,
				expected,
				lower95,
				upper95,
				relative_risk,
				smoothed_relative_risk,
				posterior_probability,
				posterior_probability_upper95,
				posterior_probability_lower95,
				residual_relative_risk,
				residual_rr_lower95,
				residual_rr_upper95,
				smoothed_smr,
				smoothed_smr_lower95,
				smoothed_smr_upper95
	FROM inserted;
		
END;

IF @XTYPE='D'
BEGIN
--
-- Check USER = OLD.username; if OK DELETE
--
	DECLARE @delete_invalid_user VARCHAR(MAX) =
	(
		select username
		from deleted
		where username != SUSER_SNAME()
		FOR XML PATH('')
	);
	IF @delete_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51130, @delete_invalid_user);
		THROW 51130, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_results]';
		THROW 51130, @err_msg3, 1;
	END CATCH;	

	DELETE FROM [rif40].[t_rif40_results]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_results].study_id
		AND b.inv_id=[rif40].[t_rif40_results].inv_id
		AND b.band_id=[rif40].[t_rif40_results].band_id
		AND b.genders=[rif40].[t_rif40_results].genders
		AND b.direct_standardisation=[rif40].[t_rif40_results].direct_standardisation
		AND b.adjusted=[rif40].[t_rif40_results].adjusted);
		
END;

END;
GO
