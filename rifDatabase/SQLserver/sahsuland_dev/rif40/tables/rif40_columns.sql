USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_columns]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_columns]
END
GO

CREATE TABLE [rif40].[rif40_columns](
	[table_or_view_name_hide] [varchar](30), 
	[column_name_hide] [varchar] (30),
	[table_or_view_name_href] [varchar] (522),
	[column_name_href] [varchar](847), 
	[nullable] [varchar](8),
	[oracle_data_type] [varchar](189),
	[comments] [varchar](4000)
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_columns] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_columns] TO [rif_manager]
GO

/*
COMMENT ON TABLE rif40_columns
  IS 'RIF40 Columns';
COMMENT ON COLUMN rif40_columns.table_or_view_name_hide IS 'Table name';
COMMENT ON COLUMN rif40_columns.column_name_hide IS 'Column name';
COMMENT ON COLUMN rif40_columns.table_or_view_name_href IS 'Table name (web version)';
COMMENT ON COLUMN rif40_columns.column_name_href IS 'Column name (web version)';
COMMENT ON COLUMN rif40_columns.nullable IS 'Nullable';
COMMENT ON COLUMN rif40_columns.oracle_data_type IS 'Oracle data type';
COMMENT ON COLUMN rif40_columns.comments IS 'Comments';
*/
