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
-- Rapid Inquiry Facility (RIF) - RIF alter script 10 - Risk Analysis Enhancements
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
-- Usage: sqlcmd -U rif40 -d <database name> -b -m-1 -e -r1 -i v4_0_alter_10.sql -v pwd="%cd%"
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

PRINT 'Running SAHSULAND schema alter script #10 Risk Analysis Enhancements.';
GO

/*

* Alter 10: Risk Analysis Enhancements

 1. Save/restore user selection methods to/from database (rif40_studies.select_state);	
 2. Save user print selection to/from database (rif40_studies.print_state), export_date;	
 3. The column predefined_group_name in the table t_rif40_inv_conditions is defined as varchar(5) in Postgres. It should be varchar(30);
    [Issue 21](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/21)
 4. Add table t_rif40_homogeneity, view rif40_homogeneity:
  
  | Column name      | Column description                                                                  |
  |------------------|-------------------------------------------------------------------------------------| 
  | study_id[PK][FK] | rif40_studies.study_id                                                              | 
  | inv_id[PK][FK]   | rif40_investigations.inv_id                                                         | 
  | adjusted[PK]     | 0 or 1 indicating adjusted/unadjusted results                                       | 
  | genders[PK]      | 1, 2 or 3, indicating Males, Females or Both                                        | 
  | homogeneity_dof  | the number of degrees of freedom                                                    | 
  | homogeneity_chi2 | the chi2-value for the homogeneity test                                             | 
  | homogeneity_p    | the p-value for the homogeneity test                                                | 
  | linearity_chi2   | the chi2-value for the linearity test                                               | 
  | linearity_p      | the p-value for the linearity test                                                  | 
  | explt5           | the number of bands in the study which have an expected number of cases less than 1 | 
  
 5. Add unique keys to description files on rif tables/projects/health themes to protect against the middleware using them as a key;
 6. Add default background layer support for geography (so sahsuland has no background);
 
 */

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_studies'
                  AND column_name  = 'select_state') BEGIN
	ALTER TABLE rif40.t_rif40_studies ADD select_state NVARCHAR(MAX) NULL;
END;
GO		

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'select_state' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'RIF Study selection state: what the user selected (see: rifs-dsub-selectstate.js):

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
};', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_studies',
		@level2type = N'Column', @level2name = 'select_state';
GO

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_studies'
                  AND column_name  = 'print_state') BEGIN
	ALTER TABLE rif40.t_rif40_studies ADD print_state NVARCHAR(MAX) NULL;
END;
GO		

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'print_state' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'RIF Study print state: what the user selected (see: rifs-util-printstate.js)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_studies',
		@level2type = N'Column', @level2name = 'print_state';
GO

IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_studies'
                  AND column_name  = 'export_date') BEGIN
	ALTER TABLE rif40.t_rif40_studies ADD export_date NVARCHAR(MAX) NULL;
END;
GO	

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_studies';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'export_date' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'RIF Study export date', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_studies',
		@level2type = N'Column', @level2name = 'export_date';
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
	c.export_date
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
				export_date)
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
				export_date
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
           export_date=inserted.export_date
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
-- 3. The column predefined_group_name in the table t_rif40_inv_conditions is defined as varchar(5) in Postgres. It should be varchar(30);
--    [Issue 21](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/21)
--		 
ALTER TABLE rif40.t_rif40_inv_conditions ALTER COLUMN predefined_group_name VARCHAR(30);
GO

--
-- 4. Add table t_rif40_homogeneity, view rif40_homogeneity:
--  
--  | Column name      | Column description                                                                  |
--  |------------------|-------------------------------------------------------------------------------------| 
--  | study_id[PK][FK] | rif40_studies.study_id                                                              | 
--  | inv_id[PK][FK]   | rif40_investigations.inv_id                                                         | 
--  | adjusted[PK]     | 0 or 1 indicating adjusted/unadjusted results                                       | 
--  | genders[PK]      | 1, 2 or 3, indicating Males, Females or Both                                        | 
--  | homogeneity_dof  | the number of degrees of freedom                                                    | 
--  | homogeneity_chi2 | the chi2-value for the homogeneity test                                             | 
--  | homogeneity_p    | the p-value for the homogeneity test                                                | 
--  | linearity_chi2   | the chi2-value for the linearity test                                               | 
--  | linearity_p      | the p-value for the linearity test                                                  | 
--  | explt5           | the number of bands in the study which have an expected number of cases less than 1 | 
--

