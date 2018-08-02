Example Postgres Load
```
You are connected to database "sahsuland" as user "rif40" on host "localhost" at port "5432".
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
                RAISE INFO 'RIF startup: not a RIF database';
        END IF;
--
-- Set a default path, schema to user
--
        IF current_user = 'rif40' THEN
                sql_stmt:='SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
        ELSE
                sql_stmt:='SET search_path TO '||USER||',rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
        END IF;
        RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
        EXECUTE sql_stmt;
END;
$$;
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  rif40_log_setup() DEFAULTED send DEBUG to INFO: off; debug function list: []
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.01s  rif40_startup(): search_path not set for: rif40
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.01s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS rif40.rif40_run_study(INTEGER, INTEGER);
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: NOTICE:  function rif40.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.06s  rif40_startup(): Created temporary table: g_rif40_study_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.08s  rif40_startup(): Created temporary table: g_rif40_comparison_areas
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.21s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.21s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.21s  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.21s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: rif40
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.21s  rif40_startup(): search_path: public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  +00000.21s  rif40_startup(): Deleted 0, created 2 tables/views/foreign data wrapper tables
psql:C:/Program Files/PostgreSQL/9.6/etc/psqlrc:48: INFO:  SQL> SET search_path TO rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
Pager usage is off.
\set ON_ERROR_STOP ON
\timing
Timing is on.

\i pg_rif40_load_seer_cancer.sql
-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load SEER numerator data into RIF; Does not reformat into RIF4.0 format (see load_seer.sql)
--                                                                * Requires the "seer_user" role
--                                Postgres script
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
-- Postgres RIF40 specific parameters
--
-- Usage: psql -U rif40 -w -e -f pg_rif40_load_seer_cancer.sql
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
-- Requires RIF USA county level geography to be loaded: rif_pg_USA_2014.sql
--

\pset pager off
Pager usage is off.
\set ECHO all
\set ON_ERROR_STOP ON
\timing
Timing is off.

--
-- Start transaction
--
BEGIN TRANSACTION;
BEGIN

--
-- Setup RIF user
--
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
                RAISE EXCEPTION 'RIF startup(SEER loader): not a RIF database';
        END IF;
--
-- Set a default path and schema for user
--
        IF current_user = 'rif40' THEN
                sql_stmt:='SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
        ELSE
                RAISE EXCEPTION 'RIF startup(SEER loader): RIF user: % is not rif40', current_user;
        END IF;
        RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
        EXECUTE sql_stmt;
END;
$$;
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  rif40_log_setup() send DEBUG to INFO: off; debug function list: []
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.03s  rif40_startup(): search_path not set for: rif40
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.03s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS rif40.rif40_run_study(INTEGER, INTEGER);
psql:pg_rif40_load_seer_cancer.sql:97: NOTICE:  function rif40.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.04s  rif40_startup(): Temporary table: g_rif40_comparison_areas exists
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.05s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.05s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.05s  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.05s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: rif40
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  +00000.05s  rif40_startup(): search_path: public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:pg_rif40_load_seer_cancer.sql:97: INFO:  SQL> SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO

--
-- Check if geography is loaded
--
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT geography
                  FROM rif40_geographies a
                 WHERE a.geography  = 'USA_2014';
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.geography = 'USA_2014' THEN
                RAISE INFO 'Geography: USA_2014 loaded';
        ELSE
                RAISE EXCEPTION 'Geography: USA_2014 is NOT loaded';
        END IF;
END;
$$;
psql:pg_rif40_load_seer_cancer.sql:120: INFO:  Geography: USA_2014 loaded
DO

--
-- Check SEER_USER role is present (needs to be created by an administrator)
--
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT rolname
                  FROM pg_roles a
                 WHERE a.rolname  = 'seer_user';
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.rolname = 'seer_user' THEN
                RAISE INFO 'SEER_USER: role is present';
        ELSE
                RAISE EXCEPTION 'SEER_USER: role is NOT present';
        END IF;
END;
$$;
psql:pg_rif40_load_seer_cancer.sql:143: INFO:  SEER_USER: role is present
DO

--
-- Remove old SEER data
--
DROP TABLE IF EXISTS rif_data.t_seer_cancer;
psql:pg_rif40_load_seer_cancer.sql:148: NOTICE:  table "t_seer_cancer" does not exist, skipping
DROP TABLE
DROP VIEW IF EXISTS rif_data.seer_cancer;
psql:pg_rif40_load_seer_cancer.sql:149: NOTICE:  view "seer_cancer" does not exist, skipping
DROP VIEW

DELETE FROM rif40.rif40_table_outcomes
 WHERE numer_tab='SEER_CANCER';
DELETE 0

DELETE FROM rif40.rif40_tables
 WHERE table_name='SEER_CANCER';
DELETE 0

 --
-- Create SEER_CANCER numerator table
--
CREATE TABLE rif_data.t_seer_cancer
(
  year integer NOT NULL, -- Year
  cb_2014_us_nation_5m text, -- United States to county level including territories
  cb_2014_us_state_500k text NOT NULL, -- State geographic Names Information System (GNIS) code
  cb_2014_us_county_500k text NOT NULL, -- County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.
  age_sex_group integer NOT NULL, -- RIF age_sex_group 1 (21 bands)
  icdot10v text, -- ICD 10 site code - recoded from ICD-O-2 to 10
  pubcsnum integer NOT NULL, -- Patient ID
  seq_num integer NOT NULL, -- Sequence number
  histo3v text, -- Histologic Type ICD-O-3
  beho3v text, -- Behavior code ICD-O-3
  rac_reca integer, -- Race recode A (WHITE, BLACK, OTHER)
  rac_recy integer, -- Race recode Y (W, B, AI, API)
  origrecb integer, -- Origin Recode NHIA (HISPANIC, NON-HISP)
  codpub text, -- Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)
  reg integer, -- SEER registry (minus 1500 so same as population file)
  CONSTRAINT seer_cancer_pk PRIMARY KEY (pubcsnum, seq_num),
  CONSTRAINT seer_cancer_asg_ck CHECK (
                        (age_sex_group >= 100 AND age_sex_group <= 121) OR
                        (age_sex_group >= 200 AND age_sex_group <= 221) OR
                        (age_sex_group IN (199, 299))
                )
        );
CREATE TABLE

--
-- Load
--
\copy rif_data.t_seer_cancer FROM 'seer_cancer.csv' WITH CSV HEADER;
COPY 9176963

--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM t_seer_cancer;
  total
---------
 9176963
(1 row)

DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT COUNT(*) AS total
                  FROM t_seer_cancer;
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.total = 9176963 THEN
                RAISE INFO 'Table: t_seer_cancer has % rows', c1_rec.total;
        ELSE
                RAISE EXCEPTION 'Table: t_seer_cancer has % rows; expecting 9176963', c1_rec.total;
        END IF;
END;
$$;
psql:pg_rif40_load_seer_cancer.sql:211: INFO:  Table: t_seer_cancer has 9176963 rows
DO

--
-- Indexes
--
CREATE INDEX seer_cancer_age_sex_group
  ON rif_data.t_seer_cancer
  (age_sex_group);
CREATE INDEX

CREATE INDEX seer_cancer_cb_2014_us_county_500k
  ON rif_data.t_seer_cancer
  (cb_2014_us_county_500k);
CREATE INDEX

CREATE INDEX seer_cancer_cb_2014_us_nation_5m
  ON rif_data.t_seer_cancer
  (cb_2014_us_nation_5m);
CREATE INDEX

CREATE INDEX seer_cancer_cb_2014_us_state_500k
  ON rif_data.t_seer_cancer
  (cb_2014_us_state_500k);
CREATE INDEX

CREATE INDEX seer_cancer_icdot10v
  ON rif_data.t_seer_cancer
  (icdot10v);
CREATE INDEX

CREATE INDEX seer_cancer_reg
  ON rif_data.t_seer_cancer
  (reg);
CREATE INDEX

CREATE INDEX seer_cancer_year
  ON rif_data.t_seer_cancer
  (year);
CREATE INDEX

--
-- Create a test view
--
CREATE OR REPLACE VIEW rif_data.seer_cancer AS
SELECT * FROM rif_data.t_seer_cancer;
CREATE VIEW

COMMENT ON TABLE rif_data.t_seer_cancer
  IS 'SEER Cancer data 1973-2013. 9 States in total';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.year IS 'Year';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.icdot10v IS 'ICD 10 site code - recoded from ICD-O-2 to 10';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.pubcsnum IS 'Patient ID';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.seq_num IS 'Sequence number';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.histo3v IS 'Histologic Type ICD-O-3';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.beho3v IS 'Behavior code ICD-O-3';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.rac_reca IS 'Race recode A (WHITE, BLACK, OTHER)';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.rac_recy IS 'Race recode Y (W, B, AI, API)';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.origrecb IS 'Origin Recode NHIA (HISPANIC, NON-HISP)';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.codpub IS 'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)';
COMMENT
COMMENT ON COLUMN rif_data.t_seer_cancer.reg IS 'SEER registry (minus 1500 so same as population file)';
COMMENT

COMMENT ON VIEW rif_data.seer_cancer
  IS 'SEER Cancer data 1973-2013 view test. 9 States in total';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.year IS 'Year';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.icdot10v IS 'ICD 10 site code - recoded from ICD-O-2 to 10';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.pubcsnum IS 'Patient ID';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.seq_num IS 'Sequence number';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.histo3v IS 'Histologic Type ICD-O-3';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.beho3v IS 'Behavior code ICD-O-3';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.rac_reca IS 'Race recode A (WHITE, BLACK, OTHER)';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.rac_recy IS 'Race recode Y (W, B, AI, API)';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.origrecb IS 'Origin Recode NHIA (HISPANIC, NON-HISP)';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.codpub IS 'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)';
COMMENT
COMMENT ON COLUMN rif_data.seer_cancer.reg IS 'SEER registry (minus 1500 so same as population file)';
COMMENT

--
-- Setup as numerator
--
INSERT INTO rif40.rif40_tables (
   theme,
   table_name,
   description,
   year_start,
   year_stop,
   total_field,
   isindirectdenominator,
   isdirectdenominator,
   isnumerator,
   automatic,
   sex_field_name,
   age_group_field_name,
   age_sex_group_field_name,
   age_group_id)
SELECT
   'cancers',                   /* theme */
   'SEER_CANCER',               /* table_name */
   'SEER Cancer data 1973-2013. 9 States in total',                             /* description */
   MIN(year),                   /* year_start */
   MAX(year),                   /* year_stop */
   NULL,                                /* total_field */
   0,                                   /* isindirectdenominator */
   0,                                   /* isdirectdenominator */
   1,                                   /* isnumerator */
   1,                                   /* automatic */
   NULL,                                /* sex_field_name */
   NULL,                                /* age_group_field_name */
   'AGE_SEX_GROUP',             /* age_sex_group_field_name */
   1                                    /* age_group_id */
  FROM rif_data.seer_cancer;
INSERT 0 1

--
-- Setup ICD field (SEER_ICDOT10V)
-- * ICD-O-3 histology (HISTO3V) to follow later
--
INSERT INTO rif40.rif40_outcome_groups(
   outcome_type, outcome_group_name, outcome_group_description, field_name, multiple_field_count)
SELECT
   'ICD' AS outcome_type,
   'SEER_ICDOT10V' AS outcome_group_name,
   'SEER ICDOT10V' AS outcome_group_description,
   'ICDOT10V' AS field_name,
   0 AS multiple_field_count
WHERE NOT EXISTS (SELECT outcome_group_name FROM  rif40.rif40_outcome_groups WHERE outcome_group_name = 'SEER_ICDOT10V');
INSERT 0 1

INSERT INTO rif40.rif40_table_outcomes (
   outcome_group_name,
   numer_tab,
   current_version_start_year)
SELECT
   'SEER_ICDOT10V',
   'SEER_CANCER',
   MIN(year)
FROM rif_data.seer_cancer;
INSERT 0 1

--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.seer_cancer TO seer_user;
GRANT

--
-- End transaction (COMMIT)
--
END;
COMMIT

--
-- Eof

\i pg_rif40_load_seer_population.sql
-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load denominator SEER data into RIF; Does not reformat into RIF4.0 format (see load_seer.sql)
--                                                                * Requires the "seer_user" role
--                                Postgres script
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
-- Postgres RIF40 specific parameters
--
-- Usage: psql -U rif40 -w -e -f pg_rif40_load_seer_population.sql.sql
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
-- Requires RIF USA county level geography to be loaded: rif_pg_USA_2014.sql
--

\pset pager off
Pager usage is off.
\set ECHO all
\set ON_ERROR_STOP ON
\timing
Timing is on.

--
-- Start transaction
--
BEGIN TRANSACTION;
BEGIN
Time: 5.242 ms

--
-- Setup RIF user
--
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
                RAISE EXCEPTION 'RIF startup(SEER loader): not a RIF database';
        END IF;
--
-- Set a default path and schema for user
--
        IF current_user = 'rif40' THEN
                sql_stmt:='SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
        ELSE
                RAISE EXCEPTION 'RIF startup(SEER loader): RIF user: % is not rif40', current_user;
        END IF;
        RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
        EXECUTE sql_stmt;
END;
$$;
psql:pg_rif40_load_seer_population.sql:97: INFO:  rif40_log_setup() send DEBUG to INFO: off; debug function list: []
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.03s  rif40_startup(): search_path not set for: rif40
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.03s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS rif40.rif40_run_study(INTEGER, INTEGER);
psql:pg_rif40_load_seer_population.sql:97: NOTICE:  function rif40.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.05s  rif40_startup(): Temporary table: g_rif40_comparison_areas exists
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.06s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.06s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.06s  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.06s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: rif40
psql:pg_rif40_load_seer_population.sql:97: INFO:  +00000.06s  rif40_startup(): search_path: rif_data, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:pg_rif40_load_seer_population.sql:97: INFO:  SQL> SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO
Time: 52.219 ms

--
-- Check if geography is loaded
--
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT geography
                  FROM rif40_geographies a
                 WHERE a.geography  = 'USA_2014';
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.geography = 'USA_2014' THEN
                RAISE INFO 'Geography: USA_2014 loaded';
        ELSE
                RAISE EXCEPTION 'Geography: USA_2014 is NOT loaded';
        END IF;
END;
$$;
psql:pg_rif40_load_seer_population.sql:120: INFO:  Geography: USA_2014 loaded
DO
Time: 2.700 ms

--
-- Check SEER_USER role is present (needs to be created by an administrator)
--
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT rolname
                  FROM pg_roles a
                 WHERE a.rolname  = 'seer_user';
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.rolname = 'seer_user' THEN
                RAISE INFO 'SEER_USER: role is present';
        ELSE
                RAISE EXCEPTION 'SEER_USER: role is NOT present';
        END IF;
END;
$$;
psql:pg_rif40_load_seer_population.sql:143: INFO:  SEER_USER: role is present
DO
Time: 4.935 ms

--
-- Remove old SEER data
--
DROP TABLE IF EXISTS rif_data.seer_population;
psql:pg_rif40_load_seer_population.sql:148: NOTICE:  table "seer_population" does not exist, skipping
DROP TABLE
Time: 1.768 ms

DELETE FROM rif40.rif40_tables
WHERE table_name='SEER_POPULATION';
DELETE 0
Time: 0.372 ms

--
-- Create SEER_POPULATION numerator table
--
CREATE TABLE rif_data.seer_population
(
  year integer NOT NULL, -- Year
  cb_2014_us_nation_5m text NOT NULL, -- United States to county level including territories
  cb_2014_us_state_500k text NOT NULL, -- State geographic Names Information System (GNIS) code
  cb_2014_us_county_500k text NOT NULL, -- County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.
  age_sex_group integer NOT NULL, -- RIF age_sex_group 1 (21 bands)
  population numeric, -- Population
  CONSTRAINT seer_population_pk PRIMARY KEY (year, cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k, age_sex_group),
  CONSTRAINT seer_population_asg_ck CHECK (age_sex_group >= 100 AND age_sex_group <= 121 OR age_sex_group >= 200 AND age_sex_group <= 221)
);
CREATE TABLE
Time: 16.006 ms
COMMENT ON TABLE rif_data.seer_population
  IS 'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total';
COMMENT
Time: 0.511 ms
COMMENT ON COLUMN rif_data.seer_population.year IS 'Year';
COMMENT
Time: 0.393 ms
COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT
Time: 0.392 ms
COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT
Time: 0.428 ms
COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT
Time: 0.346 ms
COMMENT ON COLUMN rif_data.seer_population.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
COMMENT
Time: 0.372 ms
COMMENT ON COLUMN rif_data.seer_population.population IS 'Population';
COMMENT
Time: 0.251 ms

--
-- Load
--
\copy rif_data.seer_population FROM 'seer_population.csv' WITH CSV HEADER;
COPY 614360
Time: 3435.280 ms

--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM seer_population;
 total
--------
 614360
(1 row)

Time: 66.457 ms
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT COUNT(*) AS total
                  FROM seer_population;
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.total = 614360 THEN
                RAISE INFO 'Table: seer_population has % rows', c1_rec.total;
        ELSE
                RAISE EXCEPTION 'Table: seer_population has % rows; expecting 614360', c1_rec.total;
        END IF;
END;
$$;
psql:pg_rif40_load_seer_population.sql:202: INFO:  Table: seer_population has 614360 rows
DO
Time: 45.965 ms

--
-- Convert to index organised table
--
CLUSTER rif_data.seer_population USING seer_population_pk;
CLUSTER
Time: 1138.351 ms

--
-- Indexes
--
CREATE INDEX seer_population_age_sex_group
  ON rif_data.seer_population
  (age_sex_group);
CREATE INDEX
Time: 459.565 ms

CREATE INDEX seer_population_cb_2014_us_county_500k
  ON rif_data.seer_population
  (cb_2014_us_county_500k);
CREATE INDEX
Time: 1440.758 ms

CREATE INDEX seer_population_cb_2014_us_nation_5m
  ON rif_data.seer_population
  (cb_2014_us_nation_5m);
CREATE INDEX
Time: 195.549 ms

CREATE INDEX seer_population_cb_2014_us_state_500k
  ON rif_data.seer_population
  (cb_2014_us_state_500k);
CREATE INDEX
Time: 851.007 ms

CREATE INDEX seer_population_year
  ON rif_data.seer_population
  (year);
CREATE INDEX
Time: 185.209 ms

--
-- Setup as denominator
--
INSERT INTO rif40.rif40_tables (
   theme,
   table_name,
   description,
   year_start,
   year_stop,
   total_field,
   isindirectdenominator,
   isdirectdenominator,
   isnumerator,
   automatic,
   sex_field_name,
   age_group_field_name,
   age_sex_group_field_name,
   age_group_id)
SELECT
   'cancers',           /* theme */
   'SEER_POPULATION',   /* table_name */
   'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total',  /* description */
   MIN(YEAR),           /* year_start */
   MAX(YEAR),           /* year_stop */
   'POPULATION',                        /* total_field */
   1,                           /* isindirectdenominator */
   0,                           /* isdirectdenominator */
   0,                           /* isnumerator */
   1,                           /* automatic */
   NULL,                        /* sex_field_name */
   NULL,                        /* age_group_field_name */
   'AGE_SEX_GROUP',     /* age_sex_group_field_name */
   1                            /* age_group_id */
FROM rif_data.seer_population;
INSERT 0 1
Time: 37.323 ms

--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.seer_population TO seer_user;
GRANT
Time: 0.417 ms


--
-- End transaction (COMMIT)
--
END;
COMMIT
Time: 16.087 ms

--
-- Eof
\i pg_rif40_load_seer_covariates.sql
-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load covariate SEER data into RIF; Does not reformat into RIF4.0 format (see load_seer.sql)
--                                                                * Requires the "seer_user" role
--                                Postgres script
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
-- Postgres RIF40 specific parameters
--
-- Usage: psql -U rif40 -w -e -f pg_rif40_load_seer_covariates.sql.sql.sql
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
-- Requires RIF USA county level geography to be loaded: rif_pg_USA_2014.sql
--

\pset pager off
Pager usage is off.
\set ECHO all
\set ON_ERROR_STOP ON
\timing
Timing is off.

--
-- Start transaction
--
BEGIN TRANSACTION;
BEGIN

--
-- Setup RIF user
--
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
                RAISE EXCEPTION 'RIF startup(SEER loader): not a RIF database';
        END IF;
--
-- Set a default path and schema for user
--
        IF current_user = 'rif40' THEN
                sql_stmt:='SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions';
        ELSE
                RAISE EXCEPTION 'RIF startup(SEER loader): RIF user: % is not rif40', current_user;
        END IF;
        RAISE INFO 'SQL> %;', sql_stmt::VARCHAR;
        EXECUTE sql_stmt;
END;
$$;
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  rif40_log_setup() send DEBUG to INFO: off; debug function list: []
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.02s  rif40_startup(): search_path not set for: rif40
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.02s  rif40_startup(): SQL> DROP FUNCTION IF EXISTS rif40.rif40_run_study(INTEGER, INTEGER);
psql:pg_rif40_load_seer_covariates.sql:97: NOTICE:  function rif40.rif40_run_study(pg_catalog.int4,pg_catalog.int4) does not exist, skipping
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.03s  rif40_startup(): Temporary table: g_rif40_comparison_areas exists
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.04s  rif40_startup(): PostGIS extension V2.3.5 (POSTGIS="2.3.5 r16110" GEOS="3.6.2-CAPI-1.10.2 4d2925d" PROJ="Rel. 4.9.3, 15 August 2016" GDAL="GDAL 2.2.2, released 2017/09/15" LIBXML="2.7.8" LIBJSON="0.12" RASTER)
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.04s  rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.04s  rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.04s  rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: rif40
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  +00000.04s  rif40_startup(): search_path: rif_data, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
psql:pg_rif40_load_seer_covariates.sql:97: INFO:  SQL> SET SESSION search_path TO rif_data /* default schema */, rif40, public, topology, gis, pop, data_load, rif40_sql_pkg, rif_studies, rif40_partitions;
DO

--
-- Check if geography is loaded
--
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT geography
                  FROM rif40_geographies a
                 WHERE a.geography  = 'USA_2014';
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.geography = 'USA_2014' THEN
                RAISE INFO 'Geography: USA_2014 loaded';
        ELSE
                RAISE EXCEPTION 'Geography: USA_2014 is NOT loaded';
        END IF;
END;
$$;
psql:pg_rif40_load_seer_covariates.sql:120: INFO:  Geography: USA_2014 loaded
DO

--
-- Check SEER_USER role is present (needs to be created by an administrator)
--
DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT rolname
                  FROM pg_roles a
                 WHERE a.rolname  = 'seer_user';
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.rolname = 'seer_user' THEN
                RAISE INFO 'SEER_USER: role is present';
        ELSE
                RAISE EXCEPTION 'SEER_USER: role is NOT present';
        END IF;
END;
$$;
psql:pg_rif40_load_seer_covariates.sql:143: INFO:  SEER_USER: role is present
DO
--
-- Remove old SEER data
--
DROP TABLE IF EXISTS rif_data.cov_cb_2014_us_county_500k;
DROP TABLE
DROP TABLE IF EXISTS rif_data.cov_cb_2014_us_state_500k;
DROP TABLE

DELETE FROM rif40_covariates
 WHERE geography = 'USA_2014';
DELETE 0


--
-- Covariates tables
--

-- Table: rif_data.cov_cb_2014_us_county_500k
CREATE TABLE rif_data.cov_cb_2014_us_county_500k
(
  year                                                                                          integer NOT NULL, -- Year
  cb_2014_us_county_500k                                                        character varying(30) NOT NULL, -- Geolevel name
  areaname                                                                                      character varying(200),
  total_poverty_all_ages                                                        INTEGER,
  pct_poverty_all_ages                                                          NUMERIC,
  pct_poverty_0_17                                                                      NUMERIC,
  pct_poverty_related_5_17                                                      NUMERIC,
  median_household_income                                                       NUMERIC,
  median_hh_income_quin                                                         INTEGER,
  med_pct_not_in_pov_quin                                                       INTEGER,
  med_pct_not_in_pov_0_17_quin                                          INTEGER,
  med_pct_not_in_pov_5_17r_quin                                         INTEGER,
  pct_white_quintile                                                            INTEGER,
  pct_black_quintile                                                            INTEGER,
  CONSTRAINT cov_cb_2014_us_county_500k_pkey PRIMARY KEY (year, cb_2014_us_county_500k)
);
CREATE TABLE

\copy cov_cb_2014_us_county_500k FROM 'cov_cb_2014_us_county_500k.csv' WITH CSV HEADER;
COPY 132553

--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM cov_cb_2014_us_county_500k;
 total
--------
 132553
(1 row)

DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT COUNT(*) AS total
                  FROM cov_cb_2014_us_county_500k;
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.total = 132553 THEN
                RAISE INFO 'Table: cov_cb_2014_us_county_500k has % rows', c1_rec.total;
        ELSE
                RAISE EXCEPTION 'Table: cov_cb_2014_us_county_500k has % rows; expecting 132553', c1_rec.total;
        END IF;
END;
$$;
psql:pg_rif40_load_seer_covariates.sql:201: INFO:  Table: cov_cb_2014_us_county_500k has 132553 rows
DO

--
-- Convert to index organised table
--
CLUSTER rif_data.cov_cb_2014_us_county_500k USING cov_cb_2014_us_county_500k_pkey;
CLUSTER

COMMENT ON TABLE rif_data.cov_cb_2014_us_county_500k
  IS 'Example covariate table for: The County at a scale of 1:500,000';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.year IS 'Year';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.cb_2014_us_county_500k IS 'County FIPS code (geolevel id)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.areaname IS 'Area (county) name';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_household_income IS 'Estimate of median household income';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.med_pct_not_in_pov_5_17r_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_white_quintile IS '% White quintile (1=least white, 5=most)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_black_quintile IS '% Black quintile (1=least black, 5=most)';
COMMENT

--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.cov_cb_2014_us_county_500k TO seer_user;
GRANT

-- Table: rif_data.cov_cb_2014_us_state_500k

CREATE TABLE rif_data.cov_cb_2014_us_state_500k
(
  year integer NOT NULL, -- Year
  cb_2014_us_state_500k character varying(30) NOT NULL, -- Geolevel name
  areaname                                                                                      character varying(200),
  total_poverty_all_ages                                                        INTEGER,
  pct_poverty_all_ages                                                          NUMERIC,
  pct_poverty_0_17                                                                      NUMERIC,
  pct_poverty_related_5_17                                                      NUMERIC,
  median_household_income                                                       NUMERIC,
  median_hh_income_quin                                                         INTEGER,
  med_pct_not_in_pov_quin                                                       INTEGER,
  med_pct_not_in_pov_0_17_quin                                          INTEGER,
  med_pct_not_in_pov_5_17r_quin                                         INTEGER,
  CONSTRAINT cov_cb_2014_us_state_500k_pkey PRIMARY KEY (year, cb_2014_us_state_500k)
);
CREATE TABLE

\copy cov_cb_2014_us_state_500k FROM 'cov_cb_2014_us_state_500k.csv' WITH CSV HEADER;
COPY 2296

SELECT COUNT(*) AS total FROM cov_cb_2014_us_state_500k;
 total
-------
  2296
(1 row)

DO LANGUAGE plpgsql $$
DECLARE
        c1 CURSOR FOR
                SELECT COUNT(*) AS total
                  FROM cov_cb_2014_us_state_500k;
        c1_rec RECORD;
BEGIN
        OPEN c1;
        FETCH c1 INTO c1_rec;
        CLOSE c1;
--
        IF c1_rec.total = 2296 THEN
                RAISE INFO 'Table: cov_cb_2014_us_state_500k has % rows', c1_rec.total;
        ELSE
                RAISE EXCEPTION 'Table: cov_cb_2014_us_state_500k has % rows; expecting 2296', c1_rec.total;
        END IF;
END;
$$;
psql:pg_rif40_load_seer_covariates.sql:270: INFO:  Table: cov_cb_2014_us_state_500k has 2296 rows
DO

--
-- Convert to index organised table
--
CLUSTER rif_data.cov_cb_2014_us_state_500k USING cov_cb_2014_us_state_500k_pkey;
CLUSTER

COMMENT ON TABLE rif_data.cov_cb_2014_us_state_500k
  IS 'Example covariate table for: The State at a scale of 1:500,000';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.year IS 'Year';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_household_income IS 'Estimate of median household income';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.med_pct_not_in_pov_5_17r_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
COMMENT

--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.cov_cb_2014_us_state_500k TO seer_user;
GRANT

--
-- RIF40_COVARIATES integration. Continuous variable type (2) not yet supported.
-- * Add ethnicity: % white, black quintilised
--
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_COUNTY_500K',        /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MEDIAN_HH_INCOME_QUIN',         /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(median_hh_income_quin),      /* Minimum value */
       MAX(median_hh_income_quin),      /* Maximum value */
           1                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_county_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_COUNTY_500K',        /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MED_PCT_NOT_IN_POV_QUIN',       /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(med_pct_not_in_pov_quin),    /* Minimum value */
       MAX(med_pct_not_in_pov_quin),    /* Maximum value */
           1                                                            /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_county_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_COUNTY_500K',        /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MED_PCT_NOT_IN_POV_0_17_QUIN',  /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(med_pct_not_in_pov_0_17_quin),       /* Minimum value */
       MAX(med_pct_not_in_pov_0_17_quin),       /* Maximum value */
           1                                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_county_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_COUNTY_500K',        /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MED_PCT_NOT_IN_POV_5_17R_QUIN',         /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(med_pct_not_in_pov_5_17r_quin),      /* Minimum value */
       MAX(med_pct_not_in_pov_5_17r_quin),      /* Maximum value */
           1                                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_county_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_COUNTY_500K',        /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'PCT_WHITE_QUINTILE',            /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(pct_white_quintile),         /* Minimum value */
       MAX(pct_white_quintile),         /* Maximum value */
           1                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_county_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_COUNTY_500K',        /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'PCT_BLACK_QUINTILE',            /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(pct_black_quintile),         /* Minimum value */
       MAX(pct_black_quintile),         /* Maximum value */
           1                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_county_500k;
INSERT 0 1

INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_STATE_500K',         /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MEDIAN_HH_INCOME_QUIN',         /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(median_hh_income_quin),      /* Minimum value */
       MAX(median_hh_income_quin),      /* Maximum value */
           1                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_state_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_STATE_500K',         /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MED_PCT_NOT_IN_POV_QUIN',       /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(med_pct_not_in_pov_quin),    /* Minimum value */
       MAX(med_pct_not_in_pov_quin),    /* Maximum value */
           1                                                            /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_state_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_STATE_500K',         /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MED_PCT_NOT_IN_POV_0_17_QUIN',  /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(med_pct_not_in_pov_0_17_quin),       /* Minimum value */
       MAX(med_pct_not_in_pov_0_17_quin),       /* Maximum value */
           1                                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_state_500k;
INSERT 0 1
INSERT INTO rif40_covariates(geography, geolevel_name, covariate_name, min, max, type)
SELECT 'USA_2014',                                      /* Geography (e.g EW2001) */
       'CB_2014_US_STATE_500K',         /* Name of geolevel. This will be a column name in the numerator/denominator tables */
       'MED_PCT_NOT_IN_POV_5_17R_QUIN',         /* Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE */
       MIN(med_pct_not_in_pov_5_17r_quin),      /* Minimum value */
       MAX(med_pct_not_in_pov_5_17r_quin),      /* Maximum value */
           1                                                                    /* Type: integer score */
  FROM rif_data.cov_cb_2014_us_state_500k;
INSERT 0 1

--
-- End transaction (COMMIT)
--
END;
COMMIT

--
-- Eof

--
-- Analyse tables
--
ANALYZE VERBOSE rif_data.seer_cancer;
psql:pg_rif40_load_seer.sql:70: WARNING:  skipping "seer_cancer" --- cannot analyze non-tables or special system tables
ANALYZE
ANALYZE VERBOSE rif_data.seer_population;
psql:pg_rif40_load_seer.sql:71: INFO:  analyzing "rif_data.seer_population"
psql:pg_rif40_load_seer.sql:71: INFO:  "seer_population": scanned 5123 of 5123 pages, containing 614360 live rows and 0 dead rows; 30000 rows in sample, 614360 estimated total rows
ANALYZE
ANALYZE VERBOSE rif_data.cov_cb_2014_us_county_500k;
psql:pg_rif40_load_seer.sql:72: INFO:  analyzing "rif_data.cov_cb_2014_us_county_500k"
psql:pg_rif40_load_seer.sql:72: INFO:  "cov_cb_2014_us_county_500k": scanned 1745 of 1745 pages, containing 132553 live rows and 0 dead rows; 30000 rows in sample, 132553 estimated total rows
ANALYZE
ANALYZE VERBOSE rif_data.cov_cb_2014_us_state_500k;
psql:pg_rif40_load_seer.sql:73: INFO:  analyzing "rif_data.cov_cb_2014_us_state_500k"
psql:pg_rif40_load_seer.sql:73: INFO:  "cov_cb_2014_us_state_500k": scanned 29 of 29 pages, containing 2296 live rows and 0 dead rows; 2296 rows in sample, 2296 estimated total rows
ANALYZE

--
-- Eof
```