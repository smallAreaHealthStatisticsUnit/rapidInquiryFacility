
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load SEER data; reformat into RIF4.0 format. Does not load into RIF (see rif40_load_seer.sql)
--                                Postgres script
--
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
-- Usage: psql -w -e -f pg_load_seer.sql 
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
-- Requires RIF USA county level geography to be loaded: rif_pg_USA_2014.sql
-- Also requires USA county level base tables to be loaded: pg_USA_2014.sql
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

BEGIN TRANSACTION;

\i pg_load_seer_cancer.sql
\i pg_load_seer_population.sql
 
DROP TABLE IF EXISTS saipe_state_county_yr1989_2015_fixed_length;
DROP TABLE IF EXISTS saipe_state_county_yr1989_2015;

--
-- Load est*all.txt as a fixed length record
--
CREATE TABLE saipe_state_county_yr1989_2015_fixed_length (
	record_value VARCHAR(265),
	year INTEGER
);

/*
wc -l US_census_county_poverty_estimates/est*.txt
    3192 US_census_county_poverty_estimates/est00ALL.txt
    3193 US_census_county_poverty_estimates/est01ALL.txt
    3193 US_census_county_poverty_estimates/est02ALL.txt
    3193 US_census_county_poverty_estimates/est03ALL.txt
    3193 US_census_county_poverty_estimates/est04ALL.txt
    3193 US_census_county_poverty_estimates/est05ALL.txt
    3193 US_census_county_poverty_estimates/est06ALL.txt
    3193 US_census_county_poverty_estimates/est07ALL.txt
    3194 US_census_county_poverty_estimates/est08ALL.txt
    3195 US_census_county_poverty_estimates/est09ALL.txt
    3195 US_census_county_poverty_estimates/est10ALL.txt
    3195 US_census_county_poverty_estimates/est11all.txt
    3195 US_census_county_poverty_estimates/est12ALL.txt
    3195 US_census_county_poverty_estimates/est13ALL.txt
    3194 US_census_county_poverty_estimates/est14ALL.txt
    3194 US_census_county_poverty_estimates/est15ALL.txt
    3193 US_census_county_poverty_estimates/est89ALL.txt
    3195 US_census_county_poverty_estimates/est93ALL.txt
    3194 US_census_county_poverty_estimates/est95ALL.txt
    3193 US_census_county_poverty_estimates/est97ALL.txt
    3193 US_census_county_poverty_estimates/est98ALL.txt
    3193 US_census_county_poverty_estimates/est99ALL.txt
   70261 total
 */
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est15all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2015 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est14all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2014 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est13all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2013 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est12all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2012 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est11all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2011 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est10all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2010 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est09all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2009 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est08all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2008 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est07all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2007 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est06all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2006 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est05all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2005 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est04all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2004 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est03all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2003 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est02all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2002 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est01all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2001 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est00all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 2000 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est99all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1999 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est98all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1998 WHERE year IS NULL;
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est97all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1997 WHERE year IS NULL;
-- No county level 96 data
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est95all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1995 WHERE year IS NULL;
-- No county level 94 data
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est93all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1993 WHERE year IS NULL;
-- No county level 90-92 data
\copy saipe_state_county_yr1989_2015_fixed_length(record_value) FROM 'US_census_county_poverty_estimates\est89all.txt' WITH CSV;
UPDATE saipe_state_county_yr1989_2015_fixed_length SET year = 1989 WHERE year IS NULL;

--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM saipe_state_county_yr1989_2015_fixed_length;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(*) AS total
 		  FROM saipe_state_county_yr1989_2015_fixed_length;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 70261 THEN
		RAISE INFO 'Table: saipe_state_county_yr1989_2015_fixed_length has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: saipe_state_county_yr1989_2015_fixed_length has % rows; expecting 70261', c1_rec.total;
	END IF;
END;
$$;

