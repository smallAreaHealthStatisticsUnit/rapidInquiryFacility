-- ************************************************************************
-- *
-- * THIS SCRIPT MAY BE EDITED - NO NEED TO USE ALTER SCRIPTS
-- *
-- ************************************************************************
--
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
-- Rapid Enquiry Facility (RIF) - Create instead of triggers
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
	k INTEGER:=0;
	l INTEGER:=0;
	m INTEGER:=0;
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
		m:=0;
		FOR k IN array_lower(role_list, 1) .. array_upper(role_list, 1) LOOP
			FOR l IN array_lower(priv_list, 1) .. array_upper(priv_list, 1) LOOP
				IF has_table_privilege(role_list[k], c1ciot_rec.tablename, priv_list[l]) THEN
					j:=j+1;
					sql_stmt[j]:='GRANT '||priv_list[l]||' ON '||c1ciot_rec.viewname||' TO '||role_list[k];
					m:=m+1;
				ELSE
					PERFORM rif40_log_pkg.rif40_log('WARNING', 'create_instead_of_triggers', 
                		'No % privilege for role % on table: %', 
						priv_list[l]::VARCHAR		/* Privilege */,
						role_list[k]::VARCHAR		/* Role */,
						c1ciot_rec.tablename::VARCHAR);	
				END IF;
			END LOOP;
		END LOOP;	
		
--
-- Execute SQL to create INSTEAD trigger for VIEW
--
		PERFORM rif40_sql_pkg.rif40_ddl(sql_stmt);
		
--
-- Check there were grants
--
		IF m != 0 THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'create_instead_of_triggers', 
				'Creating INSTEAD OF function and trigger for: %; % grants', 
				c1ciot_rec.viewname::VARCHAR	/* table name */,
				m::VARCHAR						/* Number of grants */);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'create_instead_of_triggers',
            	'Cannot create INSTEAD OF triggers; no grants for table: %',
				c1ciot_rec.tablename::VARCHAR);
		END IF;		
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

--
-- Eof
