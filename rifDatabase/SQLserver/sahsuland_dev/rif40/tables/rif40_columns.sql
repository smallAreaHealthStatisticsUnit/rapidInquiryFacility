
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_columns]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_columns]
END
GO


CREATE TABLE [rif40].[rif40_columns](
	[table_or_view_name_hide] [varchar](40), 
	[column_name_hide] [varchar] (40),
	[nullable] [varchar](8),
	[oracle_data_type] [varchar](189),
	[comments] [varchar](4000),
  CONSTRAINT rif40_columns_pk PRIMARY KEY (table_or_view_name_hide, column_name_hide),
  CONSTRAINT table_or_view_name_hide_fk FOREIGN KEY ([table_or_view_name_hide])
      REFERENCES [rif40].[rif40_tables_and_views] ([table_or_view_name_hide])
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT nullable_ck CHECK (nullable = 'NOT NULL' OR nullable = 'NULL')
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_columns] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_columns] TO [rif_manager]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF40 Columns' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_columns'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_columns', @level2type=N'COLUMN',@level2name=N'table_or_view_name_hide'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_columns', @level2type=N'COLUMN',@level2name=N'column_name_hide'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Nullable', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_columns', @level2type=N'COLUMN',@level2name=N'nullable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Oracle data type', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_columns', @level2type=N'COLUMN',@level2name=N'oracle_data_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Comments', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_columns', @level2type=N'COLUMN',@level2name=N'comments'
GO
