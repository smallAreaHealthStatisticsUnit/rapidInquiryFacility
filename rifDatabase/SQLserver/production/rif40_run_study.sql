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

SELECT * FROM rif40.rif40_projects;
GO
SELECT * FROM rif40.rif40_user_projects;
GO
SELECT * from rif40_num_denom;
GO
SELECT * FROM rif40.rif40_tables;
GO

SELECT TOP 5 * FROM rif_data.pop_sahsuland_pop;
GO

--
-- Save sequence in current valid sequences object for later use by
-- CURRVAL function: [rif40].[rif40_sequence_current_value]()
--
IF (OBJECT_ID('tempdb..##t_rif40_studies_seq') IS NOT NULL)
	DROP TABLE ##t_rif40_studies_seq;
GO
CREATE TABLE ##t_rif40_studies_seq (
	study_id INTEGER NOT NULL
);
GO
IF (OBJECT_ID('tempdb..##t_rif40_investigations_seq') IS NOT NULL)
	DROP TABLE ##t_rif40_investigations_seq;
GO
CREATE TABLE ##t_rif40_investigations_seq (
	inv_id INTEGER NOT NULL
);
GO

INSERT /* 1 */ INTO rif40.rif40_studies (
 		geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,
 		year_start, year_stop, max_age_group, min_age_group,
 		suppression_value, extract_permitted, transfer_permitted, stats_method)
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
		 1											/* transfer_permitted */,
		 'HET'										/* Stats method */);
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
DECLARE @study_id INT=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
DECLARE @inv_id INT=[rif40].[rif40_sequence_current_value] ('rif40.rif40_inv_id_seq');
--
-- Test sequence numbers
--
DECLARE @study_id2 INTEGER = (SELECT study_id AS t_rif40_studies_seq FROM ##t_rif40_studies_seq);
DECLARE @inv_id2 INTEGER = (SELECT inv_id AS t_rif40_investigations_seq FROM ##t_rif40_investigations_seq);
IF (@study_id = @study_id2) PRINT 'Study ID OK'
ELSE RAISERROR('Study ID mismatch %i !=%i', 16, 1, @study_id, @study_id2);
IF (@inv_id = @inv_id2) PRINT 'Study ID OK'
ELSE RAISERROR('Study ID mismatch %i !=%i', 16, 1, @inv_id, @inv_id2);

DECLARE @rif40_investigations VARCHAR(MAX) = (
		SELECT * /* 2 */
		  FROM rif40.rif40_investigations WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq')
		   FOR XML PATH('row'));
SET @rif40_investigations = 'Investigations for study: ' + CAST(@study_id AS VARCHAR) + CHAR(10) + 
	REPLACE(REPLACE(@rif40_investigations, '><', '>'+CHAR(10)+'  <'), '  </row>', '</row>');
DECLARE @rif40_studies VARCHAR(MAX) = (
		SELECT * /* 1 */
		  FROM rif40.rif40_studies WHERE study_id = [rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq')
		   FOR XML PATH('row'));
SET @rif40_studies = 'Study: ' + CAST(@study_id AS VARCHAR) + CHAR(10) + REPLACE(REPLACE(@rif40_studies, '><', '>'+CHAR(10)+'  <'), '  </row>', '</row>');
--
PRINT @rif40_investigations;
PRINT @rif40_studies;
GO

--
-- SAVE it
--
COMMIT TRANSACTION;
GO

--
-- Test rif40_GetAdjacencyMatrix()
--
-- @study_id needs to be restored via a variable to be safe
DECLARE @study_id INTEGER=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq')/* Get current sequence */;
SELECT TOP 10 SUBSTRING(areaid, 1, 20) AS areaid, num_adjacencies, SUBSTRING(adjacency_list, 1, 90) AS adjacency_list_truncated
  FROM [rif40].[sahsuland_GetAdjacencyMatrix](@study_id);
GO

DECLARE @study_id INTEGER=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq')/* Get current sequence */;
DECLARE @t TABLE(
	geolevel_id		INTEGER,
	areaid			VARCHAR(200),
	num_adjacencies INTEGER,
	adjacency_list	VARCHAR(8000))
INSERT @t EXECUTE [rif40].[rif40_GetAdjacencyMatrix] @study_id;
SELECT TOP 10 SUBSTRING(areaid, 1, 20) AS areaid, num_adjacencies, SUBSTRING(adjacency_list, 1, 90) AS adjacency_list_truncated
  FROM @t;
GO

--
-- Uncomment to enable execution plans
--
--SET SHOWPLAN_TEXT ON
--GO

--
-- Now run it
--

BEGIN
-- @study_id needs to be restored via a variable to be safe
	DECLARE @study_id INT=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');
	BEGIN TRANSACTION;
	DECLARE @rval INT=-5;
	DECLARE @rval2 INT=-6;
	DECLARE @msg VARCHAR(MAX);
--
	BEGIN TRY
		INSERT INTO rif40.rif40_study_status(study_id, study_state, ith_update, message) VALUES (@study_id, 'C', 0, 
			'Study has been created but it has not been verified.');	
		 EXECUTE @rval2=rif40.rif40_run_study
				@study_id 	/* Study_id */, 
				1 			/* Debug: 0/1 */,
				@rval		/* Result */;	
				
--
	END TRY
	BEGIN CATCH 
		SET @rval=0;
		SET @msg='Caught error in rif40.rif40_run_study(' + CAST(@study_id AS VARCHAR) + ')' + CHAR(10) + 
							'Error number: ' + NULLIF(CAST(ERROR_NUMBER() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Error severity: ' + NULLIF(CAST(ERROR_SEVERITY() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Error state: ' + NULLIF(CAST(ERROR_STATE() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Procedure with error: ' + NULLIF(ERROR_PROCEDURE() + CHAR(10), 'N/A') + 
							'Procedure line: ' + NULLIF(CAST(ERROR_LINE() AS VARCHAR), 'N/A') + CHAR(10) + 
							'Error message: ' + NULLIF(ERROR_MESSAGE(), 'N/A') + CHAR(10);
		PRINT @msg; 
		EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_run_study]';		
	END CATCH;
	SELECT * FROM rif40.rif40_study_status WHERE study_id = @study_id ORDER BY ith_update;
	SELECT statement_number, log_message, log_sqlcode, elapsed_time FROM rif40.rif40_study_sql_log 
	 WHERE study_id = @study_id AND log_sqlcode != 0 ORDER BY statement_number;
	SELECT COUNT(*) AS total FROM rif40.rif40_study_sql WHERE study_id = @study_id;
	SELECT study_id AS t_rif40_studies_seq FROM ##t_rif40_studies_seq;
	SELECT inv_id AS t_rif40_investigations_seq FROM ##t_rif40_investigations_seq;
--
	SET @rval=COALESCE(@rval2, @rval);
	SET @rval=COALESCE(@rval, -1);
	SET @msg = 'Study test run ' + CAST(@study_id AS VARCHAR) + ' OK';
	COMMIT TRANSACTION;	
	IF @rval = 1
		PRINT @msg;
	ELSE 
		RAISERROR('Study test run %i FAILED; rval=%i (see previous errors)', 16, 1, @study_id, @rval);

END;
GO

--
-- Eof (rif40_run_study.sql)