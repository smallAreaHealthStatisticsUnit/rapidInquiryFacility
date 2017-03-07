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
   geolevel_name='SAHSU_GRD_LEVEL3' AND 
   covariate_name='SES';


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='SAHSU_GRD_LEVEL3' AND 
   covariate_name='ETHNICITY';


DROP TABLE IF EXISTS rif_data.COVAR_SAHSULAND_COVARIATES3;


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='SAHSU_GRD_LEVEL4' AND 
   covariate_name='SES';


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='SAHSU_GRD_LEVEL4' AND 
   covariate_name='AREATRI1KM';


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='SAHSU_GRD_LEVEL4' AND 
   covariate_name='NEAR_DIST';


DELETE FROM rif40.rif40_covariates 
WHERE 
   geolevel_name='SAHSU_GRD_LEVEL4' AND 
   covariate_name='TRI_1KM';


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
CREATE TABLE pop.POP_SAHSULAND_POP ( 
   year INTEGER NOT NULL,
   age_sex_group INTEGER NOT NULL,
   sahsu_grd_level1 VARCHAR(20) NOT NULL,
   sahsu_grd_level2 VARCHAR(20) NOT NULL,
   sahsu_grd_level3 VARCHAR(20) NOT NULL,
   sahsu_grd_level4 VARCHAR(20) NOT NULL,
   total INTEGER NOT NULL);



\copy POP_SAHSULAND_POP(   YEAR,   AGE_SEX_GROUP,   SAHSU_GRD_LEVEL1,   SAHSU_GRD_LEVEL2,   SAHSU_GRD_LEVEL3,   SAHSU_GRD_LEVEL4,   TOTAL) FROM 'pop_sahsuland_pop.csv' DELIMITER ',' CSV HEADER;

COMMENT ON TABLE POP_SAHSULAND_POP IS 'population health file';

COMMENT ON COLUMN POP_SAHSULAND_POP.YEAR IS 'year field';

COMMENT ON COLUMN POP_SAHSULAND_POP.AGE_SEX_GROUP IS 'An integer field which represents a combination of codes for sex and age.';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL1 IS '';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL2 IS '';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL3 IS 'level 3 resolution field';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL4 IS 'level 4 resolution';

COMMENT ON COLUMN POP_SAHSULAND_POP.TOTAL IS 'total field';

CREATE INDEX POP_SAHSULAND_POP_YEAR ON POP_SAHSULAND_POP(YEAR);

CREATE INDEX POP_SAHSULAND_POP_AGE_SEX_GROUP ON POP_SAHSULAND_POP(AGE_SEX_GROUP);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL1 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL1);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL2 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL2);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL3 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL3);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL4 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL4);

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
   'population health file',
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


-- =========================================================
-- Adding Numerators
-- =========================================================

-- Adding sahsuland_cancer-1.0


CREATE TABLE rif_data.num_sahsuland_cancer ( 
   year INTEGER NOT NULL,
   age_sex_group INTEGER NOT NULL,
   sahsu_grd_level1 VARCHAR(20) NOT NULL,
   sahsu_grd_level2 VARCHAR(20) NOT NULL,
   sahsu_grd_level3 VARCHAR(20) NOT NULL,
   sahsu_grd_level4 VARCHAR(20) NOT NULL,
   icd VARCHAR NOT NULL,
   total INTEGER NOT NULL);


\copy num_sahsuland_cancer(   year,   age_sex_group,   SAHSU_GRD_LEVEL1,   SAHSU_GRD_LEVEL2,   SAHSU_GRD_LEVEL3,   SAHSU_GRD_LEVEL4,   icd,   total) FROM 'num_sahsuland_cancer.csv' DELIMITER ',' CSV HEADER;

COMMENT ON TABLE NUM_SAHSULAND_CANCER IS 'sahsuland cancer cases';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.YEAR IS '';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.AGE_SEX_GROUP IS 'An integer field which represents a combination of codes for sex and age.';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL1 IS 'level one field resolution';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL2 IS 'level 2 field resolution';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL3 IS 'level 3 resolution field';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL4 IS 'level four resolution level';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.ICD IS 'icd code';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.TOTAL IS 'total field';

CREATE INDEX NUM_SAHSULAND_CANCER_YEAR ON NUM_SAHSULAND_CANCER(YEAR);

CREATE INDEX NUM_SAHSULAND_CANCER_AGE_SEX_GROUP ON NUM_SAHSULAND_CANCER(AGE_SEX_GROUP);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL1 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL1);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL2 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL2);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL3 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL3);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL4 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL4);

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
   'sahsuland cancer cases',
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


