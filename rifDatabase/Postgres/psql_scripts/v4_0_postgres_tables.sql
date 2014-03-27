-- ************************************************************************
-- *
-- * DO NOT EDIT THIS SCRIPT OR MODIFY THE RIF SCHEMA - USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Create Postgres tables
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
\set ECHO all
\set ON_ERROR_STOP ON
\timing

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

--
-- Check database is sahsuland_dev
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev', current_database();	
	END IF;
END;
$$;

\set ON_ERROR_STOP ON

CREATE TABLE "sahsuland_pop" (
	"year" smallint NOT NULL,
	"age_sex_group" smallint NOT NULL,
	"level1" varchar(20) NOT NULL,
	"level2" varchar(20) NOT NULL,
	"level3" varchar(20) NOT NULL,
	"level4" varchar(20) NOT NULL,
	"total" double precision NOT NULL
);
CREATE INDEX sahsuland_pop_level4 ON "sahsuland_pop" ("level4");
CREATE INDEX sahsuland_pop_age_sex_group ON "sahsuland_pop" ("age_sex_group");
CREATE INDEX sahsuland_pop_level1 ON "sahsuland_pop" ("level1");
CREATE INDEX sahsuland_pop_age_group ON "sahsuland_pop" (mod("age_sex_group",100));
CREATE UNIQUE INDEX sahsuland_pop_pk ON "sahsuland_pop" ("year","level4","age_sex_group");
CREATE INDEX sahsuland_pop_year ON "sahsuland_pop" ("year");
CREATE INDEX sahsuland_pop_level2 ON "sahsuland_pop" ("level2");
CREATE INDEX sahsuland_pop_sex ON "sahsuland_pop" (trunc("age_sex_group"/100));
CREATE INDEX sahsuland_pop_level3 ON "sahsuland_pop" ("level3");
CREATE TABLE "rif40_outcome_groups" (
	"outcome_type" varchar(20) NOT NULL,
	"outcome_group_name" varchar(30) NOT NULL,
	"outcome_group_description" varchar(250) NOT NULL,
	"field_name" varchar(30) NOT NULL,
	"multiple_field_count" smallint NOT NULL
);
ALTER TABLE "rif40_outcome_groups" ADD CONSTRAINT "rif40_outcome_groups_pk" PRIMARY KEY ("outcome_group_name");
ALTER TABLE "rif40_outcome_groups" ADD CONSTRAINT "outcome_type_ck2" CHECK (outcome_type IN ('A&E', 'ICD', 'ICD-O', 'OPCS', 'BIRTHWEIGHT'));
CREATE TABLE "t_rif40_study_sql" (
	"username" varchar(90) DEFAULT USER,
	"study_id" integer NOT NULL,
	"statement_type" varchar(30) NOT NULL,
	"statement_number" integer NOT NULL,
	"sql_text" varchar(4000) NOT NULL,
	"line_number" integer NOT NULL,
	"status" varchar(1)
);
ALTER TABLE "t_rif40_study_sql" ADD CONSTRAINT "t_rif40_study_sql_pk" PRIMARY KEY ("study_id","statement_number","line_number");
ALTER TABLE "t_rif40_study_sql" ADD CONSTRAINT "statement_type_ck2" CHECK (statement_type IN ('CREATE', 'INSERT', 'POST_INSERT', 'NUMERATOR_CHECK', 'DENOMINATOR_CHECK'));
CREATE INDEX t_rif40_study_sql_uname_bm ON "t_rif40_study_sql" ("username");
CREATE INDEX t_rif40_study_sql_type_bm ON "t_rif40_study_sql" ("statement_type");
CREATE TABLE "rif40_opcs4" (
	"opcs4_1char" varchar(20),
	"opcs4_3char" varchar(3),
	"opcs4_4char" varchar(4),
	"text_1char" varchar(250),
	"text_3char" varchar(250),
	"text_4char" varchar(250)
);
CREATE TABLE "sahsuland_level2" (
	"level2" varchar(20) NOT NULL,
	"name" varchar(200) NOT NULL
);
CREATE UNIQUE INDEX sahsuland_level2_pk ON "sahsuland_level2" ("level2");
CREATE TABLE "rif40_covariates" (
	"geography" varchar(50) NOT NULL,
	"geolevel_name" varchar(30) NOT NULL,
	"covariate_name" varchar(30) NOT NULL,
	"min" double precision NOT NULL,
	"max" double precision NOT NULL,
	"type" double precision NOT NULL
);
ALTER TABLE "rif40_covariates" ADD CONSTRAINT "rif40_covariates_pk" PRIMARY KEY ("geography","geolevel_name","covariate_name");
ALTER TABLE "rif40_covariates" ADD CONSTRAINT "rif40_covariates_listing_ck" CHECK (type IN (1,2));
CREATE TABLE "rif40_a_and_e" (
	"a_and_e_3char" varchar(4) NOT NULL,
	"text_3char" varchar(200)
);
ALTER TABLE "rif40_a_and_e" ADD CONSTRAINT "rif40_a_and_e_pk" PRIMARY KEY ("a_and_e_3char");
CREATE TABLE "rif40_icd10" (
	"icd10_1char" varchar(20),
	"icd10_3char" varchar(3),
	"icd10_4char" varchar(4) NOT NULL,
	"text_1char" varchar(250),
	"text_3char" varchar(250),
	"text_4char" varchar(250)
);
ALTER TABLE "rif40_icd10" ADD CONSTRAINT "rif40_icd10_pk" PRIMARY KEY ("icd10_4char");
CREATE INDEX rif40_icd10_3char_bm ON "rif40_icd10" ("icd10_3char");
CREATE INDEX rif40_icd10_1char_bm ON "rif40_icd10" ("icd10_1char");
CREATE TABLE "rif40_population_world" (
	"year" smallint DEFAULT 1991,
	"age_sex_group" smallint NOT NULL,
	"total" double precision NOT NULL
);
ALTER TABLE "rif40_population_world" ADD CONSTRAINT "rif40_population_world_pk" PRIMARY KEY ("age_sex_group");
CREATE TABLE "t_rif40_geolevels" (
	"geography" varchar(50) NOT NULL,
	"geolevel_name" varchar(30) NOT NULL,
	"geolevel_id" smallint NOT NULL,
	"description" varchar(250) NOT NULL,
	"lookup_table" varchar(30) NOT NULL,
	"lookup_desc_column" varchar(30) NOT NULL,
	"centroidxcoordinate_column" varchar(30),
	"centroidycoordinate_column" varchar(30),
	"shapefile" varchar(512),
	"centroidsfile" varchar(512),
	"shapefile_table" varchar(30),
	"shapefile_area_id_column" varchar(30),
	"shapefile_desc_column" varchar(30),
	"st_simplify_tolerance" integer,
	"centroids_table" varchar(30),
	"centroids_area_id_column" varchar(30),
	"avg_npoints_geom" bigint,
	"avg_npoints_opt" bigint,
	"file_geojson_len" bigint,
	"leg_geom" double precision,
	"leg_opt" double precision,
	"covariate_table" varchar(30),
	"restricted" smallint DEFAULT 0,
	"resolution" smallint NOT NULL,
	"comparea" smallint NOT NULL,
	"listing" smallint NOT NULL
);
ALTER TABLE "t_rif40_geolevels" ADD CONSTRAINT "t_rif40_geolevels_pk" PRIMARY KEY ("geography","geolevel_name");
ALTER TABLE "t_rif40_geolevels" ADD CONSTRAINT "t_rif40_geol_resolution_ck" CHECK (resolution IN (0,1));
ALTER TABLE "t_rif40_geolevels" ADD CONSTRAINT "t_rif40_geol_comparea_ck" CHECK (comparea IN (0,1));
ALTER TABLE "t_rif40_geolevels" ADD CONSTRAINT "t_rif40_geol_listing_ck" CHECK (listing IN (0,1));
ALTER TABLE "t_rif40_geolevels" ADD CONSTRAINT "t_rif40_geol_restricted_ck" CHECK (restricted IN (0,1));
CREATE UNIQUE INDEX t_rif40_geolevels_uk2 ON "t_rif40_geolevels" ("geography","geolevel_id");
CREATE TABLE "t_rif40_fdw_tables" (
	"username" varchar(90) DEFAULT USER,
	"table_name" varchar(30) NOT NULL,
	"create_status" varchar(1) NOT NULL,
	"error_message" varchar(300),
	"date_created" timestamp DEFAULT LOCALTIMESTAMP,
	"rowtest_passed" smallint DEFAULT 0
);
ALTER TABLE "t_rif40_fdw_tables" ADD CONSTRAINT "t_rif40_fdw_tables_pk" PRIMARY KEY ("table_name");
ALTER TABLE "t_rif40_fdw_tables" ADD CONSTRAINT "t_rif40_fdw_tables_ck2" CHECK (rowtest_passed IN (0, 1));
ALTER TABLE "t_rif40_fdw_tables" ADD CONSTRAINT "t_rif40_fdw_tables_ck1" CHECK (create_status IN ('C','E', 'N'));
CREATE TABLE "t_rif40_parameters" (
	"param_name" varchar(30) NOT NULL,
	"param_value" varchar(50) NOT NULL,
	"param_description" varchar(250) NOT NULL
);
ALTER TABLE "t_rif40_parameters" ADD CONSTRAINT "t_rif40_parameters_pk" PRIMARY KEY ("param_name");
CREATE TABLE "rif40_icd9" (
	"icd9_3char" varchar(3),
	"icd9_4char" varchar(4) NOT NULL,
	"text_3char" varchar(250),
	"text_4char" varchar(250)
);
ALTER TABLE "rif40_icd9" ADD CONSTRAINT "rif40_icd9_pk" PRIMARY KEY ("icd9_4char");
CREATE INDEX rif40_icd9_3char_bm ON "rif40_icd9" ("icd9_3char");
CREATE TABLE "t_rif40_studies" (
	"study_id" integer NOT NULL,
	"username" varchar(90) DEFAULT USER,
	"geography" varchar(30) NOT NULL,
	"project" varchar(30) NOT NULL,
	"study_name" varchar(200) NOT NULL,
	"summary" varchar(200),
	"description" varchar(2000),
	"other_notes" varchar(2000),
	"extract_table" varchar(30) NOT NULL,
	"map_table" varchar(30) NOT NULL,
	"study_date" timestamp DEFAULT LOCALTIMESTAMP,
	"study_type" smallint NOT NULL,
	"study_state" varchar(1) DEFAULT 'C' /* created, but not executed */,
	"comparison_geolevel_name" varchar(30) NOT NULL,
	"study_geolevel_name" varchar(30) NOT NULL,
	"denom_tab" varchar(30) NOT NULL,
	"direct_stand_tab" varchar(30),
	"suppression_value" smallint NOT NULL,
	"extract_permitted" smallint DEFAULT 0,
	"transfer_permitted" smallint DEFAULT 0,
	"authorised_by" varchar(90),
	"authorised_on" timestamp,
	"authorised_notes" varchar(200),
	"audsid" varchar(90) DEFAULT SYS_CONTEXT('USERENV', 'SESSIONID')
);
ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_studies_pk" PRIMARY KEY ("study_id");
ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_studies_study_type_ck" CHECK (study_type IN (1, 11, 12, 13, 14, 15));
ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_studies_study_state_ck" CHECK (study_state IN ('C', 'V', 'E', 'R', 'U'));
ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_stud_extract_perm_ck" CHECK (extract_permitted IN (0, 1));
ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_stud_transfer_perm_ck" CHECK (transfer_permitted IN (0, 1));
CREATE UNIQUE INDEX t_rif40_extract_table_uk ON "t_rif40_studies" ("extract_table");
CREATE UNIQUE INDEX t_rif40_map_table_uk ON "t_rif40_studies" ("map_table");
CREATE TABLE "rif40_tables" (
	"theme" varchar(30) NOT NULL,
	"table_name" varchar(30) NOT NULL,
	"description" varchar(250) NOT NULL,
	"year_start" smallint NOT NULL,
	"year_stop" smallint NOT NULL,
	"total_field" varchar(30),
	"isindirectdenominator" smallint NOT NULL,
	"isdirectdenominator" smallint NOT NULL,
	"isnumerator" smallint NOT NULL,
	"automatic" smallint DEFAULT 0,
	"sex_field_name" varchar(30),
	"age_group_field_name" varchar(30),
	"age_sex_group_field_name" varchar(30) DEFAULT 'AGE_SEX_GROUP',
	"age_group_id" smallint,
	"validation_date" timestamp
);
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tables_pk" PRIMARY KEY ("table_name");
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tab_exclusive_ck" CHECK (
	(isnumerator = 1 AND isdirectdenominator = 0 AND isindirectdenominator = 0) OR
	(isnumerator = 0 AND isdirectdenominator = 1 AND isindirectdenominator = 0) OR
	(isnumerator = 0 AND isdirectdenominator = 0 AND isindirectdenominator = 1));
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tab_isnumerator_ck" CHECK (isnumerator IN (0,1));
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tab_years_ck" CHECK (year_start <= year_stop);
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tab_isindirectdenom_ck" CHECK (isindirectdenominator IN (0,1));
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tab_automatic_ck" CHECK ((automatic = 0) OR
									    (automatic = 1 AND isnumerator = 1) OR
									    (automatic = 1 AND isindirectdenominator = 1));
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tab_isdirectdenom_ck" CHECK (isdirectdenominator IN (0,1));
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tab_asg_ck" CHECK (
							(age_sex_group_field_name IS NOT NULL AND age_group_field_name IS NULL AND sex_field_name IS NULL) OR
							(age_sex_group_field_name IS NULL AND age_group_field_name IS NOT NULL AND sex_field_name IS NOT NULL));
