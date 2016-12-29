/*
 * SQL statement name: 	create_tiles_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. t_tiles_cb_2014_us_county_500k
 *						2: JSON datatype (Postgres JSON, SQL server Text)
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create tiles table
 * Note:				%%%% becomes %% after substitution
 */
CREATE TABLE %3%1 (
	geolevel_id			INTEGER			NOT NULL,
	zoomlevel			INTEGER			NOT NULL,
	x					INTEGER			NOT NULL, 
	y					INTEGER			NOT NULL,
	optimised_geojson	%2,
	optimised_topojson	%2,
	tile_id				VARCHAR(200)	NOT NULL,
	areaid_count		INTEGER			NOT NULL,
	PRIMARY KEY (tile_id))