-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
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
-- Rapid Enquiry Facility (RIF) - RIF alter script 7 - Support for ontologies (e.g. ICD9, 10); removed previous table based support.
--												       Modify t_rif40_inv_conditions to remove SQL injection risk
--
-- Rapid Enquiry Facility (RIF) - RIF alter script 4 - Hash partitioning
--
-- Alter script 4 must go after 7 - This is because 7 was written before the partitioning was enabled and does 
-- not support it
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

\echo Running SAHSULAND schema alter script #7 ontology support.

/*

Alter script #7

Support for taxonomies/ontologies (e.g. ICD9, 10); removed previous table based support.
Modify t_rif40_inv_conditions to remove SQL injection risk

Done:

* rif40_outcomes - list of ontologies to remain - remove all field except for:
	outcome_type, outcome_description, current_version, current_sub_version, previous_version
* Add new outcome_group to rif40_outcome_groups for SAHUSLAND_CANCER
* Fix rif40_tables, rif40_table_outcomes, rif40_outcome_groups join for SAHUSLAND_CANCER
  - Add view: rif40_numerator_outcome_columns
  - Add checks: to rif40_inv_conditions, rif40_num_denom, rif40_num_denom_errors
* Drop existing ontology tables (keep icd9/10 until new ontology middleware is ready)
* Modify t_rif40_inv_conditions to remove SQL injection risk:
  - Rename column condition to min_condition
  - Add columns: max_condition, predefined_group_name, outcome_group_name
  - Add foreign key constraint on rif40_predefined_groups(predefined_group_name)
  - Add foreign key constraint on rif40_outcome_groups(outcome_group_name)
  - Add check constraints: 
    1. min_condition or predefined_group_name
    2. max_condition may be null, but if set != min_condition
* Rebuild rif40_inv_conditions:
  - Add back condition, derive from: min_condition, max_condition, predefined_group_name, outcome_group_name
  - Add numer_tab, field_name, column_exists and column_comments fields for enhanced information
 * Load new rif40_create_disease_mapping_example()
 * Load new rif40_startup() with ability to disable most checks for middleware testing
   Note: must be run once to create TEMPORARY TABLES
   
*/
   
BEGIN;

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
-- outcome_type |   outcome_group_name    |                      outcome_group_description                       |  field_name   | multiple_field_count
-- -------------+-------------------------+----------------------------------------------------------------------+---------------+----------------------
-- ICD          | SAHSULAND_ICD              | Single ICD                                                           | ICD           |                    0
--
INSERT INTO rif40_outcome_groups(outcome_type, outcome_group_name, outcome_group_description, field_name, multiple_field_count)
SELECT 'ICD', 'SAHSULAND_ICD', 'Single ICD', 'ICD', 0
WHERE NOT EXISTS (
	SELECT 1 
	  FROM rif40_outcome_groups
	 WHERE outcome_group_name = 'SAHSULAND_ICD');

--
-- Fix rif40_tables, rif40_table_outcomes, rif40_outcome_groups join for SAHUSLAND_CANCER
--
UPDATE rif40_table_outcomes
   SET outcome_group_name = 'SAHSULAND_ICD'
 WHERE outcome_group_name = 'SINGLE_ICD'
   AND numer_tab          = 'SAHSULAND_CANCER';
   
\dS+ rif40_outcome_groups

--
-- Add rif40_num_denom manually for rif40 user only so that rif40_numerator_outcome_columns can be built.
-- rif40_numerator_outcome_columns uses the users own rif40_num_denom
--
CREATE OR REPLACE VIEW rif40.rif40_num_denom AS
WITH n AS (
         SELECT n1.geography,
            n1.numerator_table,
            n1.numerator_description,
            n1.automatic,
            n1.theme_description
           FROM ( SELECT g.geography,
                    n_1.table_name AS numerator_table,
                    n_1.description AS numerator_description,
                    n_1.automatic,
                    t.description AS theme_description
                   FROM rif40_geographies g,
                    rif40_tables n_1,
                    rif40_health_study_themes t
                  WHERE n_1.isnumerator = 1 AND n_1.automatic = 1 AND rif40_is_object_resolvable(n_1.table_name) = 1 AND n_1.theme::text = t.theme::text) n1
          WHERE rif40_num_denom_validate(n1.geography, n1.numerator_table) = 1
        ), d AS (
         SELECT d1.geography,
            d1.denominator_table,
            d1.denominator_description
           FROM ( SELECT g.geography,
                    d_1.table_name AS denominator_table,
                    d_1.description AS denominator_description
                   FROM rif40_geographies g,
                    rif40_tables d_1
                  WHERE d_1.isindirectdenominator = 1 AND d_1.automatic = 1 AND rif40_is_object_resolvable(d_1.table_name) = 1) d1
          WHERE rif40_num_denom_validate(d1.geography, d1.denominator_table) = 1 AND rif40_auto_indirect_checks(d1.denominator_table) IS NULL
        )
 SELECT n.geography,
    n.numerator_table,
    n.numerator_description,
    n.theme_description,
    d.denominator_table,
    d.denominator_description,
    n.automatic
   FROM n,
    d
  WHERE n.geography::text = d.geography::text
  ORDER BY n.geography, n.numerator_table, n.theme_description;

ALTER TABLE rif40.rif40_num_denom
    OWNER TO rif40;
COMMENT ON VIEW rif40.rif40_num_denom
    IS 'Numerator and indirect standardisation denominator pairs. Use RIF40_NUM_DENOM_ERROR if your numerator and denominator table pair is missing. You must have your own copy of RIF40_NUM_DENOM or you will only see the tables RIF40 has access to. Tables not rejected if the user does not have access or the table does not contain the correct geography geolevel fields.';
   
