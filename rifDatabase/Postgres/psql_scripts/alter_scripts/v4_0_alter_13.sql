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
-- Rapid Inquiry Facility (RIF) - RIF alter script 12 - More risk Analysis Enhancements, additional covariate support;
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
-- Working directory: c:/Users/Peter/Documents/GitHub/rapidInquiryFacility/rifDatabase/Postgres/psql_scripts
-- psql -U rif40 -d sahsuland -w -e -P pager=off -f alter_scripts/v4_0_alter_13.sql
--
-- The middleware must be down for this to run
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Running SAHSULAND schema alter script #13 Issue #138 State 1: study setup modal errors fixes

/*
 * Alter 13: Issue #138 State 1: study setup modal errors fixes; 

 1. Rebuild rif40_numerator_outcome_columns view
 2. Remove rif40_num_denom from rif40_startup procedure

 */
BEGIN;

-- View: rif40.rif40_numerator_outcome_columns

DROP VIEW IF EXISTS rif40.rif40_numerator_outcome_columns;

CREATE OR REPLACE VIEW rif40.rif40_numerator_outcome_columns AS
WITH a AS (
         SELECT z.geography,
            a_1.table_name,
            z.numerator_description AS table_description,
            c.outcome_group_name,
            c.outcome_type,
            c.outcome_group_description,
            c.field_name,
            c.multiple_field_count
           FROM rif40_num_denom z, /* RIF40's version of the code */
            rif40_tables a_1,
            rif40_table_outcomes b,
            rif40_outcome_groups c
          WHERE a_1.table_name       = z.numerator_table
            AND a_1.table_name       = b.numer_tab
            AND c.outcome_group_name = b.outcome_group_name
        )
 SELECT a.geography,
    a.table_name,
    a.table_description,
    a.outcome_group_name,
    a.outcome_type,
    a.outcome_group_description,
    a.field_name,
    a.multiple_field_count,
        CASE
            WHEN d.attrelid IS NOT NULL THEN true
            ELSE false
        END AS columnn_exists,
        CASE
            WHEN d.attrelid IS NOT NULL THEN col_description(LOWER(a.table_name)::regclass::oid, d.attnum::integer)
            ELSE NULL::text
        END AS column_comment
   FROM a
     LEFT JOIN pg_attribute d ON (LOWER(a.table_name)::regclass::oid = d.attrelid 
                                  AND d.attname = lower(a.field_name));

ALTER TABLE rif40.rif40_numerator_outcome_columns
    OWNER TO rif40;
COMMENT ON VIEW rif40.rif40_numerator_outcome_columns
    IS 'All numerator outcome fields (columns) ';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.geography IS 'Geography';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.table_name IS 'Numerator table';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.table_description IS 'Numerator description';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.outcome_type IS 'Outcome type: ICD, ICD-0 or OPCS';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.outcome_group_description IS 'Outcome Group Description. E.g. Single variable ICD';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.field_name IS 'Numerator field name (will only the actual field name if the multiple field count is zero.';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.multiple_field_count IS 'Outcome Group multiple field count (0-99). E.g if NULL then field is ICD_SAHSU_01; if 20 then fields are ICD_SAHSU_01 to ICD_SAHSU_20. Field numbers are assumed to tbe left padded to 2 characters with "0" and preceeded by an underscore';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.columnn_exists IS 'Does the column exist true/false';
COMMENT ON COLUMN rif40.rif40_numerator_outcome_columns.column_comment IS 'Numerator field comment';

GRANT ALL ON TABLE rif40.rif40_numerator_outcome_columns TO rif40;
GRANT SELECT ON TABLE rif40.rif40_numerator_outcome_columns TO rif_user;
GRANT SELECT ON TABLE rif40.rif40_numerator_outcome_columns TO rif_manager;

\dS+ rif40.rif40_numerator_outcome_columns

\i ../../PLpgsql/rif40_sql_pkg/rif40_startup.sql

/*
--
-- Testing stop
--

DO LANGUAGE plpgsql $$
BEGIN
	RAISE EXCEPTION 'Stop processing';
END;
$$;
 */
 
END;
--
--  Eof 
