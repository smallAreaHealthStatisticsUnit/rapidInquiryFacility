/*
 * SQL statement name: 	comment_partition_trigger.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: geometry table; e.g. geometry_cb_2014_us_500k
 *
 * Description:			Comment create partitioned tables insert trigger
 * Note:				%%%% becomes %% after substitution
 */
 COMMENT ON TRIGGER insert_%1_trigger ON %1 IS 'Partitioned tables insert trigger'