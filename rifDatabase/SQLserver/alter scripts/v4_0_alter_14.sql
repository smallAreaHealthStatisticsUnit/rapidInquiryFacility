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
-- Rapid Inquiry Facility (RIF) - RIF alter script 14 - Individual site and pooled analysis (1 or more groups of sites) Enhancements
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
-- Usage: sqlcmd -U rif40 -d <database name> -b -m-1 -e -r1 -i v4_0_alter_14.sql -v pwd="%cd%"
-- Connect flags if required: -P <password> -S<myServerinstanceName>
--
-- The middleware must be down for this to run
--
-- MAKE SURE YOU ADD TO run_alter_scripts.bat for the installer
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

PRINT 'Running SAHSULAND schema alter script #14 - Individual site and pooled analysis (1 or more groups of sites) Enhancements.';
GO

/*
 * Alter 14: Individual site and pooled analysis (1 or more groups of sites) Enhancements

 1. Add the following columns to rif40_studies/t_rif40_studies:
    * stratification_field: VARCHAR(30)
	* stratify_to:          VARCHAR(300)
	* stratification_list:  VARCHAR(300)
 2. Add the following column to rif40_study_areas/t_rif40_study_areas
    * stratification:       VARCHAR(30)
 */

--
-- t_rif40_studies
--
IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_studies'
                  AND column_name  = 'stratification_field') BEGIN
	ALTER TABLE rif40.t_rif40_studies ADD stratification_field VARCHAR(30) NULL;
END;
GO	

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_studies'
                  AND column_name  = 'stratify_to') BEGIN
	ALTER TABLE rif40.t_rif40_studies ADD stratify_to VARCHAR(300) NULL;
END;
GO	

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_studies'
                  AND column_name  = 'stratification_list') BEGIN
	ALTER TABLE rif40.t_rif40_studies ADD stratification_list VARCHAR(300) NULL;
END;
GO	

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratification_field' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Risk analysis exposure field (when bands selected by exposure values)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_studies',
		@level2type = N'Column', @level2name = 'stratification_field';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratify_to' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Stratification to perform; risk analysis only.
The stratificationType is one of:
* "NONE": No stratification;
* "GEOLEVEL": Stratification by geolevel, usually region. Geolevels 2 and 3 are available for choice;
* "MULTIPOLYGON": Stratification by a single multi polygon site;
* "SHAPEFILE_FIELD": Stratification by a field in the shape file.

E.g.

 {
	"name": "SAHSU_GRD_LEVEL2",
	"stratificationType": "GEOLEVEL",
	"description": "Stratification by geolevel(1): SAHSU_GRD_LEVEL2"
}', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_studies',
		@level2type = N'Column', @level2name = 'stratify_to';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratification_list' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'List of stratifications available. E.g.
	[{
		"name": "NONE",
		"stratificationType": "NONE",
		"description": "No stratification"
	}, {
		"name": "MULTIPOLYGON",
		"stratificationType": "MULTIPOLYGON",
		"description": "Stratification by multiple polygons"
	}, {
		"name": "SITE_TYPE",
		"stratificationType": "SHAPEFILE_FIELD",
		"description": "Stratification by shapefile field: Site type"
	}, {
		"name": "SAHSU_GRD_LEVEL2",
		"stratificationType": "GEOLEVEL",
		"description": "Stratification by geolevel(1): SAHSU_GRD_LEVEL2"
	}, {
		"name": "SAHSU_GRD_LEVEL3",
		"stratificationType": "GEOLEVEL",
		"description": "Stratification by geolevel(2): SAHSU_GRD_LEVEL3"
	}
]', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_studies',
		@level2type = N'Column', @level2name = 'stratification_list';
GO

--
-- t_rif40_study_areas
--
IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_study_areas'
                  AND column_name  = 'stratification') BEGIN
	ALTER TABLE rif40.t_rif40_study_areas ADD stratification VARCHAR(30) NULL;
