\set ECHO all
\set ON_ERROR_STOP ON
\timing
BEGIN TRANSACTION;
-- =========================================================
-- Deleting data from previous run of this script
-- =========================================================

DELETE FROM rif40.rif40_tables 
WHERE 
   table_name='POP_SAHSULAND_POP';


DROP TABLE IF EXISTS pop.POP_SAHSULAND_POP;


DELETE FROM rif40.rif40_table_outcomes 
WHERE 
   numer_tab='NUM_SAHSULAND_CANCER';


DELETE FROM rif40.rif40_tables 
WHERE 
   table_name='NUM_SAHSULAND_CANCER';


DROP TABLE IF EXISTS rif_data.NUM_SAHSULAND_CANCER;


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='LEVEL3' AND 
   covariate_name='SES';


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='LEVEL3' AND 
   covariate_name='ETHNICITY';


DROP TABLE IF EXISTS rif_data.COVAR_SAHSULAND_COVARIATES3;


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='LEVEL4' AND 
   covariate_name='SES';


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='LEVEL4' AND 
   covariate_name='AREATRI1KM';


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='LEVEL4' AND 
   covariate_name='NEAR_DIST';


DROP TABLE IF EXISTS rif_data.COVAR_SAHSULAND_COVARIATES4;


DELETE FROM rif40.rif40_health_study_themes 
WHERE 
   theme='cancers';


-- =========================================================
-- Adding Health Themes
-- =========================================================

INSERT INTO rif40.rif40_health_study_themes( 
   theme,
   description) 
VALUES ('cancers','cancer things');


-- =========================================================
-- Adding Denominators
-- =========================================================

-- Adding sahsuland_pop-1.0
CREATE TABLE rif_data.POP_SAHSULAND_POP ( 
   year INTEGER NOT NULL,
   age_sex_group INTEGER NOT NULL,
   level1 VARCHAR(20) NOT NULL,
   level2 VARCHAR(20) NOT NULL,
   level3 VARCHAR(20) NOT NULL,
   level4 VARCHAR(20) NOT NULL,
   total INTEGER NOT NULL);



\copy POP_SAHSULAND_POP(   YEAR,   AGE_SEX_GROUP,   LEVEL1,   LEVEL2,   LEVEL3,   LEVEL4,   TOTAL) FROM 'pop_sahsuland_pop.csv' DELIMITER ',' CSV HEADER;


SELECT COUNT(*) AS total FROM pop_sahsuland_pop;
WITH a AS (
	SELECT year,age_sex_group,level4, COUNT(*) total
	  FROM pop_sahsuland_pop
	 GROUP BY year,age_sex_group,level4
	HAVING COUNT(*) > 1
)
SELECT age_sex_group, SUM(total) AS total
  FROM a
 GROUP BY age_sex_group
 ORDER BY 1
LIMIT 50;
  
ALTER TABLE rif_data.pop_sahsuland_pop ADD CONSTRAINT pop_sahsuland_pop_pk PRIMARY KEY(YEAR,AGE_SEX_GROUP,LEVEL4);
CLUSTER pop_sahsuland_pop USING pop_sahsuland_pop_pk;

COMMENT ON TABLE POP_SAHSULAND_POP IS 'population data set';

COMMENT ON COLUMN POP_SAHSULAND_POP.YEAR IS 'year field';

COMMENT ON COLUMN POP_SAHSULAND_POP.AGE_SEX_GROUP IS 'An integer field which represents a combination of codes for sex and age.';

COMMENT ON COLUMN POP_SAHSULAND_POP.LEVEL1 IS 'level one';

COMMENT ON COLUMN POP_SAHSULAND_POP.LEVEL2 IS 'level 2';

COMMENT ON COLUMN POP_SAHSULAND_POP.LEVEL3 IS 'level three';

COMMENT ON COLUMN POP_SAHSULAND_POP.LEVEL4 IS 'level four';

COMMENT ON COLUMN POP_SAHSULAND_POP.TOTAL IS 'total field';

CREATE INDEX POP_SAHSULAND_POP_YEAR ON POP_SAHSULAND_POP(YEAR);

CREATE INDEX POP_SAHSULAND_POP_AGE_SEX_GROUP ON POP_SAHSULAND_POP(AGE_SEX_GROUP);

CREATE INDEX POP_SAHSULAND_POP_LEVEL1 ON POP_SAHSULAND_POP(LEVEL1);

CREATE INDEX POP_SAHSULAND_POP_LEVEL2 ON POP_SAHSULAND_POP(LEVEL2);

CREATE INDEX POP_SAHSULAND_POP_LEVEL3 ON POP_SAHSULAND_POP(LEVEL3);

CREATE INDEX POP_SAHSULAND_POP_LEVEL4 ON POP_SAHSULAND_POP(LEVEL4);

