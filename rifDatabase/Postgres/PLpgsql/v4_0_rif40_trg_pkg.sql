-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/14 17:18:41 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_rif40_trg_pkg.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/PLpgsql/v4_0_rif40_trg_pkg.sql,v $
-- $Revision: 1.4 $
-- $Id: v4_0_rif40_trg_pkg.sql,v 1.4 2014/02/14 17:18:41 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) -  Created PG psql code (INSTEAD OF triggers for views with USERNAME as a column)
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
-- $Log: v4_0_rif40_trg_pkg.sql,v $
-- Revision 1.4  2014/02/14 17:18:41  peterh
--
-- Clean build. Issue with ST_simplify(), intersection code and UK geography (to be resolved)
-- Fully commented (and check now works)
--
-- Stubs for range/hash partitioning added
--
-- Revision 1.3  2013/09/25 12:12:23  peterh
-- Baseline after 2x full clean builds at Postgres level
-- TODO.txt uptodate
--
-- Revision 1.2  2013/09/18 15:20:32  peterh
-- Checkin at end of 6 week RIF focus. Got as far as SAHSULAND run study to completion for observed only
--
-- Revision 1.1  2013/09/02 14:08:33  peterh
--
-- Baseline after full trigger implmentation
--
-- Revision 1.2  2013/03/14 17:35:39  peterh
-- Baseline for TX to laptop
--
--
\set ON_ERROR_STOP Off
DROP TRIGGER trg_rif40_parameters ON rif40_parameters;
DROP FUNCTION rif40_trg_pkg.trgf_rif40_parameters();

\set ON_ERROR_STOP ON
\echo Creating PG psql code (INSTEAD OF triggers for views with USERNAME as a column)...

CREATE OR REPLACE FUNCTION rif40_trg_pkg.create_instead_of_triggers() 
RETURNS void
AS
$func$
/*

Function: 	create_instead_of_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Create (or re-create) INSTEAD OF functions and triggers for all views of T_ tables where username as a column
	 	INSERT/UPDATE/DELETE of another users data is NOT permitted	
 */
DECLARE
	c1ciot CURSOR FOR
		WITH t AS (
			SELECT a.relname AS tablename, b.description,
			       'INSTEAD OF trigger for view '||UPPER(a.relname)||
					' to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. '||E'\n'||
					COALESCE(b.description, '') AS comments 				
 			 FROM pg_roles r, pg_namespace n, pg_attribute c, pg_class a		
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid AND b.objsubid IS NULL)
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'r' 				/* Relational table */
			   AND a.relpersistence IN ('p', 'u') 			/* Persistent */
			   AND a.relowner       = r.oid
			   AND n.oid 		= a.relnamespace
			   AND n.nspname  	= USER    
			   AND c.attrelid       = a.oid
			   AND c.attname  	= 'username'
		), v AS (
			SELECT a.relname AS viewname, b.description,
			       'INSTEAD OF trigger for view '||UPPER(a.relname)||
					' to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. '||E'\n'||
					COALESCE(b.description, '') AS comments 				
 			 FROM pg_roles r, pg_namespace n, pg_attribute c, pg_class a		
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid AND b.objsubid IS NULL)
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'v' 				/* Views */
			   AND a.relowner       = r.oid
			   AND n.oid 		= a.relnamespace
			   AND n.nspname  	= USER    
			   AND c.attrelid       = a.oid
			   AND c.attname  	= 'username'
		)
		SELECT t.tablename, v.viewname, 
		       CASE 
				WHEN v.description IS NOT NULL THEN v.comments /* Use view comments in preference */
				WHEN t.description IS NOT NULL THEN t.comments /* Use view comments in preference */
				ELSE t.comments||' [NO TABLE/VIEW comments available]'
		       END AS comments
		  FROM t, v
		 WHERE t.tablename  = 't_'||v.viewname
		 ORDER BY 1;
	c2ciot CURSOR(l_view VARCHAR) FOR
		WITH pk AS (
			SELECT c.attname
			  FROM pg_index i, pg_class a, pg_attribute c
			 WHERE a.oid    = i.indrelid
			   AND a.oid    = c.attrelid
			   AND c.attnum = ANY(i.indkey)
		   	   AND a.relname 	= 't_'||l_view
			   AND i.indisprimary
		), d AS (
			SELECT c.attname, coalesce(e.adsrc::text, '') AS default_value
			  FROM pg_class a, pg_attribute c 
				LEFT OUTER JOIN pg_attrdef e ON (c.attrelid = e.adrelid AND c.attnum = e.adnum)
			 WHERE a.oid   	        = c.attrelid
		   	   AND a.relname 	= 't_'||l_view
                   	   AND NOT (c.attname 	= 'partition_parallelisation'
                           AND      a.relname 	= 'rif40_studies')
		), k AS (
			SELECT l.relname, j.column_name, m.description
  			  FROM information_schema.columns j, pg_class l, pg_description m
			 WHERE m.objoid = l.oid AND m.objsubid = j.ordinal_position
			   AND l.relname =  't_'||l_view
			   AND l.relname = j.table_name
		)
		SELECT c.attname AS columnname,
                       CASE WHEN pk.attname IS NOT NULL THEN TRUE ELSE FALSE END::BOOLEAN AS is_pk,
		       CASE WHEN d.attname IS NOT NULL THEN TRUE ELSE FALSE END::BOOLEAN AS is_table_column,
		       d.default_value,
                       k.description
 		 FROM pg_roles r, pg_class a, pg_namespace n, pg_attribute c
			LEFT OUTER JOIN pk ON (c.attname = pk.attname)		
			LEFT OUTER JOIN d ON (c.attname = d.attname)
			LEFT OUTER JOIN k ON (c.attname = k.column_name)			
		 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
		   AND a.relkind        = 'v' 				/* Views */
		   AND a.relowner       = r.oid
		   AND n.oid 		= a.relnamespace
		   AND n.nspname  	= USER    
		   AND c.attrelid       = a.oid
		   AND a.relname 	= l_view
		 ORDER BY c.attnum;
