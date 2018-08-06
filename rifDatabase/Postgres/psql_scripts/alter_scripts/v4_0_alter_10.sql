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
-- Working directory: c:/Users/Peter/Documents/GitHub/rapidInquiryFacility/rifDatabase/Postgres/psql_scripts
-- psql -U rif40 -d sahsuland -w -e -P pager=off -f alter_scripts/v4_0_alter_10.sql
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #10 Risk Analysis Enhancements.

/*

* Alter 10: Risk Analysis Enhancements

 1. Save/restore user selection methods to/from database (rif40_studies.select_state);	
 2. Save user print selection to/from database (rif40_studies.print_state);	
 3. The column predefined_group_name in the table t_rif40_inv_conditions is defined as varchar(5) in Postgres. It should be varchar(30);
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
  
 */
BEGIN;
--
-- 1. Save/restore user selection methods to/from database (rif40_studies.select_state);	
--
ALTER TABLE t_rif40_studies ADD COLUMN IF NOT EXISTS select_state JSON NULL;
COMMENT ON COLUMN t_rif40_studies.select_state IS 'RIF Study selection state: what the user selected (see: rifs-dsub-selectstate.js):

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
};
';

--
-- 2. Save user print selection to/from database (rif40_studies.print_state);	
--
ALTER TABLE t_rif40_studies ADD COLUMN IF NOT EXISTS print_state JSON NULL;
COMMENT ON COLUMN t_rif40_studies.print_state IS 'RIF Study print state: what the user selected (see: rifs-util-printstate.js):'

--
-- 3. The column predefined_group_name in the table t_rif40_inv_conditions is defined as varchar(5) in Postgres. It should be varchar(30);
--
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

 
--
-- Testing stop
--
/*
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop processing';
END;
$$;
 */

END;
--
--  Eof 