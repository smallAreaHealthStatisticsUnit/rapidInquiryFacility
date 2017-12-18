-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Postgres script to load new extended SAHSULAND data
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
-- Usage: psql -U rif40 -w -e -f rif_pg_new_sahsuland.sql
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

BEGIN TRANSACTION;
 
DELETE FROM rif_data.covar_sahsuland_covariates3;
\copy rif_data.covar_sahsuland_covariates3(year, sahsu_grd_level3, ses, ethnicity) FROM 'covar_sahsuland_covariates3_extended.csv' DELIMITER ',' CSV HEADER;

CLUSTER covar_sahsuland_covariates3 USING covar_sahsuland_covariates3_pk;

/*
 * Before:
 
 year | sahsu_grd_level3 | ses | maxses | minses
------+------------------+-----+--------+--------
 1989 |              202 |   5 |      5 |      1
 1990 |              202 |   5 |      5 |      1
 1991 |              202 |   5 |      5 |      1
 1992 |              202 |   5 |      5 |      1
 1993 |              202 |   5 |      5 |      1
 1994 |              202 |   5 |      5 |      1
 1995 |              202 |   5 |      5 |      1
 1996 |              202 |   5 |      5 |      1
(8 rows)

 * After:

 year | sahsu_grd_level3 | ses | maxses | minses
------+------------------+-----+--------+--------
 1989 |              202 |   5 |      5 |      1
 1990 |              202 |   5 |      5 |      1
 1991 |              202 |   5 |      5 |      1
 1992 |              202 |   5 |      5 |      1
 1993 |              202 |   5 |      5 |      1
 1994 |              202 |   5 |      5 |      1
 1995 |              202 |   5 |      5 |      1
 1996 |              202 |   5 |      5 |      1
 1997 |              202 |   5 |      5 |      1
 1998 |              202 |   5 |      5 |      1
 1999 |              202 |   5 |      5 |      1
 2000 |              202 |   5 |      5 |      1
 2001 |              202 |   5 |      5 |      1
 2002 |              202 |   5 |      5 |      1
 2003 |              202 |   5 |      5 |      1
 2004 |              202 |   5 |      5 |      1
 2005 |              202 |   5 |      5 |      1
 2006 |              202 |   5 |      5 |      1
 2007 |              202 |   5 |      5 |      1
 2008 |              202 |   5 |      5 |      1
 2009 |              202 |   5 |      5 |      1
 2010 |              202 |   5 |      5 |      1
 2011 |              202 |   5 |      5 |      1
 2012 |              202 |   5 |      5 |      1
 2013 |              202 |   5 |      5 |      1
 2014 |              202 |   5 |      5 |      1
 2015 |              202 |   5 |      5 |      1
 2016 |              202 |   5 |      5 |      1
(28 rows)

 */ 
SELECT year, 
       COUNT(DISTINCT(sahsu_grd_level3)) AS sahsu_grd_level3,
       COUNT(DISTINCT(ses)) AS ses,
       MAX(ses) AS maxses,
       MIN(ses) AS minses
  FROM rif_data.covar_sahsuland_covariates3
 GROUP BY year 
 ORDER BY year;

SELECT year, 
       COUNT(DISTINCT(sahsu_grd_level4)) AS sahsu_grd_level4,
       COUNT(DISTINCT(ses)) AS ses,
       MAX(ses) AS maxses,
       MIN(ses) AS minses
  FROM rif_data.covar_sahsuland_covariates4
 GROUP BY year 
 ORDER BY year;
 
DELETE FROM rif_data.covar_sahsuland_covariates4;
\copy rif_data.covar_sahsuland_covariates4(year, sahsu_grd_level4, ses, areatri1km, near_dist) FROM 'covar_sahsuland_covariates4_extended.csv' DELIMITER ',' CSV HEADER;

CLUSTER covar_sahsuland_covariates4 USING covar_sahsuland_covariates4_pk;

