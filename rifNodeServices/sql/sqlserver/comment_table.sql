DECLARE @CurrentUser sysname /*
 * SQL statement name: 	comment_table.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%%%% becomes %% after substitution
 */
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty 'MS_Description',   
   '%2',
   'user', @CurrentUser,   
   'table', '%1'