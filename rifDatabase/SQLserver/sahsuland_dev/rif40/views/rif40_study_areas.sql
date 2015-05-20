USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_areas]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_study_areas]
END
GO

CREATE VIEW [rif40].[rif40_study_areas] AS 
SELECT c.username,
    c.study_id,
    c.area_id,
    c.band_id
   FROM [rif40].[t_rif40_study_areas] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_areas] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_areas] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_study_areas
  IS 'Links study areas and bands for a given study.';
COMMENT ON COLUMN rif40_study_areas.username IS 'Username';
COMMENT ON COLUMN rif40_study_areas.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_study_areas.area_id IS 'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE';
COMMENT ON COLUMN rif40_study_areas.band_id IS 'A band allocated to the area';
*/
