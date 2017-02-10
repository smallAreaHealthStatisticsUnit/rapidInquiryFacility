/*
 * SQL statement name: 	rif40_geolevels_view.sql
 * Type:				Postgres/PostGIS SQL
 * Parameters:			NONE
 *
 * Description:			Recreate rif40_geolevels
 * Note:				% becomes % after substitution
 */
CREATE OR REPLACE VIEW rif40_geolevels AS 
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
    a.centroidycoordinate_column,
	a.areaid_count
   FROM t_rif40_geolevels a
  WHERE sys_context('SAHSU_CONTEXT'::character varying, 'RIF_STUDENT'::character varying)::text = 'YES'::text 
    AND a.restricted <> 1
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
    a.centroidycoordinate_column,
	a.areaid_count
   FROM t_rif40_geolevels a
  WHERE sys_context('SAHSU_CONTEXT'::character varying, 'RIF_STUDENT'::character varying) IS NULL 
     OR sys_context('SAHSU_CONTEXT'::character varying, 'RIF_STUDENT'::character varying)::text = 'NO'::text
  ORDER BY 1, 3 DESC;

GRANT SELECT ON TABLE rif40_geolevels TO rif_user, rif_manager;
COMMENT ON VIEW rif40_geolevels
  IS 'Geolevels: hierarchy of level with a geography. Use this table for SELECT; use T_RIF40_GEOLEVELS for INSERT/UPDATE/DELETE. View with RIF_STUDENT security context support. If the user has the RIF_STUDENT role the geolevels are restricted to e.g. LADUA/DISTRICT level resolution or lower. This is controlled by the RESTRICTED field.';
COMMENT ON COLUMN rif40_geolevels.geography IS 'Geography (e.g EW2001)';
COMMENT ON COLUMN rif40_geolevels.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';
COMMENT ON COLUMN rif40_geolevels.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';
COMMENT ON COLUMN rif40_geolevels.description IS 'Description';
COMMENT ON COLUMN rif40_geolevels.lookup_table IS 'Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is &quot;Westminster&quot;';
COMMENT ON COLUMN rif40_geolevels.lookup_desc_column IS 'Lookup table description column name.';
COMMENT ON COLUMN rif40_geolevels.shapefile IS 'Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead,';
COMMENT ON COLUMN rif40_geolevels.centroidsfile IS 'Location of the GIS centroids file. Can also use CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN instead.';
COMMENT ON COLUMN rif40_geolevels.shapefile_table IS 'Table containing GIS shape file data (created using shp2pgsql).';
COMMENT ON COLUMN rif40_geolevels.shapefile_area_id_column IS 'Column containing the AREA_IDs in SHAPEFILE_TABLE';
COMMENT ON COLUMN rif40_geolevels.shapefile_desc_column IS 'Column containing the AREA_ID descriptions in SHAPEFILE_TABLE';
COMMENT ON COLUMN rif40_geolevels.centroids_table IS 'Table containing GIS shape file data with Arc GIS calculated population weighted centroids (created using shp2pgsql). PostGIS does not support population weighted centroids.';
COMMENT ON COLUMN rif40_geolevels.centroids_area_id_column IS 'Column containing the AREA_IDs in CENTROIDS_TABLE. X and Y co-ordinates ciolumns are asummed to be named after CENTROIDXCOORDINATE_COLUMN and CENTROIDYCOORDINATE_COLUMN.';
COMMENT ON COLUMN rif40_geolevels.avg_npoints_geom IS 'Average number of points in a geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT ON COLUMN rif40_geolevels.avg_npoints_opt IS 'Average number of points in a ST_SimplifyPreserveTopology() optimsed geometry object (AREA_ID). Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT ON COLUMN rif40_geolevels.file_geojson_len IS 'File length estimate (in bytes) for conversion of the entire geolevel geometry to GeoJSON. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT ON COLUMN rif40_geolevels.leg_geom IS 'The average length (in projection units - usually metres) of a vector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT ON COLUMN rif40_geolevels.leg_opt IS 'The average length (in projection units - usually metres) of a ST_SimplifyPreserveTopology() optimsed geometryvector leg. Used to evaluation the impact of ST_SIMPLIFY_TOLERANCE.';
COMMENT ON COLUMN rif40_geolevels.covariate_table IS 'Name of table used for covariates at this geolevel';
COMMENT ON COLUMN rif40_geolevels.resolution IS 'Can use a map for selection at this resolution (0/1)';
COMMENT ON COLUMN rif40_geolevels.comparea IS 'Able to be used as a comparison area (0/1)';
COMMENT ON COLUMN rif40_geolevels.listing IS 'Able to be used in a disease map listing (0/1)';
COMMENT ON COLUMN rif40_geolevels.restricted IS 'Is geolevel access rectricted by Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. This is enforced by the RIF application.';
COMMENT ON COLUMN rif40_geolevels.centroidxcoordinate_column IS 'Lookup table centroid X co-ordinate column name. Can also use CENTROIDSFILE instead.';
COMMENT ON COLUMN rif40_geolevels.centroidycoordinate_column IS 'Lookup table centroid Y co-ordinate column name.';
COMMENT ON COLUMN rif40_geolevels.areaid_count IS 'Area ID count'

