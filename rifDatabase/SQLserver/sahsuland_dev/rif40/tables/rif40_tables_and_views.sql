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
	[table_or_view_name_hide] [varchar](40),
	[comments] [varchar](4000),
 CONSTRAINT table_or_view_name_hide_pk PRIMARY KEY ([table_or_view_name_hide]),
  CONSTRAINT class_ck CHECK ([class] in ('Configuration', 'Documentation', 'Lookup', 'Other', 'Results', 'SQL Generator', 'Study Setup'))
)ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_tables_and_views] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_tables_and_views] TO [rif_manager]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF40 Tables and Views' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables_and_views'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Class', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables_and_views', @level2type=N'COLUMN',@level2name=N'class'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables_and_views', @level2type=N'COLUMN',@level2name=N'table_or_view'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table name (web version)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables_and_views', @level2type=N'COLUMN',@level2name=N'table_or_view_name_hide'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Comments', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_tables_and_views', @level2type=N'COLUMN',@level2name=N'comments'
GO

