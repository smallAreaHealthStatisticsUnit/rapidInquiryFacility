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
	
--
-- Save sequence in current valid sequences object for later use by
-- CURRVAL function: [rif40].[rif40_sequence_current_value]()
--

DECLARE @log_msg0 VARCHAR(MAX) = '@XTYPE='+COALESCE(@XTYPE, 'NULL')+
	'; OBJECT_ID tempdb..##t_rif40_studies_seq='+
	COALESCE(CAST(OBJECT_ID('tempdb..##t_rif40_studies_seq') AS VARCHAR), 'NULL');
EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg0;

IF @XTYPE = 'I' AND OBJECT_ID('tempdb..##t_rif40_studies_seq') IS NOT NULL 
	INSERT INTO ##t_rif40_studies_seq(study_id)
	SELECT study_id
	  FROM INSERTED;
  
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
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51013, @not_user);
		THROW 51013, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51013, @err_msg1, 1;
	END CATCH;
END;

IF @XTYPE = 'U'
BEGIN	
	DECLARE @state_change VARCHAR(MAX) = 
	(
		SELECT a.study_id, b.study_id AS new_study_id, a.study_state, b.study_state AS new_state
		  FROM inserted a, deleted b
		   FOR XML PATH(''));
	SET @state_change = 'State change: ' + @state_change;
	DECLARE @inserted VARCHAR(MAX) = 
	(
		SELECT a.*
		  FROM inserted a
		   FOR XML PATH('row'));
	SET @inserted = 'Inserted: ' + REPLACE(@inserted, '><', '>'+CHAR(10)+'<');
	DECLARE @deleted VARCHAR(MAX) = 
	(
		SELECT b.*
		  FROM deleted b
		   FOR XML PATH('row'));
	SET @deleted = 'Deleted: ' + REPLACE(@deleted, '><', '>'+CHAR(10)+'<');

	--UPDATE not allowed except for IG admin and state changes.

	DECLARE @is_state_change VARCHAR(MAX) = (
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
		and a.suppression_value=b.suppression_value
		and a.extract_permitted=b.extract_permitted
		and a.transfer_permitted=b.transfer_permitted
		and a.authorised_by=b.authorised_by
		and a.authorised_on=b.authorised_on
		and a.authorised_notes=b.authorised_notes
		and a.audsid=b.audsid
		and a.print_state=b.print_state
		and a.select_state=b.select_state
		FOR XML PATH(''));
	
	DECLARE @is_ig_update VARCHAR(MAX) = ( 
		SELECT a.study_id
		FROM inserted a, deleted b
		where a.study_id=b.study_id
		and (COALESCE(a.extract_permitted, '')  != COALESCE(b.extract_permitted, '')
		or   COALESCE(a.transfer_permitted, '') != COALESCE(b.transfer_permitted, '')
		or   COALESCE(a.authorised_by, '')      != COALESCE(b.authorised_by, '')
		or   COALESCE(a.authorised_on, '')      != COALESCE(b.authorised_on, '')
		or   COALESCE(a.authorised_notes, '')   != COALESCE(b.authorised_notes, ''))
		FOR XML PATH(''));	
		
	DECLARE @is_printselectstate VARCHAR(MAX) = ( 
		SELECT a.study_id, b.print_state, b.select_state
		FROM inserted a, deleted b
		where a.study_id=b.study_id
		and (COALESCE(a.print_state, '')  != COALESCE(b.print_state, '')
		or   COALESCE(a.select_state, '') != COALESCE(b.select_state, ''))
		FOR XML PATH(''));

	IF @is_state_change IS NOT NULL AND @is_ig_update IS NOT NULL 
	BEGIN
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 'doing a state change and IG update at the same time?';
	END;

	IF @is_state_change IS NOT NULL 
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
				DECLARE @err_msg4 VARCHAR(MAX) = formatmessage(51016, @not_first_user);
				THROW 51016, @err_msg4, 1;
			END TRY
			BEGIN CATCH
				EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[tr_studies_checks]';
				THROW 51016, @err_msg4, 1;
			END CATCH;
		END;
		ELSE	
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 'UPDATE state changes allowed on T_RIF40_STUDIES by user';
	END;
	
	IF @is_printselectstate IS NOT NULL
	BEGIN
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', 'UPDATE print/select state changes allowed on T_RIF40_STUDIES by user';
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
			DECLARE @log_msg1 VARCHAR(MAX) = 'UPDATE IG changes allowed on T_RIF40_STUDIES by USER '+SUSER_NAME();
			EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg1;
		END
	END
	
	-- This is trouble as is causes: Table name: [rif40].[t_rif40_studies] , UPDATE failed - non IG UPDATE not allowed on T_RIF40_STUDIES by user: (peter)
	-- if you do not change the record. This may need changing in future
