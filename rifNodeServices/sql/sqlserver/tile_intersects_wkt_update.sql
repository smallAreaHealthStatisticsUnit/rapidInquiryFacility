/*
 * SQL statement name: 	tile_intersects_wkt_update.sql
 * Type:				Microsoft SQL Server SQL
 * Parameters:
 *						1: table; e.g. tile_intersects_cb_2014_us_500k
 *
 * Description:			Update wellknown text column in tile intersects table from geometry
 * Note:				%% becomes % after substitution
 */
 UPDATE %1
    SET optimised_wkt = geom.STAsText()
  WHERE optimised_wkt IS NULL