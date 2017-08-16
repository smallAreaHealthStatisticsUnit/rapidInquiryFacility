-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load SEER data into RIF; Does not reformat into RIF4.0 format (see load_seer.sql)
--								  * Requires the "seer_user" role
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
-- Usage: psql -U rif40 -w -e -f pg_rif40_load_seer.sql
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

--
-- Start transaction
--
BEGIN TRANSACTION;

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

--
-- Check SEER_USER role is present (needs to be created by an adminstrator)
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

--
-- Remove old SEER data
--
DROP TABLE IF EXISTS rif_data.seer_cancer;

DELETE FROM rif40.rif40_table_outcomes 
 WHERE numer_tab='SEER_CANCER';
 
DELETE FROM rif40.rif40_tables 
 WHERE table_name='SEER_CANCER';

DROP TABLE IF EXISTS rif_data.seer_population;

DELETE FROM rif40.rif40_tables 
WHERE table_name='SEER_POPULATION';

--
-- Create SEER_CANCER numerator table
--
CREATE TABLE rif_data.seer_cancer
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
  CONSTRAINT seer_cancer_asg_ck CHECK (age_sex_group >= 100 AND age_sex_group <= 121 OR age_sex_group >= 200 AND age_sex_group <= 221 OR (age_sex_group = ANY (ARRAY[199, 299])))
);

COMMENT ON TABLE rif_data.seer_cancer
  IS 'SEER Cancer data 1973-2013. 9 States in total';
COMMENT ON COLUMN rif_data.seer_cancer.year IS 'Year';
COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN rif_data.seer_cancer.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN rif_data.seer_cancer.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
COMMENT ON COLUMN rif_data.seer_cancer.icdot10v IS 'ICD 10 site code - recoded from ICD-O-2 to 10';
COMMENT ON COLUMN rif_data.seer_cancer.pubcsnum IS 'Patient ID';
COMMENT ON COLUMN rif_data.seer_cancer.seq_num IS 'Sequence number';
COMMENT ON COLUMN rif_data.seer_cancer.histo3v IS 'Histologic Type ICD-O-3';
COMMENT ON COLUMN rif_data.seer_cancer.beho3v IS 'Behavior code ICD-O-3';
COMMENT ON COLUMN rif_data.seer_cancer.rac_reca IS 'Race recode A (WHITE, BLACK, OTHER)';
COMMENT ON COLUMN rif_data.seer_cancer.rac_recy IS 'Race recode Y (W, B, AI, API)';
COMMENT ON COLUMN rif_data.seer_cancer.origrecb IS 'Origin Recode NHIA (HISPANIC, NON-HISP)';
COMMENT ON COLUMN rif_data.seer_cancer.codpub IS 'Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html)';
COMMENT ON COLUMN rif_data.seer_cancer.reg IS 'SEER registry (minus 1500 so same as population file)';

--
-- Load
--
\copy rif_data.seer_cancer FROM 'seer_cancer.csv' WITH CSV HEADER;

--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM seer_cancer;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(*) AS total
 		  FROM seer_cancer;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 4863414 THEN
		RAISE INFO 'Table: seer_cancer has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: seer_cancer has % rows; expecting 4863414', c1_rec.total;
	END IF;
END;
$$;

--
-- Indexes
--
CREATE INDEX seer_cancer_age_sex_group
  ON rif_data.seer_cancer
  (age_sex_group);

CREATE INDEX seer_cancer_cb_2014_us_county_500k
  ON rif_data.seer_cancer
  (cb_2014_us_county_500k);
  
CREATE INDEX seer_cancer_cb_2014_us_nation_5m
  ON rif_data.seer_cancer
  (cb_2014_us_nation_5m);

CREATE INDEX seer_cancer_cb_2014_us_state_500k
  ON rif_data.seer_cancer
  (cb_2014_us_state_500k);

CREATE INDEX seer_cancer_icdot10v
  ON rif_data.seer_cancer
  (icdot10v);

CREATE INDEX seer_cancer_reg
  ON rif_data.seer_cancer
  (reg);