CREATE OR REPLACE VIEW rif40_numerator_outcome_columns   
AS
WITH a AS (
	SELECT z.geography, 
	       a.table_name, 
	       c.outcome_group_name, c.outcome_type, c.outcome_group_description, c.field_name, c.multiple_field_count
	  FROM rif40_num_denom z, rif40_tables a, rif40_table_outcomes b, rif40_outcome_groups c
	 WHERE a.table_name         = z.numerator_table
	   AND a.table_name         = b.numer_tab
       AND c.outcome_group_name = b.outcome_group_name
)
SELECT a.*, 
       CASE WHEN d.attrelid IS NOT NULL THEN true ELSE false END columnn_exists,
	   CASE WHEN d.attrelid IS NOT NULL THEN col_description(LOWER(a.table_name)::regclass, d.attnum) ELSE NULL END AS column_comment
  FROM a
  	LEFT OUTER JOIN pg_attribute d ON (LOWER(a.table_name)::regclass = d.attrelid AND d.attname = LOWER(a.field_name));

COMMENT ON VIEW rif40_numerator_outcome_columns IS 'All numerator outcome fields (columns) ';	
COMMENT ON COLUMN rif40_numerator_outcome_columns.geography IS 'Geography';
COMMENT ON COLUMN rif40_numerator_outcome_columns.table_name IS 'Numerator table name';
COMMENT ON COLUMN rif40_numerator_outcome_columns.outcome_type IS 'Outcome type: ICD, ICD-0 or OPCS';
COMMENT ON COLUMN rif40_numerator_outcome_columns.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD';
COMMENT ON COLUMN rif40_numerator_outcome_columns.outcome_group_description IS 'Outcome Group Description. E.g. &quot;Single variable ICD&quot;';
COMMENT ON COLUMN rif40_numerator_outcome_columns.field_name IS 'Numerator table outcome field name, e.g. ICD_SAHSU_01, ICD_SAHSU';
COMMENT ON COLUMN rif40_numerator_outcome_columns.multiple_field_count 
	IS 'Outcome Group multiple field count (0-99). E.g if NULL then field is ICD_SAHSU_01; if 20 then fields are ICD_SAHSU_01 to ICD_SAHSU_20. Field numbers are assumed to tbe left padded to 2 characters with &quot;0&quot; and preceeded by an &quot;_&quot;';
COMMENT ON COLUMN rif40_numerator_outcome_columns.columnn_exists IS 'Numerator table outcome columnn exists';
COMMENT ON COLUMN rif40_numerator_outcome_columns.column_comment IS 'Numerator table outcome column comment';
GRANT SELECT ON rif40_numerator_outcome_columns TO rif_manager, rif_user;

SELECT * FROM rif40_numerator_outcome_columns;

--     table_name    | outcome_group_name | outcome_type | outcome_group_name | outcome_group_description | field_name | multiple_field_count | columnn_exists
-- ------------------+--------------------+--------------+--------------------+---------------------------+------------+----------------------+----------------
--  SAHSULAND_CANCER | SAHSULAND_ICD      | ICD          | SAHSULAND_ICD      | Single ICD                | ICD        |                    0 | t
-- (1 row)
--

--
-- Add checks: to rif40_inv_conditions, rif40_num_denom, rif40_num_denom_errors
--	
 
--
-- rif40_outcomes - list of ontologies to remain - remove all field except for:
--	outcome_type, outcome_description, current_version, current_sub_version, previous_version
--
\dS+ rif40_outcomes

SELECT * FROM rif40_outcomes;

--
-- Drop validation triggers
--
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_rif40_outcomes_checks.sql

DO LANGUAGE plpgsql $$
DECLARE
	c1_a7 CURSOR FOR
		SELECT column_name 
		  FROM information_schema.columns 
		 WHERE table_name  = 'rif40_outcomes' 
		   AND column_name = 'current_lookup_table';
	c1_rec RECORD;
BEGIN
--
-- Check if column exists
--
	OPEN c1_a7;
	FETCH c1_a7 INTO c1_rec;
	CLOSE c1_a7;
--
	IF c1_rec.column_name = 'current_lookup_table' THEN
		ALTER TABLE rif40_outcomes DROP CONSTRAINT IF EXISTS previous_value_nchar_ck RESTRICT;
		ALTER TABLE rif40_outcomes DROP CONSTRAINT IF EXISTS previous_lookup_table_ck RESTRICT;
		UPDATE rif40_outcomes
		   SET current_lookup_table = NULL,
               previous_lookup_table = NULL,
               current_value_1char = NULL,
               current_value_2char = NULL,
               current_value_3char = NULL,
               current_value_4char = NULL,
               current_value_5char = NULL,
               current_description_1char = NULL,
               current_description_2char = NULL,
               current_description_3char = NULL,
               current_description_4char = NULL,
               current_description_5char = NULL,
               previous_value_1char = NULL,
               previous_value_2char = NULL,
               previous_value_3char = NULL,
               previous_value_4char = NULL,
               previous_value_5char = NULL,
               previous_description_1char = NULL,
               previous_description_2char = NULL,
               previous_description_3char = NULL,
               previous_description_4char = NULL,
               previous_description_5char = NULL;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_lookup_table RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_lookup_table RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_value_1char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_value_2char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_value_3char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_value_4char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_value_5char RESTRICT;		
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_description_1char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_description_2char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_description_3char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_description_4char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS current_description_5char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_value_1char RESTRICT;		
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_value_2char RESTRICT;		
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_value_3char RESTRICT;		
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_value_4char RESTRICT;		
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_value_5char RESTRICT;		
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_description_1char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_description_2char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_description_3char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_description_4char RESTRICT;
		ALTER TABLE rif40_outcomes DROP COLUMN IF EXISTS previous_description_5char RESTRICT;	
	ELSE
		RAISE INFO 'v4_0_alter_7.sql: rif40_outcomes already upgraded.';		
	END IF;
END;
$$;

\dS+ rif40_outcomes

SELECT * FROM rif40_outcomes;

--
-- Drop existing ontology tables (keep icd9/10 until new ontology middleware is ready)
--
-- DROP TABLE IF EXISTS rif40_icd9;
-- DROP TABLE IF EXISTS rif40_icd10;
DROP TABLE IF EXISTS rif40_icd_o_3;
DROP TABLE IF EXISTS rif40_opcs4;
DROP TABLE IF EXISTS rif40_a_and_e;

