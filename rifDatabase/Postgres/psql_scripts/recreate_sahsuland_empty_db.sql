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
-- Rapid Enquiry Facility (RIF) - Database creation script: SAHSULAND_EMPTY
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

--\set ECHO all
\set ECHO none
\set ON_ERROR_STOP on
\echo Creating SAHSULAND_EMPTY database if required

--
-- check connected as postgres to postgres
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_user != 'postgres' OR current_database() != 'postgres' THEN
		RAISE EXCEPTION 'db_create.sql() current_user: % and current database: % must both be postgres', current_user, current_database();
	END IF;
END;
$$;

DROP DATABASE IF EXISTS sahsuland_empty;
CREATE DATABASE sahsuland_empty WITH OWNER rif40 /* No sahsuland tablespace */;
COMMENT ON DATABASE sahsuland_empty IS 'RIF V4.0 PostGres SAHSULAND Empty Database';
--
-- Eof
