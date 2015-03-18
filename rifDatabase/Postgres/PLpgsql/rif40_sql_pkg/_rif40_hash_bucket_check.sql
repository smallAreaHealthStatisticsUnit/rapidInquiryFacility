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
-- Rapid Enquiry Facility (RIF) - Common partitioning functions
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
-- CHECK constraint functions for partition elimination. Do not work - suspect IMMUTABLE functions not supported in C
-- All apart from INTEGER are commented out. Usage:
--
-- CREATE TABLE rif40_study_shares_p15 (
-- CONSTRAINT rif40_study_shares_p15_ck CHECK (hash_partition_number = 15 /* bucket requested */)
-- ) INHERITS (rif40_study_shares);
--
--CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_hash_bucket_check(l_value VARCHAR, l_bucket INTEGER, l_bucket_requested INTEGER)
--RETURNS VARCHAR
--AS 'SELECT CASE WHEN l_bucket_requested IS NULL THEN NULL WHEN l_bucket_requested = (ABS(hashtext(l_value))%l_bucket)+1 THEN l_value ELSE NULL END;' LANGUAGE sql IMMUTABLE STRICT;
CREATE OR REPLACE FUNCTION rif40_sql_pkg._rif40_hash_bucket_check(l_value INTEGER, l_bucket INTEGER, l_bucket_requested INTEGER)
RETURNS INTEGER
AS 'SELECT CASE WHEN l_bucket_requested IS NULL THEN NULL WHEN l_bucket_requested = (ABS(hashtext(l_value::TEXT))%l_bucket)+1 THEN l_value ELSE NULL END;' LANGUAGE sql IMMUTABLE STRICT;

--COMMENT ON FUNCTION rif40_sql_pkg._rif40_hash_bucket_check(VARCHAR, INTEGER, INTEGER) IS 'Function: 	_rif40_hash()
--Parameters:	Value, number of buckets, bucket number requested
--Returns:	Value if bucket number requested = Hash computed in the range 1 .. l_bucket; NULL otherwise 
--Description:	Hashing function; suitable for partition elimination equalities';
COMMENT ON FUNCTION rif40_sql_pkg._rif40_hash_bucket_check(INTEGER, INTEGER, INTEGER) IS 'Function: 	_rif40_hash()
Parameters:	Value, number of buckets, bucket number requested
Returns:	Value if bucket number requested = Hash computed in the range 1 .. l_bucket; NULL otherwise 
Description:	Hashing function; suitable for partition elimination equalities';

--
-- Eof