--
-- Convert condition into outcome group condition
-- Add outcome_group_name, FK to rif40_outcome_groups.outcome_group_name
--
\dS+ t_rif40_inv_conditions
SELECT * FROM t_rif40_inv_conditions LIMIT 20;
--
--  inv_id | study_id | username | line_number |               condition
--   ------+----------+----------+-------------+----------------------------------------
--       1 |        1 | peter    |           1 | "icd" LIKE 'C34%' OR "icd" LIKE '162%'
--(1 row)
--
DO LANGUAGE plpgsql $$
DECLARE
	c1_a7 CURSOR FOR
		SELECT column_name 
		  FROM information_schema.columns 
		 WHERE table_name  = 't_rif40_inv_conditions' 
		   AND column_name IN ('condition', 'min_condition');
	c2_a7 CURSOR FOR
		SELECT COUNT(DISTINCT(inv_id)) AS investigations, 
		       COUNT(line_number) AS lines
		  FROM t_rif40_inv_conditions;
	c3_a7 CURSOR FOR
		SELECT *
		  FROM t_rif40_inv_conditions
		 WHERE study_id    = 1
  		   AND inv_id      = 1
 		   AND line_number = 1
		   AND condition   = '"icd" LIKE ''C34%'' OR "icd" LIKE ''162%''';
--
	c1_rec RECORD;		   
	c2_rec RECORD;
	c3_rec RECORD;
BEGIN
--
-- Check if t_rif40_inv_conditions has data in the correct form (i.e. test_4 only)
--
	OPEN c2_a7;
	FETCH c2_a7 INTO c2_rec;
	CLOSE c2_a7;	
	OPEN c1_a7;
	FETCH c1_a7 INTO c1_rec;
	CLOSE c1_a7;		
--
-- This will need to be improved to handle V3.1 rif upgraded data
--
	IF c2_rec.investigations BETWEEN 0 AND 1 AND c2_rec.lines BETWEEN 0 AND 2 AND c1_rec.column_name = 'condition' THEN
		IF c2_rec.investigations = 0 AND c2_rec.lines = 0 AND c1_rec.column_name = 'condition' THEN		
			RAISE INFO 'v4_0_alter_7.sql: t_rif40_inv_conditons has been TRUNCATED; can be upgraded.';	
		END IF;
--
		OPEN c3_a7;
	    FETCH c3_a7 INTO c3_rec;
	    CLOSE c3_a7;
--
-- Remove ON DELETE trigger
--
		DROP TRIGGER IF EXISTS t_rif40_inv_conditions_checks ON t_rif40_inv_conditions CASCADE;
--
-- Remove view
--
		DROP VIEW IF EXISTS rif40_inv_conditions;
--
-- Remove old test data
--	
		DELETE FROM t_rif40_inv_conditions
		WHERE study_id    = 1
		  AND inv_id      = 1
		  AND line_number = 1 
		  AND condition   = '"icd" LIKE ''C34%'' OR "icd" LIKE ''162%''';
		   
--		   
-- Modify t_rif40_inv_conditions to remove SQL injection risk:
--   - Rename column condition to min_condition
--   - Add columns: max_condition, predefined_group_name, outcome_group_name
--   - Add foreign key constraint on rif40_predefined_groups(predefined_group_name)
--   - Add foreign key constraint on rif40_outcome_groups(outcome_group_name)
--   - Add check constraints: 
--     1. min_condition or predefined_group_name
--     2. max_condition may be null, but if set != min_condition
--     
		ALTER TABLE t_rif40_inv_conditions RENAME COLUMN condition TO min_condition;
		ALTER TABLE t_rif40_inv_conditions ALTER COLUMN min_condition 		SET DATA TYPE VARCHAR(5),
										  ALTER COLUMN min_condition 		DROP 		NOT NULL,
										  ADD COLUMN max_condition 			VARCHAR(5) 	NULL,
										  ADD COLUMN predefined_group_name 	VARCHAR(5) 	NULL,										  
										  ADD COLUMN outcome_group_name 	VARCHAR(30) NOT NULL,								  
										  ADD CONSTRAINT t_rif40_inv_conditons_pgn 
												FOREIGN KEY (predefined_group_name) REFERENCES rif40_predefined_groups(predefined_group_name),										  
										  ADD CONSTRAINT t_rif40_inv_conditons_ogn 
												FOREIGN KEY (outcome_group_name) REFERENCES rif40_outcome_groups(outcome_group_name),
										  ADD CONSTRAINT predefined_group_name_ck 
											CHECK ((predefined_group_name IS NOT NULL AND min_condition IS NULL) OR
												   (predefined_group_name IS NULL AND min_condition IS NOT NULL)),
										  ADD CONSTRAINT max_condition_ck 
											CHECK ((predefined_group_name IS NULL AND max_condition IS NOT NULL AND min_condition IS NOT NULL AND max_condition != min_condition) OR
												   (predefined_group_name IS NULL AND max_condition IS NULL) OR
												   (predefined_group_name IS NOT NULL));

