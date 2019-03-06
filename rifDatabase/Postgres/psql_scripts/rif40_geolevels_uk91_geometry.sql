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
-- Rapid Enquiry Facility (RIF) -  Create and populate rif40_geolevels_geometry, intersection and lookup tables (Geographic processing) - SAHSULAND version
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

\echo Create and populate rif40_geolevels_geometry, intersection and lookup tables (Geographic processing)- UK91 version...
--

\set ECHO all
\timing
\set ON_ERROR_STOP OFF
\pset pager on
--
SELECT rif40_geo_pkg.drop_rif40_geolevels_geometry_tables('UK91');
SELECT rif40_geo_pkg.drop_rif40_geolevels_lookup_tables('UK91');
DROP TABLE sahsuland_geography_orig;
\set ON_ERROR_STOP ON

--
-- Comment out this for more debug and do not exit on error
--

--\set ON_ERROR_STOP OFF
\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
--
	rif40_geo_pkg_functions 	VARCHAR[] := ARRAY['lf_check_rif40_hierarchy_lookup_tables', 
							'populate_rif40_geometry_tables', 
							'populate_hierarchy_table', 
							'create_rif40_geolevels_geometry_tables',
							'add_population_to_rif40_geolevels_geometry',
							'fix_null_geolevel_names'];
	l_function 			VARCHAR;
	i				INTEGER:=0;
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
BEGIN
--
	stp:=clock_timestamp();
--
-- Call init function is case called from main build scripts
--
	PERFORM rif40_sql_pkg.rif40_startup();
--
-- Turn on some debug
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
	FOREACH l_function IN ARRAY rif40_geo_pkg_functions LOOP
		RAISE INFO 'Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
--
-- These are the new T_RIF40_<GEOGRAPHY>_GEOMETRY tables and
-- new T_RIF40_GEOLEVELS_GEOMETRY_<GEOGRAPHY>_<GEOELVELS> partitioned tables
--
	PERFORM rif40_geo_pkg.create_rif40_geolevels_geometry_tables('UK91');
--
-- Create and populate rif40_geolevels lookup and create hierarchy tables 
--
	PERFORM rif40_geo_pkg.create_rif40_geolevels_lookup_tables('UK91');
--
-- Populate geometry and hierarchy tables
--
	PERFORM rif40_geo_pkg.populate_rif40_geometry_tables('UK91');
	PERFORM rif40_geo_pkg.populate_hierarchy_table('UK91'); 
--
-- Add denominator population table to geography geolevel geometry data
--
--	IF current_database() = 'rif40' THEN
--		PERFORM rif40_geo_pkg.add_population_to_rif40_geolevels_geometry('UK91', 'V_UK91_RIF_POP_ASG_1_OA2001'); 
--	END IF;
--
-- Fix NULL geolevel names in geography geolevel geometry and lookup table data 
--
	PERFORM rif40_geo_pkg.fix_null_geolevel_names('UK91'); 
--
-- Simplify geometry
--
--	PERFORM rif40_geo_pkg.simplify_geometry('UK91', 10 /* l_min_point_resolution [1] */);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	RAISE INFO 'Processed UK91 geography: %s', took;
--
END;
$$;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_uk91_geometry
 WHERE geolevel_name = 'SCOUNTRY91';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_uk91_geometry
 WHERE geolevel_name = 'COUNTRY91';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_uk91_geometry
 WHERE geolevel_name = 'REGION91';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_uk91_geometry
 WHERE geolevel_name = 'COUNTY91'
 ORDER BY 2 LIMIT 20;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_uk91_geometry
 WHERE geolevel_name = 'DISTRICT91'
 ORDER BY 2 LIMIT 20;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_uk91_geometry
 WHERE geolevel_name = 'WARD91'
 ORDER BY 2 LIMIT 20;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_uk91_geometry
 WHERE geolevel_name = 'ED91'
 ORDER BY 2 LIMIT 20;

--
-- Make columns NOT NULL - Cannot be done with PL/pgsql - causes:
-- cannot ALTER TABLE "sahsuland_level1" because it is being used by active queries in this session
--
ALTER TABLE uk91_scountry91 ALTER COLUMN name SET NOT NULL;
ALTER TABLE uk91_country91 ALTER COLUMN name SET NOT NULL;
ALTER TABLE uk91_region91 ALTER COLUMN name SET NOT NULL;
ALTER TABLE uk91_county91 ALTER COLUMN name SET NOT NULL;
ALTER TABLE uk91_district91 ALTER COLUMN name SET NOT NULL;
ALTER TABLE uk91_ward91 ALTER COLUMN name SET NOT NULL;
ALTER TABLE uk91_ed91 ALTER COLUMN name SET NOT NULL;