SELECT * FROM saipe_state_county_yr1989_2015_fixed_length LIMIT 10;
--
-- Convert datatypes
--
-- Clean FIPS codes to standard NNN/NN format (i.e replace spaces with zeros)
CREATE TABLE saipe_state_county_yr1989_2015
AS
SELECT LPAD(LTRIM(SUBSTRING(record_value FROM 1 FOR 2)), 2, '0')::Text AS state_fips_code,	 	/* State FIPS code (00 for US record) */
       LPAD(LTRIM(SUBSTRING(record_value FROM 4 FOR 3)), 3, '0')::Text AS county_fips_code, 	/* County FIPS code ( 0 for US or state level records) */ 
       CASE 
			WHEN LTRIM(SUBSTRING(record_value FROM 8 FOR 8)) IN ('.', '') THEN NULL::INTEGER
			ELSE LTRIM(SUBSTRING(record_value FROM 8 FOR 8))::INTEGER
	   END AS total_poverty_all_ages, 	/* Estimate of people of all ages in poverty */ 
       CASE 
			WHEN LTRIM(SUBSTRING(record_value FROM 35 FOR 4)) IN ('. ', '') THEN NULL::NUMERIC
			ELSE LTRIM(SUBSTRING(record_value FROM 35 FOR 4))::NUMERIC
	   END AS pct_poverty_all_ages, 	/* Estimate percent of people of all ages in poverty */ 
       CASE 
			WHEN LTRIM(SUBSTRING(record_value FROM 77 FOR 4)) IN ('. ', '') THEN NULL::NUMERIC
			ELSE LTRIM(SUBSTRING(record_value FROM 77 FOR 4))::NUMERIC
	   END AS pct_poverty_0_17, 	/* Estimated percent of people age 0-17 in poverty */
       CASE 
			WHEN LTRIM(SUBSTRING(record_value FROM 119 FOR 4)) IN ('. ', '') THEN NULL::NUMERIC
			ELSE LTRIM(SUBSTRING(record_value FROM 119 FOR 4))::NUMERIC
	   END AS pct_poverty_related_5_17, 	/* Estimated percent of related children age 5-17 in families in poverty */
       CASE 
			WHEN LTRIM(SUBSTRING(record_value FROM 134 FOR 6)) IN ('.', '') THEN NULL::INTEGER
			ELSE LTRIM(SUBSTRING(record_value FROM 134 FOR 6))::INTEGER
	   END AS median_household_income, 	/* Estimate of median household income */	   
       SUBSTRING(record_value FROM 243 FOR 12) AS file_name,
	   CASE 
			WHEN SUBSTRING(record_value FROM 246 FOR 2) = '' THEN NULL::INTEGER
			ELSE SUBSTRING(record_value FROM 246 FOR 2)::INTEGER
	   END AS yr,
	   year
  FROM saipe_state_county_yr1989_2015_fixed_length;

SELECT * FROM saipe_state_county_yr1989_2015 LIMIT 10;
  
--
-- Extract Small Area Income and Poverty Estimates
-- * Convert FIPS codes to the geographic Names Information System (GNIS) codes used by the RIF
-- * Enforce primary key (cb_2014_us_county_500k, year)
-- * Convert unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g.
--   900 series to represent county/independent city combinations in Virginia.
-- * Quintilise, reversing the sence of the poverty percentages so that quintile 1 is the most deprived: 
--	   pct_not_in_poverty_all_ages,
--	   pct_not_in_poverty_0_17,
--	   pct_not_in_poverty_related_5_17,
--	   median_household_income
--

DROP TABLE IF EXISTS saipe_state_poverty_1989_2015;  
CREATE TABLE saipe_state_poverty_1989_2015
AS  
WITH a AS (
	SELECT CASE 
				WHEN yr IS NULL THEN year
				WHEN yr < 50 THEN 2000+yr 
				ELSE 1900+yr
		   END AS year, 
		   'US'::Text AS cb_2014_us_nation_5m,
		   d.statens AS cb_2014_us_state_500k, 
		   total_poverty_all_ages,
		   pct_poverty_all_ages,
		   pct_poverty_0_17,
		   pct_poverty_related_5_17,
		   median_household_income
	  FROM saipe_state_county_yr1989_2015 a
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
	 WHERE a.county_fips_code = '000'
	   AND a.state_fips_code  != '00'
)
SELECT a.year, a.cb_2014_us_nation_5m, a.cb_2014_us_state_500k, 
	   a.total_poverty_all_ages,
	   a.pct_poverty_all_ages,
	   a.pct_poverty_0_17,
	   a.pct_poverty_related_5_17,
	   a.median_household_income,
	   CASE WHEN a.median_household_income IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY a.median_household_income) ELSE NULL END AS median_hh_income_quin,
	   CASE WHEN a.pct_poverty_0_17 IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_0_17) ELSE NULL END AS med_pct_not_in_pov_0_17_quin,
	   CASE WHEN a.pct_poverty_related_5_17 IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_related_5_17) ELSE NULL END AS med_pct_not_in_pov_5_17rel_quin,
	   CASE WHEN a.pct_poverty_all_ages IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_all_ages) ELSE NULL END AS med_pct_not_in_pov_quin
  FROM a
 ORDER BY 1,2,3;
