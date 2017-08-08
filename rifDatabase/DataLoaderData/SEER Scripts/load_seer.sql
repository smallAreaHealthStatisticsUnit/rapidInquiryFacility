
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
-- Usage: psql -U rif40 -w -e -f load_seer.sql 
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

DROP TABLE IF EXISTS seer_wbo_single_ages_fixed_length;
DROP TABLE IF EXISTS seer_wbo_single_ages;

--
-- Load singleages.txt as a fixed length record
--
CREATE TABLE seer_wbo_single_ages_fixed_length (
	record_value VARCHAR(28)
);

-- wc -l SEER_1973_2013_TEXTDATA\populations\white_black_other\yr1973_2013.seer9\singleages.txt
\copy seer_wbo_single_ages_fixed_length FROM 'SEER_1973_2013_TEXTDATA\populations\white_black_other\yr1973_2013.seer9\singleages.txt' WITH CSV;

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
	IF c1_rec.total = 2764264 THEN
		RAISE INFO 'Table: seer_wbo_single_ages_fixed_length has % rows', c1_rec.total;
	ELSE
		RAISE EXCEPTION 'Table: seer_wbo_single_ages_fixed_length has % rows; expecting 2764264', c1_rec.total;
	END IF;
END;
$$;

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
  FROM seer_wbo_single_ages_fixed_length LIMIT 10;

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
SELECT a.year, d.statens AS cb_2014_us_state_500k, 
       COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code) AS cb_2014_us_county_500k,
	   (a.sex*100)+b.offset AS age_sex_group, 
       SUM(a.population) AS population
  FROM seer_wbo_single_ages a
		LEFT OUTER JOIN rif40_age_groups b ON (b.age_group_id = 1 AND a.age BETWEEN b.low_age AND b.high_age)
		LEFT OUTER JOIN cb_2014_us_county_500k c ON (a.state_fips_code = c.statefp AND a.county_fips_code = c.countyfp)
		LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.state_fips_code = d.statefp)
 GROUP BY a.year, d.statens, COALESCE(c.countyns, 'UNKNOWN: '||a.county_fips_code), a.sex, b.offset
 ORDER BY 1,2,3,4,5;
 
COMMENT ON TABLE seer_population IS 'SEER Population 1972-2013. Georgia starts in 1975, Washington in 1974. 9 States total';
COMMENT ON COLUMN seer_population.year IS 'Year';
COMMENT ON COLUMN seer_population.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN seer_population.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN seer_population.age_sex_group IS 'RIF age_sex_group 1 (21 bands)';
COMMENT ON COLUMN seer_population.population IS 'Population';

SELECT * FROM seer_population LIMIT 10;
SELECT statefp, areaname FROM cb_2014_us_state_500k ORDER BY 2;
SELECT countyfp, areaname FROM cb_2014_us_county_500k WHERE statefp = '06' ORDER BY 2;

WITH a AS (
	SELECT a.year, a.cb_2014_us_state_500k, d.name, SUM(a.population) AS population
	  FROM seer_population a
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.cb_2014_us_state_500k = d.statens)
	 GROUP BY a.year, a.cb_2014_us_state_500k, d.name
)
SELECT a13.name, 
       a73.population AS y1973,
       a74.population AS y1974,
       a75.population AS y1975,
       a76.population AS y1976,
       a77.population AS y1977,
       a78.population AS y1978,
       a79.population AS y1979,
       a80.population AS y1980,
       a81.population AS y1981,
       a82.population AS y1982, 
	   a83.population AS y1983
  FROM a a13
	LEFT OUTER JOIN a a73 ON (a13.cb_2014_us_state_500k = a73.cb_2014_us_state_500k AND a73.year = 1973)
	LEFT OUTER JOIN a a74 ON (a13.cb_2014_us_state_500k = a74.cb_2014_us_state_500k AND a74.year = 1974)
	LEFT OUTER JOIN a a75 ON (a13.cb_2014_us_state_500k = a75.cb_2014_us_state_500k AND a75.year = 1975)
	LEFT OUTER JOIN a a76 ON (a13.cb_2014_us_state_500k = a76.cb_2014_us_state_500k AND a76.year = 1976)
	LEFT OUTER JOIN a a77 ON (a13.cb_2014_us_state_500k = a77.cb_2014_us_state_500k AND a77.year = 1977)
	LEFT OUTER JOIN a a78 ON (a13.cb_2014_us_state_500k = a78.cb_2014_us_state_500k AND a78.year = 1978)
	LEFT OUTER JOIN a a79 ON (a13.cb_2014_us_state_500k = a79.cb_2014_us_state_500k AND a79.year = 1979)
	LEFT OUTER JOIN a a80 ON (a13.cb_2014_us_state_500k = a80.cb_2014_us_state_500k AND a80.year = 1980)
	LEFT OUTER JOIN a a81 ON (a13.cb_2014_us_state_500k = a81.cb_2014_us_state_500k AND a81.year = 1981)
	LEFT OUTER JOIN a a82 ON (a13.cb_2014_us_state_500k = a82.cb_2014_us_state_500k AND a82.year = 1982)
	LEFT OUTER JOIN a a83 ON (a13.cb_2014_us_state_500k = a83.cb_2014_us_state_500k AND a83.year = 1983)
 WHERE a13.year = 2013
 ORDER BY 1,2,3; 
 
