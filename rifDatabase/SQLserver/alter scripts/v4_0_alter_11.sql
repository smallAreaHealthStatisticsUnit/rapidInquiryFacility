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
 3. Exposure value support;
 4. Add intersection counting and exposure value support to extracts;
 5. View rif40_exposure_values;
 
 */

:r ..\sahsuland_dev\rif40\table_triggers\rif40_tables_trigger.sql

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_study_areas'
                  AND column_name  = 'intersect_count') BEGIN
	ALTER TABLE rif40.t_rif40_study_areas ADD intersect_count INTEGER NULL;
END
ELSE  BEGIN
	ALTER TABLE rif40.t_rif40_study_areas ALTER COLUMN intersect_count INTEGER NULL;
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
	ALTER TABLE rif40.t_rif40_study_areas ADD distance_from_nearest_source NUMERIC NULL;
END
ELSE  BEGIN
	ALTER TABLE rif40.t_rif40_study_areas ALTER COLUMN distance_from_nearest_source NUMERIC NULL;
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
	ALTER TABLE rif40.t_rif40_study_areas ADD nearest_rifshapepolyid NVARCHAR(MAX) NULL;
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
-- 3. Exposure value support
--
IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_study_areas'
                  AND column_name  = 'exposure_value') BEGIN
	ALTER TABLE rif40.t_rif40_study_areas ADD exposure_value NUMERIC NULL;
END;
GO	

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'exposure_value' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Exposure value (when bands selected by exposure values)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_study_areas',
		@level2type = N'Column', @level2name = 'exposure_value';
GO

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_studies'
                  AND column_name  = 'risk_analysis_exposure_field') BEGIN
	ALTER TABLE rif40.t_rif40_studies ADD risk_analysis_exposure_field VARCHAR(30) NULL;
END;
GO	

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'risk_analysis_exposure_field' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Risk analysis exposure field (when bands selected by exposure values)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_studies',
		@level2type = N'Column', @level2name = 'risk_analysis_exposure_field';
GO

--
-- Rebuild view: rif40_study_areas
--
ALTER VIEW [rif40].[rif40_study_areas] AS 
SELECT c.username,
    c.study_id,
    c.area_id,
    c.band_id,
	c.intersect_count,
	c.distance_from_nearest_source,
	c.nearest_rifshapepolyid,
	c.exposure_value
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
DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'exposure_value' AND [object_id] = OBJECT_ID(@tableName)))
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Exposure value (when bands selected by exposure values)', 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_areas', @level2type=N'COLUMN',@level2name=N'exposure_value'
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
				nearest_rifshapepolyid,
				exposure_value)
	SELECT
				isnull(username,SUSER_SNAME()),
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq')),
				area_id /* no default value */,
				band_id /* no default value */,
				intersect_count /* no default value */,
				distance_from_nearest_source /* no default value */,
				nearest_rifshapepolyid /* no default value */,
				exposure_value /* no default value */
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
				nearest_rifshapepolyid,
				exposure_value)
	SELECT
				username,
				study_id,
				area_id,
				band_id,
				intersect_count /* no default value */,
				distance_from_nearest_source /* no default value */,
				nearest_rifshapepolyid /* no default value */,
				exposure_value /* no default value */
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
-- Rebuild View: rif40_studies
--
ALTER VIEW [rif40].[rif40_studies] AS 
 SELECT c.username,
    c.study_id,
    c.extract_table,
    c.study_name,
    c.summary,
    c.description,
    c.other_notes,
    c.study_date,
    c.geography,
    c.study_type,
    c.study_state,
    c.comparison_geolevel_name,
    c.denom_tab,
    c.direct_stand_tab,
    i.year_start,
    i.year_stop,
    i.max_age_group,
    i.min_age_group,
    c.study_geolevel_name,
    c.map_table,
    c.suppression_value,
    c.extract_permitted,
    c.transfer_permitted,
    c.authorised_by,
    c.authorised_on,
    c.authorised_notes,
    c.audsid,
	0 AS partition_parallelisation,  --does this apply to SQL Server?
    l.covariate_table,
    c.project,
    pj.description AS project_description,
	c.stats_method,
	c.select_state,
	c.print_state,
	c.export_date,
	c.risk_analysis_exposure_field
   FROM [rif40].[t_rif40_studies] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
     LEFT JOIN ( SELECT i2.study_id,
            max(i2.year_stop) AS year_stop,
            min(i2.year_start) AS year_start,
            max(i2.max_age_group) AS max_age_group,
            min(i2.min_age_group) AS min_age_group
           FROM [rif40].[t_rif40_investigations] i2
          GROUP BY i2.study_id) i ON c.study_id = i.study_id
     LEFT JOIN [rif40].[rif40_geographies] g ON c.geography = g.geography
     LEFT JOIN [rif40].[t_rif40_geolevels] l ON c.geography = l.geography AND c.study_geolevel_name = l.geolevel_name
     LEFT JOIN [rif40].[t_rif40_projects] pj ON pj.project = c.project
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'select_state' AND [object_id] = OBJECT_ID(@tableName)))
BEGIN
	EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF Study selection state: what the user selected (see: rifs-dsub-selectstate.js):

