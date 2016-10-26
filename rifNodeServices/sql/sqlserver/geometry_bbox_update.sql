/*
 * SQL statement name: 	geometry_bbox_update.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:
 *						1: Geometry table geometry_cb_2014_us_500k
 *
 * Description:			Update bbox column in geometry table using STEnvelope
 * Note:				%% becomes % after substitution
 */

UPDATE %1
   SET bbox = geom.STEnvelope()
 WHERE bbox IS NULL 