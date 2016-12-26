/*
 * SQL statement name: 	add_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: Table name; e.g. geometry_usa_2014
 *						2: column name; e.g. wkt
 *						3: Column datatype; e.g. Text or VARCHAR(MAX)
 *
 * Description:			Add column to table if it does not exist
 * Note:				%% becomes % after substitution
 */
 DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE %1 ADD %2 %3;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column %2 already exists in %1.';
        END;
    END;
$$