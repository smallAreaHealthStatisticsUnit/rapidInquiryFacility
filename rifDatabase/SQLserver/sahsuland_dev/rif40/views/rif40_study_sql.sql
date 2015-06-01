USE [sahsuland_dev]
GO

--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_sql]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_study_sql]
END
GO

--view definition
CREATE VIEW [rif40].[rif40_study_sql] AS 
 SELECT c.username,
    c.study_id,
    c.statement_type,
    c.statement_number,
    c.sql_text,
    c.line_number,
    c.status
   FROM [rif40].[t_rif40_study_sql] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL created for study execution.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql', @level2type=N'COLUMN',@level2name=N'statement_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement number', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql', @level2type=N'COLUMN',@level2name=N'statement_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL text', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql', @level2type=N'COLUMN',@level2name=N'sql_text'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Line number', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql', @level2type=N'COLUMN',@level2name=N'line_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Status: C(reated) or R(un)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql', @level2type=N'COLUMN',@level2name=N'status'
GO
