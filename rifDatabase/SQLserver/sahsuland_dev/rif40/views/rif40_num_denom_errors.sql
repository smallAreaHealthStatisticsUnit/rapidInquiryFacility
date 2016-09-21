--30/6/2015: Create view script fails unless create view is run separately from create comments on view.  

USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_num_denom_errors]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_num_denom_errors]
END
GO

CREATE VIEW [rif40].[rif40_num_denom_errors] AS 
 WITH n AS (
         SELECT n1.geography,
            n1.numerator_table,
            n1.numerator_description,
            n1.automatic,
            n1.is_object_resolvable,
            n1.n_num_denom_validated,
            n1.numerator_owner
           FROM ( SELECT g.geography,
                    n_1.table_name AS numerator_table,
                    n_1.description AS numerator_description,
                    n_1.automatic,
                    [rif40].[rif40_is_object_resolvable](n_1.table_name) AS is_object_resolvable,
                    [rif40].[rif40_num_denom_validate](g.geography, n_1.table_name) AS n_num_denom_validated,
                    [rif40].[rif40_object_resolve](n_1.table_name) AS numerator_owner
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] n_1
                  WHERE n_1.isnumerator = 1 AND n_1.automatic = 1) n1
        ), d AS (
         SELECT d1.geography,
            d1.denominator_table,
            d1.denominator_description,
            d1.is_object_resolvable,
            d1.d_num_denom_validated,
            d1.denominator_owner,
            [rif40].[rif40_auto_indirect_checks](d1.denominator_table) AS auto_indirect_error
           FROM ( SELECT g.geography,
                    d_1.table_name AS denominator_table,
                    d_1.description AS denominator_description,
                    [rif40].[rif40_is_object_resolvable](d_1.table_name) AS is_object_resolvable,
                    [rif40].[rif40_num_denom_validate](g.geography, d_1.table_name) AS d_num_denom_validated,
                    [rif40].[rif40_object_resolve](d_1.table_name) AS denominator_owner
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] d_1
                  WHERE d_1.isindirectdenominator = 1 AND d_1.automatic = 1) d1
        )
 SELECT n.geography,
    n.numerator_owner,
    n.numerator_table,
    n.is_object_resolvable AS is_numerator_resolvable,
    n.n_num_denom_validated,
    n.numerator_description,
    d.denominator_owner,
    d.denominator_table,
    d.is_object_resolvable AS is_denominator_resolvable,
    d.d_num_denom_validated,
    d.denominator_description,
    n.automatic,
        CASE
            WHEN d.auto_indirect_error IS NULL THEN 0
            ELSE 1
        END AS auto_indirect_error_flag,
    d.auto_indirect_error /*,
    f.create_status AS n_fdw_create_status,
    f.error_message AS n_fdw_error_message,
    f.date_created AS n_fdw_date_created,
    f.rowtest_passed AS n_fdw_rowtest_passed */
   FROM d,
    n
  WHERE n.geography = d.geography;
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'All possible numerator and indirect standardisation denominator pairs with error diagnostic fields. As this is a CROSS JOIN the will be a lot of output as tables are not rejected on the basis of user access or containing the correct geography geolevel fields.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table owner' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'numerator_owner'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'numerator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is the numerator table resolvable and accessible (0/1)' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'is_numerator_resolvable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is the numerator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'n_num_denom_validated'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'numerator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table owner', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'denominator_owner'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'denominator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is the denominator table resolvable and accessible (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'is_denominator_resolvable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is the denominator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'd_num_denom_validated'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'denominator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'automatic'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Error flag 0/1. Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'auto_indirect_error_flag'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator. List of geographies and tables in error.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'auto_indirect_error'
GO
/*
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF numerator foreign data wrappers table create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'n_fdw_create_status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF numerator foreign data wrappers table error message when create status is: E(Created, errors in test SELECT, N(Not created, errors).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'n_fdw_error_message'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF numerator foreign data wrappers table date FDW table created (or attempted to be).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'n_fdw_date_created'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF numerator foreign data wrappers table SELECT rowtest passed (0/1).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', @level2type=N'COLUMN',@level2name=N'n_fdw_rowtest_passed'
GO
 */
