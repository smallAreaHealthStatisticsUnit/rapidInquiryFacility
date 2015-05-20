USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql_log]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_study_sql_log]
END
GO


CREATE TABLE [rif40].[t_rif40_study_sql_log](
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[study_id] [numeric](8, 0) NOT NULL,
	[statement_type] [varchar](30) NOT NULL,
	[statement_number] [numeric](6, 0) NOT NULL,
	[log_message] [varchar](4000) NOT NULL,
	[log_sqlcode] [varchar](5) NOT NULL,
	[rowcount] [numeric](12, 0) NOT NULL,
	[start_time] [datetimeoffset](6) NOT NULL,
	[elapsed_time] [numeric](10, 4) NOT NULL,
	[audsid] [varchar](90) NOT NULL,
 CONSTRAINT [t_rif40_study_sql_log_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[statement_number] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_study_sqllog_stdid_fk] FOREIGN KEY([STUDY_ID])
	REFERENCES [rif40].[t_rif40_studies] ([study_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [statement_type_ck1] CHECK  
	(([statement_type]='DENOMINATOR_CHECK' OR [statement_type]='NUMERATOR_CHECK' OR [statement_type]='POST_INSERT' OR [statement_type]='INSERT' OR [statement_type]='CREATE'))	
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql_log] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql_log] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_study_sql_log
  IS 'Log of SQL executed for study.';
COMMENT ON COLUMN t_rif40_study_sql_log.username IS 'Username';
COMMENT ON COLUMN t_rif40_study_sql_log.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_study_sql_log.statement_type IS 'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK';
COMMENT ON COLUMN t_rif40_study_sql_log.statement_number IS 'Statement number';
COMMENT ON COLUMN t_rif40_study_sql_log.log_message IS 'Log message';
COMMENT ON COLUMN t_rif40_study_sql_log.log_sqlcode IS 'SQL code (SQLSTATE hex code in Postgres)';
COMMENT ON COLUMN t_rif40_study_sql_log.rowcount IS 'Row count';
COMMENT ON COLUMN t_rif40_study_sql_log.start_time IS 'Start time';
COMMENT ON COLUMN t_rif40_study_sql_log.elapsed_time IS 'Elapsed time';
COMMENT ON COLUMN t_rif40_study_sql_log.audsid IS 'Link to Oracle audit subsystem. On Postgres is &quot;backend PID.Julian day.Seconds from midnight.uSeconds (backend start time)&quot;. This can be correlated to the logging messages.';
*/

CREATE INDEX t_rif40_study_sqllog_type_bm
  ON [rif40].[t_rif40_study_sql_log](statement_type)
GO
CREATE INDEX t_rif40_study_sqllog_uname_bm
  ON [rif40].[t_rif40_study_sql_log](username)
GO

--trigger - NOT DONE