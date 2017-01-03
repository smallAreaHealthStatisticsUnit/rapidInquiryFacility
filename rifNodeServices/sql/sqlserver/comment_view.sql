DECLARE @CurrentUser sysname /*
 * SQL statement name: 	comment_view.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: view; e.g. tiles_cb_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * 						SchemaName is set to either @CurrentUser (build) or 'rif_data' for rif40
 *
 * Description:			Comment view
 * Note:				%%%% becomes %% after substitution
 */
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty
@name = N'MS_Description',   
@value = N'%2', 
@level0type = N'Schema', @level0name = $(SchemaName),  
@level1type = N'View', @level1name = '%1'   