CREATE INDEX POP_SAHSULAND_POP_TOTAL ON POP_SAHSULAND_POP(TOTAL);

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
   'cancers',
   'POP_SAHSULAND_POP',
   'population data set',
   MIN(YEAR),
   MAX(YEAR),
   null,
   1,
   0,
   0,
   1,
   null,
   null,
   'AGE_SEX_GROUP',
   1
FROM
   POP_SAHSULAND_POP;


GRANT SELECT ON POP_SAHSULAND_POP TO rif_user, rif_manager;




-- =========================================================
-- Adding Numerators
-- =========================================================

-- Adding sahsuland_cancer-1.0


CREATE TABLE rif_data.num_sahsuland_cancer ( 
   year INTEGER NOT NULL,
   age_sex_group INTEGER NOT NULL,
   level1 VARCHAR(20) NOT NULL,
   level2 VARCHAR(20) NOT NULL,
   level3 VARCHAR(20) NOT NULL,
   level4 VARCHAR(20) NOT NULL,
   icd VARCHAR NOT NULL,
   total INTEGER NOT NULL);


\copy num_sahsuland_cancer(   year,   age_sex_group,   level1,   level2,   level3,   level4,   icd,   total) FROM 'num_sahsuland_cancer.csv' DELIMITER ',' CSV HEADER;

ALTER TABLE rif_data.num_sahsuland_cancer ADD CONSTRAINT num_sahsuland_cancer_pk PRIMARY KEY(YEAR,AGE_SEX_GROUP,LEVEL4);
CLUSTER num_sahsuland_cancer USING num_sahsuland_cancer_pk;



COMMENT ON TABLE NUM_SAHSULAND_CANCER IS 'cancer data set';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.YEAR IS 'year field';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.AGE_SEX_GROUP IS 'An integer field which represents a combination of codes for sex and age.';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.LEVEL1 IS 'level one';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.LEVEL2 IS 'level two';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.LEVEL3 IS 'level three';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.LEVEL4 IS 'level four';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.ICD IS 'icd';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.TOTAL IS 'total';

CREATE INDEX NUM_SAHSULAND_CANCER_YEAR ON NUM_SAHSULAND_CANCER(YEAR);

CREATE INDEX NUM_SAHSULAND_CANCER_AGE_SEX_GROUP ON NUM_SAHSULAND_CANCER(AGE_SEX_GROUP);

CREATE INDEX NUM_SAHSULAND_CANCER_LEVEL1 ON NUM_SAHSULAND_CANCER(LEVEL1);

CREATE INDEX NUM_SAHSULAND_CANCER_LEVEL2 ON NUM_SAHSULAND_CANCER(LEVEL2);

CREATE INDEX NUM_SAHSULAND_CANCER_LEVEL3 ON NUM_SAHSULAND_CANCER(LEVEL3);

CREATE INDEX NUM_SAHSULAND_CANCER_LEVEL4 ON NUM_SAHSULAND_CANCER(LEVEL4);

CREATE INDEX NUM_SAHSULAND_CANCER_ICD ON NUM_SAHSULAND_CANCER(ICD);

CREATE INDEX NUM_SAHSULAND_CANCER_TOTAL ON NUM_SAHSULAND_CANCER(TOTAL);


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
   'cancers',
   'NUM_SAHSULAND_CANCER',
   'cancer data set',
   MIN(year),
   MAX(year),
   null,
   0,
   0,
   1,
   1,
   null,
   null,
   'AGE_SEX_GROUP',
   1
FROM
   num_sahsuland_cancer;


INSERT INTO rif40.rif40_outcome_groups(
   outcome_type, outcome_group_name, outcome_group_description, field_name, multiple_field_count)
SELECT
   'ICD' AS outcome_type,
   'SAHSULAND_ICD' AS outcome_group_name,
   'SAHSULAND ICD' AS outcome_group_description,
   'ICD' AS field_name,
   0 AS multiple_field_count
WHERE NOT EXISTS (SELECT outcome_group_name FROM  rif40.rif40_outcome_groups WHERE outcome_group_name = 'SAHSULAND_ICD');



INSERT INTO rif40.rif40_table_outcomes (
   outcome_group_name,
   numer_tab,
   current_version_start_year) 
SELECT 
   'SAHSULAND_ICD',
   'NUM_SAHSULAND_CANCER',
   MIN(year) 
FROM 
   NUM_SAHSULAND_CANCER;



GRANT SELECT ON NUM_SAHSULAND_CANCER TO rif_user, rif_manager;




-- =========================================================
-- Adding Covariates
-- =========================================================

-- Adding sahsuland_covariates3-1.0