--
-- Rebuild rif40_inv_conditions:
-- Add back condition, derive from: min_condition, max_condition, predefined_group_name, outcome_group_name
-- Add numer_tab, field_name, column_exists and column_comments fields for enhanced information
--
		CREATE OR REPLACE VIEW rif40_inv_conditions AS 
		WITH a AS (
			SELECT c.username,
				   c.study_id,
				   c.inv_id,
				   c.line_number,
				   c.min_condition,
				   c.max_condition,
				   c.predefined_group_name,
				   c.outcome_group_name,
				   i.numer_tab,
				   b.field_name,
				   CASE 
						WHEN c.predefined_group_name IS NOT NULL THEN 
								(g.condition, '%', quote_ident(LOWER(b.field_name)))||
										' /* Pre defined group: '||g.predefined_group_description::Text||' */' 
						WHEN c.max_condition IS NOT NULL THEN
								quote_ident(LOWER(b.field_name))||' BETWEEN '''||
								c.min_condition||''' AND '''||c.max_condition||'~'' /* Range filter */' 
						ELSE 
								quote_ident(LOWER(b.field_name))||' LIKE '''||c.min_condition||'%'' /* Value filter */'
				   END AS condition
			  FROM t_rif40_investigations i, rif40_outcome_groups b, t_rif40_inv_conditions c
					LEFT OUTER JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
					LEFT OUTER JOIN rif40_predefined_groups g ON (c.predefined_group_name = g.predefined_group_name)
			 WHERE c.inv_id = i.inv_id AND c.study_id = i.study_id
			   AND c.outcome_group_name = b.outcome_group_name
			   AND (USER = 'rif40' OR c.username::name = USER OR 'RIF_MANAGER'::text = ((
					SELECT user_role_privs.granted_role
					  FROM user_role_privs
					 WHERE user_role_privs.granted_role = 'RIF_MANAGER'::text)) OR s.grantee_username IS NOT NULL AND s.grantee_username::text <> ''::text)
		)
		SELECT a.*, 
			   CASE WHEN d.attrelid IS NOT NULL THEN true ELSE false END columnn_exists,
			   col_description(LOWER(a.numer_tab)::regclass, d.attnum) AS column_comment
		  FROM a
			LEFT OUTER JOIN pg_attribute d ON (LOWER(a.numer_tab)::regclass = d.attrelid AND d.attname = LOWER(a.field_name)) 
		  ORDER BY a.username, a.inv_id;

		GRANT ALL ON TABLE rif40_inv_conditions TO rif40;
		GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_inv_conditions TO rif_user;
		GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_inv_conditions TO rif_manager;
		COMMENT ON VIEW rif40_inv_conditions
		  IS 'Lines of SQL conditions pertinent to an investigation.';
		COMMENT ON COLUMN rif40_inv_conditions.username IS 'Username';
		COMMENT ON COLUMN rif40_inv_conditions.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
		COMMENT ON COLUMN rif40_inv_conditions.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
		COMMENT ON COLUMN rif40_inv_conditions.line_number IS 'Line number';
		COMMENT ON COLUMN rif40_inv_conditions.min_condition 
			IS 'Minimum condition; if max condition is not null SQL WHERE Clause evaluates to: &quot;WHERE &lt;field_name&gt; LIKE ''&lt;min_condition&gt;%''&quot;.'; 	
		COMMENT ON COLUMN rif40_inv_conditions.max_condition 
			IS 'Maximum condition; if max condition is not null SQL WHERE Clause evaluates to: &quot;WHERE &lt;field_name&gt; BETWEEN ''&lt;min_condition&gt;'' AND ''&lt;max_condition&gt;~''&quot;.'; 	
		-- COMMENT ON COLUMN rif40_inv_conditions.field_name IS 'Numerator table outcome field name, e.g. ICD_SAHSU_01, ICD_SAHSU'; 			
		COMMENT ON COLUMN rif40_inv_conditions.predefined_group_name IS 'Predefined Group Name. E.g LUNG_CANCER'; 
		COMMENT ON COLUMN rif40_inv_conditions.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD'; 
		COMMENT ON COLUMN rif40_inv_conditions.numer_tab IS 'Numerator table'; 
		COMMENT ON COLUMN rif40_inv_conditions.columnn_exists IS 'Numerator table outcome columnn exists';
		COMMENT ON COLUMN rif40_inv_conditions.column_comment IS 'Numerator table outcome column comment';
		COMMENT ON COLUMN rif40_inv_conditions.field_name IS 'Numerator table outcome field name, e.g. ICD_SAHSU_01, ICD_SAHSU';
		COMMENT ON COLUMN rif40_inv_conditions.condition IS 'Condition SQL fragment';

-- Function: rif40_trg_pkg.trgf_rif40_inv_conditions()

-- DROP FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions();

		CREATE OR REPLACE FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions()
		  RETURNS trigger AS
$BODY$
BEGIN
	IF TG_OP = 'INSERT' THEN
--
-- Check (USER = NEW.username OR NULL) and USER is a RIF user; if OK INSERT
--
		IF ((USER = NEW.username OR NEW.username IS NULL /* Will be defaulted */) AND 
		    (rif40_sql_pkg.is_rif40_user_manager_or_schema())) OR 
			(USER = 'rif40' AND NEW.study_id = 1 AND NEW.inv_id = 1) /* Allow alter_7 */ THEN
			INSERT INTO t_rif40_inv_conditions (
				username,
				study_id,
				inv_id,
				line_number,
				outcome_group_name, 
				min_condition, 
				max_condition, 
				predefined_group_name)
			VALUES(
				coalesce(NEW.username, "current_user"()),
				coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
				coalesce(NEW.inv_id, (currval('rif40_inv_id_seq'::regclass))::integer),
				coalesce(NEW.line_number, 1),
				NEW.outcome_group_name, 
				NEW.min_condition, 
				NEW.max_condition, 
				NEW.predefined_group_name);
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_conditions',
				'Cannot INSERT: User % must have rif_user or rif_manager role, NEW.username (%) must be USER or NULL', USER::VARCHAR, NEW.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
--
-- Check USER = OLD.username and NEW.username = OLD.username; if OK UPDATE
--
		IF USER = OLD.username AND NEW.username = OLD.username THEN
			UPDATE t_rif40_inv_conditions
			   SET username=NEW.username,
			       study_id=NEW.study_id,
			       inv_id=NEW.inv_id,
			       line_number=NEW.line_number,
				   outcome_group_name=NEW.outcome_group_name,
			       min_condition=NEW.min_condition, 
				   max_condition=NEW.max_condition, 
				   predefined_group_name=NEW.predefined_group_name
			 WHERE study_id=OLD.study_id
			   AND inv_id=OLD.inv_id
			   AND line_number=OLD.line_number;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_conditions',
				'Cannot UPDATE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
		END IF;
		RETURN NEW;
	ELSIF TG_OP = 'DELETE' THEN