IF OBJECT_ID('rif40.t_rif40_homogeneity', 'U') IS NULL BEGIN
		CREATE TABLE rif40.t_rif40_homogeneity
		(
		  inv_id integer NOT NULL, 
		  study_id integer NOT NULL, 
		  username character varying(90) DEFAULT (suser_sname()) NOT NULL, 	-- Username
		  adjusted smallint NOT NULL,								-- 0 or 1 indicating adjusted/unadjusted results                                    
		  genders smallint NOT NULL,								-- 1, 2 or 3, indicating Males, Females or Both                                       
		  homogeneity_dof  double precision, 						-- the number of degrees of freedom                                                    
		  homogeneity_chi2 double precision,						-- the chi2-value for the homogeneity test                                            
		  homogeneity_p double precision,   						-- the p-value for the homogeneity test                                                
		  linearity_chi2 double precision,  						-- the chi2-value for the linearity test                                               
		  linearity_p double precision,     						-- the p-value for the linearity test                                                  
		  explt5  double precision,      			    			-- the number of bands in the study which have an expected number of cases less than 1 
		  CONSTRAINT t_rif40_homogeneity_pk PRIMARY KEY (study_id, inv_id, adjusted, genders),
		  CONSTRAINT t_rif40_homogeneity_si_fk FOREIGN KEY (study_id, inv_id)
			  REFERENCES rif40.t_rif40_investigations (study_id, inv_id),
		  CONSTRAINT adjusted_ck CHECK (adjusted BETWEEN 0 AND 1),
		  CONSTRAINT genders_ck CHECK (genders BETWEEN 1 AND 3)
		);
--		  
		GRANT SELECT, UPDATE, INSERT, DELETE ON rif40.t_rif40_homogeneity TO rif_user;
		GRANT SELECT, UPDATE, INSERT, DELETE ON rif40.t_rif40_homogeneity TO rif_manager;
		
		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'Riak analysis homogeneity tests.' , 
			@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'Unique investigation index: inv_id', 
			@level2type=N'COLUMN',@level2name=N'inv_id', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'Unique study index: study_id', 
			@level2type=N'COLUMN',@level2name=N'study_id', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'Username', 
			@level2type=N'COLUMN',@level2name=N'username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'0 or 1 indicating adjusted/unadjusted results', 
			@level2type=N'COLUMN',@level2name=N'adjusted', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'1, 2 or 3, indicating Males, Females or Both', 
			@level2type=N'COLUMN',@level2name=N'genders', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'the number of degrees of freedom', 
			@level2type=N'COLUMN',@level2name=N'homogeneity_dof', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'the chi2-value for the homogeneity test', 
			@level2type=N'COLUMN',@level2name=N'homogeneity_chi2', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'the chi2-value for the homogeneity test', 
			@level2type=N'COLUMN',@level2name=N'homogeneity_p', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			
		
		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'the number of bands in the study which have an expected number of cases less than 1', 
			@level2type=N'COLUMN',@level2name=N'linearity_chi2', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'the chi2-value for the linearity test', 
			@level2type=N'COLUMN',@level2name=N'linearity_p', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			

		EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
			@value=N'the chi2-value for the linearity test', 
			@level2type=N'COLUMN',@level2name=N'explt5', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_homogeneity';			
 
 
END;
GO

IF OBJECT_ID('rif40.rif40_homogeneity', 'V') IS NOT NULL DROP VIEW rif40.rif40_homogeneity;
GO

