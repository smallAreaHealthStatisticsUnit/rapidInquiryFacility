
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_reference_tables]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_reference_tables]
END
GO

--table definition
CREATE TABLE [rif40].[rif40_reference_tables](
	[table_name] [varchar](30) NOT NULL,
 CONSTRAINT [rif40_reference_tables_pk] PRIMARY KEY CLUSTERED 
(
	[table_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

--permissions
GRANT SELECT ON [rif40].[rif40_reference_tables] TO public
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'List of references tables without constraints' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_reference_tables'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_reference_tables', @level2type=N'COLUMN',@level2name=N'table_name'
GO