/*
 * Before:
 
 year | sahsu_grd_level4 | ses | maxses | minses
------+------------------+-----+--------+--------
 1989 |             1230 |   5 |      5 |      1
 1990 |             1230 |   5 |      5 |      1
 1991 |             1230 |   5 |      5 |      1
 1992 |             1230 |   5 |      5 |      1
 1993 |             1230 |   5 |      5 |      1
 1994 |             1230 |   5 |      5 |      1
 1995 |             1230 |   5 |      5 |      1
 1996 |             1230 |   5 |      5 |      1
(8 rows)

 * After:

 year | sahsu_grd_level4 | ses | maxses | minses
------+------------------+-----+--------+--------
 1989 |             1230 |   5 |      5 |      1
 1990 |             1230 |   5 |      5 |      1
 1991 |             1230 |   5 |      5 |      1
 1992 |             1230 |   5 |      5 |      1
 1993 |             1230 |   5 |      5 |      1
 1994 |             1230 |   5 |      5 |      1
 1995 |             1230 |   5 |      5 |      1
 1996 |             1230 |   5 |      5 |      1
 1997 |             1230 |   5 |      5 |      1
 1998 |             1230 |   5 |      5 |      1
 1999 |             1230 |   5 |      5 |      1
 2000 |             1230 |   5 |      5 |      1
 2001 |             1230 |   5 |      5 |      1
 2002 |             1230 |   5 |      5 |      1
 2003 |             1230 |   5 |      5 |      1
 2004 |             1230 |   5 |      5 |      1
 2005 |             1230 |   5 |      5 |      1
 2006 |             1230 |   5 |      5 |      1
 2007 |             1230 |   5 |      5 |      1
 2008 |             1230 |   5 |      5 |      1
 2009 |             1230 |   5 |      5 |      1
 2010 |             1230 |   5 |      5 |      1
 2011 |             1230 |   5 |      5 |      1
 2012 |             1230 |   5 |      5 |      1
 2013 |             1230 |   5 |      5 |      1
 2014 |             1230 |   5 |      5 |      1
 2015 |             1230 |   5 |      5 |      1
 2016 |             1230 |   5 |      5 |      1
(28 rows)

 */ 
SELECT year, 
       COUNT(DISTINCT(sahsu_grd_level4)) AS sahsu_grd_level4,
       COUNT(DISTINCT(ses)) AS ses,
       MAX(ses) AS maxses,
       MIN(ses) AS minses
  FROM rif_data.covar_sahsuland_covariates4
 GROUP BY year 
 ORDER BY year;
  
DELETE FROM rif_data.num_sahsuland_cancer;
\copy rif_data.num_sahsuland_cancer(year, age_sex_group, sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4, icd, total) FROM 'num_sahsuland_cancer_extended.csv' DELIMITER ',' CSV HEADER;

CLUSTER num_sahsuland_cancer USING num_sahsuland_cancer_pk;

REINDEX INDEX num_sahsuland_cancer_year;
REINDEX INDEX num_sahsuland_cancer_age_sex_group;
REINDEX INDEX num_sahsuland_cancer_sahsu_grd_level1;
REINDEX INDEX num_sahsuland_cancer_sahsu_grd_level2;
REINDEX INDEX num_sahsuland_cancer_sahsu_grd_level3;
REINDEX INDEX num_sahsuland_cancer_sahsu_grd_level4;
REINDEX INDEX num_sahsuland_cancer_total;