--
-- Vaccum ANALYZE tables
--
VACUUM (ANALYZE, VERBOSE) uk91_geography;
VACUUM (ANALYZE, VERBOSE) t_rif40_uk91_geometry;
VACUUM (ANALYZE, VERBOSE) uk91_scountry91;
VACUUM (ANALYZE, VERBOSE) uk91_country91;
VACUUM (ANALYZE, VERBOSE) uk91_region91;
VACUUM (ANALYZE, VERBOSE) uk91_county91;
VACUUM (ANALYZE, VERBOSE) uk91_district91;
VACUUM (ANALYZE, VERBOSE) uk91_ward91;
VACUUM (ANALYZE, VERBOSE) uk91_ed91;

CREATE TABLE uk91_geography_orig AS SELECT * FROM uk91_geography;
TRUNCATE TABLE uk91_geography_orig;
--
-- Import geospatial intersction files
--
\COPY uk91_geography_orig(scountry91, country91, region91, county91, district91, ward91, ed91) FROM  '../sahsuv3_v4/data/uk91_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
CREATE UNIQUE INDEX uk91_geography_orig_pk ON uk91_geography_orig(oa2001);
VACUUM (ANALYZE, VERBOSE) uk91_geography_orig;

--
-- For vi's benefit

--
-- This algorithm implments the prototype in: ../postgres/gis_intersection_prototype.sql. The SAHSU alogoritm differs in that
-- Where intersections overlap (e.g. level4 appears in more than 1 level3) the level3 with the greatest intersected area is chosen 
-- This is differed to ARCGis - see the prototype file for a more detailed investigation
--
WITH a_missing AS (
	SELECT scountry91, country91, region91, county91, district91, ward91, ed91
	  FROM uk91_geography
	EXCEPT
	SELECT scountry91, country91, region91, county91, district91, ward91, ed91
	  FROM uk91_geography_orig
),
a_extra AS (
	SELECT scountry91, country91, region91, county91, district91, ward91, ed91
	  FROM uk91_geography_orig
	EXCEPT
	SELECT scountry91, country91, region91, county91, district91, ward91, ed91
	  FROM uk91_geography
), b AS (
	SELECT a_missing.ed91, 
               a_missing.ward91 AS missing_ward91, 
               a_extra.ward91 AS extra_ward91, 
               a_missing.district91 AS missing_district91, 
               a_extra.district91 AS extra_district91, 
               a_missing.county91 AS missing_county91, 
               a_extra.county91 AS extra_county91, 
               a_missing.region91 AS missing_region91, 
               a_extra.region91 AS extra_region91, 
               a_missing.country91 AS missing_country91, 
               a_extra.country91 AS extra_country91, 
               a_missing.scountry91 AS missing_scountry91, 
               a_extra.scountry91 AS extra_scountry91 
	  FROM a_missing
		LEFT OUTER JOIN a_extra ON (a_extra.ward91 = a_missing.ward91)
)
SELECT ed91,
       CASE WHEN missing_ward91 = extra_ward91 THEN 'Same: '||extra_ward91 ELSE 'Move: '||extra_ward91||'=>'||missing_ward91 END AS ward91,
       CASE WHEN missing_district91 = extra_district91 THEN 'Same: '||extra_district91 ELSE 'Move: '||extra_district91||'=>'||missing_district91 END AS district91,
       CASE WHEN missing_county91 = extra_county91 THEN 'Same: '||extra_county91 ELSE 'Move: '||extra_county91||'=>'||missing_county91 END AS county91,
       CASE WHEN missing_region91 = extra_region91 THEN 'Same: '||extra_region91 ELSE 'Move: '||extra_region91||'=>'||missing_region91 END AS region91,
       CASE WHEN missing_country91 = extra_country91 THEN 'Same: '||extra_country91 ELSE 'Move: '||extra_country91||'=>'||missing_country91 END AS country91,
       CASE WHEN missing_scountry91 = extra_scountry91 THEN 'Same: '||extra_scountry91 ELSE 'Move: '||extra_scountry91||'=>'||missing_scountry91 END AS scountry91
  FROM b
ORDER BY 1, 2, 3, 4, 5, 6;

DROP TABLE IF EXISTS uk91_geography_orig;

SELECT geography, geolevel_name, geolevel_id, shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance
  FROM t_rif40_geolevels
 WHERE geography = 'UK91'
 ORDER BY geography, geolevel_id;

SELECT geography, geolevel_name, avg_npoints_geom, avg_npoints_opt, file_geojson_len, leg_geom, leg_opt
  FROM t_rif40_geolevels
 WHERE geography = 'UK91'
 ORDER BY geography, geolevel_id;

\pset pager off
\dS+ t_rif40_uk91_geometry
\dS+ uk91_geography

\echo Created and populated rif40_geolevels_geometry, intersection and lookup tables (Geographic processing)- UK91 version.

-- 
-- Eof
