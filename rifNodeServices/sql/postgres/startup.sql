/*
 * SQL statement name: 	startup.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:			None
 *
 * Description:			Run non RIF startup script (geoDataLoader version)
 * Note:				% becomes % after substitution
 */
DO LANGUAGE plpgsql $$
DECLARE
	sql_stmt VARCHAR;
BEGIN
--
-- Set a default path and schema for user
--
	IF current_user != 'rif40' THEN
		sql_stmt:='SET SESSION search_path TO '||current_user||',public, topology';
	ELSE
		RAISE EXCEPTION 'RIF startup(geoDataLoader): RIF user: % is not allowed to run this script', current_user;
	END IF;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
--
-- Eof