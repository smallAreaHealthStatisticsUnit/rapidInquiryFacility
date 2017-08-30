
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

\out pg_load_seer_reports.txt

\pset title 'seer9_yr1973_2013_fixed_length: 10 records; 1 of 3'
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

\pset title 'seer9_yr1973_2013_fixed_length: 10 records; 2 of 3'
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

\pset title 'seer9_yr1973_2013_fixed_length: 10 records; 3 of 3'
SELECT SUBSTRING(record_value FROM 233 FOR 1)::INTEGER AS rac_reca,	/* Race recode A (WHITE, BLACK, OTHER) */
       SUBSTRING(record_value FROM 234 FOR 1)::INTEGER AS rac_recy,	/* Race recode Y (W, B, AI, API) */
       SUBSTRING(record_value FROM 235 FOR 1)::INTEGER AS origrecb,	/* Origin Recode NHIA (HISPANIC, NON-HISP) */	
       SUBSTRING(record_value FROM 245 FOR 1)::Text AS firstprm,	/* First malignant primary indicator */
       SUBSTRING(record_value FROM 246 FOR 5)::Text AS st_cnty,	 	/* State-county recode */
       SUBSTRING(record_value FROM 246 FOR 2)::Text AS state_fips_code,	 /* State FIPS code */
       SUBSTRING(record_value FROM 248 FOR 3)::Text AS county_fips_code, /* County FIPS code */
       SUBSTRING(record_value FROM 255 FOR 5)::Text AS codpub	  	/* Cause of death to SEER site recode (see: https://seer.cancer.gov/codrecode/1969+_d09172004/index.html) */
  FROM seer9_yr1973_2013_fixed_length LIMIT 10;
 
\pset title 'seer_wbo_single_ages_fixed_length: 10 records'
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
  
\pset title 'seer_cancer: 10 records'
SELECT * FROM seer_cancer LIMIT 10;

\pset title 'seer_population: 10 records'
SELECT * FROM seer_population LIMIT 10;

\pset title 'seer_cancer: by 3 character ICD'
WITH a AS (
	SELECT year, SUBSTRING(icdot10v FROM 1 FOR 3) AS icd, COUNT(pubcsnum) AS cases, COUNT(DISTINCT(pubcsnum)) AS people
	  FROM seer_cancer
	 GROUP BY year, SUBSTRING(icdot10v FROM 1 FOR 3)
), b AS (
	SELECT SUBSTRING(icdot10v FROM 1 FOR 3) AS icd, COUNT(pubcsnum) AS cases, COUNT(DISTINCT(pubcsnum)) AS people
	  FROM seer_cancer
	 GROUP BY SUBSTRING(icdot10v FROM 1 FOR 3)
)
SELECT CASE 
			WHEN icd BETWEEN 'C00' AND 'C14' THEN 'Malignant neoplasms of lip, oral cavity and pharynx'
			WHEN icd BETWEEN 'C15' AND 'C26' THEN 'Malignant neoplasms of digestive organs'
			WHEN icd BETWEEN 'C30' AND 'C39' THEN 'Malignant neoplasms of respiratory and intrathoracic organs'
			WHEN icd BETWEEN 'C40' AND 'C41' THEN 'Malignant neoplasms of bone and articular cartilage'
			WHEN icd BETWEEN 'C43' AND 'C44' THEN 'Melanoma and other malignant neoplasms of skin'
			WHEN icd BETWEEN 'C45' AND 'C49' THEN 'Malignant neoplasms of mesothelial and soft tissue'
			WHEN icd = 'C50' THEN 'Malignant neoplasms of breast'
			WHEN icd BETWEEN 'C51' AND 'C58' THEN 'Malignant neoplasms of female genital organs'
			WHEN icd BETWEEN 'C60' AND 'C63' THEN 'Malignant neoplasms of male genital organs'
			WHEN icd BETWEEN 'C64' AND 'C68' THEN 'Malignant neoplasms of urinary tract'
			WHEN icd BETWEEN 'C69' AND 'C72' THEN 'Malignant neoplasms of eye, brain and other parts of central nervous system'
			WHEN icd BETWEEN 'C73' AND 'C75' THEN 'Malignant neoplasms of thyroid and other endocrine glands'
			WHEN icd BETWEEN 'C76' AND 'C80' THEN 'Malignant neoplasms of ill-defined, other secondary and unspecified sites'
			WHEN icd = 'C7A' THEN 'Malignant neuroendocrine tumors'
			WHEN icd = 'C7B' THEN 'Secondary neuroendocrine tumors'
			WHEN icd BETWEEN 'C81' AND 'C96' THEN 'Malignant neoplasms of lymphoid, hematopoietic and related tissue'
			WHEN icd BETWEEN 'D00' AND 'D09' THEN 'In situ neoplasms'
			WHEN icd BETWEEN 'D10' AND 'D36' THEN 'Benign neoplasms, except benign neuroendocrine tumors'
			WHEN icd BETWEEN 'D37' AND 'D48' THEN 'Neoplasms of uncertain behavior, polycythemia vera and myelodysplastic syndromes'
			WHEN icd = 'D3A' THEN 'Benign neuroendocrine tumors'
			WHEN icd = 'D49' THEN 'Neoplasms of unspecified behavior'
			ELSE 'Other: '||icd
	   END AS icd,
	   SUM(cases) AS cases, SUM(people) AS people
  FROM b
 GROUP BY CASE 
			WHEN icd BETWEEN 'C00' AND 'C14' THEN 'Malignant neoplasms of lip, oral cavity and pharynx'
			WHEN icd BETWEEN 'C15' AND 'C26' THEN 'Malignant neoplasms of digestive organs'
			WHEN icd BETWEEN 'C30' AND 'C39' THEN 'Malignant neoplasms of respiratory and intrathoracic organs'
			WHEN icd BETWEEN 'C40' AND 'C41' THEN 'Malignant neoplasms of bone and articular cartilage'
			WHEN icd BETWEEN 'C43' AND 'C44' THEN 'Melanoma and other malignant neoplasms of skin'
			WHEN icd BETWEEN 'C45' AND 'C49' THEN 'Malignant neoplasms of mesothelial and soft tissue'
			WHEN icd = 'C50' THEN 'Malignant neoplasms of breast'
			WHEN icd BETWEEN 'C51' AND 'C58' THEN 'Malignant neoplasms of female genital organs'
			WHEN icd BETWEEN 'C60' AND 'C63' THEN 'Malignant neoplasms of male genital organs'
			WHEN icd BETWEEN 'C64' AND 'C68' THEN 'Malignant neoplasms of urinary tract'
			WHEN icd BETWEEN 'C69' AND 'C72' THEN 'Malignant neoplasms of eye, brain and other parts of central nervous system'
			WHEN icd BETWEEN 'C73' AND 'C75' THEN 'Malignant neoplasms of thyroid and other endocrine glands'
			WHEN icd BETWEEN 'C76' AND 'C80' THEN 'Malignant neoplasms of ill-defined, other secondary and unspecified sites'
			WHEN icd = 'C7A' THEN 'Malignant neuroendocrine tumors'
			WHEN icd = 'C7B' THEN 'Secondary neuroendocrine tumors'
			WHEN icd BETWEEN 'C81' AND 'C96' THEN 'Malignant neoplasms of lymphoid, hematopoietic and related tissue'
			WHEN icd BETWEEN 'D00' AND 'D09' THEN 'In situ neoplasms'
			WHEN icd BETWEEN 'D10' AND 'D36' THEN 'Benign neoplasms, except benign neuroendocrine tumors'
			WHEN icd BETWEEN 'D37' AND 'D48' THEN 'Neoplasms of uncertain behavior, polycythemia vera and myelodysplastic syndromes'
			WHEN icd = 'D3A' THEN 'Benign neuroendocrine tumors'
			WHEN icd = 'D49' THEN 'Neoplasms of unspecified behavior'
			ELSE 'Other: '||icd
	   END
 ORDER BY 1;