WITH a AS (
	SELECT a.year, a.cb_2014_us_state_500k, d.name, SUM(a.population) AS population
	  FROM seer_population a
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.cb_2014_us_state_500k = d.statens)
	 GROUP BY a.year, a.cb_2014_us_state_500k, d.name
)
SELECT a13.name, 
       a83.population AS y1983,
       a84.population AS y1984,
       a85.population AS y1985,
       a86.population AS y1986,
       a87.population AS y1987,
       a88.population AS y1988,
       a89.population AS y1989,
       a90.population AS y1990,
       a91.population AS y1991,
       a92.population AS y1992, 
	   a93.population AS y1993 
  FROM a a13
	LEFT OUTER JOIN a a83 ON (a13.cb_2014_us_state_500k = a83.cb_2014_us_state_500k AND a83.year = 1983)
	LEFT OUTER JOIN a a84 ON (a13.cb_2014_us_state_500k = a84.cb_2014_us_state_500k AND a84.year = 1984)
	LEFT OUTER JOIN a a85 ON (a13.cb_2014_us_state_500k = a85.cb_2014_us_state_500k AND a85.year = 1985)
	LEFT OUTER JOIN a a86 ON (a13.cb_2014_us_state_500k = a86.cb_2014_us_state_500k AND a86.year = 1986)
	LEFT OUTER JOIN a a87 ON (a13.cb_2014_us_state_500k = a87.cb_2014_us_state_500k AND a87.year = 1987)
	LEFT OUTER JOIN a a88 ON (a13.cb_2014_us_state_500k = a88.cb_2014_us_state_500k AND a88.year = 1988)
	LEFT OUTER JOIN a a89 ON (a13.cb_2014_us_state_500k = a89.cb_2014_us_state_500k AND a89.year = 1989)
	LEFT OUTER JOIN a a90 ON (a13.cb_2014_us_state_500k = a90.cb_2014_us_state_500k AND a90.year = 1990)
	LEFT OUTER JOIN a a91 ON (a13.cb_2014_us_state_500k = a91.cb_2014_us_state_500k AND a91.year = 1991)
	LEFT OUTER JOIN a a92 ON (a13.cb_2014_us_state_500k = a92.cb_2014_us_state_500k AND a92.year = 1992)
	LEFT OUTER JOIN a a93 ON (a13.cb_2014_us_state_500k = a93.cb_2014_us_state_500k AND a93.year = 1993)
 WHERE a13.year = 2013
 ORDER BY 1,2,3; 
 
