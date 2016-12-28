/*
 * SQL statement name: 	create_covariate_table.sql
 * Type:				Postgres/PostGIS SQL statement
 * Parameters:
 *						1: covariate_table; e.g. COV_CB_2014_US_STATE_500K
 *						2: Geolevel name: CB_2014_US_STATE_500K
 *
 * Description:			Create example covariate table if it does not exist
 * Note:				%%%% becomes %% after substitution
 */
DO $$ 
    BEGIN
        BEGIN
             CREATE TABLE %1 (
				year 	INTEGER 	NOT NULL,
				%2		VARCHAR(30)	NOT NULL,
				PRIMARY KEY (year, %2)
			 );
        EXCEPTION
            WHEN duplicate_table THEN RAISE NOTICE 'Table %1 already exists.';
        END;
    END;
$$ 