CREATE TABLE "rif40_predefined_groups" (
	"predefined_group_name" varchar(30) NOT NULL,
	"predefined_group_description" varchar(250) NOT NULL,
	"outcome_type" varchar(20) NOT NULL,
	"condition" varchar(4000) DEFAULT '1=1'
);
ALTER TABLE "rif40_predefined_groups" ADD CONSTRAINT "rif40_predefined_groups_pk" PRIMARY KEY ("predefined_group_name");
ALTER TABLE "rif40_predefined_groups" ADD CONSTRAINT "outcome_type_ck3" CHECK (outcome_type IN ('A&E', 'ICD', 'ICD-O', 'OPCS', 'BIRTHWEIGHT'));
CREATE TABLE "sahsuland_level3" (
	"level3" varchar(20) NOT NULL,
	"name" varchar(200) NOT NULL
);
CREATE UNIQUE INDEX sahsuland_level3_pk ON "sahsuland_level3" ("level3");
CREATE TABLE "rif40_age_group_names" (
	"age_group_id" smallint NOT NULL,
	"age_group_name" varchar(50) NOT NULL
);
ALTER TABLE "rif40_age_group_names" ADD CONSTRAINT "rif40_age_group_names_pk" PRIMARY KEY ("age_group_id");
CREATE TABLE "rif40_age_groups" (
	"age_group_id" smallint NOT NULL,
	"offset" smallint NOT NULL,
	"low_age" smallint NOT NULL,
	"high_age" smallint NOT NULL,
	"fieldname" varchar(50) NOT NULL
);
ALTER TABLE "rif40_age_groups" ADD CONSTRAINT "rif40_age_groups_pk" PRIMARY KEY ("age_group_id","offset");
CREATE UNIQUE INDEX rif40_age_groups_pk2 ON "rif40_age_groups" ("age_group_id","fieldname");
CREATE TABLE "rif40_reference_tables" (
	"table_name" varchar(30) NOT NULL
);
ALTER TABLE "rif40_reference_tables" ADD CONSTRAINT "rif40_reference_tables_pk" PRIMARY KEY ("table_name");
CREATE TABLE "sahsuland_geography" (
	"level1" varchar(20) NOT NULL,
	"level2" varchar(20) NOT NULL,
	"level3" varchar(20) NOT NULL,
	"level4" varchar(20) NOT NULL
);
CREATE INDEX sahsuland_geography_bm2 ON "sahsuland_geography" ("level1");
CREATE INDEX sahsuland_geography_bm4 ON "sahsuland_geography" ("level3");
CREATE INDEX sahsuland_geography_bm3 ON "sahsuland_geography" ("level2");
CREATE UNIQUE INDEX sahsuland_geography_pk ON "sahsuland_geography" ("level4");
CREATE TABLE "rif40_geographies" (
	"geography" varchar(50) NOT NULL,
	"description" varchar(250) NOT NULL,
	"hierarchytable" varchar(30) NOT NULL,
	"srid" integer DEFAULT 0,
	"defaultcomparea" varchar(30),
	"defaultstudyarea" varchar(30),
	"postal_population_table" varchar(30),
	"postal_point_column" varchar(30),
	"partition" smallint DEFAULT 0,
	"max_geojson_digits" smallint DEFAULT 8
);
ALTER TABLE "rif40_geographies" ADD CONSTRAINT "rif40_geographies_pk" PRIMARY KEY ("geography");
ALTER TABLE "rif40_geographies" ADD CONSTRAINT "postal_population_table_ck" CHECK ((postal_population_table IS NOT NULL AND postal_point_column IS NOT NULL) OR (postal_population_table IS NULL AND postal_point_column IS NULL));
ALTER TABLE "rif40_geographies" ADD CONSTRAINT "partition_ck" CHECK (partition IN (0,1));
CREATE TABLE "rif40_table_outcomes" (
	"outcome_group_name" varchar(20) NOT NULL,
	"numer_tab" varchar(30) NOT NULL,
	"current_version_start_year" smallint
);
ALTER TABLE "rif40_table_outcomes" ADD CONSTRAINT "rif40_table_outcomes_pk" PRIMARY KEY ("outcome_group_name","numer_tab");
CREATE TABLE "t_rif40_investigations" (
	"inv_id" integer NOT NULL,
	"study_id" integer NOT NULL,
	"username" varchar(90) DEFAULT USER,
	"geography" varchar(30) NOT NULL,
	"inv_name" varchar(20) NOT NULL,
	"inv_description" varchar(250) NOT NULL,
	"classifier" varchar(30) DEFAULT 'QUANTILE',
	"classifier_bands" smallint DEFAULT 5,
	"genders" smallint NOT NULL,
	"numer_tab" varchar(30) NOT NULL,
	"year_start" smallint NOT NULL,
	"year_stop" smallint NOT NULL,
	"max_age_group" integer NOT NULL,
	"min_age_group" integer NOT NULL,
	"investigation_state" varchar(1) DEFAULT 'C' /* created, but not executed */,
	"mh_test_type" varchar(50) DEFAULT 'No Test'
);
ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_investigations_pk" PRIMARY KEY ("study_id","inv_id");
ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_state_ck" CHECK (investigation_state IN ('C', 'V', 'E', 'R', 'U'));
ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_class_bands_ck" CHECK (classifier_bands BETWEEN 2 AND 20);
ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_genders_ck" CHECK (genders BETWEEN 1 AND 3);
ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_mh_test_type_ck" CHECK (mh_test_type IN ('No Test', 'Comparison Areas', 'Unexposed Area'));
ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_classifier_ck" CHECK (classifier IN ('EQUAL_INTERVAL', 'JENKS', 'QUANTILE', 'STANDARD_DEVIATION', 'UNIQUE_INTERVAL'));
CREATE TABLE "t_rif40_user_projects" (
	"project" varchar(30) NOT NULL,
	"username" varchar(90) DEFAULT USER,
	"grant_date" timestamp DEFAULT LOCALTIMESTAMP,
	"revoke_date" timestamp
);
ALTER TABLE "t_rif40_user_projects" ADD CONSTRAINT "t_rif40_user_projects_pk" PRIMARY KEY ("project","username");
ALTER TABLE "t_rif40_user_projects" ADD CONSTRAINT "t_rif40_user_projects_date_ck" CHECK (revoke_date IS NULL OR (revoke_date > grant_date));
CREATE TABLE "t_rif40_inv_covariates" (
	"inv_id" integer NOT NULL,
	"study_id" integer NOT NULL,
	"covariate_name" varchar(20) NOT NULL,
	"username" varchar(90) DEFAULT USER,
	"geography" varchar(30) NOT NULL,
	"study_geolevel_name" varchar(30),
	"min" double precision NOT NULL,
	"max" double precision NOT NULL
);
ALTER TABLE "t_rif40_inv_covariates" ADD CONSTRAINT "t_rif40_inv_covariates_pk" PRIMARY KEY ("study_id","inv_id","covariate_name");
CREATE TABLE "t_rif40_study_areas" (
	"username" varchar(90) DEFAULT USER,
	"study_id" integer NOT NULL,
	"area_id" varchar(300) NOT NULL,
	"band_id" integer
);
ALTER TABLE "t_rif40_study_areas" ADD CONSTRAINT "t_rif40_study_areas_pk" PRIMARY KEY ("study_id","area_id");
CREATE INDEX t_rif40_study_areas_uname ON "t_rif40_study_areas" ("username");
CREATE INDEX t_rif40_study_areas_band_id ON "t_rif40_study_areas" ("band_id");
CREATE TABLE "sahsuland_covariates_level3" (
	"year" smallint NOT NULL,
	"level3" varchar(20) NOT NULL,
	"ses" smallint NOT NULL,
	"ethnicity" smallint NOT NULL
);
CREATE UNIQUE INDEX sahsuland_covariates_level3_pk ON "sahsuland_covariates_level3" ("year","level3");
CREATE TABLE "rif40_study_shares" (
	"study_id" integer NOT NULL,
	"grantor" varchar(90) DEFAULT USER,
	"grantee_username" varchar(90) NOT NULL
);
ALTER TABLE "rif40_study_shares" ADD CONSTRAINT "rif40_study_shares_pk" PRIMARY KEY ("study_id","grantee_username");
CREATE INDEX rif40_study_shares_grantor_bm ON "rif40_study_shares" ("grantor");
CREATE INDEX rif40_study_shares_grantee_bm ON "rif40_study_shares" ("grantee_username");
CREATE TABLE "t_rif40_comparison_areas" (
	"username" varchar(90) DEFAULT USER,
	"study_id" integer NOT NULL,
	"area_id" varchar(300) NOT NULL
);
ALTER TABLE "t_rif40_comparison_areas" ADD CONSTRAINT "t_rif40_comparison_areas_pk" PRIMARY KEY ("study_id","area_id");
CREATE INDEX t_rif40_comp_areas_uname ON "t_rif40_comparison_areas" ("username");
CREATE TABLE "t_rif40_inv_conditions" (
	"inv_id" integer NOT NULL,
	"study_id" integer NOT NULL,
	"username" varchar(90) DEFAULT USER,
	"line_number" integer DEFAULT 1,
	"condition" varchar(4000) DEFAULT '1=1'
);
ALTER TABLE "t_rif40_inv_conditions" ADD CONSTRAINT "t_rif40_inv_conditions_pk" PRIMARY KEY ("study_id","inv_id","line_number");
CREATE TABLE "rif40_health_study_themes" (
	"theme" varchar(30) NOT NULL,
	"description" varchar(200) NOT NULL
);
ALTER TABLE "rif40_health_study_themes" ADD CONSTRAINT "rif40_health_study_themes_pk" PRIMARY KEY ("theme");
CREATE TABLE "sahsuland_covariates_level4" (
	"year" smallint NOT NULL,
	"level4" varchar(20) NOT NULL,
	"ses" smallint NOT NULL,
	"areatri1km" smallint NOT NULL,
	"near_dist" double precision NOT NULL,
	"tri_1km" smallint NOT NULL
);
CREATE UNIQUE INDEX sahsuland_covariates_level4_pk ON "sahsuland_covariates_level4" ("year","level4");
CREATE TABLE "rif40_icd_o_3" (
	"icd_o_3_1char" varchar(20),
	"icd_o_3_4char" varchar(4) NOT NULL,
	"text_1char" varchar(250),
	"text_4char" varchar(250)
);
ALTER TABLE "rif40_icd_o_3" ADD CONSTRAINT "rif40_icd_o_3_pk" PRIMARY KEY ("icd_o_3_4char");
CREATE INDEX rif40_icd_o_3_1char_bm ON "rif40_icd_o_3" ("icd_o_3_1char");
CREATE TABLE "rif40_error_messages" (
	"error_code" integer NOT NULL,
	"tag" varchar(80) NOT NULL,
	"table_name" varchar(30),
	"cause" varchar(4000) NOT NULL,
	"action" varchar(512) NOT NULL,
	"message" varchar(512) NOT NULL
);
ALTER TABLE "rif40_error_messages" ADD CONSTRAINT "rif40_error_messages_pk" PRIMARY KEY ("error_code","tag");
ALTER TABLE "rif40_error_messages" ADD CONSTRAINT "rif40_error_messages_code_ck" CHECK (error_code IN (-1, -4088, -2290, -2291) OR error_code BETWEEN -20999 AND -20000);
CREATE TABLE "sahsuland_level4" (
	"level4" varchar(20) NOT NULL,
	"name" varchar(200) NOT NULL,
	"x_coordinate" integer NOT NULL,
	"y_coordinate" integer NOT NULL
);
CREATE UNIQUE INDEX sahsuland_level4_pk ON "sahsuland_level4" ("level4");
CREATE TABLE "t_rif40_projects" (
	"project" varchar(30) NOT NULL,
	"description" varchar(250) NOT NULL,
	"date_started" timestamp DEFAULT LOCALTIMESTAMP,
	"date_ended" timestamp
);
ALTER TABLE "t_rif40_projects" ADD CONSTRAINT "t_rif40_projects_pk" PRIMARY KEY ("project");
ALTER TABLE "t_rif40_projects" ADD CONSTRAINT "t_rif40_projects_date_ck" CHECK (date_ended IS NULL OR (date_ended >= date_started));
CREATE TABLE "sahsuland_level1" (
	"level1" varchar(20) NOT NULL,
	"name" varchar(200) NOT NULL
);
CREATE UNIQUE INDEX sahsuland_level1_pk ON "sahsuland_level1" ("level1");
CREATE TABLE "t_rif40_study_sql_log" (
	"username" varchar(90) DEFAULT USER,
	"study_id" integer NOT NULL,
	"statement_type" varchar(30) NOT NULL,
	"statement_number" integer NOT NULL,
	"log_message" varchar(4000) NOT NULL,
	"log_sqlcode" varchar(5) NOT NULL,
	"rowcount" bigint NOT NULL,
	"start_time" timestamp DEFAULT SYSTIMESTAMP(9),
	"elapsed_time" double precision NOT NULL,
	"audsid" varchar(90) DEFAULT SYS_CONTEXT('USERENV', 'SESSIONID')
);
ALTER TABLE "t_rif40_study_sql_log" ADD CONSTRAINT "t_rif40_study_sql_log_pk" PRIMARY KEY ("study_id","statement_number");
ALTER TABLE "t_rif40_study_sql_log" ADD CONSTRAINT "statement_type_ck1" CHECK (statement_type IN ('CREATE', 'INSERT', 'POST_INSERT', 'NUMERATOR_CHECK', 'DENOMINATOR_CHECK'));
CREATE INDEX t_rif40_study_sqllog_uname_bm ON "t_rif40_study_sql_log" ("username");
CREATE INDEX t_rif40_study_sqllog_type_bm ON "t_rif40_study_sql_log" ("statement_type");
CREATE TABLE "t_rif40_contextual_stats" (
	"username" varchar(90) DEFAULT USER,
	"study_id" integer NOT NULL,
	"inv_id" integer NOT NULL,
	"area_id" integer NOT NULL,
	"area_population" double precision,
	"area_observed" double precision,
	"total_comparision_population" double precision,
	"variance_high" double precision,
	"variance_low" double precision
);
ALTER TABLE "t_rif40_contextual_stats" ADD CONSTRAINT "t_rif40_contextual_stats_pk" PRIMARY KEY ("study_id","area_id","inv_id");
CREATE INDEX t_rif40_constats_uname_bm ON "t_rif40_contextual_stats" ("username");
CREATE INDEX t_rif40_constats_inv_id_fk ON "t_rif40_contextual_stats" ("inv_id");
CREATE TABLE "rif40_outcomes" (
	"outcome_type" varchar(20) NOT NULL,
	"outcome_description" varchar(250) NOT NULL,
	"current_version" varchar(20) NOT NULL,
	"current_sub_version" varchar(20),
	"previous_version" varchar(20),
	"previous_sub_version" varchar(20),
	"current_lookup_table" varchar(30),
	"previous_lookup_table" varchar(30),
	"current_value_1char" varchar(30),
	"current_value_2char" varchar(30),
	"current_value_3char" varchar(30),
	"current_value_4char" varchar(30),
	"current_value_5char" varchar(30),
	"current_description_1char" varchar(250),
	"current_description_2char" varchar(250),
	"current_description_3char" varchar(250),
	"current_description_4char" varchar(250),
	"current_description_5char" varchar(250),
	"previous_value_1char" varchar(30),
	"previous_value_2char" varchar(30),
	"previous_value_3char" varchar(30),
	"previous_value_4char" varchar(30),
	"previous_value_5char" varchar(30),
	"previous_description_1char" varchar(250),
	"previous_description_2char" varchar(250),
	"previous_description_3char" varchar(250),
	"previous_description_4char" varchar(250),
	"previous_description_5char" varchar(250)
);
ALTER TABLE "rif40_outcomes" ADD CONSTRAINT "rif40_outcomes_pk" PRIMARY KEY ("outcome_type");
ALTER TABLE "rif40_outcomes" ADD CONSTRAINT "outcome_type_ck1" CHECK (outcome_type IN ('A&E', 'ICD', 'ICD-O', 'OPCS', 'BIRTHWEIGHT'));
ALTER TABLE "rif40_outcomes" ADD CONSTRAINT "previous_lookup_table_ck" CHECK ((previous_lookup_table IS NOT NULL AND previous_version IS NOT NULL AND (
			previous_value_1char IS NOT NULL OR
			previous_value_2char IS NOT NULL OR
			previous_value_3char IS NOT NULL OR
			previous_value_4char IS NOT NULL OR
			previous_value_5char IS NOT NULL)) OR (previous_lookup_table IS NULL AND previous_version IS NULL));