/*
                                       icd                                        |  cases  | people
----------------------------------------------------------------------------------+---------+---------
 In situ neoplasms                                                                |  723924 |  695164
 Malignant neoplasms of bone and articular cartilage                              |   18647 |   18565
 Malignant neoplasms of breast                                                    | 1220268 | 1152938
 Malignant neoplasms of digestive organs                                          | 1567018 | 1539411
 Malignant neoplasms of eye, brain and other parts of central nervous system      |  133866 |  133233
 Malignant neoplasms of female genital organs                                     |  505516 |  503748
 Malignant neoplasms of ill-defined, other secondary and unspecified sites        |  176384 |  176341
 Malignant neoplasms of lip, oral cavity and pharynx                              |  206243 |  204643
 Malignant neoplasms of lymphoid, hematopoietic and related tissue                |  731582 |  728775
 Malignant neoplasms of male genital organs                                       | 1222972 | 1221937
 Malignant neoplasms of mesothelial and soft tissue                               |  109101 |  108533
 Malignant neoplasms of respiratory and intrathoracic organs                      | 1197741 | 1172367
 Malignant neoplasms of thyroid and other endocrine glands                        |  178236 |  177143
 Malignant neoplasms of urinary tract                                             |  608466 |  594238
 Melanoma and other malignant neoplasms of skin                                   |  359536 |  341028
 Neoplasms of uncertain behavior, polycythemia vera and myelodysplastic syndromes |    6369 |    6347
 Other: 888                                                                       |     185 |     185
 Other: 999                                                                       |  210908 |  207837
 Other: Q30                                                                       |       1 |       1
(19 rows)
 */
 
\pset title 'SEER_POPULATION: County misjoins'
WITH a AS ( 
	SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k, 
		   COUNT(a.cb_2014_us_county_500k) AS total, MIN(year) AS min_year, MAX(year) AS max_year
	  FROM seer_population a  
	 GROUP BY a.cb_2014_us_state_500k, a.cb_2014_us_county_500k  
)
SELECT s.areaname AS state, COALESCE(c.areaname, a.cb_2014_us_county_500k) AS county,
       a.total, a.min_year, a.max_year
  FROM a
	LEFT OUTER JOIN lookup_cb_2014_us_state_500k s ON (a.cb_2014_us_state_500k = s.cb_2014_us_state_500k)
	LEFT OUTER JOIN lookup_cb_2014_us_county_500k c ON (a.cb_2014_us_county_500k = c.cb_2014_us_county_500k)
 WHERE c.areaname IS NULL
 ORDER BY 1, 2;
/*
   state    |    county    | total | min_year | max_year
------------+--------------+-------+----------+----------
 Alaska     | UNKNOWN: 900 |   968 |     1992 |     2013
 Hawaii     | UNKNOWN: 900 |  1188 |     1973 |     1999
 New Mexico | UNKNOWN: 910 |   396 |     1973 |     1981
(3 rows)
 */ 
 
