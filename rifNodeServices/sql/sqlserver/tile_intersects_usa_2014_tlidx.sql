/*
 * SQL statement name: 	tile_intersects_usa_2014_tlidx.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: tile intersects table; e.g. tile_intersects_usa_2014
 *
 * Description:			Special index on tile intersects table for MS SQL tuning.
 *						SQL server is very inefficent otherwise
 * Note:				%% becomes % after substitution
 */
CREATE NONCLUSTERED INDEX %1_tlidx
ON %1 ([geolevel_id],[zoomlevel],[x],[y])
INCLUDE ([areaid],[geom])