/*
 * SQL statement name: 	create_constraint.sql
 * Type:				Postgres SQL statement
 * Parameters:
 *						1: Table; e.g. t_rif40_studies
 *						2: Constraint; e.g. t_rif40_std_comp_geolevel_fk
 *						3: Foreign key fields; e.g. geography, comparison_geolevel_name
 *						4: Referenced table and columns; e.g. t_rif40_geolevels (geography, geolevel_name)
 *
 * Description:			Create a constraint
 * Note:				%% becomes % after substitution
 *
 * ALTER TABLE t_rif40_studies
 *  	ADD CONSTRAINT t_rif40_std_comp_geolevel_fk FOREIGN KEY (geography, comparison_geolevel_name)
 *		REFERENCES t_rif40_geolevels (geography, geolevel_name);
 */
ALTER TABLE %1 ADD CONSTRAINT %2 
	FOREIGN KEY (%3) REFERENCES %4