/*
 * SQL statement name: 	not_null.sql
 * Type:				MS SQL Server SQL
 * Parameters:
 *						1: Tile limits table; e.g. tile_limits_cb_2014_us_500k
 *						2: not null column; e.g. zoomlevel
 *
 * Description:			Make column not null
 * Note:				%% becomes % after substitution
 */
ALTER TABLE %1 ALTER COLUMN %2 INTEGER NOT NULL