--	IF @is_state_change IS NULL AND @is_ig_update IS NULL AND @is_printselectstate IS NULL
--	BEGIN TRY
--
--		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @state_change;
--		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @inserted;
--		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @deleted;
--
--		rollback;
--		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51015, SUSER_NAME());
--		THROW 51015, @err_msg3, 1;
--	END TRY
--	BEGIN CATCH
--		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
--		THROW 51015, @err_msg3, 1;
--	END CATCH;
	
END;

IF @XTYPE = 'D'
BEGIN
	DECLARE @not_user_del VARCHAR(max) = (
		SELECT username as original_owner
		FROM inserted
		WHERE username != SUSER_NAME()
		FOR XML PATH('')
	);
	IF @not_user_del IS NOT NULL
	BEGIN TRY
		rollback
		DECLARE @err_msg5 VARCHAR(MAX) = formatmessage(51017,@not_user_del);
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
			DECLARE @err_msg6 VARCHAR(MAX) = formatmessage(51018, @geolevel_name);
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
			DECLARE @err_msg7 VARCHAR(MAX) = formatmessage(51019, @geolevel_name2);
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
	DECLARE @err_msg8 VARCHAR(MAX) = formatmessage(51020, @direct_denom);
	THROW 51020, @err_msg8, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51020, @err_msg8, 1;
END CATCH;
ELSE
BEGIN
	DECLARE @log_msg8 VARCHAR(MAX) = 'direct_stand_tab is a direct denominator table';
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
	DECLARE @err_msg9 VARCHAR(MAX) = formatmessage(51021, @indirect_denom);
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
SELECT a.TABLE_NAME 
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
	DECLARE @err_msg10 VARCHAR(MAX) = formatmessage(51022, @min_age);
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

DECLARE @resolution_check varchar(max); 
with a as (
	select geolevel_id, a.study_id
	from [rif40].[t_rif40_studies] a, [rif40].[t_rif40_geolevels] b
	where a.study_geolevel_name=b.geolevel_name
	and a.geography=b.geography
),
b as (
	select geolevel_id, a.study_id
	from [rif40].[t_rif40_studies]  a, [rif40].[t_rif40_geolevels] b
	where a.comparison_geolevel_name=b.geolevel_name
	and a.geography=b.geography)
select @resolution_check=(
	SELECT a.study_id, a.geolevel_id as study_geolevel_id, b.geolevel_id as comparison_geolevel_id
	from a, b
	where a.study_id=b.study_id
	and a.geolevel_id < b.geolevel_id
	FOR XML PATH(''));
/*
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
*/


IF @resolution_check IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg11 VARCHAR(MAX) = formatmessage(51023, @resolution_check);
	THROW 51023, @err_msg11, 1;
END TRY
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
 from [rif40].[t_rif40_parameters]
 where param_name='SuppressionValue') b
where [rif40].[rif40_has_role](a.username,'rif_student') = 1
and ([rif40].[rif40_has_role](a.username,'rif_no_suppression')=1 
     or param_value is null or param_value=0)
FOR XML PATH('')
);

IF @suppress_student_check IS NOT NULL
BEGIN TRY
    rollback;
    DECLARE @err_msg12 VARCHAR(MAX) = formatmessage(51024, @suppress_student_check);
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
 from [rif40].[t_rif40_parameters]
 where param_name='SuppressionValue') b
