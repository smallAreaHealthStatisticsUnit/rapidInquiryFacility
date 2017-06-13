/*
 * SQL statement name: 	enable_disable_constraint.sql
 * Type:				MS SQL Server SQL statement
 * Parameters:
 *						1: Schema; e.g. rif40. or ""
 *						2: Table; e.g. rif40_covariates
 *						3: NOCHECK/CHECK (disable/enable)
 *						4: Contraint; e.g. rif40_covariates_geolevel_fk
 *
 * Description:			Enable or disablee a constraint
 * Note:				%% becomes % after substitution
 */
ALTER TABLE %1%2 %3 CONSTRAINT %4