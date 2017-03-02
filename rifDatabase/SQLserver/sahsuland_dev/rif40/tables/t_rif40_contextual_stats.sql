
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_contextual_stats]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_contextual_stats]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_contextual_stats](
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[study_id] [integer] NOT NULL CONSTRAINT t_rif40_contextual_stat_study_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[inv_id] [integer] NOT NULL CONSTRAINT t_rif40_contextual_stat_inv_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_inv_id_seq')),
	[area_id]  [integer] NOT NULL,
	[area_population] [numeric](38, 6) NULL,
	[area_observed] [numeric](38, 6) NULL,
	[total_comparison_population] [numeric](38, 6) NULL,
	[variance_high] [numeric](38, 6) NULL,
	[variance_low] [numeric](38, 6) NULL,
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

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_contextual_stats] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_contextual_stats] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Contextual stats for results map. Also includes values used in internal calculations.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'area_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total population in area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'area_population'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total observed in area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'area_observed'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total comparison population. Used for internal calculations.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'total_comparison_population'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Variance (observed &gt; 100). Used for internal calculations.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'variance_high'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Variance (observed &lt;= 100). Used for internal calculations.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'variance_low'
GO

--permissions
CREATE INDEX t_rif40_constats_inv_id_fk
  ON [rif40].[t_rif40_contextual_stats](inv_id)
GO

CREATE INDEX t_rif40_constats_uname_bm
  ON [rif40].[t_rif40_contextual_stats](username)
GO