FOR XML PATH('')
);
    DECLARE @log_msg12 VARCHAR(MAX) = 'Study suppressed at: '+@suppress_student_ok;
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
		DECLARE @err_msg13 VARCHAR(MAX) = formatmessage(51025, @restricted_student_prob);
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
		DECLARE @log_msg13 VARCHAR(MAX) = 'T_RIF40_STUDIES study may be extracted, study geolevel  is not restricted for user: '+@restricted_student_check;
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg13;
		
		--is there a better way to do that:
		UPDATE [rif40].[t_rif40_studies] 
		set extract_permitted=1, transfer_permitted=1, authorised_by=SUSER_SNAME(), authorised_on=getdate(), authorised_notes='Auto authorised; study geolevel is not restricted'
		where exists (
		select 1
		from inserted b
		where [rif40].[t_rif40_studies].study_id=b.study_id
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
			DECLARE @err_msg14 VARCHAR(MAX) = formatmessage(51026, @restricted_geolevel_check);
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
		DECLARE @err_msg15 VARCHAR(MAX) = formatmessage(51027, @student_extract_prob);
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
		DECLARE @log_msg15 VARCHAR(MAX) = 'T_RIF40_STUDIES study may be extracted, user RIF_STUDENT/RIF_MANAGER tests passed: '+@rif_manager_extract;
		EXEC [rif40].[rif40_log] 'DEBUG1', '[rif40].[t_rif40_studies]', @log_msg15;
		
		--is there a better way to do that:
		UPDATE [rif40].[t_rif40_studies] 
		set extract_permitted=1,  authorised_by=SUSER_SNAME(), authorised_on=getdate()
		where exists (
		select 1
		from inserted b
		where [rif40].[t_rif40_studies].study_id=b.study_id
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
		DECLARE @err_msg16 VARCHAR(MAX) = formatmessage(51028, @not_manager_extract_prob);
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
	update [rif40].[t_rif40_studies] 
	set transfer_permitted=0
	where exists (select 1
				from [rif40].[t_rif40_geolevels] b
				where restricted=1
				and b.geography=[rif40].[t_rif40_studies].geography
				and b.geolevel_name=[rif40].[t_rif40_studies].study_geolevel_name);
	update [rif40].[t_rif40_studies] 
	set transfer_permitted=extract_permitted
	where exists (select 1
				from [rif40].[t_rif40_geolevels] b
				where restricted!=1
				and b.geography=[rif40].[t_rif40_studies].geography
				and b.geolevel_name=[rif40].[t_rif40_studies].study_geolevel_name);
END;

--
-- Check extract_table, map_table Oracle name, access (dependent on state)
--

--valid Oracle name checks:
DECLARE studies_name_cursor CURSOR FOR
	SELECT  extract_table, map_table
	from inserted;
DECLARE @curs_extract_table VARCHAR(MAX), @curs_map_table VARCHAR(MAX);
OPEN studies_name_cursor;
FETCH studies_name_cursor INTO @curs_extract_table,@curs_map_table;
WHILE @@FETCH_STATUS = 0  
BEGIN  
	EXEC [rif40].[rif40_db_name_check] 'EXTRACT_TABLE', @curs_extract_table;
	EXEC [rif40].[rif40_db_name_check] 'MAP_TABLE', @curs_map_table;
	FETCH studies_name_cursor INTO @curs_extract_table,@curs_map_table;
END;
CLOSE studies_name_cursor ;
DEALLOCATE studies_name_cursor ;

DECLARE @extract_table_access_prob VARCHAR(MAX) = 
(
	select study_id, extract_table, study_state, OBJECT_ID('rif_studies.' + LOWER(extract_table)) AS extract_table_object_id
	from inserted
	where OBJECT_ID('rif_studies.' + LOWER(extract_table)) IS NULL 
	and study_state  NOT IN ('C', 'V', 'U')
	FOR XML PATH('')
);

IF @extract_table_access_prob IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg17 VARCHAR(MAX) = formatmessage(51029, @extract_table_access_prob);
	THROW 51029, @err_msg17, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51029, @err_msg17, 1; 
END CATCH;

DECLARE @map_table_access_prob VARCHAR(MAX) = 
(
	select study_id, map_table, study_state, OBJECT_ID('rif_studies.' + LOWER(map_table)) AS map_table_object_id
	from inserted
	where OBJECT_ID('rif_studies.' + LOWER(map_table)) IS NULL 
	and study_state  NOT IN ('C', 'V', 'U', 'E')
	FOR XML PATH('')
);
IF @map_table_access_prob IS NOT NULL
BEGIN TRY
	rollback;
	DECLARE @err_msg18 VARCHAR(MAX) = formatmessage(51098, @map_table_access_prob);
	THROW 51098, @err_msg18, 1;
END TRY
BEGIN CATCH
	EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
	THROW 51098, @err_msg18, 1; 
END CATCH;

--
-- Delayed RIF40_TABLES denominator checks:
-- Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists
--
IF @has_studies_check > 0 or SUSER_SNAME() <> 'RIF40'
BEGIN
	DECLARE @denom_total_missing VARCHAR(MAX) = 
	(
		SELECT study_id, denom_tab, c.total_field
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.denom_tab=c.table_name
		AND a.denom_tab IS NOT NULL
		AND c.total_field IS NOT NULL AND c.total_field <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.denom_tab
			AND b.column_name=c.total_field)
		FOR XML PATH('')
	);
	IF @denom_total_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg19 VARCHAR(MAX) = formatmessage(51099, @denom_total_missing);
		THROW 51099, @err_msg19, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51099, @err_msg19, 1; 
	END CATCH;

	DECLARE @denom_sex_missing VARCHAR(MAX) = 
	(
		SELECT study_id, denom_tab, c.sex_field_name
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.denom_tab=c.table_name
		AND a.denom_tab IS NOT NULL
		AND c.sex_field_name IS NOT NULL AND c.sex_field_name <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.denom_tab
			AND b.column_name=c.sex_field_name)
		FOR XML PATH('')
	);
	IF @denom_sex_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg20 VARCHAR(MAX) = formatmessage(51100, @denom_sex_missing);
		THROW 51100, @err_msg20, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51100, @err_msg20, 1; 
	END CATCH;

	DECLARE @denom_age_missing VARCHAR(MAX) = 
	(
		SELECT study_id, denom_tab, c.age_group_field_name
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.denom_tab=c.table_name
		AND a.denom_tab IS NOT NULL
		AND c.age_group_field_name IS NOT NULL AND c.age_group_field_name <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.denom_tab
			AND b.column_name=c.age_group_field_name)
		FOR XML PATH('')
	);
	IF @denom_age_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg21 VARCHAR(MAX) = formatmessage(51101, @denom_age_missing);
		THROW 51101, @err_msg21, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51101, @err_msg21, 1; 
	END CATCH;	
	
	DECLARE @denom_age_sex_missing VARCHAR(MAX) = 
	(
		SELECT study_id, denom_tab, c.age_sex_group_field_name
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.denom_tab=c.table_name
		AND a.denom_tab IS NOT NULL
		AND c.age_sex_group_field_name IS NOT NULL AND c.age_sex_group_field_name <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.denom_tab
			AND b.column_name=c.age_sex_group_field_name)
		FOR XML PATH('')
	);
	IF @denom_age_sex_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg22 VARCHAR(MAX) = formatmessage(51102, @denom_age_sex_missing);
		THROW 51102, @err_msg22, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51102, @err_msg22, 1; 
	END CATCH;	
	
