/*
 * SQL statement name: 	geolevels_areaid_update.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:
 *						1: Geolevels table; e.g. geolevels_cb_2014_us_500k
 *						2: Geometry table geometry_cb_2014_us_500k
 *						3: Schema; e.g. rif_data. or ""
 * 						4: Geography
 *
 * Description:			Update areaid_count column in geolevels table using geometry table
 * Note:				%% becomes % after substitution
 */
WITH b AS (
	SELECT geolevel_id, COUNT(DISTINCT(areaid)) AS areaid_count
	  FROM %3%2
	 GROUP BY geolevel_id
)
UPDATE a
   SET areaid_count = b.areaid_count
  FROM %1 a
  JOIN b ON a.geolevel_id = b.geolevel_id
 WHERE a.geography = '%4'