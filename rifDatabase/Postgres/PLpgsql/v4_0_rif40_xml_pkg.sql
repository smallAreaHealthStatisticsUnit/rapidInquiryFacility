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
-- Rapid Enquiry Facility (RIF) - Web services integration functions for middleware
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
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_xml_pkg:
--
-- _rif40_getGeoLevelExtentCommon: 	50000 to 50099
-- rif40_get_geojson_as_js: 		50200 to 50399
-- rif40_get_geojson_tiles: 		50400 to 50599
-- _rif40_get_geojson_as_js: 		50600 to 50799
--
-- Include common code
--
\i ../PLpgsql/rif40_xml_pkg/_rif40_getGeoLevelExtentCommon.sql
\i ../PLpgsql/rif40_xml_pkg/_rif40_get_geojson_as_js.sql

--
-- Include functions
--
\i ../PLpgsql/rif40_xml_pkg/rif40_get_geojson_as_js.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_get_geojson_tiles.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_getGeoLevelFullExtent.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_getGeoLevelFullExtentForStudy.sql
\i ../PLpgsql/rif40_xml_pkg/rif40_getGeoLevelBoundsForArea.sql

COMMENT ON SCHEMA rif40_xml_pkg
  IS 'RIF XML support.

Functions:

Function: 	rif40_get_geojson_tiles()
Parameters:	Geography, geolevel_view, Y max, X max, Y min, X min as a record, return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		Fetch tiles bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Function: 	rif40_get_geojson_as_js()
Parameters:	Geography, geolevel_view, geolevel_area, geolevel_area_id, return one row (TRUE/FALSE)
Returns:	Text table [1 or more rows dependent on return_one_row]
Description:	Get GeoJSON data as a Javascript variable. 
		For the disease mapping selection dialog phrase:
		"View the <geolevel view> (e.g. GOR2001) of <geolevel area> (e.g. London) and select at <geolevel select> (e.g. LADUA2001) level."
		1 row means 1 row, no CRLFs etc!
		If return_one_row is false then a header, 1 rows/area_id and a footer is returned so the Javascript is easier to read

Function: 	rif40_getGeoLevelBoundsForArea()
Parameters:	Geography, geolevel_view, map area id
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view> <map area ID>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Function: 	rif40_getGeoLevelFullExtent()
Parameters:	Geography, geolevel_view
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 

Function: 	rif40_getGeoLevelFullExtentForStudy()
Parameters:	Geography, geolevel_view
Returns:	Y max, X max, Y min, X min as a record. Cast to REAL (6 decimal digits precision)
Description:	Get bounding box Y max, X max, Y min, X min for <geography> <geolevel view> <study>
		SRID is 4326 (WGS84)
		Note: that this is NOT box 2d. Box 2d is defined as a box composed of x min, ymin, xmax, ymax. 
';

--
-- Eof
