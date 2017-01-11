/*
 * SQL statement name: 	add_geometry_column2.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_cb_2014_us_500k
 *						2: column name; e.g. geom
 *						3: Column SRID; e.g. 4326 [NEVER USED IN SQL SERVER, set during WKT conversion]
 *						4: Spatial geometry type: e.g. POINT, MULTIPOLYGON [NEVER USED IN SQL SERVER]
 *                      5: Schema (rif_data. or "")
 *
 * Description:			Add *** geometry *** column to table
 * Note:				%%%% becomes %% after substitution
 */
ALTER TABLE %5%1 ADD %2 geometry