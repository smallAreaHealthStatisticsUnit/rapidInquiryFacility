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
-- Rapid Enquiry Facility (RIF) - Create and populate rif40_geolevels_geometry, intersection and lookup tables (Geographic processing) - SAHSULAND version
--
-- Copyright:
--
-- The RIF is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free
-- Software Foundation; either version 2, or (at your option) any later
-- version.
--
-- The RIF is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or
-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
-- for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this file; see the file LICENCE.  If not, write to:
--
-- UK Small Area Health Statistics Unit,
-- Dept. of Epidemiology and Biostatistics
-- Imperial College School of Medicine (St. Mary's Campus),
-- Norfolk Place,
-- Paddington,
-- London, W2 1PG
-- United Kingdom
--
-- The RIF uses Oracle 11g, PL/SQL, PostGres and PostGIS as part of its implementation.
--
-- Oracle11g, PL/SQL and Pro*C are trademarks of the Oracle Corporation.
--
-- All terms mentioned in this software and supporting documentation that are known to be trademarks
-- or service marks have been appropriately capitalised. Imperial College cannot attest to the accuracy
-- of this information. The use of a term in this software or supporting documentation should NOT be
-- regarded as affecting the validity of any trademark or service mark.
--
-- Summary of functions/procedures:
--
-- To be added
--
-- Error handling strategy:
--
-- Output and logging procedures do not HANDLE or PROPAGATE errors. This makes them safe to use
-- in package initialisation and NON recursive.
--
-- References:
--
-- 	None
--
-- Dependencies:
--
--	Packages: None
--
-- 	<This should include: packages, non packages procedures and functions, tables, views, objects>
--
-- Portability:
--
--	Linux, Windows 2003/2008, Oracle 11gR1
--
-- Limitations:
--
-- Change log:
--
-- $Log: rif40_geolevels_ew01_geometry.sql,v $
-- Revision 1.3  2014/02/28 10:54:02  peterh
--
-- Further work on transfer of SAHSUland to github. sahsuland build scripts Ok, UK91 geog added.
--
-- Revision 1.2  2014/02/14 17:18:40  peterh
--
-- Clean build. Issue with ST_simplify(), intersection code and UK geography (to be resolved)
-- Fully commented (and check now works)
--
-- Stubs for range/hash partitioning added
--
-- Revision 1.1  2014/01/14 08:59:47  peterh
--
-- Baseline prior to adding multipolygon support for simplification
--
-- Revision 1.1  2013/03/14 17:35:20  peterh
-- Baseline for TX to laptop
--
--
\echo Create and populate rif40_geolevels_geometry, intersection and lookup tables (Geographic processing)- EW01 version...
--

\set ECHO all
\timing
\set ON_ERROR_STOP OFF
\pset pager on
--
SELECT rif40_geo_pkg.drop_rif40_geolevels_geometry_tables('EW01');
SELECT rif40_geo_pkg.drop_rif40_geolevels_lookup_tables('EW01');
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
	PERFORM rif40_geo_pkg.create_rif40_geolevels_geometry_tables('EW01');
--
-- Create and populate rif40_geolevels lookup and create hierarchy tables 
--
	PERFORM rif40_geo_pkg.create_rif40_geolevels_lookup_tables('EW01');
--
-- Populate geometry and hierarchy tables
--
	PERFORM rif40_geo_pkg.populate_rif40_geometry_tables('EW01');
	PERFORM rif40_geo_pkg.populate_hierarchy_table('EW01'); 
--
-- Add denominator population table to geography geolevel geometry data
--
--	IF current_database() = 'rif40' THEN
--		PERFORM rif40_geo_pkg.add_population_to_rif40_geolevels_geometry('EW01', 'V_EW01_RIF_POP_ASG_1_OA2001'); 
--	END IF;
--
-- Fix NULL geolevel names in geography geolevel geometry and lookup table data 
--
	PERFORM rif40_geo_pkg.fix_null_geolevel_names('EW01'); 
--
-- Simplify geometry
--
--	PERFORM rif40_geo_pkg.simplify_geometry('EW01', 10 /* l_min_point_resolution [1] */);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	RAISE INFO 'Processed EW01 geography: %s', took;
--
END;
$$;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_ew01_geometry
 WHERE geolevel_name = 'SCNTRY2001';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_ew01_geometry
 WHERE geolevel_name = 'CNTRY2001';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_ew01_geometry
 WHERE geolevel_name = 'GOR2001';

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_ew01_geometry
 WHERE geolevel_name = 'LADUA2001'
 ORDER BY 2 LIMIT 20;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_ew01_geometry
 WHERE geolevel_name = 'WARD2001'
 ORDER BY 2 LIMIT 20;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_ew01_geometry
 WHERE geolevel_name = 'SOA2001'
 ORDER BY 2 LIMIT 20;

SELECT area_id, name, area, total_males, total_females, population_year
  FROM t_rif40_ew01_geometry
 WHERE geolevel_name = 'OA2001'
 ORDER BY 2 LIMIT 20;

--
-- Make columns NOT NULL - Cannot be done with PL/pgsql - causes:
-- cannot ALTER TABLE "sahsuland_level1" because it is being used by active queries in this session
--
ALTER TABLE "ew2001_scntry2001" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "ew2001_cntry2001" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "ew2001_gor2001" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "ew2001_ladua2001" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "ew2001_ward2001" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "ew2001_soa2001" ALTER COLUMN name  SET NOT NULL;
ALTER TABLE "ew2001_coa2001" ALTER COLUMN name  SET NOT NULL;

--
-- Vaccum ANALYZE tables
--
VACUUM (ANALYZE, VERBOSE) ew2001_geography;
VACUUM (ANALYZE, VERBOSE) t_rif40_ew01_geometry;
VACUUM (ANALYZE, VERBOSE) ew2001_scntry2001;
VACUUM (ANALYZE, VERBOSE) ew2001_cntry2001;
VACUUM (ANALYZE, VERBOSE) ew2001_gor2001;
VACUUM (ANALYZE, VERBOSE) ew2001_ladua2001;
VACUUM (ANALYZE, VERBOSE) ew2001_ward2001;
VACUUM (ANALYZE, VERBOSE) ew2001_soa2001;
VACUUM (ANALYZE, VERBOSE) ew2001_coa2001;

CREATE TABLE ew2001_geography_orig AS SELECT * FROM ew2001_geography;
TRUNCATE TABLE ew2001_geography_orig;
--
-- Import geospatial intersction files
--
\COPY ew2001_geography_orig(scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001) FROM  '../sahsuland/data/ew2001_geography.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\');
CREATE UNIQUE INDEX ew2001_geography_orig_pk ON ew2001_geography_orig(oa2001);
VACUUM (ANALYZE, VERBOSE) ew2001_geography_orig;

--
-- For vi's benefit

--
-- This algorithm implments the prototype in: ../postgres/gis_intersection_prototype.sql. The SAHSU alogoritm differs in that
-- Where intersections overlap (e.g. level4 appears in more than 1 level3) the level3 with the greatest intersected area is chosen 
-- This is differed to ARCGis - see the prototype file for a more detailed investigation
--
WITH a_missing AS (
	SELECT scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001
	  FROM ew2001_geography
	EXCEPT
	SELECT scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001
	  FROM ew2001_geography_orig
),
a_extra AS (
	SELECT scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001
	  FROM ew2001_geography_orig
	EXCEPT
	SELECT scntry2001, cntry2001, gor2001, ladua2001, ward2001, soa2001, oa2001
	  FROM ew2001_geography
), b AS (
	SELECT a_missing.oa2001, 
               a_missing.soa2001 AS missing_soa2001, 
               a_extra.soa2001 AS extra_soa2001, 
               a_missing.ward2001 AS missing_ward2001, 
               a_extra.ward2001 AS extra_ward2001, 
               a_missing.ladua2001 AS missing_ladua2001, 
               a_extra.ladua2001 AS extra_ladua2001, 
               a_missing.gor2001 AS missing_gor2001, 
               a_extra.gor2001 AS extra_gor2001, 
               a_missing.cntry2001 AS missing_cntry2001, 
               a_extra.cntry2001 AS extra_cntry2001, 
               a_missing.scntry2001 AS missing_scntry2001, 
               a_extra.scntry2001 AS extra_scntry2001 
	  FROM a_missing
		LEFT OUTER JOIN a_extra ON (a_extra.oa2001 = a_missing.oa2001)
)
SELECT oa2001,
       CASE WHEN missing_soa2001 = extra_soa2001 THEN 'Same: '||extra_soa2001 ELSE 'Move: '||extra_soa2001||'=>'||missing_soa2001 END AS soa2001,
       CASE WHEN missing_ward2001 = extra_ward2001 THEN 'Same: '||extra_ward2001 ELSE 'Move: '||extra_ward2001||'=>'||missing_ward2001 END AS ward2001,
       CASE WHEN missing_ladua2001 = extra_ladua2001 THEN 'Same: '||extra_ladua2001 ELSE 'Move: '||extra_ladua2001||'=>'||missing_ladua2001 END AS ladua2001,
       CASE WHEN missing_gor2001 = extra_gor2001 THEN 'Same: '||extra_gor2001 ELSE 'Move: '||extra_gor2001||'=>'||missing_gor2001 END AS gor2001,
       CASE WHEN missing_cntry2001 = extra_cntry2001 THEN 'Same: '||extra_cntry2001 ELSE 'Move: '||extra_cntry2001||'=>'||missing_cntry2001 END AS cntry2001,
       CASE WHEN missing_scntry2001 = extra_scntry2001 THEN 'Same: '||extra_scntry2001 ELSE 'Move: '||extra_scntry2001||'=>'||missing_scntry2001 END AS scntry2001
  FROM b
ORDER BY 1, 2, 3, 4, 5, 6;

DROP TABLE IF EXISTS ew2001_geography_orig;

SELECT geography, geolevel_name, geolevel_id, shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance
  FROM t_rif40_geolevels
 WHERE geography = 'EW01'
 ORDER BY geography, geolevel_id;

SELECT geography, geolevel_name, avg_npoints_geom, avg_npoints_opt, file_geojson_len, leg_geom, leg_opt
  FROM t_rif40_geolevels
 WHERE geography = 'EW01'
 ORDER BY geography, geolevel_id;

\pset pager off
\dS+ t_rif40_ew01_geometry
\dS+ ew2001_geography

\echo Created and populated rif40_geolevels_geometry, intersection and lookup tables (Geographic processing)- EW01 version.

-- 
-- Eof

