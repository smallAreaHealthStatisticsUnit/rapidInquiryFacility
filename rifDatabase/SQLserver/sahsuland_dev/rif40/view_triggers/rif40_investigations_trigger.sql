
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_investigations')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_investigations];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_investigations]
on [rif40].[rif40_investigations]
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
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
	DECLARE @insert_invalid_user VARCHAR(MAX) = 
	(
		SELECT SUSER_SNAME() AS username
		  FROM inserted
		 WHERE NOT (username = SUSER_SNAME() OR username is null)		  /* Not the study owner */
		   AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_user')    != 1 /* Not a rif_user or a rif_manager */
	       AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') != 1
	);	

	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51122, @insert_invalid_user);
		THROW 51122, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_inv_covariates]';
		THROW 51122, @err_msg1, 1;
	END CATCH;	

	DECLARE @inv_id INTEGER = (SELECT inv_id FROM inserted);
	IF @inv_id IS NULL SET @inv_id = (NEXT VALUE FOR [rif40].[rif40_inv_id_seq]); /* default value in t_rif40_investigations will be: NEXT VALUE FOR [rif40].[rif40_inv_id_seq] */
	
	INSERT INTO [rif40].[t_rif40_investigations] (
				username,
				inv_id,
				study_id,
				inv_name,
				year_start,
				year_stop,
				max_age_group,
				min_age_group,
				genders,
				numer_tab,
				mh_test_type,
				inv_description,
				classifier,
				classifier_bands,
				investigation_state)
	SELECT
				isnull(username,SUSER_SNAME()),
				@inv_id,
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq')),
				inv_name /* no default value */,
				year_start /* no default value */,
				year_stop /* no default value */,
				max_age_group /* no default value */,
				min_age_group /* no default value */,
				genders /* no default value */,
				numer_tab /* no default value */,
				isnull(mh_test_type, 'No Test'),
				inv_description /* no default value */,
				isnull(classifier, 'QUANTILE'),
				isnull(classifier_bands, 5),
				isnull(investigation_state, 'C')
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
		left outer join inserted b on a.study_id=b.study_id AND a.inv_id=b.inv_id
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51123, @update_invalid_user);
		THROW 51123, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_investigations]';
		THROW 51123, @err_msg2, 1;
	END CATCH;	

	UPDATE a
	   SET /* Only update state, cannot change PK: inv_id, study_id */
		   investigation_state=inserted.investigation_state
	  FROM [rif40].[t_rif40_investigations] a
	  JOIN inserted ON (inserted.study_id = a.study_id AND inserted.inv_id = a.inv_id);

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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51124, @delete_invalid_user);
		THROW 51124, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_investigations]';
		THROW 51124, @err_msg3, 1;
	END CATCH;	

	DELETE FROM [rif40].[t_rif40_investigations]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.inv_id=[rif40].[t_rif40_investigations].inv_id
		AND b.study_id=[rif40].[t_rif40_investigations].study_id);
END;

END;
GO