--
-- Check USER = OLD.username; if OK DELETE
--
		IF USER = OLD.username THEN
			DELETE FROM t_rif40_inv_conditions
			 WHERE study_id=OLD.study_id
			   AND inv_id=OLD.inv_id
			   AND line_number=OLD.line_number;
		ELSE
			PERFORM rif40_log_pkg.rif40_error(-20999, 'trg_rif40_inv_conditions',
				'Cannot DELETE: User % is not the owner (%) of the record', USER::VARCHAR, OLD.username::VARCHAR);
			RETURN OLD;
		END IF;
		RETURN NULL;
	END IF;
	RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
		ALTER FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions()
		  OWNER TO rif40;
		COMMENT ON FUNCTION rif40_trg_pkg.trgf_rif40_inv_conditions() IS 'INSTEAD OF trigger for view T_RIF40_INV_CONDITIONS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
		 [NO TABLE/VIEW comments available]';

-- Trigger: trg_rif40_inv_conditions on rif40_inv_conditions

		CREATE TRIGGER trg_rif40_inv_conditions
		  INSTEAD OF INSERT OR DELETE OR UPDATE 
		  ON rif40_inv_conditions
		  FOR EACH ROW
		  EXECUTE PROCEDURE rif40_trg_pkg.trgf_rif40_inv_conditions();
		COMMENT ON TRIGGER trg_rif40_inv_conditions ON rif40_inv_conditions IS 'INSTEAD OF trigger for view T_RIF40_INV_CONDITIONS to allow INSERT/UPDATE/DELETE. INSERT/UPDATE/DELETE of another users data is NOT permitted. 
		 [Lines of SQL conditions pertinent to an investigation.]';
												   
--						
-- Put data back
--				
		IF c2_rec.investigations > 0 AND c2_rec.lines > 0 THEN		
            INSERT INTO rif40_inv_conditions(inv_id, study_id, username, line_number, outcome_group_name, min_condition)
                 VALUES (1, 1, c3_rec.username, 1, 'SAHSULAND_ICD', 'C34');
		    INSERT INTO rif40_inv_conditions(inv_id, study_id, username, line_number, outcome_group_name, min_condition)
			     VALUES (1, 1, c3_rec.username, 2, 'SAHSULAND_ICD', '162');	
        END IF;

	ELSIF c1_rec.column_name = 'min_condition' THEN	
		RAISE INFO 'v4_0_alter_7.sql: t_rif40_inv_conditons already upgraded.';	
	ELSE
		PERFORM rif40_sql_pkg.rif40_method4('SELECT * FROM t_rif40_inv_conditions LIMIT 20', 't_rif40_inv_conditions');
		RAISE EXCEPTION 'C20199: ERROR! expecting 0 or 1 investigations (%), 0, 1 or 2 lines (%) in t_rif40_inv_conditions, column_name: %; suggest "TRUNCATE TABLE t_rif40_inv_conditions;" in dev; will be fixed to upgrade data shortly',
			c2_rec.investigations, c2_rec.lines, c1_rec.column_name::VARCHAR;		
	END IF;
	
--
-- Comment new columns
--
	COMMENT ON COLUMN t_rif40_inv_conditions.min_condition 
		IS 'Minimum condition; if max condition is not null SQL WHERE Clause evaluates to: &quot;WHERE &lt;field_name&gt; LIKE ''&lt;min_condition&gt;%''&quot;.'; 	
	COMMENT ON COLUMN t_rif40_inv_conditions.max_condition 
		IS 'Maximum condition; if max condition is not null SQL WHERE Clause evaluates to: &quot;WHERE &lt;field_name&gt; BETWEEN ''&lt;min_condition&gt;'' AND ''&lt;max_condition&gt;~''&quot;.'; 				
	COMMENT ON COLUMN t_rif40_inv_conditions.predefined_group_name IS 'Predefined Group Name. E.g LUNG_CANCER'; 
	COMMENT ON COLUMN t_rif40_inv_conditions.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD'; 		
END;
$$;	

--
-- Put triggers back
--
\i ../PLpgsql/rif40_trg_pkg/trigger_fct_t_rif40_inv_conditions_checks.sql

\dS+ t_rif40_inv_conditions
\dS+ rif40_inv_conditions

--
-- Clean up rif40_columns, rif40_tables_and_views
--
-- 1. Tidy up RIF40_TABLES_AND_VIEWS table_or_view column, drop column TABLE_OR_VIEW_NAME_HREF, add primary key and check constraint
--
UPDATE rif40_tables_and_views
   SET table_or_view = 'VIEW'
 WHERE table_or_view LIKE '<a href="?proc=web_common.view_source&owner=''RIF40''&view_name=%';
SELECT class, table_or_view, COUNT(table_or_view_name_hide) AS total
  FROM rif40_tables_and_views
 GROUP BY class, table_or_view
 ORDER BY 1, 2;
 
