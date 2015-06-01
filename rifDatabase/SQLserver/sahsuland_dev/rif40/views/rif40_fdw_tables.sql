USE [sahsuland_dev]
GO

--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_fdw_tables]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_fdw_tables]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_fdw_tables] AS 
SELECT t_rif40_fdw_tables.username,
    t_rif40_fdw_tables.table_name,
    t_rif40_fdw_tables.create_status,
    t_rif40_fdw_tables.error_message,
    t_rif40_fdw_tables.date_created,
    t_rif40_fdw_tables.rowtest_passed
   FROM [rif40].[t_rif40_fdw_tables]
  WHERE t_rif40_fdw_tables.username = SUSER_SNAME()
 GO

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_fdw_tables] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_fdw_tables] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF numerator tables which are Foreign data wrappers in SQL Server' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_fdw_tables'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF table name. Normally the schema owner will not be able to see the health data tables, so no error is raised if the table cannot be resolved to an acceisble object. The schema owner must have access to automatic indirect standardisation denominators.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'table_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'create_status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Error message when create status is: E(Created, errors in test SELECT, N(Not created, errors)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'error_message'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date FDW table created (or attempted to be)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'date_created'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SELECT rowtest passed (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'rowtest_passed'
GO
