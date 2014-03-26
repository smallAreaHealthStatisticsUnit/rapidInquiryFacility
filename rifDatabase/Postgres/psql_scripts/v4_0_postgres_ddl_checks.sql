-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/01/14 08:59:48 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_postgres_ddl_checks.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/psql_scripts/v4_0_postgres_ddl_checks.sql,v $
-- $Revision: 1.3 $
-- $Id: v4_0_postgres_ddl_checks.sql,v 1.3 2014/01/14 08:59:48 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Check all tables, triggers, columns and comments are present, 
--				  objects granted to rif_user/rif_manmger, sequences granted.
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
-- $Log: v4_0_postgres_ddl_checks.sql,v $
-- Revision 1.3  2014/01/14 08:59:48  peterh
--
-- Baseline prior to adding multipolygon support for simplification
--
-- Revision 1.2  2013/03/14 17:35:20  peterh
-- Baseline for TX to laptop
--
--
-- Triggers are INFO
-- 
\echo Checking all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted...
\set ECHO all
\set ON_ERROR_STOP ON

\set VERBOSITY terse
SHOW search_path;
DO LANGUAGE plpgsql $$
BEGIN
        PERFORM rif40_sql_pkg.rif40_ddl_checks();
END;
$$;
\set VERBOSITY default

\echo Checked all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted.
--
-- Eof
