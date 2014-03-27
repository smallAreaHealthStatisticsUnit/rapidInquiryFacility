-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/28 10:54:08 $
-- Type: Postgres PSQL script
-- $RCSfile: sahsland_shapefiles.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/shapefiles/sahsland_shapefiles.sql,v $
-- $Revision: 1.2 $
-- $Id: sahsland_shapefiles.sql,v 1.2 2014/02/28 10:54:08 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load shapefiles - now moved to GIS schema
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
-- $Log: sahsland_shapefiles.sql,v $
-- Revision 1.2  2014/02/28 10:54:08  peterh
--
-- Further work on transfer of SAHSUland to github. sahsuland build scripts Ok, UK91 geog added.
--
-- Revision 1.1  2013/03/14 17:39:46  peterh
-- Baseline for TX to laptop
--
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Load SAHSULAND shapefiles - now moved to GIS schema in ../shapefiles; created using ../shapefiles/build_shapefile.sh and shp2pgsql...

--
-- Check user is gis
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not rif40', user;	
	END IF;
END;
$$;
\set ON_ERROR_STOP OFF

DROP TABLE gis.x_sahsu_cen_level4;
DROP TABLE gis.x_sahsu_level1;
DROP TABLE gis.x_sahsu_level2;
DROP TABLE gis.x_sahsu_level3;
DROP TABLE gis.x_sahsu_level4;

\set ON_ERROR_STOP ON
\set ECHO OFF

--
-- Created using: ../shapefiles/build_shapefile.sh
--

\i ../shapefiles/x_sahsu_level1.sql  
\i ../shapefiles/x_sahsu_level2.sql  
\i ../shapefiles/x_sahsu_level3.sql  
\i ../shapefiles/x_sahsu_level4.sql

\i ../shapefiles/x_sahsu_cen_level4.sql 
 
\set ECHO all

\echo Loaded SAHSULAND shapefiles - now moved to GIS schema in ../shapefiles; created using ../shapefiles/build_shapefile.sh and shp2pgsql.
--
-- Eof