--
-- Prune duplicates from rif40_tables_and_views, rif40_columns, rif40_triggers
-- (appears to be a build problem on wpea-darwin (Linux reference)
--
/*
SELECT table_or_view_name_hide, COUNT(table_or_view_name_hide) AS total
  FROM rif40_tables_and_views
 GROUP BY table_or_view_name_hide
HAVING COUNT(table_or_view_name_hide) > 1
 ORDER BY 1, 2;
WITH RECURSIVE a(ctid, rnt, cnt, table_or_view_name_hide) AS (
	SELECT ctid, ROW_NUMBER() OVER w AS rnt, COUNT(table_or_view_name_hide) OVER w AS cnt, table_or_view_name_hide
	  FROM rif40_tables_and_views
	WINDOW w AS (PARTITION BY table_or_view_name_hide)
)
DELETE FROM rif40_tables_and_views 
 WHERE ctid IN (
	SELECT ctid 
	   FROM a 
	  WHERE rnt != cnt
        AND cnt > 1)
RETURNING ctid, table_or_view_name_hide, class;

SELECT table_or_view_name_hide, COUNT(table_or_view_name_hide) AS total
  FROM rif40_tables_and_views
 GROUP BY table_or_view_name_hide
HAVING COUNT(table_or_view_name_hide) > 1
 ORDER BY 1, 2;
 
SELECT table_or_view_name_hide, column_name_hide, COUNT(table_or_view_name_hide) AS total
  FROM rif40_columns 
 GROUP BY table_or_view_name_hide, column_name_hide
HAVING COUNT(table_or_view_name_hide) > 1
 ORDER BY 1, 2; 
WITH RECURSIVE a(ctid, rnt, cnt, table_or_view_name_hide, column_name_hide) AS (
	SELECT ctid, ROW_NUMBER() OVER w AS rnt, COUNT(table_or_view_name_hide) OVER w AS cnt, table_or_view_name_hide, column_name_hide
	  FROM rif40_columns
	WINDOW w AS (PARTITION BY table_or_view_name_hide, column_name_hide)
)
DELETE FROM rif40_columns 
 WHERE ctid IN (
	SELECT ctid 
	   FROM a 
	  WHERE rnt != cnt
        AND cnt > 1)
RETURNING ctid, table_or_view_name_hide, column_name_hide;
SELECT table_or_view_name_hide, column_name_hide, COUNT(table_or_view_name_hide) AS total
  FROM rif40_columns 
 GROUP BY table_or_view_name_hide, column_name_hide
HAVING COUNT(table_or_view_name_hide) > 1
 ORDER BY 1, 2; 

SELECT table_name, trigger_name, COUNT(table_name) AS total
  FROM rif40_triggers 
 GROUP BY table_name, trigger_name
HAVING COUNT(table_name) > 1
 ORDER BY 1, 2; 

WITH RECURSIVE a(ctid, rnt, cnt, table_name, trigger_name) AS (
	SELECT ctid, ROW_NUMBER() OVER w AS rnt, COUNT(table_name) OVER w AS cnt, table_name, trigger_name
	  FROM rif40_triggers
	WINDOW w AS (PARTITION BY table_name, trigger_name)
)
DELETE FROM rif40_triggers 
 WHERE ctid IN (
	SELECT ctid 
	   FROM a 
	  WHERE rnt != cnt
        AND cnt > 1)
RETURNING ctid, table_name, trigger_name;
SELECT table_name, trigger_name, COUNT(table_name) AS total
  FROM rif40_triggers 
 GROUP BY table_name, trigger_name
HAVING COUNT(table_name) > 1
 ORDER BY 1, 2; 
 
 */
 
ALTER TABLE rif40_tables_and_views ALTER COLUMN table_or_view_name_hide 		SET DATA TYPE VARCHAR(40);
ALTER TABLE rif40_tables_and_views
  DROP CONSTRAINT IF EXISTS class_ck;
ALTER TABLE rif40_tables_and_views
  DROP CONSTRAINT IF EXISTS table_or_view_name_hide_pk CASCADE;
ALTER TABLE rif40_tables_and_views
      ADD CONSTRAINT class_ck 
	  CHECK (class::text = ANY (ARRAY[
			'Test harness'::character varying, 
			'Configuration'::character varying, 
			'Documentation'::character varying, 
			'Lookup'::character varying, 
			'Other'::character varying, 
			'Results'::character varying, 
			'SQL Generator'::character varying, 
			'Study Setup'::character varying]::text[]));
ALTER TABLE rif40_tables_and_views			
  ADD CONSTRAINT table_or_view_name_hide_pk PRIMARY KEY (table_or_view_name_hide);
ALTER TABLE rif40_tables_and_views DROP COLUMN IF EXISTS table_or_view_name_href RESTRICT;	

SELECT table_or_view_name_hide, column_name_hide
  FROM rif40_columns
 WHERE column_name_hide        = 'TABLE_OR_VIEW_NAME_HREF'
   AND table_or_view_name_hide = 'RIF40_TABLES_AND_VIEWS';
DELETE FROM rif40_columns
 WHERE column_name_hide        = 'TABLE_OR_VIEW_NAME_HREF'
   AND table_or_view_name_hide = 'RIF40_TABLES_AND_VIEWS';
\dS+ rif40_tables_and_views

--
-- 2. Tidy up RIF40_COLUMNS, drop columns: TABLE_OR_VIEW_NAME_HREF, COLUMN_NAME_HREF; add primary and foreign key and check constraint 
--   
SELECT oracle_data_type, COUNT(column_name_hide) AS total
  FROM rif40_columns
 GROUP BY oracle_data_type
 ORDER BY 1;
SELECT nullable, COUNT(column_name_hide) AS total
  FROM rif40_columns
 GROUP BY nullable
 ORDER BY 1;
ALTER TABLE rif40_columns ALTER COLUMN table_or_view_name_hide 	SET DATA TYPE VARCHAR(40);
ALTER TABLE rif40_columns ALTER COLUMN column_name_hide 		SET DATA TYPE VARCHAR(40);
ALTER TABLE rif40_columns DROP COLUMN IF EXISTS table_or_view_name_href RESTRICT;	
ALTER TABLE rif40_columns DROP COLUMN IF EXISTS column_name_href RESTRICT;	
ALTER TABLE rif40_columns
  DROP CONSTRAINT IF EXISTS rif40_columns_pk CASCADE;
ALTER TABLE rif40_columns
  DROP CONSTRAINT IF EXISTS table_or_view_name_hide_fk;  
ALTER TABLE rif40_columns
  DROP CONSTRAINT IF EXISTS nullable_ck;
 
SELECT table_or_view_name_hide, column_name_hide, COUNT(column_name_hide)
  FROM rif40_columns
 GROUP BY table_or_view_name_hide, column_name_hide
 HAVING COUNT(column_name_hide) > 1
 ORDER BY 1, 2;
 
ALTER TABLE rif40_columns			
  ADD CONSTRAINT rif40_columns_pk PRIMARY KEY (table_or_view_name_hide, column_name_hide);
ALTER TABLE rif40_columns	
  ADD CONSTRAINT table_or_view_name_hide_fk 
		FOREIGN KEY (table_or_view_name_hide) REFERENCES rif40_tables_and_views(table_or_view_name_hide);	  
ALTER TABLE rif40_columns
  ADD CONSTRAINT nullable_ck CHECK (nullable IN ('NOT NULL', 'NULL'));
  
SELECT table_or_view_name_hide, column_name_hide
  FROM rif40_columns
 WHERE table_or_view_name_hide = 'RIF40_COLUMNS'
   AND column_name_hide        IN ('TABLE_OR_VIEW_NAME_HREF', 'COLUMN_NAME_HREF');
DELETE FROM rif40_columns
 WHERE table_or_view_name_hide = 'RIF40_COLUMNS'
   AND column_name_hide        IN ('TABLE_OR_VIEW_NAME_HREF', 'COLUMN_NAME_HREF');
\dS+ rif40_columns

--
-- 3. Remove existing ontology tables (keep icd9/10 until new ontology middleware is ready)
--
SELECT table_or_view_name_hide, column_name_hide
  FROM rif40_columns
 WHERE table_or_view_name_hide IN ('RIF40_ICD_O_3', 'RIF40_OPCS4', 'RIF40_A_AND_E'); 
DELETE FROM rif40_columns WHERE table_or_view_name_hide IN ('RIF40_ICD_O_3', 'RIF40_OPCS4', 'RIF40_A_AND_E');
SELECT class, table_or_view, table_or_view_name_hide
  FROM rif40_tables_and_views
 WHERE table_or_view_name_hide IN ('RIF40_ICD_O_3', 'RIF40_OPCS4', 'RIF40_A_AND_E'); 
DELETE FROM rif40_tables_and_views WHERE table_or_view_name_hide IN ('RIF40_ICD_O_3', 'RIF40_OPCS4', 'RIF40_A_AND_E'); 

--
-- 4. Remove columns dropped from RIF40_OUTCOMES and T_RIF40_INV_CONDITIONS
--
SELECT table_or_view_name_hide, column_name_hide
  FROM rif40_columns
 WHERE column_name_hide        IN (
			'CURRENT_DESCRIPTION_1CHAR',
			'CURRENT_DESCRIPTION_2CHAR',
			'CURRENT_DESCRIPTION_3CHAR',
			'CURRENT_DESCRIPTION_4CHAR',
			'CURRENT_DESCRIPTION_5CHAR',
			'CURRENT_LOOKUP_TABLE',
			'CURRENT_VALUE_1CHAR',
			'CURRENT_VALUE_2CHAR',
			'CURRENT_VALUE_3CHAR',
			'CURRENT_VALUE_4CHAR',
			'CURRENT_VALUE_5CHAR',
			'PREVIOUS_DESCRIPTION_1CHAR',
			'PREVIOUS_DESCRIPTION_2CHAR',
			'PREVIOUS_DESCRIPTION_3CHAR',
			'PREVIOUS_DESCRIPTION_4CHAR',
			'PREVIOUS_DESCRIPTION_5CHAR',
			'PREVIOUS_LOOKUP_TABLE',
			'PREVIOUS_VALUE_1CHAR',
			'PREVIOUS_VALUE_2CHAR',
			'PREVIOUS_VALUE_3CHAR',
			'PREVIOUS_VALUE_4CHAR',
			'PREVIOUS_VALUE_5CHAR')
   AND table_or_view_name_hide = 'RIF40_OUTCOMES';
DELETE FROM rif40_columns
 WHERE column_name_hide        IN (
			'CURRENT_DESCRIPTION_1CHAR',
			'CURRENT_DESCRIPTION_2CHAR',
			'CURRENT_DESCRIPTION_3CHAR',
			'CURRENT_DESCRIPTION_4CHAR',
			'CURRENT_DESCRIPTION_5CHAR',
			'CURRENT_LOOKUP_TABLE',
			'CURRENT_VALUE_1CHAR',
			'CURRENT_VALUE_2CHAR',
			'CURRENT_VALUE_3CHAR',
			'CURRENT_VALUE_4CHAR',
			'CURRENT_VALUE_5CHAR',
			'PREVIOUS_DESCRIPTION_1CHAR',
			'PREVIOUS_DESCRIPTION_2CHAR',
			'PREVIOUS_DESCRIPTION_3CHAR',
			'PREVIOUS_DESCRIPTION_4CHAR',
			'PREVIOUS_DESCRIPTION_5CHAR',
			'PREVIOUS_LOOKUP_TABLE',
			'PREVIOUS_VALUE_1CHAR',
			'PREVIOUS_VALUE_2CHAR',
			'PREVIOUS_VALUE_3CHAR',
			'PREVIOUS_VALUE_4CHAR',
			'PREVIOUS_VALUE_5CHAR')
   AND table_or_view_name_hide = 'RIF40_OUTCOMES';

SELECT table_or_view_name_hide, column_name_hide
  FROM rif40_columns
 WHERE column_name_hide        = 'CONDITION'
   AND table_or_view_name_hide = 'T_RIF40_INV_CONDITIONS';
DELETE FROM rif40_columns
 WHERE column_name_hide        = 'CONDITION'
   AND table_or_view_name_hide = 'T_RIF40_INV_CONDITIONS';

--
-- 5. Add new view: RIF40_NUMERATOR_OUTCOME_COLUMNS
-- 
INSERT INTO rif40_tables_and_views (class, table_or_view, comments, table_or_view_name_hide)
SELECT 'Configuration' AS class, 'VIEW' AS table_or_view, 
       obj_description(b.oid) AS comments, UPPER(b.relname) AS table_or_view_name_hide
  FROM pg_class b
 WHERE b.relname = 'rif40_numerator_outcome_columns'
   AND b.relkind = 'v'
   AND NOT EXISTS (SELECT 1 FROM rif40_tables_and_views WHERE table_or_view_name_hide IN ('RIF40_NUMERATOR_OUTCOME_COLUMNS'));  

INSERT INTO rif40_columns (table_or_view_name_hide, column_name_hide, nullable, oracle_data_type, comments)   
SELECT UPPER(b.relname) AS table_or_view_name_hide, UPPER(d.attname) AS column_name_hide, 
      CASE WHEN d.attnotnull THEN 'NOT NULL' ELSE 'NULL' END AS nullable, t.typname AS oracle_data_type,
       col_description(b.oid, d.attnum) AS comments
  FROM pg_class b, pg_attribute d, pg_type t
 WHERE b.relname::regclass = d.attrelid
   AND d.atttypid = t.oid
   AND b.relname = 'rif40_numerator_outcome_columns'
   AND b.relkind = 'v'
   AND NOT EXISTS (SELECT 1 FROM rif40_columns WHERE table_or_view_name_hide IN ('RIF40_NUMERATOR_OUTCOME_COLUMNS')); 

--
-- 6. Add new columns for: RIF40_INV_CONDITIONS and T_RIF40_INV_CONDITIONS
--
INSERT INTO rif40_columns (table_or_view_name_hide, column_name_hide, nullable, oracle_data_type, comments)   
SELECT UPPER(b.relname) AS table_or_view_name_hide, UPPER(d.attname) AS column_name_hide, 
      CASE WHEN d.attnotnull THEN 'NOT NULL' ELSE 'NULL' END AS nullable, t.typname AS oracle_data_type,
       col_description(b.oid, d.attnum) AS comments
  FROM pg_class b, pg_attribute d, pg_type t
 WHERE b.relname::regclass = d.attrelid
   AND d.atttypid = t.oid
   AND b.relname = 'rif40_inv_conditions'
   AND b.relkind = 'v'
   AND col_description(b.oid, d.attnum) IS NOT NULL
   AND d.attname NOT IN (
		SELECT LOWER(column_name_hide) AS column_name
 		  FROM rif40_columns
		 WHERE table_or_view_name_hide IN ('RIF40_INV_CONDITIONS')); 

INSERT INTO rif40_columns (table_or_view_name_hide, column_name_hide, nullable, oracle_data_type, comments)   		 
SELECT UPPER(b.relname) AS table_or_view_name_hide, UPPER(d.attname) AS column_name_hide, 
      CASE WHEN d.attnotnull THEN 'NOT NULL' ELSE 'NULL' END AS nullable, t.typname AS oracle_data_type,
       col_description(b.oid, d.attnum) AS comments
  FROM pg_class b, pg_attribute d, pg_type t
 WHERE b.relname::regclass = d.attrelid
   AND d.atttypid = t.oid
   AND b.relname = 't_rif40_inv_conditions'
   AND b.relkind = 'r'
   AND col_description(b.oid, d.attnum) IS NOT NULL
   AND d.attname NOT IN (
		SELECT LOWER(column_name_hide) AS column_name
 		  FROM rif40_columns
		 WHERE table_or_view_name_hide IN ('T_RIF40_INV_CONDITIONS')); 

SELECT class, table_or_view, table_or_view_name_hide
  FROM rif40_tables_and_views
 WHERE table_or_view_name_hide IN ('RIF40_INV_CONDITIONS', 'T_RIF40_INV_CONDITIONS', 'RIF40_NUMERATOR_OUTCOME_COLUMNS', 'RIF40_COLUMNS', 'RIF40_TABLES_AND_VIEWS');
SELECT table_or_view_name_hide, column_name_hide
  FROM rif40_columns
 WHERE table_or_view_name_hide IN ('RIF40_INV_CONDITIONS', 'T_RIF40_INV_CONDITIONS', 'RIF40_NUMERATOR_OUTCOME_COLUMNS', 'RIF40_COLUMNS', 'RIF40_TABLES_AND_VIEWS');
 
--
-- 7. RIF40_OUTCOMES trigger removed from rif40_triggers
--
SELECT * FROM rif40_triggers WHERE table_name = 'RIF40_OUTCOMES';
DELETE FROM rif40_triggers WHERE table_name = 'RIF40_OUTCOMES';

ALTER TABLE rif40_triggers
  DROP CONSTRAINT IF EXISTS rif40_triggers_pk CASCADE;
ALTER TABLE rif40_triggers
  DROP CONSTRAINT IF EXISTS table_or_view_name_hide_fk;  
   
   
--
-- 8. Add PK, FK to rif40_triggers
--
ALTER TABLE rif40_triggers			
  ADD CONSTRAINT rif40_triggers_pk PRIMARY KEY (table_name, trigger_name);
ALTER TABLE rif40_triggers	
  ADD CONSTRAINT table_or_view_name_hide_fk 
		FOREIGN KEY (table_name) REFERENCES rif40_tables_and_views(table_or_view_name_hide);	 
		
--
-- Check repaired view: rif40_inv_conditions
--
SELECT * FROM rif40_inv_conditions LIMIT 20;

--
-- Check all tables, triggers, columns and comments are present, objects granted to rif_user/rif_manmger, sequences granted
-- 
\set VERBOSITY :verbosity
--\i ../psql_scripts/v4_0_postgres_ddl_checks.sql

--
-- Load new rif40_create_disease_mapping_example() and rif40_clone_study()
--
\i ../PLpgsql/rif40_sm_pkg/rif40_create_disease_mapping_example.sql
\i ../PLpgsql/rif40_sm_pkg/rif40_clone_study.sql

--
-- Load new rif40_startup() with ability to disable most checks for middleware testing
-- Note: must be run once to create TEMPORARY TABLES
--
\i ../PLpgsql/rif40_sql_pkg/rif40_startup.sql

--
-- Testing stop
--
--DO LANGUAGE plpgsql $$
--BEGIN
--	RAISE EXCEPTION 'Stop processing';
--END;
--$$;

END;
--
--  Eof
