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
--     				  rif40_DeleteMapAreaAttributeSource
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
-- rif40_DeleteMapAreaAttributeSource: 		51800 to 51999
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

CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource(
	closeCursor1 	VARCHAR)
RETURNS VOID
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_DeleteMapAreaAttributeSource()
Parameters:	Map attribute source (temporary table name)
Returns: 	Nothing
Description:	Drop map attribute source (temporary table)

Checks:

- Check if cursor exists, if yes close REF_CURSOR (created by rif40_GetMapAreaAttributeValue)
- Check temporary table exists

 */
DECLARE
	c1deletesource CURSOR(l_c4getallatt4theme VARCHAR) FOR
		SELECT *
		  FROM pg_cursors
		 WHERE name     = l_c4getallatt4theme;
	c2deletesource CURSOR(l_table VARCHAR) FOR
		SELECT table_name 
		  FROM information_schema.tables 
		 WHERE table_name  = l_table 
	           AND table_type  = 'LOCAL TEMPORARY';
--
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt		VARCHAR;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- User must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-51800, 'rif40_DeleteMapAreaAttributeSource', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;

--
-- Check if cursor exists, if yes close it
--
	OPEN c1deletesource(quote_ident(LOWER(closeCursor1::Text)));
	FETCH c1deletesource INTO c1_rec;
	CLOSE c1deletesource;
	IF c1_rec.name IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_DeleteMapAreaAttributeSource', '[51801] Cursor: % in use, created: %, SQL>'||E'\n'||'%;',
			c1_rec.name::VARCHAR		/* Cursor name */,
			c1_rec.creation_time::VARCHAR	/* Created */,
			c1_rec.statement::VARCHAR	/* SQL */);
		PERFORM rif40_xml_pkg.rif40_closeGetMapAreaAttributeCursor(closeCursor1);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_DeleteMapAreaAttributeSource', '[51802] Cursor: % is not in use.',
			quote_ident(LOWER(closeCursor1::Text))::VARCHAR		/* Cursor name */);
	END IF;

--
-- Check temporary table exists
--
	OPEN c2deletesource(quote_ident(LOWER(closeCursor1::Text)));
	FETCH c2deletesource INTO c2_rec;
	CLOSE c2deletesource;
	IF c2_rec.table_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(51803, 'rif40_DeleteMapAreaAttributeSource', 'Temporary table: % not found.',
			quote_ident(LOWER(closeCursor1::Text))::VARCHAR		/* Cursor name */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_DeleteMapAreaAttributeSource', '[51804] Found temporary table: %.',
			c2_rec.table_name::VARCHAR		/*  Temporary table name */);
	END IF;
--
-- Drop temporary table
--
	BEGIN
		sql_stmt:='DROP TABLE '||c2_rec.table_name;
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_DeleteMapAreaAttributeSource() caught: '||E'\n'||
				SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
			RAISE INFO '51805: %', error_message;
--
			RAISE;
	END;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource(VARCHAR) IS 'Function: 	rif40_DeleteMapAreaAttributeSource()
Parameters:	Map attribute source (temporary table name)
Returns: 	Nothing
Description:	Drop map attribute source (temporary table)

Checks:

- Check if cursor exists, if yes close REF_CURSOR (created by rif40_GetMapAreaAttributeValue)
- Check temporary table exists';
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource(VARCHAR) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_DeleteMapAreaAttributeSource(VARCHAR) TO rif_user;

--
-- Eof
