/*
 * SQL statement name: 	create_lookup_table.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: table; e.g. lookup_sahsu_grd_level1
 * 						2: Lookup column - shapefile table name, e.g. sahsu_grd_level1
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create lookup table
 * Note:				%%%% becomes %% after substitution
 */
CREATE TABLE %3%1 (
	%2			VARCHAR(100)  NOT NULL,
	areaname	VARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		JSON,
	population_weighted_centroid		JSON,
	PRIMARY KEY (%2)
)