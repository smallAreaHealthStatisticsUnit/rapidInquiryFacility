/*
sys_context('SAHSU_CONTEXT','RIF_STUDENT')='YES'/null,

I don't see a nice way to set parameters or system variables in SQL Server
(although I found a bad way: SQLCMD, http://blog.sqlauthority.com/2013/06/28/sql-server-how-to-set-variable-and-use-variable-in-sqlcmd-mode/)

Just create a rif_student role?  Or was that design choice already rejected for some reason?  I will ask Peter.
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_geolevels]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_geolevels]
END
GO


CREATE VIEW [rif40].[rif40_geolevels] AS 
 SELECT a.geography,
    a.geolevel_name,
    a.geolevel_id,
    a.description,
    a.lookup_table,
    a.lookup_desc_column,
    a.shapefile,
    a.centroidsfile,
    a.shapefile_table,
    a.shapefile_area_id_column,
    a.shapefile_desc_column,
    a.centroids_table,
    a.centroids_area_id_column,
    a.avg_npoints_geom,
    a.avg_npoints_opt,
    a.file_geojson_len,
    a.leg_geom,
    a.leg_opt,
    a.covariate_table,
    a.resolution,
    a.comparea,
    a.listing,
    a.restricted,
    a.centroidxcoordinate_column,
    a.centroidycoordinate_column
   FROM [rif40].[t_rif40_geolevels] a
  WHERE IS_MEMBER(N'[rif_student]') = 1 AND a.restricted <> 1
UNION
 SELECT a.geography,
    a.geolevel_name,
    a.geolevel_id,
    a.description,
    a.lookup_table,
    a.lookup_desc_column,
    a.shapefile,
    a.centroidsfile,
    a.shapefile_table,
    a.shapefile_area_id_column,
    a.shapefile_desc_column,
    a.centroids_table,
    a.centroids_area_id_column,
    a.avg_npoints_geom,
    a.avg_npoints_opt,
    a.file_geojson_len,
    a.leg_geom,
    a.leg_opt,
    a.covariate_table,
    a.resolution,
    a.comparea,
    a.listing,
    a.restricted,
    a.centroidxcoordinate_column,
    a.centroidycoordinate_column
   FROM [rif40].[t_rif40_geolevels] a
  WHERE IS_MEMBER(N'[rif_student]') IS NULL
GO

GRANT SELECT ON [rif40].[rif40_geolevels] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_geolevels] TO [rif_manager]
GO
  
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geolevels: hierarchy of level with a geography. Use this table for INSERT/UPDATE/DELETE; use RIF40_GEOLEVELS for SELECT. In RIF40_GEOLEVELS if the user has the RIF_STUDENT role the geolevels are restricted to LADUA/DISTRICT level resolution or lower.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography (e.g EW2001)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of geolevel. This will be a column name in the numerator/denominator tables', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'geolevel_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'ID for ordering (1=lowest resolution). Up to 99 supported.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'geolevel_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is &quot;Westminster&quot;', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'lookup_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table description column name.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'lookup_desc_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table centroid X co-ordinate column name. Can also use CENTROIDSFILE instead.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroidxcoordinate_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lookup table centroid Y co-ordinate column name.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroidycoordinate_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Location of the GIS centroids file. Can also use CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN instead.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroidsfile'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table containing GIS shape file data (created using shp2pgsql).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column containing the AREA_IDs in SHAPEFILE_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile_area_id_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column containing the AREA_ID descriptions in SHAPEFILE_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'shapefile_desc_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table containing GIS shape file data with Arc GIS calculated population weighted centroids (created using shp2pgsql). PostGIS does not support population weighted centroids.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroids_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column containing the AREA_IDs in CENTROIDS_TABLE. X and Y co-ordinates ciolumns are asummed to be named after CENTROIDXCOORDINATE_COLUMN and CENTROIDYCOORDINATE_COLUMN.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'centroids_area_id_column'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Average number of points in a geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'avg_npoints_geom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Average number of points in a ST_SimplifyPreserveTopology() optimsed geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'avg_npoints_opt'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'File length estimate (in bytes) for conversion of the entire geolevel geometry to GeoJSON. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'file_geojson_len'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The average length (in projection units - usually metres) of a vector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'leg_geom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'The average length (in projection units - usually metres) of a ST_SimplifyPreserveTopology() optimsed geometryvector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'leg_opt'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of table used for covariates at this geolevel', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'covariate_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is geolevel access rectricted by Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. This is enforced by the RIF application.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'restricted'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Can use a map for selection at this resolution (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'resolution'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Able to be used as a comparison area (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'comparea'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Able to be used in a disease map listing (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_geolevels', @level2type=N'COLUMN',@level2name=N'listing'
GO