-- =========================================================
-- Adding Covariates
-- =========================================================

-- Adding sahsuland_covariates3-1.0


CREATE TABLE rif_data.COVAR_SAHSULAND_COVARIATES3 ( 
   year INTEGER NOT NULL,
   sahsu_grd_level3 VARCHAR(20) NOT NULL,
   ses INTEGER,
   ethnicity INTEGER);


\copy COVAR_SAHSULAND_COVARIATES3(   year,   SAHSU_GRD_LEVEL3,   ses,   ethnicity) FROM 'covar_sahsuland_covariates3.csv' DELIMITER ',' CSV HEADER;

CREATE UNIQUE INDEX COVAR_SAHSULAND_COVARIATES3_pk ON COVAR_SAHSULAND_COVARIATES3(YEAR,SAHSU_GRD_LEVEL3);

COMMENT ON TABLE COVAR_SAHSULAND_COVARIATES3 IS 'covariates level 3';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.YEAR IS 'year field value';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.SAHSU_GRD_LEVEL3 IS 'geographical resolution field level 3';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.SES IS 'socio economic status';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.ETHNICITY IS 'ethnicity';

UPDATE t_rif40_geolevels 
SET covariate_table = 'COVAR_SAHSULAND_COVARIATES3' WHERE geography='SAHSULAND' AND geolevel_name='SAHSU_GRD_LEVEL3';



INSERT INTO rif40.rif40_covariates (
   geography,
   geolevel_name,
   covariate_name,
   min,
   max,
   type) 
SELECT 
   'SAHSULAND',
   'SAHSU_GRD_LEVEL3',
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
   'SAHSU_GRD_LEVEL3',
   'ETHNICITY',
   MIN(ETHNICITY),
   MAX(ETHNICITY),
   1
FROM 
   COVAR_SAHSULAND_COVARIATES3;


-- Adding sahsuland_covariates4-1.0


CREATE TABLE rif_data.COVAR_SAHSULAND_COVARIATES4 ( 
   year INTEGER NOT NULL,
   sahsu_grd_level4 VARCHAR(20) NOT NULL,
   ses INTEGER,
   areatri1km INTEGER,
   near_dist DOUBLE PRECISION,
   tri_1km DOUBLE PRECISION);


\copy COVAR_SAHSULAND_COVARIATES4(   year,   SAHSU_GRD_LEVEL4,   ses,   areatri1km,   near_dist,   tri_1km) FROM 'covar_sahsuland_covariates4.csv' DELIMITER ',' CSV HEADER;

CREATE UNIQUE INDEX COVAR_SAHSULAND_COVARIATES4_pk ON COVAR_SAHSULAND_COVARIATES4(YEAR,SAHSU_GRD_LEVEL4);

COMMENT ON TABLE COVAR_SAHSULAND_COVARIATES4 IS 'level four covariates';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.YEAR IS 'year field value';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.SAHSU_GRD_LEVEL4 IS 'level four covariate';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.SES IS 'socio economic status';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.AREATRI1KM IS 'area tri 1 km';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.NEAR_DIST IS 'near distance';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.TRI_1KM IS 'tri 1 km';

UPDATE t_rif40_geolevels 
SET covariate_table = 'COVAR_SAHSULAND_COVARIATES4' WHERE geography='SAHSULAND' AND geolevel_name='SAHSU_GRD_LEVEL4';



INSERT INTO rif40.rif40_covariates (
   geography,
   geolevel_name,
   covariate_name,
   min,
   max,
   type) 
SELECT 
   'SAHSULAND',
   'SAHSU_GRD_LEVEL4',
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
   'SAHSU_GRD_LEVEL4',
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
   'SAHSU_GRD_LEVEL4',
   'NEAR_DIST',
   MIN(NEAR_DIST),
   MAX(NEAR_DIST),
   2
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
   'SAHSU_GRD_LEVEL4',
   'TRI_1KM',
   MIN(TRI_1KM),
   MAX(TRI_1KM),
   2
FROM 
   COVAR_SAHSULAND_COVARIATES4;

GRANT SELECT ON rif_data.covar_sahsuland_covariates3 TO rif_user, rif_manager;

GRANT SELECT ON rif_data.covar_sahsuland_covariates4 TO rif_user, rif_manager;

GRANT SELECT ON rif_data.num_sahsuland_cancer TO rif_user, rif_manager;

GRANT SELECT ON rif_data.pop_sahsuland_pop TO rif_user, rif_manager;

COMMIT TRANSACTION;