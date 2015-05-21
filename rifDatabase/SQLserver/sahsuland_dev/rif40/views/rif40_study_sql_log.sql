USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_sql_log]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_study_sql_log]
END
GO

CREATE VIEW [rif40].[rif40_study_sql_log] AS 
 SELECT c.username,
    c.study_id,
    c.statement_type,
    c.statement_number,
    c.log_message,
    c.audsid,
    c.log_sqlcode,
    c.[rowcount],
    c.start_time,
    c.elapsed_time
   FROM [rif40].[t_rif40_study_sql_log] c
     LEFT JOIN [rif40].[rif40_study_shares] s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql_log] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql_log] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_study_sql_log
  IS 'Log of SQL executed for study.';
COMMENT ON COLUMN rif40_study_sql_log.username IS 'Username';
COMMENT ON COLUMN rif40_study_sql_log.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_study_sql_log.statement_type IS 'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK';
COMMENT ON COLUMN rif40_study_sql_log.statement_number IS 'Statement number';
COMMENT ON COLUMN rif40_study_sql_log.log_message IS 'Log message';
COMMENT ON COLUMN rif40_study_sql_log.audsid IS 'Link to Oracle audit subsystem. On Postgres is &quot;backend PID.Julian day.Seconds from midnight.uSeconds (backend start time)&quot;. This can be correlated to the logging messages.';
COMMENT ON COLUMN rif40_study_sql_log.log_sqlcode IS 'SQL code (SQLSTATE hex code in Postgres)';
COMMENT ON COLUMN rif40_study_sql_log.rowcount IS 'Row count';
COMMENT ON COLUMN rif40_study_sql_log.start_time IS 'Start time';
COMMENT ON COLUMN rif40_study_sql_log.elapsed_time IS 'Elapsed time';
*/
