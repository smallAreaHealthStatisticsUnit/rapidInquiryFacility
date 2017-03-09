-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study
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

--
-- Check setup data is present. Create TEST project if needed
--
BEGIN TRANSACTION;
GO

SELECT geography, description FROM rif40.rif40_geographies WHERE geography = 'SAHSULAND';
GO

SELECT geolevel_name, geolevel_id, description FROM rif40.rif40_geolevels WHERE geography = 'SAHSULAND';
GO

IF NOT EXISTS (SELECT project FROM rif40.t_rif40_projects WHERE project = 'TEST')
INSERT INTO rif40.t_rif40_projects (project, description) VALUES ('TEST', 'Test project');
GO
IF NOT EXISTS (SELECT project FROM rif40.t_rif40_user_projects WHERE project = 'TEST')
INSERT INTO rif40.t_rif40_user_projects (project, username) VALUES ('TEST', USER);
GO

SELECT * FROM rif40.rif40_projects;
GO
SELECT * FROM rif40.rif40_user_projects;
GO

SELECT * FROM rif40.rif40_tables;
GO

SELECT TOP 5 * FROM rif_data.pop_sahsuland_pop;
GO

INSERT /* 1 */ INTO rif40.rif40_studies (
 		geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,
 		year_start, year_stop, max_age_group, min_age_group,
 		suppression_value, extract_permitted, transfer_permitted)
	VALUES (
		 'SAHSULAND' 								/* geography */,
		 'TEST' 									/* project */,
		 'SAHSULAND test 4 study_id 1 example' 		/* study_name */,
		 1 											/* study_type [disease mapping] */,
		 'SAHSU_GRD_LEVEL2'							/* comparison_geolevel_name */,
		 'SAHSU_GRD_LEVEL4' 						/* study_geolevel_name */,
		 'POP_SAHSULAND_POP' 						/* denom_tab */,
		 1989										/* year_start */,
		 1996 										/* year_stop */,
		 21 										/* max_age_group */,
		 0 											/* min_age_group */,
		 5 											/* suppression_value */,
		 1 											/* extract_permitted */,
		 1											/* transfer_permitted */);
GO

SELECT [rif40].[rif40_sequence_current_value]('rif40.rif40_study_id_seq');
GO

INSERT /* 2 */ INTO rif40.rif40_investigations(
	inv_name,
	inv_description,
	genders,
	numer_tab,
	year_start,
	year_stop,
	max_age_group,
	min_age_group
)
VALUES (
	'T_INV_1' 				/* inv_name */,
	'Lung cancer'			/* inv_description */,
	3						/* genders [both] */,
	'NUM_SAHSULAND_CANCER'	/* numer_tab */,
	1989					/* year_start */,
	1996 					/* year_stop */,
	21 						/* max_age_group */,
	0 						/* min_age_group */);
GO	

SELECT [rif40].[rif40_sequence_current_value]('rif40.rif40_inv_id_seq');
GO

INSERT /* 3 */ INTO rif40.rif40_inv_conditions(
			outcome_group_name, min_condition, max_condition, predefined_group_name, line_number)
VALUES
('SAHSULAND_ICD','C34',NULL,NULL, 1),
('SAHSULAND_ICD','162','1629',NULL, 2);
GO

INSERT /* 4 */ INTO rif40.rif40_study_areas(area_id, band_id)
SELECT DISTINCT sahsu_grd_level4, ROW_NUMBER() OVER(ORDER BY sahsu_grd_level4) AS band_id
  FROM rif_data.hierarchy_sahsuland;
GO

INSERT /* 5 */ INTO rif40.rif40_comparison_areas(area_id)
SELECT DISTINCT sahsu_grd_level2
  FROM rif_data.hierarchy_sahsuland;
GO

