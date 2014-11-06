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
--								  add_population_to_rif40_geolevels_geometry() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables

 */
DECLARE
	c1 CURSOR FOR
		SELECT * FROM information_schema.tables 
		 WHERE table_name IN (SELECT DISTINCT LOWER(lookup_table)
				        FROM t_rif40_geolevels WHERE lookup_table IS NOT NULL
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)
				        FROM rif40_geographies
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)||'_orig'
				        FROM rif40_geographies
				 /*    UNION 		-* NOT shapefile/centroid tables; these are loaded by ../shapefiles/shapefiles.sql and are now GIS objects *-
				      SELECT DISTINCT LOWER(shapefile_table)
				        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
				       UNION 
				      SELECT DISTINCT LOWER(centroids_table)
				        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL */);
--
	c1_rec information_schema.tables%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_lookup_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR 	/* User name */);
	END IF;
----
-- Drop existing objects
--
	FOR c1_rec IN c1 LOOP
		sql_stmt:='DROP TABLE '||quote_ident(LOWER(c1_rec.table_name))||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables() IS 'Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables

 */
DECLARE
	c1 CURSOR(l_geography VARCHAR) FOR
		SELECT * FROM information_schema.tables 
		 WHERE table_name IN (SELECT DISTINCT LOWER(lookup_table)
				        FROM t_rif40_geolevels a
				       WHERE lookup_table IS NOT NULL
				         AND a.geography = l_geography
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)
				        FROM rif40_geographies b
				       WHERE b.geography = l_geography
				       UNION 
				      SELECT DISTINCT LOWER(hierarchytable)||'_orig'
				        FROM rif40_geographies c
				       WHERE c.geography = l_geography
				 /*    UNION 		-* NOT shapefile/centroid tables; these are loaded by ../shapefiles/shapefiles.sql and are now GIS objects *-
				      SELECT DISTINCT LOWER(shapefile_table)
				        FROM t_rif40_geolevels WHERE shapefile_table IS NOT NULL
				       UNION 
				      SELECT DISTINCT LOWER(centroids_table)
				        FROM t_rif40_geolevels WHERE centroids_table IS NOT NULL */);
--
	c1_rec information_schema.tables%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_lookup_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR 	/* User name */);
	END IF;
----
-- Drop existing objects
--
	FOR c1_rec IN c1(geography) LOOP
		sql_stmt:='DROP TABLE '||quote_ident(LOWER(c1_rec.table_name))||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_lookup_tables(VARCHAR) IS 'Function: 	drop_rif40_geolevels_lookup_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels lookup and hierarchy tables';

--
-- Eof