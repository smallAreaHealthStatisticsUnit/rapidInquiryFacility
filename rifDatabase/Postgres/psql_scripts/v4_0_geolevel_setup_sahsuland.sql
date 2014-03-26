--
-- Setup SAHSLAND into rif40_geographies, rif40_geolevels ready for setup by rif40_geo_pkg functions
--

--
-- RIF40_GEOGRAPHIES, T_RIF40_GEOLEVELS
--
UPDATE rif40_geographies 
   SET defaultcomparea = NULL
 WHERE geography = 'SAHSU';
DELETE FROM t_rif40_geolevels WHERE geography = 'SAHSU';
DELETE FROM rif40_geographies WHERE geography = 'SAHSU';

INSERT INTO rif40_geographies (geography, description, hierarchytable, srid)
VALUES ('SAHSU', 'SAHSU example database', 'SAHSULAND_GEOGRAPHY', 27700);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	centroidxcoordinate_column, centroidycoordinate_column,
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance)
VALUES (
	'SAHSU', 'LEVEL1', 1, 'Lowest level of resolution', 'SAHSULAND_LEVEL1', 'NAME',
	NULL, NULL,
	NULL, /* 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_GRD_Level1.shp' */ NULL, NULL, 1,1,1,
	'X_SAHSU_LEVEL1', 'LEVEL1', NULL, 500);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	centroidxcoordinate_column, centroidycoordinate_column,
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance)
VALUES (
	'SAHSU', 'LEVEL2', 2, '2nd level of resolution', 'SAHSULAND_LEVEL2', 'NAME',
	NULL, NULL,
	NULL, /* 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_GRD_Level2.shp' */ NULL, NULL, 1,1,1,
	'X_SAHSU_LEVEL2', 'LEVEL2', 'NAME', 100);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	centroidxcoordinate_column, centroidycoordinate_column,
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance)
VALUES (
	'SAHSU', 'LEVEL3', 3, '3rd level of resolution', 'SAHSULAND_LEVEL3', 'NAME',
	NULL, NULL,
	'SAHSULAND_COVARIATES_LEVEL3', /* 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_GRD_Level3.shp' */ NULL, NULL, 1,1,1,
	'X_SAHSU_LEVEL3', 'LEVEL3', NULL, 50);
INSERT INTO t_rif40_geolevels(geography, geolevel_name, geolevel_id, description, lookup_table, lookup_desc_column, 
	covariate_table, shapefile, centroidsfile, resolution, comparea, listing,
	shapefile_table, shapefile_area_id_column, shapefile_desc_column, st_simplify_tolerance, 
	centroidxcoordinate_column, centroidycoordinate_column, centroids_table)
VALUES (
	'SAHSU', 'LEVEL4', 4, 'Highest level of resolution', 'SAHSULAND_LEVEL4', 'NAME',
	'SAHSULAND_COVARIATES_LEVEL4', /* 'C:\PROGRAM Files\RIF\RIF Shapefiles\SAHSU_GRD_Level4.shp', 'C:\Program Files\RIF\RIF Shapefiles\SAHSU_CEN_Level4.shp' USING TABLE BASED CENTROIDS  */ NULL, NULL ,1,1,1,
	'X_SAHSU_LEVEL4', 'LEVEL4', NULL, 10, 'X_COORDINATE', 'Y_COORDINATE', 'X_SAHSU_CEN_LEVEL4');

UPDATE rif40_geographies 
   SET defaultcomparea  = 'LEVEL2',
       defaultstudyarea = 'LEVEL4'
 WHERE geography = 'SAHSU';

--
-- Eof
