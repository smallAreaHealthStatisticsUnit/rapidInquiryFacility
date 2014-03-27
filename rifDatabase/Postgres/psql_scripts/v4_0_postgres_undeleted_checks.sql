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
-- Rapid Enquiry Facility (RIF) - Check all tables/views/FDW tables/temporary tables/sequences/function/triggers deleted
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

\echo Checking all tables/views/FDW tables/temporary tables/sequences/function/triggers deleted...
\set ECHO all
\set ON_ERROR_STOP OFF
\dS+ g_rif40_comparison_areas
\dS+ g_rif40_study_areas

\set ON_ERROR_STOP ON

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT 'temporary table' object_type, r.rolname||'.'||a.relname object_name 				/* Temporary tables */
 		 FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'r' 										/* Relational table */
		   AND a.relpersistence = 't' 										/* Persistence: temporary */
		   AND a.relowner       = r.oid
		   AND n.nspname        LIKE 'pg_temp%'
		   AND r.rolname	= USER
		UNION
		SELECT 'FDW table' object_type, n.nspname||'.'||a.relname object_name		/* FDW tables */
		  FROM pg_foreign_table b, pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE b.ftrelid = a.oid
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND COALESCE(n.nspname, r.rolname) = USER
		UNION
		SELECT 'local table' object_type, tablename object_name							/* Local tables */
		  FROM pg_tables t, pg_class c
		 WHERE t.tableowner = USER
		   AND t.schemaname = USER
		   AND c.relowner   = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND c.relname    = t.tablename
		   AND c.relkind    = 'r' 										/* Relational table */
		   AND c.relpersistence IN ('p', 'u') 									/* Persistence: permanent/unlogged */
		UNION
		SELECT 'view' object_type, viewname object_name								/* Local views */
		  FROM pg_views
		 WHERE viewowner = USER
		UNION
		SELECT 'trigger' object_type, COALESCE(n.nspname, r.rolname)||'.'||a.relname||'.'||tgname object_name	/* Triggers */
		  FROM pg_roles r, pg_trigger b, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE tgrelid = a.oid
		   AND relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		UNION
		SELECT 'sequence' object_type, COALESCE(n.nspname, r.rolname)||'.'||a.relname object_name		/* Sequences */
		  FROM pg_roles r, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relkind = 'S' 
		   AND a.relowner = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relowner = r.oid
		   AND COALESCE(n.nspname, r.rolname) = USER
		UNION
		SELECT l.lanname||' function' object_type, 
			COALESCE(n.nspname, r.rolname)||'.'||p.proname object_name 					/* Functions */
		  FROM pg_language l, pg_type t, pg_roles r, pg_proc p
			LEFT OUTER JOIN pg_namespace n ON (n.oid = p.pronamespace)			
		 WHERE p.prolang    = l.oid
		   AND p.prorettype = t.oid
		   AND p.proowner   = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND p.proowner   = r.oid
		 ORDER BY 1, 2;
--
	c1_rec RECORD;
--
	i INTEGER:=0;
BEGIN
--
	FOR c1_rec IN c1 LOOP
		IF c1_rec.object_type = 'temporary table' THEN
			RAISE NOTICE 'Found undeleted %: %', c1_rec.object_type, c1_rec.object_name;
		ELSE
			RAISE WARNING 'Found undeleted %: %', c1_rec.object_type, c1_rec.object_name;
			i:=i+1;
		END IF;
	END LOOP;
--
	IF i > 0 THEN
		RAISE EXCEPTION 'C209xx: % undeleted tables/views/FDW tables/temporary tables/sequences/function/triggers found', i
			USING HINT='See previous warnings for details';
	ELSE
		RAISE INFO 'C209xx: All tables/views/FDW tables/temporary tables/sequences/function/triggers deleted';
	END IF;
END;
$$;

\echo Checked all tables/views/FDW tables/temporary tables/sequences/function/triggers deleted.
--
-- Eof
