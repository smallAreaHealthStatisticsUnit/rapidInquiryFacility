USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_reference_tables]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_reference_tables]
END
GO

CREATE TABLE [rif40].[rif40_reference_tables](
	[table_name] [varchar](30) NOT NULL,
 CONSTRAINT [rif40_reference_tables_pk] PRIMARY KEY CLUSTERED 
(
	[table_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_reference_tables] TO public
GO

/*
COMMENT ON TABLE rif40_reference_tables
  IS 'List of references tables without constraints';
COMMENT ON COLUMN rif40_reference_tables.table_name IS 'Table name';
*/