--
-- Delayed RIF40_TABLES direct standardisation table checks:
-- Check - Column <TABLE_NAME>.TOTAL_FIELD, SEX_FIELD_NAME, AGE_GROUP_FIELD_NAME, AGE_SEX_GROUP_FIELD_NAME exists
--
	DECLARE @dirstd_total_missing VARCHAR(MAX) = 
	(
		SELECT study_id, direct_stand_tab, c.total_field
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.direct_stand_tab=c.table_name
		AND a.direct_stand_tab IS NOT NULL
		AND c.total_field IS NOT NULL AND c.total_field <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.direct_stand_tab
			AND b.column_name=c.total_field)
		FOR XML PATH('')
	);
	IF @dirstd_total_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg23 VARCHAR(MAX) = formatmessage(51103, @dirstd_total_missing);
		THROW 51103, @err_msg23, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51103, @err_msg23, 1; 
	END CATCH;
	
	DECLARE @dirstd_sex_missing VARCHAR(MAX) = 
	(
		SELECT study_id, direct_stand_tab, c.sex_field_name
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.direct_stand_tab=c.table_name
		AND a.direct_stand_tab IS NOT NULL
		AND c.sex_field_name IS NOT NULL AND c.sex_field_name <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.direct_stand_tab
			AND b.column_name=c.sex_field_name)
		FOR XML PATH('')
	);
	IF @dirstd_sex_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg24 VARCHAR(MAX) = formatmessage(51104, @dirstd_sex_missing);
		THROW 51104, @err_msg24, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51104, @err_msg24, 1; 
	END CATCH;	
	
	DECLARE @dirstd_age_missing VARCHAR(MAX) = 
	(
		SELECT study_id, direct_stand_tab, c.age_group_field_name
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.direct_stand_tab=c.table_name
		AND a.direct_stand_tab IS NOT NULL
		AND c.age_group_field_name IS NOT NULL AND c.age_group_field_name <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.direct_stand_tab
			AND b.column_name=c.age_group_field_name)
		FOR XML PATH('')
	);
	IF @dirstd_age_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg25 VARCHAR(MAX) = formatmessage(51105, @dirstd_age_missing);
		THROW 51105, @err_msg25, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51105, @err_msg25, 1; 
	END CATCH;	
	
	DECLARE @dirstd_age_sex_missing VARCHAR(MAX) = 
	(
		SELECT study_id, direct_stand_tab, c.age_sex_group_field_name
		FROM inserted a, [rif40].[rif40_tables] c
		WHERE a.direct_stand_tab=c.table_name
		AND a.direct_stand_tab IS NOT NULL
		AND c.age_sex_group_field_name IS NOT NULL AND c.age_sex_group_field_name <> ''
		AND NOT EXISTS (
			SELECT 1
			FROM INFORMATION_SCHEMA.COLUMNS b
			WHERE b.table_name=a.direct_stand_tab
			AND b.column_name=c.age_sex_group_field_name)
		FOR XML PATH('')
	);
	IF @dirstd_age_sex_missing IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg26 VARCHAR(MAX) = formatmessage(51106, @dirstd_age_sex_missing);
		THROW 51106, @err_msg26, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[t_rif40_studies]';
		THROW 51106, @err_msg26, 1; 
	END CATCH;	
	
END;

END;

END;
GO
