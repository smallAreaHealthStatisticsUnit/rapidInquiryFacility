
--drop view if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_sql_log]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_study_sql_log]
END
GO

--view definition
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
  IS_MEMBER(N'[rif_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
GO

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql_log] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_sql_log] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Log of SQL executed for study.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'statement_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement number', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'statement_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Log message', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'log_message'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Link to Oracle audit subsystem. On Postgres is &quot;backend PID.Julian day.Seconds from midnight.uSeconds (backend start time)&quot;. This can be correlated to the logging messages.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'audsid'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL code (SQLSTATE hex code in Postgres)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'log_sqlcode'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Row count', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'rowcount'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Start time', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'start_time'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Elapsed time', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'elapsed_time'
GO