CREATE INDEX seer_cancer_year
  ON rif_data.seer_cancer
  (year);
 
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
   'cancers',			/* theme */
   'SEER_CANCER',		/* table_name */
   'SEER Cancer data 1973-2013. 9 States in total',				/* description */
   MIN(year),			/* year_start */
   MAX(year),			/* year_stop */
   NULL,				/* total_field */
   0,					/* isindirectdenominator */
   0,					/* isdirectdenominator */
   1,					/* isnumerator */
   1,					/* automatic */
   NULL,				/* sex_field_name */
   NULL,				/* age_group_field_name */
   'AGE_SEX_GROUP',		/* age_sex_group_field_name */
   1					/* age_group_id */
  FROM rif_data.seer_cancer;

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

INSERT INTO rif40.rif40_table_outcomes (
   outcome_group_name,
   numer_tab,
   current_version_start_year) 
SELECT 
   'SEER_ICDOT10V',
   'SEER_CANCER',
   MIN(year) 
FROM rif_data.seer_cancer;

--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.seer_cancer TO seer_user;
  
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
COMMENT ON TABLE rif_data.seer_population
  IS 'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total';
COMMENT ON COLUMN rif_data.seer_population.year IS 'Year';
COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN rif_data.seer_population.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN rif_data.seer_population.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
COMMENT ON COLUMN rif_data.seer_population.population IS 'Population';

--
-- Load
--
\copy rif_data.seer_population FROM 'seer_population.csv' WITH CSV HEADER;

--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM seer_population;
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
	IF c1_rec.total = 354326 THEN
		RAISE INFO 'Table: seer_population has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: seer_population has % rows; expecting 354326', c1_rec.total;
	END IF;
END;
$$;

--
-- Convert to index organised table
--
CLUSTER rif_data.seer_population USING seer_population_pk;

--
-- Indexes
--
CREATE INDEX seer_population_age_sex_group
  ON rif_data.seer_population
  (age_sex_group);

CREATE INDEX seer_population_cb_2014_us_county_500k
  ON rif_data.seer_population
  (cb_2014_us_county_500k);

CREATE INDEX seer_population_cb_2014_us_nation_5m
  ON rif_data.seer_population
  (cb_2014_us_nation_5m);

CREATE INDEX seer_population_cb_2014_us_state_500k
  ON rif_data.seer_population
  (cb_2014_us_state_500k);

CREATE INDEX seer_population_year
  ON rif_data.seer_population
  (year);

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
   'cancers',		/* theme */
   'SEER_POPULATION',	/* table_name */
   'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total',	/* description */
   MIN(YEAR),		/* year_start */
   MAX(YEAR),		/* year_stop */
   'POPULATION',			/* total_field */		
   1,				/* isindirectdenominator */
   0,				/* isdirectdenominator */
   0,				/* isnumerator */
   1,				/* automatic */
   NULL,			/* sex_field_name */
   NULL,			/* age_group_field_name */
   'AGE_SEX_GROUP',	/* age_sex_group_field_name */
   1				/* age_group_id */
FROM rif_data.seer_population;
 
--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.seer_population TO seer_user;

--
-- US Census Small Area Income and Poverty Estimates 1989-2015 by county
--

DROP TABLE IF EXISTS rif_data.saipe_county_poverty_1989_2015;

CREATE TABLE rif_data.saipe_county_poverty_1989_2015
(
  year integer NOT NULL, -- Year
  cb_2014_us_nation_5m text, -- United States to county level including territories
  cb_2014_us_state_500k text, -- State geographic Names Information System (GNIS) code
  cb_2014_us_county_500k text NOT NULL, -- County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.
  total_poverty_all_ages integer, -- Estimate of people of all ages in poverty
  pct_poverty_all_ages numeric, -- Estimate percent of people of all ages in poverty
  pct_poverty_0_17 numeric, -- Estimated percent of people age 0-17 in poverty
  pct_poverty_related_5_17 numeric, -- Estimated percent of related children age 5-17 in families in poverty
  median_household_income integer, -- Estimate of median household income
  median_household_income_qunitile integer, -- Quintile: estimate of median household income (1=most deprived, 5=least)
  median_pct_not_in_poverty_0_17_qunitile integer, -- Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)
  median_pct_not_in_poverty_related_5_17_qunitile integer, -- Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)
  median_pct_not_in_poverty_all_ages_qunitile integer, -- Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)
  CONSTRAINT saipe_county_poverty_1989_2015_pk PRIMARY KEY (year, cb_2014_us_county_500k)
);

--
-- Load
--
\copy rif_data.saipe_county_poverty_1989_2015 FROM 'saipe_county_poverty_1989_2015.csv' WITH CSV HEADER;

