USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariates]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_inv_covariates]
END
GO

CREATE TABLE [rif40].[t_rif40_inv_covariates](
	[inv_id] [numeric](8, 0) NOT NULL,
	[study_id] [numeric](8, 0) NOT NULL,
	[covariate_name] [varchar](30) NOT NULL,
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[geography] [varchar](50) NOT NULL,
	[study_geolevel_name] [varchar](30) NULL,
	[min] [numeric](9, 3) NOT NULL,
	[max] [numeric](9, 3) NOT NULL,
 CONSTRAINT [t_rif40_inv_covariates_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[inv_id] ASC,
	[covariate_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_inv_covariates_si_fk] FOREIGN KEY([study_id], [inv_id])
	REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_inv_cov_cov_name_fk] FOREIGN KEY ([geography], [study_geolevel_name], [covariate_name])
    REFERENCES [rif40].[rif40_covariates] ([geography], [geolevel_name], [covariate_name])
    ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_inv_cov_geography_fk] FOREIGN KEY ([geography])
    REFERENCES [rif40].[rif40_geographies] ([geography]) 
    ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_inv_cov_geolevel_fk] FOREIGN KEY ([geography], [study_geolevel_name])
    REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
    ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_inv_covariates] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_inv_covariates] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_inv_covariates
  IS 'Details of each covariate used by an investigation in a study';
COMMENT ON COLUMN t_rif40_inv_covariates.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN t_rif40_inv_covariates.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_inv_covariates.covariate_name IS 'Covariate name';
COMMENT ON COLUMN t_rif40_inv_covariates.username IS 'Username';
COMMENT ON COLUMN t_rif40_inv_covariates.geography IS 'Geography (e.g EW2001). Cannot be changed by the user; present to allow a foreign key to be enforced.';
COMMENT ON COLUMN t_rif40_inv_covariates.study_geolevel_name IS 'Study area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS. Cannot be changed by the user; present to allow a foreign key to be enforced.';
COMMENT ON COLUMN t_rif40_inv_covariates.min IS 'Minimum value for a covariate';
COMMENT ON COLUMN t_rif40_inv_covariates.max IS 'Maximum value for a covariate';
*/

--triggers

