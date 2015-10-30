-- ************************************************************************
-- *
-- * DO NOT EDIT THIS SCRIPT OR MODIFY THE RIF SCHEMA - USE ALTER SCRIPTS
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
-- Rapid Enquiry Facility (RIF) - Partition all tables with study_id as a column
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
-- Check database is sahsuland_dev
--
DO LANGUAGE plpgsql $$
BEGIN
	IF current_database() = 'sahsuland_dev' THEN
		RAISE INFO 'Database check: %', current_database();	
	ELSE
		RAISE EXCEPTION 'C20901: Database check failed: % is not sahsuland_dev', current_database();	
	END IF;
END;
$$;

\echo Partition all tables with study_id as a column...

\set VERBOSITY terse
--\set VERBOSITY verbose
DO LANGUAGE plpgsql $$
DECLARE
--
	rif40_sql_pkg_functions 	VARCHAR[] := ARRAY['rif40_hash_partition',
							'_rif40_hash_partition_create',
							'_rif40_common_partition_create',
							'_rif40_common_partition_create_setup',
							'_rif40_hash_partition_create_insert',
							'_rif40_common_partition_create_complete',
							'_rif40_common_partition_triggers',
							'rif40_method4',
							'rif40_ddl'];
	l_function 			VARCHAR;
--
	c1 CURSOR FOR
		WITH d AS (
			SELECT ARRAY_AGG(a.tablename) AS table_list
			  FROM pg_tables a, pg_attribute b, pg_class c
	 		 WHERE c.oid        = b.attrelid
			   AND c.relname    = a.tablename
  		       AND c.relkind    = 'r' /* Relational table */
		       AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		       AND b.attname    = 'study_id'
		       AND a.schemaname = 'rif40'	
		)
		SELECT a.tablename AS tablename, b.attname AS columnname, schemaname AS schemaname, 
			   d.table_list	/* Tables */, c.relhassubclass
		  FROM pg_tables a, pg_attribute b, pg_class c, d
		 WHERE c.oid        = b.attrelid
		   AND c.relname    = a.tablename
		   AND c.relkind    = 'r' /* Relational table */
		   AND c.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */ 
		   AND b.attname    = 'study_id'
		   AND a.schemaname = 'rif40'
		 ORDER BY 1;
	c2 CURSOR FOR
		WITH c AS (   
			SELECT cn.nspname AS schema_child, c.relname AS child, pn.nspname AS schema_parent, p.relname AS parent
			FROM pg_attribute b, pg_inherits 
					LEFT OUTER JOIN pg_class AS c ON (inhrelid=c.oid)
					LEFT OUTER JOIN pg_class as p ON (inhparent=p.oid)
					LEFT OUTER JOIN pg_namespace pn ON pn.oid = p.relnamespace
					LEFT OUTER JOIN pg_namespace cn ON cn.oid = c.relnamespace
			WHERE cn.nspname = 'rif40_partitions'
			  AND p.relkind  = 'r' /* Relational table */
			  AND p.relpersistence IN ('p', 'u') /* Persistence: permanent/unlogged */  
			  AND p.oid      = b.attrelid
			  AND b.attname  = 'study_id'
		), b AS (
			SELECT child AS table_name
			FROM c
			UNION
			SELECT parent
			FROM c
		), y AS (
			SELECT ARRAY_AGG(table_name) AS table_list
			  FROM b
		)
		SELECT 'ALTER TABLE '||z.relname||' ENABLE TRIGGER '||t.tgname AS sql_stmt
		  FROM pg_catalog.pg_trigger t, pg_class z
		 WHERE t.tgrelid = z.oid
		   AND z.relname IN (SELECT UNNEST(y.table_list) FROM y)
		   AND NOT t.tgisinternal
		   AND t.tgenabled = 'D'
		ORDER BY 1; 
--
	c1_rec 		RECORD;
	c2_rec 		RECORD;	
--
	sql_stmt 	VARCHAR[];
	num_fks 	INTEGER:=0;
	i 			INTEGER:=0;
--
	l_fk_stmt	VARCHAR[];
	fk_stmt	VARCHAR[];	
	table_list VARCHAR[];
--
-- 16 takes too long (~40 mins!). This needs to be parameterisable
-- 2=11, 4=14, 8=2?
--
	number_of_partitions CONSTANT INTEGER:=2;
--
	error_message 		VARCHAR;
	v_detail 		VARCHAR:='(Not supported until 9.2; type SQL statement into psql to see remote error)';
