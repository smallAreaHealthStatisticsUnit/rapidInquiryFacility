DECLARE @CurrentUser sysname /*
 * SQL statement name: 	comment_column.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * 						SchemaName is set to either @CurrentUser (build) or 'rif_data' for rif40
 * Description:			Comment table
 * Note:				%%%% becomes %% after substitution
 */
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty
@name = N'MS_Description',   
@value = N'%3', 
@level0type = N'Schema', @level0name = $(SchemaName),  
@level1type = N'Table', @level1name = '%1',
@level2type = N'Column', @level2name = '%2'