--
-- Convert to index organised table
--
CLUSTER rif_data.saipe_county_poverty_1989_2015 USING saipe_county_poverty_1989_2015_pk;

COMMENT ON TABLE rif_data.saipe_county_poverty_1989_2015
  IS 'US Census Small Area Income and Poverty Estimates 1989-2015 by county';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.year IS 'Year';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.median_household_income IS 'Estimate of median household income';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.median_household_income_qunitile IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.median_pct_not_in_poverty_0_17_qunitile IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.median_pct_not_in_poverty_related_5_17_qunitile IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.saipe_county_poverty_1989_2015.median_pct_not_in_poverty_all_ages_qunitile IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';


-- Index: peter.saipe_county_poverty_1989_2015_cb_2014_us_county_500k

-- DROP INDEX peter.saipe_county_poverty_1989_2015_cb_2014_us_county_500k;

CREATE INDEX saipe_county_poverty_1989_2015_cb_2014_us_county_500k
  ON rif_data.saipe_county_poverty_1989_2015
  (cb_2014_us_county_500k);

-- Index: peter.saipe_county_poverty_1989_2015_cb_2014_us_nation_5m

-- DROP INDEX peter.saipe_county_poverty_1989_2015_cb_2014_us_nation_5m;

CREATE INDEX saipe_county_poverty_1989_2015_cb_2014_us_nation_5m
  ON rif_data.saipe_county_poverty_1989_2015
  (cb_2014_us_nation_5m);

-- Index: peter.saipe_county_poverty_1989_2015_cb_2014_us_state_500k

-- DROP INDEX peter.saipe_county_poverty_1989_2015_cb_2014_us_state_500k;

CREATE INDEX saipe_county_poverty_1989_2015_cb_2014_us_state_500k
  ON rif_data.saipe_county_poverty_1989_2015
  (cb_2014_us_state_500k);

-- Index: peter.saipe_county_poverty_1989_2015_year

-- DROP INDEX peter.saipe_county_poverty_1989_2015_year;

CREATE INDEX saipe_county_poverty_1989_2015_year
  ON rif_data.saipe_county_poverty_1989_2015
  (year);
 
--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.saipe_county_poverty_1989_2015 TO seer_user;

-- Table: peter.saipe_state_poverty_1989_2015

DROP TABLE IF EXISTS rif_data.saipe_state_poverty_1989_2015;

CREATE TABLE rif_data.saipe_state_poverty_1989_2015
(
  year integer NOT NULL, -- Year
  cb_2014_us_nation_5m text, -- United States to county level including territories
  cb_2014_us_state_500k text NOT NULL, -- State geographic Names Information System (GNIS) code
  total_poverty_all_ages integer, -- Estimate of people of all ages in poverty
  pct_poverty_all_ages numeric, -- Estimate percent of people of all ages in poverty
  pct_poverty_0_17 numeric, -- Estimated percent of people age 0-17 in poverty
  pct_poverty_related_5_17 numeric, -- Estimated percent of related children age 5-17 in families in poverty
  median_household_income integer, -- Estimate of median household income
  median_household_income_qunitile integer, -- Quintile: estimate of median household income (1=most deprived, 5=least)
  median_pct_not_in_poverty_0_17_qunitile integer, -- Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)
  median_pct_not_in_poverty_related_5_17_qunitile integer, -- Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)
  median_pct_not_in_poverty_all_ages_qunitile integer, -- Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)
  CONSTRAINT saipe_state_poverty_1989_2015_pk PRIMARY KEY (year, cb_2014_us_state_500k)
);
COMMENT ON TABLE rif_data.saipe_state_poverty_1989_2015
  IS 'US Census Small Area Income and Poverty Estimates 1989-2015 by county';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.year IS 'Year';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.median_household_income IS 'Estimate of median household income';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.median_household_income_qunitile IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.median_pct_not_in_poverty_0_17_qunitile IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.median_pct_not_in_poverty_related_5_17_qunitile IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.saipe_state_poverty_1989_2015.median_pct_not_in_poverty_all_ages_qunitile IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';

--
-- Load
--
\copy rif_data.saipe_state_poverty_1989_2015 FROM 'saipe_state_poverty_1989_2015.csv' WITH CSV HEADER;

--
-- Convert to index organised table
--
CLUSTER rif_data.saipe_state_poverty_1989_2015 USING saipe_state_poverty_1989_2015_pk;

-- Index: peter.saipe_state_poverty_1989_2015_cb_2014_us_nation_5m