--
	c1ciot_rec RECORD;
	c2ciot_rec RECORD;
--
	sql_stmt VARCHAR[];
	comment_sql VARCHAR[];
	column_list VARCHAR;
	insert_stmt VARCHAR;
	update_stmt VARCHAR;
	delete_stmt VARCHAR;
	where_stmt VARCHAR;
--
	i INTEGER:=0;
	j INTEGER:=1;
--
	role_list VARCHAR[]:=ARRAY['rif_user', 'rif_manager'];
	priv_list VARCHAR[]:=ARRAY['INSERT', 'UPDATE', 'DELETE'];
BEGIN
--
-- Must be RIF40
--
	IF USER != 'rif40' THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'create_instead_of_triggers',
                	'Cannot create INSTEAD OF triggers; user % must must be RIF40', USER::VARCHAR);
	END IF;
--
	FOR c1ciot_rec IN c1ciot LOOP
--
-- Create  INSERT/UPDATE/DELETE statements
--
		sql_stmt:=NULL;
		j:=1;
		where_stmt:=NULL;
		update_stmt:='UPDATE '||c1ciot_rec.tablename||E'\n'; 
		delete_stmt:='DELETE FROM '||c1ciot_rec.tablename; 
		i:=0;
		comment_sql:=NULL;
		FOR c2ciot_rec IN c2ciot(c1ciot_rec.viewname) LOOP /* Column bits */
--
-- Create PK WHERE clause
--
			IF c2ciot_rec.is_pk THEN
				IF where_stmt IS NULL THEN
					where_stmt:='WHERE '||c2ciot_rec.columnname||'=OLD.'||c2ciot_rec.columnname;
				ELSE
					where_stmt:=where_stmt||E'\n'||E'\t'||E'\t'||E'\t'||'   AND '||c2ciot_rec.columnname||'=OLD.'||c2ciot_rec.columnname;
				END IF;
			END IF;
--
			i:=i+1;
			IF i = 1 THEN
				IF c2ciot_rec.is_table_column THEN
					IF c2ciot_rec.default_value = '' THEN
						insert_stmt:=E'\t'||E'\t'||E'\t'||E'\t'||
							'NEW.'||c2ciot_rec.columnname||' /* no default value */';
					ELSE
						insert_stmt:=E'\t'||E'\t'||E'\t'||E'\t'||
							'coalesce(NEW.'||c2ciot_rec.columnname||', '||c2ciot_rec.default_value||')';
					END IF;
				ELSE
					insert_stmt:='';
				END IF;
				column_list:=E'\t'||E'\t'||c2ciot_rec.columnname;
				update_stmt:=update_stmt||E'\t'||E'\t'||E'\t'||'   SET '||c2ciot_rec.columnname||'=NEW.'||c2ciot_rec.columnname;
			ELSE
				IF c2ciot_rec.is_table_column THEN
					IF c2ciot_rec.default_value = '' THEN
						insert_stmt:=insert_stmt||','||E'\n'||E'\t'||E'\t'||E'\t'||E'\t'||'NEW.'||
							c2ciot_rec.columnname||' /* no default value */';
					ELSE
						insert_stmt:=insert_stmt||','||E'\n'||E'\t'||E'\t'||E'\t'||E'\t'||'coalesce(NEW.'||
							c2ciot_rec.columnname||', '||c2ciot_rec.default_value||')';
					END IF;
					column_list:=column_list||','||E'\n'||E'\t'||E'\t'||E'\t'||E'\t'||c2ciot_rec.columnname;
					update_stmt:=update_stmt||','||E'\n'||E'\t'||E'\t'||E'\t'||'       '||
						c2ciot_rec.columnname||'=NEW.'||c2ciot_rec.columnname;
				END IF;
			END IF;
			IF c2ciot_rec.description IS NOT NULL THEN
				comment_sql[i]:='COMMENT ON COLUMN '||c1ciot_rec.viewname||'.'||c2ciot_rec.columnname||
					' IS '''||c2ciot_rec.description||'''';
			ELSE
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'create_instead_of_triggers', 
					'No comment for column: %.%', 
					c1ciot_rec.viewname::VARCHAR,
					c2ciot_rec.columnname::VARCHAR);
				comment_sql[i]:='COMMENT ON COLUMN '||c1ciot_rec.viewname||'.'||c2ciot_rec.columnname||
					' IS ''N/A''';
			END If;
		END LOOP;
		insert_stmt:='INSERT INTO '||c1ciot_rec.tablename||' ('||E'\n'||E'\t'||E'\t'||column_list||')'||E'\n'||E'\t'||E'\t'||E'\t'||'VALUES('||E'\n'||insert_stmt||');';
		update_stmt:=update_stmt||E'\n'||E'\t'||E'\t'||E'\t'||' '||where_stmt||';';
		delete_stmt:=delete_stmt||E'\n'||E'\t'||E'\t'||E'\t'||' '||where_stmt||';';
		IF where_stmt IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'create_instead_of_triggers',
                		'Cannot create INSTEAD OF triggers; no primary key columns indexed for table: %', c1ciot_rec.tablename::VARCHAR);
		END IF;
