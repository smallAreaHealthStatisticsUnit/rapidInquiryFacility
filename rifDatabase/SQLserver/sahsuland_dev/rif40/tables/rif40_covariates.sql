USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_covariates]') AND type in (N'U'))
BEGIN

	--first disable foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariates]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_inv_cov_cov_name_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_inv_covariates] DROP CONSTRAINT [t_rif40_inv_cov_cov_name_fk];
	END;

	DROP TABLE [rif40].[rif40_covariates]
END
GO


CREATE TABLE [rif40].[rif40_covariates](
	[geography] [varchar](50) NOT NULL,
	[geolevel_name] [varchar](30) NOT NULL,
	[covariate_name] [varchar](30) NOT NULL,
	[min] [numeric](9, 3) NOT NULL,
	[max] [numeric](9, 3) NOT NULL,
	[type] [numeric](9, 3) NOT NULL,
	[id] [int] IDENTITY(1,1) NOT NULL,
 CONSTRAINT [rif40_covariates_pk] PRIMARY KEY CLUSTERED 
(
	[geography] ASC,
	[geolevel_name] ASC,
	[covariate_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_covariates_geog_fk] FOREIGN KEY([geography])
	REFERENCES [rif40].[rif40_geographies] ([geography])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [rif40_covariates_geolevel_fk] FOREIGN KEY([geography], [geolevel_name])
	REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [rif40_covariates_listing_ck] CHECK  (([type]=(2) OR [type]=(1)))
) ON [PRIMARY]
GO

--recreate foreign key references
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariates]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_inv_covariates]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_inv_cov_cov_name_fk] FOREIGN KEY([geography], [study_geolevel_name], [covariate_name])
	REFERENCES [rif40].[rif40_covariates] ([geography], [geolevel_name], [covariate_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_covariates] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[rif40_covariates] TO [rif_user]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF multiple covariates' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_covariates'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography (e.g EW2001)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_covariates', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of geolevel. This will be a column name in the numerator/denominator tables', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_covariates', @level2type=N'COLUMN',@level2name=N'geolevel_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_covariates', @level2type=N'COLUMN',@level2name=N'covariate_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Minimum value', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_covariates', @level2type=N'COLUMN',@level2name=N'min'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maximum value', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_covariates', @level2type=N'COLUMN',@level2name=N'max'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'TYPE of covariate (1=integer score/2=continuous variable). Min &lt; max  max/min precison is appropriate to type. Continuous variables are not currently supported. Integer scores can be a binary variable 0/1 or an NTILE e.g. 1..5 for a quintile.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_covariates', @level2type=N'COLUMN',@level2name=N'type'
GO

