USE [sahsuland_dev]
GO

--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_user_version]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_user_version]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_user_version] AS 
 SELECT '$Revision: 1.11 $' AS user_schema_revision
GO

--permissions
GRANT SELECT ON [rif40].[rif40_user_version] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_user_version] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'User schema revision control view.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_version'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Revision (derived from CVS).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_user_version', @level2type=N'COLUMN',@level2name=N'user_schema_revision'
GO
