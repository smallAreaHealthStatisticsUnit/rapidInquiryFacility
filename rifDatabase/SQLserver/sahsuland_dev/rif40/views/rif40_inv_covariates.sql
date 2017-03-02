
--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_inv_covariates]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_inv_covariates]
END
GO

--view definition
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
 
 --permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_covariates] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_inv_covariates] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Details of each covariate used by an investigation in a study', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Covariate name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'covariate_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Minimum value for a covariate', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'min'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maximum value for a covariate', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'max'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography (e.g EW2001). Cannot be changed by the user; present to allow a foreign key to be enforced.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS. Cannot be changed by the user; present to allow a foreign key to be enforced.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_inv_covariates', @level2type=N'COLUMN',@level2name=N'study_geolevel_name'
GO
