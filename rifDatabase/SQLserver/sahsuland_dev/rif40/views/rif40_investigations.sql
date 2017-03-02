
--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_investigations]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_investigations]
END
GO

--table definition
CREATE VIEW [rif40].[rif40_investigations] AS 
 SELECT c.username,
    c.inv_id,
    c.study_id,
    c.inv_name,
    c.year_start,
    c.year_stop,
    c.max_age_group,
    c.min_age_group,
    c.genders,
    c.numer_tab,
    c.mh_test_type,
    c.inv_description,
    c.classifier,
    c.classifier_bands,
    c.investigation_state
   FROM [rif40].[t_rif40_investigations] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
 --permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_investigations] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_investigations] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Details of each investigation in a study' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of investigation. Must be a valid database column name, i.e. only contain A-Z0-9_ and start with a letter.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'inv_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year investigation is to start. Must be between the limnits specified in the numerator and denominator tables' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'year_start'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year investigation is to stop' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'year_stop'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maximum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &gt; MIN_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'max_age_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Minimum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &lt; MAX_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'min_age_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Genders to be investigated: 1 - males, 2 female or 3 - both' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'genders'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'numer_tab'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Mantel-Haenszel test type: &quot;No test&quot;, &quot;Comparison Areas&quot;, &quot;Unexposed Area&quot;.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'mh_test_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description of investigation' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'inv_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maps classifier. EQUAL_INTERVAL: each classifier band represents the same sized range and intervals change based on max an min, JENKS: Jenks natural breaks, QUANTILE: equiheight (even number) distribution, STANDARD_DEVIATION, UNIQUE_INTERVAL: a version of EQUAL_INTERVAL that takes into account unique values, &lt;BESPOKE&gt;; default QUANTILE. &lt;BESPOKE&gt; classification bands are defined in: RIF40_CLASSFIER_BANDS, RIF40_CLASSFIER_BAND_NAMES and are used to create maps that are comparable accross investigations' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'classifier'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Map classifier bands; default 5. Must be between 2 and 20' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'classifier_bands'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Investigation state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'investigation_state'
GO
