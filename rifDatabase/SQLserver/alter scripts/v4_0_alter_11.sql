-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
-- *
-- ************************************************************************
--
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- $Id$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Inquiry Facility (RIF) - RIF alter script 11 - More risk Analysis Enhancements
--
-- Copyright:
--
-- The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
-- that rapidly addresses epidemiological and public health questions using 
-- routinely collected health and population data and generates standardised 
-- rates and relative risks for any given health outcome, for specified age 
-- and year ranges, for any given geographical area.
--
-- Copyright 2014 Imperial College London, developed by the Small Area
-- Health Statistics Unit. The work of the Small Area Health Statistics Unit 
-- is funded by the Public Health England as part of the MRC-PHE Centre for 
-- Environment and Health. Funding for this project has also been received 
-- from the Centers for Disease Control and Prevention.  
--
-- This file is part of the Rapid Inquiry Facility (RIF) project.
-- RIF is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- RIF is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
-- to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
-- Boston, MA 02110-1301 USA
--
-- Author:
--
-- Peter Hambly, SAHSU
--
-- Working directory: c:/Users/Peter/Documents/GitHub/rapidInquiryFacility/rifDatabase/SQLserver/alter scripts
-- Usage: sqlcmd -U rif40 -d <database name> -b -m-1 -e -r1 -i v4_0_alter_11.sql -v pwd="%cd%"
-- Connect flags if required: -P <password> -S<myServerinstanceName>
--
-- The middleware must be down for this to run
--
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;

--
-- Set schema variable used by scripts etc to RIF_DATA
--
:SETVAR SchemaName "rif_data"
--

BEGIN TRANSACTION;
GO

PRINT 'Running SAHSULAND schema alter script #11 More risk Analysis Enhancements.';
GO

/*

* Alter 11: Risk Analysis Enhancements

 1. Fix to allow delete on RIF40_TABLES;	
 2. Intersection counting (study areas only);
 */

:r ..\sahsuland_dev\rif40\table_triggers\rif40_tables_trigger.sql

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_study_areas'
                  AND column_name  = 'intersect_count') BEGIN
	ALTER TABLE t_rif40_study_areas ADD intersect_count INTEGER NULL;
END
ELSE  BEGIN
	ALTER TABLE t_rif40_study_areas ALTER COLUMN intersect_count INTEGER NULL;
END;
GO		

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'intersect_count' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Number of intersects with shapes', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_study_areas',
		@level2type = N'Column', @level2name = 'intersect_count';
GO

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_study_areas'
                  AND column_name  = 'distance_from_nearest_source') BEGIN
	ALTER TABLE t_rif40_study_areas ADD distance_from_nearest_source NUMERIC NULL;
END
ELSE  BEGIN
	ALTER TABLE t_rif40_study_areas ALTER COLUMN distance_from_nearest_source NUMERIC NULL;
END;
GO		

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'distance_from_nearest_source' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Distance from nearest source (Km)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_study_areas',
		@level2type = N'Column', @level2name = 'distance_from_nearest_source';
GO

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_study_areas'
                  AND column_name  = 'nearest_rifshapepolyid') BEGIN
	ALTER TABLE t_rif40_study_areas ADD nearest_rifshapepolyid NVARCHAR(MAX) NULL;
END;
GO		

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'nearest_rifshapepolyid' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Nearest rifshapepolyid (shape reference)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_study_areas',
		@level2type = N'Column', @level2name = 'nearest_rifshapepolyid';
GO

--
-- Rebuild view
--
ALTER VIEW [rif40].[rif40_study_areas] AS 
SELECT c.username,
    c.study_id,
    c.area_id,
    c.band_id,
	c.intersect_count,
	c.distance_from_nearest_source,
	c.nearest_rifshapepolyid
   FROM [rif40].[t_rif40_study_areas] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
 --permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_areas] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_areas] TO [rif_manager]
GO

--comments
DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'intersect_count' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Number of intersects with shapes', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_areas', @level2type=N'COLUMN',@level2name=N'intersect_count'
GO
DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'distance_from_nearest_source' AND [object_id] = OBJECT_ID(@tableName)))
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Distance from nearest source (Km)', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_areas', @level2type=N'COLUMN',@level2name=N'distance_from_nearest_source'
GO
DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'nearest_rifshapepolyid' AND [object_id] = OBJECT_ID(@tableName)))
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Nearest rifshapepolyid (shape reference)', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_areas', @level2type=N'COLUMN',@level2name=N'nearest_rifshapepolyid'
GO

--
-- Rebuild view trigger
--
------------------------------
-- create trigger code 
------------------------------
ALTER trigger [rif40].[tr_rif40_study_areas]
on [rif40].[rif40_study_areas]
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
		select SUSER_SNAME() AS username
		from inserted
		where NOT (username = SUSER_SNAME() OR username is null)
		OR NOT ([rif40].[rif40_has_role](SUSER_SNAME(),'rif_user') = 1
		AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') = 1)
	);

	IF @insert_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg1 VARCHAR(MAX) = formatmessage(51134, @insert_invalid_user);
		THROW 51134, @err_msg1, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_study_areas]';
		THROW 51134, @err_msg1, 1;
	END CATCH;	

	INSERT INTO [rif40].[t_rif40_study_areas] (
				username,
				study_id,
				area_id,
				band_id,
				intersect_count,
				distance_from_nearest_source,
				nearest_rifshapepolyid)
	SELECT
				isnull(username,SUSER_SNAME()),
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq')),
				area_id /* no default value */,
				band_id /* no default value */,
				intersect_count /* no default value */,
				distance_from_nearest_source /* no default value */,
				nearest_rifshapepolyid /* no default value */
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
		left outer join inserted b on a.study_id=b.study_id and a.area_id=b.area_id
		where a.username != SUSER_SNAME() 
		or (b.username is not null and a.username != b.username)
		FOR XML PATH ('')
	);
	IF @update_invalid_user IS NOT NULL
	BEGIN TRY
		rollback;
		DECLARE @err_msg2 VARCHAR(MAX) = formatmessage(51135, @update_invalid_user);
		THROW 51135, @err_msg2, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_study_areas]';
		THROW 51135, @err_msg2, 1;
	END CATCH;		
	
	DELETE FROM [rif40].[t_rif40_study_areas]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_study_areas].study_id
		AND b.area_id=[rif40].[t_rif40_study_areas].area_id);

	INSERT INTO [rif40].[t_rif40_study_areas] (
				username,
				study_id,
				area_id,
				band_id,
				intersect_count,
				distance_from_nearest_source,
				nearest_rifshapepolyid)
	SELECT
				username,
				study_id,
				area_id,
				band_id,
				intersect_count /* no default value */,
				distance_from_nearest_source /* no default value */,
				nearest_rifshapepolyid /* no default value */
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
		DECLARE @err_msg3 VARCHAR(MAX) = formatmessage(51136, @delete_invalid_user);
		THROW 51136, @err_msg3, 1;
	END TRY
	BEGIN CATCH
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_study_areas]';
		THROW 51136, @err_msg3, 1;
	END CATCH;		
	
	DELETE FROM [rif40].[t_rif40_study_areas]
	WHERE EXISTS (
		SELECT 1
		FROM deleted b
		WHERE b.study_id=[rif40].[t_rif40_study_areas].study_id
		AND b.area_id=[rif40].[t_rif40_study_areas].area_id);
END;

END;
GO

--
-- Testing stop
--
/*
ROLLBACK;
 */
COMMIT TRANSACTION;
GO

--
--  Eof 