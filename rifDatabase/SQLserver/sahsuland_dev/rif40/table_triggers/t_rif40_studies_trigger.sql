--NOT COMPLETE!!

/*
trigger for  
Check - USERNAME exists.
Check - USERNAME is Kerberos USER on INSERT.
Check - audsid is SYS_CONTEXT('USERENV', 'SESSIONID') on INSERT.
Check - UPDATE not allowed except for IG admin and state changes.
Check - DELETE only allowed on own records.
Check - EXTRACT_TABLE Oracle name.
Check - Comparison area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1
Check - DENOM_TAB, DIRECT_STAND_TAB are valid Oracle names and appropriate denominators, and user has access.
Check - Study area resolution (GEOLEVEL_ID) >= comparision area resolution (GEOLEVEL_ID)  [i.e study area has the same or higher resolution]

Check - suppression_value - Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter "SuppressionValue". If >0 all results with the value or below will be set to 0.
stopped here, have not implemented the rest:
Check - extract_permitted - Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any extract if the system parameter ExtractControl=1. This is enforced by the RIF application.

Check - authorised_by - must be a RIF MANAGER.

Check - transfer_permitted - Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a RIF MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be transferred

Check - authorised_notes -IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1

Delayed RIF40_TABLES denominator and direct standardisation checks:
Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists

IF USER = NEW.username (i.e. not initial RIF40 INSERT) THEN
	grant to all shared users if not already granted
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT *  FROM sys.triggers tr
INNER JOIN sys.tables t ON tr.parent_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'rif40') 
and tr.name=N'tr_studies_checks')
BEGIN
	DROP TRIGGER [rif40].[tr_studies_checks]
END
GO

------------------------------
-- create trigger code 
------------------------------
CREATE trigger [rif40].[tr_studies_checks]
on [rif40].[t_rif40_studies]
AFTER insert , update , delete
as
BEGIN 
--------------------------------------
--to  Determine the type of transaction 
---------------------------------------
Declare  @XTYPE varchar(5);

	IF EXISTS (SELECT * FROM DELETED)
	BEGIN
		SET @XTYPE = 'D'
	END;
	
	IF EXISTS (SELECT * FROM INSERTED)
	BEGIN
		IF (@XTYPE = 'D')
			SET @XTYPE = 'U'
		ELSE 
			SET @XTYPE = 'I'
	END;

DECLARE @has_studies_check VARCHAR(MAX) = 
(
	SELECT count(study_id) as total
	FROM [rif40].[t_rif40_results]
);
	
--check is new username = current user? for update and insert
IF (@XTYPE = 'I' or @XTYPE = 'U')
BEGIN
	DECLARE @not_user VARCHAR(max) = (
		SELECT username
		FROM inserted
		WHERE username != SUSER_NAME()
		FOR XML PATH(''));
	IF @not_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 = formatmessage(51013, @not_user);
		THROW 51013, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51013, @err_msg1, 1;
	END CATCH;
END;

IF @XTYPE = 'U'
BEGIN	
	
	--UPDATE not allowed except for IG admin and state changes.
	DECLARE @is_state_change	int, @is_ig_update int;

	SET @is_state_change = (
		SELECT a.study_id 
		FROM inserted a, deleted b
		where a.study_state != b.study_state
		and a.study_id=b.study_id
		and a.geography=b.geography
		and a.project=b.project
		and a.study_name=b.study_name
		and a.extract_table=b.extract_table
		and a.map_table=b.map_table
		and a.study_date=b.study_date
		and a.study_type=b.study_type
		and a.comparison_geolevel_name=b.comparison_geolevel_name
		and a.study_geolevel_name=b.study_geolevel_name
		and a.denom_tab=b.denom_tab
		and a.direct_stand_tab=b.direct_stand_tab
		and a.suppression_value=b.suppression_value
		and a.extract_permitted=b.extract_permitted
		and a.transfer_permitted=b.transfer_permitted
		and a.authorised_by=b.authorised_by
		and a.authorised_on=b.authorised_on
		and a.authorised_notes=b.authorised_notes
		and a.audsid=b.audsid
		FOR XML PATH(''));
	
	SET @is_ig_update = ( 
		SELECT a.study_id
		FROM inserted a, deleted b
		where a.study_id=b.study_id
		and a.geography=b.geography
		and a.project=b.project
		and a.study_name=b.study_name
		and a.extract_table=b.extract_table
		and a.map_table=b.map_table
		and a.study_date=b.study_date
		and a.study_type=b.study_type
		and a.comparison_geolevel_name=b.comparison_geolevel_name
		and a.study_geolevel_name=b.study_geolevel_name
		and a.denom_tab=b.denom_tab
		and a.direct_stand_tab=b.direct_stand_tab
		and a.suppression_value=b.suppression_value
		and a.audsid=b.audsid
		and (a.extract_permitted!=b.extract_permitted
		or a.transfer_permitted!=b.transfer_permitted
		or a.authorised_by!=b.authorised_by
		or a.authorised_on!=b.authorised_on
		or a.authorised_notes!=b.authorised_notes)
		FOR XML PATH(''));

	IF @is_state_change NOT NULL AND @is_ig_update NOT NULL 
	BEGIN
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 'doing a state change and IG update at the same time?';
	END;

	IF @is_state_change NOT NULL 
    BEGIN

	--check if new username = old username for the study
		DECLARE @not_first_user VARCHAR(max) = (
			SELECT a.study_id, a.username as new_user, b.username as original_user
			FROM inserted a, deleted b
			WHERE a.study_id=b.study_id
			and a.username != b.username
			FOR XML PATH(''));
		IF @not_first_user IS NOT NULL
		BEGIN
			IF @has_studies_check = 0 AND SUSER_SNAME()='RIF40'
			BEGIN
				EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 't_rif40_studies insert/update allowed during build';
			END;
			ELSE
			BEGIN TRY
				rollback;
				DECLARE @err_msg4 = formatmessage(51016, @not_first_user);
				THROW 51016, @err_msg4, 1;
			END TRY
			BEGIN CATCH
				EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_studies_checks]';
				THROW 51016, @err_msg4, 1;
			END CATCH;
		END;
		ELSE	
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 'UPDATE state changes allowed on T-RIF40_STUDIES by user';
	END;
	
	IF @is_ig_update IS NOT NULL
	BEGIN
		--only rif_manager can make IG changes
		DECLARE @not_rif_manager VARCHAR(max) = (
			SELECT study_id, username
			from inserted
			WHERE [rif40].[rif40_has_role](username,'rif_manager') = 0
			FOR XML PATH(''));	
		
		IF @not_rif_manager IS NOT NULL
		BEGIN TRY
			rollback;
			DECLARE @err_msg2 VARCHAR(max) = formatmessage(51014, @not_rif_manager);
			THROW 51014, @err_msg2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
			THROW 51014, @err_msg2, 1;
		END CATCH
		ELSE
		BEGIN 
			DECLARE @log_msg1 = 'UPDATE IG changes allowed on T_RIF40_STUDIES by USER '+SUSER_NAME();
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg1;
		END
	END
	
	IF @is_state_change IS NULL AND @is_ig_update IS NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg3 = formatmessage(51015, SUSER_NAME());
		THROW 51015, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51015, @err_msg3, 1;
	END CATCH;
	
END;

IF @XTYPE = 'D'
BEGIN
	DECLARE @not_user VARCHAR(max) = (
		SELECT username as original_owner
		FROM inserted
		WHERE username != SUSER_NAME()
		FOR XML PATH(''));
	IF @not_user IS NOT NULL
	BEGIN TRY
		rollback
		DECLARE @err_msg5 = formatmessage(51017,@not_user);
		THROW 51017, @err_msg5, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51017, @err_msg5, 1;
	END CATCH;
END;	

	
------------------------------------------------------------------------------------------
--Check - Comparison area geolevel name. 
--Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1
------------------------------------------------------------------------------------------------
IF (@XTYPE = 'I' or @XTYPE = 'U')
BEGIN
	 DECLARE @geolevel_name nvarchar(MAX) =
		(
		SELECT ic.study_id,ic.COMPARISON_GEOLEVEL_NAME,ic.geography 
		FROM inserted ic 
		WHERE NOT EXISTS (
		SELECT 1 
		FROM [rif40].[t_rif40_geolevels] c
		WHERE ic.[geography]=c.[GEOGRAPHY] and 
			c.geolevel_name=ic.[COMPARISON_GEOLEVEL_NAME] and
			c.[COMPAREA]=1
		)
		FOR XML PATH('')
		 );

	IF @geolevel_name IS NOT NULL
		BEGIN TRY
			rollback;
			DECLARE @err_msg6 = formatmessage(51018, @geolevel_name);
			THROW 51018, @err_msg6, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
			THROW 51018, @err_msg6, 1;
		END CATCH;

-----------------------------------------------------------------------------------------------------------
-- Check - STUDY_GEOLEVEL_NAME. Must be a valid GEOLEVEL_NAME for the study GEOGRAPHY in T_RIF40_GEOLEVELS
-----------------------------------------------------------------------------------------------------------

	 DECLARE @geolevel_name2 nvarchar(MAX) =
		(
		SELECT STUDY_ID,STUDY_GEOLEVEL_NAME,geography 
		FROM inserted ic 
		WHERE NOT EXISTS (
			SELECT 1
			FROM [rif40].[t_rif40_geolevels] c 
			WHERE ic.[geography]=c.[GEOGRAPHY] and 
			c.geolevel_name=ic.[STUDY_GEOLEVEL_NAME] 
		)
		FOR XML PATH('')
		);

	IF @geolevel_name2 IS NOT NULL
		BEGIN TRY
			rollback;
			DECLARE @err_msg7 = formatmessage(51019, @geolevel_name2);
			THROW 51019, @err_msg7, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
			THROW 51019, @err_msg7, 1;
		END CATCH;

-------------------------------------
-- Check -  direct denominator
------------------------------------

DECLARE @direct_denom nvarchar(MAX) =
 (
SELECT a.TABLE_NAME + '  '
FROM [rif40].[rif40_tables] a, inserted b
WHERE a.table_name=b.direct_stand_tab
AND a.ISDIRECTDENOMINATOR <>1
 FOR XML PATH('')
);

IF @direct_denom IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg8 = formatmessage(51020, @direct_denom);
	THROW 51020, @err_msg8, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51020, @err_msg8, 1;
END CATCH;
ELSE
BEGIN
	DECLARE @log_msg8 = 'direct_stand_tab is a direct denominator table';
	EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg8;
END;

DECLARE @indirect_denom nvarchar(MAX) =
 (
SELECT a.TABLE_NAME + '  '
FROM [rif40].[rif40_tables] a, inserted b
WHERE a.table_name=b.denom_tab
and a.ISINDIRECTDENOMINATOR <>1
 FOR XML PATH('')
)

IF @indirect_denom IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg9 = formatmessage(51021, @indirect_denom);
	THROW 51021, @err_msg9, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51021, @err_msg9, 1;
END CATCH;

	
------------------------------
-- max/min age group is null
------------------------------
DECLARE @min_age nvarchar(MAX) =
 (
SELECT a.TABLE_NAME + '  '
FROM [rif40].[rif40_tables] a, inserted b
WHERE a.table_name=b.denom_tab
AND NOT EXISTS (
	SELECT 1
	FROM [rif40].[rif40_age_groups] g 
	WHERE g.age_group_id  = a.age_group_id and g.offset is not null
)
FOR XML PATH('')
);

IF @min_age IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg10 = formatmessage(51022, @min_age);
	THROW 51022, @err_msg10, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51022, @err_msg10, 1;
END CATCH;
ELSE
	EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 'T_RIF40_STUDIES year/age bands checks OK against RIF40_TABLES';

--
-- Check - Study area resolution (GEOLEVEL_ID) >= comparison area resolution (GEOLEVEL_ID)
--
DECLARE @resolution_check varchar(max) = 
(
SELECT a.study_id, a.geolevel_id as study_geolevel_id, b.geolevel_id as comparison_geolevel_id
from (select geolevel_id, a.study_id
from inserted a, [rif40].[t_rif40_geolevels] b
where a.study_geolevel_name=b.geolevel_name
and a.geography=b.geography) a,
(select geolevel_id, a.study_id
from inserted a, [rif40].[t_rif40_geolevels] b
where a.comparison_geolevel_name=b.geolevel_name
and a.geography=b.geography) b
where a.study_id=b.study_id
and a.geolevel_id < b.geolevel_id
);

IF @resolution_check IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg11 = formatmessage(51023, @resolution_check);
	THROW 51023, @err_msg11, 1;
END TRY;
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51023, @err_msg11, 1;
END CATCH;
ELSE
	EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 'study area geolevel id >= comparision area [i.e study area has the same or higher resolution]';

--
-- Check - suppression_value - Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a
-- RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter "SuppressionValue". If >0 all results with the value or below will be set to 0.

--is this necessary, or will that be checked in the parameter table setup?
DECLARE @suppress_student_check varchar(max) = 
(  
    select study_id, a.username, case when [rif40].[rif40_has_role](a.username,'rif_no_suppression')=1 then 0
else b.param_value end as suppression_value
from inserted a, (select param_value 
 from [rif40].[t_rif40_paramaters]
 where param_name='SuppressionValue') b
where [rif40].[rif40_has_role](a.username,'rif_student') = 1
and ([rif40].[rif40_has_role](a.username,'rif_no_suppression')=1 
     or param_value is null or param_value=0)
FOR XML PATH('')
);

IF @suppress_student_check IS NOT NULL
BEGIN TRY
    rollback;
    DECLARE @err_msg12 = formatmessage(51024, @suppress_student_check);
    THROW 51024, @err_msg12, 1;
END TRY
BEGIN CATCH
   	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51024, @err_msg12, 1; 
END CATCH
ELSE
    BEGIN
    DECLARE @suppress_student_ok varchar(max) = 
(  
    select study_id, a.username, case when [rif40].[rif40_has_role](a.username,'rif_no_suppression')=1 then 0
else b.param_value end as suppression_value
from inserted a, (select param_value 
 from [rif40].[t_rif40_paramaters]
 where param_name='SuppressionValue') b
FOR XML PATH('')
);
    DECLARE @log_msg12 = 'Study suppressed at: '+@supress_student_ok;
    EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg12;
END;


--
-- Check - extract_permitted - Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create
-- and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1).
-- If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a
-- RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any
-- extract if the system parameter ExtractControl=1. This is enforced by the RIF application.
--

DECLARE @extractcontrol int = 
( 
	select param_value
	from [rif40].[t_rif40_parameters]
	where param_name='ExtractControl'
);

IF @extractcontrol != 0 AND @xtype='I'
BEGIN
	DECLARE @restricted_student_prob	VARCHAR(MAX) =
	(
		select study_id, geography, study_geolevel_name, username
		from inserted a
		where [rif40].[rif40_has_role](a.username,'rif_student')=1
		and exists (select 1
			from [rif40].[t_rif40_geolevels] b
			where restricted=0
			and b.geography=a.geography
			and b.geolevel_name=a.study_geolevel_name)
		FOR XML PATH('')
	);
	IF @restricted_student_prob IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg13 = formatmessage(51025, @restricted_student_prob);
		THROW 51025, @err_msg13, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51025, @err_msg13, 1; 
	END CATCH;
	
	DECLARE @restricted_student_check	VARCHAR(MAX) =
	(
		select study_id, geography, study_geolevel_name, username
		from inserted a
		where [rif40].[rif40_has_role](a.username,'rif_student')=0
		and exists (select 1
			from [rif40].[t_rif40_geolevels] b
			where restricted=0
			and b.geography=a.geography
			and b.geolevel_name=a.study_geolevel_name)
		FOR XML PATH('')
	);
	IF @restricted_student_check IS NOT NULL
	BEGIN
		DECLARE @log_msg13 = 'T_RIF40_STUDIES study may be extracted, study geolevel  is not restricted for user: '+@restricted_student_check;
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg13;
		
		--is there a better way to do that:
		UPDATE [rif40].[t_rif40_studies] a
		set extract_permitted=1, transfer_permitted=1, authorised_by=SUSER_SNAME(), authorised_on=now(), authorised_notes='Auto authorised; study geolevel is not restricted'
		where exists (
		select 1
		from inserted b
		where a.study_id=b.study_id
		and [rif40].[rif40_has_role](b.username,'rif_student')=0
		and exists (select 1
			from [rif40].[t_rif40_geolevels] c
			where restricted=0
			and c.geography=b.geography
			and c.geolevel_name=b.study_geolevel_name));
	END
	ELSE
	BEGIN
		DECLARE @restricted_geolevel_check VARCHAR(MAX) = 
		(
			select study_id, geography, study_geolevel_name, username
			from inserted a
			where [rif40].[rif40_has_role](a.username,'rif_student')=0
			and exists (select 1
				from [rif40].[t_rif40_geolevels] b
				where restricted!=0
				and b.geography=a.geography
				and b.geolevel_name=a.study_geolevel_name)
			FOR XML PATH('')
		);
		IF @restricted_geolevel_check IS NOT NULL
		BEGIN TRY
			rollback;
			DECLARE @err_msg14 = formatmessage(51026, @restricted_geolevel_check);
			THROW 51026, @err_msg14, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
			THROW 51026, @err_msg14, 1; 
		END CATCH;
	END;
END;


IF @extractcontrol != 0 AND @xtype='U'
BEGIN
	DECLARE @student_extract_prob VARCHAR(MAX)=
	(
		select username, study_id
		from inserted
		where [rif40].[rif40_has_role](username,'rif_student')=1
		and extract_permitted=1
		FOR XML PATH('')
	);
	IF @student_extract_prob IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg15 = formatmessage(51027, @student_extract_prob);
		THROW 51027, @err_msg15, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51027, @err_msg15, 1; 
	END CATCH;

	DECLARE @rif_manager_extract VARCHAR(MAX)=
	(
		select username, study_id
		from inserted
		where [rif40].[rif40_has_role](username,'rif_manager')=1
		and extract_permitted=1
		FOR XML PATH('')
	);
	IF @rif_manager_extract IS NOT NULL
	BEGIN
		DECLARE @log_msg15 = 'T_RIF40_STUDIES studymay be extracted, user RIF_STUDENT/RIF_MANAGER tests passed: '+@rif_manager_extract;
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg15;
		
		--is there a better way to do that:
		UPDATE [rif40].[t_rif40_studies] a
		set extract_permitted=1,  authorised_by=SUSER_SNAME(), authorised_on=now()
		where exists (
		select 1
		from inserted b
		where a.study_id=b.study_id
		and b.extract_permitted=1 and [rif40].[rif40_has_role](b.username,'rif_manager')=1);
	END

	DECLARE @not_manager_extract_prob VARCHAR(MAX)=
	(
		select username, study_id
		from inserted
		where extract_permitted=1
		and [rif40].[rif40_has_role](username,'rif_manager')=0
		FOR XML PATH('')
	);
	IF @not_manager_extract_prob IS NOT NULL
		BEGIN TRY
		rollback;
		DECLARE @err_msg16 = formatmessage(51028, @not_manager_extract_prob);
		THROW 51028, @err_msg16, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51028, @err_msg16, 1; 
	END CATCH;
	
--
-- Check - transfer_permitted - Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a
-- RIF_MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be
-- transferred
--
	update [rif40].[t_rif40_studies] a
	set transfer_permitted=0
	where exists (select 1
				from [rif40].[t_rif40_geolevels] b
				where restricted=1
				and b.geography=a.geography
				and b.geolevel_name=a.study_geolevel_name);
	update [rif40].[t_rif40_studies] a
	set transfer_permitted=extract_permitted
	where exists (select 1
				from [rif40].[t_rif40_geolevels] b
				where restricted!=1
				and b.geography=a.geography
				and b.geolevel_name=a.study_geolevel_name);
END;

--
-- Check extract_table, map_table Oracle name, access (dependent on state)
--


END;
