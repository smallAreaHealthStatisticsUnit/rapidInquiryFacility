USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_comparison_areas]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_comparison_areas]
END
GO

CREATE TABLE [rif40].[t_rif40_comparison_areas](
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[study_id] [numeric](8, 0) NOT NULL,
	[area_id] [varchar](300) NOT NULL,
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid()),
 CONSTRAINT [t_rif40_comparison_areas_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[area_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_compareas_study_id_fk] FOREIGN KEY([study_id])
	REFERENCES [rif40].[t_rif40_studies] ([study_id])
	 ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_comparison_areas] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_comparison_areas] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_comparison_areas
  IS 'Links comparison areas and bands for a given study.';
COMMENT ON COLUMN t_rif40_comparison_areas.username IS 'Username';
COMMENT ON COLUMN t_rif40_comparison_areas.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_comparison_areas.area_id IS 'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE';
*/

CREATE INDEX t_rif40_comp_areas_uname
  ON [rif40].[t_rif40_comparison_areas](username)
GO

--triggers NOT WRITTEN