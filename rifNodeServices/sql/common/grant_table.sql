/*
 * SQL statement name: 	grant_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table/view; e.g. tiles_cb_2014_us_county_500k
 *						2: Privileges; e.g. SELECT
 *						3: Roles; e.g. rif_user, rif_manager
 *
 * Description:			Create tiles view
 * Note:				%%%% becomes %% after substitution
 */
GRANT %2 ON %1 TO %3