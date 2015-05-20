USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_tables_and_views]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_tables_and_views]
END
GO

CREATE TABLE [rif40].[rif40_tables_and_views](
	[class] [varchar](13),
	[table_or_view] [varchar](178),
	[table_or_view_name_href] [varchar](413),
	[table_or_view_name_hide] [varchar](30),
	[comments] [varchar](4000)
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_tables_and_views] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_tables_and_views] TO [rif_manager]
GO


/*
COMMENT ON TABLE rif40_tables_and_views
  IS 'RIF40 Tables and Views';
COMMENT ON COLUMN rif40_tables_and_views.class IS 'Class';
COMMENT ON COLUMN rif40_tables_and_views.table_or_view IS 'Table name';
COMMENT ON COLUMN rif40_tables_and_views.table_or_view_name_href IS 'Table name (web version)';
COMMENT ON COLUMN rif40_tables_and_views.table_or_view_name_hide IS 'Table name (web version)';
COMMENT ON COLUMN rif40_tables_and_views.comments IS 'Comments';
*/
