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
-- Rapid Inquiry Facility (RIF) - RIF alter script 12 - rif_user role faults
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
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;

--
-- Set schema variable used by scripts etc to RIF_DATA
--
:SETVAR SchemaName "rif_data"
--

BEGIN TRANSACTION;
GO

PRINT 'Running SAHSULAND schema alter script #12 rif_user role faults.';
GO

/*

* Alter 12: rif_user role faults

 1. Fix for study creation and running with only the rif_user role:
    ```view name: [rif40].[rif40_studies], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: aina```:
 2. Fix for inserting invalid usernames into t_rif40_user_projects
 
 */

/*
 1. Fix for ```view name: [rif40].[rif40_studies], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: aina```:
    ```
	1> INSERT INTO rif40.rif40_studies(
	2>    geography,
	3>    project,
	4>    study_name,
	5>    study_type,
	6>    comparison_geolevel_name,
	7>    study_geolevel_name,
	8>    denom_tab,
	9>    year_start,
	10>    year_stop,
	11>    max_age_group,
	12>    min_age_group,
	13>    suppression_value,
	14>    extract_permitted,
	15>    transfer_permitted,
	16>    stats_method,
	17>    risk_analysis_exposure_field)
	18> VALUES ('SAHSULAND',
	19>  'TEST',
	20>  'TEST 1002',
	21>  1,
	22>  'SAHSU_GRD_LEVEL1',
	23>  'SAHSU_GRD_LEVEL4',
	24>  'POP_SAHSULAND_POP',
	25>  1995,
	26>  1996,
	27>  21,
	28>  0,
	29>  0,
	30>  0,
	31>  0,
	32>  'HET',
	33>  'null');
	34> go

	(0 rows affected)

	(1 rows affected)
	Msg 51131, Level 16, State 1, Server DESKTOP-4P2SA80, Procedure tr_rif40_studies, Line 47
	View name: [rif40].[rif40_studies], Cannot INSERT: User must have rif_user or rif_manager role, NEW.username must be USER or NULL: aina	
    ``` 
 */
:r ..\sahsuland_dev\rif40\view_triggers\rif40_studies_trigger.sql

/* Then we get:
 exception: The INSERT permission was denied on the object 'rif40_study_shares', database 'sahsuland', schema 'rif40'.
 */
GRANT INSERT ON rif40.rif40_study_shares TO rif_user;

/* Then a repeat of 1.
 */
:r ..\sahsuland_dev\rif40\view_triggers\rif40_results_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_contextual_stats_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_comparison_areas_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_inv_conditions_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_inv_covariates_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_investigations_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_study_areas_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_study_status_trigger.sql
:r ..\sahsuland_dev\rif40\view_triggers\rif40_user_projects_trigger.sql 
 
:r ..\sahsuland_dev\rif40\table_triggers\rif40_study_shares_trigger.sql
 
/* Then:
1> UPDATE rif40.rif40_studies
2> SET select_state=select_state
3> WHERE
4>    study_id=187;
5> go
LOG DEBUG1 [rif40].[t_rif40_studies]: @XTYPE=U; OBJECT_ID tempdb..##t_rif40_studies_seq=NULL
LOG DEBUG1 [rif40].[t_rif40_studies]: State change: <study_id>187</study_id><new_study_id>187</new_study_id><study_state>C</study_state><new_state>C</new_state>
LOG DEBUG1 [rif40].[t_rif40_studies]: Inserted: <row>
<study_id>187</study_id>
<username>aina</username>
<geography>SAHSULAND</geography>
<project>TEST</project>
<study_name>1006 LUNG CANCER RA</study_name>
<extract_table>S187_EXTRACT</extract_table>
<map_table>S187_MAP</map_table>
<study_date>2018-12-06T13:45:42</study_date>
<study_type>15</study_type>
<study_state>C</study_state>
<comparison_geolevel_name>SAHSU_GRD_LEVEL1</comparison_geolevel_name>
<study_geolevel_name>SAHSU_GRD_LEVEL4</study_geolevel_name>
<denom_tab>POP_SAHSULAND_POP</denom_tab>
<suppression_value>0</suppression_value>
<extract_permitted>1</extract_permitted>
<transfer_permitted>1</transfer_permitted>
<authorised_by>aina</authorised_by>
<authorised_on>2018-12-06T13:45:42</authorised_on>
<authorised_notes>Auto authorised; study geolevel is not restricted</authorised_notes>
<audsid>67</audsid>
<stats_method>NONE</stats_method>
</row>
LOG DEBUG1 [rif40].[t_rif40_studies]: Deleted: <row>
<study_id>187</study_id>
<username>aina</username>
<geography>SAHSULAND</geography>
<project>TEST</project>
<study_name>1006 LUNG CANCER RA</study_name>
<extract_table>S187_EXTRACT</extract_table>
<map_table>S187_MAP</map_table>
<study_date>2018-12-06T13:45:42</study_date>
<study_type>15</study_type>
<study_state>C</study_state>
<comparison_geolevel_name>SAHSU_GRD_LEVEL1</comparison_geolevel_name>
<study_geolevel_name>SAHSU_GRD_LEVEL4</study_geolevel_name>
<denom_tab>POP_SAHSULAND_POP</denom_tab>
<suppression_value>0</suppression_value>
<extract_permitted>1</extract_permitted>
<transfer_permitted>1</transfer_permitted>
<authorised_by>aina</authorised_by>
<authorised_on>2018-12-06T13:45:42</authorised_on>
<authorised_notes>Auto authorised; study geolevel is not restricted</authorised_notes>
<audsid>67</audsid>
<stats_method>NONE</stats_method>
</row>

(0 rows affected)

(1 rows affected)
Msg 51015, Level 16, State 1, Server DESKTOP-4P2SA80, Procedure tr_studies_checks, Line 220
Table name: [rif40].[t_rif40_studies] , UPDATE failed - non IG UPDATE not allowed on T_RIF40_STUDIES by user: (aina)
 */ 
:r ..\sahsuland_dev\rif40\table_triggers\t_rif40_studies_trigger.sql

/*
 2. Fix for inserting invalid usernames into t_rif40_user_projects
 */
:r ..\sahsuland_dev\rif40\table_triggers\t_rif40_user_projects_trigger.sql

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