-- DROP INDEX peter.saipe_state_poverty_1989_2015_cb_2014_us_nation_5m;

CREATE INDEX saipe_state_poverty_1989_2015_cb_2014_us_nation_5m
  ON rif_data.saipe_state_poverty_1989_2015
  (cb_2014_us_nation_5m);

-- Index: peter.saipe_state_poverty_1989_2015_cb_2014_us_state_500k

-- DROP INDEX peter.saipe_state_poverty_1989_2015_cb_2014_us_state_500k;

CREATE INDEX saipe_state_poverty_1989_2015_cb_2014_us_state_500k
  ON rif_data.saipe_state_poverty_1989_2015
  (cb_2014_us_state_500k);

-- Index: peter.saipe_state_poverty_1989_2015_year

-- DROP INDEX peter.saipe_state_poverty_1989_2015_year;

CREATE INDEX saipe_state_poverty_1989_2015_year
  ON rif_data.saipe_state_poverty_1989_2015
  (year);

--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.saipe_county_poverty_1989_2015 TO seer_user;

--
-- Covariates tables
--

-- Table: rif_data.cov_cb_2014_us_county_500k

DROP TABLE IF EXISTS rif_data.cov_cb_2014_us_county_500k;

CREATE TABLE rif_data.cov_cb_2014_us_county_500k
(
  year 												integer NOT NULL, -- Year
  cb_2014_us_county_500k 							character varying(30) NOT NULL, -- Geolevel name
  areaname					 						character varying(200),
  total_poverty_all_ages							INTEGER,
  pct_poverty_all_ages								INTEGER,
  pct_poverty_0_17									INTEGER,
  pct_poverty_related_5_17							INTEGER,
  median_household_income							INTEGER,
  median_household_income_qunitile					INTEGER,
  median_pct_not_in_poverty_all_ages_qunitile		INTEGER,
  median_pct_not_in_poverty_0_17_qunitile			INTEGER,
  median_pct_not_in_poverty_related_5_17_qunitile	INTEGER,
  CONSTRAINT cov_cb_2014_us_county_500k_pkey PRIMARY KEY (year, cb_2014_us_county_500k)
);

INSERT INTO rif_data.cov_cb_2014_us_county_500k(year, cb_2014_us_county_500k, areaname,
       total_poverty_all_ages,
	   pct_poverty_all_ages,
	   pct_poverty_0_17,
	   pct_poverty_related_5_17,
	   median_household_income,
	   median_household_income_qunitile,
	   median_pct_not_in_poverty_all_ages_qunitile,
	   median_pct_not_in_poverty_0_17_qunitile,
	   median_pct_not_in_poverty_related_5_17_qunitile)
WITH a AS (
	SELECT generate_series(year_start::INTEGER, year_stop::INTEGER) AS year
	  FROM rif40.rif40_tables
	 WHERE table_name = 'SEER_POPULATION'
), b AS (
	SELECT a.year, b.cb_2014_us_county_500k, b.areaname
	  FROM a CROSS JOIN rif_data.lookup_cb_2014_us_county_500k b
)
SELECT b.year, b.cb_2014_us_county_500k, b.areaname,
       c.total_poverty_all_ages,
	   c.pct_poverty_all_ages,
	   c.pct_poverty_0_17,
	   c.pct_poverty_related_5_17,
	   c.median_household_income,
	   c.median_household_income_qunitile,
	   c.median_pct_not_in_poverty_all_ages_qunitile,
	   c.median_pct_not_in_poverty_0_17_qunitile,
	   c.median_pct_not_in_poverty_related_5_17_qunitile
  FROM b
	LEFT OUTER JOIN rif_data.saipe_county_poverty_1989_2015 c ON 
		(b.year = c.year AND b.cb_2014_us_county_500k = c.cb_2014_us_county_500k)
 ORDER BY 1, 2;
 
--
-- Convert to index organised table
--
CLUSTER rif_data.cov_cb_2014_us_county_500k USING cov_cb_2014_us_county_500k_pkey;

COMMENT ON TABLE rif_data.cov_cb_2014_us_county_500k
  IS 'Example covariate table for: The County at a scale of 1:500,000';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.year IS 'Year';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.cb_2014_us_county_500k IS 'County FIPS code (geolevel id)';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.areaname IS 'Area (county) name';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_household_income IS 'Estimate of median household income';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_household_income_qunitile IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_pct_not_in_poverty_all_ages_qunitile IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_pct_not_in_poverty_0_17_qunitile IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_county_500k.median_pct_not_in_poverty_related_5_17_qunitile IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';
 
