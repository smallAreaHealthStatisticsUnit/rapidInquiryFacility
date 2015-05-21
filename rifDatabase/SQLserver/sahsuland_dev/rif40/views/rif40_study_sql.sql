USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_sql]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_study_sql]
END
GO


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
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
 GO
 
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_study_sql
  IS 'SQL created for study execution.';
COMMENT ON COLUMN rif40_study_sql.username IS 'Username';
COMMENT ON COLUMN rif40_study_sql.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_study_sql.statement_type IS 'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK';
COMMENT ON COLUMN rif40_study_sql.statement_number IS 'Statement number';
COMMENT ON COLUMN rif40_study_sql.sql_text IS 'SQL text';
COMMENT ON COLUMN rif40_study_sql.line_number IS 'Line number';
COMMENT ON COLUMN rif40_study_sql.status IS 'Status: C(reated) or R(un)';
*/