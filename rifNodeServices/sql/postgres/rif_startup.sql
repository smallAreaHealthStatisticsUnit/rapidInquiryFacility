/*
 * SQL statement name: 	rif_startup.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:			None
 *
 * Description:			Run RIF startup script (geoDataLoader version)
 * Note:				% becomes % after substitution
 */
DO LANGUAGE plpgsql $$
DECLARE
 	c1 CURSOR FOR
		SELECT p.proname
		  FROM pg_proc p, pg_namespace n
		 WHERE p.proname  = 'rif40_startup'
		   AND n.nspname  = 'rif40_sql_pkg'
		   AND p.proowner = (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
		   AND n.oid      = p.pronamespace;
--
	c1_rec RECORD;
	sql_stmt VARCHAR;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.proname = 'rif40_startup' THEN
		PERFORM rif40_sql_pkg.rif40_startup();
	ELSE
		RAISE EXCEPTION 'RIF startup(geoDataLoader): not a RIF database';
	END IF;
--
-- Set a default path, schema to user
--
	IF current_user = 'rif40' THEN
		sql_stmt:='SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
	ELSE
		RAISE EXCEPTION 'RIF startup(geoDataLoader): RIF user: % is not rif40', current_user;
	END IF;
	RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
	EXECUTE sql_stmt;
END;
$$;
--
-- Eof