BEGIN
--
-- Check user is rif40
--
	IF user = 'rif40' THEN
		RAISE INFO 'v4_0_study_id_partitions.sql: User check: %', user;	
	ELSE
		RAISE EXCEPTION 'v4_0_study_id_partitions.sql: C209xx: User check failed: % is not rif40', user;	
	END IF;
--
-- Turn on some debug
--
    PERFORM rif40_log_pkg.rif40_log_setup();
    PERFORM rif40_log_pkg.rif40_send_debug_to_info(TRUE);
--
-- Enabled debug on select rif40_sm_pkg functions
--
	FOREACH l_function IN ARRAY rif40_sql_pkg_functions LOOP
		RAISE INFO 'v4_0_study_id_partitions.sql: Enable debug for function: %', l_function;
		PERFORM rif40_log_pkg.rif40_add_to_debug(l_function||':DEBUG1');
	END LOOP;
--
-- Enable hash partitioning on each table in turn
--
	FOR c1_rec IN c1 LOOP
--
-- Check if partitioned already
--
		IF c1_rec.relhassubclass THEN
			RAISE NOTICE 'v4_0_study_id_partitions.sql: Table %.% is already partitioned', 
				c1_rec.schemaname::VARCHAR, c1_rec.tablename::VARCHAR;
		ELSE	
			i:=i+1;
--
			RAISE INFO '%********************************************************************************%*%* Hash partitioning[%]: %.% %*%********************************************************************************', 
				E'\n', E'\n', E'\n', i::VARCHAR, c1_rec.schemaname, c1_rec.tablename, E'\n', E'\n';
			l_fk_stmt:=NULL;
			l_fk_stmt:=rif40_sql_pkg.rif40_hash_partition(c1_rec.schemaname::VARCHAR, 
				c1_rec.tablename::VARCHAR, 'study_id', c1_rec.table_list::VARCHAR[], number_of_partitions);
			IF fk_stmt IS NULL AND l_fk_stmt IS NOT NULL THEN	
				table_list:=c1_rec.table_list;
				num_fks:=num_fks+array_length(l_fk_stmt, 1);	
				fk_stmt:=l_fk_stmt;
			ELSIF fk_stmt IS NOT NULL AND l_fk_stmt IS NOT NULL THEN
				num_fks:=num_fks+array_length(l_fk_stmt, 1);
				fk_stmt:=array_cat(fk_stmt, l_fk_stmt);
			END IF;
			RAISE INFO '%********************************************************************************%*%* Hash partitioning[%] complete: %.% %*%********************************************************************************', 
				E'\n', E'\n', E'\n', i::VARCHAR, c1_rec.schemaname, c1_rec.tablename, E'\n', E'\n';
		END IF;
	END LOOP;
	
--
-- Put back foreign keys, e.g.
--
/*
CREATE TRIGGER t_rif40_investigations_p16_checks BEFORE INSERT OR UPDATE OF username, inv_name, inv_description, year_start, year_stop, max_age_group, min_age_group, genders, numer_tab, investigation_state, geography, study_id, inv_id, classifier, classifier_bands, mh_test_type ON t_rif40_investigations_p16 FOR EACH ROW WHEN ((((((((((((((((((new.username IS NOT NULL) AND ((new.username)::text <> ''::text)) OR ((new.inv_name IS NOT NULL) AND ((new.inv_name)::text <> ''::text))) OR ((new.inv_description IS NOT NULL) AND ((new.inv_description)::text <> ''::text))) OR ((new.year_start IS NOT NULL) AND ((new.year_start)::text <> ''::text))) OR ((new.year_stop IS NOT NULL) AND ((new.year_stop)::text <> ''::text))) OR ((new.max_age_group IS NOT NULL) AND ((new.max_age_group)::text <> ''::text))) OR ((new.min_age_group IS NOT NULL) AND ((new.min_age_group)::text <> ''::text))) OR ((new.genders IS NOT NULL) AND ((new.genders)::text <> ''::text))) OR ((new.investigation_state IS NOT NULL) AND ((new.investigation_state)::text <> ''::text))) OR ((new.numer_tab IS NOT NULL) AND ((new.numer_tab)::text <> ''::text))) OR ((new.geography IS NOT NULL) AND ((new.geography)::text <> ''::text))) OR ((new.study_id IS NOT NULL) AND ((new.study_id)::text <> ''::text))) OR ((new.inv_id IS NOT NULL) AND ((new.inv_id)::text <> ''::text))) OR ((new.classifier IS NOT NULL) AND ((new.classifier)::text <> ''::text))) OR ((new.classifier_bands IS NOT NULL) AND ((new.classifier_bands)::text <> ''::text))) OR ((new.mh_test_type IS NOT NULL) AND ((new.mh_test_type)::text <> ''::text)))) EXECUTE PROCEDURE rif40_trg_pkg.trigger_fct_t_rif40_investigations_checks();
 */
