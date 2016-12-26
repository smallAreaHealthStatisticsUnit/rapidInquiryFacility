/*
 * SQL statement name: 	update_geometry.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: srid; e.g. 4326
 *
 * Description:			Add column to table
 * Note:				%% becomes % after substitution
 */
UPDATE %1
   SET geom = geography::STGeomFromText(wkt, %2),