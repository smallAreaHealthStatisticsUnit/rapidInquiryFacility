COMMENT /*
 * SQL statement name: 	comment_table.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%%%% becomes %% after substitution
 */
	ON TABLE %1 IS '%2'