ALTER TABLE "rif40_outcomes" ADD CONSTRAINT "current_value_nchar_ck" CHECK ((current_lookup_table IS NOT NULL AND current_version IS NOT NULL AND (
			(current_value_1char IS NOT NULL AND current_description_1char IS NOT NULL) OR
			(current_value_2char IS NOT NULL AND current_description_2char IS NOT NULL) OR
			(current_value_3char IS NOT NULL AND current_description_3char IS NOT NULL) OR
			(current_value_4char IS NOT NULL AND current_description_4char IS NOT NULL) OR
			(current_value_5char IS NOT NULL AND current_description_5char IS NOT NULL))) OR (current_lookup_table IS NULL));
ALTER TABLE "rif40_outcomes" ADD CONSTRAINT "current_lookup_table_ck" CHECK ((current_lookup_table IS NOT NULL AND current_version IS NOT NULL AND (
			current_value_1char IS NOT NULL OR
			current_value_2char IS NOT NULL OR
			current_value_3char IS NOT NULL OR
			current_value_4char IS NOT NULL OR
			current_value_5char IS NOT NULL)) OR (current_lookup_table IS NULL));
ALTER TABLE "rif40_outcomes" ADD CONSTRAINT "previous_value_nchar_ck" CHECK ((previous_lookup_table IS NOT NULL AND (
			(previous_value_1char IS NOT NULL AND previous_description_1char IS NOT NULL) OR
			(previous_value_2char IS NOT NULL AND previous_description_2char IS NOT NULL) OR
			(previous_value_3char IS NOT NULL AND previous_description_3char IS NOT NULL) OR
			(previous_value_4char IS NOT NULL AND previous_description_4char IS NOT NULL) OR
			(previous_value_5char IS NOT NULL AND previous_description_5char IS NOT NULL))) OR (previous_lookup_table IS NULL AND previous_version IS NULL));