/*
 * Before:
 
 year | age_sex_group | sahsu_grd_level1 | sahsu_grd_level2 | sahsu_grd_level3 | sahsu_grd_level4 | icd | maxicd | minicd | total
------+---------------+------------------+------------------+------------------+------------------+-----+--------+--------+-------
 1989 |            43 |                1 |               17 |              202 |             1180 |  43 | 1919   | 1550   | 17726
 1990 |            40 |                1 |               17 |              202 |             1186 |  44 | 1919   | 1550   | 17854
 1991 |            42 |                1 |               17 |              202 |             1191 |  44 | 1919   | 1550   | 18138
 1992 |            41 |                1 |               17 |              202 |             1196 |  45 | 1919   | 1550   | 19416
 1993 |            43 |                1 |               17 |              202 |             1202 |  45 | 1919   | 1550   | 19498
 1994 |            42 |                1 |               17 |              202 |             1182 |  45 | 1919   | 1550   | 19376
 1995 |            41 |                1 |               17 |              201 |             1192 |  41 | C719   | C220   | 19448
 1996 |            42 |                1 |               17 |              202 |             1195 |  41 | C719   | C220   | 18794
(8 rows)

 * After:

 year | age_sex_group | sahsu_grd_level1 | sahsu_grd_level2 | sahsu_grd_level3 | sahsu_grd_level4 | icd | maxicd | minicd | total
------+---------------+------------------+------------------+------------------+------------------+-----+--------+--------+-------
 1989 |            43 |                1 |               17 |              202 |             1180 |  43 | C719   | C220   | 17726
 1990 |            40 |                1 |               17 |              202 |             1186 |  44 | C719   | C220   | 17854
 1991 |            42 |                1 |               17 |              202 |             1191 |  44 | C719   | C220   | 18138
 1992 |            41 |                1 |               17 |              202 |             1196 |  45 | C719   | C220   | 19416
 1993 |            43 |                1 |               17 |              202 |             1202 |  45 | C719   | C220   | 19498
 1994 |            42 |                1 |               17 |              202 |             1182 |  45 | C719   | C220   | 19376
 1995 |            41 |                1 |               17 |              201 |             1192 |  41 | C719   | C220   | 19448
 1996 |            42 |                1 |               17 |              202 |             1195 |  41 | C719   | C220   | 18794
 1997 |            43 |                1 |               17 |              202 |             1225 |  47 | C719   | C220   | 19413
 1998 |            44 |                1 |               17 |              202 |             1225 |  46 | C719   | C220   | 19947
 1999 |            44 |                1 |               17 |              202 |             1229 |  48 | C719   | C220   | 20400
 2000 |            43 |                1 |               17 |              202 |             1229 |  48 | C719   | C220   | 20694
 2001 |            42 |                1 |               17 |              202 |             1226 |  48 | C719   | C220   | 21097
 2002 |            44 |                1 |               17 |              202 |             1226 |  47 | C719   | C220   | 21333
 2003 |            42 |                1 |               17 |              202 |             1226 |  47 | C719   | C220   | 21504
 2004 |            44 |                1 |               17 |              202 |             1226 |  48 | C719   | C220   | 21748
 2005 |            44 |                1 |               17 |              202 |             1226 |  48 | C719   | C220   | 22058
 2006 |            44 |                1 |               17 |              202 |             1227 |  48 | C719   | C220   | 22620
 2007 |            43 |                1 |               17 |              202 |             1226 |  46 | C719   | C220   | 22666
 2008 |            44 |                1 |               17 |              202 |             1226 |  47 | C719   | C220   | 23070
 2009 |            43 |                1 |               17 |              202 |             1226 |  48 | C719   | C220   | 23220
 2010 |            44 |                1 |               17 |              202 |             1227 |  48 | C719   | C220   | 23518
 2011 |            44 |                1 |               17 |              202 |             1227 |  48 | C719   | C220   | 23758
 2012 |            42 |                1 |               17 |              202 |             1226 |  48 | C719   | C220   | 24054
 2013 |            44 |                1 |               17 |              202 |             1226 |  48 | C719   | C220   | 24300
 2014 |            44 |                1 |               17 |              202 |             1227 |  46 | C719   | C220   | 24298
 2015 |            44 |                1 |               17 |              202 |             1227 |  47 | C719   | C220   | 24669
 2016 |            42 |                1 |               17 |              202 |             1227 |  47 | C719   | C220   | 24893
(28 rows)

 */ 
SELECT year, 
       COUNT(DISTINCT(age_sex_group)) AS age_sex_group,
       COUNT(DISTINCT(sahsu_grd_level1)) AS sahsu_grd_level1,
       COUNT(DISTINCT(sahsu_grd_level2)) AS sahsu_grd_level2,
       COUNT(DISTINCT(sahsu_grd_level3)) AS sahsu_grd_level3,
       COUNT(DISTINCT(sahsu_grd_level4)) AS sahsu_grd_level4,
       COUNT(DISTINCT(icd)) AS icd,
       MAX(icd) AS maxicd,
       MIN(icd) AS minicd,
	   SUM(total) AS total
  FROM rif_data.num_sahsuland_cancer
 GROUP BY year 
 ORDER BY year;
 
DELETE FROM rif_data.pop_sahsuland_pop;
\copy rif_data.pop_sahsuland_pop(year, age_sex_group, sahsu_grd_level1, sahsu_grd_level2, sahsu_grd_level3, sahsu_grd_level4, total) FROM 'pop_sahsuland_pop_extended.csv' DELIMITER ',' CSV HEADER;
 
CLUSTER pop_sahsuland_pop USING pop_sahsuland_pop_pk;
 
REINDEX INDEX pop_sahsuland_pop_year;
REINDEX INDEX pop_sahsuland_pop_age_sex_group;
REINDEX INDEX pop_sahsuland_pop_sahsu_grd_level1;
REINDEX INDEX pop_sahsuland_pop_sahsu_grd_level2;
REINDEX INDEX pop_sahsuland_pop_sahsu_grd_level3;
REINDEX INDEX pop_sahsuland_pop_sahsu_grd_level4;
REINDEX INDEX pop_sahsuland_pop_total;

