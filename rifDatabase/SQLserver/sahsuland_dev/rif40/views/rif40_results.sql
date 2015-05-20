USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_results]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_results]
END
GO

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
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
  
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_results] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_results] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_results
  IS 'RIF Results Table';
COMMENT ON COLUMN rif40_results.username IS 'Username';
COMMENT ON COLUMN rif40_results.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_results.inv_id IS 'Unique investigation inde:inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40_results.band_id IS 'A band allocated to the area';
COMMENT ON COLUMN rif40_results.genders IS 'Genders to be investigated: 1 - males, 2 female or 3 - both';
COMMENT ON COLUMN rif40_results.direct_standardisation IS 'Standardisation: indirect (0) or direct (1)';
COMMENT ON COLUMN rif40_results.adjusted IS 'Covariate adjustment: Unadjusted (0) or adjusted (1)';
COMMENT ON COLUMN rif40_results.observed IS 'The number of observed cases';
COMMENT ON COLUMN rif40_results.expected IS 'The number of expected cases or the rate (for direct standardised results)';
COMMENT ON COLUMN rif40_results.lower95 IS 'The lower 95% confidence interval for the relative risk (for indirectly standarised results) or the lower 95% confidence interval for the rate (for directly standardised results)';
COMMENT ON COLUMN rif40_results.upper95 IS 'The upper 95% confidence interval for the relative risk (for indirectly standarised results) or the upper 95% confidence interval for the rate (for directly standardised results)';
COMMENT ON COLUMN rif40_results.relative_risk IS 'Relaitive risk (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.smoothed_relative_risk IS 'Smoothed relaive risk (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.posterior_probability IS 'The posterior probability (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.posterior_probability_upper95 IS 'The upper 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.posterior_probability_lower95 IS 'The lower 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.residual_relative_risk IS 'The residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.residual_rr_lower95 IS 'The lower 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.residual_rr_upper95 IS 'The upper 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.smoothed_smr IS 'The smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.smoothed_smr_lower95 IS 'The lower 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN rif40_results.smoothed_smr_upper95 IS 'The upper 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)';
*/