WITH a AS (
	SELECT 'SES' AS covariate_name,
	       'SAHSU_GRD_LEVEL4' AS study_geolevel_name,
	       'SAHSULAND' AS geography
)
INSERT /* 6 */ INTO rif40.rif40_inv_covariates(geography, covariate_name, study_geolevel_name, min, max)
SELECT a.geography, a.covariate_name, a.study_geolevel_name, b.min, b.max
  FROM a
	LEFT OUTER JOIN rif40.rif40_covariates b ON
		(a.covariate_name = b.covariate_name AND a.study_geolevel_name = b.geolevel_name AND a.geography = b.geography);
GO

INSERT /* 7 */ INTO rif40.rif40_study_shares(grantee_username) VALUES (SUSER_SNAME());		
GO

SELECT [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq') AS current_study_id,
       [rif40].[rif40_sequence_current_value] ('rif40.rif40_inv_id_seq') AS current_inv_id;
GO

--
-- Create SAVEPOINT
--
SAVE TRANSACTION test_rif40_run_study_delete;
GO

--
-- Test delete
--
DELETE /* 7 */ FROM rif40.rif40_study_shares WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
DELETE /* 6 */ FROM rif40.rif40_inv_covariates WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
DELETE /* 5 */ FROM rif40.rif40_comparison_areas WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
DELETE /* 4 */ FROM rif40.rif40_study_areas WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
DELETE /* 3 */ FROM rif40.rif40_inv_conditions WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
DELETE /* 2 */ FROM rif40.rif40_investigations WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
DELETE /* 1 */ FROM rif40.rif40_studies WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO

--
-- Undo DELETE by rolling back to SAVEPOINT.
--
ROLLBACK TRANSACTION test_rif40_run_study_delete;
GO

--
-- OK so we have the study back
--
SELECT * /* 7 */ FROM rif40.rif40_study_shares WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
SELECT * /* 6 */ FROM rif40.rif40_inv_covariates WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
SELECT TOP 5 * /* 5 */ FROM rif40.rif40_comparison_areas WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
SELECT TOP 5 * /* 4 */ FROM rif40.rif40_study_areas WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
SELECT * /* 3 */ FROM rif40.rif40_inv_conditions WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
SELECT * /* 2 */ FROM rif40.rif40_investigations WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO
SELECT * /* 1 */ FROM rif40.rif40_studies WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
GO

--
-- SAVE it
--
COMMIT TRANSACTION;
GO

--
-- Now run it
--
BEGIN
	DECLARE @study_id INT=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
	BEGIN TRANSACTION;
	DECLARE @rval INT;
	DECLARE @msg VARCHAR(MAX);
--
	BEGIN TRY
		 EXECUTE rif40.rif40_run_study
				@rval		/* Result: 0/1 */, 
				@study_id 	/* Study_id */, 
				1 			/* Debug: 0/1 */, 
				default 	/* Recursion level: Use default */;
--
	END TRY
	BEGIN CATCH 
		SET @rval=0;
		SET @msg='Caught error in rif40.rif40_run_study(' + CAST(@study_id AS VARCHAR) + ')' + CHAR(10) + 
							'Error number: ' + NULLIF(CAST(ERROR_NUMBER() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Error severity: ' + NULLIF(CAST(ERROR_SEVERITY() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Error state: ' + NULLIF(CAST(ERROR_STATE() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Error procedure: ' + NULLIF(ERROR_PROCEDURE() + CHAR(10), 'N/A') + 
							'Error line: ' + NULLIF(CAST(ERROR_LINE() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Error message: ' + NULLIF(ERROR_MESSAGE(), 'N/A') + CHAR(10);
		PRINT @msg; 
	END CATCH;
--	
-- Always commit
--
	COMMIT TRANSACTION;
--
	SET @msg = 'Study ' + CAST(@study_id AS VARCHAR) + ' OK';
	IF @rval = 1 
		PRINT @msg;
	ELSE 
		RAISERROR('Study %i FAILED (see previous errors)', 16, 1, @study_id);
END;
GO

--
-- Eof