/*
 * Before:
 
 year | age_sex_group | sahsu_grd_level1 | sahsu_grd_level2 | sahsu_grd_level3 | sahsu_grd_level4 |  total
------+---------------+------------------+------------------+------------------+------------------+----------
 1989 |            44 |                1 |               17 |              202 |             1230 | 10203262
 1990 |            44 |                1 |               17 |              202 |             1230 | 10244438
 1991 |            44 |                1 |               17 |              202 |             1230 | 10299586
 1992 |            44 |                1 |               17 |              202 |             1230 | 10355832
 1993 |            44 |                1 |               17 |              202 |             1230 | 10399326
 1994 |            44 |                1 |               17 |              202 |             1230 | 10463928
 1995 |            44 |                1 |               17 |              202 |             1230 | 10523088
 1996 |            44 |                1 |               17 |              202 |             1230 | 10550510
(8 rows)

 * After:

  year | age_sex_group | sahsu_grd_level1 | sahsu_grd_level2 | sahsu_grd_level3 | sahsu_grd_level4 |  total
------+---------------+------------------+------------------+------------------+------------------+----------
 1989 |            44 |                1 |               17 |              202 |             1230 | 10203262
 1990 |            44 |                1 |               17 |              202 |             1230 | 10244438
 1991 |            44 |                1 |               17 |              202 |             1230 | 10299586
 1992 |            44 |                1 |               17 |              202 |             1230 | 10355832
 1993 |            44 |                1 |               17 |              202 |             1230 | 10399326
 1994 |            44 |                1 |               17 |              202 |             1230 | 10463928
 1995 |            44 |                1 |               17 |              202 |             1230 | 10523088
 1996 |            44 |                1 |               17 |              202 |             1230 | 10550510
 1997 |            44 |                1 |               17 |              202 |             1230 | 10595493
 1998 |            44 |                1 |               17 |              202 |             1230 | 10640311
 1999 |            44 |                1 |               17 |              202 |             1230 | 10684707
 2000 |            44 |                1 |               17 |              202 |             1230 | 10728346
 2001 |            44 |                1 |               17 |              202 |             1230 | 10771063
 2002 |            44 |                1 |               17 |              202 |             1230 | 10812725
 2003 |            44 |                1 |               17 |              202 |             1230 | 10853602
 2004 |            44 |                1 |               17 |              202 |             1230 | 10893618
 2005 |            44 |                1 |               17 |              202 |             1230 | 10932527
 2006 |            44 |                1 |               17 |              202 |             1230 | 10970798
 2007 |            44 |                1 |               17 |              202 |             1230 | 11008542
 2008 |            44 |                1 |               17 |              202 |             1230 | 11045805
 2009 |            44 |                1 |               17 |              202 |             1230 | 11082608
 2010 |            44 |                1 |               17 |              202 |             1230 | 11119108
 2011 |            44 |                1 |               17 |              202 |             1230 | 11155328
 2012 |            44 |                1 |               17 |              202 |             1230 | 11191348
 2013 |            44 |                1 |               17 |              202 |             1230 | 11227282
 2014 |            44 |                1 |               17 |              202 |             1230 | 11262884
 2015 |            44 |                1 |               17 |              202 |             1230 | 11298490
 2016 |            44 |                1 |               17 |              202 |             1230 | 11333947
(28 rows)

 */ 
SELECT year, 
       COUNT(DISTINCT(age_sex_group)) AS age_sex_group,
       COUNT(DISTINCT(sahsu_grd_level1)) AS sahsu_grd_level1,
       COUNT(DISTINCT(sahsu_grd_level2)) AS sahsu_grd_level2,
       COUNT(DISTINCT(sahsu_grd_level3)) AS sahsu_grd_level3,
       COUNT(DISTINCT(sahsu_grd_level4)) AS sahsu_grd_level4,
	   SUM(total) AS total
  FROM rif_data.pop_sahsuland_pop
 GROUP BY year 
 ORDER BY year;

UPDATE rif40.rif40_tables
   SET year_stop   = 2016
 WHERE table_name = 'POP_SAHSULAND_POP';
UPDATE rif40.rif40_tables
   SET year_stop   = 2016
 WHERE table_name = 'NUM_SAHSULAND_CANCER';
 
SELECT table_name, year_start, year_stop
  FROM rif40.rif40_tables
 ORDER BY table_name;

END;

VACUUM ANALYZE rif_data.pop_sahsuland_pop;
VACUUM ANALYZE rif_data.num_sahsuland_cancer;
VACUUM ANALYZE rif_data.covar_sahsuland_covariates3;
VACUUM ANALYZE rif_data.covar_sahsuland_covariates4;

\q

--
-- Eof