-- 'CREATE VIEW' must be the first statement in a query batch!!!
CREATE VIEW rif40.rif40_homogeneity
AS 
SELECT c.username,
	c.study_id,
	c.inv_id, 
	c.adjusted,								-- 0 or 1 indicating adjusted/unadjusted results                                    
	c.genders,								-- 1, 2 or 3, indicating Males, Females or Both                                       
	c.homogeneity_dof, 						-- the number of degrees of freedom                                                    
	c.homogeneity_chi2,						-- the chi2-value for the homogeneity test                                            
	c.homogeneity_p,   						-- the p-value for the homogeneity test                                                
	c.linearity_chi2,  						-- the chi2-value for the linearity test                                               
	c.linearity_p,     						-- the p-value for the linearity test                                                  
	c.explt5 
   FROM rif40.t_rif40_homogeneity c
	 LEFT OUTER JOIN rif40.rif40_study_shares AS s ON c.study_id = s.study_id AND s.grantee_username = SUSER_SNAME() 
  WHERE c.username=SUSER_SNAME() OR 
		IS_MEMBER(N'[rif_manager]') = 1 OR 
		(s.grantee_username IS NOT NULL AND s.grantee_username <> '');  	
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON rif40.rif40_homogeneity TO rif_user;
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON rif40.rif40_homogeneity TO rif_manager;
GO
		
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Riak analysis homogeneity tests.' , 
	@level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Unique investigation index: inv_id', 
	@level2type=N'COLUMN',@level2name=N'inv_id', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Unique study index: study_id', 
	@level2type=N'COLUMN',@level2name=N'study_id', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Username', 
	@level2type=N'COLUMN',@level2name=N'username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'0 or 1 indicating adjusted/unadjusted results', 
	@level2type=N'COLUMN',@level2name=N'adjusted', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'1, 2 or 3, indicating Males, Females or Both', 
	@level2type=N'COLUMN',@level2name=N'genders', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'the number of degrees of freedom', 
	@level2type=N'COLUMN',@level2name=N'homogeneity_dof', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'the chi2-value for the homogeneity test', 
	@level2type=N'COLUMN',@level2name=N'homogeneity_chi2', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'the chi2-value for the homogeneity test', 
	@level2type=N'COLUMN',@level2name=N'homogeneity_p', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'the number of bands in the study which have an expected number of cases less than 1', 
	@level2type=N'COLUMN',@level2name=N'linearity_chi2', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'the chi2-value for the linearity test', 
	@level2type=N'COLUMN',@level2name=N'linearity_p', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'the chi2-value for the linearity test', 
	@level2type=N'COLUMN',@level2name=N'explt5', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_homogeneity';			
GO 

