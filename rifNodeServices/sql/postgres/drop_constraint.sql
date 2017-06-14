/*
 * SQL statement name: 	drop_constraint.sql
 * Type:				Postgres SQL statement
 * Parameters:
 *						1: Table; e.g. rif40_covariates
 *						2: Constraint; e.g. rif40_covariates_geolevel_fk
 *
 * Description:			Drop a constraint
 * Note:				%% becomes % after substitution
 */
ALTER TABLE %1 DROP CONSTRAINT IF EXISTS %2