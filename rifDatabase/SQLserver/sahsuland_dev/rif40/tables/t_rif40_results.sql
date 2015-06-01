USE [sahsuland_dev]
GO

--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_results]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_results]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_results](
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[inv_id] [integer] NOT NULL DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_inv_id_seq')),
	[study_id] [integer] NOT NULL DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
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

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_results] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_results] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF Results Table' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation inde:inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'A band allocated to the area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'band_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Genders to be investigated: 1 - males, 2 female or 3 - both', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'genders'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Covariate adjustment: Unadjusted (0) or adjusted (1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'adjusted'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Standardisation: indirect (0) or direct (1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'direct_standardisation'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The number of observed cases', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'observed'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The number of expected cases or the rate (for direct standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'expected'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval for the relative risk (for indirectly standarised results) or the lower 95% confidence interval for the rate (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval for the relative risk (for indirectly standarised results) or the upper 95% confidence interval for the rate (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'upper95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Relative risk (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'relative_risk'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Smoothed relative risk (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_relative_risk'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The posterior probability (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'posterior_probability'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'posterior_probability_lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval of the posterior probability (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'posterior_probability_upper95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'residual_relative_risk'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'residual_rr_lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval of the residual relative risk(for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'residual_rr_upper95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_smr'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The lower 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_smr_lower95'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The upper 95% confidence interval of the smoothed SMR [fully Bayesian smoothing] (for indirectly standarised results) or NULL (for directly standardised results)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_results', @level2type=N'COLUMN',@level2name=N'smoothed_smr_upper95'
GO

--indices
CREATE INDEX t_rif40_results_band_id_bm
  ON [rif40].[t_rif40_results] (band_id)
GO

CREATE INDEX t_rif40_results_inv_id_fk
  ON [rif40].[t_rif40_results] (inv_id)
GO
  
CREATE INDEX t_rif40_results_username_bm
  ON [rif40].[t_rif40_results] (username)
GO
