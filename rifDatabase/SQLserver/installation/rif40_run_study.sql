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

ROLLBACK TRANSACTION;
GO

--
-- Eof