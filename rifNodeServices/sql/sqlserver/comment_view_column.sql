DECLARE @CurrentUser sysname /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_500k
 *						2: column; e.g. geography
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
   'view', '%1',
   'column', '%2'