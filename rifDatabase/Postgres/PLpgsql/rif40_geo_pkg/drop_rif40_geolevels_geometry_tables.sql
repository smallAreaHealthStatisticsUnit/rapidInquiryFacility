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
--								  drop_rif40_geolevels_geometry_tables() function
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

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables()
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 		drop_rif40_geolevels_geometry_tables()
Parameters:		Nothing
Returns:		Nothing
Description:	Drop rif40_geolevels_geometry and rif40_geolevels_maptiles tables
				Supports both old and new forms (partitions changed from t_ to p_ to make more obvious)
 */
DECLARE
	c1 CURSOR FOR /* Partitions */
		SELECT * FROM information_schema.tables 
		 WHERE (table_name LIKE 't_rif40_%_geometry' /* Old */ 
		    OR  table_name LIKE 'p_rif40_%_geometry' /* New */ 
		    OR  table_name LIKE 't_rif40_%_maptiles%' /* Old */ 
		    OR  table_name LIKE 'p_rif40_%_maptiles%' /* New */)
 		   AND table_name NOT IN ('t_rif40_geolevels_geometry', 'p_rif40_geolevels_maptiles');
	c1a CURSOR(l_trigger VARCHAR) FOR
		SELECT * FROM information_schema.triggers 
		 WHERE trigger_name = l_trigger;
	c1b CURSOR(l_routines VARCHAR) FOR
		SELECT * FROM information_schema.routines 
		 WHERE routine_name = l_routines;
--
	c1_rec information_schema.tables%ROWTYPE;
	c1a_rec information_schema.triggers%ROWTYPE;
	c1b_rec information_schema.routines%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_geometry_tables', 'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR /* Username */);
	END IF;
