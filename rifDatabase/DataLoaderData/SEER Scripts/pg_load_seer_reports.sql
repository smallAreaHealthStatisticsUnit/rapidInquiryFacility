
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Load SEER data reports
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
-- Usage: psql -U rif40 -w -e -f pg_load_seer_reports.sql 
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
-- Requires RIF USA county level geography to be loaded: rif_pg_USA_2014.sql
-- Also requires USA county level base tables to be loaded: pg_USA_2014.sql
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

SELECT SUBSTRING(record_value FROM 1 FOR 8)::INTEGER AS pubcsnum,	/* Patient ID */
       SUBSTRING(record_value FROM 9 FOR 10)::INTEGER AS reg,		/* SEER registry */
       SUBSTRING(record_value FROM 19 FOR 1)::INTEGER AS mar_stat,	/* Marital status at diagnosis */
       SUBSTRING(record_value FROM 20 FOR 2)::INTEGER AS race1v,	/* Race/ethnicity */
       SUBSTRING(record_value FROM 23 FOR 1)::INTEGER AS nhiade,	/* NHIA Derived Hisp Origin */
       SUBSTRING(record_value FROM 24 FOR 1)::INTEGER AS sex,		/* Sex */
       SUBSTRING(record_value FROM 25 FOR 3)::INTEGER AS age_dx, 	/* Age at diagnosis */
       SUBSTRING(record_value FROM 28 FOR 4)::INTEGER AS yr_brth,  	/* Year of birth */
       SUBSTRING(record_value FROM 35 FOR 2)::INTEGER AS seq_num,  	/* Sequence number */
       SUBSTRING(record_value FROM 37 FOR 2)::INTEGER AS mdxrecmp  	/* Month of diagnosis */
  FROM seer9_yr1973_2013_fixed_length LIMIT 10;

SELECT SUBSTRING(record_value FROM 39 FOR 4)::INTEGER AS year_dx,	/* Year of diagnosis */
       SUBSTRING(record_value FROM 43 FOR 4)::Text AS primsite,		/* Primary site ICD-O-2 (1973+) */
       SUBSTRING(record_value FROM 47 FOR 1)::Text AS lateral,		/* Laterality */
       SUBSTRING(record_value FROM 48 FOR 4)::Text AS histo2v,		/* Histologic Type ICD-O-2 */
       SUBSTRING(record_value FROM 52 FOR 1)::Text AS beho2v,		/* Behavior Code ICD-O-2*/
       SUBSTRING(record_value FROM 53 FOR 4)::Text AS histo3v,		/* Histologic Type ICD-O-3 */
       SUBSTRING(record_value FROM 57 FOR 1)::Text AS beho3v,		/* Behavior code ICD-O-3 */
       SUBSTRING(record_value FROM 192 FOR 2)::INTEGER AS age_1rec,	/* Age recode <1 year olds */
       SUBSTRING(record_value FROM 199 FOR 5)::Text AS siterwho,  	/* Site recode ICD-O-3/WHO 2008 */
       SUBSTRING(record_value FROM 204 FOR 4)::Text AS icdoto9v, 	/* Recode ICD-O-2 to 9 */
       SUBSTRING(record_value FROM 208 FOR 4)::Text AS icdot10v, 	/* Recode ICD-O-2 to 10 */
       SUBSTRING(record_value FROM 218 FOR 3)::Text AS iccc3who, 	/* ICCC site recode ICD-O-3/WHO 2008 */
       SUBSTRING(record_value FROM 221 FOR 3)::Text AS iccc3xwho  	/* ICCC site rec extended ICD-O-3/ WHO 2008 */
  FROM seer9_yr1973_2013_fixed_length LIMIT 10;

SELECT SUBSTRING(record_value FROM 233 FOR 1)::INTEGER AS rac_reca,	/* Race recode A (WHITE, BLACK, OTHER) */
       SUBSTRING(record_value FROM 234 FOR 1)::INTEGER AS rac_recy,	/* Race recode Y (W, B, AI, API) */
       SUBSTRING(record_value FROM 235 FOR 1)::INTEGER AS origrecb,	/* Origin Recode NHIA (HISPANIC, NON-HISP) */	
       SUBSTRING(record_value FROM 245 FOR 1)::Text AS firstprm,	/* First malignant primary indicator */
       SUBSTRING(record_value FROM 246 FOR 5)::Text AS st_cnty,	 	/* State-county recode */
       SUBSTRING(record_value FROM 246 FOR 2)::Text AS state_fips_code,	 /* State FIPS code */
       SUBSTRING(record_value FROM 248 FOR 3)::Text AS county_fips_code, /* County FIPS code */
       SUBSTRING(record_value FROM 255 FOR 5)::Text AS codpub	  	/* Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html) */
  FROM seer9_yr1973_2013_fixed_length LIMIT 10;
  
SELECT * FROM seer_cancer LIMIT 10;

WITH a AS (
	SELECT year, SUBSTRING(icdot10v FROM 1 FOR 3) AS icd, COUNT(pubcsnum) AS cases, COUNT(DISTINCT(pubcsnum)) AS people
	  FROM seer_cancer
	 GROUP BY year, SUBSTRING(icdot10v FROM 1 FOR 3)
), b AS (
	SELECT SUBSTRING(icdot10v FROM 1 FOR 3) AS icd, COUNT(pubcsnum) AS cases, COUNT(DISTINCT(pubcsnum)) AS people
	  FROM seer_cancer
	 GROUP BY SUBSTRING(icdot10v FROM 1 FOR 3)
)
SELECT icd, cases, people
  FROM b
 ORDER BY 1;
 
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

 
SELECT * FROM saipe_county_poverty_1989_2015 WHERE cb_2014_us_county_500k LIKE 'UNKNOWN%' LIMIT 10; 
SELECT * FROM saipe_county_poverty_1989_2015
 WHERE median_household_income_qunitile != median_pct_not_in_poverty_all_ages_qunitile LIMIT 10; 

SELECT year, SUM(total_poverty_all_ages) AS total_poverty_all_ages, COUNT(total_poverty_all_ages) AS counties,
       MIN(median_household_income_qunitile) AS minq,
       MAX(median_household_income_qunitile) AS maxq
  FROM saipe_county_poverty_1989_2015
 GROUP BY year
 ORDER BY year;
 
--
-- Eof