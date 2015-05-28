/*
needs functions:
	rif40_is_object_resolvable, OK
	rif40_num_denom_validate, OK
	rif40_auto_indirect_checks
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_num_denom]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_num_denom]
END
GO


CREATE VIEW [rif40].[rif40_num_denom] AS 
 WITH n AS (
         SELECT n1.geography,
            n1.numerator_table,
            n1.numerator_description,
            n1.automatic,
            n1.theme_description
           FROM ( SELECT g.geography,
                    n_1.table_name AS numerator_table,
                    n_1.description AS numerator_description,
                    n_1.automatic,
                    t.description AS theme_description
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] n_1,
                    [rif40].[rif40_health_study_themes] t
                  WHERE n_1.isnumerator = 1 AND n_1.automatic = 1 AND [rif40].[rif40_is_object_resolvable](n_1.table_name) = 1 AND n_1.theme = t.theme) n1
          WHERE [rif40].[rif40_num_denom_validate](n1.geography, n1.numerator_table) = 1
        ), d AS (
         SELECT d1.geography,
            d1.denominator_table,
            d1.denominator_description
           FROM ( SELECT g.geography,
                    d_1.table_name AS denominator_table,
                    d_1.description AS denominator_description
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] d_1
                  WHERE d_1.isindirectdenominator = 1 AND d_1.automatic = 1 AND [rif40].[rif40_is_object_resolvable](d_1.table_name) = 1) d1
          WHERE [rif40].[rif40_num_denom_validate](d1.geography, d1.denominator_table) = 1 AND [rif40].[rif40_auto_indirect_checks](d1.denominator_table) IS NULL
        )
 SELECT n.geography,
    n.numerator_table,
    n.numerator_description,
    n.theme_description,
    d.denominator_table,
    d.denominator_description,
    n.automatic
   FROM n,
    d
  WHERE n.geography = d.geography
GO


EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator and indirect standardisation denominator pairs. Use RIF40_NUM_DENOM_ERROR if your numerator and denominator table pair is missing. You must have your own copy of RIF40_NUM_DENOM or you will only see the tables RIF40 has access to. Tables not rejected if the user does not have access or the table does not contain the correct geography geolevel fields.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom', @level2type=N'COLUMN',@level2name=N'numerator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table description' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom', @level2type=N'COLUMN',@level2name=N'numerator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table health study theme description' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom', @level2type=N'COLUMN',@level2name=N'theme_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom', @level2type=N'COLUMN',@level2name=N'denominator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table description' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom', @level2type=N'COLUMN',@level2name=N'denominator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_num_denom', @level2type=N'COLUMN',@level2name=N'automatic'
GO