\pset title 'SEER_POPULATION: Missing counties'
WITH a AS ( 
	SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k, 
		   COUNT(a.cb_2014_us_county_500k) AS total, MIN(year) AS min_year, MAX(year) AS max_year
	  FROM seer_population a  
	 GROUP BY a.cb_2014_us_state_500k, a.cb_2014_us_county_500k  
), b AS (
	SELECT c.cb_2014_us_state_500k, COUNT(c.cb_2014_us_county_500k) AS missing_counties
	  FROM (
		SELECT b.cb_2014_us_state_500k, b.cb_2014_us_county_500k
		  FROM hierarchy_usa_2014 b 
		EXCEPT
		SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k
		  FROM a
	 ) AS c
	  GROUP BY c.cb_2014_us_state_500k
), d AS (
	SELECT d.cb_2014_us_state_500k, COUNT(d.cb_2014_us_county_500k) AS extra_counties
	  FROM (
		SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k
		  FROM a
		EXCEPT
		SELECT b.cb_2014_us_state_500k, b.cb_2014_us_county_500k
		  FROM hierarchy_usa_2014 b 
	 ) AS d
	  GROUP BY d.cb_2014_us_state_500k
)
SELECT s.areaname AS state,
       CASE WHEN c.areaname IS NULL THEN 'No' ELSE 'Yes' END AS county_join,
       SUM(a.total) AS total, MIN(a.min_year) AS min_year, MAX(a.max_year) AS max_year,
	   COUNT(DISTINCT(a.cb_2014_us_county_500k)) AS counties, b.missing_counties, d.extra_counties
  FROM a
	LEFT OUTER JOIN lookup_cb_2014_us_state_500k s ON (a.cb_2014_us_state_500k = s.cb_2014_us_state_500k)
	LEFT OUTER JOIN lookup_cb_2014_us_county_500k c ON (a.cb_2014_us_county_500k = c.cb_2014_us_county_500k)
	LEFT OUTER JOIN b ON (b.cb_2014_us_state_500k = a.cb_2014_us_state_500k)
	LEFT OUTER JOIN d ON (d.cb_2014_us_state_500k = a.cb_2014_us_state_500k)
  GROUP BY s.areaname,
       CASE WHEN c.areaname IS NULL THEN 'No' ELSE 'Yes' END, b.missing_counties, d.extra_counties 
 ORDER BY 1, 2;
/*
Same as Cancer:
                                     SEER_POPULATION: Missing counties
    state    | county_join | total  | min_year | max_year | counties | missing_counties | extra_counties
-------------+-------------+--------+----------+----------+----------+------------------+----------------
 Alaska      | No          |    968 |     1992 |     2013 |        1 |               29 |              1
 California  | Yes         |  43426 |     1973 |     2013 |       58 |                  |
 Connecticut | Yes         |  14432 |     1973 |     2013 |        8 |                  |
 Georgia     | Yes         | 106964 |     1975 |     2013 |      159 |                  |
 Hawaii      | No          |   1188 |     1973 |     1999 |        1 |                  |              1
 Hawaii      | Yes         |   2820 |     2000 |     2013 |        5 |                  |              1
 Iowa        | Yes         | 178596 |     1973 |     2013 |       99 |                  |
 Kentucky    | Yes         |  73916 |     2000 |     2013 |      120 |                  |
 Louisiana   | Yes         |  39424 |     2000 |     2013 |       64 |                  |
 Michigan    | Yes         |   5412 |     1973 |     2013 |        3 |               80 |
 New Jersey  | Yes         |  12936 |     2000 |     2013 |       21 |                  |
 New Mexico  | No          |    396 |     1973 |     1981 |        1 |                  |              1
 New Mexico  | Yes         |  58699 |     1973 |     2013 |       33 |                  |              1
 Utah        | Yes         |  52303 |     1973 |     2013 |       29 |                  |
 Washington  | Yes         |  22880 |     1974 |     2013 |       13 |               26 |
(15 rows)
 */

\pset title 'SEER_CANCER: County misjoins'
WITH a AS ( 
	SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k, 
		   COUNT(a.cb_2014_us_county_500k) AS total, MIN(year) AS min_year, MAX(year) AS max_year
	  FROM seer_cancer a  
	 GROUP BY a.cb_2014_us_state_500k, a.cb_2014_us_county_500k  
)
SELECT s.areaname AS state, COALESCE(c.areaname, a.cb_2014_us_county_500k) AS county,
       a.total, a.min_year, a.max_year
  FROM a
	LEFT OUTER JOIN lookup_cb_2014_us_state_500k s ON (a.cb_2014_us_state_500k = s.cb_2014_us_state_500k)
	LEFT OUTER JOIN lookup_cb_2014_us_county_500k c ON (a.cb_2014_us_county_500k = c.cb_2014_us_county_500k)
 WHERE c.areaname IS NULL
 ORDER BY 1, 2;
/*
    state    |    county    | total | min_year | max_year
-------------+--------------+-------+----------+----------
 Alaska      | UNKNOWN: 900 |  8080 |     1992 |     2013
 Connecticut | UNKNOWN: 999 |   290 |     1973 |     2013
 Hawaii      | UNKNOWN: 911 | 12248 |     1973 |     1999
 Hawaii      | UNKNOWN: 912 | 77935 |     1973 |     1999
 Hawaii      | UNKNOWN: 913 |    33 |     1973 |     1998
 Hawaii      | UNKNOWN: 914 |  4773 |     1973 |     1999
 Hawaii      | UNKNOWN: 915 |  8997 |     1973 |     1999
 Hawaii      | UNKNOWN: 999 |     7 |     2006 |     2012
 New Jersey  | UNKNOWN: 999 |   484 |     2000 |     2013
 New Mexico  | UNKNOWN: 910 |  1171 |     1973 |     1981
 New Mexico  | UNKNOWN: 999 |  2103 |     1973 |     2013
 Utah        | UNKNOWN: 999 |    20 |     1973 |     2012
(12 rows)
 */ 
-- Alaska and Hawaii are a mess!

