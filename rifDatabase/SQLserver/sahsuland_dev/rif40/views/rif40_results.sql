
--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_results]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_results]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_results] AS 
SELECT c.username,
    c.study_id,
    c.inv_id,
    c.band_id,
    c.genders,
    c.direct_standardisation,
    c.adjusted,
    c.observed,
    c.expected,
    c.lower95,
    c.upper95,
    c.relative_risk,
    c.smoothed_relative_risk,
    c.posterior_probability,
    c.posterior_probability_upper95,
    c.posterior_probability_lower95,
    c.residual_relative_risk,
    c.residual_rr_lower95,
    c.residual_rr_upper95,
    c.smoothed_smr,
    c.smoothed_smr_lower95,
    c.smoothed_smr_upper95
   FROM [rif40].[t_rif40_results] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND  s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
  
--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_results] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_results] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF Results VIEW' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation inde:inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'A band allocated to the area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'band_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Genders to be investigated: 1 - males, 2 female or 3 - both', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'genders'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Standardisation: indirect (0) or direct (1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'direct_standardisation'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Covariate adjustment: Unadjusted (0) or adjusted (1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'adjusted'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The number of observed cases', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'observed'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The number of expected cases or the rate (for direct standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'expected'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval for the relative risk (for indirectly standarised results) or the lower 95% confidence interval for the rate (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval for the relative risk (for indirectly standarised results) or the upper 95% confidence interval for the rate (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'upper95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Relative risk (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'relative_risk'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Smoothed relative risk (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_relative_risk'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The posterior probability (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'posterior_probability'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'posterior_probability_lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'posterior_probability_upper95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'residual_relative_risk'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'residual_rr_lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'residual_rr_upper95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_smr'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_smr_lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_smr_upper95'
GO
