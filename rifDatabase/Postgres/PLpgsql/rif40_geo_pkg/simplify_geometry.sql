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
--								  simplify_geometry() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.simplify_geometry(
	l_geography VARCHAR, l_min_point_resolution NUMERIC DEFAULT 1 /* metre */)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*
Function: 	simplify_geometry()
Parameters:	Geography, 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry), 
                override for rif40_geoelvels.st_simplify_tolerance
Returns:	Nothing
Description:	Simplify geography geometry. Carried out in three phases:

Phase III: Create the polygons table, Update spatial geolevel table
 */
	c1_sg	CURSOR FOR
		SELECT * 
		  FROM t_rif40_geolevels
		 WHERE geography = l_geography;
	c1_rec RECORD;
--
	sql_stmt	VARCHAR[];
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
BEGIN
--
	stp:=clock_timestamp();
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'simplify_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
	FOR c1_rec IN c1_sg LOOP
		PERFORM rif40_geo_pkg.simplify_geometry(c1_rec.geography, c1_rec.geolevel_name, 
			NULL /* Filter */, 
			l_min_point_resolution /* minimum point resolution (default 1 - assumed metre, but depends on the geometry) */,
			c1_rec.st_simplify_tolerance /* Default for geolevel */);
	END LOOP;
--
-- Drop temporary tables
--
	sql_stmt[1]:='DROP TABLE IF EXISTS simplification_lines';
	sql_stmt[2]:='DROP TABLE IF EXISTS simplification_points';
	sql_stmt[3]:='DROP TABLE IF EXISTS simplification_polygons';
	sql_stmt[4]:='DROP TABLE IF EXISTS simplification_lines_join_duplicates';
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'simplify_geometry', 'Simplified % geography in: %', 
		l_geography::VARCHAR, 
		took::VARCHAR);
--
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, NUMERIC) IS 'Function: 	simplify_geometry()
Parameters:	Geography, 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry), 
                override for rif40_geolevels.st_simplify_tolerance
Returns:	Nothing
Description:	Simplify geography geometry. Carried out in three phases:

Phase III: Create the polygons table, Update spatial geolevel table';


CREATE OR REPLACE FUNCTION rif40_geo_pkg.simplify_geometry(
	l_geography VARCHAR, l_geolevel VARCHAR, l_filter VARCHAR DEFAULT NULL, 
	l_min_point_resolution NUMERIC DEFAULT 1 /* metre */, l_st_simplify_tolerance NUMERIC DEFAULT 1)
RETURNS void 
SECURITY INVOKER
AS $body$
DECLARE
/*
 */
	sql_stmt	VARCHAR[];
--
	stp TIMESTAMP WITH TIME ZONE;
	etp TIMESTAMP WITH TIME ZONE;
	took INTERVAL;
--
	error_message 	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
	stp:=clock_timestamp();
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'simplify_geometry', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Phase I: Create the points table
--
-- Create a sequence so that points are in the original order specified by ST_DumpPoints()
-- Convert geometry to points, reduce precision to 1m
-- Test 1 - check last_pointflow = one per area (should be zero)
-- Detect and classify duplcates within co-ordinates (a side effect of the round)
--
-- O: original - first or last point in area
-- N: Not a dupicate
-- Y: A dupicate (two or more consecutive points sharing the same co-ordinate); removed later
-- L: Loop duplicate (two or more NON consecutive points sharing the same co-ordinate)
--
-- Count up number of distinct area_id using the same co-ordinate
-- Test 2 for un-detected duplicates
-- Join up adjacent edges
-- Create the points table
-- Test 3 and 4 - Checks - MUST BE 0
--	
-- a) Areas with no join sequence
-- b) Areas with mis-joined sequences
--
-- Create a list of joined_area_ids (i.e. the num_join_seq is 1)
-- Update from list of joined_area_ids (i.e. the num_join_seq is 1)
--
	PERFORM rif40_geo_pkg._simplify_geometry_phase_I(l_geography, l_geolevel, l_filter, l_min_point_resolution, l_st_simplify_tolerance);
--
-- Phase II: Create the lines table
--
	PERFORM rif40_geo_pkg._simplify_geometry_phase_II(l_geography, l_geolevel, l_filter, l_min_point_resolution, l_st_simplify_tolerance);
--
-- Phase III: Create the polygons table
--
	PERFORM rif40_geo_pkg._simplify_geometry_phase_III(l_geography, l_geolevel, l_filter, l_min_point_resolution, l_st_simplify_tolerance);
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'simplify_geometry', 'Simplified % geography geolevel %: %', 
		l_geography::VARCHAR, 
		l_geolevel::VARCHAR, 
		took::VARCHAR);
--
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
--		IF v_version THEN
--			GET STACKED DIAGNOTICS v_detail = PG_EXCETION_DETAIL;
--		END IF;
		error_message:='simplify_geometry('||l_geography::VARCHAR||', '||l_geolevel::VARCHAR||') caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
		RAISE;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.simplify_geometry(VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC) IS 'Function: 	simplify_geometry()
Parameters:	Geography, geolevel, 
                geolevel; filter (for testing, no default), 
                minimum point resolution (default 1 - assumed metre, but depends on the geometry), 
                override for rif40+geoelvelsl_st_simplify_tolerance
Returns:	Nothing
Description:	Simplify geography geolevel';

--
-- Eof