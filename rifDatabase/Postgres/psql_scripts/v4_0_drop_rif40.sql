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
-- Rapid Enquiry Facility (RIF) - Drop all (postgres) objects
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

\echo Dropping all objects...quote_ident(l_schema)||'.'||quote_ident(l_table

--
-- Drop all objects
--

--
-- Drop geography related tables
--
DO LANGUAGE plpgsql $$
BEGIN

	PERFORM rif40_geo_pkg.drop_rif40_geolevels_geometry_tables();

EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_geo_pkg.drop_rif40_geolevels_lookup_tables();
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
--
-- Tables
--
-- grep "CREATE TABLE" v4_0_postgres_tables.sql | sed 's/ (/;/g' | sed 's/CREATE/DROP/g'
--

DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE IF EXISTS rif40_geographies DROP CONSTRAINT rif40_geog_defcomparea_fk;
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE IF EXISTS t_rif40_geolevels DROP CONSTRAINT t_rif40_geolevels_geog_fk;
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE IF EXISTS t_rif40_studies DROP CONSTRAINT t_rif40_studies_geography_fk;
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
DO LANGUAGE plpgsql $$
BEGIN
	ALTER TABLE IF EXISTS t_rif40_studies DROP CONSTRAINT t_rif40_std_study_geolevel_fk;
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;

DROP TABLE IF EXISTS sahsuland_pop;
DROP TABLE IF EXISTS sahsuland_cancer;
DROP TABLE IF EXISTS sahsuland_geography;
DROP TABLE IF EXISTS sahsuland_level1;
DROP TABLE IF EXISTS sahsuland_level2;
DROP TABLE IF EXISTS sahsuland_level3;
DROP TABLE IF EXISTS sahsuland_level4;
DROP TABLE IF EXISTS sahsuland_covariates_level3;
DROP TABLE IF EXISTS sahsuland_covariates_level4;

DROP TABLE IF EXISTS t_rif40_num_denom;
DROP TABLE IF EXISTS rif40_chi2;
DROP TABLE IF EXISTS rif40_pois_distribution;

DROP TABLE IF EXISTS rif40_opcs4;
DROP TABLE IF EXISTS rif40_a_and_e;
DROP TABLE IF EXISTS rif40_icd9;
DROP TABLE IF EXISTS rif40_icd10;
DROP TABLE IF EXISTS rif40_icd_o_3;
DROP TABLE IF EXISTS rif40_population_world;
DROP TABLE IF EXISTS rif40_population_europe;
DROP TABLE IF EXISTS rif40_population_us;

DROP TABLE IF EXISTS rif40_predefined_groups;
DROP TABLE IF EXISTS rif40_table_outcomes;
DROP TABLE IF EXISTS rif40_outcome_groups;
DROP TABLE IF EXISTS rif40_outcomes;
DROP TABLE IF EXISTS rif40_geographies CASCADE;
DROP TABLE IF EXISTS rif40_version;
DROP TABLE IF EXISTS t_rif40_fdw_tables CASCADE;
DROP TABLE IF EXISTS rif40_tables CASCADE;
DROP TABLE IF EXISTS rif40_health_study_themes;
DROP TABLE IF EXISTS rif40_age_groups;
DROP TABLE IF EXISTS rif40_age_group_names;
DROP TABLE IF EXISTS rif40_error_messages;
DROP TABLE IF EXISTS rif40_reference_tables;
DROP TABLE IF EXISTS rif40_dual;

DROP TABLE IF EXISTS rif40_tables_and_views;
DROP TABLE IF EXISTS rif40_columns;
DROP TABLE IF EXISTS rif40_triggers;

DROP SEQUENCE IF EXISTS rif40_inv_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rif40_study_id_seq CASCADE;

--
-- Functions
--
-- grep "GRANT EXECUTE" v4_0_rif40*pkg.sql | grep "TO rif40" | awk '{printf("DROP"); for (i=4;i<=NF-2;i++) {printf(" %s", $i);} printf(";\n");}'
--

--
-- Drop INSTEAD OF triggers and functions
--
DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_trg_pkg.drop_instead_of_triggers();
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_trg_pkg.rif40_drop_table_triggers();
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
	
DO LANGUAGE plpgsql $$
BEGIN
	DROP FUNCTION IF EXISTS rif40_dmp_pkg.csv_dump(VARCHAR, rif40_dmp_pkg.DELIMITER_TYPE, rif40_dmp_pkg.LINE_TERMINATOR, BOOLEAN, BOOLEAN, INTEGER);
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ignored error in drop: %', sqlerrm;	
END;
$$;

--
-- These cause mysterious error after a database drop (when they should not exist by definition). 
-- Should not be here as owned by Postgres anyway.
--
--DROP FUNCTION IF EXISTS rif40_r_pkg.r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR);
--DROP FUNCTION IF EXISTS rif40_r_pkg._r_init(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL);

