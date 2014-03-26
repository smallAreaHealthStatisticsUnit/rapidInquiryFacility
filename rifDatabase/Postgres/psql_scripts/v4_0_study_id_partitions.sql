-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/27 11:29:40 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_study_id_partitions.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/v4_0_study_id_partitions.sql,v $
-- $Revision: 1.3 $
-- $Id: v4_0_study_id_partitions.sql,v 1.3 2014/02/27 11:29:40 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Partition all tables with study_id as a column
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
-- $Log: v4_0_study_id_partitions.sql,v $
-- Revision 1.3  2014/02/27 11:29:40  peterh
--
-- About to test isolated code tree for trasnfer to Github/public network
--
-- Revision 1.2  2014/02/14 17:18:41  peterh
--
-- Clean build. Issue with ST_simplify(), intersection code and UK geography (to be resolved)
-- Fully commented (and check now works)
--
-- Stubs for range/hash partitioning added
--
-- Revision 1.1  2013/03/14 17:35:39  peterh
-- Baseline for TX to laptop
--
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Partition all tables with study_id as a column...

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT a.tablename AS tablename, b.attname AS columnname, schemaname AS schemaname	/* Tables */
		  FROM pg_tables a, pg_attribute b, pg_class c
		 WHERE c.oid        = b.attrelid
		   AND c.relname    = a.tablename
		   AND c.relkind    = 'r' /* Relational table */
		   AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		   AND b.attname    = 'study_id'
		   AND a.schemaname = 'rif40'
		 ORDER BY 1;
--
	c1_rec RECORD;
--
	sql_stmt VARCHAR[];
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not rif40', user;	
	END IF;
--
	FOR c1_rec IN c1 LOOP
		RAISE INFO 'Hash partitioning: %.%', c1_rec.schemaname, c1_rec.tablename;
		PERFORM rif40_sql_pkg.rif40_hash_partition(c1_rec.schemaname::VARCHAR, c1_rec.tablename::VARCHAR, 'study_id'::VARCHAR);
	END LOOP;
END;
$$;

\echo Partitioning of all tables with study_id as a column complete.
--
-- Eof

