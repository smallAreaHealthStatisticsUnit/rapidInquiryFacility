USE [sahsuland_dev]
GO

--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_contextual_stats]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_contextual_stats]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_contextual_stats] AS 
 SELECT c.username,
    c.study_id,
    c.inv_id,
    c.area_id,
    c.area_population,
    c.area_observed,
    c.total_comparision_population,
    c.variance_high,
    c.variance_low
   FROM [rif40].[t_rif40_contextual_stats] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND  s.grantee_username= SUSER_SNAME() 
   WHERE c.username = SUSER_SNAME()  OR 
   IS_MEMBER(N'[rif_manager]') = 1 OR 
   (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
GO

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_contextual_stats] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_contextual_stats] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Contextual stats for results map. Also includes values used in internal calculations.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'area_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total population in area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'area_population'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total observed in area', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'area_observed'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total comparison population. Used for internal calculations.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'total_comparision_population'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Variance (observed &gt; 100). Used for internal calculations.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'variance_high'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Variance (observed &lt;= 100). Used for internal calculations.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_contextual_stats', @level2type=N'COLUMN',@level2name=N'variance_low'
GO
