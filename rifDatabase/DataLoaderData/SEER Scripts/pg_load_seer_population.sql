
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load SEER population data; reformat into RIF4.0 format. Does not load into RIF (see rif40_load_seer.sql)
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
-- Usage: psql -w -e -f pg_load_seer_population.sql
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
-- Requires RIF USA county level geography to be loaded: rif_pg_USA_2014.sql
-- Also requires USA county level base tables to be loaded: pg_USA_2014.sql
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

DROP TABLE IF EXISTS seer_wbo_single_ages_fixed_length;
DROP TABLE IF EXISTS seer_wbo_single_ages;

--
-- Load singleages.txt as a fixed length record
--
CREATE TABLE seer_wbo_single_ages_fixed_length (
	record_value VARCHAR(28)
);

\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\white_black_other\yr1973_2013.seer9\singleages.txt' WITH CSV;
\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\expanded.race.by.hispanic\yr1992_2013.seer9.plus.sj_la_rg_ak\singleages.txt' WITH CSV;
\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\expanded.race.by.hispanic\yr2000_2013.ca_ky_lo_nj_ga\singleages.txt' WITH CSV;
\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\expanded.race.by.hispanic\yr2005.lo_2nd_half\singleages.txt' WITH CSV;

--
-- Check rowcount
--
SELECT COUNT(*) AS total FROM seer_wbo_single_ages_fixed_length;
DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(*) AS total
 		  FROM seer_wbo_single_ages_fixed_length;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 10234237 THEN
		RAISE INFO 'Table: seer_wbo_single_ages_fixed_length has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: seer_wbo_single_ages_fixed_length has % rows; expecting 10234237', c1_rec.total;
	END IF;
END;
$$;

--
-- Convert datatypes
--
CREATE TABLE seer_wbo_single_ages
AS
SELECT SUBSTRING(record_value FROM 1 FOR 4)::INTEGER AS year,
       SUBSTRING(record_value FROM 5 FOR 2) AS state_postal_abbreviation,
       SUBSTRING(record_value FROM 7 FOR 2)::Text AS state_fips_code,
       SUBSTRING(record_value FROM 9 FOR 3)::Text AS county_fips_code,
       SUBSTRING(record_value FROM 12 FOR 2)::INTEGER AS registry,
       SUBSTRING(record_value FROM 14 FOR 1)::INTEGER AS race,
       SUBSTRING(record_value FROM 15 FOR 1)::INTEGER AS origin,
       SUBSTRING(record_value FROM 16 FOR 1)::INTEGER AS sex,
       SUBSTRING(record_value FROM 17 FOR 2)::INTEGER AS age,
       SUBSTRING(record_value FROM 19 FOR 10)::NUMERIC AS population
  FROM seer_wbo_single_ages_fixed_length;
/* 
 * Registry codes:
 *
	01 = San Francisco-Oakland SMSA
	02 = Connecticut
	20 = Detroit (Metropolitan)
	21 = Hawaii
	22 = Iowa
	23 = New Mexico
	25 = Seattle (Puget Sound)
	26 = Utah
	27 = Atlanta (Metropolitan)
	29 = Alaska Natives
	31 = San Jose-Monterey
	35 = Los Angeles
	37 = Rural Georgia
	41 = California excluding SF/SJM/LA
	42 = Kentucky
	43 = Louisiana
	44 = New Jersey
	47 = Greater Georgia
 */
 
--
-- Extract population
-- * Convert age, sex to RIF age_sex_group 1
-- * Convert FIPS codes to the geographic Names Information System (GNIS) codes used by the RIF
-- * Enforce primary key
-- * Check AGE_SEX_GROUP
-- * Convert unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g.
--   900 series to represent county/independent city combinations in Virginia.
--
DROP TABLE IF EXISTS seer_population;  
CREATE TABLE seer_population
AS  
SELECT a.year, 
	   'US'::Text AS cb_2014_us_nation_5m,
       d.statens AS cb_2014_us_state_500k, 
       COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code) AS cb_2014_us_county_500k,
	   (a.sex*100)+b.offset AS age_sex_group, 
       SUM(a.population) AS population
  FROM seer_wbo_single_ages a
		LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND a.age BETWEEN b.low_age AND b.high_age)
		LEFT OUTER JOIN cb_2014_us_county_500k c ON (a.state_fips_code = c.statefp AND a.county_fips_code = c.countyfp)
		LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
 GROUP BY a.year, d.statens, COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code), a.sex, b.offset
 ORDER BY 1,2,3,4,5;
 
COMMENT ON TABLE seer_population IS 'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States in total';
COMMENT ON COLUMN seer_population.year IS 'Year';
COMMENT ON COLUMN seer_population.cb_2014_us_nation_5m IS 'United States to county level including territories';
COMMENT ON COLUMN seer_population.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN seer_population.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN seer_population.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
COMMENT ON COLUMN seer_population.population IS 'Population';

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
	IF c1_rec.total = 614360 THEN
		RAISE INFO 'Table: seer_population has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: seer_population has % rows; expecting 614360', c1_rec.total;
	END IF;
END;
$$;
 
ALTER TABLE seer_population ADD CONSTRAINT seer_population_pk 
	PRIMARY KEY (year, cb_2014_us_nation_5m, cb_2014_us_state_500k, cb_2014_us_county_500k, age_sex_group);	
ALTER TABLE seer_population ADD CONSTRAINT seer_population_asg_ck 
	CHECK (age_sex_group BETWEEN 100 AND 121 OR age_sex_group BETWEEN 200 AND 221);

CLUSTER seer_population USING seer_population_pk;
CREATE INDEX seer_population_year ON seer_population (year);
CREATE INDEX seer_population_cb_2014_us_nation_5m ON seer_population(cb_2014_us_nation_5m);
CREATE INDEX seer_population_cb_2014_us_state_500k ON seer_population(cb_2014_us_state_500k);
CREATE INDEX seer_population_cb_2014_us_county_500k ON seer_population(cb_2014_us_county_500k);
CREATE INDEX seer_population_age_sex_group ON seer_population(age_sex_group);

\copy seer_population TO 'seer_population.csv' WITH CSV HEADER;
\dS+ seer_population
 
--
-- Eof