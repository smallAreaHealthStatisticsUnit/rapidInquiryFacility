USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_user_version]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_user_version]
END
GO


CREATE VIEW [rif40].[rif40_user_version] AS 
 SELECT '$Revision: 1.11 $' AS user_schema_revision
GO

GRANT SELECT ON [rif40].[rif40_user_version] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_user_version] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_user_version
  IS 'User schema revision control view.';
COMMENT ON COLUMN rif40_user_version.user_schema_revision IS 'Revision (derived from CVS).';
*/