--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM saipe_state_poverty_1989_2015;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(*) AS total
 		  FROM saipe_state_poverty_1989_2015;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 1122 THEN
		RAISE INFO 'Table: saipe_state_poverty_1989_2015 has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: saipe_state_poverty_1989_2015 has % rows; expecting 1122', c1_rec.total;
	END IF;
END;
$$;

COMMENT ON TABLE saipe_state_poverty_1989_2015 IS 'US Census Small Area Income and Poverty Estimates 1989-2015 by county';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.year IS 'Year';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.median_household_income IS 'Estimate of median household income';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN saipe_state_poverty_1989_2015.med_pct_not_in_pov_5_17rel_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';

ALTER TABLE saipe_state_poverty_1989_2015 ADD CONSTRAINT saipe_state_poverty_1989_2015_pk 
	PRIMARY KEY (year, cb_2014_us_state_500k);	
CREATE INDEX saipe_state_poverty_1989_2015_year ON saipe_state_poverty_1989_2015 (year);
CREATE INDEX saipe_state_poverty_1989_2015_cb_2014_us_nation_5m ON saipe_state_poverty_1989_2015(cb_2014_us_nation_5m);
CREATE INDEX saipe_state_poverty_1989_2015_cb_2014_us_state_500k ON saipe_state_poverty_1989_2015(cb_2014_us_state_500k);

CLUSTER saipe_state_poverty_1989_2015 USING saipe_state_poverty_1989_2015_pk;
	
\copy saipe_state_poverty_1989_2015 TO 'saipe_state_poverty_1989_2015.csv' WITH CSV HEADER;
\dS+ saipe_state_poverty_1989_2015

DROP TABLE IF EXISTS saipe_county_poverty_1989_2015;  
CREATE TABLE saipe_county_poverty_1989_2015
AS  
WITH a AS (
	SELECT CASE 
				WHEN yr IS NULL THEN year
				WHEN yr < 50 THEN 2000+yr 
				ELSE 1900+yr
		   END AS year, 
		   'US'::Text AS cb_2014_us_nation_5m,
		   d.statens AS cb_2014_us_state_500k, 
		   COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code) AS cb_2014_us_county_500k,
		   total_poverty_all_ages,
		   pct_poverty_all_ages,
		   pct_poverty_0_17,
		   pct_poverty_related_5_17,
		   median_household_income
	  FROM saipe_state_county_yr1989_2015 a
			LEFT OUTER JOIN cb_2014_us_county_500k c ON (a.state_fips_code = c.statefp AND a.county_fips_code = c.countyfp)
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
	 WHERE a.county_fips_code != '000'
)
SELECT a.year, a.cb_2014_us_nation_5m, a.cb_2014_us_state_500k, a.cb_2014_us_county_500k,
	   a.total_poverty_all_ages,
	   a.pct_poverty_all_ages,
	   a.pct_poverty_0_17,
	   a.pct_poverty_related_5_17,
	   a.median_household_income,
	   CASE WHEN a.median_household_income IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY a.median_household_income) ELSE NULL END AS median_hh_income_quin,
	   CASE WHEN a.pct_poverty_0_17 IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_0_17) ELSE NULL END AS med_pct_not_in_pov_0_17_quin,
	   CASE WHEN a.pct_poverty_related_5_17 IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_related_5_17) ELSE NULL END AS med_pct_not_in_pov_5_17rel_quin,
	   CASE WHEN a.pct_poverty_all_ages IS NOT NULL THEN NTILE(5) OVER (PARTITION BY a.year ORDER BY 100-a.pct_poverty_all_ages) ELSE NULL END AS med_pct_not_in_pov_quin
  FROM a
 ORDER BY 1,2,3,4;
 
--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM saipe_county_poverty_1989_2015;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(*) AS total
 		  FROM saipe_county_poverty_1989_2015;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 69117 THEN
		RAISE INFO 'Table: saipe_county_poverty_1989_2015 has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: saipe_county_poverty_1989_2015 has % rows; expecting 69117', c1_rec.total;
	END IF;
END;
$$;
 
COMMENT ON TABLE saipe_county_poverty_1989_2015 IS 'US Census Small Area Income and Poverty Estimates 1989-2015 by county';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.year IS 'Year';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.total_poverty_all_ages IS 'Estimate of people of all ages in poverty';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.pct_poverty_all_ages IS 'Estimate percent of people of all ages in poverty';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.pct_poverty_0_17 IS 'Estimated percent of people age 0-17 in poverty';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.pct_poverty_related_5_17 IS 'Estimated percent of related children age 5-17 in families in poverty';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.median_household_income IS 'Estimate of median household income';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.median_hh_income_quin IS 'Quintile: estimate of median household income (1=most deprived, 5=least)';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.med_pct_not_in_pov_quin IS 'Quintile: estimate percent of people of all ages NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.med_pct_not_in_pov_0_17_quin IS 'Quintile: estimated percent of people age 0-17 NOT in poverty (1=most deprived, 5=least)';
COMMENT ON COLUMN saipe_county_poverty_1989_2015.med_pct_not_in_pov_5_17rel_quin IS 'Quintile: estimated percent of related children age 5-17 in families NOT in poverty (1=most deprived, 5=least)';

