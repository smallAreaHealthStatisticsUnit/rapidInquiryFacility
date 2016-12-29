/*
 * SQL statement name: 	create_lookup_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. t_tiles_cb_2014_us_county_500k
 * 						2: Lookup column - shapefile table name
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create tiles table
 * Note:				%%%% becomes %% after substitution
 */
CREATE TABLE %3%1 (
	%2			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	PRIMARY KEY (%2)
)