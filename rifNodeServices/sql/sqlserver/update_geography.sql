/*
 * SQL statement name: 	update_geography.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: srid; e.g. 4326
 *
 * Description:			Update geometry column in table
 * Note:				%% becomes % after substitution
 */
UPDATE %1
   SET geom = Geography::STGeomFromText(wkt, %2)