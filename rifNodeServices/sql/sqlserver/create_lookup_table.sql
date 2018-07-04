/*
 * SQL statement name: 	create_lookup_table.sql
 * Type:				Microsoft SQL Server T/sql
 * Parameters:
 *						1: table; e.g. lookup_sahsu_grd_level1
 * 						2: Lookup column - shapefile table name, e.g. sahsu_grd_level1
 *						3: Schema; e.g. rif_data. or ""
 *
 * Description:			Create lookup table
 * Note:				%%%% becomes %% after substitution
 */
CREATE TABLE %3%1 (
	%2			NVARCHAR(100)  NOT NULL,
	areaname	NVARCHAR(1000),
	gid			INTEGER		  NOT NULL,
	geographic_centroid		VARCHAR(1000),
	population_weighted_centroid		VARCHAR(1000),
	PRIMARY KEY (%2)
)