\pset title 'SEER_CANCER: Missing counties'
WITH a AS ( 
	SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k, 
		   COUNT(a.cb_2014_us_county_500k) AS total, MIN(year) AS min_year, MAX(year) AS max_year
	  FROM seer_cancer a  
	 GROUP BY a.cb_2014_us_state_500k, a.cb_2014_us_county_500k  
), b AS (
	SELECT c.cb_2014_us_state_500k, COUNT(c.cb_2014_us_county_500k) AS missing_counties
	  FROM (
		SELECT b.cb_2014_us_state_500k, b.cb_2014_us_county_500k
		  FROM hierarchy_usa_2014 b 
		EXCEPT
		SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k
		  FROM a
	 ) AS c
	  GROUP BY c.cb_2014_us_state_500k
), d AS (
	SELECT d.cb_2014_us_state_500k, COUNT(d.cb_2014_us_county_500k) AS extra_counties
	  FROM (
		SELECT a.cb_2014_us_state_500k, a.cb_2014_us_county_500k
		  FROM a
		EXCEPT
		SELECT b.cb_2014_us_state_500k, b.cb_2014_us_county_500k
		  FROM hierarchy_usa_2014 b 
	 ) AS d
	  GROUP BY d.cb_2014_us_state_500k
)
SELECT s.areaname AS state,
       CASE WHEN c.areaname IS NULL THEN 'No' ELSE 'Yes' END AS county_join,
       SUM(a.total) AS total, MIN(a.min_year) AS min_year, MAX(a.max_year) AS max_year,
	   COUNT(DISTINCT(a.cb_2014_us_county_500k)) AS counties, b.missing_counties, d.extra_counties
  FROM a
	LEFT OUTER JOIN lookup_cb_2014_us_state_500k s ON (a.cb_2014_us_state_500k = s.cb_2014_us_state_500k)
	LEFT OUTER JOIN lookup_cb_2014_us_county_500k c ON (a.cb_2014_us_county_500k = c.cb_2014_us_county_500k)
	LEFT OUTER JOIN b ON (b.cb_2014_us_state_500k = a.cb_2014_us_state_500k)
	LEFT OUTER JOIN d ON (d.cb_2014_us_state_500k = a.cb_2014_us_state_500k)
  GROUP BY s.areaname,
       CASE WHEN c.areaname IS NULL THEN 'No' ELSE 'Yes' END, b.missing_counties, d.extra_counties 
 ORDER BY 1, 2;
/*
                                      SEER_CANCER: Missing counties
    state    | county_join |  total  | min_year | max_year | counties | missing_counties | extra_counties
-------------+-------------+---------+----------+----------+----------+------------------+----------------
 Alaska      | No          |    8080 |     1992 |     2013 |        1 |               29 |              1
 California  | Yes         | 3185854 |     1973 |     2013 |       58 |                  |
 Connecticut | No          |     290 |     1973 |     2013 |        1 |                  |              1
 Connecticut | Yes         |  752971 |     1973 |     2013 |        8 |                  |              1
 Georgia     | Yes         |  810910 |     1975 |     2013 |      159 |                  |
 Hawaii      | No          |  103993 |     1973 |     2012 |        6 |                  |              6
 Hawaii      | Yes         |   97085 |     2000 |     2013 |        5 |                  |              6
 Iowa        | Yes         |  632565 |     1973 |     2013 |       99 |                  |
 Kentucky    | Yes         |  366513 |     2000 |     2013 |      120 |                  |
 Louisiana   | Yes         |  340659 |     2000 |     2013 |       64 |                  |
 Michigan    | Yes         |  854021 |     1973 |     2013 |        3 |               80 |
 New Jersey  | No          |     484 |     2000 |     2013 |        1 |                  |              1
 New Jersey  | Yes         |  752115 |     2000 |     2013 |       21 |                  |              1
 New Mexico  | No          |    3274 |     1973 |     2013 |        2 |                  |              2
 New Mexico  | Yes         |  264387 |     1973 |     2013 |       33 |                  |              2
 Utah        | No          |      20 |     1973 |     2012 |        1 |                  |              1
 Utah        | Yes         |  260654 |     1973 |     2013 |       29 |                  |              1
 Washington  | Yes         |  743088 |     1974 |     2013 |       13 |               26 |
(18 rows)
 */

\pset title 'SEER_POPULATION: 1973 to 2013'
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
 
