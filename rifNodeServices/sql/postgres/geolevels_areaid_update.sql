/*
 * SQL statement name: 	geolevels_areaid_update.sql
 * Type:				Postgres SQL statement
 * Parameters:
 *						1: Geolevels table; e.g. geolevels_cb_2014_us_500k
 *						2: Geometry table geometry_cb_2014_us_500k
 *						3: Schema; e.g. rif_data. or ""
 * 						4: Geography
 *
 * Description:			Update areaid_count column in geolevels table using geometry table
 * Note:				%% becomes % after substitution
 */
UPDATE %1 a
   SET areaid_count = (
			SELECT COUNT(DISTINCT(areaid)) AS areaid_count
			  FROM %2 b
			 WHERE a.geolevel_id = b.geolevel_id)
 WHERE geography = '%4'