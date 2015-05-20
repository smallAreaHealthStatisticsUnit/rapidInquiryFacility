USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_geographies]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_geographies]
END
GO

CREATE TABLE [rif40].[rif40_geographies](
	[geography] [varchar](50) NOT NULL,
	[description] [varchar](250) NOT NULL,
	[hierarchytable] [varchar](30) NOT NULL,
	[srid] [numeric](6, 0) NOT NULL DEFAULT (0),
	[defaultcomparea] [varchar](30) NULL,
	[defaultstudyarea] [varchar](30) NULL,
	[postal_population_table] [varchar](30) NULL,
	[postal_point_column] [varchar](30) NULL,
	[partition] [numeric](1, 0) NOT NULL DEFAULT (0),
	[max_geojson_digits] [numeric](2, 0) NULL DEFAULT (8),
CONSTRAINT [rif40_geographies_pk] PRIMARY KEY CLUSTERED ( [geography] ASC ),
CONSTRAINT [postal_population_table_ck] CHECK  (([postal_population_table] IS NOT NULL AND [postal_point_column] IS NOT NULL 
OR [postal_population_table] IS NULL AND [postal_point_column] IS NULL))
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_geographies] TO [rif_manager]
GO
GRANT SELECT, REFERENCES ON [rif40].[rif40_geographies] TO [public]
GO

