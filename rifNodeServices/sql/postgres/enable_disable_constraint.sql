/*
 * SQL statement name: 	enable_disable_constraint.sql
 * Type:				Postgres SQL statement
 * Parameters:
 *						1: Schema; e.g. rif40. or ""
 *						2: Table; e.g. rif40_covariates
 *						3: DISABLE/ENABLE
 *						4: Contraint; e.g. rif40_covariates_geolevel_fk
 *
 * Description:			Enable or disablee a constraint
 * Note:				%% becomes % after substitution
 *						THIS IS NOT VALID POSTGRES SQL - DROP AND RECREATE THE CONSTRAINT
 */
ALTER TABLE %1%2 %3 CONSTRAINT %4