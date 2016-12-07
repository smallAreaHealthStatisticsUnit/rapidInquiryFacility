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
-- Rapid Enquiry Facility (RIF) -- Setup SAHSLAND into rif40_geographies, rif40_geolevels ready for setup by rif40_geo_pkg functions
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
-- Check database is sahsuland_dev or sahsuland_empty
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSIF current_database() = 'sahsuland_empty' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev or sahsuland_empty', current_database();	
	END IF;
END;
$$;

--
-- RIF40_GEOGRAPHIES, T_RIF40_GEOLEVELS
--
UPDATE rif40_geographies 
   SET defaultcomparea = NULL
 WHERE geography = 'SAHSU';
DELETE FROM t_rif40_geolevels WHERE geography = 'SAHSU';
DELETE FROM rif40_geographies WHERE geography = 'SAHSU';

INSERT INTO rif40_geographies (geography, description, hierarchytable, srid)
VALUES ('SAHSU', 'SAHSU example database', 'SAHSULAND_GEOGRAPHY', 27700);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	centroidxcoordinate_column, centroidycoordinate_column,
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance)
VALUES (
	'SAHSU', 'LEVEL1', 1, 'Lowest level of resolution', 'SAHSULAND_LEVEL1', 'NAME',
	NULL, NULL,
	NULL, /* 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_GRD_Level1.shp' */ NULL, NULL, 1,1,1,
	'X_SAHSU_LEVEL1', 'LEVEL1', NULL, 500);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	centroidxcoordinate_column, centroidycoordinate_column,
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance)
VALUES (
	'SAHSU', 'LEVEL2', 2, '2nd level of resolution', 'SAHSULAND_LEVEL2', 'NAME',
	NULL, NULL,
	NULL, /* 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_GRD_Level2.shp' */ NULL, NULL, 1,1,1,
	'X_SAHSU_LEVEL2', 'LEVEL2', 'NAME', 100);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	centroidxcoordinate_column, centroidycoordinate_column,
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance)
VALUES (
	'SAHSU', 'LEVEL3', 3, '3rd level of resolution', 'SAHSULAND_LEVEL3', 'NAME',
	NULL, NULL,
	'SAHSULAND_COVARIATES_LEVEL3', /* 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_GRD_Level3.shp' */ NULL, NULL, 1,1,1,
	'X_SAHSU_LEVEL3', 'LEVEL3', NULL, 50);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance, 
	centroidxcoordinate_column, centroidycoordinate_column, centroids_table)
VALUES (
	'SAHSU', 'LEVEL4', 4, 'Highest level of resolution', 'SAHSULAND_LEVEL4', 'NAME',
	'SAHSULAND_COVARIATES_LEVEL4', /* 'C:\PROGRAM Files\RIF\RIF Shapefiles\SAHSU_GRD_Level4.shp', 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_CEN_Level4.shp' USING TABLE BASED CENTROIDS  */ NULL, NULL ,1,1,1,
	'X_SAHSU_LEVEL4', 'LEVEL4', NULL, 10, 'X_COORDINATE', 'Y_COORDINATE', 'X_SAHSU_CEN_LEVEL4');

UPDATE rif40_geographies 
   SET defaultcomparea  = 'LEVEL2',
       defaultstudyarea = 'LEVEL4'
 WHERE geography = 'SAHSU';

--
-- Eof