WITH a AS (
	SELECT a.year, a.cb_2014_us_state_500k, d.name, SUM(a.population) AS population
	  FROM seer_population a
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.cb_2014_us_state_500k = d.statens)
	 GROUP BY a.year, a.cb_2014_us_state_500k, d.name
)
SELECT a13.name, 
       a93.population AS y1993,
       a94.population AS y1994,
       a95.population AS y1995,
       a96.population AS y1996,
       a97.population AS y1997,
       a98.population AS y1998,
       a99.population AS y1999,
       a00.population AS y2000,
       a01.population AS y2001,
       a02.population AS y2002, 
	   a03.population AS y2003 
  FROM a a13
	LEFT OUTER JOIN a a93 ON (a13.cb_2014_us_state_500k = a93.cb_2014_us_state_500k AND a93.year = 1993)
	LEFT OUTER JOIN a a94 ON (a13.cb_2014_us_state_500k = a94.cb_2014_us_state_500k AND a94.year = 1994)
	LEFT OUTER JOIN a a95 ON (a13.cb_2014_us_state_500k = a95.cb_2014_us_state_500k AND a95.year = 1995)
	LEFT OUTER JOIN a a96 ON (a13.cb_2014_us_state_500k = a96.cb_2014_us_state_500k AND a96.year = 1996)
	LEFT OUTER JOIN a a97 ON (a13.cb_2014_us_state_500k = a97.cb_2014_us_state_500k AND a97.year = 1997)
	LEFT OUTER JOIN a a98 ON (a13.cb_2014_us_state_500k = a98.cb_2014_us_state_500k AND a98.year = 1998)
	LEFT OUTER JOIN a a99 ON (a13.cb_2014_us_state_500k = a99.cb_2014_us_state_500k AND a99.year = 1999)
	LEFT OUTER JOIN a a00 ON (a13.cb_2014_us_state_500k = a00.cb_2014_us_state_500k AND a00.year = 2000)
	LEFT OUTER JOIN a a01 ON (a13.cb_2014_us_state_500k = a01.cb_2014_us_state_500k AND a01.year = 2001)
	LEFT OUTER JOIN a a02 ON (a13.cb_2014_us_state_500k = a02.cb_2014_us_state_500k AND a02.year = 2002)
	LEFT OUTER JOIN a a03 ON (a13.cb_2014_us_state_500k = a03.cb_2014_us_state_500k AND a03.year = 2003)
 WHERE a13.year = 2013
 ORDER BY 1,2,3; 
 
WITH a AS (
	SELECT a.year, a.cb_2014_us_state_500k, d.name, SUM(a.population) AS population
	  FROM seer_population a
			LEFT OUTER JOIN cb_2014_us_state_500k d ON (a.cb_2014_us_state_500k = d.statens)
	 GROUP BY a.year, a.cb_2014_us_state_500k, d.name
)
SELECT a13.name, 
       a03.population AS y2003,
       a04.population AS y2004,
       a05.population AS y2005,
       a06.population AS y2006,
       a07.population AS y2007,
       a08.population AS y2008,
       a09.population AS y2009,
       a10.population AS y2010,
       a11.population AS y2011,
       a12.population AS y2012, 
	   a13.population AS y2013
  FROM a a13
	LEFT OUTER JOIN a a03 ON (a13.cb_2014_us_state_500k = a03.cb_2014_us_state_500k AND a03.year = 2003)
	LEFT OUTER JOIN a a04 ON (a13.cb_2014_us_state_500k = a04.cb_2014_us_state_500k AND a04.year = 2004)
	LEFT OUTER JOIN a a05 ON (a13.cb_2014_us_state_500k = a05.cb_2014_us_state_500k AND a05.year = 2005)
	LEFT OUTER JOIN a a06 ON (a13.cb_2014_us_state_500k = a06.cb_2014_us_state_500k AND a06.year = 2006)
	LEFT OUTER JOIN a a07 ON (a13.cb_2014_us_state_500k = a07.cb_2014_us_state_500k AND a07.year = 2007)
	LEFT OUTER JOIN a a08 ON (a13.cb_2014_us_state_500k = a08.cb_2014_us_state_500k AND a08.year = 2008)
	LEFT OUTER JOIN a a09 ON (a13.cb_2014_us_state_500k = a09.cb_2014_us_state_500k AND a09.year = 2009)
	LEFT OUTER JOIN a a10 ON (a13.cb_2014_us_state_500k = a10.cb_2014_us_state_500k AND a10.year = 2010)
	LEFT OUTER JOIN a a11 ON (a13.cb_2014_us_state_500k = a11.cb_2014_us_state_500k AND a11.year = 2011)
	LEFT OUTER JOIN a a12 ON (a13.cb_2014_us_state_500k = a12.cb_2014_us_state_500k AND a12.year = 2012)
 WHERE a13.year = 2013
 ORDER BY 1,2,3; 
 
SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k, d.name, 
       SUM(a.population) AS population, MIN(a.year) AS min_year, MAX(a.year) AS max_year
  FROM seer_population a
		LEFT OUTER JOIN cb_2014_us_county_500k d ON (a.cb_2014_us_county_500k = d.countyns)
 WHERE d.name IS NULL
 GROUP BY a.cb_2014_us_state_500k, a.cb_2014_us_county_500k, d.name
 ORDER BY 1,2,3;
 
WITH a AS ( 
	SELECT a.year, TRUNC(a.age_sex_group/100) AS sex, SUM(a.population) AS population
	  FROM seer_population a
	 GROUP BY a.year, TRUNC(a.age_sex_group/100)
)
SELECT a.year, SUM(a.population) AS population, m.population AS males, f.population AS females
  FROM a
	LEFT OUTER JOIN a m ON (a.year = m.year AND m.sex = 1)
	LEFT OUTER JOIN a f ON (a.year = f.year AND f.sex = 2)
 GROUP BY a.year, m.population, f.population
 ORDER BY a.year;
   
ALTER TABLE seer_population ADD CONSTRAINT seer_population_pk 
	PRIMARY KEY (year, cb_2014_us_state_500k, cb_2014_us_county_500k, age_sex_group);	
ALTER TABLE seer_population ADD CONSTRAINT seer_population_asg_ck 
	CHECK (age_sex_group BETWEEN 100 AND 121 OR age_sex_group BETWEEN 200 AND 221);

\copy seer_population TO 'seer_population.csv' WITH CSV HEADER;
\dS+ seer_population
 
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
	SELECT a.year, d.statens AS cb_2014_us_state_500k, 
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
SELECT year, cb_2014_us_state_500k, cb_2014_us_county_500k, (10*race)+COALESCE(origin, 0) AS ethnicity, a.population
  FROM a
 ORDER BY 1,2,3,4;

COMMENT ON TABLE seer_wbo_ethnicity IS 'SEER Ethnicity 1972-2013; white/blacks/other + hispanic/non-hispanic';
COMMENT ON COLUMN seer_wbo_ethnicity.year IS 'Year';
COMMENT ON COLUMN seer_wbo_ethnicity.cb_2014_us_state_500k IS 'State geographic Names Information System (GNIS) code';
COMMENT ON COLUMN seer_wbo_ethnicity.cb_2014_us_county_500k IS 'County geographic Names Information System (GNIS) code. Unjoined county FIPS codes to "UNKNOWN: " + county FIPS code; e.g. the 900 series to represent county/independent city combinations in Virginia.';
COMMENT ON COLUMN seer_wbo_ethnicity.ethnicity IS 'Ethnicity coded 19: White; 29: Black, 39: American Indian/Alaska Native (1992 onwards); 99: Other (1973-1991); all with no hispanic data ';
COMMENT ON COLUMN seer_wbo_ethnicity.population IS 'Population'; 
ALTER TABLE seer_wbo_ethnicity ADD CONSTRAINT seer_wbo_ethnicity_pk 
	PRIMARY KEY (year, cb_2014_us_state_500k, cb_2014_us_county_500k, ethnicity);
\copy seer_wbo_ethnicity TO 'seer_wbo_ethnicity.csv' WITH CSV HEADER;
\dS+ seer_wbo_ethnicity

SELECT CASE	
			WHEN a.ethnicity = 19 THEN 'White, no hispanic data'
			WHEN a.ethnicity = 29 THEN 'Black, no hispanic data'
			WHEN a.ethnicity = 39 THEN 'American Indian/Alaska Native, no hispanic data'
			WHEN a.ethnicity = 99 THEN 'Other, no hispanic data'
			ELSE                       'Unknown: '||a.ethnicity::Text
	   END AS ethnicity,
	   SUM(a.population) AS population, MIN(a.year) AS min_year, MAX(a.year) AS max_year
  FROM seer_wbo_ethnicity a
 GROUP BY a.ethnicity
 ORDER BY 1;
 
END;

--
-- Eof