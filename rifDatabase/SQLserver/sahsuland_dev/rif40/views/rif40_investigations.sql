USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_investigations]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_investigations]
END
GO

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
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_investigations] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_investigations] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_investigations
  IS 'Details of each investigation in a study';
COMMENT ON COLUMN rif40_investigations.username IS 'Username';
COMMENT ON COLUMN rif40_investigations.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40_investigations.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_investigations.inv_name IS 'Name of investigation. Must be a valid database column name, i.e. only contain A-Z0-9_ and start with a letter.';
COMMENT ON COLUMN rif40_investigations.year_start IS 'Year investigation is to start. Must be between the limnits specified in the numerator and denominator tables';
COMMENT ON COLUMN rif40_investigations.year_stop IS 'Year investigation is to stop';
COMMENT ON COLUMN rif40_investigations.max_age_group IS 'Maximum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &gt; MIN_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME';
COMMENT ON COLUMN rif40_investigations.min_age_group IS 'Minimum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &lt; MAX_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME';
COMMENT ON COLUMN rif40_investigations.genders IS 'Genders to be investigated: 1 - males, 2 female or 3 - both';
COMMENT ON COLUMN rif40_investigations.numer_tab IS 'Numerator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF.';
COMMENT ON COLUMN rif40_investigations.mh_test_type IS 'Mantel-Haenszel test type: &quot;No test&quot;, &quot;Comparison Areas&quot;, &quot;Unexposed Area&quot;.';
COMMENT ON COLUMN rif40_investigations.inv_description IS 'Description of investigation';
COMMENT ON COLUMN rif40_investigations.classifier IS 'Maps classifier. EQUAL_INTERVAL: each classifier band represents the same sized range and intervals change based on max an min, JENKS: Jenks natural breaks, QUANTILE: equiheight (even number) distribution, STANDARD_DEVIATION, UNIQUE_INTERVAL: a version of EQUAL_INTERVAL that takes into account unique values, &lt;BESPOKE&gt;; default QUANTILE. &lt;BESPOKE&gt; classification bands are defined in: RIF40_CLASSFIER_BANDS, RIF40_CLASSFIER_BAND_NAMES and are used to create maps that are comparable accross investigations';
COMMENT ON COLUMN rif40_investigations.classifier_bands IS 'Map classifier bands; default 5. Must be between 2 and 20';
COMMENT ON COLUMN rif40_investigations.investigation_state IS 'Investigation state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.';
*/