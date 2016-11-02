/*
 * SQL statement name: 	partition_trigger.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Create partitioned tables insert trigger
 * Note:				%%%% becomes %% after substitution
 */
 CREATE TRIGGER insert_%1_trigger
    BEFORE INSERT ON %1
    FOR EACH ROW EXECUTE PROCEDURE %1_insert_trigger()