-- Table: rif_data.cov_cb_2014_us_state_500k

DROP TABLE IF EXISTS rif_data.cov_cb_2014_us_state_500k;

CREATE TABLE rif_data.cov_cb_2014_us_state_500k
(
  year integer NOT NULL, -- Year
  cb_2014_us_state_500k character varying(30) NOT NULL, -- Geolevel name
  areaname					 						character varying(200),
  total_poverty_all_ages							INTEGER,
  pct_poverty_all_ages								INTEGER,
  pct_poverty_0_17									INTEGER,
  pct_poverty_related_5_17							INTEGER,
  median_household_income							INTEGER,
  median_household_income_qunitile					INTEGER,
  median_pct_not_in_poverty_all_ages_qunitile		INTEGER,
  median_pct_not_in_poverty_0_17_qunitile			INTEGER,
  median_pct_not_in_poverty_related_5_17_qunitile	INTEGER,
  CONSTRAINT cov_cb_2014_us_state_500k_pkey PRIMARY KEY (year, cb_2014_us_state_500k)
);

INSERT INTO rif_data.cov_cb_2014_us_state_500k(year, cb_2014_us_state_500k, areaname,
       total_poverty_all_ages,
	   pct_poverty_all_ages,
	   pct_poverty_0_17,
	   pct_poverty_related_5_17,
	   median_household_income,
	   median_household_income_qunitile,
	   median_pct_not_in_poverty_all_ages_qunitile,
	   median_pct_not_in_poverty_0_17_qunitile,
	   median_pct_not_in_poverty_related_5_17_qunitile)
WITH a AS (
	SELECT generate_series(year_start::INTEGER, year_stop::INTEGER) AS year
	  FROM rif40.rif40_tables
	 WHERE table_name = 'SEER_POPULATION'
), b AS (
	SELECT a.year, b.cb_2014_us_state_500k, b.areaname
	  FROM a CROSS JOIN rif_data.lookup_cb_2014_us_state_500k b
)
SELECT b.year, b.cb_2014_us_state_500k, b.areaname,
       c.total_poverty_all_ages,
	   c.pct_poverty_all_ages,
	   c.pct_poverty_0_17,
	   c.pct_poverty_related_5_17,
	   c.median_household_income,
	   c.median_household_income_qunitile,
	   c.median_pct_not_in_poverty_all_ages_qunitile,
	   c.median_pct_not_in_poverty_0_17_qunitile,
	   c.median_pct_not_in_poverty_related_5_17_qunitile
  FROM b
	LEFT OUTER JOIN rif_data.saipe_state_poverty_1989_2015 c ON 
		(b.year = c.year AND b.cb_2014_us_state_500k = c.cb_2014_us_state_500k)
 ORDER BY 1, 2;
 
--
-- Convert to index organised table
--
CLUSTER rif_data.cov_cb_2014_us_state_500k USING cov_cb_2014_us_state_500k_pkey;

COMMENT ON TABLE rif_data.cov_cb_2014_us_state_500k
  IS 'Example covariate table for: The State at a scale of 1:500,000';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.year IS 'Year';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.cb_2014_us_state_500k IS 'Geolevel name';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_household_income IS 'Estimate of median household income';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_household_income_qunitile IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_pct_not_in_poverty_all_ages_qunitile IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_pct_not_in_poverty_0_17_qunitile IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN rif_data.cov_cb_2014_us_state_500k.median_pct_not_in_poverty_related_5_17_qunitile IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';

--
-- Grant
-- * The role SEER_USER needs to be created by an administrator
--
GRANT SELECT ON rif_data.cov_cb_2014_us_state_500k TO seer_user;

--
-- End transaction (COMMIT)
--
END;

--
-- Analyse tables
--
ANALYZE VERBOSE rif_data.seer_cancer;
ANALYZE VERBOSE rif_data.seer_population;
ANALYZE VERBOSE rif_data.saipe_county_poverty_1989_2015;
ANALYZE VERBOSE rif_data.saipe_state_poverty_1989_2015;
ANALYZE VERBOSE rif_data.cov_cb_2014_us_county_500k;
ANALYZE VERBOSE rif_data.cov_cb_2014_us_state_500k;

--
-- Eof