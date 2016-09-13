DECLARE @CurrentUser sysname /*
 * SQL statement name: 	comment_column.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%%%% becomes %% after substitution
 */
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty 'MS_Description',   
   '%3',
   'user', @CurrentUser,   
   'table', '%1',
   'column', '%2