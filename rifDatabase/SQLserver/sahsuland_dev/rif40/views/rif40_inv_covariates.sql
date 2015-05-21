USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_inv_covariates]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_inv_covariates]
END
GO

CREATE VIEW [rif40].[rif40_inv_covariates] AS 
SELECT c.username,
    c.study_id,
    c.inv_id,
    c.covariate_name,
    c.min,
    c.max,
    c.geography,
    c.study_geolevel_name
   FROM [rif40].[t_rif40_inv_covariates] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_covariates] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_covariates] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_inv_covariates
  IS 'Details of each covariate used by an investigation in a study';
COMMENT ON COLUMN rif40_inv_covariates.username IS 'Username';
COMMENT ON COLUMN rif40_inv_covariates.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_inv_covariates.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40_inv_covariates.covariate_name IS 'Covariate name';
COMMENT ON COLUMN rif40_inv_covariates.min IS 'Minimum value for a covariate';
COMMENT ON COLUMN rif40_inv_covariates.max IS 'Maximum value for a covariate';
COMMENT ON COLUMN rif40_inv_covariates.geography IS 'Geography (e.g EW2001). Cannot be changed by the user; present to allow a foreign key to be enforced.';
COMMENT ON COLUMN rif40_inv_covariates.study_geolevel_name IS 'Study area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS. Cannot be changed by the user; present to allow a foreign key to be enforced.';
*/
