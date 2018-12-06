
IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.views t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_rif40_contextual_stats')
BEGIN
	DROP TRIGGER [rif40].[tr_rif40_contextual_stats];
END
GO


------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_rif40_contextual_stats]
on [rif40].[rif40_contextual_stats]
instead of insert , update , delete
as
BEGIN 
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
DECLARE  @XTYPE varchar(5);
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
		SELECT SUSER_SNAME() AS username, study_id, inv_id, area_id
		  FROM inserted
		 WHERE NOT (username = SUSER_SNAME() OR username is null)		  /* Not the study owner */
		   AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_user')    != 1 /* Not a rif_user or a rif_manager */
	       AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') != 1
		FOR XML PATH('')
	);
	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51110, @insert_invalid_user);
		THROW 51110, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_contextual_stats]';
		THROW 51110, @err_msg1, 1;
	END CATCH;	

	INSERT INTO [rif40].[t_rif40_contextual_stats] (
				username,
				study_id,
				inv_id,
				area_id,
				area_population,
				area_observed,
				total_comparison_population,
				variance_high,
				variance_low)
	SELECT isnull(username, SUSER_SNAME()),
		isnull(study_id, [rif40].[rif40_sequence_current_value]('rif40_study_id_seq')),
		isnull(inv_id, [rif40].[rif40_sequence_current_value]('rif40_inv_id_seq')),
		area_id,
		area_population,
		area_observed,
		total_comparison_population,
		variance_high,
		variance_low
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
		left outer join inserted b on a.study_id=b.study_id and a.inv_id=b.inv_id and a.area_id=b.area_id
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51111, @update_invalid_user);
		THROW 51111, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_contextual_stats]';
		THROW 51111, @err_msg2, 1;
	END CATCH;	
	
	delete from [rif40].[t_rif40_contextual_stats]
	where exists (
		select 1
		from deleted b
		where [rif40].[t_rif40_contextual_stats].study_id=b.study_id
		and [rif40].[t_rif40_contextual_stats].inv_id=b.inv_id
		and [rif40].[t_rif40_contextual_stats].area_id=b.area_id);

	INSERT INTO [rif40].[t_rif40_contextual_stats] (
				username,
				study_id,
				inv_id,
				area_id,
				area_population,
				area_observed,
				total_comparison_population,
				variance_high,
				variance_low)
	SELECT username,
		study_id,
		inv_id,
		area_id,
		area_population,
		area_observed,
		total_comparison_population,
		variance_high,
		variance_low
	FROM inserted;	
END;

IF @XTYPE = 'D'
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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51112, @delete_invalid_user);
		THROW 51112, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_contextual_stats]';
		THROW 51112, @err_msg3, 1;
	END CATCH;	

	delete from [rif40].[t_rif40_contextual_stats]
	where exists (
		select 1
		from deleted b
		where [rif40].[t_rif40_contextual_stats].study_id=b.study_id
		and [rif40].[t_rif40_contextual_stats].inv_id=b.inv_id
		and [rif40].[t_rif40_contextual_stats].area_id=b.area_id);
END;

END;
GO
