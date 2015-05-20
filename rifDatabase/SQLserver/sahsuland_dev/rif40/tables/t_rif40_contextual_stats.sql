USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_contextual_stats]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_contextual_stats]
END
GO

CREATE TABLE [rif40].[t_rif40_contextual_stats](
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[study_id] [numeric](8, 0) NOT NULL,
	[inv_id] [numeric](8, 0) NOT NULL,
	[area_id] [numeric](8, 0) NOT NULL,
	[area_population] [numeric](38, 6) NULL,
	[area_observed] [numeric](38, 6) NULL,
	[total_comparision_population] [numeric](38, 6) NULL,
	[variance_high] [numeric](38, 6) NULL,
	[variance_low] [numeric](38, 6) NULL,
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid()),
 CONSTRAINT [t_rif40_contextual_stats_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[area_id] ASC,
	[inv_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_constats_study_id_fk] FOREIGN KEY([study_id], [inv_id])
	REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_contextual_stats] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_contextual_stats] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_contextual_stats
  IS 'Contextual stats for results map. Also includes values used in internal calculations.';
COMMENT ON COLUMN t_rif40_contextual_stats.username IS 'Username';
COMMENT ON COLUMN t_rif40_contextual_stats.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_contextual_stats.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN t_rif40_contextual_stats.area_id IS 'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE';
COMMENT ON COLUMN t_rif40_contextual_stats.area_population IS 'Total population in area';
COMMENT ON COLUMN t_rif40_contextual_stats.area_observed IS 'Total observed in area';
COMMENT ON COLUMN t_rif40_contextual_stats.total_comparision_population IS 'Total comparison population. Used for internal calculations.';
COMMENT ON COLUMN t_rif40_contextual_stats.variance_high IS 'Variance (observed &gt; 100). Used for internal calculations.';
COMMENT ON COLUMN t_rif40_contextual_stats.variance_low IS 'Variance (observed &lt;= 100). Used for internal calculations.';
*/

CREATE INDEX t_rif40_constats_inv_id_fk
  ON [rif40].[t_rif40_contextual_stats](inv_id)
GO

CREATE INDEX t_rif40_constats_uname_bm
  ON [rif40].[t_rif40_contextual_stats](username)
GO

--triggers