CREATE TABLE "sahsuland_cancer" (
	"year" smallint NOT NULL,
	"age_sex_group" smallint NOT NULL,
	"level1" varchar(20) NOT NULL,
	"level2" varchar(20) NOT NULL,
	"level3" varchar(20) NOT NULL,
	"level4" varchar(20) NOT NULL,
	"icd" varchar(4) NOT NULL,
	"total" double precision NOT NULL
);
CREATE INDEX sahsuland_cancer_age_sex_group ON "sahsuland_cancer" ("age_sex_group");
CREATE INDEX sahsuland_cancer_icd ON "sahsuland_cancer" ("icd");
CREATE INDEX sahsuland_cancer_sex ON "sahsuland_cancer" (trunc("age_sex_group"/100));
CREATE UNIQUE INDEX sahsuland_cancer_pk ON "sahsuland_cancer" ("year","level4","age_sex_group","icd");
CREATE INDEX sahsuland_cancer_year ON "sahsuland_cancer" ("year");
CREATE INDEX sahsuland_cancer_age_group ON "sahsuland_cancer" (mod("age_sex_group",100));
CREATE INDEX sahsuland_cancer_level4 ON "sahsuland_cancer" ("level4");
CREATE INDEX sahsuland_cancer_level2 ON "sahsuland_cancer" ("level2");
CREATE INDEX sahsuland_cancer_level3 ON "sahsuland_cancer" ("level3");
CREATE INDEX sahsuland_cancer_level1 ON "sahsuland_cancer" ("level1");
CREATE TABLE "rif40_population_europe" (
	"year" smallint DEFAULT 1991,
	"age_sex_group" smallint NOT NULL,
	"total" double precision NOT NULL
);
ALTER TABLE "rif40_population_europe" ADD CONSTRAINT "rif40_population_europe_pk" PRIMARY KEY ("age_sex_group");
CREATE TABLE "rif40_version" (
	"version" varchar(50) NOT NULL,
	"schema_created" timestamp DEFAULT LOCALTIMESTAMP,
	"schema_amended" timestamp,
	"cvs_revision" varchar(50) NOT NULL
);
CREATE TABLE "t_rif40_num_denom" (
	"geography" varchar(50) NOT NULL,
	"numerator_table" varchar(30) NOT NULL,
	"denominator_table" varchar(30) NOT NULL
);
ALTER TABLE "t_rif40_num_denom" ADD CONSTRAINT "t_rif40_num_denom_pk" PRIMARY KEY ("geography","numerator_table","denominator_table");
CREATE TABLE "rif40_population_us" (
	"year" smallint DEFAULT 2000,
	"age_sex_group" smallint NOT NULL,
	"total" double precision NOT NULL
);
ALTER TABLE "rif40_population_us" ADD CONSTRAINT "rif40_population_us_pk" PRIMARY KEY ("age_sex_group");
CREATE TABLE "t_rif40_results" (
	"username" varchar(90) DEFAULT USER,
	"study_id" integer NOT NULL,
	"inv_id" integer NOT NULL,
	"band_id" integer NOT NULL,
	"genders" smallint NOT NULL,
	"adjusted" smallint NOT NULL,
	"direct_standardisation" smallint NOT NULL,
	"observed" double precision,
	"expected" double precision,
	"lower95" double precision,
	"upper95" double precision,
	"relative_risk" double precision,
	"smoothed_relative_risk" double precision,
	"posterior_probability" double precision,
	"posterior_probability_lower95" double precision,
	"posterior_probability_upper95" double precision,
	"residual_relative_risk" double precision,
	"residual_rr_lower95" double precision,
	"residual_rr_upper95" double precision,
	"smoothed_smr" double precision,
	"smoothed_smr_lower95" double precision,
	"smoothed_smr_upper95" double precision
);
ALTER TABLE "t_rif40_results" ADD CONSTRAINT "t_rif40_results_pk" PRIMARY KEY ("study_id","band_id","inv_id","genders","adjusted","direct_standardisation");
ALTER TABLE "t_rif40_results" ADD CONSTRAINT "t_rif40_res_dir_stand_ck" CHECK (direct_standardisation BETWEEN 0 AND 1);
ALTER TABLE "t_rif40_results" ADD CONSTRAINT "t_rif40_results_genders_ck" CHECK (genders BETWEEN 1 AND 3);
ALTER TABLE "t_rif40_results" ADD CONSTRAINT "t_rif40_res_adjusted_ck" CHECK (adjusted BETWEEN 0 AND 1);
CREATE INDEX t_rif40_results_band_id_bm ON "t_rif40_results" ("band_id");
CREATE INDEX t_rif40_results_inv_id_fk ON "t_rif40_results" ("inv_id");
CREATE INDEX t_rif40_results_username_bm ON "t_rif40_results" ("username");
CREATE TABLE "rif40_dual" (
	"dummy" varchar(1)
);
ALTER TABLE "rif40_outcome_groups" ADD CONSTRAINT "rif40_outcome_groups_type_fk" FOREIGN KEY ("outcome_type") REFERENCES "rif40_outcomes" ("outcome_type") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_study_sql" ADD CONSTRAINT "t_rif40_study_sql_sid_line_fk" FOREIGN KEY ("study_id","statement_number") REFERENCES "t_rif40_study_sql_log" ("study_id","statement_number") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "rif40_covariates" ADD CONSTRAINT "rif40_covariates_geog_fk" FOREIGN KEY ("geography") REFERENCES "rif40_geographies" ("geography") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "rif40_covariates" ADD CONSTRAINT "rif40_covariates_geolevel_fk" FOREIGN KEY ("geography","geolevel_name") REFERENCES "t_rif40_geolevels" ("geography","geolevel_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_geolevels" ADD CONSTRAINT "t_rif40_geolevels_geog_fk" FOREIGN KEY ("geography") REFERENCES "rif40_geographies" ("geography") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_fdw_tables" ADD CONSTRAINT "t_rif40_fdw_tables_tn_fk" FOREIGN KEY ("table_name") REFERENCES "rif40_tables" ("table_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_studies_geography_fk" FOREIGN KEY ("geography") REFERENCES "rif40_geographies" ("geography") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_std_study_geolevel_fk" FOREIGN KEY ("geography","study_geolevel_name") REFERENCES "t_rif40_geolevels" ("geography","geolevel_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_std_comp_geolevel_fk" FOREIGN KEY ("geography","comparison_geolevel_name") REFERENCES "t_rif40_geolevels" ("geography","geolevel_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "rif40_studies_project_fk" FOREIGN KEY ("project") REFERENCES "t_rif40_projects" ("project") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_stud_denom_tab_fk" FOREIGN KEY ("denom_tab") REFERENCES "rif40_tables" ("table_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_studies" ADD CONSTRAINT "t_rif40_stud_direct_stand_fk" FOREIGN KEY ("direct_stand_tab") REFERENCES "rif40_tables" ("table_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tables_age_group_id_fk" FOREIGN KEY ("age_group_id") REFERENCES "rif40_age_group_names" ("age_group_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "rif40_tables" ADD CONSTRAINT "rif40_tables_theme_fk" FOREIGN KEY ("theme") REFERENCES "rif40_health_study_themes" ("theme") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "rif40_predefined_groups" ADD CONSTRAINT "rif40_predefined_type_fk" FOREIGN KEY ("outcome_type") REFERENCES "rif40_outcomes" ("outcome_type") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "rif40_age_groups" ADD CONSTRAINT "rif40_age_group_id_fk" FOREIGN KEY ("age_group_id") REFERENCES "rif40_age_group_names" ("age_group_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "rif40_table_outcomes" ADD CONSTRAINT "rif40_outcome_group_name_fk" FOREIGN KEY ("outcome_group_name") REFERENCES "rif40_outcome_groups" ("outcome_group_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "rif40_table_outcomes" ADD CONSTRAINT "rif40_outcome_numer_tab_fk" FOREIGN KEY ("numer_tab") REFERENCES "rif40_tables" ("table_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_numer_tab_fk" FOREIGN KEY ("numer_tab") REFERENCES "rif40_tables" ("table_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_geography_fk" FOREIGN KEY ("geography") REFERENCES "rif40_geographies" ("geography") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_investigations" ADD CONSTRAINT "t_rif40_inv_study_id_fk" FOREIGN KEY ("study_id") REFERENCES "t_rif40_studies" ("study_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_user_projects" ADD CONSTRAINT "rif40_user_projects_project_fk" FOREIGN KEY ("project") REFERENCES "t_rif40_projects" ("project") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_inv_covariates" ADD CONSTRAINT "t_rif40_inv_covariates_si_fk" FOREIGN KEY ("study_id","inv_id") REFERENCES "t_rif40_investigations" ("study_id","inv_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_inv_covariates" ADD CONSTRAINT "t_rif40_inv_cov_cov_name_fk" FOREIGN KEY ("geography","study_geolevel_name","covariate_name") REFERENCES "rif40_covariates" ("geography","geolevel_name","covariate_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_inv_covariates" ADD CONSTRAINT "t_rif40_inv_cov_geolevel_fk" FOREIGN KEY ("geography","study_geolevel_name") REFERENCES "t_rif40_geolevels" ("geography","geolevel_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_inv_covariates" ADD CONSTRAINT "t_rif40_inv_cov_geography_fk" FOREIGN KEY ("geography") REFERENCES "rif40_geographies" ("geography") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_study_areas" ADD CONSTRAINT "t_rif40_studyareas_study_id_fk" FOREIGN KEY ("study_id") REFERENCES "t_rif40_studies" ("study_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "rif40_study_shares" ADD CONSTRAINT "rif40_study_shares_study_id_fk" FOREIGN KEY ("study_id") REFERENCES "t_rif40_studies" ("study_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_comparison_areas" ADD CONSTRAINT "t_rif40_compareas_study_id_fk" FOREIGN KEY ("study_id") REFERENCES "t_rif40_studies" ("study_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_inv_conditions" ADD CONSTRAINT "t_rif40_inv_conditions_si_fk" FOREIGN KEY ("study_id","inv_id") REFERENCES "t_rif40_investigations" ("study_id","inv_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_study_sql_log" ADD CONSTRAINT "t_rif40_study_sqllog_stdid_fk" FOREIGN KEY ("study_id") REFERENCES "t_rif40_studies" ("study_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_contextual_stats" ADD CONSTRAINT "t_rif40_constats_study_id_fk" FOREIGN KEY ("study_id","inv_id") REFERENCES "t_rif40_investigations" ("study_id","inv_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_num_denom" ADD CONSTRAINT "t_rif40_num_denom_geog_fk" FOREIGN KEY ("geography") REFERENCES "rif40_geographies" ("geography") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_num_denom" ADD CONSTRAINT "t_rif40_num_denom_numer_fk" FOREIGN KEY ("numerator_table") REFERENCES "rif40_tables" ("table_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "t_rif40_num_denom" ADD CONSTRAINT "t_rif40_num_denom_denom_fk" FOREIGN KEY ("denominator_table") REFERENCES "rif40_tables" ("table_name") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "t_rif40_results" ADD CONSTRAINT "t_rif40_results_study_id_fk" FOREIGN KEY ("study_id","inv_id") REFERENCES "t_rif40_investigations" ("study_id","inv_id") ON DELETE NO ACTION NOT DEFERRABLE INITIALLY IMMEDIATE;
