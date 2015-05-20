USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_covariates]') AND type in (N'U'))
BEGIN
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

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_covariates] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[rif40_covariates] TO [rif_user]
GO

/*
COMMENT ON TABLE rif40_covariates
  IS 'RIF multiple covariates';
COMMENT ON COLUMN rif40_covariates.geography IS 'Geography (e.g EW2001)';
COMMENT ON COLUMN rif40_covariates.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';
COMMENT ON COLUMN rif40_covariates.covariate_name IS 'Covariate name. This will be a column name in RIF40_GEOLEVELS.COVARIATE_TABLE';
COMMENT ON COLUMN rif40_covariates.min IS 'Minimum value';
COMMENT ON COLUMN rif40_covariates.max IS 'Maximum value';
COMMENT ON COLUMN rif40_covariates.type IS 'TYPE of covariate (1=integer score/2=continuous variable). Min &lt; max  max/min precison is appropriate to type. Continuous variables are not currently supported. Integer scores can be a binary variable 0/1 or an NTILE e.g. 1..5 for a quintile.';
*/
-- + trigger
--EXEC rif40_covariates_trigger.sql