CREATE TABLE rif_data.COVAR_SAHSULAND_COVARIATES3 ( 
   year INTEGER NOT NULL,
   level3 VARCHAR(20) NOT NULL);


\copy COVAR_SAHSULAND_COVARIATES3(   year,   level3,   ses,   ethnicity) FROM 'covar_sahsuland_covariates3.csv' DELIMITER ',' CSV HEADER;

ALTER TABLE rif_data.covar_sahsuland_covariates3 ADD CONSTRAINT covar_sahsuland_covariates3_pk PRIMARY KEY(YEAR,LEVEL3);
CLUSTER covar_sahsuland_covariates3 USING covar_sahsuland_covariates3_pk;



CREATE UNIQUE INDEX covar_sahsuland_covariates3_pk ON rif_data.covar_sahsuland_covariates3(YEAR,LEVEL3);

COMMENT ON TABLE COVAR_SAHSULAND_COVARIATES3 IS 'covariate level 3';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.YEAR IS 'year field';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.LEVEL3 IS '';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.SES IS 'ses';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.ETHNICITY IS 'ethnicity';


GRANT SELECT ON COVAR_SAHSULAND_COVARIATES3 TO rif_user, rif_manager;



UPDATE t_rif40_geolevels 
SET covariate_table = 'COVAR_SAHSULAND_COVARIATES3' WHERE geography='SAHSULAND' AND geolevel_name='LEVEL3';



INSERT INTO rif40.rif40_covariates (
   geography,
   geolevel_name,
   covariate_name,
   min,
   max,
   type) 
SELECT 
   'SAHSULAND',
   'LEVEL3',
   'SES',
   MIN(SES),
   MAX(SES),
   1
FROM 
   COVAR_SAHSULAND_COVARIATES3;

INSERT INTO rif40.rif40_covariates (
   geography,
   geolevel_name,
   covariate_name,
   min,
   max,
   type) 
SELECT 
   'SAHSULAND',
   'LEVEL3',
   'ETHNICITY',
   MIN(ETHNICITY),
   MAX(ETHNICITY),
   1
FROM 
   COVAR_SAHSULAND_COVARIATES3;


-- Adding sahsuland_covariates4-1.0


CREATE TABLE rif_data.COVAR_SAHSULAND_COVARIATES4 ( 
   year INTEGER NOT NULL,
   level4 VARCHAR(20) NOT NULL);


\copy COVAR_SAHSULAND_COVARIATES4(   year,   level4,   ses,   areatri1km,   near_dist) FROM 'covar_sahsuland_covariates4.csv' DELIMITER ',' CSV HEADER;

ALTER TABLE rif_data.covar_sahsuland_covariates4 ADD CONSTRAINT covar_sahsuland_covariates4_pk PRIMARY KEY(YEAR,LEVEL4);
CLUSTER covar_sahsuland_covariates4 USING covar_sahsuland_covariates4_pk;



CREATE UNIQUE INDEX covar_sahsuland_covariates4_pk ON rif_data.covar_sahsuland_covariates4(YEAR,LEVEL4);

COMMENT ON TABLE COVAR_SAHSULAND_COVARIATES4 IS 'covariates level 4';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.YEAR IS '';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.LEVEL4 IS 'level4';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.SES IS 'ses';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.AREATRI1KM IS 'areatri 1km';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.NEAR_DIST IS 'near dist';


GRANT SELECT ON COVAR_SAHSULAND_COVARIATES4 TO rif_user, rif_manager;



UPDATE t_rif40_geolevels 
SET covariate_table = 'COVAR_SAHSULAND_COVARIATES4' WHERE geography='SAHSULAND' AND geolevel_name='LEVEL4';



INSERT INTO rif40.rif40_covariates (
   geography,
   geolevel_name,
   covariate_name,
   min,
   max,
   type) 
SELECT 
   'SAHSULAND',
   'LEVEL4',
   'SES',
   MIN(SES),
   MAX(SES),
   1
FROM 
   COVAR_SAHSULAND_COVARIATES4;

INSERT INTO rif40.rif40_covariates (
   geography,
   geolevel_name,
   covariate_name,
   min,
   max,
   type) 
SELECT 
   'SAHSULAND',
   'LEVEL4',
   'AREATRI1KM',
   MIN(AREATRI1KM),
   MAX(AREATRI1KM),
   1
FROM 
   COVAR_SAHSULAND_COVARIATES4;

INSERT INTO rif40.rif40_covariates (
   geography,
   geolevel_name,
   covariate_name,
   min,
   max,
   type) 
SELECT 
   'SAHSULAND',
   'LEVEL4',
   'NEAR_DIST',
   MIN(NEAR_DIST),
   MAX(NEAR_DIST),
   1
FROM 
   COVAR_SAHSULAND_COVARIATES4;



COMMIT TRANSACTION;