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
-- Rapid Enquiry Facility (RIF) - Insert SAHSUland geospatial data
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

--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Load SAHSULAND shapefiles - now moved to GIS schema in ../shapefiles; created using ../shapefiles/build_shapefile.sh and shp2pgsql...

\set ON_ERROR_STOP ON

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

CREATE TABLE gis.sahsu_grd_level1 (
	wkt 	VARCHAR,
	id		int4,
	level1 	varchar(5),
	area 	numeric);
CREATE TABLE gis.x_sahsu_level1 (
	gid serial,
	id 		int4,
	level1 	varchar(5),
	area 	numeric);
ALTER TABLE gis.x_sahsu_level1 ADD PRIMARY KEY (gid);
SELECT AddGeometryColumn('gis','x_sahsu_level1','geom','27700'/* UK */,'MULTIPOLYGON',2);

\COPY gis.sahsu_grd_level1(wkt, id, level1, area) FROM  '../../GeospatialData/SAHSULAND/sahsu_grd_level1.csv' WITH (FORMAT csv, QUOTE '"', ESCAPE '\', HEADER);
--
-- For editor's benefit
--
INSERT INTO x_sahsu_level1(id, level1, area, geom) SELECT id, level1, area, ST_GeomFromText(wkt, 27700) AS geom FROM sahsu_grd_level1;
CREATE INDEX x_sahsu_level1_geom_gist ON gis.x_sahsu_level1 USING GIST (geom);

SELECT level1, area, ST_area(geom) AS n_area, ST_area(geom)/(1000*1000) AS n_area_km2, area-ST_area(geom) AS area_diff, ST_IsValid(geom) AS valid 
  FROM gis.x_sahsu_level1;

\set ECHO OFF

--
-- Created using: ../shapefiles/build_shapefile.sh
--

--\i ../shapefiles/x_sahsu_level1.sql  
--\i ../shapefiles/x_sahsu_level2.sql  
--\i ../shapefiles/x_sahsu_level3.sql  
--\i ../shapefiles/x_sahsu_level4.sql

--\i ../shapefiles/x_sahsu_cen_level4.sql 
 
\set ECHO all
DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Test STOP';
END;
$$;

\echo Loaded SAHSULAND shapefiles - now moved to GIS schema in ../shapefiles; created using ../shapefiles/build_shapefile.sh and shp2pgsql.
--
-- Eof
