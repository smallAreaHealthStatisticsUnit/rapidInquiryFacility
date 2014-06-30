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
--     				  rif40_GetGeometryColumnNames
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
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_xml_pkg:
--
-- rif40_GetGeometryColumnNames: 		51000 to 51199
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

CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_GetGeometryColumnNames(
	l_geography 		VARCHAR)
RETURNS TABLE(
		column_name		VARCHAR, 
		column_description	VARCHAR)
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_GetGeometryColumnNames()
Parameters:	Geography
Returns:	Table: column_name, column_description
Description:	Get all the SRID 4326 (WGS84) geometry column names for geography

SELECT *
  FROM rif40_xml_pkg.rif40_GetGeometryColumnNames('SAHSU');
psql:alter_scripts/v4_0_alter_2.sql:480: INFO:  [DEBUG1] rif40_GetGeometryColumnNames(): [51004] Geography: SAHSU; SQL fetch returned 1 rows, took: 00:00:00.016.
    column_name     |
                                                                                                                                                                 column_description


--------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------------
 optimised_geometry | Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only). Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determ
ines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points
. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be p
rocessed independently and not necessarily in the same manner. This is fixed using the PostGIS Topology extension and processing as edges.
(1 row)

 */
DECLARE
	c1getgeocols CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography     = l_geography;
--
	c1_rec RECORD;
--

	i		INTEGER;
	stp 		TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp 		TIMESTAMP WITH TIME ZONE;
	took 		INTERVAL;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- User must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-51000, 'rif40_GetGeometryColumnNames', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51001, 'rif40_GetGeometryColumnNames', 'NULL geography parameter');
	END IF;	
--
	OPEN c1getgeocols(l_geography);
	FETCH c1getgeocols INTO c1_rec;
	CLOSE c1getgeocols;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51002, 'rif40_GetGeometryColumnNames', 'geography: % not found', 
			l_geography::VARCHAR		/* Geography */);
	END IF;	
--
-- Get all the SRID 4326 (WGS84) geometry column names for geography
--
	BEGIN
		RETURN QUERY
			SELECT b.column_name::VARCHAR,
			       COALESCE(col_description((quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry'))::regclass /* Obj id */, 
					b.ordinal_position /* Column number */), '[Column not found]')::VARCHAR AS column_description
				  FROM geometry_columns a, information_schema.columns b 
					 WHERE a.f_table_name      = b.table_name
			    		   AND a.f_geometry_column = b.column_name
					   AND a.srid              = 4326 /* WGS84 */
					   AND b.table_name        = quote_ident('t_rif40_'||LOWER(l_geography)||'_geometry')
				 ORDER BY 1;
		GET DIAGNOSTICS i = ROW_COUNT;
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_GetGeometryColumnNames() caught: '||E'\n'||
				SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
			RAISE INFO '51003: %', error_message;
--
			RAISE;
	END;
--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_GetGeometryColumnNames', 
		'[51004] Geography: %; SQL fetch returned % rows, took: %.', 			
		l_geography::VARCHAR			/* Geography */, 
		i::VARCHAR				/* Actual */,
		took::VARCHAR				/* Time taken */);
--
	RETURN;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_GetGeometryColumnNames(VARCHAR) IS 'Function: 	rif40_GetGeometryColumnNames()
Parameters:	Geography
Returns:	Table: column_name, column_description
Description:	Get all the SRID 4326 (WGS84) geometry column names for geography

SELECT *
  FROM rif40_xml_pkg.rif40_GetGeometryColumnNames(''SAHSU'');
psql:alter_scripts/v4_0_alter_2.sql:480: INFO:  [DEBUG1] rif40_GetGeometryColumnNames(): [51004] Geography: SAHSU; SQL fetch returned 1 rows, took: 00:00:00.016.
    column_name     |
                                                                                                                                                                 column_description


--------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------------
 optimised_geometry | Optimised spatial data for geolevel in SRID 4326 [WGS84] (PostGress/PostGIS only). Can also use SHAPEFILE instead. RIF40_GEOGRAPHIES.MAX_GEOJSON_DIGITS determ
ines the number of digits in the GeoJSON output and RIF40_GEOLEVELS.ST_SIMPLIFY_TOLERANCE determines the minimum distance (in metres for most projections) between simplified points
. Will contain small slivers and overlaps due to limitation in the Douglas-Peucker algorithm (it works onj an object by object basis; the edge between two areas will therefore be p
rocessed independently and not necessarily in the same manner. This is fixed using the PostGIS Topology extension and processing as edges.
(1 row)';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetGeometryColumnNames(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetGeometryColumnNames(VARCHAR) TO rif_user;

--
-- Eof
