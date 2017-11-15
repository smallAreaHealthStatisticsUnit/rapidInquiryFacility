\set ECHO all
\set ON_ERROR_STOP ON
\timing
BEGIN TRANSACTION;

DO LANGUAGE plpgsql $$
DECLARE
	c1 CURSOR FOR
		SELECT COUNT(DISTINCT(a.study_id)) AS total
		  FROM t_rif40_studies a, t_rif40_investigations b
		 WHERE (b.numer_tab = 'NUM_SAHSULAND_CANCER' 
    OR  a.denom_tab = 'POP_SAHSULAND_POP')
		   AND a.geography  = 'SAHSULAND'
		   AND A.study_id   = b.study_id;
	c1_rec RECORD;
BEGIN
	OPEN c1;
	FETCH c1 INTO c1_rec;
	CLOSE c1;
--
	IF c1_rec.total = 0 THEN
		RAISE INFO 'Geography: SAHSULAND is not used by any studies';
	ELSE
		RAISE EXCEPTION 'Geography: SAHSULAND is used by: % studies', c1_rec.total;
	END IF;
END;
$$;

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
VALUES ('cancers','covering various types of cancers');


-- =========================================================
-- Adding Denominators
-- =========================================================

-- Adding sahsuland_pop-1.0
CREATE TABLE rif_data.POP_SAHSULAND_POP ( 
   year INTEGER NOT NULL,
   age_sex_group INTEGER NOT NULL,
   sahsu_grd_level1 VARCHAR(20) NOT NULL,
   sahsu_grd_level2 VARCHAR(20) NOT NULL,
   sahsu_grd_level3 VARCHAR(20) NOT NULL,
   sahsu_grd_level4 VARCHAR(20) NOT NULL,
   total INTEGER NOT NULL);



\copy POP_SAHSULAND_POP(   YEAR,   AGE_SEX_GROUP,   SAHSU_GRD_LEVEL1,   SAHSU_GRD_LEVEL2,   SAHSU_GRD_LEVEL3,   SAHSU_GRD_LEVEL4,   TOTAL) FROM 'pop_sahsuland_pop_extended.csv' DELIMITER ',' CSV HEADER;

ALTER TABLE rif_data.pop_sahsuland_pop ADD CONSTRAINT pop_sahsuland_pop_pk PRIMARY KEY(YEAR,AGE_SEX_GROUP,SAHSU_GRD_LEVEL4);
CLUSTER pop_sahsuland_pop USING pop_sahsuland_pop_pk;



COMMENT ON TABLE POP_SAHSULAND_POP IS 'population health file';

COMMENT ON COLUMN POP_SAHSULAND_POP.YEAR IS 'year field';

COMMENT ON COLUMN POP_SAHSULAND_POP.AGE_SEX_GROUP IS 'An integer field which represents a combination of codes for sex and age.';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL1 IS 'first level geographical resolution';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL2 IS 'second geographical resolution';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL3 IS 'third level of geographical resolution';

COMMENT ON COLUMN POP_SAHSULAND_POP.SAHSU_GRD_LEVEL4 IS 'fourth level geographical resolution';

COMMENT ON COLUMN POP_SAHSULAND_POP.TOTAL IS 'total field';

CREATE INDEX POP_SAHSULAND_POP_YEAR ON POP_SAHSULAND_POP(YEAR);

CREATE INDEX POP_SAHSULAND_POP_AGE_SEX_GROUP ON POP_SAHSULAND_POP(AGE_SEX_GROUP);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL1 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL1);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL2 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL2);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL3 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL3);

CREATE INDEX POP_SAHSULAND_POP_SAHSU_GRD_LEVEL4 ON POP_SAHSULAND_POP(SAHSU_GRD_LEVEL4);

CREATE INDEX POP_SAHSULAND_POP_TOTAL ON POP_SAHSULAND_POP(TOTAL);

CREATE INDEX pop_sahsuland_pop_age_group
  ON rif_data.pop_sahsuland_pop
  (MOD(age_sex_group, 100));
CREATE INDEX pop_sahsuland_pop_sex
  ON rif_data.pop_sahsuland_pop
  (TRUNC(age_sex_group/100));

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
   'TOTAL',
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


GRANT SELECT ON rif_data.POP_SAHSULAND_POP TO rif_user, rif_manager;




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


\copy num_sahsuland_cancer(   year,   age_sex_group,   SAHSU_GRD_LEVEL1,   SAHSU_GRD_LEVEL2,   SAHSU_GRD_LEVEL3,   SAHSU_GRD_LEVEL4,   icd,   total) FROM 'num_sahsuland_cancer_extended.csv' DELIMITER ',' CSV HEADER;

ALTER TABLE rif_data.num_sahsuland_cancer ADD CONSTRAINT num_sahsuland_cancer_pk PRIMARY KEY(YEAR,AGE_SEX_GROUP,SAHSU_GRD_LEVEL4,ICD);
CLUSTER num_sahsuland_cancer USING num_sahsuland_cancer_pk;