--
-- 5. Add unique keys to description files on rif tables/projects/health themes to protect against the middleware using them as a key;
--
IF IndexProperty(Object_Id('rif40.rif40_geographies'), 'rif40_geographies_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX rif40_geographies_desc ON rif40.rif40_geographies(description);
GO
 
IF IndexProperty(Object_Id('rif40.rif40_health_study_themes'), 'rif40_health_study_themes_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX rif40_health_study_themes_desc ON rif40.rif40_health_study_themes(description);
GO

IF IndexProperty(Object_Id('rif40.rif40_outcome_groups'), 'rif40_outcome_groups_desc', 'IndexId') IS NULL BEGIN
	UPDATE rif40.rif40_outcome_groups
	   SET outcome_group_description = 'SAHSULAND Single ICD'
	 WHERE outcome_group_name = 'SAHSULAND_ICD' AND outcome_group_description = 'Single ICD';
	CREATE UNIQUE INDEX rif40_outcome_groups_desc ON rif40.rif40_outcome_groups(outcome_group_description);
END;
GO

IF IndexProperty(Object_Id('rif40.rif40_outcomes'), 'rif40_outcomes_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX rif40_outcomes_desc ON rif40.rif40_outcomes(outcome_description);
GO
IF IndexProperty(Object_Id('rif40.rif40_predefined_groups'), 'rif40_predefined_groups_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX rif40_predefined_groups_desc ON rif40.rif40_predefined_groups(predefined_group_description);
GO
IF IndexProperty(Object_Id('rif40.rif40_tables'), 'rif40_tables_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX rif40_tables_desc ON rif40.rif40_tables(description);
GO
IF IndexProperty(Object_Id('rif40.t_rif40_geolevels'), 't_rif40_geolevels_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX t_rif40_geolevels_desc ON rif40.t_rif40_geolevels(description);
GO
IF IndexProperty(Object_Id('rif40.t_rif40_parameters'), 't_rif40_parameters_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX t_rif40_parameters_desc ON rif40.t_rif40_parameters(param_description);
GO
IF IndexProperty(Object_Id('rif40.t_rif40_projects'), 't_rif40_projects_desc', 'IndexId') IS NULL
	CREATE UNIQUE INDEX t_rif40_projects_desc ON rif40.t_rif40_projects(description);
GO

--
-- 6. Add default background layer support for geography (so sahsuland has no background);
--
IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 'rif40_geographies'
                  AND column_name  = 'map_background') BEGIN
	ALTER TABLE rif40.rif40_geographies ADD map_background VARCHAR(200) DEFAULT 'OpenStreetMap Mapnik' NULL;
	
	EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
		@value=N'RIF geography map background', 
		@level2type=N'COLUMN',@level2name=N'map_background', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies';	
END;
GO

IF NOT EXISTS (SELECT constraint_name
                 FROM information_schema.table_constraints
                WHERE table_schema    = 'rif40'
                  AND table_name      = 'rif40_geographies'
                  AND constraint_name = 'map_background_ck')
ALTER TABLE rif40.rif40_geographies ADD CONSTRAINT map_background_ck CHECK (map_background IN (
		'OpenStreetMap Mapnik','OpenStreetMap BlackAndWhite','OpenTopoMap','Humanitarian OpenStreetMap','Thunderforest OpenCycleMap',
		'Thunderforest Transport','Thunderforest TransportDark','Thunderforest Landscape','Thunderforest SpinalMap','Thunderforest Outdoors',
		'Thunderforest Pioneer','Thunderforest Mobile Atlas','Thunderforest Neighbourhood','OpenMapSurfer Roads','OpenMapSurfer Grayscale',
		'Hydda Full','Hydda Base','Stamen Toner','Stamen TonerBackground','Stamen TonerLite','Stamen Watercolor','Esri WorldStreetMap',
		'Esri DeLorme','Esri WorldTopoMap','Esri WorldImagery','Esri WorldTerrain','Esri WorldShadedRelief','Esri WorldPhysical',
		'Esri OceanBasemap','Esri NatGeoWorldMap','Esri WorldGrayCanvas','CartoDB Positron','CartoDB PositronNoLabels',
		'CartoDB PositronOnlyLabels','CartoDB DarkMatter','CartoDB DarkMatterNoLabels','CartoDB DarkMatterOnlyLabels',
		'HikeBike HikeBike','HikeBike HillShading','NASAGIBS ViirsEarthAtNight2012','OSM UK Postcodes','Code-Point Open UK Postcodes'));
GO
		
UPDATE rif40.rif40_geographies SET map_background = 'OpenStreetMap Mapnik' WHERE geography != 'SAHSULAND' AND map_background IS NULL;
GO
UPDATE rif40.rif40_geographies SET map_background = NULL WHERE geography = 'SAHSULAND' AND map_background IS NOT NULL;
GO

:r ..\sahsuland_dev\rif40\table_triggers\t_rif40_studies_trigger.sql
:r ..\sahsuland_dev\rif40\functions\rif40_create_extract.sql
:r ..\sahsuland_dev\rif40\functions\rif40_compute_results.sql

--
-- Testing stop
--
/*
ROLLBACK;
 */
COMMIT TRANSACTION;
GO

SELECT geography, map_background
  FROM rif40_geographies
 ORDER BY 1;
GO

--
--  Eof 