--
-- Funcion code
--
		sql_stmt[j]:='CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_'||c1ciot_rec.viewname||'()'||E'\n'||
			'RETURNS TRIGGER'||E'\n'||
			'LANGUAGE plpgsql'||E'\n'||
			'AS $trigger_function$'||E'\n'||
			'BEGIN'||E'\n'||
			E'\t'||'IF TG_OP = ''INSERT'' THEN'||E'\n'||
			'--'||E'\n'||
			'-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT'||E'\n'||
			'--'||E'\n'||
			E'\t'||E'\t'||'IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN'||E'\n'||
			E'\t'||E'\t'||E'\t'||insert_stmt||E'\n'||
			E'\t'||E'\t'||'ELSE'||E'\n'||
			E'\t'||E'\t'||E'\t'||'PERFORM rif40_log_pkg.rif40_error(-20999, ''trg_'||c1ciot_rec.viewname||''','||E'\n'
			||E'\t'||E'\t'||E'\t'||E'\t'||'''Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL'', USER::VARCHAR, NEW.username::VARCHAR);'||E'\n'||
			E'\t'||E'\t'||'END IF;'||E'\n'||
			E'\t'||E'\t'||'RETURN NEW;'||E'\n'||
			E'\t'||'ELSIF TG_OP = ''UPDATE'' THEN'||E'\n'||
			'--'||E'\n'||
			'-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE'||E'\n'||
			'--'||E'\n'||
			E'\t'||E'\t'||'IF USER = OLD.username AND NEW.username = OLD.username THEN'||E'\n'||
			E'\t'||E'\t'||E'\t'||update_stmt||E'\n'||
			E'\t'||E'\t'||'ELSE'||E'\n'||
			E'\t'||E'\t'||E'\t'||'PERFORM rif40_log_pkg.rif40_error(-20999, ''trg_'||c1ciot_rec.viewname||''','||E'\n'||
			E'\t'||E'\t'||E'\t'||E'\t'||'''Cannot UPDATE: User % is not the owner (%) of the record'', USER::VARCHAR, OLD.username::VARCHAR);'||E'\n'||
			E'\t'||E'\t'||'END IF;'||E'\n'||
			E'\t'||E'\t'||'RETURN NEW;'||E'\n'||
			E'\t'||'ELSIF TG_OP = ''DELETE'' THEN'||E'\n'||
			'--'||E'\n'||
			'-- Check USER = OLD.username; if OK DELETE'||E'\n'||
			'--'||E'\n'||
			E'\t'||E'\t'||'IF USER = OLD.username THEN'||E'\n'||
			E'\t'||E'\t'||E'\t'||delete_stmt||E'\n'||
			E'\t'||E'\t'||'ELSE'||E'\n'||
			E'\t'||E'\t'||E'\t'||'PERFORM rif40_log_pkg.rif40_error(-20999, ''trg_'||c1ciot_rec.viewname||''','||E'\n'||
			E'\t'||E'\t'||E'\t'||E'\t'||'''Cannot DELETE: User % is not the owner (%) of the record'', USER::VARCHAR, OLD.username::VARCHAR);'||E'\n'||
			E'\t'||E'\t'||'END IF;'||E'\n'||
			E'\t'||E'\t'||'RETURN NULL;'||E'\n'||
			E'\t'||'END IF;'||E'\n'||
			E'\t'||'RETURN NEW;'||E'\n'||
			'END;'||E'\n'||
			'$trigger_function$;';
--
-- Trigger code
--
		j:=j+1;
		sql_stmt[j]:='CREATE TRIGGER trg_'||c1ciot_rec.viewname||' INSTEAD OF INSERT OR UPDATE OR DELETE ON '||
			c1ciot_rec.viewname||' FOR EACH ROW EXECUTE PROCEDURE rif40_trg_pkg.trgf_'||c1ciot_rec.viewname||'()';
