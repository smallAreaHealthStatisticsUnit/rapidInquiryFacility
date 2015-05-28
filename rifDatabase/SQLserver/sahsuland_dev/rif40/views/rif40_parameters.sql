USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_parameters]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_parameters]
END
GO

CREATE VIEW [rif40].[rif40_parameters] AS 
 SELECT t_rif40_parameters.param_name,
    t_rif40_parameters.param_value,
    t_rif40_parameters.param_description as description
   FROM [rif40].[t_rif40_parameters]
  WHERE t_rif40_parameters.param_name <> 'SuppressionValue'
UNION
 SELECT p.param_name,
        CASE
            WHEN IS_MEMBER(N'[RIF_NO_SUPPRESSION]') = 1 THEN '0'
            ELSE p.param_value
        END AS param_value,
    p.param_description
   FROM [rif40].[t_rif40_parameters] p
  WHERE p.param_name = 'SuppressionValue'
UNION
 SELECT 'RifParametersTable' AS param_name,
    'Virtual' AS param_value,
    'Is this the T_RIF40_PARAMETERS table or the VIRTUAL view' AS param_description
GO

GRANT SELECT, UPDATE, INSERT ON [rif40].[rif40_parameters] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[rif40_parameters] TO [rif_user]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF40 parameters with automatic SuppressionValue parameter.  Use this table for SELECT; use T_RIF40_PARAMETERS for INSERT/UPDATE/DELETE. User needs RIF_NO_SUPPRESSION granted as a role to see unsuppressed results' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_parameters'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Parameter', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_parameters', @level2type=N'COLUMN',@level2name=N'param_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Value', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_parameters', @level2type=N'COLUMN',@level2name=N'param_value'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_parameters', @level2type=N'COLUMN',@level2name=N'description'
GO
