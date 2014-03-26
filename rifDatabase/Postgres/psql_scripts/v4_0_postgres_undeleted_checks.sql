-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2013/09/02 14:08:33 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_postgres_undeleted_checks.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/v4_0_postgres_undeleted_checks.sql,v $
-- $Revision: 1.2 $
-- $Id: v4_0_postgres_undeleted_checks.sql,v 1.2 2013/09/02 14:08:33 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Check all tables/views/FDW tables/temporary tables/sequences/function/triggers deleted
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
-- $Log: v4_0_postgres_undeleted_checks.sql,v $
-- Revision 1.2  2013/09/02 14:08:33  peterh
--
-- Baseline after full trigger implmentation
--
-- Revision 1.1  2013/03/14 17:35:39  peterh
-- Baseline for TX to laptop
--
--
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