-- 
--
	IF fk_stmt IS NOT NULL THEN
	
		RAISE INFO '%Tables: % % %**************** FOREIGN KEYS **********************%', 
			E'\n', array_to_string(table_list, ','||E'\n'), E'\n', E'\n', E'\n';
		FOR i IN array_lower(fk_stmt, 1) .. array_upper(fk_stmt, 1) LOOP
			RAISE INFO 'SQL> %;', fk_stmt[i];
		END LOOP;
		RAISE INFO '%**************** FOREIGN KEYS **********************%', E'\n', E'\n';		
--		RAISE EXCEPTION 'Stop 1';
		BEGIN
			PERFORM rif40_sql_pkg.rif40_ddl(fk_stmt);
--			RAISE EXCEPTION 'Stop 2';
		EXCEPTION
			WHEN others THEN
--
-- Catch foreign key errors:
--
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:145: INFO:  [DEBUG1] rif40_ddl(): SQL> ALTER TABLE rif40.rif40_study_shares_p10
--       ADD CONSTRAINT /* Add support for local partitions */ rif40_study_shares_p10_study_id_fk FOREIGN KEY (study_id) REFERENCES t_rif40_studies_p10(study_id)
-- /* Referenced foreign key table: rif40.rif40_study_shares_p10 has partitions: false, is a partition: true */
-- /* Referenced foreign key partition: 10 of 16 */;
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:145: WARNING:  rif40_ddl(): SQL in error (42830)> ALTER TABLE rif40.rif40_study_shares_p10
--        ADD CONSTRAINT /* Add support for local partitions */ rif40_study_shares_p10_study_id_fk
-- FOREIGN KEY (study_id) REFERENCES t_rif40_studies_p10(study_id)
-- /* Referenced foreign key table: rif40.rif40_study_shares_p10 has partitions: false, is a partition: true */
-- /* Referenced foreign key partition: 10 of 16 */;
-- psql:../psql_scripts/v4_0_study_id_partitions.sql:145: ERROR:  there is no unique constraint matching given keys for referenced table "t_rif40_studies_p10"
-- Time: 205205.927 ms
--
-- [this is caused by a missing PRIMARY KEY on t_rif40_studies_p10]
--
				GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
				error_message:='v4_0_study_id_partitions.sql: caught in rif40_ddl(fk_stmt): '::VARCHAR||E'\n'::VARCHAR||
					SQLERRM::VARCHAR||' in SQL> '::VARCHAR||sql_stmt::VARCHAR||E'\n'::VARCHAR||'Detail: '::VARCHAR||v_detail::VARCHAR;
				RAISE INFO '2: %', error_message;
--
				RAISE;
		END;
		
--
-- Enable all DISABLED triggers
--
		FOR c2_rec IN c2 LOOP
			BEGIN
				PERFORM rif40_sql_pkg.rif40_ddl(c2_rec.sql_stmt);
			EXCEPTION
				WHEN others THEN
	--
					GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
					error_message:='v4_0_study_id_partitions.sql: caught in rif40_ddl(ENABLE triggers): '::VARCHAR||E'\n'::VARCHAR||
						SQLERRM::VARCHAR||' in SQL> '::VARCHAR||sql_stmt::VARCHAR||E'\n'::VARCHAR||'Detail: '::VARCHAR||v_detail::VARCHAR;
					RAISE INFO '2: %', error_message;
	--
					RAISE;			
			END;
		END LOOP;	
		
	END IF;	
	RAISE INFO 'v4_0_study_id_partitions.sql: % hash partitions created % foreign keys', i::VARCHAR, num_fks::VARCHAR;
--	RAISE EXCEPTION 'Stop 3';
END;
$$;

--
-- Fix views to include hash_partition_number
--
\i ../views/rif40_comparision_areas.sql
\i ../views/rif40_contextual_stats.sql

--
-- Re-create instead of triggers
--

--
-- Check all view for t_ tables now have hash_partition_number
--

\echo Partitioning of all tables with study_id as a column complete.
--
-- Eof

