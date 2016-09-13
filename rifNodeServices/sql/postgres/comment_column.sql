COMMENT /*
 * SQL statement name: 	comment_column.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%%%% becomes %% after substitution
 */
	ON COLUMN %1.%2 IS '%3'