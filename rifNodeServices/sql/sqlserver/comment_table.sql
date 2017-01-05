DECLARE @CurrentUser sysname
DECLARE @columnName  sysname 
DECLARE @tableName   sysname  /*
 * SQL statement name: 	comment_table.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: table; e.g. cb_2014_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * 						SchemaName is set to either @CurrentUser (build) or 'rif_data' for rif40
 *
 * Description:			Comment table
 * Note:				%%%% becomes %% after substitution
 */
SELECT @CurrentUser = user_name(); 
SELECT @tableName  = '$(SchemaName)';
IF (@tableName = '@CurrentUser')
	SELECT @tableName = @CurrentUser + '.%1'
ELSE
	SELECT @tableName = '$(SchemaName).%1';
IF EXISTS (
        SELECT class_desc
          FROM SYS.EXTENDED_PROPERTIES
		 WHERE [major_id] = OBJECT_ID(@tableName)
           AND [name]     = N'MS_Description'
		   AND [minor_id] = 0)
    EXECUTE sp_updateextendedproperty
		@name = N'MS_Description',   
		@value = N'%2', 
		@level0type = N'Schema', @level0name = $(SchemaName),  
		@level1type = N'Table', @level1name = '%1'
ELSE
	EXECUTE sp_addextendedproperty
		@name = N'MS_Description',   
		@value = N'%2', 
		@level0type = N'Schema', @level0name = $(SchemaName),  
		@level1type = N'Table', @level1name = '%1'