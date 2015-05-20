USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_contextual_stats]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_contextual_stats]
END
GO


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
   IS_MEMBER(N'[rif40_manager]') = 1 OR 
   (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_contextual_stats] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_contextual_stats] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_contextual_stats
  IS 'Contextual stats for results map. Also includes values used in internal calculations.';
COMMENT ON COLUMN rif40_contextual_stats.username IS 'Username';
COMMENT ON COLUMN rif40_contextual_stats.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_contextual_stats.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40_contextual_stats.area_id IS 'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE';
COMMENT ON COLUMN rif40_contextual_stats.area_population IS 'Total population in area';
COMMENT ON COLUMN rif40_contextual_stats.area_observed IS 'Total observed in area';
COMMENT ON COLUMN rif40_contextual_stats.total_comparision_population IS 'Total comparison population. Used for internal calculations.';
COMMENT ON COLUMN rif40_contextual_stats.variance_high IS 'Variance (observed &gt; 100). Used for internal calculations.';
COMMENT ON COLUMN rif40_contextual_stats.variance_low IS 'Variance (observed &lt;= 100). Used for internal calculations.';
*/

--plus trigger