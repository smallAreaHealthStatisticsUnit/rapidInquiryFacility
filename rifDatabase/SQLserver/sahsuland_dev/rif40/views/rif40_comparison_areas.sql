USE [sahsuland_dev]
GO

--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_comparison_areas]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_comparison_areas]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_comparison_areas] AS 
 SELECT c.username,
    c.study_id,
    c.area_id
  FROM [rif40].[t_rif40_comparison_areas] c
  LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username= SUSER_SNAME() 
  WHERE c.username = SUSER_SNAME()  OR
   IS_MEMBER(N'[rif_manager]') = 1  OR 
   (s.grantee_username IS NOT NULL AND s.grantee_username<> '')
 GO

 --permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_comparison_areas] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON  [rif40].[rif40_comparison_areas] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Links study areas and bands for a given study.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_comparison_areas'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_comparison_areas', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_comparison_areas', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_comparison_areas', @level2type=N'COLUMN',@level2name=N'area_id'
GO