ALTER TABLE saipe_county_poverty_1989_2015 ADD CONSTRAINT saipe_county_poverty_1989_2015_pk 
	PRIMARY KEY (year, cb_2014_us_county_500k);	
CREATE INDEX saipe_county_poverty_1989_2015_year ON saipe_county_poverty_1989_2015 (year);
CREATE INDEX saipe_county_poverty_1989_2015_cb_2014_us_nation_5m ON saipe_county_poverty_1989_2015(cb_2014_us_nation_5m);
CREATE INDEX saipe_county_poverty_1989_2015_cb_2014_us_state_500k ON saipe_county_poverty_1989_2015(cb_2014_us_state_500k);
CREATE INDEX saipe_county_poverty_1989_2015_cb_2014_us_county_500k ON saipe_county_poverty_1989_2015(cb_2014_us_county_500k);

CLUSTER saipe_county_poverty_1989_2015 USING saipe_county_poverty_1989_2015_pk;
	
\copy saipe_county_poverty_1989_2015 TO 'saipe_county_poverty_1989_2015.csv' WITH CSV HEADER;
\dS+ saipe_county_poverty_1989_2015

-- 
-- Extract ethnicity covariates
-- * Convert age, sex to RIF age_sex_group 1
-- * Convert FIPS codes to the geographic Names Information System (GNIS) codes used by the RIF
-- * Enforce primary key
-- * Check AGE_SEX_GROUP
-- * Convert unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g.
--   900 series to represent county/independent city combinations in Virginia.
--
DROP TABLE IF EXISTS seer_wbo_ethnicity; 
CREATE TABLE seer_wbo_ethnicity
AS  
WITH a AS (
	SELECT a.year, 
	       'US'::Text AS cb_2014_us_nation_5m,
	       d.statens AS cb_2014_us_state_500k, 
		   COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code) AS cb_2014_us_county_500k,
		   CASE WHEN a.year < 1992 AND a.race = 3 /* Recode other to 9 */ THEN 9 ELSE a.race END AS race, 
		   a.origin,
		   SUM(a.population) AS population
	  FROM seer_wbo_single_ages a
			LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND a.age BETWEEN b.low_age AND b.high_age)
			LEFT OUTER JOIN cb_2014_us_county_500k c ON (a.state_fips_code = c.statefp AND a.county_fips_code = c.countyfp)
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
	 GROUP BY a.year, d.statens, 
			  COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code), 
			  CASE WHEN a.year < 1992 AND a.race = 3 /* Recode other to 9 */ THEN 9 ELSE a.race END, a.origin
)
SELECT year, cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k, 
      (10*race)+COALESCE(origin, 0) AS ethnicity, a.population
  FROM a
 ORDER BY 1,2,3,4,5;

COMMENT ON TABLE seer_wbo_ethnicity IS 'SEER Ethnicity 1972-2013; white/blacks/other + hispanic/non-hispanic. 9 States in total';
COMMENT ON COLUMN seer_wbo_ethnicity.year IS 'Year';
COMMENT ON COLUMN seer_wbo_ethnicity.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN seer_wbo_ethnicity.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN seer_wbo_ethnicity.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN seer_wbo_ethnicity.ethnicity IS 'Ethnicity coded 19: White; 29: Black, 39: American Indian/Alaska Native (1992 onwards); 99: Other (1973-1991); all with no hispanic data ';
COMMENT ON COLUMN seer_wbo_ethnicity.population IS 'Population'; 
 
-- 
-- Add PK, make index organised table
--
ALTER TABLE seer_wbo_ethnicity ADD CONSTRAINT seer_wbo_ethnicity_pk 
	PRIMARY KEY (year, cb_2014_us_state_500k, cb_2014_us_county_500k, ethnicity);
CLUSTER seer_wbo_ethnicity USING seer_wbo_ethnicity_pk;

