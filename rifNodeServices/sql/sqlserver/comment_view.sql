DECLARE @CurrentUser sysname /*
 * SQL statement name: 	comment_view.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: view; e.g. tiles_cb_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment view
 * Note:				%%%% becomes %% after substitution
 */
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty 'MS_Description',   
   '%2',
   'user', @CurrentUser,   
   'view', '%1'