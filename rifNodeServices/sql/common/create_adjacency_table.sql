/*
 * SQL statement name: 	create_adjacency_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: adjacency table; e.g. adjacency_cb_2014_us_500k
 *						2: schema; e.g.rif_data. or ""
 *
 * Description:			Create adjacency table
 * Note:				%% becomes % after substitution
 */
CREATE TABLE %2%1 (
	geolevel_id		INTEGER			NOT NULL,
	areaid			VARCHAR(200)	NOT NULL,
	num_adjacencies INTEGER			NOT NULL,
	adjacency_list	VARCHAR(8000)	NOT NULL,
	CONSTRAINT %1_pk PRIMARY KEY (geolevel_id, areaid)
)