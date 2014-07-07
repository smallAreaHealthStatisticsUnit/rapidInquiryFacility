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
--     				  rif40_GetMapAreaAttributeValue
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
-- rif40_GetMapAreaAttributeValue: 		51200 to 51399
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

DROP FUNCTION IF EXISTS rif40_xml_pkg.rif40_GetMapAreaAttributeValue(REFCURSOR, VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme,
		VARCHAR, VARCHAR[], INTEGER, INTEGER);
CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_GetMapAreaAttributeValue(
	getmap_temp_table	VARCHAR,
	getmap_ref_cursor 	REFCURSOR 	DEFAULT NULL,
	l_offset		INTEGER		DEFAULT 0,
	l_row_limit		INTEGER		DEFAULT NULL)
RETURNS REFCURSOR
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_GetMapAreaAttributeValue()
Parameters:	Temporary table (created by rif40_CreateMapAreaAttributeSource()), 
		REFCURSOR [Default NULL - same as temporary table name,
		offset [Default 0], row limit [Default NULL - All rows]
Returns:	Scrollable REFCURSOR
Description:    Return REFCURSOR as SELECT FROM temporary table
		This function returns a REFCURSOR, so only parses the SQL and does not execute it.
		Offset and row limit are used for cursor control

 */
DECLARE
	c5getallatt4theme CURSOR(l_c4getallatt4theme VARCHAR) FOR
		SELECT *
		  FROM pg_cursors
		 WHERE name     = l_c4getallatt4theme;
	c1getallatt4theme CURSOR(l_c1getallatt4theme VARCHAR) FOR
		SELECT *
		  FROM information_schema.tables
		 WHERE table_name     = l_c1getallatt4theme;
--
	c1_rec RECORD;
	c5_rec RECORD;
--
	sql_stmt			VARCHAR;
	l_getmap_ref_cursor		REFCURSOR;
--
	stp 				TIMESTAMP WITH TIME ZONE:=clock_timestamp();
	etp 				TIMESTAMP WITH TIME ZONE;
	took 				INTERVAL;
--
	error_message 			VARCHAR;
	v_detail 			VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';	
BEGIN
--
-- User must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-51200, 'rif40_GetMapAreaAttributeValue', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
--
-- Check if temporsry table exists
--
	IF getmap_temp_table IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51201, 'rif40_GetMapAreaAttributeValue', 
			'Null temporary table name');
	ELSE
		OPEN c1getallatt4theme(LOWER(quote_ident(getmap_temp_table)));
		FETCH c1getallatt4theme INTO c1_rec;
		CLOSE c1getallatt4theme;
		IF c1_rec.table_name IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-51202, 'rif40_GetMapAreaAttributeValue', 
				'Temporary table: % does not exist',
			       	getmap_temp_table::VARCHAR	/* Temporsry table name */);
		ELSIF c1_rec.table_type != 'LOCAL TEMPORARY' THEN
			PERFORM rif40_log_pkg.rif40_error(-51203, 'rif40_GetMapAreaAttributeValue', 
				'Temporary table: % is not LOCAL TEMPORARY: %',
			       	getmap_temp_table::VARCHAR	/* Temporsry table name */,
				c1_rec.table_type::VARCHAR	/* Table type */);
		END IF;
	END IF;
	
--
-- Set cursor name
--
	IF getmap_ref_cursor IS NULL THEN
		l_getmap_ref_cursor:=getmap_temp_table::REFCURSOR;
	ELSE
		l_getmap_ref_cursor:=getmap_ref_cursor;
	END IF;

--
-- Check if cursor exists
--
	OPEN c5getallatt4theme(LOWER(quote_ident(l_getmap_ref_cursor::Text)));
	FETCH c5getallatt4theme INTO c5_rec;
	CLOSE c5getallatt4theme;
	IF c5_rec.name IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-51204, 'rif40_GetMapAreaAttributeValue', 'Cursor: % is use, created: %, SQL>'||E'\n'||'%;',
			c5_rec.name::VARCHAR		/* Cursor name */,
			c5_rec.creation_time::VARCHAR	/* Created */,
			c5_rec.statement::VARCHAR	/* SQL */);
	END IF;

--
-- Build SQL Statement
--
	sql_stmt:='SELECT * FROM '||quote_ident(lower(getmap_temp_table));
--
-- Row limit and offset control
--
	IF l_offset IS NOT NULL THEN
		IF l_row_limit IS NOT NULL THEN
			sql_stmt:=sql_stmt||' ORDER BY gid_rowindex OFFSET $1 LIMIT $2';
		ELSE
			sql_stmt:=sql_stmt||' ORDER BY gid_rowindex OFFSET $1';
		END IF;				
	ELSE
		IF l_row_limit IS NOT NULL THEN
			sql_stmt:=sql_stmt||' ORDER BY gid_rowindex LIMIT $2';
		ELSE
			sql_stmt:=sql_stmt||' ORDER BY gid_rowindex';
		END IF;				
	END IF;				
--
-- As function returns a REFCURSOR it only parses and does NOT execute
--
	BEGIN

--
-- Create temporary table
--
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_GetMapAreaAttributeValue', 
			'[51205] c4getallatt4theme SQL> '||E'\n'||'%;', sql_stmt::VARCHAR); 
		OPEN l_getmap_ref_cursor SCROLL FOR EXECUTE sql_stmt /* Make cursor scrollable */ USING l_offset, l_row_limit;
	EXCEPTION
		WHEN others THEN
--
-- Print exception to INFO, re-raise
--
			GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
			error_message:='rif40_GetMapAreaAttributeValue() caught: '||E'\n'||
				SQLERRM::VARCHAR||', detail: '||v_detail::VARCHAR;
			RAISE INFO '51206: %', error_message;
--
			RAISE;
	END;

--
-- Instrument
--
	etp:=clock_timestamp();
	took:=age(etp, stp);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_GetMapAreaAttributeValue', 
		'[51207] Cursor: %, temporary table: %; offset: %, row limit: %, SQL parse took: %.', 			
		LOWER(quote_ident(l_getmap_ref_cursor::Text))::VARCHAR	/* Cursor name */, 
		LOWER(quote_ident(getmap_temp_table))::VARCHAR		/* Temporary table */, 
		l_offset::VARCHAR					/* Cursor offset */, 
		l_row_limit::VARCHAR					/* Cursor row limit */, 
		took::VARCHAR						/* Time taken */);
--
	RETURN l_getmap_ref_cursor;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_GetMapAreaAttributeValue(VARCHAR, REFCURSOR, INTEGER, INTEGER) IS 'Function: 	rif40_GetMapAreaAttributeValue()';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetMapAreaAttributeValue(VARCHAR, REFCURSOR, INTEGER, INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_GetMapAreaAttributeValue(VARCHAR, REFCURSOR, INTEGER, INTEGER) TO rif_user;

DROP FUNCTION IF EXISTS rif40_xml_pkg._rif40_GetMapAreaAttributeValue_explain_ddl(
	 VARCHAR, VARCHAR, VARCHAR, INTEGER, INTEGER);

--
-- Eof