END;
GO		

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratification' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Stratification value. Multi Site Risk Stratification: NONE, <rifshapepolyid: poygon identifier for band 1> or <geolevel code: usually a regional code> or <field from shapefile DBF>; risk analysis only', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_study_areas',
		@level2type = N'Column', @level2name = 'stratification';
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
	c.exposure_value,
	c.stratification
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
DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.rif40_study_areas';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratification' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Stratification value. Multi Site Risk Stratification: NONE, <rifshapepolyid: poygon identifier for band 1> or <geolevel code: usually a regional code> or <field from shapefile DBF>; risk analysis only', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'VIEW', @level1name  = 'rif40_study_areas',
		@level2type = N'Column', @level2name = 'stratification';
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
				exposure_value,
				stratification)
	SELECT
				isnull(username,SUSER_SNAME()),
				isnull(study_id,[rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq')),
				area_id /* no default value */,
				band_id /* no default value */,
				intersect_count /* no default value */,
				distance_from_nearest_source /* no default value */,
				nearest_rifshapepolyid /* no default value */,
				exposure_value /* no default value */,
				stratification /* no default value */
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
				exposure_value,
				stratification)
	SELECT
				username,
				study_id,
				area_id,
				band_id,
				intersect_count /* no default value */,
				distance_from_nearest_source /* no default value */,
				nearest_rifshapepolyid /* no default value */,
				exposure_value /* no default value */,
				stratification /* no default value */
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
	c.risk_analysis_exposure_field,
	c.stratification_field,
	c.stratify_to,
	c.stratification_list
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
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratification_field' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Risk analysis exposure field (when bands selected by exposure values)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_studies',
		@level2type = N'Column', @level2name = 'stratification_field';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratify_to' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Stratification to perform; risk analysis only.
The stratificationType is one of:
* "NONE": No stratification;
* "GEOLEVEL": Stratification by geolevel, usually region. Geolevels 2 and 3 are available for choice;
* "MULTIPOLYGON": Stratification by a single multi polygon site;
* "SHAPEFILE_FIELD": Stratification by a field in the shape file.

E.g.

 {
	"name": "SAHSU_GRD_LEVEL2",
	"stratificationType": "GEOLEVEL",
	"description": "Stratification by geolevel(1): SAHSU_GRD_LEVEL2"
}', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_studies',
		@level2type = N'Column', @level2name = 'stratify_to';
GO

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'stratification_list' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'List of stratifications available. E.g.
	[{
		"name": "NONE",
		"stratificationType": "NONE",
		"description": "No stratification"
	}, {
		"name": "MULTIPOLYGON",
		"stratificationType": "MULTIPOLYGON",
		"description": "Stratification by multiple polygons"
	}, {
		"name": "SITE_TYPE",
		"stratificationType": "SHAPEFILE_FIELD",
		"description": "Stratification by shapefile field: Site type"
	}, {
		"name": "SAHSU_GRD_LEVEL2",
		"stratificationType": "GEOLEVEL",
		"description": "Stratification by geolevel(1): SAHSU_GRD_LEVEL2"
	}, {
		"name": "SAHSU_GRD_LEVEL3",
		"stratificationType": "GEOLEVEL",
		"description": "Stratification by geolevel(2): SAHSU_GRD_LEVEL3"
	}
]', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'View', @level1name  = 'rif40_studies',
		@level2type = N'Column', @level2name = 'stratification_list';
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
				risk_analysis_exposure_field,
				stratification_field,
				stratify_to,
				stratification_list)
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
				risk_analysis_exposure_field,
				stratification_field,
				stratify_to,
				stratification_list
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
		   risk_analysis_exposure_field=inserted.risk_analysis_exposure_field,
		   stratification_field=inserted.stratification_field,
		   stratify_to=inserted.stratify_to,
		   stratification_list=inserted.stratification_list
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
-- Add stratification to extract
--
:r ..\sahsuland_dev\rif40\functions\rif40_create_extract.sql
:r ..\sahsuland_dev\rif40\functions\rif40_create_insert_statement.sql
:r ..\sahsuland_dev\rif40\functions\rif40_insert_extract.sql
:r ..\sahsuland_dev\rif40\functions\rif40_execute_insert_statement.sql
:r ..\sahsuland_dev\rif40\functions\rif40_compute_results.sql

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
