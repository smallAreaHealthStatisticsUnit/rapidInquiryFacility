USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_results]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_results]
END
GO

CREATE TABLE [rif40].[t_rif40_results](
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[study_id] [numeric](8, 0) NOT NULL,
	[inv_id] [numeric](8, 0) NOT NULL,
	[band_id] [numeric](8, 0) NOT NULL,
	[genders] [numeric](1, 0) NOT NULL,
	[adjusted] [numeric](1, 0) NOT NULL,
	[direct_standardisation] [numeric](1, 0) NOT NULL,
	[observed] [numeric](38, 6) NULL,
	[expected] [numeric](38, 6) NULL,
	[lower95] [numeric](38, 6) NULL,
	[upper95] [numeric](38, 6) NULL,
	[relative_risk] [numeric](38, 6) NULL,
	[smoothed_relative_risk] [numeric](38, 6) NULL,
	[posterior_probability] [numeric](38, 6) NULL,
	[posterior_probability_lower95] [numeric](38, 6) NULL,
	[posterior_probability_upper95] [numeric](38, 6) NULL,
	[residual_relative_risk] [numeric](38, 6) NULL,
	[residual_rr_lower95] [numeric](38, 6) NULL,
	[residual_rr_upper95] [numeric](38, 6) NULL,
	[smoothed_smr] [numeric](38, 6) NULL,
	[smoothed_smr_lower95] [numeric](38, 6) NULL,
	[smoothed_smr_upper95] [numeric](38, 6) NULL,
 CONSTRAINT [t_rif40_results_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[band_id] ASC,
	[inv_id] ASC,
	[genders] ASC,
	[adjusted] ASC,
	[direct_standardisation] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_results_study_id_fk] FOREIGN KEY([study_id], [inv_id])
	REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_res_adjusted_ck] CHECK  
	(([adjusted]>=(0) AND [adjusted]<=(1))),
CONSTRAINT [t_rif40_res_dir_stand_ck] CHECK  
	(([direct_standardisation]>=(0) AND [direct_standardisation]<=(1))),
CONSTRAINT [t_rif40_results_genders_ck] CHECK  
	(([genders]>=(1) AND [genders]<=(3)))	
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_results] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_results] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_results
  IS 'RIF Results Table';
COMMENT ON COLUMN t_rif40_results.username IS 'Username';
COMMENT ON COLUMN t_rif40_results.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_results.inv_id IS 'Unique investigation inde:inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN t_rif40_results.band_id IS 'A band allocated to the area';
COMMENT ON COLUMN t_rif40_results.genders IS 'Genders to be investigated: 1 - males, 2 female or 3 - both';
COMMENT ON COLUMN t_rif40_results.adjusted IS 'Covariate adjustment: Unadjusted (0) or adjusted (1)';
COMMENT ON COLUMN t_rif40_results.direct_standardisation IS 'Standardisation: indirect (0) or direct (1)';
COMMENT ON COLUMN t_rif40_results.observed IS 'The number of observed cases';
COMMENT ON COLUMN t_rif40_results.expected IS 'The number of expected cases or the rate (for direct standardised results)';
COMMENT ON COLUMN t_rif40_results.lower95 IS 'The lower 95% confidence interval for the relative risk (for indirectly standarised results) or the lower 95% confidence interval for the rate (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.upper95 IS 'The upper 95% confidence interval for the relative risk (for indirectly standarised results) or the upper 95% confidence interval for the rate (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.relative_risk IS 'Relaitive risk (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.smoothed_relative_risk IS 'Smoothed relaive risk (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.posterior_probability IS 'The posterior probability (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.posterior_probability_lower95 IS 'The lower 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.posterior_probability_upper95 IS 'The upper 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.residual_relative_risk IS 'The residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.residual_rr_lower95 IS 'The lower 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.residual_rr_upper95 IS 'The upper 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.smoothed_smr IS 'The smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.smoothed_smr_lower95 IS 'The lower 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)';
COMMENT ON COLUMN t_rif40_results.smoothed_smr_upper95 IS 'The upper 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)';
*/

CREATE INDEX t_rif40_results_band_id_bm
  ON [rif40].[t_rif40_results] (band_id)
GO

CREATE INDEX t_rif40_results_inv_id_fk
  ON [rif40].[t_rif40_results] (inv_id)
GO
  
CREATE INDEX t_rif40_results_username_bm
  ON [rif40].[t_rif40_results] (username)
GO

--trigger
