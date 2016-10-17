COMMENT /*
 * SQL statement name: 	comment_view.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: view; e.g. tiles_cb_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment view
 * Note:				%%%% becomes %% after substitution
 */
	ON VIEW %1 IS '%2'