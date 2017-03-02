/*
removed foreign key temporarily
*/


--drop if already exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_areas]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_study_areas]
END
GO

--table definition

CREATE TABLE [rif40].[t_rif40_study_areas](
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[study_id] [integer] NOT NULL CONSTRAINT t_rif40_study_area_study_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[area_id] [varchar](300) NOT NULL,
	[band_id] [numeric](8, 0) NULL,
 CONSTRAINT [t_rif40_study_areas_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[area_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_studyareas_study_id_fk] FOREIGN KEY ([study_id])
      REFERENCES [rif40].[t_rif40_studies] ([study_id])
      ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_areas] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_areas] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Links study areas and bands for a given study.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_areas'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_areas', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_areas', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_areas', @level2type=N'COLUMN',@level2name=N'area_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'A band allocated to the area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_areas', @level2type=N'COLUMN',@level2name=N'band_id'
GO

--indices
CREATE INDEX t_rif40_study_areas_band_id
  ON [rif40].[t_rif40_study_areas](band_id)
GO
CREATE INDEX t_rif40_study_areas_uname
  ON [rif40].[t_rif40_study_areas](username)
GO