--
-- Comments
--
		j:=j+1;
		sql_stmt[j]:='COMMENT ON TRIGGER trg_'||c1ciot_rec.viewname||' ON '||c1ciot_rec.viewname||' IS '''||c1ciot_rec.comments||'''';
		j:=j+1;
		sql_stmt[j]:='COMMENT ON FUNCTION rif40_trg_pkg.trgf_'||c1ciot_rec.viewname||'() IS '''||c1ciot_rec.comments||'''';
--
-- Column comments
--
		FOR k IN array_lower(comment_sql, 1) .. array_upper(comment_sql, 1) LOOP
			j:=j+1;
			sql_stmt[j]:=comment_sql[k];
		END LOOP;
--
-- Grants
--
		FOR k IN array_lower(role_list, 1) .. array_upper(role_list, 1) LOOP
			FOR l IN array_lower(priv_list, 1) .. array_upper(priv_list, 1) LOOP
				IF has_table_privilege(role_list[k], c1ciot_rec.tablename, priv_list[l]) THEN
					j:=j+1;
					sql_stmt[j]:='GRANT '||priv_list[l]||' ON '||c1ciot_rec.viewname||' TO '||role_list[k];
				END IF;
			END LOOP;
		END LOOP;
--
		PERFORM rif40_log_pkg.rif40_log('INFO', 'create_instead_of_triggers', 'Creating INSTEAD OF function and trigger for: %', 
			c1ciot_rec.viewname::VARCHAR);
--
-- Execute SQL to create INSTEAD trigger for VIEW
--
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_trg_pkg.create_instead_of_triggers() IS 'Function: 	create_instead_of_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Create (or re-create) INSTEAD OF functions and triggers for all views of T_ tables where username as a column
	 	INSERT/UPDATE/DELETE of another users data in NOT permitted
Generates SQL:

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_user_projects()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $trigger_function$
BEGIN
        IF TG_OP = ''INSERT'' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
                IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
                        INSERT INTO t_rif40_user_projects (
                                project,
                                username,
                                grant_date,
                                revoke_date,
                                description,
                                date_started,
                                date_ended)
                        VALUES(
                                NEW.project,
                                NEW.username,
                                NEW.grant_date,
                                NEW.revoke_date,
                                NEW.description,
                                NEW.date_started,
                                NEW.date_ended);
                ELSE
                        PERFORM rif40_log_pkg.rif40_error(-20999, ''trg_rif40_user_projects'',
                                ''Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL'', USER::VARCHAR, NEW.username::VARCHAR);
                END IF;
                RETURN NEW;
        ELSIF TG_OP = ''UPDATE'' THEN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
                IF USER = OLD.username AND NEW.username = OLD.username THEN
                        UPDATE t_rif40_user_projects
                           SET project=NEW.project,
                               username=NEW.username,
                               grant_date=NEW.grant_date,
                               revoke_date=NEW.revoke_date,
                               description=NEW.description,
                               date_started=NEW.date_started,
                               date_ended=NEW.date_ended
                         WHERE project=OLD.project
                           AND username=OLD.username;
                ELSE
                        PERFORM rif40_log_pkg.rif40_error(-20999, ''trg_rif40_user_projects'',
                                ''Cannot UPDATE: User % is not the owner (%) of the record'', USER::VARCHAR, OLD.username::VARCHAR);
                END IF;
                RETURN NEW;
        ELSIF TG_OP = ''DELETE'' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
                IF USER = OLD.username THEN
                        DELETE FROM t_rif40_user_projects
                         WHERE project=OLD.project
                           AND username=OLD.username;
                ELSE
                        PERFORM rif40_log_pkg.rif40_error(-20999, ''trg_rif40_user_projects'',
                                ''Cannot DELETE: User % is not the owner (%) of the record'', USER::VARCHAR, OLD.username::VARCHAR);
                END IF;
                RETURN NULL;
        END IF;
        RETURN NEW;
END;
$trigger_function$;

CREATE TRIGGER trg_rif40_user_projects INSTEAD OF INSERT OR UPDATE OR DELETE ON rif40_user_projects FOR EACH ROW EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_user_projects();

COMMENT ON TRIGGER trg_rif40_user_projects ON rif40_user_projects IS ''INSTEAD OF trigger for view T_RIF40_USER_PROJECTS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]'';

COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_user_projects() IS ''INSTEAD OF trigger for view T_RIF40_USER_PROJECTS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
 [NO TABLE/VIEW comments available]'';

GRANT INSERT ON rif40_user_projects TO rif_manager
GRANT UPDATE ON rif40_user_projects TO rif_manager
GRANT DELETE ON rif40_user_projects TO rif_manager
';

CREATE OR REPLACE FUNCTION rif40_trg_pkg.drop_instead_of_triggers() 
RETURNS void
AS
$func$
/*

Function: 	drop_instead_of_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop INSTEAD OF functions and triggers for all views of T_ tables where username as a column
	 	INSERT/UPDATE/DELETE of another users data is NOT permitted	
 */
DECLARE
	c1diot CURSOR FOR
		WITH t AS (
			SELECT a.relname AS tablename 				
 			 FROM pg_roles r, pg_class a, pg_namespace n, pg_attribute c		
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'r' 				/* Relational table */
			   AND a.relpersistence IN ('p', 'u') 			/* Persistent */
			   AND a.relowner       = r.oid
			   AND n.oid 		= a.relnamespace
			   AND n.nspname  	= USER    
			   AND c.attrelid       = a.oid
			   AND c.attname  	= 'username'
		), v AS (
			SELECT a.relname AS viewname 				
 			 FROM pg_roles r, pg_namespace n, pg_attribute c, pg_class a		
				LEFT OUTER JOIN pg_description b ON (b.objoid = a.oid)
			 WHERE a.relowner       = (SELECT oid FROM pg_roles WHERE rolname = USER)
			   AND a.relkind        = 'v' 				/* Views */
			   AND a.relowner       = r.oid
			   AND n.oid 		= a.relnamespace
			   AND n.nspname  	= USER    
			   AND c.attrelid       = a.oid
			   AND c.attname  	= 'username'
		)
		SELECT t.tablename, v.viewname
		  FROM t, v
		 WHERE t.tablename  = 't_'||v.viewname
		 ORDER BY 1;
--
	c1diot_rec RECORD;
	sql_stmt VARCHAR[];
BEGIN
--
-- Must be RIF40
--
	IF USER != 'rif40' THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'drop_instead_of_triggers',
                	'Cannot drop INSTEAD OF triggers; user % must must be RIF40', USER::VARCHAR);
	END IF;
--
	FOR c1diot_rec IN c1diot LOOP
		sql_stmt[1]:='DROP TRIGGER IF EXISTS trg_'||c1diot_rec.viewname||' ON '||c1diot_rec.viewname||' CASCADE';
		sql_stmt[2]:='DROP FUNCTION IF EXISTS rif40_trg_pkg.trgf_'||c1diot_rec.viewname||'() CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_trg_pkg.drop_instead_of_triggers() IS 'Function: 	drop_instead_of_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop INSTEAD OF functions and triggers for all views of T_ tables where username as a column
	 	INSERT/UPDATE/DELETE of another users data is NOT permitted';

CREATE OR REPLACE FUNCTION rif40_trg_pkg.rif40_db_name_check(column_name VARCHAR, value VARCHAR) 
RETURNS void
AS
$func$
/*

Function: 	rif40_db_name_check()
Parameters:	Column name, value
Returns:	NONE
Description:	Check column name value obeys DB naming conventions; i.e. Oracles. Value is assumed to be in upper case; even on Postgres where the convention is lower case	

SELECT regexp_replace('1 AA_123a ()*+Cb', '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g');
 regexp_replace 
----------------
 1 a ()*+b
(1 row)


 */
DECLARE
--
-- Check for valid Oracle name
--
	c3dbnc CURSOR(l_value VARCHAR) FOR
		SELECT REGEXP_REPLACE(l_value, '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g') AS invalid_characters,
		       CASE
				WHEN LENGTH(REGEXP_REPLACE(l_value, '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g')) > 0 THEN TRUE
				ELSE FALSE END::BOOLEAN AS is_invalid;
--
	c3dbnc_rec 		RECORD;
	maxlen 			INTEGER:=30;
BEGIN
--
-- Check for valid Oracle name
--
	OPEN c3dbnc(value);
	FETCH c3dbnc INTO c3dbnc_rec;
--
-- Invalid Oracle name
--
	IF c3dbnc_rec.is_invalid THEN
		CLOSE c3dbnc;
		PERFORM rif40_log_pkg.rif40_error(-20098, 'rif40_db_name_check', 'Invalid Oracle/Postgres name %: "%"; contains NON alphanumeric characters: %',
			UPPER(column_name)::VARCHAR/* Oracle name */, 
			value::VARCHAR		/* Value */,
			c3dbnc_rec.invalid_characters::VARCHAR	/* Invalid characters */);
	END IF;
	CLOSE c3dbnc;
--
-- Length must <= 30
--
	IF LENGTH(value) > maxlen THEN
		PERFORM rif40_log_pkg.rif40_error(-20097, 'rif40_db_name_check', 'Invalid Oracle name %: "%"; length (%) > %',
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			value::VARCHAR		/* Value */,
			LENGTH(value)::VARCHAR	/* Length */, 
			maxlen::VARCHAR		/* Maximum permitted length */);
	END IF;
--
-- First character must be a letter
--
	IF SUBSTR(value, 1, 1) NOT BETWEEN 'A' AND 'Z' THEN
		PERFORM rif40_log_pkg.rif40_error(-20096, 'rif40_db_name_check', 'Invalid Oracle/Postgres name s: "%"; First character (%) must be a letter', 
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			value::VARCHAR		/* Value */, 
			SUBSTR(value, 1, 1)::VARCHAR	/* First character */);
	END IF;
--
	IF value IS NOT NULL THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_db_name_check', 'rif40_db_name_check(%, %) Ok',
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			value::VARCHAR		/* Value */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_db_name_check', 'rif40_db_name_check(%, %) Ok',
			UPPER(column_name)::VARCHAR	/* Oracle name */, 
			'NULL'::VARCHAR		/* Value */);
	END IF;
END;
$func$
LANGUAGE 'plpgsql';

COMMENT ON FUNCTION rif40_trg_pkg.rif40_db_name_check(VARCHAR, VARCHAR) IS 'Function: 	rif40_db_name_check()
Parameters:	Column name, value
Returns:	NONE
Description:	Check column name value obeys DB naming conventions; i.e. Oracle''s. Value is assumed to be in upper case; even on Postgres where the convention is lower case';

CREATE OR REPLACE FUNCTION rif40_trg_pkg.rif40_sql_injection_check(
	table_name	VARCHAR,
	study_id	VARCHAR,
	inv_id 		VARCHAR,
	line_number	VARCHAR,
	name 		VARCHAR,
	value		VARCHAR)
RETURNS void
AS
$func$
/*
Parameters: 	Table nmame, study_id, inv_id, line_number, name, value
Description:	Check value for SQL injection	
 */
DECLARE
BEGIN
	PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_sql_injection_check', 'SQL injection check not yet implemented for table: %, study: %', 
		table_name::VARCHAR, study_id::VARCHAR);
END;
$func$
LANGUAGE 'plpgsql';

COMMENT ON FUNCTION rif40_trg_pkg.rif40_sql_injection_check(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Parameters: 	Table nmame, study_id, inv_id, line_number, name, value
Description:	Check value for SQL injection';

\df rif40_trg_pkg.rif40_sql_injection_check

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_sql_injection_check(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_sql_injection_check(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_user, rif_manager;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.rif40_drop_table_triggers() 
RETURNS void
AS
$func$
/*

Function: 	rif40_drop_table_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop triggers and trigger functions on all standard RIF tables (Used as part of the build ONLY)
		rfi40 table trigger in schema rif40_trg_pkg

*/
DECLARE
	c1dtt CURSOR FOR
		SELECT tgname AS trigger_name, 
		       relname AS table_name,
		       n.nspname AS table_schema,
		       n2.nspname||'.'||p.proname||rif40_sql_pkg.rif40_get_function_arg_types(p.proargtypes) AS function_name
		  FROM pg_tables t, pg_class c, pg_trigger b, pg_namespace n, pg_namespace n2, pg_proc p
		 WHERE b.tgrelid        = c.oid				
                   AND NOT b.tgisinternal				/* Ignore constraints */
		   AND n.oid            = c.relnamespace		/* Table schema */
		   AND b.tgfoid         = p.oid				/* Trigger function */
		   AND n2.oid            = p.pronamespace		/* Fu8nction schema */
		   AND c.relowner       IN (SELECT oid FROM pg_roles WHERE rolname = USER)	/* RIF40 tables */
		   AND n2.nspname        = 'rif40_trg_pkg' 		/* Function schema: rif40_trg_pkg */
		   AND c.relname        = t.tablename			/* Tables only */
		   AND c.relkind        = 'r' 				/* Relational table */
		   AND c.relpersistence IN ('p', 'u') 			/* Persistence: permanent/unlogged */
		 ORDER BY 1, 2;
--
	c1dtt_rec RECORD;
--
	sql_stmt VARCHAR[];
BEGIN
--
-- Must be RIF40
--
	IF USER != 'rif40' THEN
		PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_drop_table_triggers',
                	'Cannot drop INSTEAD OF triggers; user % must must be RIF40', USER::VARCHAR);
	END IF;
--
	FOR c1dtt_rec IN c1dtt LOOP
		sql_stmt[1]:='DROP TRIGGER IF EXISTS '||c1dtt_rec.trigger_name||' ON '||c1dtt_rec.table_schema||'.'||c1dtt_rec.table_name||' CASCADE';
		sql_stmt[2]:='DROP FUNCTION IF EXISTS '||c1dtt_rec.function_name||' CASCADE';
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
	END LOOP;
END;
$func$
LANGUAGE 'plpgsql';

COMMENT ON FUNCTION rif40_trg_pkg.rif40_drop_table_triggers() IS 'Function: 	rif40_drop_table_triggers()
Parameters:	NONE
Returns:	NONE
Description:	Drop triggers and trigger functions on all standard RIF tables (Used as part of the build ONLY)
		rfi40 table trigger in schema rif40_trg_pkg';

\df rif40_trg_pkg.rif40_drop_table_triggers

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_drop_table_triggers() TO rif40;

/* Remaining INSTEAD OF triggers for VIEW of TABLES without USERNAME */ 

\df rif40_trg_pkg.rif40_db_name_check

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_db_name_check(VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_trg_pkg.rif40_db_name_check(VARCHAR, VARCHAR) TO rif_user, rif_manager;

\df rif40_trg_pkg.create_instead_of_triggers

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.create_instead_of_triggers() TO rif40;

\df rif40_trg_pkg.drop_instead_of_triggers

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.drop_instead_of_triggers() TO rif40;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.lp_outcomes_check_column(
	l_outcome_type	VARCHAR,
	l_rif40_column	VARCHAR,
	l_schema 	VARCHAR,
	l_table_name	VARCHAR,
	l_column	VARCHAR)
RETURNS void
AS
$func$
/*

Function: 	lp_outcomes_check_column()
Parameters:	Outcome type, Column name in RIF40_OUTCOMES, Column (value in above), table, column [both being checked]
Returns:	NONE
Description:	Check outcome type exists

*/
DECLARE
	c1outc CURSOR (l_schema VARCHAR, l_table VARCHAR, l_column VARCHAR) IS
		SELECT column_name
		  FROM information_schema.columns
		 WHERE table_schema = LOWER(l_schema)
		   AND table_name   = LOWER(l_table)
		   AND column_name  = LOWER(l_column);
--
	c1outc_rec RECORD;
BEGIN
	IF (l_column IS NOT NULL AND l_column::text <> '') THEN
		OPEN c1outc(l_schema, l_table_name, l_column);
		FETCH c1outc INTO c1outc_rec;
		IF c1outc_rec.column_name IS NULL THEN
			PERFORM rif40_log_pkg.rif40_error(-20401, 'lp_outcomes_check_column', 'RIF40_OUTCOMES outcome type: % % column (%) not found in table %.%',
				l_outcome_type::VARCHAR 	/* Outcome type */,
				l_rif40_column::VARCHAR 	/* Column name in RIF40_OUTCOMES */,
				l_column::VARCHAR  		/* Column (value in above) */,
				l_schema::VARCHAR  		/* Schema */,
				l_table_name::VARCHAR		/* Table */);
			CLOSE c1outc;
		END IF;
		CLOSE c1outc;
	END IF;
END;
$func$
LANGUAGE 'plpgsql';
COMMENT ON FUNCTION rif40_trg_pkg.lp_outcomes_check_column(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) IS 'Function: 	lp_outcomes_check_column()
Parameters:	Outcome type, Column name in RIF40_OUTCOMES, Column (value in above), table, column [both being checked]
Returns:	NONE
Description:	Check outcome type exists';

\df rif40_trg_pkg.lp_outcomes_check_column_check

GRANT EXECUTE ON FUNCTION rif40_trg_pkg.lp_outcomes_check_column(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_trg_pkg.lp_outcomes_check_column(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rif_user, rif_manager;

--
-- Execute
--
\set VERBOSITY terse
DO LANGUAGE plpgsql $$
BEGIN
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_ddl:DEBUG1'); /* SQL statements - timing DEBUG4 */
	PERFORM rif40_log_pkg.rif40_add_to_debug('rif40_db_name_check:DEBUG1'); /* SQL statements - timing DEBUG4 */
	PERFORM rif40_log_pkg.rif40_add_to_debug('create_instead_of_triggers:DEBUG1'); /* SQL statements - timing DEBUG4 */
        PERFORM rif40_log_pkg.rif40_log_setup();
        PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
	PERFORM rif40_trg_pkg.drop_instead_of_triggers();
	PERFORM rif40_trg_pkg.create_instead_of_triggers();
	PERFORM rif40_trg_pkg.rif40_db_name_check('TEST', 'AA123');
END;
$$;

\dS+ rif40_comparison_areas
\dft trg_rif40_comparison_areas
\df rif40_trg_pkg.trgf_rif40_comparison_areas
\dd trg_rif40_comparison_areas
\dd rif40_trg_pkg.trgf_rif40_comparison_areas
\dd+ rif40_comparison_areas

--
-- Create any tables with views without INSTEAD OF triggers (e.g. rif40_parameters)
--
WITH t AS (	/* Existing triggers */
	SELECT tgname AS trigger_name, 
	       c.relname,
	       n2.nspname||'.'||p.proname||rif40_sql_pkg.rif40_get_function_arg_types(p.proargtypes) AS function_name
 	  FROM pg_views v, pg_class c, pg_trigger b, pg_proc p, pg_namespace n2
	 WHERE b.tgrelid        = c.oid				
           AND NOT b.tgisinternal				/* Ignore constraints */
	   AND b.tgfoid         = p.oid				/* Trigger function */
	   AND n2.oid           = p.pronamespace		/* Function schema */
	   AND c.relowner      IN (SELECT oid FROM pg_roles WHERE rolname = USER)	/* RIF40 tables */
	   AND c.relname        = v.viewname			/* Views only */
	   AND c.relkind        = 'v' 				/* Relational table */
), v AS	(	/* table/view pairs */
	SELECT t.tablename, v.viewname
	  FROM pg_tables t, pg_views v
	 WHERE t.tableowner     = USER	/* RIF40 tables */
	   AND v.viewowner      = USER
	   AND 't_'||v.viewname = t.tablename
)
SELECT v.tablename, v.viewname, t.trigger_name
  FROM v
	LEFT OUTER JOIN t ON (t.relname = v.viewname)
 WHERE viewname NOT IN ('rif40_num_denom', 'rif40_projects', 'rif40_geolevels') /* These views cannot be inserted into */
 ORDER BY 1, 2;

CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_parameters()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $trigger_function$
BEGIN
        IF TG_OP = 'INSERT' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
                IF (USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
                        INSERT INTO t_rif40_parameters (
                                param_name,
                                param_value,
                                param_description)
                        VALUES(
                                NEW.param_name,
                                NEW.param_value,
                                NEW.param_description);
                ELSE
                        PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_parameters',
                                'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
                END IF;
                RETURN NEW;
        ELSIF TG_OP = 'UPDATE' THEN
--
-- Cannot update SuppressionValue or RifParametersTable
--
                IF NEW.param_name NOT IN ('SuppressionValue', 'RifParametersTable') THEN
                        UPDATE t_rif40_parameters
                           SET param_value=NEW.param_value,
                               param_description=NEW.param_description;
                ELSE
                        PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_parameters',
                                'Cannot UPDATE: RIF40_PARAMETERS param_name  % (=%)', NEW.param_name::VARCHAR, NEW.param_value::VARCHAR);
                END IF;
                RETURN NEW;
        ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
                PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_parameters',
                    	'Cannot DELETE RIF40_PARAMETERS records');
                RETURN NULL;
        END IF;
        RETURN NEW;
END;
$trigger_function$;

CREATE TRIGGER trg_rif40_parameters INSTEAD OF INSERT OR UPDATE OR DELETE ON rif40_parameters FOR EACH ROW EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_parameters();

COMMENT ON TRIGGER trg_rif40_parameters ON rif40_parameters IS 'INSTEAD OF trigger for view T_RIF40_PARAMETERS to allow INSERT/UPDATE by the RIF manager. Update not allowed on parameters: SuppressionValue or RifParametersTable';

COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_parameters() IS 'INSTEAD OF trigger for view T_RIF40_PARAMETERS to allow INSERT/UPDATE by the RIF manager. Update not allowed on parameters: SuppressionValue or RifParametersTable';

GRANT INSERT ON rif40_parameters TO rif_manager;
GRANT UPDATE ON rif40_parameters TO rif_manager;

/*
BEGIN;
INSERT INTO rif40_comparison_areas(username, study_id, area_id) VALUES (NULL, 123456, 'XXYY');
ROLLBACK;
 */
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		WITH t AS (	/* Existing triggers */
			SELECT tgname AS trigger_name, 
			       c.relname,
			       n2.nspname||'.'||p.proname||rif40_sql_pkg.rif40_get_function_arg_types(p.proargtypes) AS function_name
		 	  FROM pg_views v, pg_class c, pg_trigger b, pg_proc p, pg_namespace n2
			 WHERE b.tgrelid        = c.oid				
		           AND NOT b.tgisinternal				/* Ignore constraints */
			   AND b.tgfoid         = p.oid				/* Trigger function */
			   AND n2.oid           = p.pronamespace		/* Function schema */
			   AND c.relowner      IN (SELECT oid FROM pg_roles WHERE rolname = USER)	/* RIF40 tables */
			   AND c.relname        = v.viewname			/* Views only */
			   AND c.relkind        = 'v' 				/* Relational table */
		), v AS	(	/* table/view pairs */
			SELECT t.tablename, v.viewname
			  FROM pg_tables t, pg_views v
			 WHERE t.tableowner     = USER	/* RIF40 tables */
			   AND v.viewowner      = USER
			   AND 't_'||v.viewname = t.tablename
		)
		SELECT v.tablename, v.viewname
		  FROM v
			LEFT OUTER JOIN t ON (t.relname = v.viewname)
 		WHERE viewname NOT IN ('rif40_num_denom', 'rif40_projects', 'rif40_geolevels') /* These views cannot be inserted into */
 		  AND  t.trigger_name IS NULL
 		ORDER BY 1, 2;
	c1_rec RECORD;
--
	i INTEGER:=0;
BEGIN
	FOR c1_rec IN c1 LOOP
		RAISE WARNING 'Found view: % without INSTEAD OF trigger', c1_rec.viewname;
		i:=i+1;
	END LOOP;
--
	IF i > 0 THEN
		RAISE EXCEPTION 'Found % view(s) without INSTEAD OF triggers', i;
	ELSE
		RAISE INFO 'All views requiring INSTEAD OF triggers have them';
	END IF;
END;
$$;
\set VERBOSITY default

\echo Created PG psql code (INSTEAD OF triggers for views with USERNAME as a column).
--
-- Eof
