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

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Pre-defined hierarchial geographies. Usually based on Census geography. Beware of the foreign key from RIF40_GEOGRAPHIES.DEFAULTCOMPAREA to RIF40_GEOLEVELS. When inserting a new geography do NOT set DEFAULTCOMPAREA, update it after adding geolevels to T_RIF40_GEOLEVELS' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Hierarchy table', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'hierarchytable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Postgres projection SRID', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'srid'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Default comparison area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'defaultcomparea'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Default study area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'defaultstudyarea'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Postal population table. Table of postal points (e.g. postcodes, ZIP codes); geolevels; X and YCOORDINATES (in projection SRID); male, female and total populations. Converted to SRID points by loader [not in 4326 Web Mercator lat/long]. Used in creating population wieght centroids and in converting postal points to geolevels. Expected columns &lt;postal_point_column&gt;, XCOORDINATE, YCOORDINATE, 1+ &lt;GEOLEVEL_NAME&gt;, MALES, FEMALES, TOTAL', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'postal_population_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column name for postal points (e.g. POSTCODE, ZIP_CODE)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'postal_point_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Enable partitioning. Extract tables will be partition if the number of years >= 2x the RIF40_PARAMETERS parameters Parallelisation [which has a default of 4, so extracts covering 8 years or more will be partitioned]. ', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'partition'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Max digits in ST_AsGeoJson() [optimises file size by removing unecessary precision, the default value of 8 is normally fine.]', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_geographies', @level2type=N'COLUMN',@level2name=N'max_geojson_digits'
GO
