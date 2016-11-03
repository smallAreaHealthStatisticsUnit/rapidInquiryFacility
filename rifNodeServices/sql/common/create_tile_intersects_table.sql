/*
 * SQL statement name: 	create_tile_intersects_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. tile_intersects_cb_2014_us_500k
 *						2: JSON datatype (Postgres: JSON, MS SQL Server: Text)
 *						3: ST_Within()/STWithin() return datatype: bit (SQL Server: 0/1) or BOOLEAN (Postgres)
 *
 * Description:			Create tile intersects table
 * Note:				%% becomes % after substitution
 */
CREATE TABLE %1 (
	geolevel_id				INTEGER			NOT NULL,
	zoomlevel				INTEGER			NOT NULL, 
	areaid					VARCHAR(200)	NOT NULL,
	x						INTEGER			NOT NULL, 
	y						INTEGER			NOT NULL, 
    optimised_geojson		%2,
	within					%3				NOT NULL,
	optimised_wkt			Text
)