DROP TYPE IF EXISTS rif40_dmp_pkg.DELIMITER_TYPE CASCADE;
DROP TYPE IF EXISTS rif40_dmp_pkg.LINE_TERMINATOR CASCADE;

DROP TYPE IF EXISTS rif40_log_pkg.rif40_log_debug_level CASCADE;
DROP TYPE IF EXISTS rif40_log_pkg.rif40_debug_record CASCADE;

DROP FUNCTION IF EXISTS rif40_r_pkg.r_cleanup();
DROP FUNCTION IF EXISTS rif40_r_pkg._r_cleanup();
DROP FUNCTION IF EXISTS rif40_r_pkg.installed_packages();
DROP FUNCTION IF EXISTS rif40_r_pkg.install_package_from_internet(VARCHAR);
DROP FUNCTION IF EXISTS rif40_r_pkg._install_all_packages_from_internet();

DROP FUNCTION IF EXISTS rif40_r_pkg.rif40_install_rcmd(VARCHAR, VARCHAR);

DROP FUNCTION IF EXISTS rif40_trg_pkg.create_instead_of_triggers() CASCADE;
DROP FUNCTION IF EXISTS rif40_trg_pkg.drop_instead_of_triggers() CASCADE;
DROP FUNCTION IF EXISTS rif40_trg_pkg.rif40_drop_table_triggers() CASCADE;
DROP FUNCTION IF EXISTS rif40_trg_pkg.rif40_db_name_check(VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_trg_pkg.rif40_sql_injection_check(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_trg_pkg.lp_outcomes_check_column(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) CASCADE;

DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_verify_state_change(INTEGER, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_run_study(INTEGER, INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_create_extract(INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_compute_results(INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_insert_extract(INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_create_insert_statement(INTEGER, VARCHAR, INTEGER, INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_execute_insert_statement(INTEGER, VARCHAR, VARCHAR, INTEGER, INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_study_ddl_definer(INTEGER, VARCHAR, VARCHAR, VARCHAR[]);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_delete_study(INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_reset_study(INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(BOOLEAN);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_clone_study(INTEGER);
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_create_disease_mapping_example(
	VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR[],VARCHAR,VARCHAR,VARCHAR,VARCHAR,INTEGER,INTEGER,VARCHAR[],VARCHAR[],VARCHAR[]) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.does_role_exist(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_does_role_exist(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sm_pkg.rif40_rename_map_and_extract_tables(INTEGER, INTEGER);

DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_drop_user_table_or_view(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_method4(VARCHAR, VARCHAR) CASCADE; 
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_hash_partition(VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_range_partition(VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_range_partition_create(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR);

DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_a();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_b();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_c();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_d();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_e();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_f();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_g();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_h();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_i();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_j();
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_check_k();

DO LANGUAGE plpgsql $$
BEGIN
	DROP TRIGGER IF EXISTS trg_rif40_parameters ON rif40_parameters;
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_parameters();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_num_denom_checks() CASCADE;
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_fdw_tables();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_studies();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_error_messages_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_geog_hierarchytable();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_outcomes_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_predefined_groups_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_tables_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_table_outcomes_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_version_checks();

DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_log_setup() CASCADE;
DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_send_debug_to_info(BOOLEAN) CASCADE;
DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_get_error_code_action(INTEGER, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_error(INTEGER, VARCHAR, VARIADIC VARCHAR[]) CASCADE;
DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_add_to_debug(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_remove_from_debug(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_get_debug(VARCHAR) CASCADE;
DO LANGUAGE plpgsql $$
BEGIN
	DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_log(RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL, VARCHAR, VARIADIC VARCHAR[]) CASCADE;
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;
DO LANGUAGE plpgsql $$
BEGIN
	DROP FUNCTION IF EXISTS rif40_log_pkg.rif40_is_debug_enabled(function_name VARCHAR, debug RIF40_LOG_PKG.RIF40_LOG_DEBUG_LEVEL) CASCADE;
EXCEPTION	
	WHEN others THEN
			RAISE INFO 'Ingored error in drop: %', sqlerrm;	
END;
$$;

DROP FUNCTION IF EXISTS rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.populate_hierarchy_table();
DROP FUNCTION IF EXISTS rif40_geo_pkg.populate_hierarchy_table(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.create_rif40_geolevels_lookup_tables();
DROP FUNCTION IF EXISTS rif40_geo_pkg.create_rif40_geolevels_lookup_tables(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.create_rif40_geolevels_geometry_tables();
DROP FUNCTION IF EXISTS rif40_geo_pkg.create_rif40_geolevels_geometry_tables(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.drop_rif40_geolevels_lookup_tables();
DROP FUNCTION IF EXISTS rif40_geo_pkg.drop_rif40_geolevels_geometry_tables();
DROP FUNCTION IF EXISTS rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.check_rif40_hierarchy_lookup_tables();
DROP FUNCTION IF EXISTS rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.populate_rif40_geometry_tables();
DROP FUNCTION IF EXISTS rif40_geo_pkg.populate_rif40_geometry_tables(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.fix_null_geolevel_names(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.fix_null_geolevel_names();
DROP FUNCTION IF EXISTS rif40_geo_pkg.get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.add_population_to_rif40_geolevels_geometry();
DROP FUNCTION IF EXISTS rif40_geo_pkg.populate_rif40_tiles(VARCHAR);
DROP FUNCTION IF EXISTS rif40_xml_pkg._populate_rif40_tiles_explain_ddl(sql_stmt character varying, l_zoomlevel integer, l_geography character varying);

--
-- Drop old and new (without st_simplify_tolerance) forms
--
DROP FUNCTION IF EXISTS rif40_geo_pkg.simplify_geometry(VARCHAR, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg.simplify_geometry(VARCHAR);
DROP FUNCTION IF EXISTS rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_checks(VARCHAR, VARCHAR, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_I(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_I(VARCHAR, VARCHAR, VARCHAR, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_II(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_II(VARCHAR, VARCHAR, VARCHAR, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_III(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg._simplify_geometry_phase_III(VARCHAR, VARCHAR, VARCHAR, NUMERIC);
DROP FUNCTION IF EXISTS rif40_geo_pkg.gid_rowindex_fix(VARCHAR);

DROP FUNCTION IF EXISTS rif40_geo_pkg.longitude2tile(DOUBLE PRECISION, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.latitude2tile(DOUBLE PRECISION, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.tile2latitude(INTEGER, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.tile2longitude(INTEGER, INTEGER);
DROP FUNCTION IF EXISTS rif40_geo_pkg.y_osm_tile2_tms_tile(INTEGER, INTEGER);

DROP FUNCTION IF EXISTS rif40_sql_pkg._print_table_size(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_explain_ddl(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl(VARCHAR[]) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.sys_context(VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.systimestamp(BIGINT) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_startup() CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_ddl_checks() CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.is_rif40_user_manager_or_schema() CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.is_rif40_manager_or_schema() CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_get_function_arg_types(oid[]) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_auto_indirect_checks(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_object_resolve(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_object_resolve(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_is_object_resolvable(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_num_denom_validate(VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_create_fdw_table(VARCHAR, INTEGER, BOOLEAN, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_create_fdw_views(BOOLEAN, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_create_fdw_view(BOOLEAN, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[]) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_fdw_table_select_test(VARCHAR, VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_update_fdw_tables(VARCHAR, VARCHAR, VARCHAR, INTEGER) CASCADE;
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_getgeolevelextentcommon(l_geography character varying, l_geolevel_view character varying, l_map_area character varying, l_study_id integer, OUT y_max real, OUT x_max real, OUT y_min real, OUT x_min real);
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR[], INTEGER);
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR[], INTEGER, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR[], INTEGER, BOOLEAN, INTEGER);
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR[], INTEGER, BOOLEAN, INTEGER, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_geojson_explain_ddl(sql_stmt character varying, geolevel_view character varying, geolevel_area_id_list character varying[]);
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_geojson_explain_ddl2(sql_stmt character varying, x_min real, y_min real, x_max real, y_max real, l_geolevel_view character varying);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_get_geojson_as_js(l_geography character varying, geolevel_view character varying, geolevel_area character varying, geolevel_area_id character varying, return_one_row boolean, produce_json_only boolean);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, INTEGER, VARCHAR, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, INTEGER, VARCHAR, BOOLEAN, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, INTEGER, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_get_geojson_tiles(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL, INTEGER, BOOLEAN, BOOLEAN);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_getgeolevelfullextent(l_geography character varying, l_geolevel_view character varying, OUT y_max real, OUT x_max real, OUT y_min real, OUT x_min real);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_getgeolevelfullextentforstudy(l_geography character varying, l_geolevel_view character varying, l_study_id integer, OUT y_max real, OUT x_max real, OUT y_min real, OUT x_min real);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_getgeolevelboundsforarea(l_geography character varying, l_geolevel_view character varying, l_map_area character varying, OUT y_max real, OUT x_max real, OUT y_min real, OUT x_min real);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_getgeometrycolumnnames(l_geography character varying);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_closegetmapareaattributecursor(closecursor1 character varying);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_GetMapAreas(VARCHAR, VARCHAR, REAL, REAL, REAL, REAL);

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_hash_partition_functional_index(l_schema character varying, l_table character varying, l_column character varying, num_partitions integer, OUT ddl_stmt character varying[]);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_hash_partition(l_schema character varying, l_table character varying, l_column character varying, l_num_partitions integer);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_hash_partition(VARCHAR, VARCHAR, VARCHAR, VARCHAR[], INTEGER);

DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_hash_partition_create(l_schema character varying, master_table character varying, partition_table character varying, l_column character varying, l_value integer, num_partitions integer);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_hash_partition_create_insert(VARCHAR, VARCHAR, VARCHAR, integer, integer, OUT ddl_stmt character varying[], OUT index_name character varying);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_hash_partition_create_insert(VARCHAR, VARCHAR, VARCHAR, VARCHAR, integer, integer, OUT ddl_stmt character varying[], OUT index_name character varying);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_range_partition_create_insert(VARCHAR, VARCHAR, VARCHAR, integer, OUT ddl_stmt character varying[], OUT index_name character varying);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_range_partition_create_insert(VARCHAR, VARCHAR, VARCHAR, VARCHAR, integer, OUT ddl_stmt character varying[], OUT index_name character varying);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_partition_count(l_schema character varying, l_table character varying);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_drop_master_trigger(l_schema character varying, l_table character varying);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_triggers(l_schema character varying, l_table character varying, l_column character varying, enable_or_disable character varying, OUT ddl_stmt character varying[]);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_create(l_schema character varying, master_table character varying, partition_table character varying, l_column character varying, l_value character varying);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_create_setup(VARCHAR, VARCHAR, VARCHAR, INTEGER, 
	OUT VARCHAR[], OUT VARCHAR[], OUT VARCHAR[], OUT INTEGER, OUT INTEGER, OUT INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_create_setup(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR[], INTEGER, 
	OUT VARCHAR[], OUT VARCHAR[], OUT VARCHAR[], OUT INTEGER, OUT INTEGER, OUT INTEGER);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_common_partition_create_complete(l_schema character varying, l_table character varying, l_column character varying, index_name character varying);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_getmapareaattributevalue(getmap_temp_table character varying, getmap_ref_cursor refcursor, l_offset integer, l_row_limit integer);
DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_createmapareaattributesource_explain_ddl(sql_stmt character varying, l_geography character varying, l_geolevel_select character varying);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_deletemapareaattributesource(closecursor1 character varying);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_getallattributesforgeolevelattributetheme(l_geography character varying, l_geolevel_select character varying, l_theme character varying, l_attribute_name_array character varying[]);
DROP FUNCTION IF EXISTS rif40_geo_pkg.rif40_zoom_levels(l_latitude numeric);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_createmapareaattributesource(c4getallatt4theme character varying, l_geography character varying, l_geolevel_select character varying, l_theme character varying, l_attribute_source character varying, l_attribute_name_array character varying[]
);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_hash(l_value character varying, l_bucket integer);
DROP FUNCTION IF EXISTS rif40_sql_pkg._rif40_hash_bucket_check(l_value integer, l_bucket integer, l_bucket_requested integer);
DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_getadjacencymatrix(l_study_id integer);
DROP FUNCTION IF EXISTS rif40_sql_pkg.rif40_table_diff(test_tag character varying, table_1 character varying, table_2 character varying, 
	table_1_columns character varying[], table_2_columns character varying[]);
 
DROP TABLE IF EXISTS g_rif40_comparison_areas;
DROP TABLE IF EXISTS g_rif40_study_areas;

DROP TABLE IF EXISTS simplification_points;
DROP TABLE IF EXISTS simplification_lines;
DROP TABLE IF EXISTS simplification_polygons;
DROP TABLE IF EXISTS simplification_lines_join_duplicates;

DROP TABLE IF EXISTS gis.x_sahsu_cen_level4;
DROP TABLE IF EXISTS gis.x_sahsu_level1;
DROP TABLE IF EXISTS gis.x_sahsu_level2;
DROP TABLE IF EXISTS gis.x_sahsu_level3;
DROP TABLE IF EXISTS gis.x_sahsu_level4;

DROP TABLE IF EXISTS gis.sahsu_cen_level4;
DROP TABLE IF EXISTS gis.sahsu_grd_level1;
DROP TABLE IF EXISTS gis.sahsu_grd_level2;
DROP TABLE IF EXISTS gis.sahsu_grd_level3;
DROP TABLE IF EXISTS gis.sahsu_grd_level4;

DROP TABLE IF EXISTS rif_studies.s1_extract;
DROP TABLE IF EXISTS rif_studies.s1_map;

--
-- Views
--
-- grep "CREATE OR REPLACE VIEW" v4_0_postgres_views.sql | sed 's/ (/;/g' | sed 's/CREATE OR REPLACE/DROP/g' | awk -F";" '{print $1.";"}'
--
DROP VIEW IF EXISTS rif40_comparison_areas;
DROP VIEW IF EXISTS rif40_contextual_stats;
DROP VIEW IF EXISTS rif40_geolevels;
DROP VIEW IF EXISTS rif40_investigations;
DROP VIEW IF EXISTS rif40_inv_conditions;
DROP VIEW IF EXISTS rif40_inv_covariates;
DROP VIEW IF EXISTS rif40_parameters;
DROP VIEW IF EXISTS rif40_projects;
DROP VIEW IF EXISTS rif40_results;
DROP VIEW IF EXISTS rif40_studies;
DROP VIEW IF EXISTS rif40_study_areas;
DROP VIEW IF EXISTS rif40_study_sql;
DROP VIEW IF EXISTS rif40_study_sql_log;
DROP VIEW IF EXISTS rif40_user_projects;
DROP VIEW IF EXISTS rif40_fdw_tables;

DROP TABLE IF EXISTS t_rif40_parameters;
DROP TABLE IF EXISTS t_rif40_study_sql;
DROP TABLE IF EXISTS t_rif40_study_sql_log;
DROP TABLE IF EXISTS t_rif40_user_projects;
DROP TABLE IF EXISTS t_rif40_contextual_stats;
DROP TABLE IF EXISTS t_rif40_results;
DROP TABLE IF EXISTS t_rif40_comparison_areas;
DROP TABLE IF EXISTS t_rif40_study_areas;
DROP TABLE IF EXISTS t_rif40_inv_covariates;
DROP TABLE IF EXISTS t_rif40_inv_conditions;
DROP TABLE IF EXISTS rif40_covariates;
DROP TABLE IF EXISTS rif40_study_shares;
DROP TABLE IF EXISTS t_rif40_investigations;
DROP TABLE IF EXISTS t_rif40_studies;
DROP TABLE IF EXISTS t_rif40_projects;
DROP TABLE IF EXISTS t_rif40_geolevels;

DROP VIEW IF EXISTS rif40_user_version;
DROP VIEW IF EXISTS user_role_privs;
DROP VIEW IF EXISTS user_role_privs;

--
-- Test data
--
DROP TABLE IF EXISTS rif_studies.sahsuland_example_extract;
DROP TABLE IF EXISTS rif_studies.sahsuland_example_map;
DROP TABLE IF EXISTS rif_studies.sahsuland_example_bands;
DROP TABLE IF EXISTS rif_studies.test_4_study_id_1_extract;
DROP TABLE IF EXISTS rif_studies.test_4_study_id_1_map;
DROP TABLE IF EXISTS rif_studies.test_4_study_id_1_bands;

--
-- Old, no longer needed
--
--DROP FUNCTION lf_rif40_check_all_geography2();
--DROP FUNCTION p_rif40_check_all_geography();
--DROP FUNCTION lp_rif40_check_all_geography();
--DROP FUNCTION rif40_geo_pkg.rif40_geolevels_geometry_tables();
--DROP FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR);
--DROP FUNCTION lf_rif40_check_all_geography2(INTEGER, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR);
--DROP FUNCTION t_rif40_geolevels_geometry_insert();
--
--DROP VIEW rif40_result_maps;
--DROP VIEW ew2001_coa2001;
--DROP TABLE t_rif40_result_maps;
--
-- These needs to be removed when no longer needed
--
--DROP VIEW geotest_london_ladua2001;
--DROP VIEW geotest_london_ward2001;
--DROP VIEW geotest_london_soa2001;
--DROP VIEW geotest_london_oa2001;

--
-- Added for Macos
--
DROP TABLE IF EXISTS t_rif40_sahsu_geometry CASCADE;
DROP TABLE IF EXISTS t_rif40_sahsu_maptiles CASCADE;
DROP TABLE IF EXISTS t_rif40_geolevels_geometry_sahsu_level1;
DROP TABLE IF EXISTS t_rif40_geolevels_geometry_sahsu_level2;
DROP TABLE IF EXISTS t_rif40_geolevels_geometry_sahsu_level3;
DROP TABLE IF EXISTS t_rif40_geolevels_geometry_sahsu_level4;
DROP TABLE IF EXISTS p_rif40_geolevels_geometry_sahsu_level1;
DROP TABLE IF EXISTS p_rif40_geolevels_geometry_sahsu_level2;
DROP TABLE IF EXISTS p_rif40_geolevels_geometry_sahsu_level3;
DROP TABLE IF EXISTS p_rif40_geolevels_geometry_sahsu_level4;
DROP TABLE IF EXISTS t_rif40_geolevels_maptiles_sahsu_level1;
DROP TABLE IF EXISTS t_rif40_geolevels_maptiles_sahsu_level2;
DROP TABLE IF EXISTS t_rif40_geolevels_maptiles_sahsu_level3;
DROP TABLE IF EXISTS t_rif40_geolevels_maptiles_sahsu_level4;
DROP TABLE IF EXISTS p_rif40_geolevels_maptiles_sahsu_level1;
DROP TABLE IF EXISTS p_rif40_geolevels_maptiles_sahsu_level2;
DROP TABLE IF EXISTS p_rif40_geolevels_maptiles_sahsu_level3;
DROP TABLE IF EXISTS p_rif40_geolevels_maptiles_sahsu_level4;

DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_covariates_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_rif40_study_shares_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_studies_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_study_areas_checks2();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_comp_areas_checks2();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_inv_conditions_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_inv_covariates_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_geolevels_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_user_projects_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_results_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_contextualstats_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_study_sql_log_checks();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trigger_fct_t_rif40_study_sql_checks();
DROP FUNCTION IF EXISTS rif40.t_rif40_sahsu_geometry_insert();
DROP FUNCTION IF EXISTS rif40.t_rif40_sahsu_maptiles_insert();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_comparison_areas();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_contextual_stats();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_inv_conditions();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_inv_covariates();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_investigations();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_results();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_study_areas();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_study_sql();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_study_sql_log();
DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_rif40_user_projects();

DROP AGGREGATE IF EXISTS array_agg_mult(anyarray);
DROP TYPE IF EXISTS rif40_goejson_type;

\echo Dropped all objects.

--
-- Eof
