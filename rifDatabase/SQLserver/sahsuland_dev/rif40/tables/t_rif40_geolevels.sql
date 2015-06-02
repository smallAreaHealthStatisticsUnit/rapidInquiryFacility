USE [sahsuland_dev]
GO

--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_geolevels]') AND type in (N'U'))
BEGIN
	--drop foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[rif40_covariates]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_covariates_geolevel_fk')
	BEGIN
		ALTER TABLE [rif40].[rif40_covariates] DROP CONSTRAINT [rif40_covariates_geolevel_fk];
	END;
	
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariates]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_inv_cov_geolevel_fk')
	BEGIN	
		ALTER TABLE [rif40].[t_rif40_inv_covariates] DROP CONSTRAINT [t_rif40_inv_cov_geolevel_fk];
	END;
	
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_std_comp_geolevel_fk')
	BEGIN	
		ALTER TABLE [rif40].[t_rif40_studies] DROP CONSTRAINT [t_rif40_std_comp_geolevel_fk];
	END;
	
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_std_study_geolevel_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_studies] DROP CONSTRAINT [t_rif40_std_study_geolevel_fk];
	END;
		
	DROP TABLE [rif40].[t_rif40_geolevels]
END
GO

--table definition
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
	[listing] [numeric](1, 0) NOT NULL,
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

--recreate foreign key references
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[rif40_covariates]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[rif40_covariates]  WITH CHECK ADD  
	CONSTRAINT [rif40_covariates_geolevel_fk] FOREIGN KEY([geography], [geolevel_name])
	REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END;
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariates]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_inv_covariates]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_inv_cov_geolevel_fk] FOREIGN KEY([geography], [study_geolevel_name])
	REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END;
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))		
BEGIN
	ALTER TABLE [rif40].[t_rif40_studies]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_std_study_geolevel_fk] FOREIGN KEY([geography], [study_geolevel_name])
	REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
	ALTER TABLE [rif40].[t_rif40_studies]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_std_comp_geolevel_fk] FOREIGN KEY([geography], [comparison_geolevel_name])
	REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END;

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON  [rif40].[t_rif40_geolevels] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[t_rif40_geolevels] TO public
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geolevels: hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels are restricted to LADUA/DISTRICT level resolution or lower.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography (e.g EW2001)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of geolevel. This will be a column name in the numerator/denominator tables', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'geolevel_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'ID for ordering (1=lowest resolution). Up to 99 supported.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'geolevel_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is &quot;Westminster&quot;', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'lookup_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table description column name.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'lookup_desc_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table centroid X co-ordinate column name. Can also use CENTROIDSFILE instead.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroidxcoordinate_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table centroid Y co-ordinate column name.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroidycoordinate_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Location of the GIS centroids file. Can also use CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN instead.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroidsfile'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table containing GIS shape file data (created using shp2pgsql).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column containing the AREA_IDs in SHAPEFILE_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile_area_id_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column containing the AREA_ID descriptions in SHAPEFILE_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile_desc_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table containing GIS shape file data with Arc GIS calculated population weighted centroids (created using shp2pgsql). PostGIS does not support population weighted centroids.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroids_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column containing the AREA_IDs in CENTROIDS_TABLE. X and Y co-ordinates ciolumns are asummed to be named after CENTROIDXCOORDINATE_COLUMN and CENTROIDYCOORDINATE_COLUMN.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroids_area_id_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Average number of points in a geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'avg_npoints_geom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Average number of points in a ST_SimplifyPreserveTopology() optimsed geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'avg_npoints_opt'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'File length estimate (in bytes) for conversion of the entire geolevel geometry to GeoJSON. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'file_geojson_len'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The average length (in projection units - usually metres) of a vector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'leg_geom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The average length (in projection units - usually metres) of a ST_SimplifyPreserveTopology() optimsed geometryvector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'leg_opt'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of table used for covariates at this geolevel', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'covariate_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is geolevel access rectricted by Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. This is enforced by the RIF application.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'restricted'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Can use a map for selection at this resolution (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'resolution'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Able to be used as a comparison area (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'comparea'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Able to be used in a disease map listing (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_geolevels', @level2type=N'COLUMN',@level2name=N'listing'
GO

--index
CREATE UNIQUE INDEX [t_rif40_geolevels_uk2]
  ON [rif40].[t_rif40_geolevels] (geography)
GO

