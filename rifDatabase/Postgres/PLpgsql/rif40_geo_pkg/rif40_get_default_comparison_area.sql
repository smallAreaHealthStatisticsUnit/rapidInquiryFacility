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
--								  rif40_get_default_comparison_area() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(
	l_geography VARCHAR, geolevel_select VARCHAR, geolevel_selection VARCHAR[])
RETURNS VARCHAR[] 
SECURITY INVOKER
AS $body$
/*

Function: 	rif40_get_default_comparison_area()
Parameters:	Geography, geolevel_select, geolevel_selection ARRAY
Returns:	Array of area IDs of default comparison area
Description:	Get default comparison area

1. Get the default comparison area (defaultcomparea) and hierarchy table name <hierarchytable> from RIF40_GEOGRPAHIES
2. Returns DISTINCT <defaultcomparea> FROM <hierarchytable> WHERE <geolevel_select> IN (SELECT unnest(<geolevel_selection>))
   i.e. all areas at the default comparison area level covered by the users selected geolevels

Test statement:

SELECT array_agg(DISTINCT level2) AS comparison_area
  FROM sahsuland_geography 
 WHERE level2 IN (
	SELECT unnest(level2_array)
	  FROM (
		SELECT array_agg(level2) AS level2_array FROM sahsuland_level2) a);

 */
DECLARE
	c1gdca CURSOR(l_geography VARCHAR) FOR
		SELECT defaultcomparea, hierarchytable
		  FROM rif40_geographies a
		 WHERE a.geography = l_geography;
	c2gdca		REFCURSOR;	
	c1gdca_rec 	RECORD;
	c2gdca_rec 	RECORD;
--
	sql_stmt 	VARCHAR;
--
	error_message 	VARCHAR;
	v_detail 	VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Get comparison geolevel - is the default set in rif40_geographies
--
	OPEN c1gdca(l_geography);
	FETCH c1gdca INTO c1gdca_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-10013, 'rif40_get_default_comparison_area', 
			'Cannot find rif40_geographies geography: %',
			l_geography::VARCHAR			/* Geography */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_get_default_comparison_area', 
			'Default comparision area for geography: % is: %',
			l_geography::VARCHAR			/* Geography */,
			c1gdca_rec.defaultcomparea::VARCHAR	/* Default comparison area defined for geography */);
	END IF;
	CLOSE c1gdca;
--
	sql_stmt:='SELECT array_agg(DISTINCT '||quote_ident(LOWER(c1gdca_rec.defaultcomparea))||') AS comparision_area /* defaultcomparea */'||E'\n'||
'  FROM '||quote_ident(LOWER(c1gdca_rec.hierarchytable))||' /* hierarchytable */'||E'\n'||
' WHERE '||quote_ident(LOWER(geolevel_select))||' /* <Gelevel select> */ IN ('||E'\n'||
'	SELECT unnest($1))';
--
-- Execute SQL statement, returning comparison area
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_get_default_comparison_area', 'SQL> %;', sql_stmt::VARCHAR);
	OPEN c2gdca FOR EXECUTE sql_stmt USING geolevel_selection;
	FETCH c2gdca INTO c2gdca_rec;
	CLOSE c2gdca;
--
	RETURN c2gdca_rec.comparision_area;
EXCEPTION
	WHEN others THEN
-- 
-- Not supported until 9.2
--
--		IF v_version THEN
--			GET STACKED DIAGNOTICS v_detail = PG_EXCETION_DETAIL;
--		END IF;
		error_message:='rif40_get_default_comparison_area() caught: '||E'\n'||SQLERRM::VARCHAR||' in SQL (see previous trapped error)'||E'\n'||'Detail: '||v_detail::VARCHAR;
		RAISE INFO '1: %', error_message;
--
		RAISE;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.rif40_get_default_comparison_area(VARCHAR, VARCHAR, VARCHAR[]) IS 'Function: 	rif40_get_default_comparison_area()
Parameters:	Geography, geolevel_select, geolevel_selection ARRAY
Returns:	Array of area IDs of default comparison area
Description:	Get default comparison area

1. Get the default comparison area (defaultcomparea) and hierarchy table name <hierarchytable> from RIF40_GEOGRPAHIES
2. Returns DISTINCT <defaultcomparea> FROM <hierarchytable> WHERE <geolevel_select> IN (SELECT unnest(<geolevel_selection>))
   i.e. all areas at the default comparison area level covered by the users selected geolevels

Test statement:

SELECT array_agg(DISTINCT level2) AS comparision_area /* defaultcomparea */
  FROM sahsuland_geography /* hierarchytable */
 WHERE level2 /* <Gelevel select> */ IN (
	SELECT unnest(level2_array)
	  FROM (
		SELECT array_agg(level2) AS level2_array FROM sahsuland_level2) a /* Geolevel selection array */);';

--
-- Eof