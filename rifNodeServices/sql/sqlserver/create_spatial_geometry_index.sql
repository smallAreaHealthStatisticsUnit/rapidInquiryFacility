/*
 * SQL statement name: 	create_spatial_geometry_index.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:
 *						1: index name;e.g. geometry_cb_2014_us_500k_gix
 *						2: table name; e.g. geometry_cb_2014_us_500k
 *						3: Geometry field name; e.g. geom
 *						4: Xmin (4326); e.g. -179.13729006727 
 *						5: Ymin (4326); e.g. -14.3737802873213 
 *						6: Xmax (4326); e.g.  179.773803959804  
 *						7: Ymax (4326); e.g. 71.352561 
 *
 * Description:			Create geometry table
 * Note:				%% becomes % after substitution
 */
CREATE SPATIAL INDEX %1 ON %2 (%3)
	WITH ( BOUNDING_BOX = (xmin=%4 ymin=%5, xmax=%6, ymax=%7))