--
-- Drop existing objects
--
	FOR c1_rec IN c1 LOOP
		OPEN c1a(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1a INTO c1a_rec;
		CLOSE c1a;
		IF c1a_rec.trigger_name IS NOT NULL THEN
			sql_stmt:='DROP TRIGGER '||quote_ident(c1_rec.table_name||'_insert')||' ON '||c1_rec.table_name;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		OPEN c1b(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1b INTO c1b_rec;
		CLOSE c1b;
		IF c1b_rec.routine_name IS NOT NULL THEN
			sql_stmt:='DROP FUNCTION '||quote_ident(c1_rec.table_name||'_insert')||'()';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		sql_stmt:='DROP TABLE '||quote_ident(c1_rec.table_name)||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables() IS 'Function: 	drop_rif40_geolevels_geometry_tables()
Parameters:	Nothing
Returns:	Nothing
Description:	Drop rif40_geolevels_geometry tables and rif40_geolevels_maptiles
				Supports both old and new forms (partitions changed from t_ to p_ to make more obvious)';

CREATE OR REPLACE FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(t_geography VARCHAR)
RETURNS void 
SECURITY INVOKER
AS $body$
/*

Function: 	drop_rif40_geolevels_geometry_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels_geometry and rif40_geolevels_maptiles tables
				Supports both old and new forms (partitions changed from t_ to p_ to make more obvious)

 */
DECLARE
	c1 CURSOR(l_table_name VARCHAR) FOR
		SELECT * FROM information_schema.tables 
		 WHERE table_name = quote_ident(l_table_name) 
		   AND table_name NOT IN ('t_rif40_geolevels_geometry', 'p_rif40_geolevels_maptiles');
	c1a CURSOR(l_trigger VARCHAR) FOR
		SELECT * FROM information_schema.triggers 
		 WHERE trigger_name = l_trigger;
	c1b CURSOR(l_routines VARCHAR) FOR
		SELECT * FROM information_schema.routines 
		 WHERE routine_name = l_routines;
--
	c1_rec information_schema.tables%ROWTYPE;
	c1a_rec information_schema.triggers%ROWTYPE;
	c1b_rec information_schema.routines%ROWTYPE;
--
	sql_stmt VARCHAR;
BEGIN
--
-- Must be rif40 or have rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-10001, 'drop_rif40_geolevels_geometry_tables', 
			'User % must be rif40 or have rif_manager role', 
			USER::VARCHAR /* Username */);
	END IF;
--
-- Drop existing objects
--
-- Maptiles - OLD
--
	FOR c1_rec IN c1('t_rif40_'||LOWER(t_geography)||'_maptiles') LOOP
		OPEN c1a(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1a INTO c1a_rec;
		CLOSE c1a;
		IF c1a_rec.trigger_name IS NOT NULL THEN
			sql_stmt:='DROP TRIGGER '||quote_ident(c1_rec.table_name||'_insert')||' ON '||c1_rec.table_name;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		OPEN c1b(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1b INTO c1b_rec;
		CLOSE c1b;
		IF c1b_rec.routine_name IS NOT NULL THEN
			sql_stmt:='DROP FUNCTION '||quote_ident(c1_rec.table_name||'_insert')||'()';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		sql_stmt:='DROP TABLE '||quote_ident(c1_rec.table_name)||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
--
-- Maptiles - NEW
--
	FOR c1_rec IN c1('p_rif40_'||LOWER(t_geography)||'_maptiles') LOOP
		OPEN c1a(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1a INTO c1a_rec;
		CLOSE c1a;
		IF c1a_rec.trigger_name IS NOT NULL THEN
			sql_stmt:='DROP TRIGGER '||quote_ident(c1_rec.table_name||'_insert')||' ON '||c1_rec.table_name;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		OPEN c1b(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1b INTO c1b_rec;
		CLOSE c1b;
		IF c1b_rec.routine_name IS NOT NULL THEN
			sql_stmt:='DROP FUNCTION '||quote_ident(c1_rec.table_name||'_insert')||'()';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		sql_stmt:='DROP TABLE '||quote_ident(c1_rec.table_name)||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
--
-- Geometry - OLD
--
	FOR c1_rec IN c1('t_rif40_'||LOWER(t_geography)||'_geometry') LOOP
		OPEN c1a(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1a INTO c1a_rec;
		CLOSE c1a;
		IF c1a_rec.trigger_name IS NOT NULL THEN
			sql_stmt:='DROP TRIGGER '||quote_ident(c1_rec.table_name||'_insert')||' ON '||c1_rec.table_name;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		OPEN c1b(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1b INTO c1b_rec;
		CLOSE c1b;
		IF c1b_rec.routine_name IS NOT NULL THEN
			sql_stmt:='DROP FUNCTION '||quote_ident(c1_rec.table_name||'_insert')||'()';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		sql_stmt:='DROP TABLE '||quote_ident(c1_rec.table_name)||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
--
-- Geometry - NEW
--
	FOR c1_rec IN c1('p_rif40_'||LOWER(t_geography)||'_geometry') LOOP
		OPEN c1a(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1a INTO c1a_rec;
		CLOSE c1a;
		IF c1a_rec.trigger_name IS NOT NULL THEN
			sql_stmt:='DROP TRIGGER '||quote_ident(c1_rec.table_name||'_insert')||' ON '||c1_rec.table_name;
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		OPEN c1b(quote_ident(c1_rec.table_name||'_insert'));
		FETCH c1b INTO c1b_rec;
		CLOSE c1b;
		IF c1b_rec.routine_name IS NOT NULL THEN
			sql_stmt:='DROP FUNCTION '||quote_ident(c1_rec.table_name||'_insert')||'()';
			PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		END IF;
		sql_stmt:='DROP TABLE '||quote_ident(c1_rec.table_name)||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$body$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_geo_pkg.drop_rif40_geolevels_geometry_tables(VARCHAR) IS 'Function: 	drop_rif40_geolevels_geometry_tables()
Parameters:	Geography
Returns:	Nothing
Description:	Drop rif40_geolevels_geometry and rif40_geolevels_maptiles tables
				Supports both old and new forms (partitions changed from t_ to p_ to make more obvious)';

--
-- Eof