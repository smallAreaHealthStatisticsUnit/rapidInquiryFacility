/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. cb_2014_us_county_500k
 *						2: column name; e.g. geographic_centroid
 *						3: Column SRID; e.g. 4326
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON
 *                      5: Schema (rif_data. or "") [NEVER USED IN POSTGRES]
 *
 * Description:			Add geometry column to table
 * Note:				%%%% becomes %% after substitution
 */
SELECT AddGeometryColumn('%1','%2', %3, '%4', 
			2 		/* Dimension */, 
			false 	/* use typmod geometry column instead of constraint-based */)