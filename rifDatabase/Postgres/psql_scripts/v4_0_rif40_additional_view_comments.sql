\set VERBOSITY terse
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		WITH a AS (
			SELECT table_name table_or_view, table_schema, column_name, ordinal_position
				  FROM information_schema.columns a
					LEFT OUTER JOIN pg_tables b1 ON 
						(b1.schemaname = a.table_schema AND a.table_name = b1.tablename) 
					LEFT OUTER JOIN pg_views b2  ON 
						(b2.schemaname = a.table_schema AND a.table_name = b2.viewname) 
			 WHERE (viewowner IN (USER, 'rif40') OR tableowner IN (USER, 'rif40'))
				AND table_schema = 'rif40'
		), b1 AS (
			SELECT table_or_view, table_schema, column_name, ordinal_position, b.oid
  			  FROM a, pg_class b
			 WHERE b.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, 'rif40')) 
			   AND b.relname  = a.table_or_view
		), b2 AS (
			SELECT b.relname AS table_or_view, table_schema, column_name, ordinal_position, b.oid
  			  FROM a, pg_class b
			 WHERE b.relowner IN (SELECT oid FROM pg_roles WHERE rolname IN (USER, 'rif40')) 
			   AND b.relname  = a.table_or_view
		), c1 AS (
			SELECT table_or_view, table_schema, column_name, ordinal_position, c.description
			  FROM b1
				LEFT OUTER JOIN pg_description c ON (c.objoid = b1.oid AND c.objsubid = b1.ordinal_position)
			 WHERE c.description IS NULL
		), c2 AS (
			SELECT table_or_view, table_schema, column_name, ordinal_position, c.description
			  FROM b2
				LEFT OUTER JOIN pg_description c ON (c.objoid = b2.oid AND c.objsubid = b2.ordinal_position)
			 WHERE c.description IS NOT NULL
		)
		SELECT c1.table_or_view, c1.table_schema, c1.column_name, c2.description
		  FROM c1
			LEFT OUTER JOIN c2 ON ('t_'||c1.table_or_view = c2.table_or_view AND c1.column_name = c2.column_name)
		 WHERE c2.description IS NOT NULL
		 ORDER BY 1, 2;
	c1_rec RECORD;
--
	sql_stmt	VARCHAR[];
	i INTEGER:=0;
BEGIN
--
-- Turn on some debug
--
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl:DEBUG1');
--
	FOR c1_rec IN c1 LOOP
		i:=i+1;
		sql_stmt[i]:='COMMENT ON COLUMN '||c1_rec.table_or_view||'.'||c1_rec.column_name||' IS '''||c1_rec.description||'''';
	END LOOP;
	PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
END;
$$;
\set VERBOSITY default

COMMENT ON COLUMN rif40_columns.table_or_view_name_hide  IS 'Table name';
COMMENT ON COLUMN rif40_columns.column_name_hide         IS 'Column name';
COMMENT ON COLUMN rif40_columns.table_or_view_name_href  IS 'Table name (web version)';
COMMENT ON COLUMN rif40_columns.column_name_href         IS 'Column name (web version)'; 
COMMENT ON COLUMN rif40_columns.nullable                 IS 'Nollable';
COMMENT ON COLUMN rif40_columns.oracle_data_type         IS 'Oracle data type';
COMMENT ON COLUMN rif40_columns.comments  	         IS 'Comments';

COMMENT ON COLUMN rif40_tables_and_views.class                   IS 'Class';          
COMMENT ON COLUMN rif40_tables_and_views.table_or_view           IS 'Table name';      
COMMENT ON COLUMN rif40_tables_and_views.table_or_view_name_href IS 'Table name (web version)';
COMMENT ON COLUMN rif40_tables_and_views.table_or_view_name_hide IS 'Table name (web version)';
COMMENT ON COLUMN rif40_tables_and_views.comments                IS 'Comments';  

COMMENT ON COLUMN rif40_triggers.table_name         IS 'Table name';
COMMENT ON COLUMN rif40_triggers.column_name        IS 'Column name';
COMMENT ON COLUMN rif40_triggers.trigger_name       IS 'Trigger name';
COMMENT ON COLUMN rif40_triggers.trigger_type       IS 'Type type';
COMMENT ON COLUMN rif40_triggers.triggering_event   IS 'Triggering event';
COMMENT ON COLUMN rif40_triggers.when_clause        IS 'When clause';
COMMENT ON COLUMN rif40_triggers.action_type        IS 'Action type';
COMMENT ON COLUMN rif40_triggers.comments           IS 'Comments';
--
-- Eof
