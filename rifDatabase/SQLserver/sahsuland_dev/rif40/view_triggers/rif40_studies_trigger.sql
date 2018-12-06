
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_studies')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_studies];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_studies]
on [rif40].[rif40_studies]
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
		SELECT SUSER_SNAME() AS username
		  FROM inserted
		 WHERE NOT (username = SUSER_SNAME() OR username is null)		  /* Not the study owner */
		   AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_user')    != 1 /* Not a rif_user or a rif_manager */
	       AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') != 1
	);

	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51131, @insert_invalid_user);
		THROW 51131, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_studies]';
		THROW 51131, @err_msg1, 1;
	END CATCH;	

	DECLARE @study_id INTEGER = (SELECT study_id FROM inserted);
	IF @study_id IS NULL SET @study_id = (NEXT VALUE FOR [rif40].[rif40_study_id_seq]); /* default value in t_rif40_studies will be: NEXT VALUE FOR [rif40].[rif40_study_id_seq] */
	
	INSERT INTO [rif40].[t_rif40_studies] (
				username,
				study_id,
				extract_table,
				study_name,
				summary,
				description,
				other_notes,
				study_date,
				geography,
				study_type,
				study_state,
				comparison_geolevel_name,
				denom_tab,
				direct_stand_tab,
				study_geolevel_name,
				map_table,
				suppression_value,
				extract_permitted,
				transfer_permitted,
				authorised_by,
				authorised_on,
				authorised_notes,
				audsid,
				project,
				stats_method)
	SELECT
				isnull(username, SUSER_SNAME()),
				@study_id, 
				isnull(extract_table, 'S' + CAST(@study_id AS VARCHAR) + '_EXTRACT') /* S<study_id>_EXTRACT */,
				study_name /* no default value */,
				summary /* no default value */,
				description /* no default value */,
				other_notes /* no default value */,
				isnull(study_date,sysdatetime()),
				geography /* no default value */,
				study_type /* no default value */,
				isnull(study_state,'C'),
				comparison_geolevel_name /* no default value */,
				denom_tab /* no default value */,
				direct_stand_tab /* no default value */,
				study_geolevel_name /* no default value */,
				isnull(map_table, 'S' + CAST(@study_id AS VARCHAR) + '_MAP') /* S<study_id>_MAP */,
				suppression_value /* no default value */,
				isnull(extract_permitted, 0),
				isnull(transfer_permitted, 0),
				authorised_by /* no default value */,
				authorised_on /* no default value */,
				authorised_notes /* no default value */,
				isnull(audsid, @@spid),
				project /* no default value */,
				isnull(stats_method, 'NONE')
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
		left outer join inserted b on a.study_id=b.study_id 
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51132, @update_invalid_user);
		THROW 51132, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_studies]';
		THROW 51132, @err_msg2, 1;
	END CATCH;	
	
--
-- IG update: extract_permitted, transfer_permitted, authorised_by, authorised_on, authorised_notes
-- State change: study_state
--
	UPDATE a
       SET extract_permitted=inserted.extract_permitted, 
           transfer_permitted=inserted.transfer_permitted,
           authorised_by=inserted.authorised_by, 
           authorised_on=inserted.authorised_on, 
           authorised_notes=inserted.authorised_notes,
           study_state=inserted.study_state,
		   select_state=inserted.select_state,
		   print_state=inserted.print_state
      FROM [rif40].[t_rif40_studies] a
	  JOIN inserted ON (inserted.study_id = a.study_id);
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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51133, @delete_invalid_user);
		THROW 51133, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_studies]';
		THROW 51133, @err_msg3, 1;
	END CATCH;		
	
	DELETE FROM [rif40].[t_rif40_studies]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_studies].study_id);
		
END;

END;
GO

--
-- Eof