/*
                                                          SEER_POPULATION: 1973 to 2013
    name     |   y1973   |   y1974   |   y1975   |   y1976   |   y1977   |   y1978   |   y1979   |   y1980   |   y1981   |   y1982   |   y1983
-------------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------
 Alaska      |           |           |           |           |           |           |           |           |           |           |
 California  | 3146214.0 | 3152351.0 | 3158336.0 | 3187020.0 | 3199296.0 | 3213627.0 | 3226422.0 | 3257524.0 | 3295801.0 | 3333983.0 | 3386083.0
 Connecticut | 3069158.0 | 3075790.0 | 3084745.0 | 3085999.0 | 3088745.0 | 3094869.0 | 3099931.0 | 3114221.0 | 3128849.0 | 3139008.0 | 3162366.0
 Georgia     |           |           | 1557188.0 | 1568693.0 | 1596719.0 | 1623547.0 | 1662510.0 | 1697302.0 | 1735379.0 | 1769249.0 | 1816071.0
 Hawaii      |  850517.0 |  866559.0 |  884406.0 |  902061.0 |  915753.0 |  928804.0 |  950042.0 |  961525.0 |  978201.0 |  993775.0 | 1012718.0
 Iowa        | 2864033.0 | 2867942.0 | 2881482.0 | 2903776.0 | 2914404.0 | 2919047.0 | 2916824.0 | 2915562.0 | 2908003.0 | 2888209.0 | 2870580.0
 Kentucky    |           |           |           |           |           |           |           |           |           |           |
 Louisiana   |           |           |           |           |           |           |           |           |           |           |
 Michigan    | 4180145.0 | 4157470.0 | 4124659.0 | 4085999.0 | 4068078.0 | 4061990.0 | 4052133.0 | 4028901.0 | 3963166.0 | 3914028.0 | 3879085.0
 New Jersey  |           |           |           |           |           |           |           |           |           |           |
 New Mexico  | 1104347.0 | 1129663.0 | 1162743.0 | 1195208.0 | 1225310.0 | 1251898.0 | 1280594.0 | 1309108.0 | 1332768.0 | 1363854.0 | 1394380.0
 Utah        | 1168826.0 | 1198852.0 | 1233990.0 | 1272410.0 | 1316443.0 | 1364279.0 | 1416142.0 | 1473171.0 | 1515483.0 | 1558322.0 | 1594953.0
 Washington  |           | 2355599.0 | 2404089.0 | 2444801.0 | 2499651.0 | 2577709.0 | 2667954.0 | 2769482.0 | 2831591.0 | 2862038.0 | 2882160.0
(13 rows)

                                                           SEER_POPULATION: 1973 to 2013
    name     |   y1983   |   y1984   |   y1985   |   y1986   |   y1987   |   y1988   |   y1989   |   y1990   |   y1991   |   y1992    |   y1993
-------------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+------------+------------
 Alaska      |           |           |           |           |           |           |           |           |           |    92191.0 |    94087.0
 California  | 3386083.0 | 3428076.0 | 3479469.0 | 3523875.0 | 3558980.0 | 3603418.0 | 3656983.0 | 3719675.0 | 3760370.0 | 18841579.0 | 18974465.0
 Connecticut | 3162366.0 | 3180017.0 | 3201120.0 | 3223744.0 | 3247304.0 | 3271957.0 | 3283420.0 | 3291967.0 | 3302895.0 |  6601424.0 |  6618350.0
 Georgia     | 1816071.0 | 1871676.0 | 1939915.0 | 2003589.0 | 2063294.0 | 2114397.0 | 2153220.0 | 2191036.0 | 2250436.0 |  4737651.0 |  4895848.0
 Hawaii      | 1012718.0 | 1027923.0 | 1039689.0 | 1051767.0 | 1067914.0 | 1079830.0 | 1094587.0 | 1113491.0 | 1136754.0 |  2317226.0 |  2345676.0
 Iowa        | 2870580.0 | 2858631.0 | 2829691.0 | 2791923.0 | 2767011.0 | 2768370.0 | 2770581.0 | 2781018.0 | 2797613.0 |  5636802.0 |  5673944.0
 Kentucky    |           |           |           |           |           |           |           |           |           |            |
 Louisiana   |           |           |           |           |           |           |           |           |           |            |
 Michigan    | 3879085.0 | 3875699.0 | 3886701.0 | 3905961.0 | 3921395.0 | 3908776.0 | 3908437.0 | 3912880.0 | 3940948.0 |  7938608.0 |  7976560.0
 New Jersey  |           |           |           |           |           |           |           |           |           |            |
 New Mexico  | 1394380.0 | 1416763.0 | 1438385.0 | 1462762.0 | 1478507.0 | 1490356.0 | 1503936.0 | 1521574.0 | 1555305.0 |  3190884.0 |  3272906.0
 Utah        | 1594953.0 | 1622339.0 | 1642906.0 | 1662830.0 | 1678126.0 | 1689374.0 | 1705886.0 | 1731223.0 | 1779780.0 |  3673598.0 |  3796808.0
 Washington  | 2882160.0 | 2916611.0 | 2966106.0 | 3017922.0 | 3090138.0 | 3184588.0 | 3272395.0 | 3394610.0 | 3474519.0 |  7134434.0 |  7274356.0
(13 rows)

                                                               SEER_POPULATION: 1973 to 2013
    name     |   y1993    |   y1994    |   y1995    |   y1996    |   y1997    |   y1998    |   y1999    |   y2000    |   y2001    |   y2002    |   y2003
-------------+------------+------------+------------+------------+------------+------------+------------+------------+------------+------------+------------
 Alaska      |    94087.0 |    95764.0 |    96969.0 |    99390.0 |   101459.0 |   103986.0 |   105727.0 |   107966.0 |   108715.0 |   109756.0 |   110936.0
 California  | 18974465.0 | 19008647.0 | 19074192.0 | 19229705.0 | 19484185.0 | 19750768.0 | 19989429.0 | 38123852.0 | 38654246.0 | 39030387.0 | 39399124.0
 Connecticut |  6618350.0 |  6632242.0 |  6648288.0 |  6673370.0 |  6698696.0 |  6730704.0 |  6772802.0 |  6823554.0 |  6865670.0 |  6917498.0 |  6968672.0
 Georgia     |  4895848.0 |  5054998.0 |  5210731.0 |  5357107.0 |  5502876.0 |  5657288.0 |  5819485.0 | 11156932.0 | 11356509.0 | 11515227.0 | 11654169.0
 Hawaii      |  2345676.0 |  2375072.0 |  2393708.0 |  2407510.0 |  2423280.0 |  2430466.0 |  2420600.0 |  2427038.0 |  2451896.0 |  2479226.0 |  2502308.0
 Iowa        |  5673944.0 |  5701492.0 |  5734746.0 |  5760000.0 |  5782238.0 |  5805744.0 |  5835268.0 |  5858134.0 |  5863994.0 |  5868468.0 |  5883998.0
 Kentucky    |            |            |            |            |            |            |            |  4049021.0 |  4068132.0 |  4089875.0 |  4117170.0
 Louisiana   |            |            |            |            |            |            |            |  4471885.0 |  4477875.0 |  4497267.0 |  4521042.0
 Michigan    |  7976560.0 |  8004876.0 |  8056756.0 |  8107276.0 |  8107114.0 |  8093768.0 |  8088880.0 |  8088532.0 |  8088326.0 |  8064958.0 |  8047784.0
 New Jersey  |            |            |            |            |            |            |            |  8430621.0 |  8492671.0 |  8552643.0 |  8601402.0
 New Mexico  |  3272906.0 |  3364796.0 |  3440788.0 |  3504652.0 |  3549678.0 |  3586968.0 |  3616164.0 |  3642408.0 |  3663380.0 |  3710618.0 |  3755148.0
 Utah        |  3796808.0 |  3920892.0 |  4028354.0 |  4135952.0 |  4239568.0 |  4331920.0 |  4406964.0 |  4489004.0 |  4567430.0 |  4649630.0 |  4720274.0
 Washington  |  7274356.0 |  7378608.0 |  7509184.0 |  7625654.0 |  7778124.0 |  7916534.0 |  8020604.0 |  8115410.0 |  8227160.0 |  8310272.0 |  8364520.0
(13 rows)
                                                               SEER_POPULATION: 1973 to 2013
    name     |   y2003    |   y2004    |   y2005    |   y2006    |   y2007    |   y2008    |   y2009    |   y2010    |   y2011    |   y2012    |   y2013
-------------+------------+------------+------------+------------+------------+------------+------------+------------+------------+------------+------------
 Alaska      |   110936.0 |   112721.0 |   114256.0 |   115621.0 |   116298.0 |   117376.0 |   119327.0 |   120767.0 |   122177.0 |   122842.0 |   123178.0
 California  | 39399124.0 | 39707976.0 | 39965801.0 | 40169926.0 | 40433948.0 | 40848269.0 | 41264059.0 | 41681190.0 | 42102083.0 | 42524959.0 | 42961047.0
 Connecticut |  6968672.0 |  6992188.0 |  7013912.0 |  7034920.0 |  7054540.0 |  7091158.0 |  7123614.0 |  7158690.0 |  7181074.0 |  7188724.0 |  7198682.0
 Georgia     | 11654169.0 | 11828073.0 | 12027681.0 | 12338413.0 | 12595287.0 | 12801825.0 | 12958134.0 | 13091077.0 | 13246819.0 | 13419764.0 | 13536422.0
 Hawaii      |  2502308.0 |  2547138.0 |  2585458.0 |  2619462.0 |  2631350.0 |  2664426.0 |  2693434.0 |  2727900.0 |  2756502.0 |  2785532.0 |  2817974.0
 Iowa        |  5883998.0 |  5907270.0 |  5928908.0 |  5965288.0 |  5998424.0 |  6033468.0 |  6065740.0 |  6100590.0 |  6129808.0 |  6151870.0 |  6184682.0
 Kentucky    |  4117170.0 |  4146101.0 |  4182742.0 |  4219239.0 |  4256672.0 |  4289878.0 |  4317074.0 |  4349838.0 |  4370038.0 |  4383465.0 |  4399583.0
 Louisiana   |  4521042.0 |  4552238.0 |  4411396.0 |  4302665.0 |  4375581.0 |  4435586.0 |  4491648.0 |  4545581.0 |  4575972.0 |  4604744.0 |  4629284.0
 Michigan    |  8047784.0 |  8022120.0 |  7984562.0 |  7936148.0 |  7873114.0 |  7804920.0 |  7755722.0 |  7718714.0 |  7710578.0 |  7722302.0 |  7725040.0
 New Jersey  |  8601402.0 |  8634561.0 |  8651974.0 |  8661679.0 |  8677885.0 |  8711090.0 |  8755602.0 |  8803580.0 |  8842614.0 |  8876000.0 |  8911502.0
 New Mexico  |  3755148.0 |  3807616.0 |  3864548.0 |  3924274.0 |  3980140.0 |  4021324.0 |  4073604.0 |  4129900.0 |  4156814.0 |  4169188.0 |  4173790.0
 Utah        |  4720274.0 |  4803160.0 |  4915438.0 |  5051014.0 |  5195492.0 |  5326058.0 |  5446842.0 |  5548692.0 |  5630648.0 |  5710388.0 |  5805574.0
 Washington  |  8364520.0 |  8452780.0 |  8553660.0 |  8715570.0 |  8834822.0 |  8969992.0 |  9115018.0 |  9201754.0 |  9319160.0 |  9438368.0 |  9565352.0
(13 rows)
 */
 
