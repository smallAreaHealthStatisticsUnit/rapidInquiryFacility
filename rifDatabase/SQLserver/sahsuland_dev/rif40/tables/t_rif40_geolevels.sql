USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_geolevels]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_geolevels]
END
GO

CREATE TABLE [rif40].[t_rif40_geolevels](
	[geography] [varchar](50) NOT NULL,
	[geolevel_name] [varchar](30) NOT NULL,
	[geolevel_id] [numeric](2, 0) NOT NULL,
	[description] [varchar](250) NOT NULL,
	[lookup_table] [varchar](30) NOT NULL,
	[lookup_desc_column] [varchar](30) NOT NULL,
	[centroidxcoordinate_column] [varchar](30) NULL,
	[centroidycoordinate_column] [varchar](30) NULL,
	[shapefile] [varchar](512) NULL,
	[centroidsfile] [varchar](512) NULL,
	[shapefile_table] [varchar](30) NULL,
	[shapefile_area_id_column] [varchar](30) NULL,
	[shapefile_desc_column] [varchar](30) NULL,
	[st_simplify_tolerance] [numeric](6, 0) NULL,
	[centroids_table] [varchar](30) NULL,
	[centroids_area_id_column] [varchar](30) NULL,
	[avg_npoints_geom] [numeric](12, 0) NULL,
	[avg_npoints_opt] [numeric](12, 0) NULL,
	[file_geojson_len] [numeric](12, 0) NULL,
	[leg_geom] [numeric](12, 1) NULL,
	[leg_opt] [numeric](12, 1) NULL,
	[covariate_table] [varchar](30) NULL,
	[restricted] [numeric](1, 0) NOT NULL DEFAULT ((0)),
	[resolution] [numeric](1, 0) NOT NULL,
	[comparea] [numeric](1, 0) NOT NULL,
	[LISTING] [numeric](1, 0) NOT NULL,
	[id] [int] IDENTITY(1,1) NOT NULL,
 CONSTRAINT [t_rif40_geolevels_pk] PRIMARY KEY CLUSTERED 
(
	[geography] ASC,
	[geolevel_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_geolevels_geog_fk] FOREIGN KEY([geography])
	REFERENCES [rif40].[rif40_geographies] ([geography])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_geol_comparea_ck] CHECK  (([comparea]=(1) OR [comparea]=(0))),
CONSTRAINT [t_rif40_geol_listing_ck] CHECK  (([listing]=(1) OR [listing]=(0))),
CONSTRAINT [t_rif40_geol_resolution_ck] CHECK  (([resolution]=(1) OR [resolution]=(0))),
CONSTRAINT [t_rif40_geol_restricted_ck] CHECK  (([restricted]=(1) OR [restricted]=(0)))
) ON [PRIMARY]
GO

CREATE UNIQUE INDEX [t_rif40_geolevels_uk2]
  ON [rif40].[t_rif40_geolevels] (geography)
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON  [rif40].[t_rif40_geolevels] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[t_rif40_geolevels] TO public
GO

--trigger