COMMENT ON TABLE NUM_SAHSULAND_CANCER IS 'cancer numerator';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.YEAR IS 'year field';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.AGE_SEX_GROUP IS 'An integer field which represents a combination of codes for sex and age.';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL1 IS 'first level geographical resolution';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL2 IS 'second geographical resolution';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL3 IS 'third level of geographical resolution';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.SAHSU_GRD_LEVEL4 IS 'fourth level geographical resolution';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.ICD IS 'ICD code field';

COMMENT ON COLUMN NUM_SAHSULAND_CANCER.TOTAL IS 'total field';

CREATE INDEX NUM_SAHSULAND_CANCER_YEAR ON NUM_SAHSULAND_CANCER(YEAR);

CREATE INDEX NUM_SAHSULAND_CANCER_AGE_SEX_GROUP ON NUM_SAHSULAND_CANCER(AGE_SEX_GROUP);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL1 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL1);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL2 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL2);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL3 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL3);

CREATE INDEX NUM_SAHSULAND_CANCER_SAHSU_GRD_LEVEL4 ON NUM_SAHSULAND_CANCER(SAHSU_GRD_LEVEL4);

CREATE INDEX NUM_SAHSULAND_CANCER_ICD ON NUM_SAHSULAND_CANCER(ICD);

CREATE INDEX NUM_SAHSULAND_CANCER_TOTAL ON NUM_SAHSULAND_CANCER(TOTAL);

CREATE INDEX num_sahsuland_cancer_age_group
  ON rif_data.num_sahsuland_cancer
  (MOD(age_sex_group, 100));
CREATE INDEX num_sahsuland_cancer_sex
  ON rif_data.num_sahsuland_cancer
  (TRUNC(age_sex_group/100));
  
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
   'cancer numerator',
   MIN(year),
   MAX(year),
   'TOTAL',
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



GRANT SELECT ON rif_data.NUM_SAHSULAND_CANCER TO rif_user, rif_manager;




-- =========================================================
-- Adding Covariates
-- =========================================================

-- Adding sahsuland_covariates3-1.0


CREATE TABLE rif_data.COVAR_SAHSULAND_COVARIATES3 ( 
   year INTEGER NOT NULL,
   sahsu_grd_level3 VARCHAR(20) NOT NULL,
   ses INTEGER,
   ethnicity INTEGER);


\copy COVAR_SAHSULAND_COVARIATES3(   year,   SAHSU_GRD_LEVEL3,   ses,   ethnicity) FROM 'covar_sahsuland_covariates3_extended.csv' DELIMITER ',' CSV HEADER;

ALTER TABLE rif_data.covar_sahsuland_covariates3 ADD CONSTRAINT covar_sahsuland_covariates3_pk PRIMARY KEY(YEAR,SAHSU_GRD_LEVEL3);
CLUSTER covar_sahsuland_covariates3 USING covar_sahsuland_covariates3_pk;



COMMENT ON TABLE COVAR_SAHSULAND_COVARIATES3 IS 'covariate file';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.YEAR IS 'year field';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.SAHSU_GRD_LEVEL3 IS 'third level of geographical resolution';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.SES IS 'socio-economic status';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES3.ETHNICITY IS 'ethnicity';


GRANT SELECT ON rif_data.COVAR_SAHSULAND_COVARIATES3 TO rif_user, rif_manager;



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
   near_dist DOUBLE PRECISION);


\copy COVAR_SAHSULAND_COVARIATES4(   year,   SAHSU_GRD_LEVEL4,   ses,   areatri1km,   near_dist) FROM 'covar_sahsuland_covariates4_extended.csv' DELIMITER ',' CSV HEADER;

ALTER TABLE rif_data.covar_sahsuland_covariates4 ADD CONSTRAINT covar_sahsuland_covariates4_pk PRIMARY KEY(YEAR,SAHSU_GRD_LEVEL4);
CLUSTER covar_sahsuland_covariates4 USING covar_sahsuland_covariates4_pk;



COMMENT ON TABLE COVAR_SAHSULAND_COVARIATES4 IS 'covariate file';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.YEAR IS 'year field';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.SAHSU_GRD_LEVEL4 IS 'fourth level geographical resolution';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.SES IS 'socio-economic status';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.AREATRI1KM IS 'area tri 1 km covariate';

COMMENT ON COLUMN COVAR_SAHSULAND_COVARIATES4.NEAR_DIST IS 'near distance covariate';


GRANT SELECT ON rif_data.COVAR_SAHSULAND_COVARIATES4 TO rif_user, rif_manager;



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



COMMIT TRANSACTION;


ANALYZE VERBOSE rif_data.pop_sahsuland_pop;
ANALYZE VERBOSE rif_data.num_sahsuland_cancer;
ANALYZE VERBOSE rif_data.covar_sahsuland_covariates4;
ANALYZE VERBOSE rif_data.covar_sahsuland_covariates4;
