COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%%%% becomes %% after substitution
 */
	ON COLUMN %1.%2 IS '%3'