\pset title 'SEER_POPULATION: Uncoded counties' 
SELECT s.areaname AS state, a.cb_2014_us_county_500k, d.name AS county_name, 
       SUM(a.population) AS population, MIN(a.year) AS min_year, MAX(a.year) AS max_year
  FROM seer_population a
		LEFT OUTER JOIN cb_2014_us_county_500k d ON (a.cb_2014_us_county_500k = d.countyns)
		LEFT OUTER JOIN lookup_cb_2014_us_state_500k s ON (a.cb_2014_us_state_500k = s.cb_2014_us_state_500k)
 WHERE d.name IS NULL
 GROUP BY a.cb_2014_us_state_500k, a.cb_2014_us_county_500k, d.name, s.areaname
 ORDER BY 1,2,3;
/*
                          SEER_POPULATION: Uncoded counties
   state    | cb_2014_us_county_500k | county_name | population | min_year | max_year
------------+------------------------+-------------+------------+----------+----------
 Alaska     | UNKNOWN: 900           |             |  2411509.0 |     1992 |     2013
 Hawaii     | UNKNOWN: 900           |             | 37969854.0 |     1973 |     1999
 New Mexico | UNKNOWN: 910           |             |   466166.0 |     1973 |     1981
(3 rows)
 */

\pset title 'SEER_POPULATION: By year'  
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
/*
           SEER_POPULATION: By year
 year | population  |   males    |  females
------+-------------+------------+------------
 1973 |  16383240.0 |  8028965.0 |  8354275.0
 1974 |  18804226.0 |  9225043.0 |  9579183.0
 1975 |  20491638.0 | 10035168.0 | 10456470.0
 1976 |  20645967.0 | 10109252.0 | 10536715.0
 1977 |  20824399.0 | 10190444.0 | 10633955.0
 1978 |  21035770.0 | 10288056.0 | 10747714.0
 1979 |  21272552.0 | 10400768.0 | 10871784.0
 1980 |  21526796.0 | 10522213.0 | 11004583.0
 1981 |  21689241.0 | 10600394.0 | 11088847.0
 1982 |  21822466.0 | 10664150.0 | 11158316.0
 1983 |  21998396.0 | 10751949.0 | 11246447.0
 1984 |  22197735.0 | 10851660.0 | 11346075.0
 1985 |  22423982.0 | 10963253.0 | 11460729.0
 1986 |  22644373.0 | 11074773.0 | 11569600.0
 1987 |  22872669.0 | 11186897.0 | 11685772.0
 1988 |  23111066.0 | 11306487.0 | 11804579.0
 1989 |  23349445.0 | 11424312.0 | 11925133.0
 1990 |  23657474.0 | 11582818.0 | 12074656.0
 1991 |  23998620.0 | 11754761.0 | 12243859.0
 1992 |  60164397.0 | 29602932.0 | 30561465.0
 1993 |  60923000.0 | 29982677.0 | 30940323.0
 1994 |  61537387.0 | 30290027.0 | 31247360.0
 1995 |  62193716.0 | 30623424.0 | 31570292.0
 1996 |  62900616.0 | 30975680.0 | 31924936.0
 1997 |  63667218.0 | 31360892.0 | 32306326.0
 1998 |  64408146.0 | 31736756.0 | 32671390.0
 1999 |  65075923.0 | 32081334.0 | 32994589.0
 2000 | 105784357.0 | 52166530.0 | 53617827.0
 2001 | 106886004.0 | 52734858.0 | 54151146.0
 2002 | 107795825.0 | 53180182.0 | 54615643.0
 2003 | 108646547.0 | 53565180.0 | 55081367.0
 2004 | 109513942.0 | 54002990.0 | 55510952.0
 2005 | 110200336.0 | 54347618.0 | 55852718.0
 2006 | 111054219.0 | 54777148.0 | 56277071.0
 2007 | 112023553.0 | 55248417.0 | 56775136.0
 2008 | 113115370.0 | 55791773.0 | 57323597.0
 2009 | 114179818.0 | 56320493.0 | 57859325.0
 2010 | 115178273.0 | 56802767.0 | 58375506.0
 2011 | 116144287.0 | 57300845.0 | 58843442.0
 2012 | 117098146.0 | 57781521.0 | 59316625.0
 2013 | 118032110.0 | 58254223.0 | 59777887.0
(41 rows)
 */
 