{
	studyType: "risk_analysis_study",
	studySelection: {			
		studySelectAt: undefined,
		studySelectedAreas: [],
		riskAnalysisType: 12, 
		riskAnalysisDescription: "Risk Analysis (point sources, many areas, one to six bands)",
		studyShapes: [],
		comparisonShapes: [],
		comparisonSelectAt: undefined,
		comparisonSelectedAreas: [],
		fileList: [],
		bandAttr: []
	},
	showHideCentroids: false,
	showHideSelectionShapes: true
};
					
//
// Risk analysis study types (as per rif40_studies.stype_type): 
//
// 11 - Risk Analysis (many areas, one band), 
// 12 - Risk Analysis (point sources, many areas, one to six bands) [DEFAULT], 
// 13 - Risk Analysis (exposure covariates), 
// 14 - Risk Analysis (coverage shapefile), 
// 15 - Risk Analysis (exposure shapefile)

{
	studyType: "disease_mapping_study",
	studySelection: {			
		studySelectAt: undefined,
		studySelectedAreas: [],
		studyShapes: [],
		comparisonSelectAt: undefined,
		comparisonSelectedAreas: [],
		fileList: [],
		bandAttr: []
	},
	showHideCentroids: false,
	showHideSelectionShapes: true
};' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'select_state'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF Study print state: what the user selected (see: rifs-util-printstate.js)' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'print_state'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF Study export date' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'export_date'

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Risk analysis exposure field (when bands selected by exposure values)' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'risk_analysis_exposure_field'

END;
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON rif40.rif40_studies TO rif_user;
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON rif40.rif40_studies TO rif_manager;
GO

------------------------------
-- create trigger code 
------------------------------
ALTER trigger [rif40].[tr_rif40_studies]
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
		select SUSER_SNAME() AS username
		from inserted
		where NOT (username = SUSER_SNAME() OR username is null)
		OR NOT ([rif40].[rif40_has_role](SUSER_SNAME(),'rif_user') = 1
		AND [rif40].[rif40_has_role](SUSER_SNAME(),'rif_manager') = 1)
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
				stats_method,
				select_state,
				print_state,
				export_date,
				risk_analysis_exposure_field)
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
				isnull(stats_method, 'NONE'),
				select_state,
				print_state,
				export_date,
				risk_analysis_exposure_field
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
           print_state=inserted.print_state,
           export_date=inserted.export_date,
		   risk_analysis_exposure_field=inserted.risk_analysis_exposure_field
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
-- 4. Add intersection counting and exposure value support to extracts
--
:r ..\sahsuland_dev\rif40\functions\rif40_create_extract.sql
:r ..\sahsuland_dev\rif40\functions\rif40_create_insert_statement.sql
:r ..\sahsuland_dev\rif40\functions\rif40_insert_extract.sql

--
-- 5. View rif40_exposure_values
--
IF EXISTS (SELECT column_name
			 FROM information_schema.columns
			WHERE table_schema = 'rif40'
			  AND table_name   = 'rif40_exposure_values') BEGIN
	DROP VIEW rif40.rif40_exposure_values;
END;
GO

CREATE VIEW rif40.rif40_exposure_values AS
 SELECT username,
    study_id,
	band_id,
    COUNT(area_id) AS total_areas,
	COUNT(DISTINCT(nearest_rifshapepolyid)) AS total_rifshapepolyid,
	MIN(intersect_count) AS min_intersect_count,
	MAX(intersect_count) AS max_intersect_count,
	MIN(distance_from_nearest_source) AS min_distance_from_nearest_source,
	MAX(distance_from_nearest_source) AS max_distance_from_nearest_source,
	MIN(exposure_value) AS min_exposure_value,
	MAX(exposure_value) AS max_exposure_value
   FROM rif40.rif40_study_areas
  WHERE exposure_value IS NOT NULL
  GROUP BY username, study_id, band_id;
GO
GRANT SELECT ON rif40.rif40_exposure_values TO rif_user, rif_manager;
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name]     = N'MS_Description'
		   AND [minor_id] = 0)
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Minimum/maximum exposure values by band for risk analysis study areas. Study type: 13 (exposure covariates)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name = 'rif40_exposure_values';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'username' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Username', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'username';
GO	

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'study_id' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'study_id';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'band_id' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'A band allocated to the area', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'band_id';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'total_areas' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Total area id', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'total_areas';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'total_rifshapepolyid' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Total rifshapepolyid (shape reference)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'total_rifshapepolyid';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'min_distance_from_nearest_source' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Minimum distance from nearest source (Km)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'min_distance_from_nearest_source';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'max_distance_from_nearest_source' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Maximum distance from nearest source (Km)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'max_distance_from_nearest_source';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'min_intersect_count' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Minimum number of intersects with shapes', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'min_intersect_count';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'max_intersect_count' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Maximum number of intersects with shapes', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'max_intersect_count';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'min_exposure_value' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Minimum exposure value', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'min_exposure_value';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_exposure_values';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS 
		                      WHERE [name] = 'max_exposure_value' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Maximum exposure value', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_exposure_values',
		@level2type = N'Column', @level2name = 'max_exposure_value';
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
