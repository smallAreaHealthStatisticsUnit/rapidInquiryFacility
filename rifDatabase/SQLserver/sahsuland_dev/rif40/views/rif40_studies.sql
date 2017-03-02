
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_studies]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_studies]
END
GO

CREATE VIEW [rif40].[rif40_studies] AS 
 SELECT c.username,
    c.study_id,
    c.extract_table,
    c.study_name,
    c.summary,
    c.description,
    c.other_notes,
    c.study_date,
    c.geography,
    c.study_type,
    c.study_state,
    c.comparison_geolevel_name,
    c.denom_tab,
    c.direct_stand_tab,
    i.year_start,
    i.year_stop,
    i.max_age_group,
    i.min_age_group,
    c.study_geolevel_name,
    c.map_table,
    c.suppression_value,
    c.extract_permitted,
    c.transfer_permitted,
    c.authorised_by,
    c.authorised_on,
    c.authorised_notes,
    c.audsid,
	0 AS partition_parallelisation,  --does this apply to SQL Server?
    l.covariate_table,
    c.project,
    pj.description AS project_description
   FROM [rif40].[t_rif40_studies] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
     LEFT JOIN ( SELECT i2.study_id,
            max(i2.year_stop) AS year_stop,
            min(i2.year_start) AS year_start,
            max(i2.max_age_group) AS max_age_group,
            min(i2.min_age_group) AS min_age_group
           FROM [rif40].[t_rif40_investigations] i2
          GROUP BY i2.study_id) i ON c.study_id = i.study_id
     LEFT JOIN [rif40].[rif40_geographies] g ON c.geography = g.geography
     LEFT JOIN [rif40].[t_rif40_geolevels] l ON c.geography = l.geography AND c.study_geolevel_name = l.geolevel_name
     LEFT JOIN [rif40].[t_rif40_projects] pj ON pj.project = c.project
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
GO 
 
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_studies] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_studies] TO [rif_manager]
GO


EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF studies' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Extract table. Must only contain A-Z0-9_ and start with a letter.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'extract_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study name' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'study_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study summary' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'summary'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study description' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study other notes' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'other_notes'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study date' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'study_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography (e.g EW2001)' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study type: 1 - disease mapping, 11 - Risk Analysis (many areas, one band), 12 - Risk Analysis (point sources), 13 - Risk Analysis (exposure covariates), 14 - Risk Analysis (coverage shapefile), 15 - Risk Analysis (exposure shapefile)' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'study_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'study_state'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Comparison area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'comparison_geolevel_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF. Note the old RIF allowed studies to have different denominators between investigations; this capability has been removed to simplify the extract SQL and to allow a single rotated high performance extract table to be used for all investigations in a study. The extract table is then based on the standard rotated denominator (i.e. in age_sex_group, total format rather than M0 .. M5-9 ... etc) with one extract column per covariate and investigation. Multiple investigations may use a different numerator (1 per investigation).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'denom_tab'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of table to be used in direct standardisation. COMPARISON_GEOLEVEL_NAME must be NULL. May be &quot;DUMMY&quot; if extract created outside of the RIF. Note the old RIF allowed studies to have different denominators between investigations; this capability has been removed to simplify the extract SQL and to allow a single rotated high performance extract table to be used for all investigations in a study. The extract table is then based on the standard rotated denominator (i.e. in age_sex_group, total format rather than M0 ... M5-9 ... etc) with one extract column per covariate and investigation. Multiple investigations may use a different numerator (1 per investigation).', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'direct_stand_tab'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'year_start'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'year_stop'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'max_age_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'min_age_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Study area geolevel name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'study_geolevel_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Map table. Must only contain A-Z0-9_ and start with a letter.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'map_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter &quot;SuppressionValue&quot;. If >0 all results with the value or below will be set to 0.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'suppression_value'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create and run a RIF study and to view the results. Geolevel access is restricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any extract if the system parameter ExtractControl=1. This is enforced by the RIF application.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'extract_permitted'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a RIF MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be transferred.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'transfer_permitted'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Who authorised extract and/or transfer. Must be a RIF MANAGER.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'authorised_by'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'When was the extract and/or transfer authorised', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'authorised_on'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'authorised_notes'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Link to Oracle audit subsystem.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'audsid'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'partition_parallelisation'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'covariate_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Project running the study. The user must be allocated to the project.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'project'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'N/A', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_studies', @level2type=N'COLUMN',@level2name=N'project_description'
GO