\pset title 'SEER_WBO_ETHNICITY' 
SELECT CASE	
			WHEN a.ethnicity = 10 THEN 'White, non hispanic'
			WHEN a.ethnicity = 11 THEN 'White, hispanic'
			WHEN a.ethnicity = 19 THEN 'White, no hispanic data'
			WHEN a.ethnicity = 20 THEN 'Black, non hispanic'
			WHEN a.ethnicity = 21 THEN 'Black, hispanic data'
			WHEN a.ethnicity = 29 THEN 'Black, no hispanic data'
			WHEN a.ethnicity = 30 THEN 'American Indian/Alaska Native, non hispanic'
			WHEN a.ethnicity = 31 THEN 'American Indian/Alaska Native, hispanic'
			WHEN a.ethnicity = 39 THEN 'American Indian/Alaska Native, no hispanic data'
			WHEN a.ethnicity = 40 THEN 'Asian or Pacific Islander, non hispanic'
			WHEN a.ethnicity = 41 THEN 'Asian or Pacific Islander, hispanic'
			WHEN a.ethnicity = 49 THEN 'Asian or Pacific Islander, no hispanic data'
			WHEN a.ethnicity = 99 THEN 'Other, no hispanic data'
			ELSE                       'Unknown: '||a.ethnicity::Text
	   END AS ethnicity,
	   SUM(a.population) AS population, MIN(a.year) AS min_year, MAX(a.year) AS max_year
  FROM seer_wbo_ethnicity a
 GROUP BY a.ethnicity
 ORDER BY 1;
/*
                                 SEER_WBO_ETHNICITY
                    ethnicity                    | population  | min_year | max_year
-------------------------------------------------+-------------+----------+----------
 American Indian/Alaska Native, hispanic         |   8638857.0 |     1992 |     2013
 American Indian/Alaska Native, no hispanic data |  70358994.0 |     1992 |     2013
 American Indian/Alaska Native, non hispanic     |  13368513.0 |     1992 |     2013
 Asian or Pacific Islander, hispanic             |   6811115.0 |     1992 |     2013
 Asian or Pacific Islander, non hispanic         | 136427681.0 |     1992 |     2013
 Black, hispanic data                            |  10099021.0 |     1992 |     2013
 Black, no hispanic data                         | 115006990.0 |     1973 |     2013
 Black, non hispanic                             | 169466985.0 |     1992 |     2013
 Other, no hispanic data                         |  29823821.0 |     1973 |     1991
 White, hispanic                                 | 275899275.0 |     1992 |     2013
 White, no hispanic data                         | 793886778.0 |     1973 |     2013
 White, non hispanic                             | 847485215.0 |     1992 |     2013
(12 rows)
 */
 
