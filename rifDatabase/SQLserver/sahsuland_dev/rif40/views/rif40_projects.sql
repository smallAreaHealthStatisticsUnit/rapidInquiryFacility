
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_projects]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_projects]
END
GO


CREATE VIEW [rif40].[rif40_projects] AS 
 SELECT a.project,
    a.description,
    a.date_started,
    a.date_ended
   FROM [rif40].[t_rif40_projects] a
  WHERE a.project IN ( SELECT a.project
           FROM [rif40].[t_rif40_user_projects] b
          WHERE b.username=SUSER_SNAME() OR 
			IS_MEMBER(N'[rif_manager]') = 1 )
GO

GRANT SELECT ON [rif40].[rif40_projects] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_projects] TO [rif_manager]
GO


EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF projects (restricted to those granted to the user)' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_projects'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Project name' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_projects', @level2type=N'COLUMN',@level2name=N'project'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Project description' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_projects', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project started' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_projects', @level2type=N'COLUMN',@level2name=N'date_started'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date project ended' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_projects', @level2type=N'COLUMN',@level2name=N'date_ended'
GO
