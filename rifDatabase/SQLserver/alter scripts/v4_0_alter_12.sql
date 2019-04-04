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
-- Rapid Inquiry Facility (RIF) - RIF alter script 12 - More risk Analysis Enhancements, additional covariate support; 
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
-- Usage: sqlcmd -U rif40 -d <database name> -b -m-1 -e -r1 -i v4_0_alter_12.sql -v pwd="%cd%"
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

PRINT 'Running SAHSULAND schema alter script #12 More risk Analysis Enhancements, additional covariate support; ';
GO

/*

* Alter 12: More risk Analysis Enhancements, additional covariate support; 

 1. rif40_homogeneity view grants [NOT SQL Server]
 2. Additional covariate support
 
 */
IF NOT EXISTS (SELECT column_name
                 FROM information_schema.columns
                WHERE table_schema = 'rif40'
                  AND table_name   = 't_rif40_inv_covariates'
                  AND column_name  = 'covariate_type') BEGIN
	ALTER TABLE [rif40].[t_rif40_inv_covariates] ADD covariate_type VARCHAR(1) NULL;
    ALTER TABLE [rif40].[t_rif40_inv_covariates] ADD DEFAULT ('N') FOR [covariate_type]; 
END
ELSE  BEGIN
	ALTER TABLE [rif40].[t_rif40_inv_covariates] ALTER COLUMN covariate_type VARCHAR(1) NULL;   
END;
GO		

DECLARE @tableName   sysname 
SELECT @tableName  = 'rif40.t_rif40_inv_covariates';
IF NOT EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name] = N'MS_Description'
		   AND [minor_id] = (SELECT [column_id] FROM SYS.COLUMNS WHERE [name] = 'covariate_type' AND [object_id] = OBJECT_ID(@tableName)))
EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'Covariate type: N normal; A: additional (not used in the calculations)', 
		@level0type = N'Schema', @level0name = 'rif40',  
		@level1type = N'Table', @level1name  = 't_rif40_inv_covariates',
		@level2type = N'Column', @level2name = 'covariate_type';
GO
DISABLE TRIGGER [rif40].[tr_inv_covariate] ON [rif40].[t_rif40_inv_covariates];
GO

UPDATE rif40.t_rif40_inv_covariates
   SET covariate_type = 'N' 
 WHERE covariate_type IS NULL;
GO
ALTER TABLE [rif40].[t_rif40_inv_covariates] DROP CONSTRAINT IF EXISTS rif40_covariates_type_ck;
GO
ALTER TABLE [rif40].[t_rif40_inv_covariates] ADD CONSTRAINT rif40_covariates_type_ck CHECK (([covariate_type] = 'N' OR [covariate_type] = 'A'));
GO
ALTER TABLE [rif40].[t_rif40_inv_covariates] ALTER COLUMN [covariate_type] VARCHAR(1) NOT NULL;
GO

-- Trigger: t_rif40_inv_covariates_checks: NO CHANGES
:r ..\sahsuland_dev\rif40\table_triggers\t_rif40_inv_covariates_trigger.sql
    
--
-- Rebuild view: rif40_inv_covariates
--
--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_inv_covariates]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_inv_covariates]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_inv_covariates] AS 
SELECT c.username,
    c.study_id,
    c.inv_id,
    c.covariate_name,
    c.covariate_type,
    c.min,
    c.max,
    c.geography,
    c.study_geolevel_name
   FROM [rif40].[t_rif40_inv_covariates] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
 --permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_covariates] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_covariates] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Details of each covariate used by an investigation in a study', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Covariate name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'covariate_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Covariate type: N normal; A: additional (not used in the calculations)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'covariate_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Minimum value for a covariate', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'min'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maximum value for a covariate', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'max'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography (e.g EW2001). Cannot be changed by the user; present to allow a foreign key to be enforced.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS. Cannot be changed by the user; present to allow a foreign key to be enforced.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'study_geolevel_name'
GO

:r rif40_inv_covariates_trigger.sql

SELECT geography, covariate_name, covariate_type, COUNT(*) AS total
  FROM rif40.t_rif40_inv_covariates
 GROUP BY geography, covariate_name, covariate_type; 
GO
SELECT geography, covariate_name, covariate_type, COUNT(*) AS total
  FROM rif40.rif40_inv_covariates
 GROUP BY geography, covariate_name, covariate_type; 
GO

:r ..\sahsuland_dev\rif40\functions\rif40_create_insert_statement.sql
:r rif40_study_areas_trigger.sql
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