\pset title 'SAIPE_COUNTY_POVERTY_1989_2015: Uncoded counties'  
SELECT s.areaname AS state, cb_2014_us_county_500k, 
       total_poverty_all_ages, pct_poverty_all_ages, pct_poverty_0_17, 
       pct_poverty_related_5_17, median_household_income, median_hh_income_quin, med_pct_not_in_pov_0_17_quin, 
	   med_pct_not_in_pov_5_17rel_quin, med_pct_not_in_pov_quin
  FROM saipe_county_poverty_1989_2015 a
		LEFT OUTER JOIN lookup_cb_2014_us_state_500k s ON (a.cb_2014_us_state_500k = s.cb_2014_us_state_500k)
 WHERE cb_2014_us_county_500k LIKE 'UNKNOWN%' LIMIT 10; 
/*                                                                                                                          SEER_POPULATION: By year
  state   | cb_2014_us_county_500k | total_poverty_all_ages | pct_poverty_all_ages | pct_poverty_0_17 | pct_poverty_related_5_17 | median_household_income | median_hh_income_quin | med_pct_not_in_pov_0_17_quin | med_pct_not_in_pov_5_17rel_quin | med_pct_not_in_pov_quin
----------+------------------------+------------------------+----------------------+------------------+--------------------------+-------------------------+-----------------------+------------------------------+---------------------------------+-------------------------
 Florida  | UNKNOWN: 025           |                 413489 |                 21.4 |             33.3 |                     29.1 |                   23349 |                     3 |                            1 |                               1 |                       1
 Montana  | UNKNOWN: 113           |                        |                      |                  |                          |                         |                       |                              |                                 |
 Alaska   | UNKNOWN: 201           |                    774 |                 12.2 |             20.0 |                     16.8 |                   31106 |                     5 |                            3 |                               3 |                       4
 Alaska   | UNKNOWN: 231           |                    427 |                  9.6 |             13.3 |                     11.2 |                         |                       |                            4 |                               5 |                       4
 Alaska   | UNKNOWN: 280           |                    570 |                  8.1 |             10.5 |                      8.8 |                   39651 |                     5 |                            5 |                               5 |                       5
 Virginia | UNKNOWN: 515           |                    719 |                 12.5 |             16.3 |                     15.4 |                         |                       |                            4 |                               3 |                       3
 Virginia | UNKNOWN: 560           |                    770 |                 17.2 |             27.2 |                     22.8 |                         |                       |                            2 |                               2 |                       2
 Virginia | UNKNOWN: 780           |                    928 |                 13.5 |             15.8 |                     14.7 |                         |                       |                            4 |                               4 |                       3
 Florida  | UNKNOWN: 025           |                 512176 |                 25.4 |             38.4 |                     34.8 |                   26677 |                     3 |                            1 |                               1 |                       1
 Montana  | UNKNOWN: 113           |                      0 |                  0.0 |              0.0 |                      0.0 |                   36443 |                     5 |                            5 |                               5 |                       5
(10 rows)
 */
 
SELECT * FROM saipe_county_poverty_1989_2015
 WHERE median_household_income_qunitile != median_pct_not_in_poverty_all_ages_qunitile LIMIT 10; 

SELECT year, SUM(total_poverty_all_ages) AS total_poverty_all_ages, COUNT(total_poverty_all_ages) AS counties,
       MIN(median_household_income_qunitile) AS minq,
       MAX(median_household_income_qunitile) AS maxq
  FROM saipe_county_poverty_1989_2015
 GROUP BY year
 ORDER BY year;
 
 
SELECT * FROM rif_data.seer_wbo_ethnicity_covariates LIMIT 20;
 
/*
 year | cb_2014_us_county_500k | pct_white | pct_black | pct_white_quintile | pct_black_quintile
------+------------------------+-----------+-----------+--------------------+--------------------
 1973 | 00212338               |     92.39 |      7.17 |                  1 |                  5
 1973 | 00212668               |     98.20 |      1.28 |                  2 |                  4
 1973 | 00212794               |     92.03 |      7.45 |                  1 |                  5
 1973 | 00212796               |     98.93 |      0.80 |                  3 |                  4
 1973 | 00212797               |     96.46 |      3.19 |                  1 |                  4
 1973 | 00212798               |     91.56 |      7.97 |                  1 |                  5
 1973 | 00212799               |     95.72 |      3.38 |                  1 |                  4
 1973 | 00212801               |     99.00 |      0.64 |                  3 |                  4
 1973 | 00277285               |     95.57 |      2.50 |                  1 |                  4
 1973 | 00277302               |     70.14 |     13.28 |                  1 |                  5
 1973 | 00277305               |     89.07 |      5.50 |                  1 |                  5
 1973 | 00465190               |     99.81 |           |                  5 |                  5
 1973 | 00465191               |     99.84 |           |                  5 |                  5
 1973 | 00465192               |     99.76 |      0.05 |                  4 |                  2
 1973 | 00465193               |     98.80 |      0.94 |                  2 |                  4
 1973 | 00465194               |     99.88 |           |                  5 |                  5
 1973 | 00465195               |     99.85 |      0.01 |                  5 |                  1
 1973 | 00465196               |     94.21 |      5.37 |                  1 |                  5
 1973 | 00465197               |     99.63 |      0.14 |                  4 |                  2
 1973 | 00465198               |     99.48 |      0.27 |                  3 |                  3
(20 rows)
 */ 
--
-- Eof