CREATE INDEX seer_wbo_ethnicity_year ON seer_wbo_ethnicity (year);
CREATE INDEX seer_wbo_ethnicity_cb_2014_us_nation_5m ON seer_wbo_ethnicity(cb_2014_us_nation_5m);
CREATE INDEX seer_wbo_ethnicity_cb_2014_us_state_500k ON seer_wbo_ethnicity(cb_2014_us_state_500k);
CREATE INDEX seer_wbo_ethnicity_cb_2014_us_county_500k ON seer_wbo_ethnicity(cb_2014_us_county_500k);
CREATE INDEX seer_wbo_ethnicity_ethnicity ON seer_wbo_ethnicity(ethnicity);

\copy seer_wbo_ethnicity TO 'seer_wbo_ethnicity.csv' WITH CSV HEADER;
\dS+ seer_wbo_ethnicity

DROP TABLE IF EXISTS seer_wbo_ethnicity_covariates; 
CREATE TABLE seer_wbo_ethnicity_covariates
AS
WITH a AS (
	SELECT year, cb_2014_us_county_500k, SUM(population) AS population
       FROM seer_wbo_ethnicity
	  GROUP BY year, cb_2014_us_county_500k
), b AS (
	SELECT year, cb_2014_us_county_500k, SUM(population) AS whites
       FROM seer_wbo_ethnicity
	  WHERE ethnicity = 19
	  GROUP BY year, cb_2014_us_county_500k
), c AS (
	SELECT year, cb_2014_us_county_500k, SUM(population) AS blacks
       FROM seer_wbo_ethnicity
	  WHERE ethnicity = 29
	  GROUP BY year, cb_2014_us_county_500k
), d AS (
	SELECT a.year, a.cb_2014_us_county_500k, 
		   ROUND(CAST((b.whites/a.population)*100 AS NUMERIC),2) AS pct_white, 
		   ROUND(CAST((c.blacks/a.population)*100 AS NUMERIC),2) AS pct_black
	  FROM a
			LEFT OUTER JOIN b ON (a.year = b.year AND a.cb_2014_us_county_500k = b.cb_2014_us_county_500k)
			LEFT OUTER JOIN c ON (a.year = c.year AND a.cb_2014_us_county_500k = c.cb_2014_us_county_500k)
)
SELECT d.year, d.cb_2014_us_county_500k, d.pct_white, d.pct_black,	
		/* Handle NULLs in the data: should not have a quantile and not impact on the distribution */	
	   CASE WHEN d.pct_white IS NOT NULL THEN NTILE(5) OVER(PARTITION BY d.year ORDER BY d.pct_white) ELSE NULL END AS pct_white_quintile,
	   CASE WHEN d.pct_black IS NOT NULL THEN NTILE(5) OVER(PARTITION BY d.year ORDER BY d.pct_black) ELSE NULL END AS pct_black_quintile
  FROM d
 ORDER BY d.year, d.cb_2014_us_county_500k;
 
COMMENT ON TABLE seer_wbo_ethnicity_covariates IS 'SEER Ethnicity covariates 1972-2013. 9 States in total';
COMMENT ON COLUMN seer_wbo_ethnicity_covariates.year IS 'Year';
COMMENT ON COLUMN seer_wbo_ethnicity_covariates.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN seer_wbo_ethnicity_covariates.pct_white IS '% White'; 
COMMENT ON COLUMN seer_wbo_ethnicity_covariates.pct_black IS '% Black'; 
COMMENT ON COLUMN seer_wbo_ethnicity_covariates.pct_white_quintile IS '% White quintile (1=least white, 5=most)'; 
COMMENT ON COLUMN seer_wbo_ethnicity_covariates.pct_black_quintile IS '% Black quintile (1=least black, 5=most)'; 
 
-- 
-- Add PK, make index organised table
--
ALTER TABLE seer_wbo_ethnicity_covariates ADD CONSTRAINT seer_wbo_ethnicity_covariates_pk 
	PRIMARY KEY (year, cb_2014_us_county_500k);
CLUSTER seer_wbo_ethnicity_covariates USING seer_wbo_ethnicity_covariates_pk;

CREATE INDEX seer_wbo_ethnicity_covariates_year ON seer_wbo_ethnicity_covariates (year);

\copy seer_wbo_ethnicity_covariates TO 'seer_wbo_ethnicity_covariates.csv' WITH CSV HEADER;
\dS+ seer_wbo_ethnicity_covariates
 
END;

--
-- Analyse tables
--
ANALYZE VERBOSE seer_cancer;
ANALYZE VERBOSE seer_population;
ANALYZE VERBOSE seer_wbo_ethnicity;
ANALYZE VERBOSE seer_wbo_ethnicity_covariates;
ANALYZE VERBOSE saipe_county_poverty_1989_2015;

--
-- Eof