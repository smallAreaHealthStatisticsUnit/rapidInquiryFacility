-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Create PG psql code (Geographic processing)
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

\echo Creating PG psql code (Geographic processing)...

\i ../PLpgsql/rif40_geo_pkg/get_srid_projection_parameters.sql
\i ../PLpgsql/rif40_geo_pkg/drop_rif40_geolevels_lookup_tables.sql

--
-- Simplification sub-programs (now run separately)
--
--\i  ../PLpgsql/rif40_geo_pkg/v4_0_rif40_geo_pkg_simplification.sql

\i ../PLpgsql/rif40_geo_pkg/rif40_zoom_levels.sql
\i ../PLpgsql/rif40_geo_pkg/simplify_geometry.sql
\i ../PLpgsql/rif40_geo_pkg/create_rif40_geolevels_lookup_tables.sql
\i ../PLpgsql/rif40_geo_pkg/populate_rif40_geometry_tables.sql
\i ../PLpgsql/rif40_geo_pkg/create_rif40_geolevels_geometry_tables.sql
\i ../PLpgsql/rif40_geo_pkg/fix_null_geolevel_names.sql
\i ../PLpgsql/rif40_geo_pkg/populate_hierarchy_table.sql
\i ../PLpgsql/rif40_geo_pkg/add_population_to_rif40_geolevels_geometry.sql
\i ../PLpgsql/rif40_geo_pkg/drop_rif40_geolevels_geometry_tables.sql
\i ../PLpgsql/rif40_geo_pkg/check_rif40_hierarchy_lookup_tables.sql
\i ../PLpgsql/rif40_geo_pkg/rif40_get_default_comparison_area.sql
\i ../PLpgsql/rif40_geo_pkg/gid_rowindex_fix.sql
\i ../PLpgsql/rif40_geo_pkg/populate_rif40_tiles.sql

--
-- Moved to XML package
--
DROP FUNCTION IF EXISTS rif40_geo_pkg.get_geojson_as_js(VARCHAR, VARCHAR, VARCHAR, VARCHAR);

GRANT EXECUTE ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.get_srid_projection_parameters(VARCHAR, VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_lookup_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.create_rif40_geolevels_geometry_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.check_rif40_hierarchy_lookup_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR, VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.lf_check_rif40_hierarchy_lookup_tables(VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_hierarchy_table(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(VARCHAR, VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.add_population_to_rif40_geolevels_geometry(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.fix_null_geolevel_names() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables() TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables() TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.populate_rif40_geometry_tables(VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, NUMERIC) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, NUMERIC) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) TO rif40;

\df rif40_geo_pkg.create_rif40_geolevels_geometry_tables
\df rif40_geo_pkg.create_rif40_geolevels_lookup_tables
\df rif40_geo_pkg.drop_rif40_geolevels_lookup_tables
\df rif40_geo_pkg.drop_rif40_geolevels_geometry_tables
\df rif40_geo_pkg.populate_rif40_geometry_tables
\df rif40_geo_pkg.rif40_get_default_comparison_area

\echo Created PG psql code (Geographic processing).
--
-- Eof
